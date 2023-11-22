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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The Class SynchronizationWatcher.
 */
@Component
@Scope("singleton")
public class SynchronizationWatcher {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SynchronizationWatcher.class);

    /** The modified. */
    private AtomicBoolean modified = new AtomicBoolean(false);

    /**
     * Initialize.
     *
     * @param folder the folder
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    public void initialize(String folder) throws IOException, InterruptedException {
        logger.debug("Initializing the Registry file watcher...");

        WatchService watchService = FileSystems.getDefault()
                                               .newWatchService();
        Path path = Paths.get(folder);
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        Executors.newFixedThreadPool(1)
                 .submit(() -> {
                     WatchKey watchKey;
                     try {
                         while ((watchKey = watchService.take()) != null) {
                             List<WatchEvent<?>> events = watchKey.pollEvents();
                             if (!events.isEmpty()) {
                                 modified.set(true);
                             }
                             watchKey.reset();
                         }
                     } catch (InterruptedException e) {
                         logger.error("Failed to take watch keys", e);
                     }
                 });

        logger.debug("Done initializing the Registry file watcher.");
    }

    /**
     * Register the given directory and all its sub-directories with the WatchService.
     *
     * @param start the start
     * @param watchService the watch service
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void registerAll(final Path start, WatchService watchService) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }

        });

    }

    /**
     * Checks if is modified.
     *
     * @return true, if is modified
     */
    public boolean isModified() {
        return modified.get();
    }

    /**
     * Reset.
     */
    public void reset() {
        modified.set(false);
    }

    /**
     * Force.
     */
    public void force() {
        modified.set(true);
    }

}
