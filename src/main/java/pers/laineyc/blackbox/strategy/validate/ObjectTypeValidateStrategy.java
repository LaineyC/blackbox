package pers.laineyc.blackbox.strategy.validate;

import org.springframework.util.CollectionUtils;
import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.Param;

import java.util.*;

public class ObjectTypeValidateStrategy extends ValidateStrategy {

    public ObjectTypeValidateStrategy() {
        register(ValueType.OBJECT);
    }

    @Override
    public Object validateAndBuildValue(VerifiableValue verifiableValue, Object value, String path) {
        String name = verifiableValue.getName();
        Object defaultValue = verifiableValue.getValue();
        ValueType type = verifiableValue.getType();
        List<Param> properties = verifiableValue.getProperties();
        Param.Validation validation = verifiableValue.getValidation();
        if(value == null) {
            value = defaultValue;
        }

        boolean required = Objects.nonNull(validation) && Boolean.TRUE.equals(validation.getRequired());

        if(value == null) {
            if(required) {
                throw new CommonException(buildPath(path, name) + "必填");
            }

            return null;
        }

        if(!(value instanceof Map)) {
            throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
        }

        Map<String, Object> returnValue = new HashMap<>();
        Map<String, Object> mapValue = (Map)value;

        if(CollectionUtils.isEmpty(properties)) {
            return Collections.unmodifiableMap(mapValue);
        }

        properties.forEach(propertyParam -> {
            String propertyParamName = propertyParam.getName();
            ValueType propertyParamType = propertyParam.getType();
            ValidateStrategy strategy = ValidateStrategy.getStrategy(propertyParamType);
            Object v = strategy.validateAndBuildValue(propertyParam, mapValue.get(propertyParamName), buildPath(path, name));
            returnValue.put(propertyParamName, v);
        });

        if(validation == null) {
            return Collections.unmodifiableMap(returnValue);
        }

        // validation code

        return Collections.unmodifiableMap(returnValue);
    }


}
