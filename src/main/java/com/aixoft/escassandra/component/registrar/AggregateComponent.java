package com.aixoft.escassandra.component.registrar;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@AllArgsConstructor
@Value
public class AggregateComponent {
    List<Class> classes;
}
