package com.eleodorodev.specification.exception;

/**
 * DynamicSpecificationException
 * @apiNote Custom RuntimeException
 */
public class DynamicSpecificationException extends RuntimeException {
    public DynamicSpecificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicSpecificationException(Throwable cause) {
        super(cause);
    }
}
