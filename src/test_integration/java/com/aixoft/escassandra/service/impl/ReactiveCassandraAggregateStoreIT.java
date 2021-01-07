package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.config.TestEsCassandraConfiguration;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.impl.ReactiveCassandraEventDescriptorRepository;
import com.aixoft.escassandra.service.impl.model.AggregateDataMock;
import com.aixoft.escassandra.service.impl.model.command.AddPointsCommand;
import com.aixoft.escassandra.service.impl.model.command.CreateCommand;
import com.aixoft.escassandra.service.impl.model.command.CreateSnapshotCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@EnableCassandraEventSourcing(eventPackages = "com.aixoft.escassandra.service.impl", aggregatePackages = "com.aixoft.escassandra.service.impl")
@ContextConfiguration(classes = TestEsCassandraConfiguration.class)
class ReactiveCassandraAggregateStoreIT {
    @Autowired
    private ReactiveCassandraAggregateStore reactiveCassandraAggregateStore;

    @SpyBean
    private ReactiveCassandraEventDescriptorRepository reactiveCassandraEventDescriptorRepository;

    @Test
    void save_NoEventsAfterFirstSave_ReturnsEmptyOnSecondSave() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0001");

        Aggregate<AggregateDataMock> aggregate = Aggregate.create(uuid);
        aggregate.handleCommand(new CreateCommand("name"));

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregate))
            .expectNext()
            .expectComplete();

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregate))
            .expectComplete();
    }

    @Test
    void save_AggregateWithNoDataAndNoInitializingEvent_ReturnsAggregateWithNoDataException() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0001");

        Aggregate<AggregateDataMock> aggregate = Aggregate.create(uuid);
        aggregate.handleCommand(new CreateCommand("name"));

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregate))
            .assertNext(aggregateResult -> {
                assertTrue(aggregateResult.getUncommittedEvents().isEmpty());
                assertEquals(new EventVersion(0,1), aggregateResult.getCommittedVersion());
                assertEquals(new EventVersion(0,1), aggregateResult.getCurrentVersion());
                assertEquals("name", aggregateResult.getData().getUserName());
                assertEquals(uuid, aggregateResult.getId());
            })
            .verifyComplete();
    }

    @Test
    void save_TwoActionsPublishedOnAggregate_AggregateUpdatedAndUncommittedChangesCleared() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0002");
        String userName = "user";
        int points = 100;

        Aggregate<AggregateDataMock> aggregate = Aggregate.create(uuid);
        aggregate.handleCommand(new CreateCommand(userName));
        aggregate.handleCommand(new AddPointsCommand(points));

        //Assert apply methods not executed on event publish before save. Events stored in uncommitted changes.
        assertNull(aggregate.getData());
        assertEquals(2, aggregate.getUncommittedEvents().size());

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregate))
            .assertNext( aggregateResult -> {
                assertEquals(userName, aggregateResult.getData().getUserName());
                assertEquals(points, aggregateResult.getData().getPoints());
                assertTrue(aggregateResult.getUncommittedEvents().isEmpty());
                assertEquals(new EventVersion(0,2), aggregateResult.getCommittedVersion());
                assertEquals(new EventVersion(0,2), aggregateResult.getCurrentVersion());
            })
            .verifyComplete();
    }

    @Test
    void loadById_AggregateWithGivenIdIsNotPersisted_ReturnsAggregateNotFoundException() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0003");

        StepVerifier.create(reactiveCassandraAggregateStore.loadById(uuid, AggregateDataMock.class))
            .verifyError(AggregateNotFoundException.class);

        verify(reactiveCassandraEventDescriptorRepository).findAllByAggregateId(AggregateDataMock.class, uuid);
    }


    @Test
    void loadById_TwoActionsPublishedOnAggregate_AggregateRestoredFromAllEvents() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c0444444");
        String userName = "user";
        int points = 100;

        Aggregate<AggregateDataMock> aggregate = Aggregate.create(uuid);
        aggregate.handleCommand(new CreateCommand(userName));
        aggregate.handleCommand(new AddPointsCommand(points));

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregate))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraAggregateStore.loadById(uuid, AggregateDataMock.class))
            .assertNext( restoredAggregate -> {
                assertEquals(new EventVersion(0, 2), restoredAggregate.getCommittedVersion());
                assertEquals(userName, restoredAggregate.getData().getUserName());
                assertEquals(points, restoredAggregate.getData().getPoints());
                assertEquals(0, restoredAggregate.getUncommittedEvents().size());
            })
            .verifyComplete();

        verify(reactiveCassandraEventDescriptorRepository).findAllByAggregateId(AggregateDataMock.class, uuid);
    }

    @Test
    void loadByIdWithSnapshot_SnapshotCreated_AggregateRestoredFromAllEventsSinceSnapshot() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0004");

        Aggregate<AggregateDataMock> aggregateObj = Aggregate.create(uuid);

        StepVerifier.create(
            Mono.just(aggregateObj)
                .doOnNext(aggregate -> aggregate.handleCommand(new CreateCommand("user1")))
                .doOnNext(aggregate -> aggregate.handleCommand(new AddPointsCommand(100)))
                .doOnNext(aggregate -> aggregate.handleSnapshotCommand(new CreateSnapshotCommand("user2", 200)))
                .doOnNext(aggregate -> aggregate.handleCommand(new AddPointsCommand(50)))
                .flatMap(reactiveCassandraAggregateStore::save)
        )
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraAggregateStore.loadById(uuid, 1, AggregateDataMock.class))
            .assertNext( restoredAggregate -> {
                assertEquals(new EventVersion(1, 1), restoredAggregate.getCommittedVersion());
                assertEquals("user2", restoredAggregate.getData().getUserName());
                assertEquals(250, restoredAggregate.getData().getPoints());
                assertEquals(0, restoredAggregate.getUncommittedEvents().size());
            })
            .verifyComplete();

        verify(reactiveCassandraEventDescriptorRepository).findAllByAggregateIdSinceSnapshot(AggregateDataMock.class, uuid, 1);
    }
}
