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
 * The Create Table Primary Key Builder.
 */
public class CreateTablePrimaryKeyBuilder extends AbstractCreateTableConstraintBuilder<CreateTablePrimaryKeyBuilder> {

    /**
     * Instantiates a new creates the table primary key builder.
     *
     * @param dialect the dialect
     * @param name the name
     */
    public CreateTablePrimaryKeyBuilder(ISqlDialect dialect, String name) {
        super(dialect, name);
    }

}
