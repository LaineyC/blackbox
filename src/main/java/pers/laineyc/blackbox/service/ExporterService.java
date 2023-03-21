package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.config.Properties;
import pers.laineyc.blackbox.context.CollectorContext;
import pers.laineyc.blackbox.context.ContextApi;
import pers.laineyc.blackbox.enums.*;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.*;
import pers.laineyc.blackbox.model.meter.Counter;
import pers.laineyc.blackbox.model.meter.Gauge;
import pers.laineyc.blackbox.model.meter.Summary;
import pers.laineyc.blackbox.model.meter.Timer;
import pers.laineyc.blackbox.param.ExporterCollectParam;
import pers.laineyc.blackbox.context.CaseContext;
import pers.laineyc.blackbox.strategy.validate.ValidateStrategy;
import pers.laineyc.blackbox.util.ThreadLocalUtil;
import pers.laineyc.blackbox.util.YamlUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@DependsOn({"validateStrategyConfigurer"})
@Component
public class ExporterService {

    @Autowired
    private Properties properties;

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private CollectorContextService collectorContextService;

    @Autowired
    private ThreadPoolService threadPoolService;

    @Autowired
    private LoadLockService loadLockService;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private ScriptService scriptService;

    private Map<String, Exporter> exporterCache = new HashMap<>();

