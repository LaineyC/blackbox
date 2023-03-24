package pers.laineyc.blackbox.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.enums.Monitor;
import java.util.ArrayList;
import java.util.List;

@Data
public class Exporter {

    @Pattern(regexp = Constant.NAME_PATTERN, message = "Exporter.name" + Constant.NAME_PATTERN_MESSAGE)
    @NotBlank(message = "Exporter.name不能为空")
    private String name;

    @NotEmpty(message = "Exporter.monitors不能为空")
    private List<@NotNull(message = "Exporter.monitors元素不能为空") Monitor> monitors;

    @Valid
    //@NotEmpty(message = "Exporter.params不能为空")
    private List<@NotNull(message = "Exporter.params元素不能为空") Param> params = new ArrayList<>();

    @Valid
    @NotNull(message = "Exporter.meters不能为空")
    private Meters meters;

    @Valid
    @NotEmpty(message = "Exporter.scripts不能为空")
    private List<@NotNull(message = "Exporter.scripts元素不能为空") Script> scripts = new ArrayList<>();

    @Valid
    //@NotEmpty(message = "Exporter.pipelines不能为空")
    private List<@NotNull(message = "Exporter.pipelines元素不能为空") Pipeline> pipelines = new ArrayList<>();

    @Valid
    private List<@NotNull(message = "Exporter.triggers元素不能为空") Trigger> triggers = new ArrayList<>();

    @Valid
    @NotEmpty(message = "Exporter.collectors不能为空")
    private List<@NotNull(message = "Exporter.collectors元素不能为空") Collector> collectors = new ArrayList<>();

    @Valid
    private Extensions extensions;

}
