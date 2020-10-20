package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.ReactiveEventDescriptorRepository;
import com.aixoft.escassandra.repository.StatementBinder;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.repository.util.EventDescriptorRowUtil;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReactiveCassandraEventDescriptorRepository implements ReactiveEventDescriptorRepository {
    CqlSession session;
    StatementBinder statementBinder;
    EventReadingConverter eventReadingConverter;

    public ReactiveCassandraEventDescriptorRepository(CassandraSession cassandraSession, StatementBinder statementBinder, EventReadingConverter eventReadingConverter) {
        this.session = cassandraSession.getSession();
        this.statementBinder = statementBinder;
        this.eventReadingConverter = eventReadingConverter;
    }


    @Override
    public Mono<Boolean> insertAll(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                             @NonNull UUID aggregateId,
                                             @NonNull List<EventDescriptor> newEventDescriptors) {

        return Mono.fromCallable(() -> statementBinder.bindInsertEventDescriptors(aggregateClass, aggregateId, newEventDescriptors))
            .flux()
            .flatMap(session::executeReactive)
            .map(ReactiveRow::wasApplied)
            .reduce(Boolean.FALSE, (init, res) -> res);
    }

    @Override
    public Flux<EventDescriptor> findAllByAggregateId(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId) {
        return executeFindStatement(
            statementBinder.bindFindAllEventDescriptors(aggregateClass, aggregateId)
        );
    }

    @Override
    public Flux<EventDescriptor> findAllByAggregateIdSinceLastSnapshot(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                                                       @NonNull UUID aggregateId,
                                                                       int snapshotVersion) {
        return executeFindStatement(
            statementBinder.bindFindAllSinceLastSnapshotEventDescriptors(aggregateClass, aggregateId, snapshotVersion)
        );
    }

    private Flux<EventDescriptor> executeFindStatement(Statement boundStatement) {
        return Flux.from(session.executeReactive(boundStatement))
            .map(row -> EventDescriptorRowUtil.toEventDescriptor(row, eventReadingConverter));
    }


}
