package com.aixoft.escassandra.benchmark.test.repository;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
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
public class InsertOneEventDescriptorBenchmark extends BenchmarkWithContext {

    private static CassandraEventDescriptorRepository cassandraEventDescriptorRepository;
    private List<EventDescriptor> eventDescriptors = new ArrayList<>(1);

    @Autowired
    public void setReactiveCassandraEventDescriptorRepository(CassandraEventDescriptorRepository cassandraEventDescriptorRepository) {
        InsertOneEventDescriptorBenchmark.cassandraEventDescriptorRepository = cassandraEventDescriptorRepository;
    }

    @Setup
    public void setup() {
        eventDescriptors = List.of(new EventDescriptor(new EventVersion(0, 1), new AggregateCreated("userName")));
    }

    @Benchmark
    public void insertOneEventDescriptor(){
        cassandraEventDescriptorRepository.insertAll(AggregateMock.class,
            Uuids.timeBased(),
            eventDescriptors
            );
    }

}
