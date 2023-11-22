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
package org.eclipse.dirigible.components.websockets.endpoint;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.eclipse.dirigible.components.websockets.domain.Websocket;
import org.eclipse.dirigible.components.websockets.repository.WebsocketRepository;
import org.eclipse.dirigible.components.websockets.service.WebsocketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = {"org.eclipse.dirigible.components"})
@Transactional
public class WebsocketEndpointTest {
    @Autowired
    private WebsocketService websocketService;

    @Autowired
    private WebsocketRepository websocketRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @BeforeEach
    public void setup() {

        cleanup();

        websocketService.save(new Websocket("/a/b/c/w1.websocket", "name1", "description", "endpoint1", "handler1", "engine1"));
        websocketService.save(new Websocket("/a/b/c/w2.websocket", "name2", "description", "endpoint2", "handler2", "engine2"));
        websocketService.save(new Websocket("/a/b/c/w3.websocket", "name3", "description", "endpoint3", "handler3", "engine3"));
    }

    @AfterEach
    public void cleanup() {
        websocketRepository.deleteAll();
    }

    @Test
    public void findAllExtensionPoints() throws Exception {
        mockMvc.perform(get("/services/websockets"))
               .andDo(print())
               .andExpect(status().is2xxSuccessful());
        // .andExpect(jsonPath("$.content[0].location").value("/a/b/c/w1.websocket"));
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
