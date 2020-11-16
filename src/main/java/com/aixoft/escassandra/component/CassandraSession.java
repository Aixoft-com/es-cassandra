package com.aixoft.escassandra.component;

import com.datastax.oss.driver.api.core.CqlSession;

/**
 * The interface to get Cassandra session.
 */
public interface CassandraSession {
    /**
     * Gets session.
     *
     * @return Cassandra session.
     */
    CqlSession getSession();
}
