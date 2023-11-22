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
package org.eclipse.dirigible.components.ide.workspace.project;

import java.util.List;

import org.eclipse.dirigible.components.project.ProjectAction;
import org.eclipse.dirigible.components.base.publisher.PublisherHandler;
import org.eclipse.dirigible.components.ide.workspace.domain.Project;
import org.eclipse.dirigible.components.ide.workspace.service.ActionsService;
import org.eclipse.dirigible.components.ide.workspace.service.WorkspaceService;
import org.eclipse.dirigible.repository.api.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class ProjectActionsPublisherHandler.
 */
@Component
public class ProjectActionsPublisherHandler implements PublisherHandler {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(ProjectActionsPublisherHandler.class);

    /** The actions service. */
    @Autowired
    private ActionsService actionsService;

    /** The workspace service. */
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Before publish.
     *
     * @param location the location
     */
    @Override
    public void beforePublish(String location) {
        try {
            RepositoryPath path = new RepositoryPath(location);
            String[] segments = path.getSegments();
            if (segments.length == 4) {
                String workspace = path.getSegments()[2];
                String project = path.getSegments()[3];
                beforePublishProject(workspace, project);
            } else if (segments.length == 3) {
                String workspace = path.getSegments()[2];
                List<Project> projects = workspaceService.getWorkspace(workspace)
                                                         .getProjects();
                for (Project project : projects) {
                    beforePublishProject(workspace, project.getName());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Before publish project.
     *
     * @param workspace the workspace
     * @param project the project
     */
    public void beforePublishProject(String workspace, String project) {
        try {
            List<ProjectAction> actions = actionsService.listRegisteredActions(workspace, project);
            for (ProjectAction action : actions) {
                if (action.isPublish()) {
                    try {
                        actionsService.executeAction(workspace, project, action.getName());
                    } catch (Exception e) {
                        logger.error("Failed in executing the action: {} of project: {} under workspace: {} with: {}", action, project,
                                workspace, e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }



}
