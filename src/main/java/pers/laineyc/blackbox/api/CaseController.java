package pers.laineyc.blackbox.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pers.laineyc.blackbox.context.CaseContext;
import pers.laineyc.blackbox.param.CaseCollectParam;
import pers.laineyc.blackbox.service.CaseContextService;
import pers.laineyc.blackbox.service.CaseService;

@Slf4j
@Controller
public class CaseController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private CaseContextService caseContextService;

    @GetMapping(path="/api/v1/case/collect/{caseName}")
    @ResponseBody
    public ResponseMessage<String> collect(@PathVariable String caseName) {
        CaseCollectParam param = new CaseCollectParam();
        param.setName(caseName);
        caseService.collect(param);
        return ResponseMessage.ok();
    }

    @GetMapping(path="/api/v1/case/prometheus/{caseName}", produces="text/plain")
    @ResponseBody
    public String prometheus(@PathVariable String caseName)  {
        CaseCollectParam param = new CaseCollectParam();
        param.setName(caseName);
        caseService.collect(param);

        CaseContext caseContext = caseContextService.getByUri(caseName);
        return caseContext.scrape();
    }

}
