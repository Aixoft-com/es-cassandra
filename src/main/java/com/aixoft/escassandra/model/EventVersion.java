package com.aixoft.escassandra.model;

import lombok.Value;

/**
 * Contains information about event version (major and minor).
 */
@Value
public class EventVersion {
    /**
     * Major version of the event. It indicates the snapshot number.
     * Value is incremented with each snapshot.
     */
    int major;
    /**
     * Minor version of the event. It indicates event number after previous snapshot.
     * Value is set to 0 after each snapshot.
     */
    int minor;

    /**
     * Gets next minor event version.
     * Shall be used for non-snapshot events and results in minor version increment only.
     *
     * @return event version with minor version incremented.
     */
    public EventVersion getNextMinor() {
        return new EventVersion(major, minor + 1);
    }

    /**
     * Gets next event version.
     * Shall be used for snapshot events and results in major version increment and minor version reset.
     *
     * @return event version with major version incremented and minor version reset.
     */
    public EventVersion getNextMajor() {
        return new EventVersion(major + 1, 0);
    }

    /**
     * Initial event version.
     *
     * @return Event version with major and minor versions equal 0.
     */
    public static EventVersion initial() {
        return new EventVersion(0, 0);
    }

    @Override
    public String toString() {
        return String.format("%d.%d", major, minor);
    }


}
