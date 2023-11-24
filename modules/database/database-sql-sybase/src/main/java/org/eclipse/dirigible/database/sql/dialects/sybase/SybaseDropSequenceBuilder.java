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
package org.eclipse.dirigible.database.sql.dialects.sybase;

import org.eclipse.dirigible.database.sql.ISqlDialect;
import org.eclipse.dirigible.database.sql.builders.sequence.DropSequenceBuilder;

/**
 * The Sybase Drop Sequence Builder.
 */
public class SybaseDropSequenceBuilder extends DropSequenceBuilder {

    /**
     * Instantiates a new Sybase drop sequence builder.
     *
     * @param dialect the dialect
     * @param sequence the sequence
     */
    public SybaseDropSequenceBuilder(ISqlDialect dialect, String sequence) {
        super(dialect, sequence);
    }

    /**
     * Generate.
     *
     * @return the string
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.builders.sequence.DropSequenceBuilder#generate()
     */
    @Override
    public String generate() {
        throw new IllegalStateException("Sybase does not support Sequences");
    }

}
