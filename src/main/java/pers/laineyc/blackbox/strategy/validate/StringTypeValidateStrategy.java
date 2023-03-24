package pers.laineyc.blackbox.strategy.validate;

import org.springframework.util.StringUtils;
import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.Param;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class StringTypeValidateStrategy extends ValidateStrategy {

    public StringTypeValidateStrategy() {
        register(ValueType.STRING);
    }

    @Override
    public Object validateAndBuildValue(boolean checkValue, VerifiableValue verifiableValue, Object value, String path) {
        String name = verifiableValue.getName();
        Object defaultValue = verifiableValue.getValue();
        ValueType type = verifiableValue.getType();
        Param.Validation validation = verifiableValue.getValidation();

        if(value == null) {
            value = defaultValue;
        }

        if(value == null) {
            if(checkValue && Objects.nonNull(validation) && Boolean.TRUE.equals(validation.getNotNull())) {
                throw new CommonException(buildPath(path, name) + "必填");
            }

            return null;
        }
        else {
            if (!(value instanceof String stringValue)) {
                throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
            }

            if(validation == null) {
                return value;
            }

            String script = validation.getScript();
            Boolean notEmpty = validation.getNotEmpty();
            Boolean notBlank = validation.getNotBlank();
            String pattern = validation.getPattern();
            Double max = validation.getMax();
            Double min = validation.getMin();

            int length = stringValue.length();
            if(Boolean.TRUE.equals(notEmpty) && length == 0) {
                throw new CommonException(buildPath(path, name) + "不能为空字符串");
            }

            if(Objects.nonNull(min) && length < min) {
                throw new CommonException(buildPath(path, name) + "长度不能小于" + min);
            }

            if(Objects.nonNull(max) && length > max) {
                throw new CommonException(buildPath(path, name) + "长度不能大于" + max);
            }

            if(Boolean.TRUE.equals(notBlank) && !StringUtils.hasText(stringValue)) {
                throw new CommonException(buildPath(path, name) + "不能全为空字符组成");
            }

            if(Objects.nonNull(pattern) && !Pattern.matches(pattern, stringValue)) {
                throw new CommonException(buildPath(path, name) + "不符合正则格式" + pattern);
            }

            validScript(script, Collections.singletonMap("self", value), buildPath(path, name));

            return value;
        }
    }

}
