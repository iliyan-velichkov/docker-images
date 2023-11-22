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
package org.eclipse.dirigible.database.sql.dialects.snowflake;

import static java.text.MessageFormat.format;

import org.eclipse.dirigible.database.sql.ISqlDialect;
import org.eclipse.dirigible.database.sql.builders.sequence.NextValueSequenceBuilder;

/**
 * The Snowflake Next Value Sequence Builder.
 */
public class SnowflakeNextValueSequenceBuilder extends NextValueSequenceBuilder {

    /** The Constant PATTERN_SELECT_NEXT_VAL_SEQUENCE. */
    private static final String PATTERN_SELECT_NEXT_VAL_SEQUENCE = "SELECT {0}.NEXTVAL FROM DUAL";

    /**
     * Instantiates a new Snowflake next value sequence builder.
     *
     * @param dialect the dialect
     * @param sequence the sequence
     */
    public SnowflakeNextValueSequenceBuilder(ISqlDialect dialect, String sequence) {
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
     * @see org.eclipse.dirigible.database.sql.builders.sequence.NextValueSequenceBuilder#generate()
     */
    @Override
    public String generate() {
        String sequenceName = this.getSequence();
        String sql = format(PATTERN_SELECT_NEXT_VAL_SEQUENCE, sequenceName);
        return sql;
    }
}
