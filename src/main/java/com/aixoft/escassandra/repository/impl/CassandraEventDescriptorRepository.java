package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.repository.EventDescriptorRepository;
import com.aixoft.escassandra.repository.StatementBinder;
import com.aixoft.escassandra.repository.converter.EventReadingConverter;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.aixoft.escassandra.repository.util.EventDescriptorRowUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CassandraEventDescriptorRepository implements EventDescriptorRepository {
    CqlSession session;
    StatementBinder statementBinder;
    EventReadingConverter eventReadingConverter;

    public CassandraEventDescriptorRepository(CassandraSession cassandraSession, StatementBinder statementBinder, EventReadingConverter eventReadingConverter) {
        this.session = cassandraSession.getSession();
        this.statementBinder = statementBinder;
        this.eventReadingConverter = eventReadingConverter;
    }

    @Override
    public boolean insertAll(@NonNull Class<? extends AggregateRoot> aggregateClass,
                          @NonNull UUID aggregateId,
                          @NonNull List<EventDescriptor> newEventDescriptors) {
        return session.execute(statementBinder.bindInsertEventDescriptors(aggregateClass, aggregateId, newEventDescriptors))
            .wasApplied();
    }

    @Override
    public List<EventDescriptor> findAllByAggregateId(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                                      @NonNull UUID aggregateId) {
        return executeFindStatement(
            statementBinder.bindFindAllEventDescriptors(aggregateClass, aggregateId)
        );
    }

    @Override
    public List<EventDescriptor> findAllByAggregateIdSinceLastSnapshot(@NonNull Class<? extends AggregateRoot> aggregateClass,
                                                                       @NonNull UUID aggregateId,
                                                                       int snapshotVersion) {
        return executeFindStatement(
            statementBinder.bindFindAllSinceLastSnapshotEventDescriptors(aggregateClass, aggregateId, snapshotVersion)
        );
    }

    private List<EventDescriptor> executeFindStatement(Statement statement) {
        ResultSet resultSet = session.execute(statement);

        return resultSet.all().stream()
            .map(row -> EventDescriptorRowUtil.toEventDescriptor(row, eventReadingConverter))
            .collect(Collectors.toList());
    }

}
