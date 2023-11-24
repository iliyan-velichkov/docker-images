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
package org.eclipse.dirigible.components.ide.workspace.json;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.fs.FileSystemRepository;
import org.eclipse.dirigible.repository.local.LocalWorkspaceMapper;

/**
 * The Class WorkspaceGitHelper.
 */
public class WorkspaceGitHelper {

    /** The Constant GIT_REPOSITORY_PROJECT_DEPTH. */
    // {workspace}/{userName}/{gitRepositoryName}
    private static final int GIT_REPOSITORY_PROJECT_DEPTH = 3;

    /** The Constant DOT_GIT. */
    private static final String DOT_GIT = ".git";

    /**
     * Get the git info. Returns a Pair. Left value is boolean indicating the existence of the git
     * directory Right value contains the path to the root git project folder name
     *
     * @param repository the repository
     * @param repositoryPath the path
     *
     * @return if the project is git aware and project root folder name
     */
    public static Pair<Boolean, String> getGitAware(IRepository repository, String repositoryPath) {
        File gitFolder = getGitFolderForProject(repository, repositoryPath);
        if (gitFolder != null && gitFolder.exists()) {
            return Pair.of(true, gitFolder.getName());
        }
        return Pair.of(false, null);
    }

    /**
     * Get the git folder.
     *
     * @param repository the repository
     * @param repositoryPath the path
     * @return the git folder
     */
    public static File getGitFolderForProject(IRepository repository, String repositoryPath) {
        try {
            if (repository instanceof FileSystemRepository) {
                String path = LocalWorkspaceMapper.getMappedName((FileSystemRepository) repository, repositoryPath);
                File directory = new File(new File(path).getCanonicalPath());
                String directoryPath = directory.getCanonicalPath();
                if (directoryPath.lastIndexOf(DOT_GIT) > 0) {
                    String projectLocation =
                            directoryPath.substring(directoryPath.lastIndexOf(DOT_GIT) + DOT_GIT.length() + IRepository.SEPARATOR.length());
                    int segmentsCount = projectLocation.split(IRepository.SEPARATOR).length - GIT_REPOSITORY_PROJECT_DEPTH;
                    for (int i = 0; i < segmentsCount; i++) {
                        directory = directory.getParentFile();
                    }
                    boolean haveGitDirectory = false;
                    for (File next : directory.listFiles()) {
                        if (next.exists() && next.isDirectory() && next.getName()
                                                                       .equals(DOT_GIT)) {
                            haveGitDirectory = true;
                            break;
                        }
                    }
                    if (directory.exists() && haveGitDirectory) {
                        return directory;
                    }
                }
            }
        } catch (Throwable e) {
            return null;
        }
        return null;
    }

    /**
     * Describe project.
     *
     * @param rootFolder the collection
     *
     * @return the project descriptor
     */
    public static ProjectDescriptor describeProject(File rootFolder) {
        ProjectDescriptor project = new ProjectDescriptor();
        project.setName(rootFolder.getName());
        project.setPath(rootFolder.getPath());
        project.setGit(true);

        List<File> allFiles = Arrays.asList(rootFolder.listFiles());
        List<File> folders = allFiles.stream()
                                     .filter(e -> !e.isFile())
                                     .collect(Collectors.toList());
        List<File> files = allFiles.stream()
                                   .filter(e -> e.isFile())
                                   .collect(Collectors.toList());
        for (File next : folders) {
            if (!next.isFile() && !next.getName()
                                       .equals(DOT_GIT)) {
                project.getFolders()
                       .add(describeFolder(next));
            }
        }

        for (File next : files) {
            FileDescriptor file = new FileDescriptor();
            file.setName(next.getName());
            file.setPath(next.getPath());
            project.getFiles()
                   .add(file);
        }

        return project;
    }

    /**
     * Describe folder.
     *
     * @param rootFolder the collection
     * @return the folder descriptor
     */
    public static FolderDescriptor describeFolder(File rootFolder) {
        FolderDescriptor folder = new FolderDescriptor();
        folder.setName(rootFolder.getName());
        folder.setPath(rootFolder.getPath());
        List<File> allFiles = Arrays.asList(rootFolder.listFiles());
        List<File> folders = allFiles.stream()
                                     .filter(e -> !e.isFile())
                                     .collect(Collectors.toList());
        List<File> files = allFiles.stream()
                                   .filter(e -> e.isFile())
                                   .collect(Collectors.toList());
        for (File next : folders) {
            folder.getFolders()
                  .add(describeFolder(next));
        }

        for (File next : files) {
            FileDescriptor file = new FileDescriptor();
            file.setName(next.getName());
            file.setPath(next.getPath());
            folder.getFiles()
                  .add(file);
        }

        return folder;
    }
}
