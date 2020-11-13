package com.aixoft.escassandra.component.registrar;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@AllArgsConstructor
@Value
public class AggregateComponent {
    List<Class<? extends AggregateRoot>> classes;
}
