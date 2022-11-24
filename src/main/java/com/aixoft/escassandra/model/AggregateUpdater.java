package com.aixoft.escassandra.model;

/**
 * The interface to update aggregate data.
 *
 * @param <T> the type of aggregate data.
 */
@FunctionalInterface
public interface AggregateUpdater<T> {

    /**
     * Updates aggregate data.
     *
     * <p>
     * For immutable aggregate data class, the copy shall be returned if method modifies input aggregate data.
     * If aggregate data class is mutable (e.g. for better performance), then the input aggregate data can be returned.
     *
     * @param aggregateData the input aggregate data.
     * @return the updated aggregate data.
     */
    T apply(T aggregateData);
}
