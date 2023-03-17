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

    @GetMapping(path="/api/v1/case/{case}/collect")
    @ResponseBody
    public ResponseMessage<String> collect(@PathVariable("case") String caseName) {
        CaseCollectParam param = new CaseCollectParam();
        param.setName(caseName);
        caseService.collect(param);
        return ResponseMessage.ok();
    }

    @GetMapping(path="/api/v1/case/{case}/prometheus", produces="text/plain")
    @ResponseBody
    public String prometheus(@PathVariable("case") String caseName)  {
        CaseContext caseContext = caseContextService.getByUri(caseName);
        if(caseContext == null) {
            CaseCollectParam param = new CaseCollectParam();
            param.setName(caseName);
            caseService.collect(param);
            caseContext = caseContextService.getByUri(caseName);
        }
        return caseContext.scrape();
    }

}
