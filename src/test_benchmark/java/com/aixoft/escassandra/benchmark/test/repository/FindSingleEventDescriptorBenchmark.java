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

import java.util.List;
import java.util.UUID;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class FindSingleEventDescriptorBenchmark extends BenchmarkWithContext {

    private static CassandraEventDescriptorRepository cassandraEventDescriptorRepository;
    private UUID uuid = Uuids.timeBased();

    @Autowired
    public void setReactiveCassandraEventDescriptorRepository(CassandraEventDescriptorRepository cassandraEventDescriptorRepository) {
        FindSingleEventDescriptorBenchmark.cassandraEventDescriptorRepository = cassandraEventDescriptorRepository;
    }

    @Setup
    public void setup() {
        cassandraEventDescriptorRepository.insertAll(
            AggregateDataMock.class,
            uuid,
            List.of(new EventDescriptor(new EventVersion(0, 1), new NameChanged("userName"))));
    }

    @Benchmark
    public void findSingleEventDescriptor(){
        cassandraEventDescriptorRepository.findAllByAggregateId(AggregateDataMock.class, uuid);
    }
}
