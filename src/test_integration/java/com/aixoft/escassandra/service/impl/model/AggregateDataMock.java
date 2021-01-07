package com.aixoft.escassandra.service.impl.model;

import com.aixoft.escassandra.annotation.AggregateData;
import lombok.Getter;
import lombok.Value;

@AggregateData(tableName = "test_aggregate_store")
@Getter
@Value
public class AggregateDataMock {
    String userName;
    int points;
}

