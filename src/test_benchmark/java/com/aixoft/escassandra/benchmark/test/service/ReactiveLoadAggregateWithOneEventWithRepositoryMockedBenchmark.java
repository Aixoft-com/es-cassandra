package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateMock;
import com.aixoft.escassandra.benchmark.model.event.AggregateCreated;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.impl.ReactiveCassandraEventDescriptorRepository;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.service.impl.ReactiveCassandraAggregateStore;
import org.junit.jupiter.api.BeforeEach;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.Mockito.when;

@State(Scope.Benchmark)
@EnableCassandraEventSourcing(
    aggregatePackages = "com.aixoft.escassandra.benchmark.model",
    eventPackages = "com.aixoft.escassandra.benchmark.model.event"
)
public class ReactiveLoadAggregateWithOneEventWithRepositoryMockedBenchmark extends BenchmarkWithContext {
    private static ReactiveCassandraAggregateStore cassandraAggregateStore;

    private UUID uuid = UUID.fromString("dbb7a300-2601-11eb-9b05-f1088c02d444");

    @MockBean
    private ReactiveCassandraEventDescriptorRepository cassandraEventDescriptorRepository;

    @BeforeEach
    private void beforeEach() {
        when(cassandraEventDescriptorRepository.findAllByAggregateId(AggregateMock.class, uuid))
            .thenReturn(Flux.just(new EventDescriptor(EventVersion.initial(), new AggregateCreated("name"))));
    }

    @Autowired
    public void setCassandraAggregateStore(ReactiveCassandraAggregateStore cassandraAggregateStore) {
        ReactiveLoadAggregateWithOneEventWithRepositoryMockedBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Benchmark
    public void loadAggregateWithOneEvent(){
        cassandraAggregateStore.loadById(uuid, AggregateMock.class)
            .block();
    }
}
