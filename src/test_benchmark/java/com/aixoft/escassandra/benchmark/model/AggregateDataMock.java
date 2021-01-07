package com.aixoft.escassandra.benchmark.model;

import com.aixoft.escassandra.annotation.AggregateData;
import lombok.Value;

@AggregateData(tableName = "benchmark_aggregate")
@Value
public class AggregateDataMock {
    String name;
}


