package com.aixoft.escassandra.component;

import com.datastax.oss.driver.api.core.CqlSession;

public interface CassandraSession {
    CqlSession getSession();
}
