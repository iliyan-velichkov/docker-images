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
exports.initRepository = function (user, email, workspaceName, projectName, repositoryName, commitMessage) {
    return org.eclipse.dirigible.components.api.git.GitFacade.initRepository(user, email, workspaceName, projectName, repositoryName, commitMessage);
}

exports.commit = function (user, userEmail, workspaceName, repositoryName, commitMessage, add)  {
    return org.eclipse.dirigible.components.api.git.GitFacade.commit(user, userEmail, workspaceName, repositoryName, commitMessage, add);
}

exports.getGitRepositories = function (workspaceName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getGitRepositories(workspaceName);
}

exports.getHistory = function(repositoryName, workspaceName, path) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getHistory(repositoryName, workspaceName, path);
}

exports.deleteRepository = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.deleteRepository(workspaceName, repositoryName);
}

exports.cloneRepository = function(workspaceName, repositoryUri, username, password, branch) {
    return org.eclipse.dirigible.components.api.git.GitFacade.cloneRepository(workspaceName, repositoryUri, username, password, branch);
}

exports.pull = function(workspaceName, repositoryName, username, password) {
    return org.eclipse.dirigible.components.api.git.GitFacade.pull(workspaceName, repositoryName, username, password);
}

exports.push = function(workspaceName, repositoryName, username, password) {
    return org.eclipse.dirigible.components.api.git.GitFacade.push(workspaceName, repositoryName, username, password);
}

exports.checkout = function(workspaceName, repositoryName, branchName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.checkout(workspaceName, repositoryName, branchName);
}

exports.createBranch = function(workspaceName, repositoryName, branchName, startingPoint) {
    return org.eclipse.dirigible.components.api.git.GitFacade.createBranch(workspaceName, repositoryName, branchName, startingPoint);
}

exports.deleteBranch = function(workspaceName, repositoryName, branchName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.createBranch(workspaceName, repositoryName, branchName);
}

exports.renameBranch = function(workspaceName, repositoryName, oldName, newName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.createBranch(workspaceName, repositoryName, oldName, newName);
}

exports.createRemoteBranch = function(workspaceName, repositoryName, branchName, startingPoint, username, password) {
    return org.eclipse.dirigible.components.api.git.GitFacade.createRemoteBranch(workspaceName, repositoryName, branchName, startingPoint, username, password);
}

exports.deleteRemoteBranch = function(workspaceName, repositoryName, branchName, username, password) {
    return org.eclipse.dirigible.components.api.git.GitFacade.createRemoteBranch(workspaceName, repositoryName, branchName, username, password);
}

exports.hardReset = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.hardReset(workspaceName, repositoryName);
}

exports.rebase = function(workspaceName, repositoryName, branchName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.rebase(workspaceName, repositoryName, branchName);
}

exports.status = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.status(workspaceName, repositoryName);
}

exports.getBranch = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getBranch(workspaceName, repositoryName);
}

exports.getLocalBranches = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getLocalBranches(workspaceName, repositoryName);
}

exports.getRemoteBranches = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getRemoteBranches(workspaceName, repositoryName);
}

exports.getUnstagedChanges = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getUnstagedChanges(workspaceName, repositoryName);
}

exports.getStagedChanges = function(workspaceName, repositoryName) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getStagedChanges(workspaceName, repositoryName);
}

exports.getFileContent = function(workspaceName, repositoryName, filePath, revStr) {
    return org.eclipse.dirigible.components.api.git.GitFacade.getFileContent(workspaceName, repositoryName, filePath, revStr);
}
