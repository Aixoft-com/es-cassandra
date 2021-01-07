package com.aixoft.escassandra.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventVersionTest {

    @Test
    void getNextMinor_ReturnsNewEventVersionWithMinorIncremented() {
        EventVersion eventVersion = new EventVersion(1, 1);

        EventVersion nextVersion = eventVersion.getNextMinor();

        assertEquals(1, nextVersion.getMajor());
        assertEquals(2, nextVersion.getMinor());
    }

    @Test
    void getNextMajor_ReturnsNewEventVersionWithMajorIncrementedAndMinorReset() {
        EventVersion eventVersion = new EventVersion(2, 2);

        EventVersion nextVersion = eventVersion.getNextMajor();

        assertEquals(3, nextVersion.getMajor());
        assertEquals(0, nextVersion.getMinor());
    }
}
