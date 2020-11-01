package com.aixoft.escassandra.exception.runtime;

public class AggregateStatementNotFoundException extends RuntimeException {
    public AggregateStatementNotFoundException(String message) {
        super(message);
    }
}
