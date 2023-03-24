package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.context.CollectorContext;
import pers.laineyc.blackbox.enums.Monitor;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.*;
import pers.laineyc.blackbox.param.CaseCollectParam;
import pers.laineyc.blackbox.strategy.validate.ValidateStrategy;
import pers.laineyc.blackbox.util.YamlUtil;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CaseService {
    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CaseContextService caseContextService;

    @Autowired
    private CollectorContextService collectorContextService;

    @Autowired
    private LoadLockService loadLockService;

    @Autowired
    private ValidatorService validatorService;

    private Map<String, Case> caseCache = new HashMap<>();

    public Case load(File caseDir){
        Case case_;
        try (FileReader fileReader = new FileReader(new File(caseDir, Constant.CASE_FILE))){
            case_ = YamlUtil.getYaml().loadAs(fileReader, Case.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        validate(case_, caseDir);

        caseCache.put(case_.getName(), case_);

        String exporterName = case_.getExporter();
        Exporter exporter = exporterService.getByName(exporterName);
        List<Collector> collectors = exporter.getCollectors();
        Map<String, Collector> collectCache = collectors.stream().collect(Collectors.toMap(Collector::getName, v -> v, (v1, v2) -> v1));
        List<String> collectorNames = case_.getCollectors();

        for(String collectorName : collectorNames) {
            Collector collector = collectCache.get(collectorName);
            if(collector == null){
                continue;
            }

            String cron = collector.getCron();
            if (!StringUtils.hasText(cron)) {
                continue;
            }

            scheduleService.addCronTask(new CronTask(() -> {
                CollectorContext collectorContext = collectorContextService.createContext(exporter, case_);
                List<String> collectNames = new ArrayList<>();
                collectNames.add(collectorName);
                exporterService.collect(collectorContext, collectNames);
            }, cron));
        }

        caseContextService.createContext(exporter, case_);

        return case_;
    }

    public Case getByName(String name){
        return caseCache.get(name);
    }

    public String collect(CaseCollectParam param){
        validatorService.validThrowException(param);

        return loadLockService.readAction(() -> {
            String name = param.getName();
            Case case_ = getByName(name);
            if(case_ == null) {
                throw new CommonException("case[" + name + "]不存在");
            }
            String exporterName = case_.getExporter();
            Exporter exporter = exporterService.getByName(exporterName);

            CollectorContext collectorContext = collectorContextService.createContext(exporter, case_);
            List<Collector> collectors = exporter.getCollectors();
            Map<String, Collector> collectCache = collectors.stream().collect(Collectors.toMap(Collector::getName, v -> v, (v1, v2) -> v1));

            List<String> collectorNames = case_.getCollectors();
            List<String> collectNames = new ArrayList<>();
            for(String collectorName : collectorNames) {
                Collector collector = collectCache.get(collectorName);
                if (collector == null) {
                    continue;
                }

                String cron = collector.getCron();
                if (StringUtils.hasText(cron)) {
                    continue;
                }

                collectNames.add(collectorName);
            }

            return exporterService.collect(collectorContext, collectNames);
        });
    }

    public void clear(){
        caseCache = new HashMap<>();
    }

    private void validate(Case case_, File caseDir){
        validatorService.validThrowException(case_);

        String name = case_.getName();
        Case byNameCase = getByName(name);
        if(byNameCase != null) {
            throw new CommonException("Case.name[" + name + "]已存在");
        }

        String caseDirName = caseDir.getName();
        if(!Objects.equals(name, caseDirName)) {
            throw new CommonException("Case.name[" + name + "]与用例目录[" + caseDirName + "]名称不一致");
        }

        String exporterName = case_.getExporter();
        Exporter exporter = exporterService.getByName(exporterName);
        if(exporter == null) {
            throw new CommonException("Case.exporter[" + exporterName + "]不存在");
        }

        Map<String, Object> params = exporterService.buildParamValue(exporter, case_, "Case." + name + ".params");
        case_.setParams(params);

        Monitor monitor = case_.getMonitor();
        if(!exporter.getMonitors().contains(monitor)) {
            throw new CommonException("Case.monitor[" + monitor + "]在Exporter.monitors不支持");
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

}
