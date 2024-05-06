package com.eleodorodev.specification.annotations;


import com.eleodorodev.specification.enums.Conditional;
import com.eleodorodev.specification.enums.Conjunction;

import java.lang.annotation.*;

/**
 * Note to configure filters in your entity or DTO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface DynamicSpecAttr {
  String property();
  String alias() default "";
  String[] parents() default {};
  Conjunction conjunction() default Conjunction.AND;
  Conditional conditional() default Conditional.EQ;
  boolean negate() default false;
}
