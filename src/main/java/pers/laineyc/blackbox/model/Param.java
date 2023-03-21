package pers.laineyc.blackbox.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pers.laineyc.blackbox.config.Constant;
import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.strategy.validate.VerifiableValue;

import java.util.ArrayList;
import java.util.List;

@Data
public class Param extends VerifiableValue {

    @Pattern(regexp = Constant.KEY_PATTERN, message = "Param.name" + Constant.KEY_PATTERN_MESSAGE)
    @NotBlank(message = "Param.name不能为空")
    private String name;

    private Object value;

    @NotNull(message = "Param.type不能为空")
    private ValueType type = ValueType.STRING;

    @Valid
    private Validation validation;

    @Valid
    private Item item;

    @Valid
    private List<@NotNull(message = "Param.properties元素不能为空") Param> properties = new ArrayList<>();

    @Data
    public static class Item extends VerifiableValue {

        private String name;

        private Object value;

        @NotNull(message = "Item.type不能为空")
        private ValueType type = ValueType.STRING;

        @Valid
        private Validation validation;

        @Valid
        private Item item;

        @Valid
        private List<@NotNull(message = "Item.properties元素不能为空") Param> properties = new ArrayList<>();
    }

    @Data
    public static class Validation {

        @NotNull(message = "Validation.required不能为空")
        private Boolean required = false;

        @Valid
        private List<@NotNull(message = "Validation.rules元素不能为空") Rule> rules = new ArrayList<>();
    }

    @Data
    public static class Rule {

        @NotBlank(message = "Rule.rule不能为空")
        private String rule;

        @NotBlank(message = "Rule.message不能为空")
        private String message;
    }

}

