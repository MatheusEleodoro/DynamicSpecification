package com.eleodorodev.specification.params.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicParam {
    String name() default "q";

    boolean search() default false;

    boolean pageable() default true;

    boolean required() default true;

    String[] mandatory() default {};

    Class<?> type() default Void.class;
}
