package com.aixoft.escassandra.model;

/**
 * Abstract initializing event which shall be used as first event published on the aggregate.
 *
 * @param <T> the type of aggregate data.
 */
public abstract class InitializingEvent<T> implements Event<T> {

    /**
     * Creates aggregate data.
     *
     * @return new instance of the aggregate data.
     */
    protected abstract T initialize();


    /**
     * Create initializer for aggregate data.
     *
     * @return aggregate updater which initialize aggregate data.
     */
    @Override
    public AggregateUpdater<T> createUpdater() {
        return obj -> initialize();
    }
}
