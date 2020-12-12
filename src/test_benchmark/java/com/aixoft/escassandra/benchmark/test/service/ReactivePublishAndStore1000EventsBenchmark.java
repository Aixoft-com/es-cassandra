package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.service.impl.ReactiveCassandraAggregateStore;
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
public class ReactivePublishAndStore1000EventsBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_EVENTS_IN_BATCH = 1000;
    private static ReactiveCassandraAggregateStore cassandraAggregateStore;

    private List<Event> events = new ArrayList<>(NUMBER_OF_EVENTS_IN_BATCH);

    @Setup
    public void setup() {
        events.add(new AggregateCreated("name"));

        for(int it = 1; it < NUMBER_OF_EVENTS_IN_BATCH; it++) {
            events.add(new NameChanged("Name+" + it));
        }
    }

    @Autowired
    public void setCassandraAggregateStore(ReactiveCassandraAggregateStore cassandraAggregateStore) {
        ReactivePublishAndStore1000EventsBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Benchmark
    public void save1000OneByOne(){
        AggregateMock aggregateMock = new AggregateMock(Uuids.timeBased());

        events.stream()
            .forEach( event -> {
                aggregateMock.publishEvent(event);
                cassandraAggregateStore.save(aggregateMock)
                    .block();
            });
    }
}
