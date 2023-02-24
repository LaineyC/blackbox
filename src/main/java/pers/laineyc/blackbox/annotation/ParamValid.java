package pers.laineyc.blackbox.annotation;

import java.lang.annotation.*;

// @Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamValid {

    // hibernate 验证的 group
    Class<?>[] value() default {};

}