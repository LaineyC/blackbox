package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pers.laineyc.blackbox.context.CaseContext;
import pers.laineyc.blackbox.context.CollectorContext;
import pers.laineyc.blackbox.model.Case;
import pers.laineyc.blackbox.model.Exporter;

@Slf4j
@Component
public class CollectorContextService {

    @Autowired
    private CaseContextService caseContextService;

    @Autowired
    private HttpClientService httpClientService;

    public CollectorContext createContext(Exporter exporter, Case case_){
        CaseContext caseContext = caseContextService.createContext(exporter, case_);
        CollectorContext collectorContext = new CollectorContext(caseContext, false);
        return collectorContext;
    }

    public CollectorContext newContext(Exporter exporter, Case case_, Logger logger){
        CaseContext caseContext = caseContextService.newContext(exporter, case_, logger, httpClientService.getHttpClient());
        CollectorContext collectorContext = new CollectorContext(caseContext, true);
        return collectorContext;
    }

}
