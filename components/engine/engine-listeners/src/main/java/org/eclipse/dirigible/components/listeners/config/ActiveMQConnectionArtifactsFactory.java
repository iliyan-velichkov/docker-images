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
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A factory for creating ActiveMQConnectionArtifacts objects.
 */
@Component
public class ActiveMQConnectionArtifactsFactory {

    /** The connection factory. */
    private final ActiveMQConnectionFactory connectionFactory;

    /**
     * Instantiates a new active MQ connection artifacts factory.
     *
     * @param connectionFactory the connection factory
     */
    @Autowired
    ActiveMQConnectionArtifactsFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Creates a new ActiveMQConnectionArtifacts object.
     *
     * @param connection the connection
     * @return the session
     * @throws JMSException the JMS exception
     */
    public Session createSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Creates a new ActiveMQConnectionArtifacts object.
     *
     * @param exceptionListener the exception listener
     * @return the connection
     * @throws IllegalStateException the illegal state exception
     */
    public Connection createConnection(ExceptionListener exceptionListener) throws IllegalStateException {
        try {
            Connection connection = connectionFactory.createConnection();
            connection.setExceptionListener(exceptionListener);

            connection.start();

            return connection;
        } catch (JMSException ex) {
            throw new IllegalStateException("Failed to create connection to ActiveMQ", ex);
        }
    }

}
