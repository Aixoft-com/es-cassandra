package com.aixoft.escassandra.model;

import lombok.Value;

@Value
public class EventVersion {
    int snapshotNumber;
    int eventNumber;

    public EventVersion getNext(boolean isSnapshot) {
        return isSnapshot ? new EventVersion(snapshotNumber + 1, 0)
            : new EventVersion(snapshotNumber, eventNumber + 1);
    }
}
