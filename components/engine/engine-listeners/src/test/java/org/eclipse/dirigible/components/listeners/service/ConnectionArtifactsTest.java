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
package org.eclipse.dirigible.components.listeners.service;

import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class ConnectionArtifactsTest.
 */
@ExtendWith(MockitoExtension.class)
class ConnectionArtifactsTest {

    /** The artifacts. */
    @InjectMocks
    private ConnectionArtifacts artifacts;

    /** The connection. */
    @Mock
    private Connection connection;

    /** The session. */
    @Mock
    private Session session;

    /** The message consumer. */
    @Mock
    private MessageConsumer messageConsumer;

    /**
     * Test close all.
     *
     * @throws JMSException the JMS exception
     */
    @Test
    void testCloseAll() throws JMSException {
        artifacts.closeAll();

        verifyClosedArtifacts();
    }

    /**
     * Test close all closes all resource on exception.
     *
     * @throws JMSException the JMS exception
     */
    @Test
    void testCloseAllClosesAllResourceOnException() throws JMSException {
        doThrow(IOException.class).when(messageConsumer)
                                  .close();
        doThrow(IOException.class).when(session)
                                  .close();
        doThrow(IOException.class).when(connection)
                                  .close();

        artifacts.closeAll();

        verifyClosedArtifacts();
    }

    /**
     * Verify closed artifacts.
     *
     * @throws JMSException the JMS exception
     */
    private void verifyClosedArtifacts() throws JMSException {
        InOrder inOrder = Mockito.inOrder(messageConsumer, session, connection);

        inOrder.verify(messageConsumer)
               .close();

        inOrder.verify(session)
               .close();

        inOrder.verify(connection)
               .close();
    }
}
