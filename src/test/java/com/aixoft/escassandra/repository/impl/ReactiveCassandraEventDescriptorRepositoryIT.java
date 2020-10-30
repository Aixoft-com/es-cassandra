package com.aixoft.escassandra.repository.impl;

import com.aixoft.escassandra.aggregate.AggregateRoot;
import com.aixoft.escassandra.annotation.Aggregate;
import com.aixoft.escassandra.annotation.DomainEvent;
import com.aixoft.escassandra.annotation.EnableCassandraEventSourcing;
import com.aixoft.escassandra.config.TestEsCassandraConfiguration;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@EnableCassandraEventSourcing(eventPackages = "com.aixoft.escassandra.it.repository.impl", aggregatePackages = "com.aixoft.escassandra.it.repository.impl")
@ContextConfiguration(classes = TestEsCassandraConfiguration.class)
class ReactiveCassandraEventDescriptorRepositoryIT {

    @Autowired
    private ReactiveCassandraEventDescriptorRepository reactiveCassandraEventDescriptorRepository;

    @Test
    void insertTwoEventDescriptors_FindAllInsertedEdsByAggregateId() {
        UUID uuid = UUID.fromString("62853cd0-1888-11eb-be70-e792c04e2799");

        StepVerifier.create(reactiveCassandraEventDescriptorRepository.insertAll(AggregateMock.class, uuid,
                List.of(
                    new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")),
                    new EventDescriptor(new EventVersion(0 ,2), new PointsAdded(100))
                )))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraEventDescriptorRepository.findAllByAggregateId(AggregateMock.class, uuid))
            .expectNext(new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")))
            .expectNext(new EventDescriptor(new EventVersion(0 ,2), new PointsAdded(100)))
            .verifyComplete();
    }

    @Test
    void findAllByAggregateId_AggregateDoesNotExists_CompletesWithNoElements() {
        UUID uuid = UUID.fromString("8432ade0-15e5-11eb-9d4d-c7745ce10000");

        StepVerifier.create(reactiveCassandraEventDescriptorRepository.findAllByAggregateId(AggregateMock.class, uuid))
            .verifyComplete();
    }

    @Test
    void findAllByAggregateId_SnapshotInserted_ReturnsAllEventsFromBeginning() {
        UUID uuid = UUID.fromString("8432ade0-15e5-11eb-9d4d-c7745ce11bfb");

        StepVerifier.create(reactiveCassandraEventDescriptorRepository.insertAll(AggregateMock.class, uuid,
            List.of(
                new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")),
                new EventDescriptor(new EventVersion(1, 0), new SnapshotCreated("aggregate1", 0)),
                new EventDescriptor(new EventVersion(1 ,1), new PointsAdded(100))
            )))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraEventDescriptorRepository.findAllByAggregateId(AggregateMock.class, uuid))
            .expectNext(new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")))
            .expectNext(new EventDescriptor(new EventVersion(1 ,0), new SnapshotCreated("aggregate1", 0)))
            .expectNext(new EventDescriptor(new EventVersion(1 ,1), new PointsAdded(100)))
            .verifyComplete();
    }

    @Test
    void findAllByAggregateIdSinceSnapshot_SnapshotInserted_ReturnsAllEventsSinceGivenMajorVersion() {
        UUID uuid = UUID.fromString("f5ade710-1323-11eb-a4ac-6f68a8deff48");

        // Given
        StepVerifier.create(reactiveCassandraEventDescriptorRepository.insertAll(AggregateMock.class, uuid,
            List.of(
                new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")),
                new EventDescriptor(new EventVersion(1, 0), new SnapshotCreated("aggregate1", 0)),
                new EventDescriptor(new EventVersion(1 ,1), new PointsAdded(100))
            )))
            .expectNext(true)
            .verifyComplete();

        // When
        StepVerifier.create(reactiveCassandraEventDescriptorRepository.findAllByAggregateIdSinceSnapshot(AggregateMock.class, uuid, 1))
        // Then
            .expectNext(new EventDescriptor(new EventVersion(1 ,0), new SnapshotCreated("aggregate1", 0)))
            .expectNext(new EventDescriptor(new EventVersion(1 ,1), new PointsAdded(100)))
            .verifyComplete();
    }

    @Test
    void findAllByAggregateIdSinceSnapshot_SnapshotInsertedAndTryToFindSinceNonExistingVersion_ReturnsNoEvent() {
        UUID uuid = UUID.fromString("2ac7eaa0-1323-11eb-b5d3-87470e4aeb38");

        // Given
        StepVerifier.create(reactiveCassandraEventDescriptorRepository.insertAll(AggregateMock.class, uuid,
            List.of(
                new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")),
                new EventDescriptor(new EventVersion(1, 0), new SnapshotCreated("aggregate1", 0)),
                new EventDescriptor(new EventVersion(1 ,1), new PointsAdded(100))
            )))
            .expectNext(true)
            .verifyComplete();

        // When
        StepVerifier.create(reactiveCassandraEventDescriptorRepository.findAllByAggregateIdSinceSnapshot(AggregateMock.class, uuid, 2))
        // Then
            .verifyComplete();
    }

    @Test
    void insertEventDescriptorsWithConflictingVersion_ReturnsMonoFalseAndNoEventFromBatchIsInserted() {
        UUID uuid = UUID.fromString("639899f0-1559-11eb-972a-a77f7d00b735");

        // Given
        StepVerifier.create(reactiveCassandraEventDescriptorRepository.insertAll(AggregateMock.class, uuid,
                List.of(
                    new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")),
                    new EventDescriptor(new EventVersion(0 ,2), new PointsAdded(100))
                )))
            .expectNext(true)
            .verifyComplete();

        // When
        StepVerifier.create(reactiveCassandraEventDescriptorRepository.insertAll(AggregateMock.class, uuid,
            List.of(
                new EventDescriptor(new EventVersion(0 ,3), new PointsAdded(150)),
                new EventDescriptor(new EventVersion(0 ,2), new PointsAdded(200))
            )))
        // Then
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(reactiveCassandraEventDescriptorRepository.findAllByAggregateId(AggregateMock.class, uuid))
            .expectNext(new EventDescriptor(new EventVersion(0 ,1), new AggregateCreated("aggregate1")))
            .expectNext(new EventDescriptor(new EventVersion(0 ,2), new PointsAdded(100)))
            .verifyComplete();
    }
}

@Aggregate(tableName = "test_aggregate")
class AggregateMock extends AggregateRoot {
    public AggregateMock(UUID id) {
        super(id);
    }
}

@Value
@DomainEvent(event = "AggregateCreated")
class AggregateCreated implements Event {
    String userName;

    @JsonCreator
    public AggregateCreated(@JsonProperty("userName") String userName) {
        this.userName = userName;
    }
}

@Value
@DomainEvent(event = "PointsAdded")
class PointsAdded implements Event {
    int points;

    @JsonCreator
    public PointsAdded(@JsonProperty("points") int points) {
        this.points = points;
    }
}

@Value
@DomainEvent(event = "SnapshotCreated")
class SnapshotCreated implements SnapshotEvent {
    String userName;
    int points;

    @JsonCreator
    public SnapshotCreated(@JsonProperty("userName") String userName, @JsonProperty("points") int points) {
        this.userName = userName;
        this.points = points;
    }
}
