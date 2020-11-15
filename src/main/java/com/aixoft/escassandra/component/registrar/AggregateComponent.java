package com.aixoft.escassandra.component.registrar;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * Contains list of all Aggregate classes.
 */
@AllArgsConstructor
@Value
public class AggregateComponent {
    /**
     * List of Aggregate classes.
     */
    List<Class<? extends AggregateRoot>> classes;
}
