package pers.laineyc.blackbox.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import pers.laineyc.blackbox.annotation.ParamValid;
import pers.laineyc.blackbox.service.ValidatorService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
public class ParamValidInterceptor extends StaticMethodMatcherPointcutAdvisor {

    @Autowired
    private ValidatorService validatorService;

    public ParamValidInterceptor() {
        setAdvice((MethodBeforeAdvice) (method, arguments, target) -> {
            Annotation[][] annotations = method.getParameterAnnotations();

            for(int i = 0; i < arguments.length; i++) {
                Annotation[] annotationArray = annotations[i];
                if(annotationArray == null) {
                    continue;
                }

                for(Annotation annotation: annotationArray) {
                    if(!ParamValid.class.equals(annotation.annotationType())) {
                        continue;
                    }

                    validatorService.validThrowException(arguments[i], ((ParamValid)annotation).value());
                }
            }
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        Annotation[][] annotations = method.getParameterAnnotations();

        for(Annotation[] annotationArray : annotations) {
            if(annotationArray == null) {
                continue;
            }

            for(Annotation annotation : annotationArray) {
                if(ParamValid.class.equals(annotation.annotationType())) {
                    return true;
                }
            }
        }

        return false;
    }

}

