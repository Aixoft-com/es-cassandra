package com.aixoft.escassandra.model;

import lombok.Value;

@Value
public class EventVersion {
    int major;
    int minor;

    public EventVersion getNext(boolean isSnapshot) {
        return isSnapshot ? new EventVersion(major + 1, 0)
            : new EventVersion(major, minor + 1);
    }

    public static EventVersion initial() {
        return new EventVersion(0, 0);
    }


}
