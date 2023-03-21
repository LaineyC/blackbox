package pers.laineyc.blackbox.strategy.validate;

import lombok.Data;
import pers.laineyc.blackbox.enums.ValueType;
import pers.laineyc.blackbox.model.Param;
import java.util.ArrayList;
import java.util.List;

@Data
public class VerifiableValue {

    private String name;

    private Object value;

    private ValueType type = ValueType.STRING;

    private Param.Validation validation;

    private Param.Item item;

    private List<Param> properties = new ArrayList<>();

}
