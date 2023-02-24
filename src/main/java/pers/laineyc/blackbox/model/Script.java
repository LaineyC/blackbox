package pers.laineyc.blackbox.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;

@Data
public class Script {

    @Pattern(regexp = Constant.NAME_PATTERN, message = "Script.name" + Constant.NAME_PATTERN_MESSAGE)
    @NotBlank(message = "Script.name不能为空")
    private String name;

    @NotBlank(message = "Script.file不能为空")
    private String file;

}

