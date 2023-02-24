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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Case {

    @Pattern(regexp = Constant.NAME_PATTERN, message = "Case.name" + Constant.NAME_PATTERN_MESSAGE)
    @NotBlank(message = "Case.name不能为空")
    private String name;

    @NotBlank(message = "Case.exporter不能为空")
    private String exporter;

    @NotNull(message = "Case.monitor不能为空")
    private Monitor monitor;

    private Map<String, Object> params = new HashMap<>();

    private Map<@Pattern(regexp = Constant.KEY_PATTERN, message = "Case.commonTags key " + Constant.KEY_PATTERN_MESSAGE) String, String> commonTags = new HashMap<>();

    @NotEmpty(message = "Case.collectors不能为空")
    private List<@NotNull(message = "Case.collectors元素不能为空") String> collectors = new ArrayList<>();

    @Valid
    @NotNull(message = "Case.logging不能为空")
    private Logging logging = new Logging();

}
