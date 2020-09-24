package com.aixoft.escassandra.aggregate;

import com.aixoft.escassandra.service.EventHandler;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class AggregateRoot implements EventHandler {
    private EventVersion committedVersion;
    private final UUID id;
    private final List<Event> changes = new LinkedList<>();

    protected AggregateRoot(@NonNull UUID id) {
        this.id = id;
    }

    protected void publish(@NonNull Event event) {
        changes.add(event);
    }

    public UUID getId() {
        return id;
    }

    public EventVersion getCommittedVersion() {
        return committedVersion;
    }

    public void setCommittedVersion(@NonNull EventVersion committedVersion) {
        this.committedVersion = committedVersion;
    }

    public List<Event> getUncommittedChanges() {
        return changes;
    }

    public void markChangesAsCommitted() {
        changes.clear();
    }

    @Override
    public String toString() {
        return "AggregateRoot{" +
                "committedVersion=" + committedVersion +
                ", id=" + id +
                '}';
    }
}
