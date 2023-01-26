package com.aixoft.escassandra.model;

import com.aixoft.escassandra.aggregate.Aggregate;


/**
 * The aggregate command interface.
 *
 * @param <T> the type of aggregate data.
 */
public interface Command<T> {

    /**
     * Validates command against aggregate data.
     * If validation returns 'false' then command will be ignored
     * and no {@link Event} will be published on the aggregate.
     * <p>
     * Method can throw an exception to indicate failure and break processing chain.
     *
     * @param aggregate the aggregate.
     * @return the result of validation.
     */
    default boolean validate(Aggregate<T> aggregate) {
        return true;
    }

    /**
     * Converts command to list of events.
     *
     * @return the list of events generated by command.
     */
    Iterable<Event<T>> toEvents(Aggregate<T> aggregate);
}
