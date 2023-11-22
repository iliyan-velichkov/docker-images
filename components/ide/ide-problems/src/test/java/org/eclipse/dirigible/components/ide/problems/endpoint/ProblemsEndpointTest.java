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
package org.eclipse.dirigible.components.ide.problems.endpoint;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.persistence.EntityManager;

import org.eclipse.dirigible.components.ide.problems.domain.Problem;
import org.eclipse.dirigible.components.ide.problems.repository.ProblemRepository;
import org.eclipse.dirigible.components.ide.problems.service.ProblemService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@EntityScan("org.eclipse.dirigible.components")
@Transactional
public class ProblemsEndpointTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @BeforeEach
    public void setup() throws Exception {

        cleanup();

        problemService.save(createProblem("location1", "type1", "line1", "column1", "cause1", "expected1", "category1", "module1",
                "source1", "program1"));
        problemService.save(createProblem("location2", "type2", "line2", "column2", "cause2", "expected2", "category2", "module2",
                "source2", "program2"));

    }

    @AfterEach
    public void cleanup() throws Exception {

    }

    @Test
    public void findAllProblems() {
        Integer size = 10;
        Integer page = 0;
        Pageable pageable = PageRequest.of(page, size);
        assertNotNull(problemService.getPages(pageable));
    }

    @Test
    public void getProblems() throws Exception {
        mockMvc.perform(get("/services/ide/problems/"))
               .andDo(print())
               .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getProblemsByCondition() throws Exception {
        mockMvc.perform(get("/services/ide/problems/search?condition=co&limit=5"))
               .andDo(print())
               .andExpect(status().is2xxSuccessful());
    }

    public static Problem createProblem(String location, String type, String line, String column, String cause, String expected,
            String category, String module, String source, String program) {
        Problem problem = new Problem(location, type, line, column, cause, expected, category, module, source, program);
        return problem;
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
