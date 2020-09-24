package com.aixoft.escassandra.component.impl;

import com.aixoft.escassandra.component.CassandraSession;
import com.aixoft.escassandra.config.EsCassandraProperties;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lombok.NonNull;

import javax.annotation.PostConstruct;

public class CassandraSessionComponent implements CassandraSession {
    private final EsCassandraProperties esCassandraProperties;
    private Session session;

    public CassandraSessionComponent(@NonNull EsCassandraProperties esCassandraProperties) {
        this.esCassandraProperties = esCassandraProperties;
    }

    @PostConstruct
    public void initSession() {
        session = Cluster.builder()
            .withClusterName(esCassandraProperties.getLocalDatacenter())
            .withoutJMXReporting()
            .withPort(Integer.parseInt(esCassandraProperties.getPort()))
            .addContactPoint(esCassandraProperties.getIp())
            .build()
            .connect(esCassandraProperties.getKeyspace());
    }

    @Override
    public Session getSession() {
        return session;
    }
}
