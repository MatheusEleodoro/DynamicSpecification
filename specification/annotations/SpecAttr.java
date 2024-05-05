package br.com.evertec.sinqia.contabil.specification.annotations;


import br.com.evertec.sinqia.contabil.specification.enums.Conditional;
import br.com.evertec.sinqia.contabil.specification.enums.Conjunction;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface SpecAttr {
  String property();
  String alias() default "";
  String[] parents() default {};
  Conjunction conjunction() default Conjunction.AND;
  Conditional conditional() default Conditional.EQ;
  boolean negate() default false;
}
