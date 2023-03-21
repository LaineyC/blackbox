package pers.laineyc.blackbox.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.config.Properties;
import pers.laineyc.blackbox.strategy.monitor.PrometheusMonitorStrategy;

import java.io.File;

@Slf4j
@Component
public class LoadService {

    @Autowired
    private Properties properties;

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private CaseContextService caseContextService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private LoadLockService loadLockService;

    @PostConstruct
    public void init(){
        load();
    }

    public void load(){
        String rootPath = properties.getRootPath();
        File rootDir = new File(rootPath);
        if (!rootDir.exists()) {
            log.warn("rootPath:{} 目录不存在", rootPath);
            return;
        }

        File exportersDir = new File(rootPath, Constant.EXPORTERS_DIR);
        File[] exporterDirList = exportersDir.listFiles();
        if(exporterDirList == null) {
            return;
        }

        for(File exporterDir : exporterDirList) {
            exporterService.load(exporterDir);
        }

        File casesDir = new File(rootPath, Constant.CASES_DIR);
        File[] caseDirList = casesDir.listFiles();
        if (caseDirList == null) {
            return;
        }

        for(File caseDir : caseDirList) {
            caseService.load(caseDir);
        }
    }

    public void reload(){
        loadLockService.writeAction(() -> {
            scheduleService.clear();
            scriptService.clear();
            caseContextService.clear();
            exporterService.clear();
            caseService.clear();
            load();
            return null;
        });
    }
}
