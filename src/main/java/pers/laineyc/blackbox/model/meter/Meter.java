package pers.laineyc.blackbox.model.meter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;

import java.util.ArrayList;
import java.util.List;

@Data
public class Meter {

    @Pattern(regexp = Constant.METRIC_PATTERN, message = "Meter.name" + Constant.METRIC_PATTERN_MESSAGE)
    @NotBlank(message = "Meter.name不能为空")
    private String name;

    private String desc;

    @Pattern(regexp = Constant.KEY_PATTERN, message = "Meter.unit" + Constant.KEY_PATTERN_MESSAGE)
    private String unit;

    private List<
            @NotNull(message = "Meter.tags元素不能为空")
            @Pattern(regexp = Constant.KEY_PATTERN, message = "Meter.tags元素" + Constant.KEY_PATTERN_MESSAGE)
            String> tags = new ArrayList<>();

}

