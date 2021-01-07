package com.aixoft.escassandra.benchmark.test.repository;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateDataMock;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.impl.ReactiveCassandraEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class ReactiveFind1000EventDescriptorsBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_EVENTS_IN_BATCH = 1000;
    private static ReactiveCassandraEventDescriptorRepository repository;

    private UUID uuid = Uuids.timeBased();
    private List<EventDescriptor> eventDescriptors = new ArrayList<>(NUMBER_OF_EVENTS_IN_BATCH);

    @Autowired
    public void setReactiveCassandraEventDescriptorRepository(ReactiveCassandraEventDescriptorRepository repository) {
        ReactiveFind1000EventDescriptorsBenchmark.repository = repository;
    }

    @Setup
    public void setup() {
        EventVersion eventVersion = EventVersion.initial();

        for(int it =0; it < NUMBER_OF_EVENTS_IN_BATCH; it++) {
            eventDescriptors.add(new EventDescriptor(eventVersion, new NameChanged("Name_" + it)));
            eventVersion = eventVersion.getNextMinor();
        }

        repository.insertAll(AggregateDataMock.class, uuid, eventDescriptors)
            .blockLast();
    }

    @Benchmark
    public void find1000EventDescriptors(){
        repository.findAllByAggregateId(AggregateDataMock.class, uuid)
            .blockLast();
    }
}
