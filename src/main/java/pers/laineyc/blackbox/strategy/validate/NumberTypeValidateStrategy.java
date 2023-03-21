package pers.laineyc.blackbox.strategy.validate;

import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.Param;

import java.util.Objects;

public class NumberTypeValidateStrategy extends ValidateStrategy {

    public NumberTypeValidateStrategy() {
        register(ValueType.NUMBER);
    }

    @Override
    public Object validateAndBuildValue(VerifiableValue verifiableValue, Object value, String path) {
        String name = verifiableValue.getName();
        Object defaultValue = verifiableValue.getValue();
        ValueType type = verifiableValue.getType();
        Param.Validation validation = verifiableValue.getValidation();

        if(value == null) {
            value = defaultValue;
        }

        if(Objects.nonNull(value) && !(value instanceof Number)) {
            throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
        }

        if(validation == null) {
            return value;
        }

        if(value == null) {
            if(Boolean.TRUE.equals(validation.getRequired())) {
                throw new CommonException(buildPath(path, name) + "必填");
            }

            return null;
        }

        return value;
    }
}
