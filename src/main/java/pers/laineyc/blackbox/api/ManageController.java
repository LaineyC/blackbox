package pers.laineyc.blackbox.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pers.laineyc.blackbox.service.LoadService;

@Slf4j
@Controller
public class ManageController {

    @Autowired
    private LoadService loadService;

    @PostMapping(path="/api/v1/manage/reload")
    @ResponseBody
    public ResponseMessage<String> reload() {
        loadService.reload();
        return ResponseMessage.ok();
    }

}
