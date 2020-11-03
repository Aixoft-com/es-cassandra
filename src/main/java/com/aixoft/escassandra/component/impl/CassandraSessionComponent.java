package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.component.registrar.AggregateComponent;
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

@Slf4j
public class CassandraSessionComponent implements CassandraSession, InitializingBean {

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
            try(CqlSession tempSession = buildSession(false)) {

                if(keyspaceModifier != null) {
                    keyspaceModifier.accept(tempSession);
                }

                if(tableModifier != null) {
                    aggregateComponent.getClasses().stream()
                        .map(TableNameUtil::fromAggregateClass)
                        .forEach(table -> tableModifier.accept(tempSession, table));
                }
            }
        }
    }
    private CqlSession buildSession(boolean withKeyspace) {
        CqlSessionBuilder builder = new CqlSessionBuilder();
        builder
            .addContactPoints(getContactPoints())
            .withLocalDatacenter(esCassandraProperties.getLocalDatacenter());

        if(withKeyspace) {
            builder.withKeyspace(esCassandraProperties.getKeyspace());
        } else {
            builder.withConfigLoader(getDriverConfigLoader());
        }

        return builder.build();
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

    private DriverConfigLoader getDriverConfigLoader() {
        return DriverConfigLoader.programmaticBuilder()
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(20))
            .build();
    }
}
