package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
import com.aixoft.escassandra.component.util.TableNameUtil;
import com.aixoft.escassandra.config.EsCassandraProperties;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class CassandraSessionComponent implements CassandraSession, InitializingBean {
    private static final String CREATE_KEYSPACE_FORMAT = "CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : %d};";
    private static final String DROP_KEYSPACE_IF_EXISTS_FORMAT = "DROP KEYSPACE IF EXISTS %s";
    private static final String CREATE_TABLE_IF_NOT_EXISTS_FORMAT = "CREATE TABLE IF NOT EXISTS %s.%s (aggregateId uuid, majorVersion int,minorVersion int, event text, PRIMARY KEY(aggregateId, majorVersion, minorVersion));";
    private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s.%s (aggregateId uuid, majorVersion int, minorVersion int, event text, PRIMARY KEY(aggregateId, majorVersion, minorVersion));";
    private static final String DROP_TABLE_IF_EXISTS_FORMAT = "DROP TABLE IF EXISTS %s.%s";

    private final EsCassandraProperties esCassandraProperties;
    private final AggregateComponent aggregateComponent;
    private CqlSession session;

    public CassandraSessionComponent(@NonNull EsCassandraProperties esCassandraProperties, @NonNull AggregateComponent aggregateComponent) {
        this.esCassandraProperties = esCassandraProperties;
        this.aggregateComponent = aggregateComponent;
    }

    @Override
    public CqlSession getSession() {
        return session;
    }

    @Override
    public void afterPropertiesSet() {
        doSchemaAction();

        session = buildSession(true);
    }

    private void doSchemaAction() {
        switch (esCassandraProperties.getSchemaAction()) {
            case CREATE -> executeSchemaActionQuery(
                    cqlSession -> cqlSession.execute(String.format(CREATE_KEYSPACE_FORMAT, esCassandraProperties.getKeyspace(), esCassandraProperties.getReplicationFactor())),
                    (cqlSession, table) -> cqlSession.execute(String.format(CREATE_TABLE_FORMAT, esCassandraProperties.getKeyspace(), table))
                );
            case CREATE_IF_NOT_EXISTS -> executeSchemaActionQuery(
                    cqlSession -> cqlSession.execute(String.format(CREATE_KEYSPACE_FORMAT, esCassandraProperties.getKeyspace(), esCassandraProperties.getReplicationFactor())),
                    (cqlSession, table) -> cqlSession.execute(String.format(CREATE_TABLE_IF_NOT_EXISTS_FORMAT, esCassandraProperties.getKeyspace(), table))
                );
            case RECREATE -> executeSchemaActionQuery(
                    cqlSession -> cqlSession.execute(String.format(CREATE_KEYSPACE_FORMAT, esCassandraProperties.getKeyspace(), esCassandraProperties.getReplicationFactor())),
                    (cqlSession, table) -> {
                        cqlSession.execute(String.format(DROP_TABLE_IF_EXISTS_FORMAT, esCassandraProperties.getKeyspace(), table));
                        cqlSession.execute(String.format(CREATE_TABLE_FORMAT, esCassandraProperties.getKeyspace(), table));
                    }
                );
            case RECREATE_DROP_UNUSED -> executeSchemaActionQuery(
                    cqlSession -> {
                        cqlSession.execute(String.format(DROP_KEYSPACE_IF_EXISTS_FORMAT, esCassandraProperties.getKeyspace()));
                        cqlSession.execute(String.format(CREATE_KEYSPACE_FORMAT, esCassandraProperties.getKeyspace(), esCassandraProperties.getReplicationFactor()));
                    },
                    (cqlSession, table) -> cqlSession.execute(String.format(CREATE_TABLE_FORMAT, esCassandraProperties.getKeyspace(), table))
                );
        }
    }

    private void executeSchemaActionQuery(
        Consumer<CqlSession> sessionConsumer,
        BiConsumer<CqlSession, String> tableSessionConsumer)
    {
        try(CqlSession tempSession = buildSession(false)) {
            sessionConsumer.accept(tempSession);

            aggregateComponent.getClasses().stream()
                .map(TableNameUtil::fromAggregateClass)
                .forEach(table -> tableSessionConsumer.accept(tempSession, table));
        }
    }
    private CqlSession buildSession(boolean withKeyspace) {
        CqlSessionBuilder builder = new CqlSessionBuilder();
        builder
            .addContactPoint(
                new InetSocketAddress(esCassandraProperties.getIp(), Integer.parseInt(esCassandraProperties.getPort())))
            .withLocalDatacenter(esCassandraProperties.getLocalDatacenter());

        if(withKeyspace) {
            builder.withKeyspace(esCassandraProperties.getKeyspace());
        } else {
            builder.withConfigLoader(getDriverConfigLoader());
        }

        return builder.build();
    }

    private DriverConfigLoader getDriverConfigLoader() {
        return DriverConfigLoader.programmaticBuilder()
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(20))
            .build();
    }
}
