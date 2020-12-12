package com.aixoft.escassandra.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventVersionTest {

    @Test
    void getNext_NoSnapshotEvent_IncrementMinor() {
        EventVersion eventVersion = new EventVersion(1, 1);

        EventVersion nextVersion = eventVersion.getNext(false);

        assertEquals(1, nextVersion.getMajor());
        assertEquals(2, nextVersion.getMinor());
    }

    @Test
    void getNext_napshotEvent_IncrementMajorAndResetMinor() {
        EventVersion eventVersion = new EventVersion(2, 2);

        EventVersion nextVersion = eventVersion.getNext(true);

        assertEquals(3, nextVersion.getMajor());
        assertEquals(0, nextVersion.getMinor());
    }
}
