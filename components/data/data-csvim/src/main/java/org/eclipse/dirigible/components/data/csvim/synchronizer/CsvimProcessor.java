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
package org.eclipse.dirigible.components.data.csvim.synchronizer;

import static org.eclipse.dirigible.components.api.platform.RepositoryFacade.getResource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.dirigible.commons.config.Configuration;
import org.eclipse.dirigible.components.data.csvim.domain.Csv;
import org.eclipse.dirigible.components.data.csvim.domain.CsvFile;
import org.eclipse.dirigible.components.data.csvim.domain.CsvRecord;
import org.eclipse.dirigible.components.data.csvim.utils.CsvimUtils;
import org.eclipse.dirigible.components.data.management.domain.ColumnMetadata;
import org.eclipse.dirigible.components.data.management.domain.TableMetadata;
import org.eclipse.dirigible.database.sql.SqlFactory;
import org.eclipse.dirigible.database.sql.builders.records.SelectBuilder;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.api.IRepositoryStructure;
import org.eclipse.dirigible.repository.api.IResource;
import org.eclipse.dirigible.repository.api.RepositoryReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class CsvimProcessor.
 */
@Component
public class CsvimProcessor {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CsvimProcessor.class);

    /**
     * The Constant DIRIGIBLE_CSV_DATA_MAX_COMPARE_SIZE.
     */
    private static final String DIRIGIBLE_CSV_DATA_MAX_COMPARE_SIZE = "DIRIGIBLE_CSV_DATA_MAX_COMPARE_SIZE";

    /**
     * The Constant DIRIGIBLE_CSV_DATA_MAX_COMPARE_SIZE_DEFAULT.
     */
    private static final int DIRIGIBLE_CSV_DATA_MAX_COMPARE_SIZE_DEFAULT = 1000;

    /**
     * The Constant DIRIGIBLE_CSV_DATA_BATCH_SIZE.
     */
    private static final String DIRIGIBLE_CSV_DATA_BATCH_SIZE = "DIRIGIBLE_CSV_DATA_BATCH_SIZE";

    /**
     * The Constant DIRIGIBLE_CSV_DATA_BATCH_SIZE_DEFAULT.
     */
    private static final int DIRIGIBLE_CSV_DATA_BATCH_SIZE_DEFAULT = 100;

    /**
     * The Constant MODULE.
     */
    private static final String MODULE = "dirigible-cms-csvim";

    /**
     * The Constant ERROR_TYPE_PROCESSOR.
     */
    private static final String ERROR_TYPE_PROCESSOR = "PROCESSOR";

    /**
     * The Constant ERROR_MESSAGE_NO_PRIMARY_KEY.
     */
    private static final String ERROR_MESSAGE_NO_PRIMARY_KEY = "Error while trying to process CSV from location [%s] - no primary key.";

    /**
     * The Constant ERROR_MESSAGE_DIFFERENT_COLUMNS_SIZE.
     */
    private static final String ERROR_MESSAGE_DIFFERENT_COLUMNS_SIZE =
            "Error while trying to process CSV record with Id [%s] from location [%s]. The number of CSV items should be equal to the number of columns of the database entity.";

    /**
     * The Constant ERROR_MESSAGE_INSERT_RECORD.
     */
    private static final String ERROR_MESSAGE_INSERT_RECORD =
            "Error occurred while trying to insert in table [%s] a CSV record [%s] from location [%s].";

    /**
     * The Constant PROBLEM_MESSAGE_NO_PRIMARY_KEY.
     */
    private static final String PROBLEM_MESSAGE_NO_PRIMARY_KEY =
            "No primary key. Check the configured CSVIM delimiter, whether matches the delimiter used in the CSV data file.";

    /**
     * The Constant PROBLEM_MESSAGE_DIFFERENT_COLUMNS_SIZE.
     */
    private static final String PROBLEM_MESSAGE_DIFFERENT_COLUMNS_SIZE =
            "Error while trying to process CSV record with Id [%s]. The number of CSV items should be equal to the number of columns of the database entity.";

    /**
     * The Constant PROBLEM_MESSAGE_INSERT_RECORD.
     */
    private static final String PROBLEM_MESSAGE_INSERT_RECORD = "Error occurred while trying to insert in table [%s] a CSV record [%s].";

    /** The Constant PROBLEM_WITH_TABLE_METADATA_OR_CSVPARSER. */
    private static final String PROBLEM_WITH_TABLE_METADATA_OR_CSVPARSER =
            "No table metadata found for table [%s] or CSVParser not created";

    /**
     * The csv processor.
     */
    private final CsvProcessor csvProcessor;

    /**
     * Instantiates a new csvim processor.
     *
     * @param csvProcessor the csvprocessor service
     */
    @Autowired
    public CsvimProcessor(CsvProcessor csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    /**
     * Process.
     *
     * @param csvFile the csv file
     * @param content the content
     * @param connection the connection
     * @throws Exception the exception
     */
    public void process(CsvFile csvFile, InputStream content, Connection connection) throws Exception {
        String tableName = csvFile.getTable();
        CSVParser csvParser = getCsvParser(csvFile, content);
        TableMetadata tableMetadata = CsvimUtils.getTableMetadata(tableName, connection);
        String pkName = getPkName(tableMetadata, csvParser.getHeaderNames());

        if (tableMetadata == null) {
            String error = String.format(PROBLEM_WITH_TABLE_METADATA_OR_CSVPARSER, tableName);
            logger.error(error);
            CsvimUtils.logProcessorErrors(error, ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
            return;
        }

        List<CSVRecord> recordsToInsert = new ArrayList<>();
        List<CSVRecord> recordsToUpdate = new ArrayList<>();

        String pkNameForCSVRecord = getPkNameForCSVRecord(connection, tableName, csvParser.getHeaderNames());

        List<ColumnMetadata> tableColumns = tableMetadata.getColumns();
        boolean skipComparing = isEmptyTable(tableName, connection);

        int countAll = 0;
        int countBatch = 0;
        int batchSize = getCsvDataBatchSize();
        for (CSVRecord csvRecord : csvParser) {
            countAll++;
            countBatch++;
            if (skipComparing) {
                recordsToInsert.add(csvRecord);
            } else {
                String pkValueForCSVRecord = getPkValueForCSVRecord(csvRecord, tableMetadata, csvParser.getHeaderNames());

                if (pkValueForCSVRecord == null) {
                    recordsToInsert.add(csvRecord);
                } else {
                    if (csvRecord.size() != tableColumns.size()) {
                        CsvimUtils.logProcessorErrors(String.format(PROBLEM_MESSAGE_DIFFERENT_COLUMNS_SIZE, pkValueForCSVRecord),
                                ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
                        throw new Exception(String.format(ERROR_MESSAGE_DIFFERENT_COLUMNS_SIZE, pkValueForCSVRecord, csvFile.getFile()));
                    }
                    if (!recordExists(tableName, pkNameForCSVRecord, pkValueForCSVRecord, connection)) {
                        recordsToInsert.add(csvRecord);
                    } else {
                        recordsToUpdate.add(csvRecord);
                    }
                }
            }
            if (countBatch >= batchSize) {
                countBatch = 0;
                insertCsvRecords(connection, tableMetadata, recordsToInsert, csvParser.getHeaderNames(), csvFile);
                updateCsvRecords(connection, tableMetadata, recordsToUpdate, csvParser.getHeaderNames(), pkName, csvFile);
                recordsToInsert.clear();
                recordsToUpdate.clear();
            }
        }

        insertCsvRecords(connection, tableMetadata, recordsToInsert, csvParser.getHeaderNames(), csvFile);
        updateCsvRecords(connection, tableMetadata, recordsToUpdate, csvParser.getHeaderNames(), pkName, csvFile);

        if (countAll > 0 && csvFile.getSequence() != null) {
            int sequenceStart = countAll + 1;

            PreparedStatement preparedStatement = null;
            try {
                String createSequenceSql = SqlFactory.getNative(connection)
                                                     .create()
                                                     .sequence(csvFile.getSequence())
                                                     .start(sequenceStart)
                                                     .build();
                preparedStatement = connection.prepareStatement(createSequenceSql);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                try {
                    String alterSequenceSql = SqlFactory.getNative(connection)
                                                        .alter()
                                                        .sequence(csvFile.getSequence())
                                                        .restartWith(sequenceStart)
                                                        .build();
                    preparedStatement = connection.prepareStatement(alterSequenceSql);
                    preparedStatement.executeUpdate();
                } catch (SQLException e1) {
                    logger.error("Failed to restart database sequence [" + csvFile.getSequence() + "]", e1);
                } finally {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                }
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
        }
    }

    /**
     * Gets the csv resource.
     *
     * @param csvFile the csv file
     * @return the csv resource
     */
    public static IResource getCsvResource(CsvFile csvFile) {
        return getResource(convertToActualFileName(csvFile.getFile()));
    }

    /**
     * Convert to actual file name.
     *
     * @param fileNamePath the file name path
     * @return the string
     */
    private static String convertToActualFileName(String fileNamePath) {
        return IRepositoryStructure.PATH_REGISTRY_PUBLIC + IRepository.SEPARATOR + fileNamePath;
    }

    /**
     * Gets the csv content.
     *
     * @param resource the resource
     * @return the csv content
     * @throws RepositoryReadException the repository read exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte[] getCsvContent(IResource resource) throws RepositoryReadException, IOException {
        return resource.getContent();
    }

    private CSVParser getCsvParser(CsvFile csvFile, InputStream contentAsInputStream) throws Exception {
        try {
            CSVFormat csvFormat = createCSVFormat(csvFile);
            return CSVParser.parse(contentAsInputStream, StandardCharsets.UTF_8, csvFormat);
        } catch (Exception ex) {
            String errorMessage = String.format("Error occurred while trying to parse data from CSV file [%s].", csvFile.getFile());
            CsvimUtils.logProcessorErrors(errorMessage, ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
            logger.error(errorMessage, ex);
            throw ex;
        }
    }

    /**
     * Creates the CSV format.
     *
     * @param csvFile the csv file
     * @return the CSV format
     * @throws Exception the exception
     */
    private CSVFormat createCSVFormat(CsvFile csvFile) throws Exception {
        if (csvFile.getDelimField() != null && (!csvFile.getDelimField()
                                                        .equals(",")
                && !csvFile.getDelimField()
                           .equals(";"))) {
            String errorMessage = "Only ';' or ',' characters are supported as delimiters for CSV files.";
            CsvimUtils.logProcessorErrors(errorMessage, ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
            throw new Exception(errorMessage);
        }
        if (csvFile.getDelimEnclosing() != null && csvFile.getDelimEnclosing()
                                                          .length() > 1) {
            String errorMessage = "Delim enclosing should only contain one character.";
            CsvimUtils.logProcessorErrors(errorMessage, ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
            throw new Exception(errorMessage);
        }

        char delimiter = Objects.isNull(csvFile.getDelimField()) ? ','
                : csvFile.getDelimField()
                         .charAt(0);
        char quote = Objects.isNull(csvFile.getDelimEnclosing()) ? '"'
                : csvFile.getDelimEnclosing()
                         .charAt(0);
        CSVFormat csvFormat = CSVFormat.newFormat(delimiter)
                                       .withIgnoreEmptyLines()
                                       .withQuote(quote)
                                       .withEscape('\\');

        boolean useHeader = !Objects.isNull(csvFile.getHeader()) && csvFile.getHeader();
        if (useHeader) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }

        return csvFormat;
    }

    /**
     * Gets the pk name for CSV record.
     *
     * @param connection the connection
     * @param tableName the table name
     * @param headerNames the header names
     * @return the pk name for CSV record
     */
    private String getPkNameForCSVRecord(Connection connection, String tableName, List<String> headerNames) {
        TableMetadata tableModel = CsvimUtils.getTableMetadata(tableName, connection);
        if (tableModel != null) {
            List<ColumnMetadata> columnModels = tableModel.getColumns();
            if (headerNames.size() > 0) {
                ColumnMetadata found = columnModels.stream()
                                                   .filter(ColumnMetadata::isKey)
                                                   .findFirst()
                                                   .orElse(null);
                return found != null ? found.getName() : null;
            }

            for (ColumnMetadata columnModel : columnModels) {
                if (columnModel.isKey()) {
                    return columnModel.getName();
                }
            }
        }

        return null;
    }

    /**
     * Gets the csv data batch size.
     *
     * @return the csv data batch size
     */
    private int getCsvDataBatchSize() {
        int batchSize = DIRIGIBLE_CSV_DATA_BATCH_SIZE_DEFAULT;
        try {
            batchSize = Integer.parseInt(Configuration.get(DIRIGIBLE_CSV_DATA_BATCH_SIZE));
        } catch (NumberFormatException e) {
            // Do nothing
        }
        return batchSize;
    }

    /**
     * Insert csv records.
     *
     * @param connection the connection
     * @param tableModel the table model
     * @param recordsToProcess the records to process
     * @param headerNames the header names
     * @param csvFile the csv file
     */
    private void insertCsvRecords(Connection connection, TableMetadata tableModel, List<CSVRecord> recordsToProcess,
            List<String> headerNames, CsvFile csvFile) {
        try {
            List<CsvRecord> csvRecords = recordsToProcess.stream()
                                                         .map(e -> new CsvRecord(e, tableModel, headerNames,
                                                                 csvFile.getDistinguishEmptyFromNull()))
                                                         .collect(Collectors.toList());
            csvProcessor.insert(connection, tableModel, csvRecords, headerNames, csvFile);
        } catch (Exception e) {
            String csvRecordValue = e.getMessage();
            CsvimUtils.logProcessorErrors(String.format(PROBLEM_MESSAGE_INSERT_RECORD, tableModel.getName(), csvRecordValue),
                    ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
            if (logger.isErrorEnabled()) {
                logger.error(String.format(ERROR_MESSAGE_INSERT_RECORD, tableModel.getName(), csvRecordValue, csvFile.getFile()), e);
            }
        }
    }

    /**
     * Update csv records.
     *
     * @param connection the connection
     * @param tableModel the table model
     * @param recordsToProcess the records to process
     * @param headerNames the header names
     * @param pkName the pk name
     * @param csvFile the csv file
     */
    private void updateCsvRecords(Connection connection, TableMetadata tableModel, List<CSVRecord> recordsToProcess,
            List<String> headerNames, String pkName, CsvFile csvFile) {
        try {
            List<CsvRecord> csvRecords = recordsToProcess.stream()
                                                         .map(e -> new CsvRecord(e, tableModel, headerNames,
                                                                 csvFile.getDistinguishEmptyFromNull()))
                                                         .collect(Collectors.toList());
            csvProcessor.update(connection, tableModel, csvRecords, headerNames, pkName, csvFile);
        } catch (SQLException e) {
            String csvRecordValue = e.getMessage();
            CsvimUtils.logProcessorErrors(String.format(PROBLEM_MESSAGE_INSERT_RECORD, tableModel.getName(), csvRecordValue),
                    ERROR_TYPE_PROCESSOR, csvFile.getFile(), Csv.ARTEFACT_TYPE, MODULE);
            if (logger.isErrorEnabled()) {
                logger.error(String.format(ERROR_MESSAGE_INSERT_RECORD, tableModel.getName(), csvRecordValue, csvFile.getFile()), e);
            }
        }
    }

    /**
     * Gets the pk value for CSV record.
     *
     * @param csvRecord the csv record
     * @param tableModel the table model
     * @param headerNames the header names
     * @return the pk value for CSV record
     */
    private String getPkValueForCSVRecord(CSVRecord csvRecord, TableMetadata tableModel, List<String> headerNames) {
        if (tableModel != null) {
            List<ColumnMetadata> columnModels = tableModel.getColumns();
            if (headerNames.size() > 0) {
                ColumnMetadata found = columnModels.stream()
                                                   .filter(ColumnMetadata::isKey)
                                                   .findFirst()
                                                   .orElse(null);
                String pkColumnName = found != null ? found.getName() : null;
                if (pkColumnName != null) {
                    int csvRecordPkValueIndex = headerNames.indexOf(pkColumnName);
                    return csvRecordPkValueIndex >= 0 ? csvRecord.get(csvRecordPkValueIndex) : null;
                }
                return null;
            }

            for (int i = 0; i < csvRecord.size(); i++) {
                boolean isColumnPk = columnModels.get(i)
                                                 .isKey();
                if (isColumnPk && !StringUtils.isEmpty(csvRecord.get(i))) {
                    return csvRecord.get(i);
                }
            }
        }

        return null;
    }

    /**
     * Gets the pk value for CSV record.
     *
     * @param tableModel the table model
     * @param headerNames the header names
     * @return the pk name or null
     */
    private String getPkName(TableMetadata tableModel, List<String> headerNames) {
        if (tableModel != null) {
            List<ColumnMetadata> columnModels = tableModel.getColumns();
            if (headerNames.size() > 0) {
                ColumnMetadata found = columnModels.stream()
                                                   .filter(ColumnMetadata::isKey)
                                                   .findFirst()
                                                   .orElse(null);
                return found != null ? found.getName() : null;
            }
        }
        return null;
    }

    /**
     * Record exists.
     *
     * @param tableName the table name
     * @param pkNameForCSVRecord the pk name for CSV record
     * @param pkValueForCSVRecord the pk value for CSV record
     * @param connection the connection
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    private boolean recordExists(String tableName, String pkNameForCSVRecord, String pkValueForCSVRecord, Connection connection)
            throws SQLException {
        boolean exists = false;
        SelectBuilder selectBuilder = new SelectBuilder(SqlFactory.deriveDialect(connection));
        String sql = selectBuilder.distinct()
                                  .column("1 " + pkNameForCSVRecord)
                                  .from(tableName)
                                  .where(pkNameForCSVRecord + " = ?")
                                  .build();
        try (PreparedStatement pstmt = connection.prepareCall(sql)) {
            ResultSet rs;
            try {
                pstmt.setString(1, pkValueForCSVRecord);
                rs = pstmt.executeQuery();
            } catch (Throwable e) {
                pstmt.setInt(1, Integer.parseInt(pkValueForCSVRecord));
                rs = pstmt.executeQuery();
            }
            if (rs.next()) {
                try {
                    exists = "1".equals(rs.getString(1));
                } catch (Throwable e) {
                    exists = 1 == rs.getInt(1);
                }
            }
        }
        return exists;
    }

    /**
     * Checks if is empty table.
     *
     * @param tableName the table name
     * @param connection the connection
     * @return true, if is empty table
     * @throws SQLException the SQL exception
     */
    private boolean isEmptyTable(String tableName, Connection connection) throws SQLException {
        boolean isEmpty = false;
        SelectBuilder selectBuilder = new SelectBuilder(SqlFactory.deriveDialect(connection));
        String sql = selectBuilder.column("COUNT(*)")
                                  .from(tableName)
                                  .build();
        try (PreparedStatement pstmt = connection.prepareCall(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                isEmpty = rs.getInt(1) == 0;
            }
        }
        return isEmpty;
    }
}
