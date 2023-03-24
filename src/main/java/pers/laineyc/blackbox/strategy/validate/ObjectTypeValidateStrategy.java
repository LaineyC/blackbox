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
    public Object validateAndBuildValue(boolean checkValue, VerifiableValue verifiableValue, Object value, String path) {
        String name = verifiableValue.getName();
        Object defaultValue = verifiableValue.getValue();
        ValueType type = verifiableValue.getType();
        List<Param> properties = verifiableValue.getProperties();
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
            if (!(value instanceof Map mapValue)) {
                throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
            }

            if (validation != null) {
                String script = validation.getScript();
                Boolean notEmpty = validation.getNotEmpty();
                Double max = validation.getMax();
                Double min = validation.getMin();

                int size = mapValue.size();
                if(Boolean.TRUE.equals(notEmpty) && size == 0) {
                    throw new CommonException(buildPath(path, name) + "不能为空对象");
                }

                if(Objects.nonNull(min) && size < min) {
                    throw new CommonException(buildPath(path, name) + "属性数量不能小于" + min);
                }

                if(Objects.nonNull(max) && size > max) {
                    throw new CommonException(buildPath(path, name) + "属性数量不能大于" + max);
                }

                validScript(script, Collections.singletonMap("self", value), buildPath(path, name));
            }

            if(CollectionUtils.isEmpty(properties)) {
                return value;
            }

            Map<String, Object> returnValue = new HashMap<>();
            properties.forEach(propertyParam -> {
                String propertyParamName = propertyParam.getName();
                ValueType propertyParamType = propertyParam.getType();
                ValidateStrategy strategy = ValidateStrategy.getStrategy(propertyParamType);
                Object v = strategy.validateAndBuildValue(checkValue, propertyParam, mapValue.get(propertyParamName), buildPath(path, name));
                returnValue.put(propertyParamName, v);
            });

            return returnValue;
        }
    }

}
