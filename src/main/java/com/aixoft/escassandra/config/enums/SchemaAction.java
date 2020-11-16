package com.aixoft.escassandra.config.enums;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Enum which identify schema action to be performed at startup.
 */
public enum SchemaAction {
    /**
     * No schema action (default).
     */
    NONE {
        @Override
        public Consumer<CqlSession> getKeyspaceModifier(String keyspace, int replicationFactor) {
            return null;
        }

        @Override
        public BiConsumer<CqlSession, String> getTableModifier(String keyspace) {
            return null;
        }
    },

    /**
     * Create table for each aggregate, fail if table already exists.
     * Create keyspace if not exists.
     */
    CREATE {
        @Override
        public Consumer<CqlSession> getKeyspaceModifier(String keyspace, int replicationFactor) {
            return cqlSession -> cqlSession.execute(String.format(CREATE_KEYSPACE_IF_NOT_EXISTS_FORMAT, keyspace, replicationFactor));
        }

        @Override
        public BiConsumer<CqlSession, String> getTableModifier(String keyspace) {
            return (cqlSession, table) -> cqlSession.execute(String.format(CREATE_TABLE_IF_NOT_EXISTS_FORMAT, keyspace, table));
        }
    },

    /**
     * Create table for each aggregate only if not exists.
     * Create keyspace if not exists.
     */
    CREATE_IF_NOT_EXISTS {
        @Override
        public Consumer<CqlSession> getKeyspaceModifier(String keyspace, int replicationFactor) {
            return cqlSession -> cqlSession.execute(String.format(CREATE_KEYSPACE_IF_NOT_EXISTS_FORMAT, keyspace, replicationFactor));
        }

        @Override
        public BiConsumer<CqlSession, String> getTableModifier(String keyspace) {
            return (cqlSession, table) -> cqlSession.execute(String.format(CREATE_TABLE_IF_NOT_EXISTS_FORMAT, keyspace, table));
        }
    },

    /**
     * Create table for each aggregate if necessary, dropping the table if already exists.
     * Create keyspace if not exists.
     */
    RECREATE {
        @Override
        public Consumer<CqlSession> getKeyspaceModifier(String keyspace, int replicationFactor) {
            return cqlSession -> cqlSession.execute(String.format(CREATE_KEYSPACE_IF_NOT_EXISTS_FORMAT, keyspace, replicationFactor));
        }

        @Override
        public BiConsumer<CqlSession, String> getTableModifier(String keyspace) {
            return (cqlSession, table) -> {
                cqlSession.execute(String.format(DROP_TABLE_IF_EXISTS_FORMAT, keyspace, table));
                cqlSession.execute(String.format(CREATE_TABLE_FORMAT, keyspace, table));
            };
        }
    },

    /**
     * Drop all tables in the keyspace and then create table for each aggregate.
     * Create keyspace if not exists.
     */
    RECREATE_DROP_UNUSED {
        @Override
        public Consumer<CqlSession> getKeyspaceModifier(String keyspace, int replicationFactor) {
            return cqlSession -> {
                cqlSession.execute(String.format(DROP_KEYSPACE_IF_EXISTS_FORMAT, keyspace));
                cqlSession.execute(String.format(CREATE_KEYSPACE_IF_NOT_EXISTS_FORMAT, keyspace, replicationFactor));
            };
        }

        @Override
        public BiConsumer<CqlSession, String> getTableModifier(String keyspace) {
            return (cqlSession, table) -> cqlSession.execute(String.format(CREATE_TABLE_FORMAT, keyspace, table));
        }
    };

    /**
     * Gets keyspace modifier.
     *
     * @param keyspace          Keyspace name.
     * @param replicationFactor Replication factor.
     *
     * @return Keyspace modifier.
     */
    public abstract Consumer<CqlSession> getKeyspaceModifier(String keyspace, int replicationFactor);

    /**
     * Gets table modifier.
     *
     * @param keyspace Keyspace name.
     *
     * @return Table modifier.
     */
    public abstract BiConsumer<CqlSession, String> getTableModifier(String keyspace);

    private static final String CREATE_KEYSPACE_IF_NOT_EXISTS_FORMAT = "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : %d};";
    private static final String DROP_KEYSPACE_IF_EXISTS_FORMAT = "DROP KEYSPACE IF EXISTS %s";
    private static final String CREATE_TABLE_IF_NOT_EXISTS_FORMAT = "CREATE TABLE IF NOT EXISTS %s.%s (aggregateId uuid, majorVersion int,minorVersion int, event text, PRIMARY KEY(aggregateId, majorVersion, minorVersion));";
    private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s.%s (aggregateId uuid, majorVersion int, minorVersion int, event text, PRIMARY KEY(aggregateId, majorVersion, minorVersion));";
    private static final String DROP_TABLE_IF_EXISTS_FORMAT = "DROP TABLE IF EXISTS %s.%s";
}
