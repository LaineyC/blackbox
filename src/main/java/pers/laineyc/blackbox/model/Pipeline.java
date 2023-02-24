package pers.laineyc.blackbox.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pipeline {

    @Pattern(regexp = Constant.NAME_PATTERN, message = "Pipeline.name" + Constant.NAME_PATTERN_MESSAGE)
    @NotBlank(message = "Pipeline.name不能为空")
    private String name;

    @Valid
    @NotEmpty(message = "Pipeline.stages不能为空")
    private List<@NotNull(message = "Pipeline.stages元素不能为空") Stage> stages = new ArrayList<>();

    @Data
    public static class Stage{

        @NotBlank(message = "Stage.title不能为空")
        private String title;

        private List<@NotNull(message = "Stage.serial元素不能为空") String> serial = new ArrayList<>();

        private List<@NotNull(message = "Stage.parallel元素不能为空") String> parallel = new ArrayList<>();

    }
}
