package com.aixoft.escassandra.service;

import java.io.Serializable;

/**
 * Interface to indicate the lister of events.
 * <p>
 * Class which implements this interface can be scanned for
 * {@link com.aixoft.escassandra.annotation.SubscribeAll} annotations during listener registration.
 */
public interface EventListener extends Serializable {
}
