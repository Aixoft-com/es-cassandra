package com.aixoft.escassandra.aggregate;

import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotEvent;
import com.aixoft.escassandra.repository.model.EventDescriptor;
import lombok.NonNull;

import java.util.*;

/**
 * Base class for each aggregate.
 *
 * @param <T> Aggregate data type parameter.
 */
public abstract class AggregateRoot<T> {
    private final UUID id;
    private EventVersion committedVersion;
    private EventVersion currentVersion;
    private final LinkedList<EventDescriptor> changes = new LinkedList<>();

    /**
     * Constructs base for the aggregate.
     *
     * @param id UUID of the aggregate.
     */
    protected AggregateRoot(@NonNull UUID id) {
        this.id = id;
    }

    /**
     * Adds events to unpublished events' queue. It does not modify aggregate data connected with the event.
     * <p>
     * Updates aggregate's current version (See {@link EventVersion#getNextMinor()}).
     *
     * @param events the event list.
     */
    protected void publish(@NonNull Iterable<Event<T>> events) {
        for(Event<T> event: events) {
            currentVersion = currentVersion.getNextMinor();
            changes.add(new EventDescriptor(currentVersion, event));
        }
    }

    /**
     * Adds event to unpublished events' queue. It does not modify aggregate data connected with the event.
     * <p>
     * Updates aggregate's current version (See {@link EventVersion#getNextMinor()}).
     *
     * @param event the event.
     */
    protected void publish(@NonNull Event<T> event) {
        currentVersion = currentVersion.getNextMinor();
        changes.add(new EventDescriptor(currentVersion, event));
    }


    /**
     * Publishes snapshot event. It does not modify aggregate data connected with the event.
     * <p>
     * Updates aggregate's current version (See {@link EventVersion#getNextMajor()}).
     *
     * @param event the snapshot event.
     */
    protected void publishSnapshot(@NonNull SnapshotEvent<T> event) {
        currentVersion = currentVersion.getNextMajor();
        changes.add(new EventDescriptor(currentVersion, event));
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
     * Gets committed version which indicates last committed event.
     *
     * @return The committed version.
     */
    public EventVersion getCommittedVersion() {
        return committedVersion;
    }


    /**
     * Sets committed version which indicates last committed event.
     *
     * @param committedVersion the committed version.
     */
    protected void setCommittedVersion(EventVersion committedVersion) {
        this.committedVersion = committedVersion;
    }

    /**
     * Gets current version which indicates last added event.
     *
     * @return The current version.
     */
    public EventVersion getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Sets current version which indicates last added event.
     *
     * @param currentVersion the current version.
     */
    protected void setCurrentVersion(EventVersion currentVersion) {
        this.currentVersion = currentVersion;
    }

    /**
     * Gets unmodifiable list of uncommitted events.
     *
     * @return All events which were published for the aggregate but not committed.
     */
    public List<EventDescriptor> getUncommittedEvents() {
        return Collections.unmodifiableList(changes);
    }

    /**
     * Gets original list of uncommitted events.
     *
     * @return All events which were published for the aggregate but not committed.
     */
    protected LinkedList<EventDescriptor> getOriginalUncommittedEvents() {
        return changes;
    }

    @Override
    public String toString() {
        return this.getClass().getName()
            +"(id=" + id
            + ", committedVersion=" + committedVersion
            + ", currentVersion=" + currentVersion
            + ", changes=" + this.changes
            + ")";
    }
}
