package pers.laineyc.blackbox.config;

import org.springframework.context.annotation.Configuration;
import pers.laineyc.blackbox.strategy.validate.*;

@Configuration
public class ValidateStrategyConfigurer {
    static {
        new BooleanTypeValidateStrategy();
        new IntegerTypeValidateStrategy();
        new NumberTypeValidateStrategy();
        new StringTypeValidateStrategy();
        new ArrayTypeValidateStrategy();
        new ObjectTypeValidateStrategy();
    }

}
