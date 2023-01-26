package com.aixoft.escassandra.exception.validation;

public class UnexpectedEventVersionException extends InvalidCommandException {
    public UnexpectedEventVersionException(String message) {
        super(message);
    }
}
