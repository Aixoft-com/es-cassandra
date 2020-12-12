package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.service.impl.CassandraAggregateStore;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class LoadAggregateWithOneEventBenchmark extends BenchmarkWithContext {
    private static CassandraAggregateStore cassandraAggregateStore;

    private UUID uuid = Uuids.timeBased();

    @Autowired
    public void setCassandraAggregateStore(CassandraAggregateStore cassandraAggregateStore) {
        LoadAggregateWithOneEventBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Setup
    public void setup() {
        AggregateMock aggregateMock = new AggregateMock(uuid);
        aggregateMock.publishEvent(new AggregateCreated("name"));

        cassandraAggregateStore.save(aggregateMock);
    }

    @Benchmark
    public void loadAggregateWithOneEvent(){
        cassandraAggregateStore.loadById(uuid, AggregateMock.class);
    }
}
