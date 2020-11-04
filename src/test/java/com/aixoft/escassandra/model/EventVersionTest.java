package com.aixoft.escassandra.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventVersionTest {

    @Test
    @DisplayName("Initial event version should be 0.0")
    void shouldHaveInitialVersionZeroZero() {
        // when
        EventVersion ev = EventVersion.initial();

        // then
        assertEquals(0, ev.getMajor());
        assertEquals(0, ev.getMinor());
    }

    @Test
    @DisplayName("Should increment major version when snapshot")
    void shouldIncrementMinorWhenSnapshot() {
        // given
        EventVersion initial = EventVersion.initial();

        // when
        EventVersion eventVersion = initial.getNext(true);

        // then
        assertEquals(1, eventVersion.getMajor());
        assertEquals(0, eventVersion.getMinor());
    }

    @Test
    @DisplayName("Should increment minor version when not snapshot")
    void shouldIncrementMajorWhenNotSnapshot() {
        // given
        EventVersion initial = EventVersion.initial();

        // when
        EventVersion eventVersion = initial.getNext(false);

        // then
        assertEquals(0, eventVersion.getMajor());
        assertEquals(1, eventVersion.getMinor());
    }
}
