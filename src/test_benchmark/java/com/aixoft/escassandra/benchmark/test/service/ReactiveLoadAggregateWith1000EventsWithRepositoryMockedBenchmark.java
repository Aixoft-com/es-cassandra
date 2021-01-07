package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateDataMock;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.benchmark.util.EventDescriptorGenerator;
import com.aixoft.escassandra.repository.impl.ReactiveCassandraEventDescriptorRepository;
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
public class ReactiveLoadAggregateWith1000EventsWithRepositoryMockedBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_EVENTS_IN_BATCH = 1000;
    private static ReactiveCassandraAggregateStore cassandraAggregateStore;

    private UUID uuid = UUID.fromString("729fcc30-2601-11eb-8972-ed380c475aa5");

    @MockBean
    private ReactiveCassandraEventDescriptorRepository cassandraEventDescriptorRepository;

    @BeforeEach
    private void beforeEach() {

        when(cassandraEventDescriptorRepository.findAllByAggregateId(AggregateDataMock.class, uuid))
            .thenReturn(Flux.fromIterable(EventDescriptorGenerator.generate(NUMBER_OF_EVENTS_IN_BATCH)));
    }

    @Autowired
    public void setCassandraAggregateStore(ReactiveCassandraAggregateStore cassandraAggregateStore) {
        ReactiveLoadAggregateWith1000EventsWithRepositoryMockedBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Benchmark
    public void loadAggregateWith1000Events(){
        cassandraAggregateStore.loadById(uuid, AggregateDataMock.class)
            .block();
    }
}
