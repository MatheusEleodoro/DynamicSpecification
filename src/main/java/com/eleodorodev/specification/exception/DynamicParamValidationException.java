package com.eleodorodev.specification.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
public class DynamicParamValidationException extends RuntimeException {
    private final transient Set<Object> violations;
    private final Class<?> root;

    public DynamicParamValidationException(String message, Class<?> root) {
        super(message);
        this.root = root;
        this.violations = Collections.emptySet();
    }

    public DynamicParamValidationException(String message, Class<?> root, Set<Object> violations) {
        super(message);
        this.root = root;
        this.violations = violations;
    }

    public DynamicParamValidationException(String message) {
        super(message);
        this.violations = Collections.emptySet();
        this.root = null;
    }

}
