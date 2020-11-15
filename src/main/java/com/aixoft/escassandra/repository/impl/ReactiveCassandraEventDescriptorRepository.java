package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.repository.ReactiveEventDescriptorRepository;
import com.aixoft.escassandra.repository.StatementBinder;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.repository.util.EventDescriptorRowUtil;
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

/**
 * Reactive cassandra repository to store {@link EventDescriptor}.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReactiveCassandraEventDescriptorRepository implements ReactiveEventDescriptorRepository {
    CqlSession session;
    StatementBinder statementBinder;
    EventReadingConverter eventReadingConverter;

    /**
     * Instantiates a new Reactive cassandra event descriptor repository.
     *
     * @param cassandraSession      the cassandra session
     * @param statementBinder       the statement binder
     * @param eventReadingConverter the event reading converter
     */
    public ReactiveCassandraEventDescriptorRepository(CassandraSession cassandraSession, StatementBinder statementBinder, EventReadingConverter eventReadingConverter) {
        this.session = cassandraSession.getSession();
        this.statementBinder = statementBinder;
        this.eventReadingConverter = eventReadingConverter;
    }


    /**
     * Creates {@link Mono} for inserting {@link EventDescriptor} list to database.
     *
     * If list contains more then one element, then inserting will be performed in a batch.
     *
     * If any event with same version is is already persisted in the database then operation will fail and no element
     * will be stored.
     *
     * @param aggregateClass Type of the aggregate.
     * @param aggregateId UUID of aggregate for which EventDescriptors will be inserted.
     * @param eventDescriptors EventDescriptors to be inserted.
     * @return Mono with TRUE indicating if insert was successful or FALSE otherwise.
     */
    @Override
    public Mono<Boolean> insertAll(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                             @NonNull UUID aggregateId,
                                             @NonNull List<EventDescriptor> eventDescriptors) {

        return Mono.fromCallable(() -> statementBinder.bindInsertEventDescriptors(aggregateClass, aggregateId, eventDescriptors))
            .flux()
            .flatMap(session::executeReactive)
            .map(ReactiveRow::wasApplied)
            .reduce(Boolean.FALSE, (init, res) -> res);
    }

    /**
     * Creates Flux for finding all event descriptors for aggregate of given type and id.
     * @param aggregateClass Type of the aggregate.
     * @param aggregateId UUID of aggregate.
     * @return Flux from event descriptors.
     */
    @Override
    public Flux<EventDescriptor> findAllByAggregateId(Class<? extends AggregateRoot> aggregateClass, UUID aggregateId) {
        return executeFindStatement(
            statementBinder.bindFindAllEventDescriptors(aggregateClass, aggregateId)
        );
    }

    /**
     * Creates Flux for finding all event descriptors since given major version (snapshot number)
     * for aggregate of given type and id.
     * @param aggregateClass Type of the aggregate.
     * @param aggregateId UUID of aggregate.
     * @param snapshotVersion Major version of the event ({@link com.aixoft.escassandra.model.EventVersion#getMajor()}).
     * @return Flux from event descriptors.
     */
    @Override
    public Flux<EventDescriptor> findAllByAggregateIdSinceSnapshot(@NonNull Class<? extends AggregateRoot> aggregateClass,
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
