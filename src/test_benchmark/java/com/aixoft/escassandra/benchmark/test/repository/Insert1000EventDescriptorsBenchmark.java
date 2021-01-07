package com.aixoft.escassandra.benchmark.test.repository;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateDataMock;
import com.aixoft.escassandra.benchmark.model.event.NameChanged;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.impl.CassandraEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class Insert1000EventDescriptorsBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_EVENTS_IN_BATCH = 1000;
    private static CassandraEventDescriptorRepository cassandraEventDescriptorRepository;
    private List<EventDescriptor> eventDescriptors = new ArrayList<>(NUMBER_OF_EVENTS_IN_BATCH);

    @Autowired
    public void setReactiveCassandraEventDescriptorRepository(CassandraEventDescriptorRepository cassandraEventDescriptorRepository) {
        Insert1000EventDescriptorsBenchmark.cassandraEventDescriptorRepository = cassandraEventDescriptorRepository;
    }

    @Setup
    public void setup() {
        EventVersion eventVersion = EventVersion.initial();

        for(int it = 0; it < NUMBER_OF_EVENTS_IN_BATCH; it++) {
            eventVersion = eventVersion.getNextMinor();
            eventDescriptors.add(new EventDescriptor(eventVersion, new NameChanged("Name_" + it)));
        }
    }

    @Benchmark
    public void insertEventDescriptors(){
        cassandraEventDescriptorRepository.insertAll(AggregateDataMock.class,
            Uuids.timeBased(),
            eventDescriptors
            );
    }

}
