package com.aixoft.escassandra.aggregate;

import com.aixoft.escassandra.exception.runtime.AggregateCreationException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Base class for each aggregate.
 * It is required that child Aggregate class has defined constructor with single parameter of type {@code UUID}.
 */
public abstract class AggregateRoot {
    private EventVersion committedVersion;
    private final UUID id;
    private final List<Event> changes = new LinkedList<>();

    /**
     * Constructor.
     * It is required that child Aggregate class has defined constructor with single parameter of type {@code UUID}.
     *
     * @param id UUID of the aggregate.
     */
    protected AggregateRoot(@NonNull UUID id) {
        this.id = id;
    }

    /**
     * Method results in adding event on changes queue.
     * It does not modify aggregate data connected with the event.
     *
     * <p>
     * Event will be applied on aggregate save: {@link com.aixoft.escassandra.service.AggregateStore#save(AggregateRoot)}
     * or {@link com.aixoft.escassandra.service.ReactiveAggregateStore#save(AggregateRoot)}
     *
     * @param event the event
     */
    protected void publish(@NonNull Event event) {
        changes.add(event);
    }

    /**
     * Gets id.
     *
     * @return UUID of the aggregate.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Committed version is version of the last event persisted in database.
     *
     * @return Committed version.
     */
    public EventVersion getCommittedVersion() {
        return committedVersion;
    }

    /**
     * Internal use only. Sets committed version.
     *
     * @param committedVersion Version of last committed event.
     */
    public void setCommittedVersion(EventVersion committedVersion) {
        this.committedVersion = committedVersion;
    }

    /**
     * Gets uncommitted events.
     *
     * @return All events which were published for the aggregate but are not persisted in the database.
     */
    public List<Event> getUncommittedEvents() {
        return changes;
    }

    /**
     * Internal use only. Removed all uncommitted events.
     */
    public void markEventsAsCommitted() {
        changes.clear();
    }

    @Override
    public String toString() {
        return "AggregateRoot{" +
            "committedVersion=" + committedVersion +
            ", id=" + id +
            '}';
    }

    /**
     * Creates Aggregate by given type and UUID.
     * It is required that Aggregate class has defined constructor with single parameter of type {@code UUID}.
     *
     * @param <T>            Type of the aggregate.
     * @param aggregateId    UUID of the aggregate.
     * @param aggregateClass Aggregate class.
     *
     * @return New instance of Aggregate with given type.
     */
    public static <T extends AggregateRoot> T create(UUID aggregateId, Class<T> aggregateClass) {
        T aggregateRoot;
        try {
            aggregateRoot = aggregateClass.getDeclaredConstructor(UUID.class).newInstance(aggregateId);
        } catch (ReflectiveOperationException ex) {
            throw new AggregateCreationException(String.format("Not able to create instance of '%s'.", aggregateClass.getName()), ex);
        }
        return aggregateRoot;
    }
}
