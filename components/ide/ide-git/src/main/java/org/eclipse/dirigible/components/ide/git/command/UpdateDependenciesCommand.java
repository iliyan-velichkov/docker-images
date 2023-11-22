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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dirigible.components.api.security.UserFacade;
import org.eclipse.dirigible.components.ide.git.domain.GitConnectorException;
import org.eclipse.dirigible.components.ide.git.model.GitUpdateDependenciesModel;
import org.eclipse.dirigible.components.ide.workspace.domain.Project;
import org.eclipse.dirigible.components.ide.workspace.domain.Workspace;
import org.eclipse.dirigible.components.ide.workspace.project.ProjectMetadataManager;
import org.eclipse.dirigible.components.ide.workspace.service.PublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Update project's dependencies.
 */
@Component
public class UpdateDependenciesCommand extends CloneCommand {

    /**
     * Instantiates a new update dependencies command.
     *
     * @param publisherService the publisher service
     * @param projectMetadataManager the project metadata manager
     */
    @Autowired
    public UpdateDependenciesCommand(PublisherService publisherService, ProjectMetadataManager projectMetadataManager) {
        super(publisherService, projectMetadataManager);
    }

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(UpdateDependenciesCommand.class);

    /**
     * Execute Update Dependencies Command.
     *
     * @param workspace the workspace
     * @param projects the projects
     * @param model the git update dependencies model
     * @throws GitConnectorException in case of exception
     */
    public void execute(final Workspace workspace, final Project[] projects, GitUpdateDependenciesModel model)
            throws GitConnectorException {
        for (Project selectedProject : projects) {
            String user = UserFacade.getName();
            try {
                Set<String> clonedProjects = new HashSet<String>();
                cloneDependencies(user, model.getUsername(), model.getPassword(), workspace, clonedProjects, selectedProject.getName());
                if (model.isPublish()) {
                    publishProjects(workspace, clonedProjects);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("Project's [%s] dependencies has been updated successfully.", selectedProject.getName()));
                }
            } catch (IOException | GitConnectorException e) {
                String errorMessage =
                        String.format("Error occured while updating dependencies of the project [%s]", selectedProject.getName());
                if (logger.isErrorEnabled()) {
                    logger.error(errorMessage, e);
                }
                throw new GitConnectorException(errorMessage, e);
            }
        }
    }

}
