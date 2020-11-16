package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.impl.CassandraAggregateStore;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class Publish1000EventsAndStoreInBatchBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_EVENTS_IN_BATCH = 1000;
    private static CassandraAggregateStore cassandraAggregateStore;

    private List<Event> events = new ArrayList<>(NUMBER_OF_EVENTS_IN_BATCH);

    @Autowired
    public void setCassandraAggregateStore(CassandraAggregateStore cassandraAggregateStore) {
        Publish1000EventsAndStoreInBatchBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Setup
    public void setup() {
        events.add(new AggregateCreated("name"));

        for(int it = 1; it < NUMBER_OF_EVENTS_IN_BATCH; it++) {
            events.add(new NameChanged("Name+" + it));
        }
    }

    @Benchmark
    public void save(){
        AggregateMock aggregateMock = new AggregateMock(Uuids.timeBased());

        events.stream()
            .forEach(aggregateMock::publishEvent);

        cassandraAggregateStore.save(aggregateMock);
    }
}
