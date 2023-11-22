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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.dirigible.commons.api.helpers.ContentTypeHelper;
import org.eclipse.dirigible.commons.api.helpers.FileSystemUtils;
import org.eclipse.dirigible.repository.api.RepositoryCache;
import org.eclipse.dirigible.repository.api.RepositoryWriteException;
import org.eclipse.dirigible.repository.fs.FileSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Local Repository Dao.
 */
public class LocalRepositoryDao {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(LocalRepositoryDao.class);

    /** The Constant OBJECT_TYPE_FOLDER. */
    static final int OBJECT_TYPE_FOLDER = 0;

    /** The Constant OBJECT_TYPE_DOCUMENT. */
    static final int OBJECT_TYPE_DOCUMENT = 1;

    /** The Constant OBJECT_TYPE_BINARY. */
    static final int OBJECT_TYPE_BINARY = 2;

    /** The repository. */
    private FileSystemRepository repository;

    /** The cache. */
    private final RepositoryCache cache = new RepositoryCache();


    /**
     * Instantiates a new local repository dao.
     *
     * @param repository the repository
     */
    public LocalRepositoryDao(FileSystemRepository repository) {
        this.repository = repository;
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     */
    public FileSystemRepository getRepository() {
        return this.repository;
    }

    /**
     * Creates the file.
     *
     * @param path the path
     * @param content the content
     * @param isBinary the is binary
     * @param contentType the content type
     * @throws LocalRepositoryException the local repository exception
     */
    public void createFile(String path, byte[] content, boolean isBinary, String contentType) throws LocalRepositoryException {
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            FileSystemUtils.saveFile(workspacePath, content);
            cache.put(workspacePath, content);
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }

    }

    /**
     * Check initialized.
     */
    public void checkInitialized() {
        // TODO Auto-generated method stub

    }

    /**
     * Sets the file content.
     *
     * @param localFile the local file
     * @param content the content
     */
    public void setFileContent(LocalFile localFile, byte[] content) {
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), localFile.getPath());
            FileSystemUtils.saveFile(workspacePath, content);
            cache.put(workspacePath, content);
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Gets the file content.
     *
     * @param localFile the local file
     * @return the file content
     */
    public byte[] getFileContent(LocalFile localFile) {
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), localFile.getPath());
            byte[] content = cache.get(workspacePath);
            if (content == null) {
                content = FileSystemUtils.loadFile(workspacePath);
                cache.put(workspacePath, content);
            }
            return content;
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Rename file.
     *
     * @param path the path
     * @param newPath the new path
     */
    public void renameFile(String path, String newPath) {
        try {
            String workspacePathOld = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            String workspacePathNew = LocalWorkspaceMapper.getMappedName(getRepository(), newPath);
            FileSystemUtils.moveFile(workspacePathOld, workspacePathNew);
            cache.remove(workspacePathOld);
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Copy file.
     *
     * @param path the path
     * @param newPath the new path
     */
    public void copyFile(String path, String newPath) {
        try {
            String workspacePathOld = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            String workspacePathNew = LocalWorkspaceMapper.getMappedName(getRepository(), newPath);
            FileSystemUtils.copyFile(workspacePathOld, workspacePathNew);
            cache.remove(workspacePathOld);
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Removes the file by path.
     *
     * @param path the path
     */
    public void removeFileByPath(String path) {
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            FileSystemUtils.removeFile(workspacePath);
            cache.remove(workspacePath);
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Removes the folder by path.
     *
     * @param path the path
     */
    public void removeFolderByPath(String path) {
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            FileSystemUtils.removeFile(workspacePath);
            cache.clear();
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Creates the folder.
     *
     * @param normalizePath the normalize path
     */
    public void createFolder(String normalizePath) {
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), normalizePath);
            FileSystemUtils.createFolder(workspacePath);
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (RepositoryWriteException e) {
            throw new LocalRepositoryException(e);
        }
    }

    /**
     * Rename folder.
     *
     * @param path the path
     * @param newPath the new path
     */
    public void renameFolder(String path, String newPath) {
        try {
            String workspacePathOld = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            String workspacePathNew = LocalWorkspaceMapper.getMappedName(getRepository(), newPath);
            FileSystemUtils.moveFile(workspacePathOld, workspacePathNew);
            cache.clear();
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }

    }

    /**
     * Copy folder.
     *
     * @param path the path
     * @param newPath the new path
     */
    public void copyFolder(String path, String newPath) {
        try {
            String workspacePathOld = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            String workspacePathNew = LocalWorkspaceMapper.getMappedName(getRepository(), newPath);
            FileSystemUtils.copyFolder(workspacePathOld, workspacePathNew, new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return !".git".equals(pathname.getName());
                }
            });
            cache.clear();
            ((LocalRepository) getRepository()).setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }

    }

    /**
     * Gets the object by path.
     *
     * @param path the path
     * @return the object by path
     */
    public LocalObject getObjectByPath(String path) {

        LocalObject localObject = null;

        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            File objectFile = new File(workspacePath);
            if (!objectFile.exists()) {
                // This is folder, that was not created
                if (ContentTypeHelper.getExtension(workspacePath)
                                     .isEmpty()
                        && !workspacePath.endsWith(".")) {
                    return null;
                }
            }
            if (objectFile.isFile()) {
                String contentType = ContentTypeHelper.getContentType(FileSystemUtils.getExtension(workspacePath));
                localObject = new LocalFile(repository, ContentTypeHelper.isBinary(contentType), contentType);
            } else {
                localObject = new LocalFolder(repository);
            }
            localObject.setName(objectFile.getName());
            localObject.setPath(workspacePath);
            localObject.setModifiedAt(new Date(objectFile.lastModified()));
            String owner;
            try {
                owner = Files.getOwner(objectFile.toPath())
                             .getName();
            } catch (Exception e) {
                owner = "SYSTEM";
            }
            localObject.setModifiedBy(owner);

        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
        return localObject;

    }

    /**
     * Gets the children by folder.
     *
     * @param path the path
     * @return the children by folder
     */
    public List<LocalObject> getChildrenByFolder(String path) {
        List<LocalObject> localObjects = new ArrayList<LocalObject>();
        try {
            String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
            File objectFile = new File(workspacePath);
            if (objectFile.isDirectory()) {
                File[] children = FileSystemUtils.listFiles(objectFile);
                if (children != null) {
                    for (File file : children) {
                        localObjects.add(getObjectByPath(file.getAbsolutePath()));
                    }
                }
            }
        } catch (IOException e) {
            throw new LocalRepositoryException(e);
        }
        return localObjects;
    }

    /**
     * Check whether the file exists.
     *
     * @param path the path
     * @return true if exists
     */
    public boolean fileExists(String path) {
        String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
        return FileSystemUtils.fileExists(workspacePath);
    }

    /**
     * Check whether the directory exists.
     *
     * @param path the path
     * @return true if exists
     */
    public boolean directoryExists(String path) {
        String workspacePath = LocalWorkspaceMapper.getMappedName(getRepository(), path);
        return FileSystemUtils.directoryExists(workspacePath);
    }

}
