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
package org.eclipse.dirigible.database.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.dirigible.database.persistence.PersistenceManager;
import org.junit.Test;

/**
 * The Class PersistenceManagerGeneratedValueSequenceTest.
 */
public class PersistenceManagerGeneratedValueSequenceTest extends AbstractPersistenceManagerTest {

    /**
     * Inquiry CRUD tests.
     *
     * @throws SQLException the SQL exception
     */
    @Test
    public void inquiryCrudTests() throws SQLException {
        PersistenceManager<Inquiry> persistenceManager = new PersistenceManager<Inquiry>();
        Connection connection = null;
        try {
            connection = getDataSource().getConnection();
            // create table
            createTableForPojo(connection, persistenceManager);
            // check whether it is created successfully
            assertTrue(existsTable(connection, persistenceManager));
            // insert a record in the table for a pojo
            insertPojo(connection, persistenceManager);
            // insert a record in the table for a pojo
            insertSecondPojo(connection, persistenceManager);
            // get the list of all the records
            findAllPojo(connection, persistenceManager);
            // drop the table
            dropTableForPojo(connection, persistenceManager);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Creates the table for pojo.
     *
     * @param connection the connection
     * @param persistenceManager the persistence manager
     */
    private void createTableForPojo(Connection connection, PersistenceManager<Inquiry> persistenceManager) {
        persistenceManager.tableCreate(connection, Inquiry.class);
    }

    /**
     * Exists table.
     *
     * @param connection the connection
     * @param persistenceManager the persistence manager
     * @return true, if successful
     */
    private boolean existsTable(Connection connection, PersistenceManager<Inquiry> persistenceManager) {
        return persistenceManager.tableExists(connection, Inquiry.class);
    }

    /**
     * Insert pojo.
     *
     * @param connection the connection
     * @param persistenceManager the persistence manager
     */
    private void insertPojo(Connection connection, PersistenceManager<Inquiry> persistenceManager) {
        Inquiry inquiry = new Inquiry();
        inquiry.setSubject("Subject 1");
        persistenceManager.insert(connection, inquiry);
    }

    /**
     * Insert second pojo.
     *
     * @param connection the connection
     * @param persistenceManager the persistence manager
     */
    private void insertSecondPojo(Connection connection, PersistenceManager<Inquiry> persistenceManager) {
        Inquiry inquiry = new Inquiry();
        inquiry.setSubject("Subject 2");
        persistenceManager.insert(connection, inquiry);
    }

    /**
     * Find all pojo.
     *
     * @param connection the connection
     * @param persistenceManager the persistence manager
     */
    private void findAllPojo(Connection connection, PersistenceManager<Inquiry> persistenceManager) {
        List<Inquiry> list = persistenceManager.findAll(connection, Inquiry.class);

        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
        Inquiry inquiry = list.get(0);
        assertEquals("Subject 1", inquiry.getSubject());

        System.out.println(inquiry.getId());

    }

    /**
     * Drop table for pojo.
     *
     * @param connection the connection
     * @param persistenceManager the persistence manager
     */
    private void dropTableForPojo(Connection connection, PersistenceManager<Inquiry> persistenceManager) {
        persistenceManager.tableDrop(connection, Inquiry.class);
    }

}
