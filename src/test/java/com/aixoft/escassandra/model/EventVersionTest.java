package com.aixoft.escassandra.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventVersionTest {

    @Test
    @DisplayName("Initial event version should be 0.0")
    public void shouldHaveInitialVersionZeroZero() {

        // when
        EventVersion ev = EventVersion.initial();

        // then
        assertEquals(0, ev.getMajor());
        assertEquals(0, ev.getMinor());
    }
}
