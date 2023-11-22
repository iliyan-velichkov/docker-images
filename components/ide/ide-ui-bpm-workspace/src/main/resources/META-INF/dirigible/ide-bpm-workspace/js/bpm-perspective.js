/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2023 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
let IDEBPMWorkspacePerspective = angular.module("IDEBPMWorkspace", ["ngResource", "ideLayout", "ideUI"]);

IDEBPMWorkspacePerspective.config(["messageHubProvider", function (messageHubProvider) {
    messageHubProvider.eventIdPrefix = 'bpm';
}]);

IDEBPMWorkspacePerspective.controller("BpmWorkspaceViewController", ["$scope", function ($scope) {
    $scope.layoutModel = {
        views: ["bpm-process-definitions-view", "bpm-process-instances-view", "bpm-image-viewer", "bpm-user-tasks"],
        viewSettings: {
            "bpm-process-definitions-view": { expanded: true },
            "bpm-process-instances-view": { expanded: true },
            "bpm-image-viewer": { closable: false },
        },
        layoutSettings: {
            hideEditorsPane: false,
            hideCenterTabs: true,
        },
    };
    $scope.layoutConfig = {
        leftPaneMinSize: 360,
    };
}]);