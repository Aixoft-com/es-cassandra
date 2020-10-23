package com.aixoft.escassandra.config.enums;
/**
 * Enum which identify schema action to be performed at startup
 */
public enum SchemaAction {
    /**
     * No schema action (default).
     */
    NONE,
    /**
     * Create keyspace if not exists.
     * Create table for each aggregate. Fail if table already exists.
     */
    CREATE,
    /**
     * Create keyspace if not exists.
     * Create table for each aggregate only if not exists.
     */
    CREATE_IF_NOT_EXISTS,
    /**
     * Create keyspace if not exists.
     * Create table for each aggregate if necessary, dropping the table if already exists.
     */
    RECREATE,
    /**
     * Create keyspace if not exists.
     * Drop all tables in the keyspace and then create table for each aggregate.
     */
    RECREATE_DROP_UNUSED
}
