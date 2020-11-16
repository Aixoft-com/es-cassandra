package com.aixoft.escassandra.exception.runtime;

/**
 * The type Class not found by bean definition exception.
 */
public class ClassNotFoundByBeanDefinitionException extends RuntimeException {
    /**
     * Instantiates a new Class not found by bean definition exception.
     *
     * @param message Exception message.
     */
    public ClassNotFoundByBeanDefinitionException(String message) {
        super(message);
    }
}
