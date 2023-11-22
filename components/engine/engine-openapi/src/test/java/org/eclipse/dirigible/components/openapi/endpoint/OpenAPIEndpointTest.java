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
package org.eclipse.dirigible.components.openapi.endpoint;

import static org.eclipse.dirigible.components.openapi.repository.OpenAPIRepositoryTest.createOpenAPI;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eclipse.dirigible.components.openapi.repository.OpenAPIRepository;
import org.eclipse.dirigible.components.openapi.synchronizer.OpenAPISynchronizer;
import org.eclipse.dirigible.components.repository.RepositoryConfig;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.api.IRepositoryStructure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {OpenAPIRepository.class, RepositoryConfig.class})
@AutoConfigureMockMvc
@ComponentScan(basePackages = {"org.eclipse.dirigible.components"})
@EntityScan("org.eclipse.dirigible.components")
@Transactional
class OpenAPIEndpointTest {

    @Autowired
    private OpenAPIRepository openAPIRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private IRepository repository;

    @BeforeEach
    public void setup() {

        cleanup();

        try {
            // Create test OpenAPI
            openAPIRepository.save(createOpenAPI("/a/b/c/test1.openapi", "test1", "description"));
            openAPIRepository.save(createOpenAPI("/a/b/c/test2.openapi", "test2", "description"));
            openAPIRepository.save(createOpenAPI("/a/b/c/test3.openapi", "test3", "description"));
            openAPIRepository.save(createOpenAPI("/a/b/c/test4.openapi", "test4", "description"));
            openAPIRepository.save(createOpenAPI("/a/b/c/test5.openapi", "test5", "description"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void cleanup() {
        openAPIRepository.deleteAll();
    }

    @Test
    public void testGetVersion() throws Exception {
        String openAPILocation = "/META-INF/dirigible/test/test.openapi";
        byte[] content = OpenAPISynchronizer.class.getResourceAsStream(openAPILocation)
                                                  .readAllBytes();
        openAPIRepository.save(createOpenAPI(openAPILocation, "test", "description"));
        repository.createResource(IRepositoryStructure.PATH_REGISTRY_PUBLIC + openAPILocation, content);
        mockMvc.perform(get("/services/openapi"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().string(containsString(
                       "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Eclipse Dirigible - Applications REST Services API\",\"description\":\"Eclipse Dirigible API of the REST services provided by the applications\",\"contact\":{\"name\":\"Eclipse Dirigible\",\"url\":\"https://www.dirigible.io\",\"email\":\"dirigible-dev@eclipse.org\"},\"license\":{\"name\":\"Eclipse Public License - v 2.0\",\"url\":\"https://www.eclipse.org/legal/epl-v20.html\"},\"version\":\"0.0.1\"},\"servers\":[{\"url\":\"/services/js\"}],\"security\":[],\"tags\":[],\"paths\":{\"/test/openapi/api.mjs/hello-world\":{\"get\":{\"description\":\"Returns Hello World message\",\"responses\":{\"200\":{\"description\":\"Hello World response\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/HelloWorldModel\"}}}}}}}},\"components\":{\"schemas\":{\"HelloWorldModel\":{\"type\":\"object\",\"properties\":{\"status\":{\"type\":\"string\"}}}},\"responses\":{},\"parameters\":{},\"examples\":{},\"requestBodies\":{},\"headers\":{},\"securitySchemes\":{},\"links\":{},\"callbacks\":{}}}")));
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
