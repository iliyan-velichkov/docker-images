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
angular.module('page', ["ideUI", "ideView"])
	.controller('PageController', function ($scope, messageHub, $window, ViewParameters) {
		let contents;
		let csrfToken;
		$scope.errorMessage = 'Аn unknown error was encountered. Please see console for more information.';
		$scope.forms = {
			editor: {},
		};
		$scope.state = {
			isBusy: true,
			error: false,
			busyText: "Loading...",
		};
		$scope.types = [
			{ value: "string", label: "string" },
			{ value: "number", label: "number" },
			{ value: "boolean", label: "boolean" },
			{ value: "choice", label: "choice" },
		];
		$scope.editParameterIndex = 0;

		angular.element($window).bind("focus", function () {
			messageHub.setFocusedEditor($scope.dataParameters.file);
			messageHub.setStatusCaret('');
		});

		function getResource(resourcePath) {
			let xhr = new XMLHttpRequest();
			xhr.open('GET', resourcePath, false);
			xhr.setRequestHeader('X-CSRF-Token', 'Fetch');
			xhr.send();
			if (xhr.status === 200) {
				csrfToken = xhr.getResponseHeader("x-csrf-token");
				return xhr.responseText;
			} else {
				$scope.state.error = true;
				$scope.errorMessage = "Unable to load the file. See console, for more information.";
				messageHub.setStatusError(`Error loading '${$scope.dataParameters.file}'`);
				return '{}';
			}
		}

		function load() {
			if (!$scope.state.error) {
				contents = getResource('/services/ide/workspaces' + $scope.dataParameters.file);
				$scope.job = JSON.parse(contents);
				if (!$scope.job.parameters)
					$scope.job.parameters = [];
				contents = JSON.stringify($scope.job, null, 4);
				$scope.state.isBusy = false;
			}
		}

		function saveContents(text) {
			let xhr = new XMLHttpRequest();
			xhr.open('PUT', '/services/ide/workspaces' + $scope.dataParameters.file);
			xhr.setRequestHeader('X-Requested-With', 'Fetch');
			xhr.setRequestHeader('X-CSRF-Token', csrfToken);
			xhr.onreadystatechange = function () {
				if (xhr.readyState === 4) {
					messageHub.announceFileSaved({
						name: $scope.dataParameters.file.substring($scope.dataParameters.file.lastIndexOf('/') + 1),
						path: $scope.dataParameters.file.substring($scope.dataParameters.file.indexOf('/', 1)),
						contentType: $scope.dataParameters.contentType,
						workspace: $scope.dataParameters.file.substring(1, $scope.dataParameters.file.indexOf('/', 1)),
					});
					messageHub.setStatusMessage(`File '${$scope.dataParameters.file}' saved`);
					messageHub.setEditorDirty($scope.dataParameters.file, false);
					$scope.$apply(function () {
						$scope.state.isBusy = false;
					});
				}
			};
			xhr.onerror = function (error) {
				console.error(`Error saving '${$scope.dataParameters.file}'`, error);
				messageHub.setStatusError(`Error saving '${$scope.dataParameters.file}'`);
				messageHub.showAlertError('Error while saving the file', 'Please look at the console for more information');
				$scope.$apply(function () {
					$scope.state.isBusy = false;
				});
			};
			xhr.send(text);
		}

		$scope.save = function () {
			if ($scope.forms.editor.$valid && !$scope.state.error) {
				$scope.state.busyText = "Saving...";
				$scope.state.isBusy = true;
				contents = JSON.stringify($scope.job, null, 4);
				saveContents(contents);
			}
		};

		messageHub.onEditorFocusGain(function (msg) {
			if (msg.resourcePath === $scope.dataParameters.file) messageHub.setStatusCaret('');
		});

		messageHub.onEditorReloadParameters(
			function (event) {
				$scope.$apply(() => {
					if (event.resourcePath === $scope.dataParameters.file) {
						$scope.dataParameters = ViewParameters.get();
					}
				});
			}
		);

		messageHub.onDidReceiveMessage(
			"editor.file.save.all",
			function () {
				if (!$scope.state.error && $scope.forms.editor.$valid) {
					let job = JSON.stringify($scope.job, null, 4);
					if (contents !== job) {
						$scope.save();
					}
				}
			},
			true,
		);

		messageHub.onDidReceiveMessage(
			"editor.file.save",
			function (msg) {
				if (!$scope.state.error && $scope.forms.editor.$valid) {
					let file = msg.data && typeof msg.data === 'object' && msg.data.file;
					if (file && file === $scope.dataParameters.file) {
						let job = JSON.stringify($scope.job, null, 4);
						if (contents !== job) $scope.save();
					}
				}
			},
			true,
		);

		messageHub.onDidReceiveMessage(
			"jobsEditor.parameter.add",
			function (msg) {
				if (msg.data.buttonId === "b1") {
					let parameter = {
						name: msg.data.formData[0].value,
						type: msg.data.formData[1].value,
						defaultValue: msg.data.formData[2].value,
						choices: msg.data.formData[3].value,
						description: msg.data.formData[4].value,
					};
					if (parameter.type !== 'choice') delete parameter['choices'];
					$scope.$apply(function () {
						$scope.job.parameters.push(parameter);
					});
				}
				messageHub.hideFormDialog("jobsEditorAddParameter");
			},
			true
		);

		messageHub.onDidReceiveMessage(
			"jobsEditor.parameter.edit",
			function (msg) {
				if (msg.data.buttonId === "b1") {
					$scope.$apply(function () {
						$scope.job.parameters[$scope.editParameterIndex].name = msg.data.formData[0].value;
						$scope.job.parameters[$scope.editParameterIndex].type = msg.data.formData[1].value;
						$scope.job.parameters[$scope.editParameterIndex].defaultValue = msg.data.formData[2].value;
						$scope.job.parameters[$scope.editParameterIndex].description = msg.data.formData[4].value;
						if (msg.data.formData[1].value === 'choice') $scope.job.parameters[$scope.editParameterIndex]['choices'] = msg.data.formData[3].value;
						else delete $scope.job.parameters[$scope.editParameterIndex]['choices'];
					});
				}
				messageHub.hideFormDialog("jobsEditorEditParameter");
			},
			true
		);

		$scope.$watch('job', function () {
			if (!$scope.state.error) {
				let job = JSON.stringify($scope.job, null, 4);
				messageHub.setEditorDirty($scope.dataParameters.file, contents !== job);
			}
		}, true);

		$scope.addParameter = function () {
			let excludedNames = [];
			for (let i = 0; i < $scope.job.parameters.length; i++) {
				excludedNames.push($scope.job.parameters[i].name);
			}
			messageHub.showFormDialog(
				"jobsEditorAddParameter",
				"Add parameter",
				[
					{
						id: "jeapiName",
						type: "input",
						label: "Name",
						required: true,
						placeholder: "Enter name",
						minlength: 1,
						maxlength: 255,
						inputRules: {
							excluded: excludedNames,
							patterns: ['^[a-zA-Z0-9_.-]*$'],
						},
						value: '',
					},
					{
						id: "jeapdType",
						type: "dropdown",
						label: "Type",
						required: true,
						value: 'string',
						items: $scope.types,
					},
					{
						id: "jeapiDefaultValue",
						type: "input",
						label: "Default Value",
						placeholder: "Enter default value",
						value: '',
					},
					{
						id: "jeapiChoices",
						type: "input",
						label: "Choices",
						placeholder: "Comma separated choices",
						value: '',
						visibility: {
							hidden: true,
							id: "jeapdType",
							value: "choice",
						},
					},
					{
						id: "jeapiDescription",
						type: "input",
						label: "Description",
						placeholder: "Enter description",
						value: '',
					},
				],
				[{
					id: "b1",
					type: "emphasized",
					label: "Add",
					whenValid: true,
				},
				{
					id: "b2",
					type: "transparent",
					label: "Cancel",
				}],
				"jobsEditor.parameter.add",
				"Adding parameter..."
			);
		};

		$scope.editParameter = function (index) {
			$scope.editParameterIndex = index;
			let excludedNames = [];
			for (let i = 0; i < $scope.job.parameters.length; i++) {
				if (i !== index)
					excludedNames.push($scope.job.parameters[i].name);
			}
			messageHub.showFormDialog(
				"jobsEditorEditParameter",
				"Edit parameter",
				[
					{
						id: "jeepiName",
						type: "input",
						label: "Name",
						required: true,
						placeholder: "Enter name",
						minlength: 1,
						maxlength: 255,
						inputRules: {
							excluded: excludedNames,
							patterns: ['^[a-zA-Z0-9_.-]*$'],
						},
						value: $scope.job.parameters[index].name,
					},
					{
						id: "jeepdType",
						type: "dropdown",
						label: "Type",
						required: true,
						value: $scope.job.parameters[index].type,
						items: $scope.types,
					},
					{
						id: "jeepiDefaultValue",
						type: "input",
						label: "Default Value",
						placeholder: "Enter default value",
						value: $scope.job.parameters[index].defaultValue,
					},
					{
						id: "jeepiChoices",
						type: "input",
						label: "Choices",
						placeholder: "Comma separated choices",
						value: $scope.job.parameters[index]['choices'] || '',
						visibility: {
							hidden: true,
							id: "jeepdType",
							value: "choice",
						},
					},
					{
						id: "jeepiDescription",
						type: "input",
						label: "Description",
						placeholder: "Enter description",
						value: $scope.job.parameters[index].description,
					},
				],
				[{
					id: "b1",
					type: "emphasized",
					label: "Update",
					whenValid: true,
				},
				{
					id: "b2",
					type: "transparent",
					label: "Cancel",
				}],
				"jobsEditor.parameter.edit",
				"Updating parameter..."
			);
		};

		$scope.deleteParameter = function (index) {
			messageHub.showDialogAsync(
				`Delete ${$scope.job.parameters[index].name}?`,
				'This action cannot be undone.',
				[{
					id: 'b1',
					type: 'negative',
					label: 'Delete',
				},
				{
					id: 'b2',
					type: 'transparent',
					label: 'Cancel',
				}],
			).then(function (dialogResponse) {
				if (dialogResponse.data === 'b1') {
					$scope.$apply(function () {
						$scope.job.parameters.splice(index, 1);
					});
				}
			});
		};

		$scope.dataParameters = ViewParameters.get();
		if (!$scope.dataParameters.hasOwnProperty('file')) {
			$scope.state.error = true;
			$scope.errorMessage = "The 'file' data parameter is missing.";
		} else if (!$scope.dataParameters.hasOwnProperty('contentType')) {
			$scope.state.error = true;
			$scope.errorMessage = "The 'contentType' data parameter is missing.";
		} else load();
	});