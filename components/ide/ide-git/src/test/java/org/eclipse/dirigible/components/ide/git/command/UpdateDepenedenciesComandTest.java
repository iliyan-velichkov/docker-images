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
package org.eclipse.dirigible.components.ide.git.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.dirigible.components.ide.git.domain.GitConnectorException;
import org.eclipse.dirigible.components.ide.git.domain.IGitConnector;
import org.eclipse.dirigible.components.ide.git.model.GitCloneModel;
import org.eclipse.dirigible.components.ide.git.model.GitUpdateDependenciesModel;
import org.eclipse.dirigible.components.ide.workspace.domain.Project;
import org.eclipse.dirigible.components.ide.workspace.domain.Workspace;
import org.eclipse.dirigible.components.ide.workspace.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The Class UpdateDepenedenciesComandTest.
 */
@WithMockUser
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = {"org.eclipse.dirigible.components"})
@EntityScan("org.eclipse.dirigible.components")
public class UpdateDepenedenciesComandTest {

    /** The clone command. */
    private CloneCommand cloneCommand;

    /** The update dependencies command. */
    private UpdateDependenciesCommand updateDependenciesCommand;

    /** The workspaces service. */
    private WorkspaceService workspaceService;

    /**
     * Creates the workspace test.
     *
     * @throws GitConnectorException the git connector exception
     */
    @Test
    public void createWorkspaceTest() throws GitConnectorException {
        String gitEnabled = System.getenv(GitConnectorTest.DIRIGIBLE_TEST_GIT_ENABLED);
        if (gitEnabled != null) {
            Workspace workspace1 = workspaceService.getWorkspace("workspace1");
            GitCloneModel cloneModel = new GitCloneModel();
            cloneModel.setRepository("https://github.com/dirigiblelabs/sample_git_test.git");
            cloneModel.setBranch(IGitConnector.GIT_MASTER);
            cloneModel.setPublish(true);
            cloneCommand.execute(workspace1, cloneModel);
            assertNotNull(workspace1);
            assertTrue(workspace1.exists());
            Project project1 = workspace1.getProject("project1");
            assertNotNull(project1);
            assertTrue(project1.exists());
            String username = System.getProperty("dirigibleTestGitUsername");
            String password = System.getProperty("dirigibleTestGitPassword");
            if (username != null && password != null) {
                GitUpdateDependenciesModel model = new GitUpdateDependenciesModel();
                model.setUsername(username);
                model.setPassword(password);
                model.setPublish(true);
                updateDependenciesCommand.execute(workspace1, new Project[] {project1}, model);
            }
        }
    }

    /**
     * The Class TestConfiguration.
     */
    @SpringBootApplication
    static class TestConfiguration {
    }

}
