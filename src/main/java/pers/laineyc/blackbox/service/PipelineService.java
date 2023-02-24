package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pers.laineyc.blackbox.context.CaseContext;
import pers.laineyc.blackbox.context.CollectorContext;
import pers.laineyc.blackbox.model.Exporter;
import pers.laineyc.blackbox.model.Pipeline;
import pers.laineyc.blackbox.model.Script;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public final class PipelineService {

    @Autowired
    private ThreadPoolService threadPoolService;

    @Autowired
    private ScriptService scriptService;

    public void exec(CollectorContext collectorContext, Pipeline pipeline, Map<String, Object> bindings, File scriptsDir, Map<String, Script> scriptCache){
        CaseContext caseContext = collectorContext.getCaseContext();
        Logger logger = caseContext.getLogger();
        boolean isDebug = Boolean.TRUE.equals(collectorContext.getIsDebug());
        Exporter exporter = caseContext.getExporter();
        String exporterName = exporter.getName();
        //Case case_ = caseContext.getCase();
        //String caseName = case_.getName();

        String pipelineName = pipeline.getName();
        List<Pipeline.Stage> stages = pipeline.getStages();
        logger.info("--------Pipeline[{}] start", pipelineName);
        for(Pipeline.Stage stage : stages) {
            String stageTitle = stage.getTitle();
            logger.info("--------Pipeline[{}] Stage[{}] start", pipelineName, stageTitle);
            List<String> serial = stage.getSerial();
            if(!CollectionUtils.isEmpty(serial)) {
                for(String scriptName : serial) {
                    logger.info("--------Pipeline[{}] Stage[{}] serial Script[{}] start", pipelineName, stageTitle, scriptName);
                    try (FileReader fileReader = new FileReader(new File(scriptsDir, scriptCache.get(scriptName).getFile()))){
                        scriptService.eval(bindings, fileReader, isDebug ? null : exporterName + "." + scriptName);
                    }
                    catch (Exception e) {
                        logger.error("--------Pipeline[{}] Stage[{}] serial Script[{}] error", pipelineName, stageTitle, scriptName, e);
                    }
                    logger.info("--------Pipeline[{}] Stage[{}] serial Script[{}] end", pipelineName, stageTitle, scriptName);
                }
                continue;
            }

            List<String> parallel = stage.getParallel();
            if(!CollectionUtils.isEmpty(parallel)) {
                List<ScriptFuture> futures = new ArrayList<>();
                ExecutorService pipelineExecutor = threadPoolService.getPipelineExecutor();
                for(String scriptName : parallel) {
                    Future<String> future = pipelineExecutor.submit(() -> {
                        logger.info("--------Pipeline[{}] Stage[{}] parallel Script[{}] start", pipelineName, stageTitle, scriptName);
                        try (FileReader fileReader = new FileReader(new File(scriptsDir, scriptCache.get(scriptName).getFile()))){
                            scriptService.eval(bindings, fileReader, isDebug ? null : exporterName + "." + scriptName);
                        }
                        catch (Exception e) {
                            logger.error("--------Pipeline[{}] Stage[{}] parallel Script[{}] error", pipelineName, stageTitle, scriptName, e);
                        }
                        return scriptName;
                    });

                    futures.add(new ScriptFuture(future, scriptName));
                }

                for(ScriptFuture scriptFuture : futures) {
                    String scriptName = scriptFuture.script();
                    Future<String> future = scriptFuture.future();
                    try {
                        future.get(10, TimeUnit.SECONDS);
                        logger.info("--------Pipeline[{}] Stage[{}] parallel Script[{}] end", pipelineName, stageTitle, scriptName);
                    }
                    catch (Exception e) {
                        logger.error("--------Pipeline[{}] Stage[{}] parallel Script[{}] error", pipelineName, stageTitle, scriptName, e);
                    }
                }
            }
            logger.info("--------Pipeline[{}] Stage[{}] end", pipelineName, stageTitle);
        }
        logger.info("--------Pipeline[{}] end", pipelineName);
    }

    private record ScriptFuture(Future<String> future, String script){

    }
}
