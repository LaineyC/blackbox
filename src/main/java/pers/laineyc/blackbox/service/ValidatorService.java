package pers.laineyc.blackbox.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pers.laineyc.blackbox.exception.CommonException;
import java.util.Set;

@Slf4j
@Component
public class ValidatorService {

    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    public void validThrowException(Object bean, Class<?>... groups) {
        Validator validator = localValidatorFactoryBean.getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean, groups);
        if (!CollectionUtils.isEmpty(constraintViolations)) {
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                throw new CommonException(constraintViolation.getMessage());
            }
        }
    }

    public String validReturnMessage(Object bean, Class<?>... groups) {
        Validator validator = localValidatorFactoryBean.getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean, groups);
        if (!CollectionUtils.isEmpty(constraintViolations)) {
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                return constraintViolation.getMessage();
            }
        }

        return null;
    }

}
