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
package org.eclipse.dirigible.components.initializers.synchronizer;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * The Class SynchronizationJobService.
 */
@Service
@Scope("singleton")
public class SynchronizationJobService {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(SynchronizationJobService.class);

    /** The count. */
    private AtomicInteger count = new AtomicInteger();

    /** The synchronization processor. */
    private final SynchronizationProcessor synchronizationProcessor;

    /**
     * Instantiates a new synchronizers initializer.
     *
     * @param synchronizationProcessor the synchronization processor
     */
    @Autowired
    public SynchronizationJobService(SynchronizationProcessor synchronizationProcessor) {
        this.synchronizationProcessor = synchronizationProcessor;
    }

    /**
     * Execute synchronization job.
     */
    public void executeSynchronizationJob() {

        if (logger.isDebugEnabled()) {
            logger.debug("Synchronization started...");
        }
        try {
            synchronizationProcessor.processSynchronizers();
        } finally {
            count.incrementAndGet();
            if (logger.isDebugEnabled()) {
                logger.debug("Synchronization finished.");
            }
        }
    }

    /**
     * Gets the number of invocations.
     *
     * @return the number of invocations
     */
    public int getNumberOfInvocations() {
        return count.get();
    }

}
