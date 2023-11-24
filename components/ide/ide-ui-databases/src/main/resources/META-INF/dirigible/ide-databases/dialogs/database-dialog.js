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
const dbdialog = angular.module('dbdialog', ['ideUI', 'ideView']);

dbdialog.controller('DBDialogController', ['$scope', 'messageHub', 'ViewParameters', function ($scope, messageHub, ViewParameters) {
    $scope.state = {
        isBusy: true,
        error: false,
        busyText: "Loading...",
    };

    $scope.forms = {
        dbForm: {},
    };

    $scope.inputRules = {
        patterns: ['^(?! ).*(?<! )$']
    };

    $scope.editMode = false;

    $scope.urls = {
        "org.h2.Driver": "jdbc:h2:path/name",
        "org.postgresql.Driver": "jdbc:postgresql://host:port/database",
        "com.mysql.jdbc.Driver": "jdbc:mysql://host:port/database",
        "com.sap.db.jdbc.Driver": "jdbc:sap://host:port/?encrypt=true&validateCertificate=false",
        "net.snowflake.client.jdbc.SnowflakeDriver": "jdbc:snowflake://account_identifier.snowflakecomputing.com/?db=SNOWFLAKE_SAMPLE_DATA&schema=TPCH_SF1000",
        "org.eclipse.dirigible.mongodb.jdbc.Driver": "jdbc:mongodb://host:port/database",
    };

    $scope.drivers = [
        { text: "H2 - org.h2.Driver", value: "org.h2.Driver" },
        { text: "PostgreSQL - org.postgresql.Driver", value: "org.postgresql.Driver" },
        { text: "MySQL - com.mysql.jdbc.Driver", value: "com.mysql.jdbc.Driver" },
        { text: "SAP HANA - com.sap.db.jdbc.Driver", value: "com.sap.db.jdbc.Driver" },
        { text: "Snowflake - net.snowflake.client.jdbc.SnowflakeDriver", value: "net.snowflake.client.jdbc.SnowflakeDriver" },
        { text: "MongoDB - org.eclipse.dirigible.mongodb.jdbc.Driver", value: "org.eclipse.dirigible.mongodb.jdbc.Driver" }
    ];

    $scope.database = {
        name: '',
        driver: '',
        url: '',
        username: '',
        password: '',
        parameters: '',
    };

    $scope.driverChanged = function () {
        $scope.database.url = $scope.urls[$scope.database.driver];
        $scope.database.username = "";
        $scope.database.password = "";
    };

    function getTopic() {
        if ($scope.editMode) return 'ide-databases.database.edit';
        return 'ide-databases.database.create';
    }

    $scope.save = function () {
        $scope.state.busyText = "Sending data..."
        $scope.state.isBusy = true;
        messageHub.postMessage(getTopic(), $scope.database, true);
    };

    $scope.cancel = function () {
        messageHub.closeDialogWindow('database-create-edit');
    };

    $scope.dataParameters = ViewParameters.get();
    if (!$scope.dataParameters.hasOwnProperty('editMode')) {
        $scope.state.error = true;
        $scope.errorMessage = "The 'editMode' parameter is missing.";
    } else {
        $scope.editMode = $scope.dataParameters.editMode;
        if ($scope.editMode) {
            if (!$scope.dataParameters.hasOwnProperty('database')) {
                $scope.state.error = true;
                $scope.errorMessage = "The 'database' parameter is missing.";
            } else {
                $scope.database = $scope.dataParameters.database;
            }
        }
        $scope.state.isBusy = false;
    }
}]);