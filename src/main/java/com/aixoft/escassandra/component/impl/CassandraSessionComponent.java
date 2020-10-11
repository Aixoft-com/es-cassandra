package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.config.EsCassandraProperties;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import lombok.NonNull;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

public class CassandraSessionComponent implements CassandraSession {
    private final EsCassandraProperties esCassandraProperties;
    private CqlSession session;

    public CassandraSessionComponent(@NonNull EsCassandraProperties esCassandraProperties) {
        this.esCassandraProperties = esCassandraProperties;
    }

    @PostConstruct
    public void initSession() {
        CqlSessionBuilder builder = new CqlSessionBuilder();
        session = builder
            .addContactPoint(
                new InetSocketAddress(esCassandraProperties.getIp(), Integer.parseInt(esCassandraProperties.getPort())))
            .withLocalDatacenter(esCassandraProperties.getLocalDatacenter())
            .withKeyspace(esCassandraProperties.getKeyspace())
            .build();
    }

    @Override
    public CqlSession getSession() {
        return session;
    }
}
