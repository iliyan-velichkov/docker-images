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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.dirigible.components.api.platform.ProblemsFacade;
import org.eclipse.dirigible.components.base.artefact.Artefact;
import org.eclipse.dirigible.components.base.artefact.ArtefactLifecycle;
import org.eclipse.dirigible.components.base.artefact.ArtefactPhase;
import org.eclipse.dirigible.components.base.artefact.topology.TopologicalDepleter;
import org.eclipse.dirigible.components.base.artefact.topology.TopologicalSorter;
import org.eclipse.dirigible.components.base.artefact.topology.TopologyFactory;
import org.eclipse.dirigible.components.base.artefact.topology.TopologyWrapper;
import org.eclipse.dirigible.components.base.healthcheck.status.HealthCheckStatus;
import org.eclipse.dirigible.components.base.healthcheck.status.HealthCheckStatus.Jobs.JobStatus;
import org.eclipse.dirigible.components.base.synchronizer.Synchronizer;
import org.eclipse.dirigible.components.base.synchronizer.SynchronizerCallback;
import org.eclipse.dirigible.components.ide.problems.domain.Problem;
import org.eclipse.dirigible.components.initializers.definition.Definition;
import org.eclipse.dirigible.components.initializers.definition.DefinitionService;
import org.eclipse.dirigible.components.initializers.definition.DefinitionState;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.api.IRepositoryStructure;
import org.eclipse.dirigible.repository.local.LocalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The Class SynchronizationProcessor.
 */
