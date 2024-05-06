package com.eleodorodev.specification;

import org.springframework.data.jpa.domain.Specification;

/**
 * BaseDynamicFilter
 *
 * @author <a href="https://github.com/MatheusEleodoro">Matheus Eleodoro</a>
 * @version 1.0.0
 * @apiNote To standardize classes that will implement filters in the body
 * @see  <a href="https://github.com/MatheusEleodoro">...</a>
 */
@FunctionalInterface
public interface BaseDynamicFilter<T> {
    Specification<T> apply();
}
