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

import org.eclipse.dirigible.components.listeners.config.ActiveMQConnectionArtifactsFactory;
import org.eclipse.dirigible.components.listeners.domain.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A factory for creating ListenerManager objects.
 */
@Component
public class ListenerManagerFactory {

    /** The connection artifacts factory. */
    private final ActiveMQConnectionArtifactsFactory connectionArtifactsFactory;

    /**
     * Instantiates a new listener manager factory.
     *
     * @param connectionArtifactsFactory the connection artifacts factory
     */
    @Autowired
    public ListenerManagerFactory(ActiveMQConnectionArtifactsFactory connectionArtifactsFactory) {
        this.connectionArtifactsFactory = connectionArtifactsFactory;
    }

    /**
     * Creates the.
     *
     * @param listener the listener
     * @return the listener manager
     */
    public ListenerManager create(Listener listener) {
        return new ListenerManager(listener, connectionArtifactsFactory);
    }
}
