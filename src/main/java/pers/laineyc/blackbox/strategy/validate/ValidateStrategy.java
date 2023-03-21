package pers.laineyc.blackbox.strategy.validate;

import pers.laineyc.blackbox.enums.ValueType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ValidateStrategy {

    private static final Map<ValueType, ValidateStrategy> STRATEGY_MAP = new HashMap<>();

    public static ValidateStrategy getStrategy(ValueType valueType){
        return STRATEGY_MAP.get(valueType);
    }

    protected void register(ValueType valueType){
        STRATEGY_MAP.put(valueType, this);
    }

    public abstract Object validateAndBuildValue(VerifiableValue verifiableValue, Object value, String path);

    protected String buildPath(String... paths){
        return Arrays.asList(paths).stream().collect(Collectors.joining("."));
    }
}
