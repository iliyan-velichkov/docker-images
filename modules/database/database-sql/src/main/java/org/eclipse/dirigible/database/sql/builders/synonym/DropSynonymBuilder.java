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
package org.eclipse.dirigible.database.sql.builders.synonym;

import org.eclipse.dirigible.database.sql.ISqlDialect;
import org.eclipse.dirigible.database.sql.builders.AbstractDropSqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Drop Synonym Builder.
 */
public class DropSynonymBuilder extends AbstractDropSqlBuilder {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(DropSynonymBuilder.class);

    /** The synonym. */
    private String synonym = null;

    /**
     * Instantiates a new drop synonym builder.
     *
     * @param dialect the dialect
     * @param synonym the synonym
     */
    public DropSynonymBuilder(ISqlDialect dialect, String synonym) {
        super(dialect);
        this.synonym = synonym;
    }

    /**
     * Generate.
     *
     * @return the string
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.ISqlBuilder#generate()
     */
    @Override
    public String generate() {

        StringBuilder sql = new StringBuilder();

        // DROP
        generateDrop(sql);

        // SYNONYM
        generateSynonym(sql);

        String generated = sql.toString();

        if (logger.isTraceEnabled()) {
            logger.trace("generated: " + generated);
        }

        return generated;
    }

    /**
     * Generate synonym.
     *
     * @param sql the sql
     */
    protected void generateSynonym(StringBuilder sql) {
        String synonymName = (isCaseSensitive()) ? encapsulate(this.getSynonym(), true) : this.getSynonym();
        sql.append(SPACE)
           .append(KEYWORD_SYNONYM)
           .append(SPACE)
           .append(synonymName);
    }

    /**
     * Gets the synonym.
     *
     * @return the synonym
     */
    public String getSynonym() {
        return synonym;
    }

}
