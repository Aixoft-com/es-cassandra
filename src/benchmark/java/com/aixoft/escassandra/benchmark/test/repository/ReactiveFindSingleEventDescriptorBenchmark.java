package com.aixoft.escassandra.benchmark.test.repository;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
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

import java.util.List;
import java.util.UUID;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class ReactiveFindSingleEventDescriptorBenchmark extends BenchmarkWithContext {

    private static ReactiveCassandraEventDescriptorRepository repository;
    private UUID uuid = Uuids.timeBased();

    @Autowired
    public void setReactiveCassandraEventDescriptorRepository(ReactiveCassandraEventDescriptorRepository repository) {
        ReactiveFindSingleEventDescriptorBenchmark.repository = repository;
    }

    @Setup
    public void setup() {
        repository.insertAll(
            AggregateMock.class,
            uuid,
            List.of(new EventDescriptor(new EventVersion(0, 1), new AggregateCreated("userName"))))
        .block();
    }

    @Benchmark
    public void findSingleEventDescriptor(){
        repository.findAllByAggregateId(AggregateMock.class, uuid)
        .blockLast();
    }
}
