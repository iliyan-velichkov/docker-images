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
package org.eclipse.dirigible.database.sql.builders.table;

import org.eclipse.dirigible.database.sql.ISqlDialect;

/**
 * The Create Table Unique Index Builder.
 */
public class CreateTableUniqueIndexBuilder extends AbstractCreateTableConstraintBuilder<CreateTableUniqueIndexBuilder> {

    /** The index type. */
    private String indexType;

    /** The order. */
    private String order;

    /**
     * Instantiates a new creates the table unique index builder.
     *
     * @param dialect the dialect
     * @param name the name
     */
    public CreateTableUniqueIndexBuilder(ISqlDialect dialect, String name) {
        super(dialect, name);
    }



    /**
     * Gets the index type.
     *
     * @return the index type
     */
    public String getIndexType() {
        return indexType;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    /**
     * Sets the index type.
     *
     * @param indexType the new index type
     */
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    /**
     * Sets the order.
     *
     * @param order the new order
     */
    public void setOrder(String order) {
        this.order = order;
    }
}
