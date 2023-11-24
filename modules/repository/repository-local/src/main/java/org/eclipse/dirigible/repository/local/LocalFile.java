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
package org.eclipse.dirigible.repository.local;

import org.eclipse.dirigible.repository.fs.FileSystemRepository;

/**
 * Internal representation of a File/Resource kind of object.
 */
public class LocalFile extends LocalObject {

    /** The binary. */
    private boolean binary = false;

    /** The content type. */
    private String contentType;

    /**
     * Instantiates a new local file.
     *
     * @param repository the repository
     * @param isBinary the is binary
     * @param contentType the content type
     */
    public LocalFile(FileSystemRepository repository, boolean isBinary, String contentType) {
        super(repository);
        this.binary = isBinary;
        this.contentType = contentType;
    }

    /**
     * Delete.
     *
     * @throws LocalRepositoryException the local repository exception
     */
    public void delete() throws LocalRepositoryException {
        getRepository().getRepositoryDao()
                       .removeFileByPath(getPath());
    }

    /**
     * Rename.
     *
     * @param newPath the new path
     * @throws LocalRepositoryException the local repository exception
     */
    public void rename(String newPath) throws LocalRepositoryException {
        getRepository().getRepositoryDao()
                       .renameFile(getPath(), newPath);
    }

    /**
     * Copy to.
     *
     * @param newPath the new path
     * @throws LocalRepositoryException the local repository exception
     */
    public void copyTo(String newPath) throws LocalRepositoryException {
        getRepository().getRepositoryDao()
                       .copyFile(getPath(), newPath);
    }

    /**
     * Gets the data.
     *
     * @return the data
     * @throws LocalRepositoryException the local repository exception
     */
    public byte[] getData() throws LocalRepositoryException {
        return getRepository().getRepositoryDao()
                              .getFileContent(this);
    }

    /**
     * Sets the data.
     *
     * @param content the new data
     * @throws LocalRepositoryException the local repository exception
     */
    public void setData(byte[] content) throws LocalRepositoryException {
        getRepository().getRepositoryDao()
                       .setFileContent(this, content);
    }

    /**
     * Checks if is binary.
     *
     * @return true, if is binary
     */
    public boolean isBinary() {
        return binary;
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }
}
