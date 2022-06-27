package com.aixoft.escassandra.model.command;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.exception.validation.UnexpectedEventVersionException;
import com.aixoft.escassandra.model.Event;
import com.aixoft.escassandra.model.EventVersion;
import com.aixoft.escassandra.model.SnapshotCommand;

/**
 * Abstraction for command interface.
 * Shall be used to perform validation of expected aggregate version before validation of business logic.
 * If validation of expected version fails then {@link UnexpectedEventVersionException} is thrown.
 * @param <T> the type of aggregate data.
 */
public abstract class VersionedSnapshotCommand<T> implements SnapshotCommand<T> {

    public abstract EventVersion getExpectedVersion();

    /**
     * Validates command against aggregate data.
     * Validation happens only if version validation succeed.
     * If validation returns 'false' then command will be ignored
     * and no {@link Event} will be published on the aggregate.
     * <p>
     * Method can throw an exception to indicate failure and break processing chain.
     *
     * @param aggregate the aggregate.
     * @return the result of validation.
     */
    public abstract boolean postValidate(Aggregate<T> aggregate);


    /**
     * Validates if aggregate's committed version is equal to expected version.
     * If validation succeed then post validation is performed.
     *
     * @param aggregate the aggregate.
     * @return the result of validation.
     * @throws UnexpectedEventVersionException if validation of expected version fails.
     */
    @Override
    public final boolean validate(Aggregate<T> aggregate) {
        CommandVersionValidator.assertAggregateVersion(aggregate, getExpectedVersion());

        return postValidate(aggregate);
    }
}

