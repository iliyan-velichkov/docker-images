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

import java.util.ArrayList;
import java.util.List;

/**
 * The ProjectDescriptor transport object.
 */
public class ProjectDescriptor {

    /** The name. */
    private String name;

    /** The path. */
    private String path;

    /** The type. */
    private String type = "project";

    /** The git. */
    private boolean git;

    /** The git name. */
    private String gitName;

    /** The folders. */
    private List<FolderDescriptor> folders = new ArrayList<FolderDescriptor>();

    /** The files. */
    private List<FileDescriptor> files = new ArrayList<FileDescriptor>();

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the folders.
     *
     * @return the folders
     */
    public List<FolderDescriptor> getFolders() {
        return folders;
    }

    /**
     * Sets the.
     *
     * @param folders the folders
     */
    public void set(List<FolderDescriptor> folders) {
        this.folders = folders;
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    public List<FileDescriptor> getFiles() {
        return files;
    }

    /**
     * Sets the files.
     *
     * @param files the new files
     */
    public void setFiles(List<FileDescriptor> files) {
        this.files = files;
    }

    /**
     * Is Git enabled.
     *
     * @return the git
     */
    public boolean isGit() {
        return git;
    }

    /**
     * Set it Git enabled.
     *
     * @param git the git to set
     */
    public void setGit(boolean git) {
        this.git = git;
    }

    /**
     * Gets the git project name, which is the root folder or the repo.
     *
     * @return the gitName
     */
    public String getGitName() {
        return gitName;
    }

    /**
     * Sets the git project name, which is the root folder or the repo.
     *
     * @param name the new name
     */
    public void setGitName(String name) {
        this.gitName = name;
    }

}
