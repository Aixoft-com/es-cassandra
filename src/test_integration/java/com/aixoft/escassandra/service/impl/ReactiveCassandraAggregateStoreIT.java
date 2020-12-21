package com.aixoft.escassandra.service.impl;

import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.config.TestEsCassandraConfiguration;
import com.aixoft.escassandra.exception.checked.AggregateFailedSaveException;
import com.aixoft.escassandra.exception.checked.AggregateNotFoundException;
import com.aixoft.escassandra.service.impl.model.AggregateMock;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.repository.impl.ReactiveCassandraEventDescriptorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void save_AggregateWithNoEvent_ReturnsAggregateFailedSaveException() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0001");

        AggregateMock aggregateMock = new AggregateMock(uuid);

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregateMock))
            .verifyError(AggregateFailedSaveException.class);
    }

    @Test
    void save_TwoActionsPublishedOnAggregate_AggregateUpdatedAndUncommittedChangesCleared() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0002");
        String userName = "user";
        int points = 100;

        AggregateMock aggregateMock = new AggregateMock(uuid);
        aggregateMock.initialize(userName);
        aggregateMock.addPoints(points);

        //Assert apply methods not executed on event publish before save. Events stored in uncommitted changes.
        assertNull(aggregateMock.getUserName());
        assertEquals(0, aggregateMock.getPoints());
        assertEquals(2, aggregateMock.getUncommittedEvents().size());

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregateMock))
            .expectNextCount(1)
            .verifyComplete();

        //Assert apply methods executed on save resulting in aggregate update. No uncommitted changes after save.
        assertEquals(userName, aggregateMock.getUserName());
        assertEquals(points, aggregateMock.getPoints());
        assertEquals(0, aggregateMock.getUncommittedEvents().size());
    }

    @Test
    void loadById_AggregateWithGivenIdIsNotPersisted_ReturnsAggregateNotFoundException() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0003");

        StepVerifier.create(reactiveCassandraAggregateStore.loadById(uuid, AggregateMock.class))
            .verifyError(AggregateNotFoundException.class);

        verify(reactiveCassandraEventDescriptorRepository).findAllByAggregateId(AggregateMock.class, uuid);
    }


    @Test
    void loadById_TwoActionsPublishedOnAggregate_AggregateRestoredFromAllEvents() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c0444444");
        String userName = "user";
        int points = 100;

        AggregateMock aggregateMock = new AggregateMock(uuid);
        aggregateMock.initialize(userName);
        aggregateMock.addPoints(points);

        StepVerifier.create(reactiveCassandraAggregateStore.save(aggregateMock))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraAggregateStore.loadById(uuid, AggregateMock.class))
            .assertNext( restoredAggregate -> {
                assertEquals(new EventVersion(0, 2), restoredAggregate.getCommittedVersion());
                assertEquals(userName, restoredAggregate.getUserName());
                assertEquals(points, restoredAggregate.getPoints());
                assertEquals(0, aggregateMock.getUncommittedEvents().size());
            })
            .verifyComplete();

        verify(reactiveCassandraEventDescriptorRepository).findAllByAggregateId(AggregateMock.class, uuid);
    }

    @Test
    void loadByIdWithSnapshot_SnapshotCreated_AggregateRestoredFromAllEventsSinceSnapshot() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e0004");

        StepVerifier.create(
            Mono.just(new AggregateMock(uuid))
                .doOnNext(aggregateMock -> aggregateMock.initialize("user1"))
                .doOnNext(aggregateMock -> aggregateMock.addPoints(100))
                .doOnNext(aggregateMock -> aggregateMock.createSnapshot("user2", 200))
                .doOnNext(aggregateMock -> aggregateMock.addPoints(50))
                .flatMap(reactiveCassandraAggregateStore::save)
        )
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraAggregateStore.loadById(uuid, 1, AggregateMock.class))
            .assertNext( restoredAggregate -> {
                assertEquals(new EventVersion(1, 1), restoredAggregate.getCommittedVersion());
                assertEquals("user2", restoredAggregate.getUserName());
                assertEquals(250, restoredAggregate.getPoints());
                assertEquals(0, restoredAggregate.getUncommittedEvents().size());
            })
            .verifyComplete();

        verify(reactiveCassandraEventDescriptorRepository).findAllByAggregateIdSinceSnapshot(AggregateMock.class, uuid, 1);
    }
}
