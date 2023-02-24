package pers.laineyc.blackbox.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.enums.ValueType;

import java.util.ArrayList;
import java.util.List;

@Data
public class Param {

    @Pattern(regexp = Constant.KEY_PATTERN, message = "Param.name" + Constant.KEY_PATTERN_MESSAGE)
    @NotBlank(message = "Param.name不能为空")
    private String name;

    private Object value;

    @NotNull(message = "Param.required不能为空")
    private Boolean required = true;

    @NotNull(message = "Param.type不能为空")
    private ValueType type = ValueType.STRING;

    @Valid
    private List<@NotNull(message = "Param.nest元素不能为空") Param> nest = new ArrayList<>();

}

