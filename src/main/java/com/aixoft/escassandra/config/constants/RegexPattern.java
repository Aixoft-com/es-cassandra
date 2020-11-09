package com.aixoft.escassandra.config.enums;

public enum RegexPattern {
    IS_ALPHANUMERIC("^\\w+$");

    private String pattern;

    RegexPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
