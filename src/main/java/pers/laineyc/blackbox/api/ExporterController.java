package pers.laineyc.blackbox.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pers.laineyc.blackbox.param.ExporterCollectParam;
import pers.laineyc.blackbox.service.ExporterService;

@Slf4j
@Controller
public class ExporterController {

    @Autowired
    private ExporterService exporterService;

    @PostMapping(path="/api/v1/exporter/collect", produces="text/plain")
    @ResponseBody
    public String collect( @RequestBody ExporterCollectParam param) {
        return exporterService.collect(param);
    }

}
