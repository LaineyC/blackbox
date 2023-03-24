package pers.laineyc.blackbox.strategy.validate;

import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.Param;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NumberTypeValidateStrategy extends ValidateStrategy {

    public NumberTypeValidateStrategy() {
        register(ValueType.NUMBER);
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
            if (!(value instanceof Number numberValue)) {
                throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
            }

            if(validation == null) {
                return value;
            }

            String script = validation.getScript();
            Double max = validation.getMax();
            Double min = validation.getMin();

            if(Objects.nonNull(min) && numberValue.doubleValue() < min) {
                throw new CommonException(buildPath(path, name) + "不能小于" + min);
            }

            if(Objects.nonNull(max) && numberValue.doubleValue() > max) {
                throw new CommonException(buildPath(path, name) + "不能大于" + max);
            }

            validScript(script, Collections.singletonMap("self", value), buildPath(path, name));

            return value;
        }
    }
}
