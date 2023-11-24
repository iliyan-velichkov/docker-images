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
package org.eclipse.dirigible.components.data.structures.synchronizer.table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dirigible.commons.config.Configuration;
import org.eclipse.dirigible.components.data.structures.domain.Table;
import org.eclipse.dirigible.components.data.structures.domain.TableColumn;
import org.eclipse.dirigible.components.database.DatabaseParameters;
import org.eclipse.dirigible.components.database.DatabaseNameNormalizer;
import org.eclipse.dirigible.database.sql.DataType;
import org.eclipse.dirigible.database.sql.DataTypeUtils;
import org.eclipse.dirigible.database.sql.ISqlKeywords;
import org.eclipse.dirigible.database.sql.SqlFactory;
import org.eclipse.dirigible.database.sql.builders.table.AlterTableBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Table Alter Processor.
 */
public class TableAlterProcessor {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(TableAlterProcessor.class);

    /** The Constant INCOMPATIBLE_CHANGE_OF_TABLE. */
    private static final String INCOMPATIBLE_CHANGE_OF_TABLE = "Incompatible change of table [%s] by adding a column [%s] which is [%s]"; //$NON-NLS-1$

    /**
     * Execute the corresponding statement.
     *
     * @param connection the connection
     * @param tableModel the table model
     * @throws SQLException the SQL exception
     */
    public static void execute(Connection connection, Table tableModel) throws SQLException {
        boolean caseSensitive =
                Boolean.parseBoolean(Configuration.get(DatabaseParameters.DIRIGIBLE_DATABASE_NAMES_CASE_SENSITIVE, "false"));
        String tableName = tableModel.getName();
        if (caseSensitive) {
            tableName = "\"" + tableName + "\"";
        }
        if (logger.isInfoEnabled()) {
            logger.info("Processing Alter Table: " + tableName);
        }

        Map<String, String> columnDefinitions = new HashMap<String, String>();
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet rsColumns = dmd.getColumns(null, null, DatabaseNameNormalizer.normalizeTableName(tableName), null);
        while (rsColumns.next()) {
            // String typeName =
            // nativeDialect.getDataTypeName(DataTypeUtils.getDatabaseType(rsColumns.getInt(5)));
            String typeName = DataTypeUtils.getDatabaseTypeName(rsColumns.getInt(5));
            columnDefinitions.put(rsColumns.getString(4)
                                           .toUpperCase(),
                    typeName);
        }

        List<String> modelColumnNames = new ArrayList<String>();

        // ADD iteration
        for (TableColumn columnModel : tableModel.getColumns()) {
            String name = columnModel.getName();
            String nameOriginal = name;
            if (caseSensitive) {
                name = "\"" + name + "\"";
            }
            DataType type = DataType.valueOfByName(columnModel.getType());
            String length = columnModel.getLength();
            boolean isNullable = columnModel.isNullable();
            boolean isPrimaryKey = columnModel.isPrimaryKey();
            boolean isUnique = columnModel.isUnique();
            String defaultValue = columnModel.getDefaultValue();
            String scale = columnModel.getScale();
            String args = "";
            if (length != null) {
                if (type.equals(DataType.VARCHAR) || type.equals(DataType.CHAR) || type.equals(DataType.NVARCHAR)
                        || type.equals(DataType.CHARACTER_VARYING) || type.equals(DataType.CHARACTER)) {
                    args = ISqlKeywords.OPEN + length + ISqlKeywords.CLOSE;
                }
                if (scale != null) {
                    if (type.equals(DataType.DECIMAL)) {
                        args = ISqlKeywords.OPEN + length + "," + scale + ISqlKeywords.CLOSE;
                    }
                }
            }
            if (defaultValue != null) {
                if ("".equals(defaultValue)) {
                    if (type.equals(DataType.VARCHAR) || type.equals(DataType.CHAR) || type.equals(DataType.NVARCHAR)
                            || type.equals(DataType.CHARACTER_VARYING) || type.equals(DataType.CHARACTER)) {
                        args += " DEFAULT '" + defaultValue + "' ";
                    }
                } else {
                    args += " DEFAULT " + defaultValue + " ";
                }

            }

            modelColumnNames.add(name.toUpperCase());

            if (!columnDefinitions.containsKey(nameOriginal.toUpperCase())) {

                AlterTableBuilder alterTableBuilder = SqlFactory.getNative(connection)
                                                                .alter()
                                                                .table(tableName);

                alterTableBuilder.add()
                                 .column(name, type, isPrimaryKey, isNullable, isUnique, args);

                if (!isNullable) {
                    throw new SQLException(String.format(INCOMPATIBLE_CHANGE_OF_TABLE, tableName, name, "NOT NULL"));
                }
                if (isPrimaryKey) {
                    throw new SQLException(String.format(INCOMPATIBLE_CHANGE_OF_TABLE, tableName, name, "PRIMARY KEY"));
                }

                executeAlterBuilder(connection, alterTableBuilder);

            } else if (!columnDefinitions.get(nameOriginal.toUpperCase())
                                         .equals(type.toString())) {
                throw new SQLException(String.format(INCOMPATIBLE_CHANGE_OF_TABLE, tableName, name,
                        "of type " + columnDefinitions.get(name) + " to be changed to" + type));
            }
        }

        // DROP iteration

        for (String columnName : columnDefinitions.keySet()) {
            if (caseSensitive) {
                columnName = "\"" + columnName + "\"";
            }
            if (!modelColumnNames.contains(columnName.toUpperCase())) {
                AlterTableBuilder alterTableBuilder = SqlFactory.getNative(connection)
                                                                .alter()
                                                                .table(tableName);
                alterTableBuilder.drop()
                                 .column(columnName, DataType.BOOLEAN);
                executeAlterBuilder(connection, alterTableBuilder);
            }
        }

    }

    /**
     * Execute alter builder.
     *
     * @param connection the connection
     * @param alterTableBuilder the alter table builder
     * @throws SQLException the SQL exception
     */
    private static void executeAlterBuilder(Connection connection, AlterTableBuilder alterTableBuilder) throws SQLException {
        final String sql = alterTableBuilder.build();
        if (logger.isInfoEnabled()) {
            logger.info(sql);
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            statement.executeUpdate();
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error(sql);
            }
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

}
