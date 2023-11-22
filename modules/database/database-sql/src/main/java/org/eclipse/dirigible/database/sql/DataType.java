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
package org.eclipse.dirigible.database.sql;

/**
 * The Enum DataType.
 */
public enum DataType {

    /** The varchar. */
    VARCHAR("VARCHAR"),

    /** The char. */
    CHAR("CHAR"),

    /** The date. */
    DATE("DATE"),

    /** The seconddate. */
    SECONDDATE("SECONDDATE"),

    /** The time. */
    TIME("TIME"),

    /** The timestamp. */
    TIMESTAMP("TIMESTAMP"),

    /** The integer. */
    INTEGER("INTEGER"),

    /** The tinyint. */
    TINYINT("TINYINT"),

    /** The bigint. */
    BIGINT("BIGINT"),

    /** The smallint. */
    SMALLINT("SMALLINT"),

    /** The real. */
    REAL("REAL"),

    /** The double. */
    DOUBLE("DOUBLE"),

    /** The double precision. */
    DOUBLE_PRECISION("DOUBLE PRECISION"),

    /** The boolean. */
    BOOLEAN("BOOLEAN"),

    /** The blob. */
    BLOB("BLOB"),

    /** The decimal. */
    DECIMAL("DECIMAL"),

    /** The bit. */
    BIT("BIT"),

    /** The nvarchar. */
    NVARCHAR("NVARCHAR"),

    /** The float. */
    FLOAT("FLOAT"),

    /** The byte. */
    BYTE("BYTE"),

    /** The nclob. */
    NCLOB("NCLOB"),

    /** The array. */
    ARRAY("ARRAY"),

    /** The varbinary. */
    VARBINARY("VARBINARY"),

    /** The binary varying. */
    BINARY_VARYING("BINARY VARYING"),

    /** The shorttext. */
    SHORTTEXT("SHORTTEXT"),

    /** The alphanum. */
    ALPHANUM("ALPHANUM"),

    /** The clob. */
    CLOB("CLOB"),

    /** The smalldecimal. */
    SMALLDECIMAL("SMALLDECIMAL"),

    /** The binary. */
    BINARY("BINARY"),

    /** The st point. */
    ST_POINT("ST_POINT"),

    /** The st geometry. */
    ST_GEOMETRY("ST_GEOMETRY"),

    /** The character varying. */
    CHARACTER_VARYING("CHARACTER VARYING"),

    /** The binary large object. */
    BINARY_LARGE_OBJECT("BINARY LARGE OBJECT"),

    /** The character large object. */
    CHARACTER_LARGE_OBJECT("CHARACTER LARGE OBJECT"),

    /** The character. */
    CHARACTER("CHARACTER"),

    /** The nchar. */
    NCHAR("NCHAR"),

    /** The numeric. */
    NUMERIC("NUMERIC");

    /** The name. */
    private String name;

    /**
     * Instantiates a new data type.
     *
     * @param name the name
     */
    DataType(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
        return name;
    }

    /**
     * Value of by name.
     *
     * @param name the name
     * @return the data type
     */
    public static final DataType valueOfByName(String name) {
        for (DataType type : DataType.class.getEnumConstants()) {
            if (type.toString()
                    .equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("DataType not found: " + name);
    }


}
