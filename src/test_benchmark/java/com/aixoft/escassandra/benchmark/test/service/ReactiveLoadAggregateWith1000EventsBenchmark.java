package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.service.impl.ReactiveCassandraAggregateStore;
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
public class ReactiveLoadAggregateWith1000EventsBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_EVENTS_IN_BATCH = 1000;
    private static ReactiveCassandraAggregateStore cassandraAggregateStore;

    private UUID uuid = Uuids.timeBased();

    @Autowired
    public void setCassandraAggregateStore(ReactiveCassandraAggregateStore cassandraAggregateStore) {
        ReactiveLoadAggregateWith1000EventsBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Setup
    public void setup() {
        AggregateMock aggregateMock = new AggregateMock(uuid);
        aggregateMock.publishEvent(new AggregateCreated("name"));

        for(int it = 1; it < NUMBER_OF_EVENTS_IN_BATCH; it++) {
            aggregateMock.publishEvent(new NameChanged("Name+" + it));
        }

        cassandraAggregateStore.save(aggregateMock)
            .block();
    }

    @Benchmark
    public void loadAggregateWith1000Events(){
        cassandraAggregateStore.loadById(uuid, AggregateMock.class)
            .block();
    }
}