@Component
@Scope("singleton")
public class SynchronizationProcessor implements SynchronizationWalkerCallback, SynchronizerCallback {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SynchronizationProcessor.class);

    /** The definitions. */
    private Map<Synchronizer<Artefact>, Map<String, Definition>> definitions = Collections.synchronizedMap(new HashMap<>());

    /** The artefacts. */
    private Map<String, Artefact> artefacts = Collections.synchronizedMap(new HashMap<>());

    /** The repository. */
    private IRepository repository;

    /** The synchronizers. */
    private final List<Synchronizer<Artefact>> synchronizers;

    /** The errors. */
    private List<String> errors = Collections.synchronizedList(new ArrayList<>());

    /** The definition service. */
    private DefinitionService definitionService;

    /** The synchronization watcher. */
    private SynchronizationWatcher synchronizationWatcher;

    /** The initialized. */
    private AtomicBoolean initialized = new AtomicBoolean(false);

    /** The prepared. */
    private AtomicBoolean prepared = new AtomicBoolean(false);

    /** The processing. */
    private AtomicBoolean processing = new AtomicBoolean(false);

    /**
     * Instantiates a new synchronization processor.
     *
     * @param repository the repository
     * @param synchronizers the synchronizers
     * @param definitionService the definition service
     * @param synchronizationWatcher the synchronization watcher
     */
    @Autowired
    public SynchronizationProcessor(IRepository repository, List<Synchronizer<Artefact>> synchronizers, DefinitionService definitionService,
            SynchronizationWatcher synchronizationWatcher) {
        this.repository = repository;
        this.synchronizers = Collections.synchronizedList(synchronizers);
        this.definitionService = definitionService;
        this.synchronizationWatcher = synchronizationWatcher;
        this.synchronizers.forEach(s -> s.setCallback(this));
    }

    /**
     * Prepare synchronizers.
     */
    public synchronized void prepareSynchronizers() {
        this.synchronizers.forEach(s -> s.getService()
                                         .getAll()
                                         .forEach(a -> {
                                             a.setRunning(false);
                                             s.getService()
                                              .save(a);
                                         }));
        try {
            this.synchronizationWatcher.initialize(getRegistryFolder());
            prepared.set(true);
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Process synchronizers.
     */
    public synchronized void processSynchronizers() {

        if (!this.synchronizationWatcher.isModified() && initialized.get()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipped synchronization as no changes in the Registry.");
            }
            return;
        }

        if (!prepared.get()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipped synchronization as the runtime is not prepared yet.");
            }
            return;
        }

        if (processing.get()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipped synchronization as it is currently in progress.");
            }
            return;
        }

        processing.set(true);

        if (logger.isDebugEnabled()) {
            logger.debug("Processing synchronizers started...");
        }

        try {

            prepare();

            // prepare map
            synchronizers.forEach(s -> definitions.put(s, Collections.synchronizedMap(new HashMap<>())));

            if (logger.isTraceEnabled()) {
                logger.trace("Collecting files...");
            }

            // collect definitions for processing
            collectFiles();

            if (logger.isDebugEnabled()) {
                logger.debug("Collecting files done. {} known types of definitions collected - {}.", synchronizers.size(),
                        synchronizers.stream()
                                     .map(Synchronizer::getArtefactType)
                                     .collect(Collectors.toUnmodifiableList()));
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Loading definitions...");
            }

            // mark the deleted definitions, which previousely processed
            markDeleted();

            // parse definitions to artefacts
            parseDefinitions();

            int countNew = 0;
            int countModified = 0;
            for (Artefact artefact : artefacts.values()) {
                if (ArtefactLifecycle.NEW.equals(artefact.getLifecycle())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Processing a new artefact: {}", artefact.getKey());
                    }
                    countNew++;
                } ;
                if (ArtefactLifecycle.MODIFIED.equals(artefact.getLifecycle())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Processing a modified artefact: {}", artefact.getKey());
                    }
                    countModified++;
                } ;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Loading of {} definitions done. {} artefacts found in total. {} new and {} modified.", definitions.values()
                                                                                                                                .size(),
                        artefacts.size(), countNew, countModified);
            }

            if (countNew > 0 || countModified > 0 || !initialized.get()) {

                TopologicalSorter<TopologyWrapper<? extends Artefact>> sorter = new TopologicalSorter<>();
                TopologicalDepleter<TopologyWrapper<? extends Artefact>> depleter = new TopologicalDepleter<>();

                List<TopologyWrapper<? extends Artefact>> wrappers = TopologyFactory.wrap(artefacts.values(), synchronizers);

                if (logger.isTraceEnabled()) {
                    logger.trace("Topological sorting...");
                }

                // topological sorting by dependencies
                wrappers = sorter.sort(wrappers);

                // reverse the order
                Collections.reverse(wrappers);

                if (logger.isTraceEnabled()) {
                    logger.trace("Preparing for processing...");
                }

                List<TopologyWrapper<? extends Artefact>> undepleted = new ArrayList<>();

                // preparing and depleting
                for (Synchronizer<? extends Artefact> synchronizer : synchronizers) {
                    List<TopologyWrapper<? extends Artefact>> unmodifiable = wrappers.stream()
                                                                                     .filter(w -> w.getSynchronizer()
                                                                                                   .equals(synchronizer))
                                                                                     .collect(Collectors.toUnmodifiableList());
                    try {
                        List<TopologyWrapper<? extends Artefact>> results = depleter.deplete(unmodifiable, ArtefactPhase.PREPARE);
                        undepleted.addAll(results);
                        registerErrors(results, ArtefactLifecycle.PREPARED);
                    } catch (Exception e) {
                        if (logger.isErrorEnabled()) {
                            logger.error(e.getMessage(), e);
                        }
                        addError(e.getMessage());
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Preparing for processing done.");
                }

                // return back to the sorted the order
                Collections.reverse(wrappers);

                if (logger.isTraceEnabled()) {
                    logger.trace("Processing of artefacts...");
                }
                // processing and depleting
                for (Synchronizer<? extends Artefact> synchronizer : synchronizers) {
                    HealthCheckStatus.getInstance()
                                     .getJobs()
                                     .setStatus(synchronizer.getClass()
                                                            .getSimpleName(),
                                             JobStatus.Running);
                    List<TopologyWrapper<? extends Artefact>> unmodifiable = wrappers.stream()
                                                                                     .filter(w -> w.getSynchronizer()
                                                                                                   .equals(synchronizer))
                                                                                     .collect(Collectors.toUnmodifiableList());
                    try {

                        // phase create
                        List<TopologyWrapper<? extends Artefact>> results = depleter.deplete(unmodifiable, ArtefactPhase.CREATE);
                        undepleted.addAll(results);
                        registerErrors(results, ArtefactLifecycle.CREATED);

                        // phase update
                        results = depleter.deplete(unmodifiable, ArtefactPhase.UPDATE);
                        undepleted.addAll(results);
                        registerErrors(results, ArtefactLifecycle.UPDATED);

                        // phase start
                        results = depleter.deplete(unmodifiable, ArtefactPhase.START);
                        undepleted.addAll(results);
                        registerErrors(results, ArtefactLifecycle.STARTED);

                    } catch (Exception e) {
                        if (logger.isErrorEnabled()) {
                            logger.error(e.getMessage());
                        }
                        addError(e.getMessage());
                        HealthCheckStatus.getInstance()
                                         .getJobs()
                                         .setStatus(synchronizer.getClass()
                                                                .getSimpleName(),
                                                 JobStatus.Failed);
                    }
                    HealthCheckStatus.getInstance()
                                     .getJobs()
                                     .setStatus(synchronizer.getClass()
                                                            .getSimpleName(),
                                             JobStatus.Succeeded);
                }

                // Processing of cross-synchronizer artefacts once again due to eventual dependency issues
                if (undepleted.size() > 0) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Cross-processing of undepleated artefacts...");
                    }
                    try {
                        while (undepleted.size() > 0) {
                            List<TopologyWrapper<? extends Artefact>> cross = new ArrayList<>();
                            List<TopologyWrapper<? extends Artefact>> results = depleter.deplete(undepleted, ArtefactPhase.PREPARE);
                            cross.addAll(results);
                            registerErrors(results, ArtefactLifecycle.PREPARED);

                            results = depleter.deplete(undepleted, ArtefactPhase.CREATE);
                            cross.addAll(results);
                            registerErrors(results, ArtefactLifecycle.CREATED);

                            results = depleter.deplete(undepleted, ArtefactPhase.UPDATE);
                            cross.addAll(results);
                            registerErrors(results, ArtefactLifecycle.UPDATED);

                            results = depleter.deplete(undepleted, ArtefactPhase.START);
                            cross.addAll(results);
                            registerErrors(results, ArtefactLifecycle.STARTED);

                            if (cross.size() > 0 && cross.size() == undepleted.size()) {
                                // No artefacts were depleted, exiting an infinite loop
                                if (logger.isErrorEnabled()) {
                                    String crossArtefactsLeft = cross.stream()
                                                                     .map(e -> e.getArtefact()
                                                                                .getKey())
                                                                     .collect(Collectors.joining(", "));
                                    String errorMessage = "Undepleted artefacts left after cross-processing: " + crossArtefactsLeft;
                                    logger.error(errorMessage);
                                }
                                break;
                            } else if (cross.size() > 0) {
                                undepleted.clear();
                                undepleted.addAll(cross);
                                if (logger.isWarnEnabled()) {
                                    String crossArtefactsLeft = cross.stream()
                                                                     .map(e -> e.getArtefact()
                                                                                .getKey())
                                                                     .collect(Collectors.joining(", "));
                                    String warnMessage = "Retring to deplete artefacts left after cross-processing: " + crossArtefactsLeft;
                                    logger.warn(warnMessage);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Error occurred while cross-processing of undepleated artefacts", e);
                        }
                    }
                    if (logger.isWarnEnabled()) {
                        logger.warn("Cross-processing of undepleated artefacts done.");
                    }
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("Processing of artefacts done.");
                }

            }

            if (logger.isTraceEnabled()) {
                logger.trace("Cleaning up removed artefacts...");
            }

            // cleanup
            for (Synchronizer<Artefact> synchronizer : synchronizers) {
                List<? extends Artefact> registered = synchronizer.getService()
                                                                  .getAll();
                for (Artefact artefact : registered) {
                    if (synchronizer.isAccepted(artefact.getType())) {
                        if (!repository.getResource(IRepositoryStructure.PATH_REGISTRY_PUBLIC + artefact.getLocation())
                                       .exists()) {
                            synchronizer.cleanup(artefact);
                            break;
                        }
                    }
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Cleaning up removed artefacts done.");
            }

            // report results
            getErrors().forEach(e -> {
                if (logger.isErrorEnabled()) {
                    logger.error("Error occured during synchronization: " + e);
                }
            });
        } finally {
            if (logger.isDebugEnabled()) {
                int countCreated = 0;
                int countUpdated = 0;
                int countFailed = 0;
                for (Artefact artefact : artefacts.values()) {
                    if (ArtefactLifecycle.CREATED.equals(artefact.getLifecycle()))
                        countCreated++;
                    if (ArtefactLifecycle.UPDATED.equals(artefact.getLifecycle()))
                        countUpdated++;
                    if (ArtefactLifecycle.FAILED.equals(artefact.getLifecycle()))
                        countFailed++;
                }
                logger.debug("Processing synchronizers done. {} artefacts processed in total. {} ({}/{}) successful and {} failed.",
                        artefacts.size(), countCreated + countUpdated, countCreated, countUpdated, countFailed);
            }
            // clear maps
            definitions.clear();
            artefacts.clear();

            synchronizationWatcher.reset();
            initialized.set(true);
            processing.set(false);
        }
    }

    /**
     * Prepare.
     */
    private void prepare() {
        errors.clear();
        definitions.clear();
        artefacts.clear();
    }

    /**
     * Collect files.
     */
    private void collectFiles() {
        String registryFolder = getRegistryFolder();
        SynchronizationWalker synchronizationWalker = new SynchronizationWalker(this);
        try {
            synchronizationWalker.walk(registryFolder);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            addError(e.getMessage());
        }
    }

    /**
     * Mark deleted.
     */
    private void markDeleted() {
        Map<String, Definition> found = new HashMap<>();
        for (Synchronizer<? extends Artefact> synchronizer : synchronizers) {
            Map<String, Definition> map = checkSynchronizerMap(synchronizer);
            Collection<Definition> immutableDefinitions = Collections.synchronizedCollection(map.values());
            for (Definition definition : immutableDefinitions) {
                found.put(definition.getLocation(), definition);
            }
        }
        List<Definition> existings = definitionService.getAll();
        for (Definition existing : existings) {
            if (found.get(existing.getLocation()) == null && !existing.getState()
                                                                      .equals(DefinitionState.DELETED)) {
                registerDeleteState(existing);
            }
        }

    }

    /**
     * Load definitions.
     */
    private void parseDefinitions() {
        for (Synchronizer<? extends Artefact> synchronizer : synchronizers) {
            Map<String, Definition> map = checkSynchronizerMap(synchronizer);
            Collection<Definition> immutableDefinitions = Collections.synchronizedCollection(map.values());
            for (Definition definition : immutableDefinitions) {
                try {
                    if (definition.getContent() == null) {
                        String error = String.format("Content of %s has not been loaded correctly", definition.getLocation());
                        registerBrokenState(definition, error);
                        continue;
                    }

                    List parsed;
                    switch (definition.getState()) {
                        case NEW: // brand new definition
                            try {
                                parsed = synchronizer.parse(definition.getLocation(), definition.getContent());
                                parsed.forEach(a -> {
                                    ((Artefact) a).setPhase(ArtefactPhase.CREATE);
                                    synchronizer.setStatus(((Artefact) a), ArtefactLifecycle.NEW, "");
                                });
                                addArtefacts(parsed);
                                registerParsedState(definition);
                            } catch (ParseException e) {
                                registerBrokenState(definition, e.getMessage());
                            }
                            break;
                        case MODIFIED: // modified existing definition
                            try {
                                parsed = synchronizer.parse(definition.getLocation(), definition.getContent());
                                parsed.forEach(a -> {
                                    ((Artefact) a).setPhase(ArtefactPhase.UPDATE);
                                    synchronizer.setStatus(((Artefact) a), ArtefactLifecycle.MODIFIED, "");
                                });
                                addArtefacts(parsed);
                                registerParsedState(definition);
                            } catch (ParseException e) {
                                registerBrokenState(definition, e.getMessage());
                            }
                            break;
                        case PARSED: // not new nor modified
                            parsed = synchronizer.retrieve(definition.getLocation());
                            addArtefacts(parsed);
                            break;
                        case BROKEN: // has been broken
                            // do not try to parse it again as it is still not modified
                            break;
                        case DELETED: // has been deleted before, but appeared again
                            try {
                                parsed = synchronizer.parse(definition.getLocation(), definition.getContent());
                                parsed.forEach(a -> {
                                    ((Artefact) a).setPhase(ArtefactPhase.CREATE);
                                    synchronizer.setStatus(((Artefact) a), ArtefactLifecycle.NEW, "");
                                });
                                addArtefacts(parsed);
                                registerParsedState(definition);
                            } catch (ParseException e) {
                                registerBrokenState(definition, e.getMessage());
                            }
                            break;
                    }
                } catch (Exception e) {
                    registerBrokenState(definition, e.getMessage());
                }
            }
        }
    }

    /**
     * Register parsed state.
     *
     * @param definition the definition
     */
    public void registerParsedState(Definition definition) {
        if (!definition.getState()
                       .equals(DefinitionState.PARSED)) {
            definition.setState(DefinitionState.PARSED);
            definition.setMessage("");
            definitionService.save(definition);
        }
    }

    /**
     * Register broken state.
     *
     * @param definition the definition
     * @param e the e
     */
    public void registerBrokenState(Definition definition, String e) {
        if (logger.isErrorEnabled()) {
            logger.error(e);
        }
        addError(e);
        definition.setState(DefinitionState.BROKEN);
        definition.setMessage(e);
        definitionService.save(definition);
    }

    /**
     * Register delete state.
     *
     * @param definition the definition
     */
    public void registerDeleteState(Definition definition) {
        String msg = "Definition deleted: " + definition.getLocation();
        if (logger.isWarnEnabled()) {
            logger.warn(msg);
        }
        definition.setState(DefinitionState.DELETED);
        definition.setMessage(msg);
        definitionService.save(definition);
    }

    /**
     * Adds the artefacts.
     *
     * @param parsed the parsed
     */
    private void addArtefacts(List<Artefact> parsed) {
        for (Artefact artefact : parsed) {
            artefacts.put(artefact.getKey(), artefact);
        }
    }

    /**
     * Gets the registry folder.
     *
     * @return the registry folder
     */
    public String getRegistryFolder() {
        String internalRegistryPath = ((LocalRepository) repository).getInternalResourcePath(IRepositoryStructure.PATH_REGISTRY_PUBLIC);
        File registry = Path.of(internalRegistryPath)
                            .toFile();
        if (!registry.exists()) {
            registry.mkdirs();
        }
        return internalRegistryPath;
    }

    /**
     * Visit file.
     *
     * @param file the file
     * @param attrs the attrs
     * @param location the location
     */
    @Override
    public void visitFile(Path file, BasicFileAttributes attrs, String location) {
        checkFile(file, attrs, location);
    }

    /**
     * Check file.
     *
     * @param file the file
     * @param attrs the attrs
     * @param location the location
     */
    private void checkFile(Path file, BasicFileAttributes attrs, String location) {
        for (Synchronizer<Artefact> synchronizer : synchronizers) {
            if (synchronizer.isAccepted(file, attrs)) {
                // synchronizer knows this artefact, hence check whether to process it or not
                try {
                    checkAndCollect(file, location, synchronizer);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                break;
            }
        }
    }

    /**
     * Collect for processing, if new or modified.
     *
     * @param file the file
     * @param location the location
     * @param synchronizer the synchronizer
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotFoundException the file not found exception
     */
    private void checkAndCollect(Path file, String location, Synchronizer<Artefact> synchronizer)
            throws IOException, FileNotFoundException {

        String type = synchronizer.getArtefactType();

        // load the content to calculate the checksum
        byte[] content = Files.readAllBytes(file);
        if (content == null) {
            logger.error("Reading file {} returns null content", file.toString());
            return;
        }
        Definition definition = new Definition(location, FilenameUtils.getBaseName(file.getFileName()
                                                                                       .toString()),
                type, content);
        // check whether this artefact has been processed in the past already
        Definition maybe = definitionService.findByKey(definition.getKey());
        Map<String, Definition> map = checkSynchronizerMap(synchronizer);
        if (maybe != null) {
            // artefact has been processed in the past
            if (!maybe.getChecksum()
                      .equals(definition.getChecksum())) {
                // the content has been modified since the last processing
                maybe.setChecksum(definition.getChecksum());
                maybe.setState(DefinitionState.MODIFIED);
                maybe.setContent(definition.getContent());
                // update the artefact with the new checksum and status
                definitionService.save(maybe);
                // added to artefacts for processing
                map.put(maybe.getKey(), maybe);
            } else {
                // not modified content, but still known definition
                switch (maybe.getState()) {
                    case NEW: // has been started to be processed as new, but not completed
                    case MODIFIED: // has been started to be processed as modified, but not completed
                    case PARSED: // has been successfully parsed in the past
                        if (map.get(maybe.getKey()) == null) {
                            maybe.setContent(definition.getContent());
                            map.put(maybe.getKey(), maybe);
                        }
                        break;
                    case BROKEN: // has been started in the past, but failed to parse the file
                        logger.warn("Definition with key: {} has been failed with reason: {}", maybe.getKey(), maybe.getMessage());
                        if (map.get(maybe.getKey()) == null) {
                            maybe.setContent(definition.getContent());
                            map.put(maybe.getKey(), maybe);
                        }
                        break;
                    case DELETED: // has been deleted in the past
                        if (map.get(maybe.getKey()) == null) {
                            maybe.setContent(definition.getContent());
                            maybe.setState(DefinitionState.NEW);
                            map.put(maybe.getKey(), maybe);
                        }
                        break;
                }
            }
        } else {
            // artefact is new, hence stored for processing
            definition.setState(DefinitionState.NEW);
            definitionService.save(definition);
            map.put(definition.getKey(), definition);
        }
    }

    /**
     * Check synchronizer map.
     *
     * @param synchronizer the synchronizer
     * @return the map
     */
    public Map<String, Definition> checkSynchronizerMap(Synchronizer synchronizer) {
        Map<String, Definition> map = definitions.get(synchronizer);
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap<>());;
            definitions.put(synchronizer, map);
        }
        return map;
    }

    /**
     * Adds the error.
     *
     * @param error the error
     */
    @Override
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * Gets the errors.
     *
     * @return the errors
     */
    @Override
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Register errors.
     *
     * @param remained the remained
     * @param lifecycle the lifecycle
     */
    @Override
    public void registerErrors(List<TopologyWrapper<? extends Artefact>> remained, ArtefactLifecycle lifecycle) {
        if (remained.size() > 0) {
            for (TopologyWrapper<? extends Artefact> wrapper : remained) {
                String errorMessage = String.format("Undepleted Artefact of type: [%s] with key: [%s] in phase: [%s]", wrapper.getArtefact()
                                                                                                                              .getType(),
                        wrapper.getId(), lifecycle);
                if (logger.isErrorEnabled()) {
                    logger.error(errorMessage);
                }
                errors.add(errorMessage);
                if (wrapper.getArtefact()
                           .getError() != null
                        && !wrapper.getArtefact()
                                   .getError()
                                   .equals("")) {
                    errorMessage += " | " + wrapper.getArtefact()
                                                   .getError();
                }
                wrapper.getSynchronizer()
                       .setStatus(wrapper.getArtefact(), ArtefactLifecycle.FAILED, errorMessage);
                Problem problem = ProblemsFacade.getArtefactSynchronizationProblem(wrapper.getArtefact());
                if (problem != null) {
                    ProblemsFacade.updateArtefactSynchronizationProblem(wrapper.getArtefact(), errorMessage);
                } else {
                    ProblemsFacade.saveArtefactSynchronizationProblem(wrapper.getArtefact(), errorMessage);
                }
            }
        }
    }

    /**
     * Register fatal errors.
     *
     * @param remained the remained
     * @param lifecycle the lifecycle
     */
    @Override
    public void registerFatals(List<TopologyWrapper<? extends Artefact>> remained, ArtefactLifecycle lifecycle) {
        if (remained.size() > 0) {
            for (TopologyWrapper<? extends Artefact> wrapper : remained) {
                String errorMessage = String.format(
                        "Fatal undepleted Artefact of type: [%s] with key: [%s] in phase: [%s]", wrapper.getArtefact()
                                                                                                        .getType(),
                        wrapper.getId(), lifecycle);
                if (logger.isErrorEnabled()) {
                    logger.error(errorMessage);
                }
                errors.add(errorMessage);
                wrapper.getSynchronizer()
                       .setStatus(wrapper.getArtefact(), lifecycle, errorMessage);
            }
        }
    }

    /**
     * Register state.
     *
     * @param synchronizer the synchronizer
     * @param wrapper the wrapper
     * @param lifecycle the lifecycle
     * @param message the message
     */
    @Override
    public void registerState(Synchronizer<? extends Artefact> synchronizer, TopologyWrapper<? extends Artefact> wrapper,
            ArtefactLifecycle lifecycle, String message) {
        registerState(synchronizer, wrapper.getArtefact(), lifecycle, message);
    }

    /**
     * Register state.
     *
     * @param synchronizer the synchronizer
     * @param artefact the artefact
     * @param lifecycle the lifecycle
     * @param message the message
     */
    @Override
    public void registerState(Synchronizer<? extends Artefact> synchronizer, Artefact artefact, ArtefactLifecycle lifecycle,
            String message) {
        // if (logger.isTraceEnabled()) {logger.trace("Processed artefact with key: {} with status: {}",
        // artefact.getKey(), lifecycle.getValue());}
        synchronizer.setStatus(artefact, lifecycle, message);
    }

}
