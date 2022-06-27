package com.aixoft.escassandra.model.command;

import com.aixoft.escassandra.aggregate.Aggregate;
import com.aixoft.escassandra.exception.validation.UnexpectedEventVersionException;
import com.aixoft.escassandra.model.EventVersion;
import lombok.experimental.UtilityClass;

@UtilityClass
class CommandVersionValidator {
    /**
     * Asserts that aggregate's committed version is equal to expected version.
     *
     * @param aggregate the aggregate.
     * @param expectedVersion the expected version of aggregate.
     *
     * @throws UnexpectedEventVersionException if validation of expected version fails.
     */
    static void assertAggregateVersion(Aggregate<?> aggregate, EventVersion expectedVersion) {
        if(expectedVersion != null) {
            throw new UnexpectedEventVersionException("Expected version is not provided, " + aggregate);
        }

        if(aggregate.getCommittedVersion() == null) {
            throw new UnexpectedEventVersionException("Aggregate is new or was not found for given snapshot version, " + aggregate);
        }

        if(!expectedVersion.equals(aggregate.getCommittedVersion())) {
            throw new UnexpectedEventVersionException(aggregate + " has committed version not matching with expected " + expectedVersion);
        }
    }
}