    public Exporter load(File exporterDir){
        Exporter exporter;
        try (FileReader fileReader = new FileReader(new File(exporterDir, Constant.EXPORTER_FILE))){
            exporter = YamlUtil.getYaml().loadAs(fileReader, Exporter.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        validate(exporter, exporterDir);

        exporterCache.put(exporter.getName(), exporter);

        return exporter;
    }

    public Exporter getByName(String name){
        return exporterCache.get(name);
    }

    public String collect(ExporterCollectParam param){
        String message = validatorService.validReturnMessage(param);
        return loadLockService.readAction(() -> {
            Case case_ = param.getCase_();

            Logging logging = case_ == null ? null : case_.getLogging();
            if(logging == null) {
                logging = new Logging();
                logging.setLevel(LoggLevel.ALL);
            }

            Logger logger = loggerService.newByteArrayOutputStreamLogger("exporter-case", logging);
            ThreadLocalUtil.set(Constant.LOGGER, logger);

            if(message != null) {
                throw new CommonException(message);
            }

            case_.setLogging(logging);

            validateCase(case_);

            Exporter exporter = getByName(case_.getExporter());

            CollectorContext collectorContext = collectorContextService.newContext(exporter, case_, logger);

            return collect(collectorContext, case_.getCollectors());
        });
    }

    public String collect(CollectorContext collectorContext, List<String> collectorNames){
        boolean isDebug = Boolean.TRUE.equals(collectorContext.getIsDebug());
        CaseContext caseContext = collectorContext.getCaseContext();
        Exporter exporter = caseContext.getExporter();
        Case case_ = caseContext.getCase();
        List<Collector> collectors = exporter.getCollectors().stream().filter(collector -> collectorNames.contains(collector.getName())).toList();
        Logger logger = caseContext.getLogger();

        String exporterName = exporter.getName();
        List<Script> scripts = exporter.getScripts();
        List<Pipeline> pipelines = exporter.getPipelines();
        List<Trigger> triggers = exporter.getTriggers();
        Map<String, Pipeline> pipelineCache = pipelines.stream().collect(Collectors.toMap(Pipeline::getName, v -> v, (v1, v2) -> v2));
        Map<String, Script> scriptCache = scripts.stream().collect(Collectors.toMap(Script::getName, v -> v, (v1, v2) -> v2));

        Map<String, Object> params = case_.getParams();

        Map<String, Object> bindings = new HashMap<>();
        Map<String, Object> root = new HashMap<>();
        bindings.put("$", root);
        //root.put("exporter", caseContext.getExporter());
        //root.put("case", caseContext.getCase());
        root.put("params", params);
        root.put("vars", new ContextApi.Scope());
        root.put("stores", caseContext.getStores());
        root.put("meters", caseContext.getMeters());
        root.put("log", caseContext.getLog());
        root.put("http", caseContext.getHttp());
        root.put("shell", caseContext.getShell());
        root.put("json", caseContext.getJson());

        String rootPath = properties.getRootPath();
        File exportersDir = new File(rootPath, Constant.EXPORTERS_DIR);
        File exporterDir = new File(exportersDir, exporterName);
        File scriptsDir = new File(exporterDir, Constant.SCRIPTS_DIR);

        triggers = triggers == null ? new ArrayList<>() : triggers;
        Map<Event, Map<Time, List<Trigger>>> triggerGrouping = triggers.stream().collect(Collectors.groupingBy(Trigger::getEvent, Collectors.groupingBy(Trigger::getTime)));
        Map<Time, List<Trigger>> collectTriggers = triggerGrouping.getOrDefault(Event.COLLECT, new HashMap<>());
        List<Trigger> beforeCollectTriggers = collectTriggers.getOrDefault(Time.BEFORE, new ArrayList<>());
        List<Trigger> afterCollectTriggers = collectTriggers.getOrDefault(Time.AFTER, new ArrayList<>());

        logger.info("Collect start");

        execTriggers(
                beforeCollectTriggers, Time.BEFORE,
                pipelineCache, scriptCache,
                logger, isDebug,
                collectorContext, scriptsDir,
                bindings, exporterName);

        List<CollectorFuture> futures = new ArrayList<>();
        ExecutorService collectorExecutor = threadPoolService.getCollectorExecutor();

        logger.info("Collector collect start");
        for(Collector collector : collectors){
            String collectorName = collector.getName();
            Future<String> future = collectorExecutor.submit(() -> {
                logger.info("----Collector[{}] start", collectorName);
                String pipelineName = collector.getPipeline();
                String scriptName = collector.getScript();

                if(StringUtils.hasText(pipelineName)) {
                    Pipeline pipeline = pipelineCache.get(pipelineName);
                    pipelineService.exec(collectorContext, pipeline, bindings, scriptsDir, scriptCache);
                }
                else if(StringUtils.hasText(scriptName)) {
                    Script script = scriptCache.get(scriptName);
                    logger.info("--------Collector[{}] Script[{}] start", collectorName, scriptName);
                    try (FileReader fileReader = new FileReader(new File(scriptsDir, script.getFile()))){
                        scriptService.eval(bindings, fileReader, Boolean.TRUE.equals(isDebug) ? null : exporterName + "." + scriptName);
                    }
                    catch (Exception e) {
                        logger.error("--------Collector[{}] Script[{}] error", collectorName, scriptName, e);
                    }
                    logger.info("--------Collector[{}] Script[{}] end", collectorName, scriptName);
                }

                return collectorName;
            });
            futures.add(new CollectorFuture(future, collectorName));
        }
        for(CollectorFuture collectorFuture: futures) {
            String collectorName = collectorFuture.collector();
            Future<String> future = collectorFuture.future();
            try {
                future.get(10, TimeUnit.SECONDS);
                logger.info("----Collector[{}] end", collectorName);
            }
            catch (Exception e) {
                logger.error("----Collector[{}] error", collectorName, e);
            }
        }
        logger.info("Collector collect end");

        execTriggers(
                afterCollectTriggers, Time.AFTER,
                pipelineCache, scriptCache,
                logger, isDebug,
                collectorContext, scriptsDir,
                bindings, exporterName);

        logger.info("Collect end\r\n");

        if(logger.isDebugEnabled()) {
            logger.debug("Exporter Yaml: \r\n{}", YamlUtil.getYaml().dumpAsMap(exporter));
            logger.debug("Case Yaml: \r\n{}", YamlUtil.getYaml().dumpAsMap(case_));
            logger.debug("Metric Result: \r\n{}", collectorContext.getCaseContext().scrape());
        }

        return Boolean.TRUE.equals(collectorContext.getIsDebug()) ? loggerService.getByteArrayOutputStreamLoggerInfo(logger) : "";
    }

    public void clear(){
        exporterCache = new HashMap<>();
    }

    private void execTriggers(List<Trigger> collectTriggers, Time time,
                              Map<String, Pipeline> pipelineCache, Map<String, Script> scriptCache,
                              Logger logger, boolean isDebug,
                              CollectorContext collectorContext, File scriptsDir,
                              Map<String, Object> bindings, String exporterName) {
        logger.info("Trigger {} COLLECT start", time);
        for(Trigger trigger : collectTriggers){
            String pipelineName = trigger.getPipeline();
            String scriptName = trigger.getScript();
            if(StringUtils.hasText(pipelineName)) {
                Pipeline pipeline = pipelineCache.get(pipelineName);
                logger.info("----Trigger {} COLLECT Pipeline[{}] start", time, pipelineName);
                pipelineService.exec(collectorContext, pipeline, bindings, scriptsDir, scriptCache);
                logger.info("----Trigger {} COLLECT Pipeline[{}] end", time, pipelineName);
            }
            else if(StringUtils.hasText(scriptName)) {
                Script script = scriptCache.get(scriptName);
                logger.info("----Trigger {} COLLECT Script[{}] start", time, scriptName);
                try (FileReader fileReader = new FileReader(new File(scriptsDir, script.getFile()))) {
                    scriptService.eval(bindings, fileReader, Boolean.TRUE.equals(isDebug) ? null : exporterName + "." + scriptName);
                }
                catch (Exception e) {
                    logger.error("----Trigger {} COLLECT Script[{}] error", time, scriptName, e);
                }
                logger.info("----Trigger {} COLLECT Script[{}] end", time, scriptName);
            }
        }
        logger.info("Trigger {} COLLECT end", time);
    }

    private void validate(Exporter exporter, File exporterDir){
        validatorService.validThrowException(exporter);

        String name = exporter.getName();
        Exporter byNameExporter = getByName(name);
        if(byNameExporter != null) {
            throw new CommonException("Exporter.name[" + name + "]已存在");
        }

        String exporterDirName = exporterDir.getName();
        if(!Objects.equals(name, exporterDirName)) {
            throw new CommonException("Exporter.name[" + name + "]与采集器目录[" + exporterDirName + "]名称不一致");
        }

        //List<Monitor> monitors = exporter.getMonitors();

        List<Param> params = exporter.getParams();
        if(!CollectionUtils.isEmpty(params)) {
            params.forEach(param -> {
                ValueType type = param.getType();
                ValidateStrategy strategy = ValidateStrategy.getStrategy(type);
                strategy.validateAndBuildValue(param, null, "Exporter." + name + ".params");
            });
        }

        Meters meters = exporter.getMeters();
        List<Counter> counters = meters.getCounters();
        List<Gauge> gauges = meters.getGauges();
        List<Timer> timers = meters.getTimers();
        List<Summary> summaries = meters.getSummaries();
        if(counters == null && gauges == null && timers == null && summaries == null) {
            throw new CommonException("Exporter.meters不能为空");
        }

        List<Script> scripts = exporter.getScripts();
        Map<String, Script> scriptCache = new HashMap<>();
        scripts.forEach(script -> {
            String scriptName = script.getName();
            if(scriptCache.containsKey(scriptName)) {
                throw new CommonException("Script.name[" + scriptName + "]已存在");
            }

            String file = script.getFile();

            File scriptDir = new File(exporterDir, Constant.SCRIPTS_DIR);
            File scriptFile = new File(scriptDir, file);
            if(!scriptFile.exists()) {
                throw new CommonException("Script.file[" + file + "]文件不存在");
            }

            scriptCache.put(scriptName, script);
        });

        List<Pipeline> pipelines = exporter.getPipelines();
        Map<String, Pipeline> pipelineCache = new HashMap<>();
        pipelines.forEach(pipeline -> {
            String pipelineName = pipeline.getName();
            if(pipelineCache.containsKey(pipelineName)) {
                throw new CommonException("Pipeline.name[" + pipelineName + "]已存在");
            }

            List<Pipeline.Stage> stages = pipeline.getStages();
            stages.forEach(stage -> {
                List<String> serial = stage.getSerial();
                List<String> parallel = stage.getParallel();
                if(serial == null && parallel == null) {
                    throw new CommonException("Stage.serial与Stage.parallel不能同时为空");
                }
            });

            pipelineCache.put(pipelineName, pipeline);
        });

        List<Trigger> triggers = exporter.getTriggers();
        if(!CollectionUtils.isEmpty(triggers)) {
            triggers.forEach(trigger -> {
                String pipeline = trigger.getPipeline();
                String script = trigger.getScript();
                if(StringUtils.hasText(pipeline)) {
                    if(!pipelineCache.containsKey(pipeline)) {
                        throw new CommonException("Trigger.pipeline[" + pipeline + "]不存在");
                    }
                }

                else if(StringUtils.hasText(script)) {
                    if(!scriptCache.containsKey(script)) {
                        throw new CommonException("Trigger.script[" + script + "]不存在");
                    }
                }
                else {
                    throw new CommonException("Trigger.pipeline与Trigger.script不能同时为空");
                }
            });
        }

        List<Collector> collectors = exporter.getCollectors();
        Map<String, Collector> collectorCache = new HashMap<>();
        collectors.forEach(collector -> {
            String collectorName = collector.getName();
            if(collectorCache.containsKey(collectorName)) {
                throw new CommonException("Collector.name[" + collectorName + "]已存在");
            }

            String pipeline = collector.getPipeline();
            String script = collector.getScript();
            if(StringUtils.hasText(pipeline)) {
                if(!pipelineCache.containsKey(pipeline)) {
                    throw new CommonException("Collector.pipeline[" + pipeline + "]不存在");
                }
            }
            else if(StringUtils.hasText(script)) {
                if(!scriptCache.containsKey(script)) {
                    throw new CommonException("Collector.script[" + script + "]不存在");
                }
            }
            else{
                throw new CommonException("Trigger.pipeline与Trigger.script不能同时为空");
            }
            collectorCache.put(collectorName, collector);
        });
    }


    public Map<String, Object> buildParamValue(List<Param> exporterParams, Map<String, Object> params, String path) {
        if(CollectionUtils.isEmpty(exporterParams)) {
            return Map.of();
        }

        Map<String, Object> copyParams = new HashMap<>();
        exporterParams.forEach(param -> {
            String paramName = param.getName();
            Object value = params.get(paramName);
            ValueType type = param.getType();
            ValidateStrategy strategy = ValidateStrategy.getStrategy(type);
            value = strategy.validateAndBuildValue(param, value, path);
            copyParams.put(paramName, value);
        });

        return Collections.unmodifiableMap(copyParams);
    }

    private void validateCase(Case case_){
        validatorService.validThrowException(case_);

        String exporterName = case_.getExporter();
        Exporter exporter = getByName(exporterName);
        if(exporter == null) {
            throw new CommonException("Case.exporter[" + exporterName + "]不存在");
        }

        Map<String, Object> params = case_.getParams();
        List<Param> exporterParams = exporter.getParams();
        params = buildParamValue(exporterParams, params, "Case.params");
        case_.setParams(params);

        Monitor monitor = case_.getMonitor();
        if(!exporter.getMonitors().contains(monitor)) {
            throw new CommonException("Case.monitor[" + monitor + "]在Exporter.monitors中不存在");
        }

        // Map<String, String> commonTags = case_.getCommonTags();

        Map<String, Collector> collectCache = exporter.getCollectors().stream().collect(Collectors.toMap(Collector::getName, v -> v));
        List<String> collectors = case_.getCollectors();
        collectors.forEach(collector -> {
            if(!collectCache.containsKey(collector)) {
                throw new CommonException("Case.collector[" + collector + "]不存在");
            }
        });

        // Logging logging = case_.getLogging();
    }

    private record CollectorFuture(Future<String> future, String collector){

    }

}
