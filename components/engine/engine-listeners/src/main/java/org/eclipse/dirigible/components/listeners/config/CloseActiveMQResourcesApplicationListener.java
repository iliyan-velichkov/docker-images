/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible
 * contributors SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.components.listeners.config;

import javax.jms.Connection;
import javax.jms.Session;
import org.apache.activemq.broker.BrokerService;
import org.eclipse.dirigible.components.listeners.service.ListenersManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

/**
 * The listener interface for receiving closeActiveMQResourcesApplication events. The class that is
 * interested in processing a closeActiveMQResourcesApplication event implements this interface, and
 * the object created with that class is registered with a component using the component's
 * <code>addCloseActiveMQResourcesApplicationListener<code> method. When the
 * closeActiveMQResourcesApplication event occurs, that object's appropriate method is invoked.
 *
 * @see CloseActiveMQResourcesApplicationEvent
 */
@Component
class CloseActiveMQResourcesApplicationListener implements ApplicationListener<ApplicationEvent> {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseActiveMQResourcesApplicationListener.class);

    /** The broker. */
    private final BrokerService broker;

    /** The connection. */
    private final Connection connection;

    /** The session. */
    private final Session session;

    /** The listeners manager. */
    private final ListenersManager listenersManager;

    /**
     * Instantiates a new close active MQ resources application listener.
     *
     * @param broker the broker
     * @param connection the connection
     * @param session the session
     * @param listenersManager the listeners manager
     */
    @Autowired
    CloseActiveMQResourcesApplicationListener(BrokerService broker, @Qualifier("ActiveMQConnection") Connection connection,
            @Qualifier("ActiveMQSession") Session session, ListenersManager listenersManager) {
        this.broker = broker;
        this.connection = connection;
        this.session = session;
        this.listenersManager = listenersManager;
    }

    /**
     * On application event.
     *
     * @param event the event
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (isApplicableEvent(event)) {
            closeResources(event);
        }
    }

    /**
     * Checks if is applicable event.
     *
     * @param event the event
     * @return true, if is applicable event
     */
    private boolean isApplicableEvent(ApplicationEvent event) {
        return event instanceof ContextStoppedEvent || event instanceof ContextClosedEvent;
    }

    /**
     * Close resources.
     *
     * @param event the event
     */
    private void closeResources(ApplicationEvent event) {
        LOGGER.info("Closing ActiveMQ resources due to event {}", event);
        stopListeners();
        closeResource(session);
        closeResource(connection);
        stopBroker();
    }

    /**
     * Stop listeners.
     */
    private void stopListeners() {
        try {
            listenersManager.stopListeners();
        } catch (RuntimeException ex) {
            LOGGER.warn("Failed to stop listeners", ex);
        }
    }

    /**
     * Close resource.
     *
     * @param closeable the closeable
     */
    private void closeResource(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception ex) {
            LOGGER.warn("Failed to close {}", closeable, ex);
        }
    }

    /**
     * Stop broker.
     */
    private void stopBroker() {
        try {
            broker.stop();
        } catch (Exception ex) {
            LOGGER.warn("Failed to close broker {}", broker, ex);
        }
    }
}
