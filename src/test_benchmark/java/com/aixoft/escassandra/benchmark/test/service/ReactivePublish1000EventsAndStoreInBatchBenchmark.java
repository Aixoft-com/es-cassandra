package com.aixoft.escassandra.benchmark.test.service;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.benchmark.model.AggregateDataMock;
import com.aixoft.escassandra.benchmark.model.command.ChangeNameCommand;
import com.aixoft.escassandra.benchmark.runner.BenchmarkWithContext;
import com.aixoft.escassandra.model.Command;
import com.aixoft.escassandra.service.impl.ReactiveCassandraAggregateStore;
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
public class ReactivePublish1000EventsAndStoreInBatchBenchmark extends BenchmarkWithContext {
    private static final int NUMBER_OF_COMMANDS_IN_BATCH = 1000;
    private static ReactiveCassandraAggregateStore cassandraAggregateStore;

    private List<Command<AggregateDataMock>> commands = new ArrayList<>(NUMBER_OF_COMMANDS_IN_BATCH);

    @Autowired
    public void setCassandraAggregateStore(ReactiveCassandraAggregateStore cassandraAggregateStore) {
        ReactivePublish1000EventsAndStoreInBatchBenchmark.cassandraAggregateStore = cassandraAggregateStore;
    }

    @Setup
    public void setup() {
        for(int it = 0; it < NUMBER_OF_COMMANDS_IN_BATCH; it++) {
            commands.add(new ChangeNameCommand("Name+" + it));
        }
    }

    @Benchmark
    public void save(){
        Aggregate<AggregateDataMock> aggregate = Aggregate.create(UUID.randomUUID());

        commands.stream()
            .forEach(aggregate::handleCommand);

        cassandraAggregateStore.save(aggregate)
            .block();
    }
}
