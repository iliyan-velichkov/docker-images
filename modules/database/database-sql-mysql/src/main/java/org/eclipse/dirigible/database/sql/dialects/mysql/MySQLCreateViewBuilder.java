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
package org.eclipse.dirigible.database.sql.dialects.mysql;

import org.eclipse.dirigible.database.sql.ISqlDialect;
import org.eclipse.dirigible.database.sql.builders.view.CreateViewBuilder;

/**
 * The Class MySQLCreateViewBuilder.
 */
public class MySQLCreateViewBuilder extends CreateViewBuilder {

    /** The values. */
    private String values = null;


    /**
     * Instantiates a new my SQL create view builder.
     *
     * @param dialect the dialect
     * @param view the view
     */
    public MySQLCreateViewBuilder(ISqlDialect dialect, String view) {
        super(dialect, view);
    }


    /**
     * As select.
     *
     * @param select the select
     * @return the my SQL create view builder
     */
    @Override
    public MySQLCreateViewBuilder asSelect(String select) {

        if (this.values != null) {
            throw new IllegalStateException("Create VIEW can use either AS SELECT or AS VALUES, but not both.");
        }
        setSelect(this.getSelectProperEscaping(this.getSelectProperEscaping(select)));
        return this;
    }

    /**
     * Gets the select proper escaping.
     *
     * @param select the select
     * @return the select proper escaping
     */
    private String getSelectProperEscaping(String select) {
        return select.replaceAll("\"", "`");
    }


}
