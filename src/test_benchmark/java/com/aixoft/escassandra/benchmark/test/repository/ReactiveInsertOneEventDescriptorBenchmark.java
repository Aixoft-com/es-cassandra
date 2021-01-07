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

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class ReactiveInsertOneEventDescriptorBenchmark extends BenchmarkWithContext {

    private static ReactiveCassandraEventDescriptorRepository repository;
    private List<EventDescriptor> eventDescriptors = new ArrayList<>(1);

    @Autowired
    public void setReactiveCassandraEventDescriptorRepository(ReactiveCassandraEventDescriptorRepository repository) {
        ReactiveInsertOneEventDescriptorBenchmark.repository = repository;
    }

    @Setup
    public void setup() {
        eventDescriptors = List.of(new EventDescriptor(new EventVersion(0, 1), new NameChanged("userName")));
    }

    @Benchmark
    public void insertOneEventDescriptor(){
        repository.insertAll(AggregateDataMock.class,
            Uuids.timeBased(),
            eventDescriptors
            )
        .blockLast();
    }

}
