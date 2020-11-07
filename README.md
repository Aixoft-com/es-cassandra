# Project description

EsCassandra is Spring Boot library that implements Event Sourcing pattern which can be used together with CQRS.

It is designed to persist data in Cassandra database which is:
- designed to manage massive amounts of data,
- fast,
- proven,
- fault tolerant,
- performant,
- decentralized,
- scalable,
- durable.

More info: https://cassandra.apache.org/

### Benefits:
- Easy to use.
- Annotation based.
- Spring Boot auto-configuration.
- Snapshotting.
- Event Versioning.
- Automatic schema generation.
- Support for Reactive Programming.
- All advantages of Cassandra usage.

## Usage

Following repository contains examples of usage:
https://bitbucket.org/aixoft/escassandra-sample

## Configuration

### Aggregate schema

Table shall be created per each aggregate type, cassandra definition:

    CREATE TABLE <keyspace>.<aggregate_table> (
        aggregateId uuid,
        majorVersion int,
        minorVersion int,
        event text,
        PRIMARY KEY(aggregateId, majorVersion, minorVersion)
    );

aggregateId:
- Identifies aggregate instance.
- Aggregate with different aggregateId can be located on different nodes (partition key)

majorVersion:
- Part of event version which describes the previous snapshot number.

minorVersion:
- Part of event version which describes the event index after snapshot creation.

event:
- JSON serialized event with name specified by @DomainEvent annotation.

### EsCassandra Configuration

Properties which can used for EsCassandra:

    escassandra.local-datacenter

        - The default policy will only include nodes from this datacenter in its query plans.
        - It take precedence over datastax-java-driver property.
        - Default: 'datacenter1'

    escassandra.contact-points

        - List of contact points (hostname:port).
        - It take precedence over datastax-java-driver property.
        - Default: '127.0.0.1:9042"

    escassandra.keyspace

        - The name of the keyspace that the session should initially be connected to.
        - Value has to be alphanumeric + '_' character
        - Default: 'eskeyspace'.

    escassandra.schema-action

        - Describe schema action to be performed on application startup.
        - Keyspace is created with 'SimpleStrategy' and replication defined by 'replicationFactor' property.
        - Default: 'NONE'

        Options:
        NONE
            - No schema action (default).
        CREATE
            - Create table for each aggregate, fail if table already exists.
            - Create keyspace if not exists.
        CREATE_IF_NOT_EXISTS
            - Create table for each aggregate only if not exists.
            - Create keyspace if not exists.
        RECREATE
            - Create table for each aggregate if necessary, dropping the table if already exists.
            - Create keyspace if not exists.
        RECREATE_DROP_UNUSED
            - Drop all tables in the keyspace and then create table for each aggregate.
            - Create keyspace if not exists.

    escassandra.replication-factor

        - Define replica factor used for keyspace creation.
        - Relevant only if SchemaAction is different then NONE.
        - Default: '1'.

### Datastax Driver Configuration

Configuration for cassandra driver can be specified in:

    application.conf
    application.json
    application.properties
    reference.conf

Example with application.properties:

    datastax-java-driver.basic.request.timeout = 5 seconds

Example with application.conf:

    datastax-java-driver {
        basic {
            config-reload-interval = 5 minutes
            request.timeout = 10 seconds
        }
    }

Driver configuration does not support .yml files.

More info on:

    https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/configuration/
    https://docs.datastax.com/en/developer/java-driver/4.9/manual/core/configuration/reference/
