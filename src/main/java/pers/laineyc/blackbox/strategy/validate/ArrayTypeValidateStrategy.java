package pers.laineyc.blackbox.strategy.validate;

import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.Param;
import java.util.*;

public class ArrayTypeValidateStrategy extends ValidateStrategy {

    public ArrayTypeValidateStrategy() {
        register(ValueType.ARRAY);
    }

    @Override
    public Object validateAndBuildValue(boolean checkValue, VerifiableValue verifiableValue, Object value, String path) {
        String name = verifiableValue.getName();
        Object defaultValue = verifiableValue.getValue();
        ValueType type = verifiableValue.getType();
        Param.Validation validation = verifiableValue.getValidation();
        Param.Item item = verifiableValue.getItem();

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
            if (!(value instanceof List listValue)) {
                throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
            }

            if (validation != null) {
                String script = validation.getScript();
                Boolean notEmpty = validation.getNotEmpty();
                Double max = validation.getMax();
                Double min = validation.getMin();

                int size = listValue.size();
                if(Boolean.TRUE.equals(notEmpty) && size == 0) {
                    throw new CommonException(buildPath(path, name) + "不能为空数组");
                }

                if(Objects.nonNull(min) && size < min) {
                    throw new CommonException(buildPath(path, name) + "长度不能小于" + min);
                }

                if(Objects.nonNull(max) && size > max) {
                    throw new CommonException(buildPath(path, name) + "长度不能大于" + max);
                }

                validScript(script, Collections.singletonMap("self", value), buildPath(path, name));
            }

            if (item == null) {
                return value;
            }

            List<Object> returnValue = new ArrayList<>();
            ValidateStrategy strategy = ValidateStrategy.getStrategy(item.getType());
            for (int i = 0, n = listValue.size(); i < n; i++) {
                item.setName(String.valueOf(i));
                Object v = strategy.validateAndBuildValue(checkValue, item, listValue.get(i), buildPath(path, name));
                returnValue.add(v);
            }

            return returnValue;
        }
    }

}
