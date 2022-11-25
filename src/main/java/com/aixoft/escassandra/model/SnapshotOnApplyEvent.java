package com.aixoft.escassandra.model;

/**
 * Abstract snapshot event to perform snapshot from aggregate data's version just before snapshot version.
 * <p>
 * Can be used if snapshot event is published in chain after some other events and all of then are committed together.
 * <p>
 * If loading of the aggregate from events is performed starting since version lower then snapshot version then aggregate will not be modified
 * according to snapshot event.
 * <p>
 * If loading of the aggregate from events is performed starting since snapshot version
 * then restoreAggregateData method will be used to update aggregate.
 * <p>
 *
 * @param <T> the type parameter
 */
public abstract class SnapshotOnApplyEvent<T> implements SnapshotEvent<T> {

    /**
     * Updates event data which will be persisted during aggregate commit based on aggregate data
     * (state of aggregate data just before event).
     *
     * @param aggregateData the aggregate data
     */
    protected abstract void updateEventDataOnApply(T aggregateData);

    /**
     * Restores aggregate data base on data included in snapshot event.
     * <p>
     * Restore happen only if aggregate is loaded from snapshot version.
     *
     * @return the aggregate data restored from snapshot event data.
     */
    protected abstract T restoreAggregateData();

    @Override
    public AggregateUpdater<T> updater() {
        return obj -> {
            if(obj != null) {
                updateEventDataOnApply(obj);
            } else {
                obj = restoreAggregateData();
            }

            return obj;
        };
    }


}
