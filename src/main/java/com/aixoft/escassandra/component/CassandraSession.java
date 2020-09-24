package com.aixoft.escassandra.component;

import com.datastax.driver.core.Session;

public interface CassandraSession {
    Session getSession();
}
