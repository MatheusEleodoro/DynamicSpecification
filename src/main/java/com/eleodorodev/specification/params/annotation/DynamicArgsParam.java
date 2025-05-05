package com.eleodorodev.specification.params.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicArgsParam {
  String name() default "q";
  boolean search() default false;
  boolean pageable() default true;
}
