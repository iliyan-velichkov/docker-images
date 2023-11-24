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
package org.eclipse.dirigible.components.extensions.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.eclipse.dirigible.components.extensions.domain.Extension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = {"org.eclipse.dirigible.components"})
@EntityScan("org.eclipse.dirigible.components")
@Transactional
public class ExtensionRepositoryTest {

    @Autowired
    private ExtensionRepository extensionRepository;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    public void setup() throws Exception {

        cleanup();

        // create test Extensions
        extensionRepository.save(createExtension("/a/b/c/e1.extension", "e1", "description", "epoint1", "e1"));
        extensionRepository.save(createExtension("/a/b/c/e2.extension", "e2", "description", "epoint1", "e2"));
        extensionRepository.save(createExtension("/a/b/c/e3.extension", "e3", "description", "epoint2", "e3"));
        extensionRepository.save(createExtension("/a/b/c/e4.extension", "e4", "description", "epoint2", "e4"));
        extensionRepository.save(createExtension("/a/b/c/e5.extension", "e5", "description", "epoint2", "e5"));
    }

    @AfterEach
    public void cleanup() throws Exception {
        extensionRepository.deleteAll();
    }


    @Test
    public void getOne() {
        Long id = extensionRepository.findAll()
                                     .get(0)
                                     .getId();
        Optional<Extension> optional = extensionRepository.findById(id);
        Extension extension = optional.isPresent() ? optional.get() : null;
        assertNotNull(extension);
        assertNotNull(extension.getLocation());
        assertNotNull(extension.getCreatedBy());
        assertEquals("SYSTEM", extension.getCreatedBy());
        assertNotNull(extension.getCreatedAt());
        // assertEquals("extension:/a/b/c/e1.extension:e1",extension.getKey());
    }

    @Test
    public void getReferenceUsingEntityManager() {
        Long id = extensionRepository.findAll()
                                     .get(0)
                                     .getId();
        Extension extension = entityManager.getReference(Extension.class, id);
        assertNotNull(extension);
        assertNotNull(extension.getLocation());
    }

    public static Extension createExtension(String location, String name, String description, String extensionPoint, String module) {
        Extension extension = new Extension(location, name, description, extensionPoint, module);
        return extension;
    }

    @SpringBootApplication
    static class TestConfiguration {
    }

}
