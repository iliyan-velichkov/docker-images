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
let registryView = angular.module('registry', ['ideUI', 'ideView', 'ideRepository', 'ideRegistry']);
registryView.controller('RegistryController', [
	'$scope',
	'messageHub',
	'ViewParameters',
	'repositoryApi',
	'registryApi',
	function (
		$scope,
		messageHub,
		ViewParameters,
		repositoryApi,
		registryApi
	) {
		$scope.searchVisible = false;
		$scope.searchField = { text: '' };
		$scope.newNodeData = {
			parent: '',
			path: '',
		};
		$scope.renameNodeData;
		$scope.imageFileExts = ['ico', 'bmp', 'png', 'jpg', 'jpeg', 'gif', 'svg'];
		$scope.modelFileExts = ['extension', 'extensionpoint', 'edm', 'model', 'dsm', 'schema', 'bpmn', 'job', 'listener', 'websocket', 'roles', 'constraints', 'table', 'view'];

		$scope.treeData = [];
		$scope.basePath = '/';

		$scope.jstreeWidget = angular.element('#dgRegistry');
		$scope.spinnerObj = {
			text: "Loading...",
			type: "spinner",
			li_attr: { spinner: true },
		};
		$scope.jstreeConfig = {
			core: {
				check_callback: true,
				themes: {
					name: "fiori",
					variant: "compact",
				},
				data: function (node, cb) {
					cb($scope.treeData);
				},
			},
			search: {
				case_sensitive: false,
			},
			plugins: ['wholerow', 'search', 'state', 'types', 'sort'],
			dnd: {
				large_drop_target: true,
				large_drag_target: true,
			},
			sort: function (firstNodeId, secondNodeId) {
				let firstNode = this.get_node(firstNodeId);
				let secondNode = this.get_node(secondNodeId);
				if (firstNode.type === "spinner") return -1;
				else if (secondNode.type === "spinner") return 1;
				else if (firstNode.type === secondNode.type) {
					let res = firstNode.text.localeCompare(secondNode.text, "en-GB", { numeric: true, sensitivity: "base" });
					if (res < 0) return -1;
					else if (res > 0) return 1;
					return 0;
				} else if (firstNode.type === "folder") return -1;
				else if (secondNode.type === "folder") return 1;
				else {
					let res = firstNode.text.localeCompare(secondNode.text, "en-GB", { numeric: true, sensitivity: "base" });
					if (res < 0) return -1;
					else if (res > 0) return 1;
					return 0;
				}
			},
			state: { key: 'ide-registry' },
			types: {
				"default": {
					icon: "sap-icon--question-mark",
					valid_children: [],
				},
				file: {
					icon: "jstree-file",
					valid_children: [],
				},
				folder: {
					icon: "jstree-folder",
					valid_children: ['folder', 'file', 'spinner'],
				},
				spinner: {
					icon: "jstree-spinner",
					valid_children: [],
				},
			},
		};

		$scope.jstreeWidget.on('dblclick.jstree', function (event) {
			let node = $scope.jstreeWidget.jstree(true).get_node(event.target);
			if (node.type === 'file') {
				console.log(node);
				openFile(node, 'monaco'); // Temporarily set monaco
			}
		});

		function getChildrenNames(node, type = '') {
			let root = $scope.jstreeWidget.jstree(true).get_node(node);
			let names = [];
			if (type) {
				for (let i = 0; i < root.children.length; i++) {
					let child = $scope.jstreeWidget.jstree(true).get_node(root.children[i]);
					if (child.type === type) names.push(child.text);
				}
			} else {
				for (let i = 0; i < root.children.length; i++) {
					names.push($scope.jstreeWidget.jstree(true).get_text(root.children[i]));
				}
			}
			return names;
		}

		$scope.toggleSearch = function () {
			$scope.searchField.text = '';
			$scope.jstreeWidget.jstree(true).clear_search();
			$scope.searchVisible = !$scope.searchVisible;
		};

		$scope.deleteFileFolder = function (path, callback) {
			repositoryApi.remove(path).then(function (response) {
				if (response.status !== 204) {
					messageHub.setStatusError(`Unable to delete '${path}'.`);
				} else {
					messageHub.setStatusMessage(`Deleted '${path}'.`);
					if (callback) callback();
				}
			});
		};

		$scope.reloadFileTree = function (basePath, setConfig = false) {
			$scope.treeData.length = 0;
			registryApi.load(basePath).then(function (response) {
				let collections = processChildren(response.data.collections);
				let resources = processChildren(response.data.resources);
				$scope.treeData = [].concat(collections, resources);
				if (setConfig) $scope.jstreeWidget.jstree($scope.jstreeConfig);
				else $scope.jstreeWidget.jstree(true).refresh();
			})
		};

		$scope.contextMenuContent = function (element) {
			if ($scope.jstreeWidget[0].contains(element)) {
				let id;
				if (element.tagName !== "LI") {
					let closest = element.closest("li");
					if (closest) id = closest.id;
					else return {
						callbackTopic: "registry.tree.contextmenu",
						hasIcons: true,
						items: [
							{
								id: "folder",
								label: "New Folder",
								icon: "sap-icon--add-folder",
								data: {
									path: $scope.basePath,
									parent: '#',
								},
							},
							{
								id: "file",
								label: "New File",
								icon: "sap-icon--add-document",
								data: {
									path: $scope.basePath,
									parent: '#',
								},
							},
						]
					}
				} else {
					id = element.id;
				}
				if (id) {
					let node = $scope.jstreeWidget.jstree(true).get_node(id);
					let newSubmenu = {
						id: "new",
						label: "New",
						icon: "sap-icon--create",
						items: [
							{
								id: "file",
								label: "File",
								data: {
									path: node.data.path,
									parent: node.id
								},
							},
							{
								id: "folder",
								label: "Folder",
								data: {
									path: node.data.path,
									parent: node.id
								},
							}
						]
					};
					let deleteObj = {
						id: "delete",
						label: "Delete",
						divider: true,
						shortcut: "Del",
						icon: "sap-icon--delete",
						data: node,
					};
					let menuObj = {
						callbackTopic: 'registry.tree.contextmenu',
						hasIcons: true,
						items: [],
					};
					if (node.type === "folder") {
						menuObj.items.push(newSubmenu);
					} else if (node.type === "file") {
						let open = {
							id: "open",
							label: "Open",
							icon: "sap-icon--action",
							data: node,
						};
						// Temporarily disabled because of editor issues
						// let openWith = {
						//     id: "openWith",
						//     label: "Open With",
						//     icon: "sap-icon--action",
						//     items: getEditorsForType(node)
						// };
						menuObj.items.push(open);
						// menuObj.items.push(openWith);
					}
					menuObj.items.push(deleteObj);
					return menuObj;
				}
				return;
			} else return;
		};

		let to = 0;
		$scope.search = function () {
			if (to) { clearTimeout(to); }
			to = setTimeout(function () {
				$scope.jstreeWidget.jstree(true).search($scope.searchField.text);
			}, 250);
		};

		function processChildren(children) {
			let treeChildren = [];
			for (let i = 0; i < children.length; i++) {
				let child = {
					text: children[i].name,
					type: (children[i].type === 'collection' ? 'folder' : 'file'),
					data: {
						path: `/registry/public${children[i].path.slice(9)}`,
					}
				};
				if (children[i].type === 'resource') {
					child.data.contentType = children[i].contentType;
					let icon = getFileIcon(children[i].name);
					if (icon) child.icon = icon;
				}
				if (children[i].collections && children[i].resources) {
					child['children'] = processChildren(children[i].collections.concat(children[i].resources));
				} else if (children[i].collections) {
					child['children'] = processChildren(children[i].collections);
				} else if (children[i].resources) {
					child['children'] = processChildren(children[i].resources);
				}
				treeChildren.push(child);
			}
			return treeChildren;
		}

		function getFileExtension(fileName) {
			return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length).toLowerCase();
		}

		function getFileIcon(fileName) {
			let ext = getFileExtension(fileName);
			let icon;
			if (ext === 'js' || ext === 'mjs' || ext === 'xsjs' || ext === 'ts' || ext === 'json') {
				icon = "sap-icon--syntax";
			} else if (ext === 'css' || ext === 'less' || ext === 'scss') {
				icon = "sap-icon--number-sign";
			} else if (ext === 'txt') {
				icon = "sap-icon--text";
			} else if (ext === 'pdf') {
				icon = "sap-icon--pdf-attachment";
			} else if ($scope.imageFileExts.indexOf(ext) !== -1) {
				icon = "sap-icon--picture";
			} else if ($scope.modelFileExts.indexOf(ext) !== -1) {
				icon = "sap-icon--document-text";
			} else {
				icon = 'jstree-file';
			}
			return icon;
		}

		function openFile(node, editor) {
			messageHub.openEditor(
				node.data.path,
				node.text,
				node.data.contentType,
				editor,
				{
					readOnly: $scope.parameters.perspectiveId !== 'workbench',
					resourceType: "repository",
				},
			);
		}

		function createFile(parent, name, path) {
			repositoryApi.createResource(path, name).then(function (response) {
				if (response.status === 201) {
					$scope.jstreeWidget.jstree(true).deselect_all(true);
					$scope.jstreeWidget.jstree(true).select_node(
						$scope.jstreeWidget.jstree(true).create_node(
							parent,
							{
								text: name,
								type: 'file',
								data: {
									path: (path.endsWith('/') ? path + name : `${path}/${name}`),
									contentType: 'text/plain',
								}
							},
						)
					);
					// Bug #1948
					// repositoryApi.getMetadata(response.data).then(function (metadata) {
					// 	if (metadata.status === 200) {
					// 		$scope.jstreeWidget.jstree(true).deselect_all(true);
					// 		$scope.jstreeWidget.jstree(true).select_node(
					// 			$scope.jstreeWidget.jstree(true).create_node(
					// 				parent,
					// 				{
					// 					text: metadata.data.name,
					// 					type: 'file',
					// 					data: {
					// 						path: metadata.data.path,
					// 						contentType: metadata.data.contentType,
					// 					}
					// 				},
					// 			)
					// 		);
					// 	} else {
					// 		messageHub.showAlertError('Could not get metadata', `There was an error while getting metadata for '${name}'`);
					// 	}
					// });
				} else {
					messageHub.showAlertError('Could not create a file', `There was an error while creating '${name}'`);
				}
			});
		}

		function createFolder(parent, name, path) {
			repositoryApi.createCollection(path, name).then(function (response) {
				if (response.status === 201) {
					$scope.jstreeWidget.jstree(true).deselect_all(true);
					$scope.jstreeWidget.jstree(true).select_node(
						$scope.jstreeWidget.jstree(true).create_node(
							parent,
							{
								text: name,
								type: 'folder',
								data: {
									path: (path.endsWith('/') ? path + name : `${path}/${name}`),
								}
							},
						)
					);
					// Bug #1948
					// repositoryApi.getMetadata(response.data).then(function (metadata) {
					//     if (metadata.status === 200) {
					//         $scope.jstreeWidget.jstree(true).deselect_all(true);
					//         $scope.jstreeWidget.jstree(true).select_node(
					//             $scope.jstreeWidget.jstree(true).create_node(
					//                 parent,
					//                 {
					//                     text: metadata.data.name,
					//                     type: 'folder',
					//                     data: {
					//                         path: metadata.data.path,
					//                     }
					//                 },
					//             )
					//         );
					//     } else {
					//         messageHub.showAlertError('Could not get metadata', `There was an error while getting metadata for '${name}'`);
					//     }
					// });
				} else {
					messageHub.showAlertError('Could not create a folder', `There was an error while creating '${name}'`);
				}
			});
		}

		messageHub.onRepositoryModified(function () {
			$scope.reloadFileTree();
		});

		messageHub.onDidReceiveMessage(
			'registry.tree.select',
			function (msg) {
				let objects = $scope.jstreeWidget.jstree(true).get_json(
					'#',
					{
						no_state: true,
						no_li_attr: true,
						no_a_attr: true,
						flat: true
					}
				);
				for (let i = 0; i < objects.length; i++) {
					if (objects[i].data.path === msg.data.filePath) {
						$scope.jstreeWidget.jstree(true).select_node(objects[i]);
						break;
					}
				}
			},
			true
		);

		messageHub.onDidReceiveMessage(
			"registry.formDialog.create.file",
			function (msg) {
				if (msg.data.buttonId === "b1") {
					createFile($scope.newNodeData.parent, msg.data.formData[0].value, $scope.newNodeData.path);
				}
				messageHub.hideFormDialog("registryNewFileForm");
			},
			true
		);

		messageHub.onDidReceiveMessage(
			"registry.formDialog.create.folder",
			function (msg) {
				if (msg.data.buttonId === "b1") {
					createFolder($scope.newNodeData.parent, msg.data.formData[0].value, $scope.newNodeData.path);
				}
				messageHub.hideFormDialog("registryNewFolderForm");
			},
			true
		);

		messageHub.onDidReceiveMessage(
			'registry.tree.contextmenu',
			function (msg) {
				if (msg.data.itemId === 'file') {
					$scope.newNodeData.parent = msg.data.data.parent;
					if (msg.data.data.parent === '#') {
						$scope.newNodeData.path = '/registry/public';
					} else $scope.newNodeData.path = msg.data.data.path;
					messageHub.showFormDialog(
						"registryNewFileForm",
						"Create a new file",
						[{
							id: "rnffi1",
							type: "input",
							submitOnEnterId: "b1",
							label: "Name",
							required: true,
							inputRules: {
								excluded: getChildrenNames(msg.data.data.parent, 'file'),
								patterns: ['^[^/:]*$'],
							},
							value: '',
						}],
						[{
							id: "b1",
							type: "emphasized",
							label: "Create",
							whenValid: true
						},
						{
							id: "b2",
							type: "transparent",
							label: "Cancel",
						}],
						"registry.formDialog.create.file",
						"Creating..."
					);
				} else if (msg.data.itemId === 'folder') {
					$scope.newNodeData.parent = msg.data.data.parent;
					if (msg.data.data.parent === '#') {
						$scope.newNodeData.path = '/registry/public';
					} else $scope.newNodeData.path = msg.data.data.path;
					messageHub.showFormDialog(
						"registryNewFolderForm",
						"Create new folder",
						[{
							id: "rnffi1",
							type: "input",
							submitOnEnterId: "b1",
							label: "Name",
							required: true,
							inputRules: {
								excluded: getChildrenNames(msg.data.data.parent, 'folder'),
								patterns: ['^[^/:]*$'],
							},
							value: '',
						}],
						[{
							id: "b1",
							type: "emphasized",
							label: "Create",
							whenValid: true
						},
						{
							id: "b2",
							type: "transparent",
							label: "Cancel",
						}],
						"registry.formDialog.create.folder",
						"Creating..."
					);
				} else if (msg.data.itemId === 'delete') {
					messageHub.showDialogAsync(
						`Delete '${msg.data.data.text}'?`,
						'This action cannot be undone.',
						[{
							id: 'b1',
							type: 'emphasized',
							label: 'Delete',
						},
						{
							id: 'b3',
							type: 'transparent',
							label: 'Cancel',
						}],
					).then(function (dialogResponse) {
						if (dialogResponse.data === 'b1') {
							$scope.deleteFileFolder(msg.data.data.data.path, function deleteNode() {
								$scope.jstreeWidget.jstree(true).delete_node(msg.data.data);
								messageHub.closeEditor(msg.data.data.data.path);
							});
						}
					});
				}
			},
			true
		);

		// Initialization
		$scope.reloadFileTree($scope.basePath, true);
		$scope.parameters = ViewParameters.get();
	}]);