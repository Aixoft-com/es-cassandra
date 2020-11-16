package com.aixoft.escassandra.config.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants with Regex patterns.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexPattern {
    /**
     * Alphanumeric including '_' character.
     */
    public static final String IS_ALPHANUMERIC = "^\\w+$";
}
