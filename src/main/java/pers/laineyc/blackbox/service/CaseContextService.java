package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.config.Properties;
import pers.laineyc.blackbox.model.Case;
import pers.laineyc.blackbox.model.Exporter;
import pers.laineyc.blackbox.model.Logging;
import pers.laineyc.blackbox.tool.CaseTool;
import pers.laineyc.blackbox.context.CaseContext;
import java.io.File;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@DependsOn({"monitorStrategyConfigurer"})
@Component
public class CaseContextService {

    @Autowired
    private Properties properties;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private HttpClientService httpClientService;

    private Map<String, CaseContext> caseContextCache = new ConcurrentHashMap<>();

    public CaseContext createContext(Exporter exporter, Case case_){
        return caseContextCache.computeIfAbsent(CaseTool.buildUri(case_), key -> new CaseContext(exporter, case_, newLogger(case_), httpClientService.getHttpClient()));
    }

    public CaseContext newContext(Exporter exporter, Case case_, Logger logger, HttpClient httpClient){
        return new CaseContext(exporter, case_, logger, httpClient);
    }

    public CaseContext getByUri(String uri){
        return caseContextCache.get(uri);
    }

    public void clear(){
        Map<String, CaseContext> old = caseContextCache;
        caseContextCache = new ConcurrentHashMap<>();
        old.forEach((s, caseContext) -> loggerService.stop(caseContext.getLogger()));
    }

    private Logger newLogger(Case case_) {
        String caseName = case_.getName();
        String rootPath = properties.getRootPath();
        File casesDir = new File(rootPath, Constant.CASES_DIR);
        File caseDir = new File(casesDir, caseName);
        File logsDir = new File(caseDir, Constant.LOGS_DIR);
        Logging logging = case_.getLogging();
        return loggerService.newRollingFileLogger(caseName, logsDir, logging);
    }

}
