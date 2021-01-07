package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.registrar.AggregateDataComponent;
import com.aixoft.escassandra.component.util.TableNameUtil;
import com.aixoft.escassandra.config.EsCassandraProperties;
import com.aixoft.escassandra.config.enums.SchemaAction;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Configures cassandra CqlSession according to properties {@link EsCassandraProperties}.
 * Creates session attached to given keyspace.
 * <p>
 * If SchemaAction is different then 'NONE' then temporary session is created to initialize schema.
 */
@Slf4j
public class CassandraSessionComponent implements CassandraSession, InitializingBean {

    private final EsCassandraProperties esCassandraProperties;
    private final AggregateDataComponent aggregateDataComponent;
    private CqlSession session;

    /**
     * Instantiates a new Cassandra session component.
     *
     * @param esCassandraProperties Cassandra properties.
     * @param aggregateDataComponent    Aggregate component with aggregate classes.
     */
    public CassandraSessionComponent(@NonNull EsCassandraProperties esCassandraProperties, @NonNull AggregateDataComponent aggregateDataComponent) {
        this.esCassandraProperties = esCassandraProperties;
        this.aggregateDataComponent = aggregateDataComponent;
    }

    /**
     * @return Cassandra session.
     */
    @Override
    public CqlSession getSession() {
        return session;
    }

    @Override
    public void afterPropertiesSet() {
        doSchemaAction();

        session = buildSession(false);
    }

    private void doSchemaAction() {
        SchemaAction schemaAction = esCassandraProperties.getSchemaAction();

        executeSchemaActionQuery(
            schemaAction.getKeyspaceModifier(esCassandraProperties.getKeyspace(), esCassandraProperties.getReplicationFactor()),
            schemaAction.getTableModifier(esCassandraProperties.getKeyspace())
        );
    }

    private void executeSchemaActionQuery(
        Consumer<CqlSession> keyspaceModifier,
        BiConsumer<CqlSession, String> tableModifier)
    {
        if(keyspaceModifier != null || tableModifier != null) {
            try(CqlSession tempSession = buildSession(true)) {

                if(keyspaceModifier != null) {
                    keyspaceModifier.accept(tempSession);
                }

                if(tableModifier != null) {
                    aggregateDataComponent.getClasses().stream()
                        .map(TableNameUtil::fromAggregateDataClass)
                        .forEach(table -> tableModifier.accept(tempSession, table));
                }
            }
        }
    }
    private CqlSession buildSession(boolean forSchemaAction) {
        CqlSessionBuilder builder = new CqlSessionBuilder();

        if(forSchemaAction) {
            builder.withConfigLoader(getDriverConfigLoaderWithExtendedRequestTimeout());
        } else {
            builder.withKeyspace(esCassandraProperties.getKeyspace());
        }

        return builder
            .withLocalDatacenter(esCassandraProperties.getLocalDatacenter())
            .addContactPoints(getContactPoints())
            .build();
    }

    private Collection<InetSocketAddress> getContactPoints() {
        return esCassandraProperties.getContactPoints()
            .stream()
            .map(this::parseInetSocketAddress)
            .collect(Collectors.toList());
    }

    private InetSocketAddress parseInetSocketAddress(String addressPortStr) {
        URI uri = URI.create("//"  + addressPortStr);

        return new InetSocketAddress(uri.getHost(), uri.getPort());
    }

    private DriverConfigLoader getDriverConfigLoaderWithExtendedRequestTimeout() {
        return DriverConfigLoader.programmaticBuilder()
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(20))
            .build();
    }
}
