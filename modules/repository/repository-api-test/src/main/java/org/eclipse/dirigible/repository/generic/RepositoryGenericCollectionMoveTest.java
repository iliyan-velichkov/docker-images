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
package org.eclipse.dirigible.repository.generic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.dirigible.repository.api.ICollection;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.api.IResource;
import org.junit.Test;

/**
 * The Class RepositoryGenericCollectionMoveTest.
 */
public class RepositoryGenericCollectionMoveTest {

    /** The repository. */
    protected IRepository repository;

    /**
     * Test move.
     */
    @Test
    public void testMove() {
        if (repository == null) {
            return;
        }

        ICollection collection = null;
        try {
            collection = repository.createCollection("/a/b/c"); //$NON-NLS-1$
            assertNotNull(collection);
            assertTrue(collection.exists());

            collection.moveTo("/a/b/x");

            collection = repository.getCollection("/a/b/x"); //$NON-NLS-1$
            assertNotNull(collection);
            assertTrue(collection.exists());
            assertEquals("x", collection.getName());

            collection = repository.getCollection("/a/b/c"); //$NON-NLS-1$
            assertNotNull(collection);
            assertFalse(collection.exists());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                repository.removeCollection("/a");
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }

    /**
     * Test move deep.
     */
    @Test
    public void testMoveDeep() {
        if (repository == null) {
            return;
        }

        ICollection collection = null;
        try {
            collection = repository.createCollection("/a/b/c"); //$NON-NLS-1$
            assertNotNull(collection);
            assertTrue(collection.exists());

            repository.createCollection("/a/b/c/d");
            repository.createCollection("/a/b/c/d/e");
            repository.createResource("/a/b/c/d/e/f.txt", "test".getBytes());

            collection.moveTo("/a/b/x");

            collection = repository.getCollection("/a/b/x"); //$NON-NLS-1$
            assertNotNull(collection);
            assertTrue(collection.exists());
            assertEquals("x", collection.getName());
            collection = repository.getCollection("/a/b/x/d"); //$NON-NLS-1$
            assertNotNull(collection);
            assertTrue(collection.exists());
            collection = repository.getCollection("/a/b/x/d/e"); //$NON-NLS-1$
            assertNotNull(collection);
            assertTrue(collection.exists());
            IResource resource = repository.getResource("/a/b/x/d/e/f.txt"); //$NON-NLS-1$
            assertNotNull(resource);
            assertTrue(resource.exists());
            assertTrue("test".equals(new String(resource.getContent())));

            collection = repository.getCollection("/a/b/c"); //$NON-NLS-1$
            assertNotNull(collection);
            assertFalse(collection.exists());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                repository.removeCollection("/a");
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }
    }

}
