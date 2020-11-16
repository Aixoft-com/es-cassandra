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
     * Gets next event version.
     * Non-snapshot event results in minor version increment only.
     * Snapshot event results in major version increment and minor version reset.
     *
     * @param isSnapshot Specify if next version shall be created for snapshot event.
     *
     * @return Next event version.
     */
    public EventVersion getNext(boolean isSnapshot) {
        return isSnapshot ? new EventVersion(major + 1, 0)
            : new EventVersion(major, minor + 1);
    }

    /**
     * Initial event version.
     *
     * @return Event version with major and minor versions equal 0.
     */
    public static EventVersion initial() {
        return new EventVersion(0, 0);
    }


}
