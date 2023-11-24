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
package org.eclipse.dirigible.components.odata.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.dirigible.components.base.artefact.ArtefactService;
import org.eclipse.dirigible.components.odata.domain.ODataHandler;
import org.eclipse.dirigible.components.odata.repository.ODataHandlerRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class ODataHandlerService.
 */
@Service
@Transactional
public class ODataHandlerService implements ArtefactService<ODataHandler>, InitializingBean {

    /** The instance. */
    private static ODataHandlerService INSTANCE;

    /**
     * After properties set.
     *
     * @throws Exception the exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        INSTANCE = this;
    }

    /**
     * Gets the.
     *
     * @return the o data handler service
     */
    public static ODataHandlerService get() {
        return INSTANCE;
    }

    /** The ODataHandler repository. */
    @Autowired
    private ODataHandlerRepository odataHandlerRepository;

    /**
     * Gets the all.
     *
     * @return the all
     */
    @Override
    public List<ODataHandler> getAll() {
        return odataHandlerRepository.findAll();
    }

    /**
     * Find all.
     *
     * @param pageable the pageable
     * @return the page
     */
    @Override
    public Page<ODataHandler> getPages(Pageable pageable) {
        return odataHandlerRepository.findAll(pageable);
    }

    /**
     * Find by id.
     *
     * @param id the id
     * @return the ODataHandler
     */
    @Override
    public ODataHandler findById(Long id) {
        Optional<ODataHandler> odataHandler = odataHandlerRepository.findById(id);
        if (odataHandler.isPresent()) {
            return odataHandler.get();
        } else {
            throw new IllegalArgumentException("OData Handler with id does not exist: " + id);
        }
    }

    /**
     * Find by name.
     *
     * @param name the name
     * @return the ODataHandler
     */
    @Override
    public ODataHandler findByName(String name) {
        ODataHandler filter = new ODataHandler();
        filter.setName(name);
        Example<ODataHandler> example = Example.of(filter);
        Optional<ODataHandler> odataHandler = odataHandlerRepository.findOne(example);
        if (odataHandler.isPresent()) {
            return odataHandler.get();
        } else {
            throw new IllegalArgumentException("OData Handler with name does not exist: " + name);
        }
    }

    /**
     * Find by location.
     *
     * @param location the location
     * @return the list
     */
    @Override
    @Transactional(readOnly = true)
    public List<ODataHandler> findByLocation(String location) {
        ODataHandler filter = new ODataHandler();
        filter.setLocation(location);
        Example<ODataHandler> example = Example.of(filter);
        List<ODataHandler> list = odataHandlerRepository.findAll(example);
        return list;
    }

    /**
     * Find by key.
     *
     * @param key the key
     * @return the ODataHandler
     */
    @Override
    @Transactional(readOnly = true)
    public ODataHandler findByKey(String key) {
        ODataHandler filter = new ODataHandler();
        filter.setKey(key);
        Example<ODataHandler> example = Example.of(filter);
        Optional<ODataHandler> odataHandler = odataHandlerRepository.findOne(example);
        if (odataHandler.isPresent()) {
            return odataHandler.get();
        }
        return null;
    }

    /**
     * Save.
     *
     * @param odataHandler the ODataHandler
     * @return the ODataHandler
     */
    @Override
    public ODataHandler save(ODataHandler odataHandler) {
        return odataHandlerRepository.saveAndFlush(odataHandler);
    }

    /**
     * Delete.
     *
     * @param odataHandler the ODataHandler
     */
    @Override
    public void delete(ODataHandler odataHandler) {
        odataHandlerRepository.delete(odataHandler);
    }

    /**
     * Removes the handler.
     *
     * @param location the location
     */
    public void removeHandlers(String location) {
        ODataHandler filter = new ODataHandler();
        filter.setLocation(location);
        Example<ODataHandler> example = Example.of(filter);
        odataHandlerRepository.deleteAll(odataHandlerRepository.findAll(example));
    }

    /**
     * Gets the by namespace name method and kind.
     *
     * @param namespace the namespace
     * @param name the name
     * @param method the method
     * @param kind the kind
     * @return the by namespace name method and kind
     */
    public List<ODataHandler> getByNamespaceNameMethodAndKind(String namespace, String name, String method, String kind) {
        ODataHandler filter = new ODataHandler();
        filter.setNamespace(namespace);
        filter.setName(name);
        filter.setMethod(method);
        filter.setKind(kind);
        Example<ODataHandler> example = Example.of(filter);
        return odataHandlerRepository.findAll(example);
    }

}
