package com.aixoft.escassandra.component.registrar;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Contains list of all aggregate data classes.
 */
@AllArgsConstructor
@Value
public class AggregateDataComponent {
    /**
     * List of aggregate data classes.
     */
    List<Class<?>> classes;
}
