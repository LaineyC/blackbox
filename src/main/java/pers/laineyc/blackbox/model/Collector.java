package pers.laineyc.blackbox.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;

@Data
public class Collector {

    @Pattern(regexp = Constant.NAME_PATTERN, message = "Collector.name" + Constant.NAME_PATTERN_MESSAGE)
    @NotNull(message = "Collector.name不能为空")
    private String name;

    private String cron;

    private String pipeline;

    private String script;

}

