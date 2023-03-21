package pers.laineyc.blackbox.strategy.validate;

import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.exception.CommonException;
import pers.laineyc.blackbox.model.Param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArrayTypeValidateStrategy extends ValidateStrategy {

    public ArrayTypeValidateStrategy() {
        register(ValueType.ARRAY);
    }

    @Override
    public Object validateAndBuildValue(VerifiableValue verifiableValue, Object value, String path) {
        String name = verifiableValue.getName();
        Object defaultValue = verifiableValue.getValue();
        ValueType type = verifiableValue.getType();
        Param.Validation validation = verifiableValue.getValidation();
        Param.Item item = verifiableValue.getItem();

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

        if(!(value instanceof List)) {
            throw new CommonException(buildPath(path, name) + "必须为" + type.name() + "类型");
        }

        List<Object> returnValue = new ArrayList<>();
        List<Object> listValue = (List)value;
        if(item == null) {
            return Collections.unmodifiableList(listValue);
        }

        ValidateStrategy strategy = ValidateStrategy.getStrategy(item.getType());

        for(int i = 0, n = listValue.size(); i < n; i++) {
            item.setName(String.valueOf(i));
            Object v = strategy.validateAndBuildValue(item, listValue.get(i), buildPath(path, name));
            returnValue.add(v);
        }

        if(validation == null) {
            return Collections.unmodifiableList(returnValue);
        }

        // validation code

        return Collections.unmodifiableList(returnValue);
    }

}
