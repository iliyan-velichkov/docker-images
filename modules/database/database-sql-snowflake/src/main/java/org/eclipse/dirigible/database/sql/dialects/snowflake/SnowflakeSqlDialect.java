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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dirigible.database.sql.builders.AlterBranchingBuilder;
import org.eclipse.dirigible.database.sql.builders.CreateBranchingBuilder;
import org.eclipse.dirigible.database.sql.builders.DropBranchingBuilder;
import org.eclipse.dirigible.database.sql.builders.records.DeleteBuilder;
import org.eclipse.dirigible.database.sql.builders.records.InsertBuilder;
import org.eclipse.dirigible.database.sql.builders.records.SelectBuilder;
import org.eclipse.dirigible.database.sql.builders.records.UpdateBuilder;
import org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect;

/**
 * The Snowflake SQL Dialect.
 */
public class SnowflakeSqlDialect extends
        DefaultSqlDialect<SelectBuilder, InsertBuilder, UpdateBuilder, DeleteBuilder, CreateBranchingBuilder, AlterBranchingBuilder, DropBranchingBuilder, SnowflakeNextValueSequenceBuilder, SnowflakeLastValueIdentityBuilder> {

    /** The Constant FUNCTIONS. */
    public static final Set<String> FUNCTIONS = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(new String[] {"ABS", "ACOS",
            "ACOSH", "ADD_MONTHS", "ALERT_HISTORY", "ALL_USER_NAMES", "ANY_VALUE", "APPROX_COUNT_DISTINCT", "APPROX_PERCENTILE",
            "APPROX_PERCENTILE_ACCUMULATE", "APPROX_PERCENTILE_COMBINE", "APPROX_PERCENTILE_ESTIMATE", "APPROX_TOP_K",
            "APPROX_TOP_K_ACCUMULATE", "APPROX_TOP_K_COMBINE", "APPROX_TOP_K_ESTIMATE", "APPROXIMATE_JACCARD_INDEX",
            "APPROXIMATE_SIMILARITY", "ARRAY_AGG", "ARRAY_APPEND", "ARRAY_CAT", "ARRAY_COMPACT", "ARRAY_CONSTRUCT",
            "ARRAY_CONSTRUCT_COMPACT", "ARRAY_CONTAINS", "ARRAY_DISTINCT", "ARRAY_EXCEPT", "ARRAY_GENERATE_RANGE", "ARRAY_INSERT",
            "ARRAY_INTERSECTION", "ARRAY_MAX", "ARRAY_MIN", "ARRAY_POSITION", "ARRAY_PREPEND", "ARRAY_REMOVE", "ARRAY_REMOVE_AT",
            "ARRAY_SIZE", "ARRAY_SLICE", "ARRAY_SORT", "ARRAY_TO_STRING", "ARRAY_UNION_AGG", "ARRAY_UNIQUE_AGG", "ARRAYS_OVERLAP",
            "AS_ARRAY", "AS_BINARY", "AS_BOOLEAN", "AS_CHAR", "AS_VARCHAR", "AS_DATE", "AS_DECIMAL", "AS_NUMBER", "AS_DOUBLE", "AS_REAL",
            "AS_INTEGER", "AS_OBJECT", "AS_TIME", "AS_TIMESTAMP", "ASCII", "ASIN", "ASINH", "ATAN", "ATAN2", "ATANH",
            "AUTO_REFRESH_REGISTRATION_HISTORY", "AUTOMATIC_CLUSTERING_HISTORY", "AVG", "BASE64_DECODE_BINARY", "BASE64_DECODE_STRING",
            "BASE64_ENCODE", "BETWEEN", "BIT_LENGTH", "BITAND", "BITAND_AGG", "BITMAP_BIT_POSITION", "BITMAP_BUCKET_NUMBER",
            "BITMAP_CONSTRUCT_AGG", "BITMAP_COUNT", "BITMAP_OR_AGG", "BITNOT", "BITOR", "BITOR_AGG", "BITSHIFTLEFT", "BITSHIFTRIGHT",
            "BITXOR", "BITXOR_AGG", "BOOLAND", "BOOLAND_AGG", "BOOLNOT", "BOOLOR", "BOOLOR_AGG", "BOOLXOR", "BOOLXOR_AGG",
            "BUILD_SCOPED_FILE_URL", "BUILD_STAGE_FILE_URL", "CASE", "CAST", "CBRT", "CEIL", "CHARINDEX", "CHECK_JSON", "CHECK_XML", "CHR",
            "CHAR", "CLEANUP_DATABASE_ROLE_GRANTS", "COALESCE", "COLLATE", "COLLATION", "COMPLETE_TASK_GRAPHS", "COMPRESS", "CONCAT",
            "CONCAT_WS", "CONDITIONAL_CHANGE_EVENT", "CONDITIONAL_TRUE_EVENT", "CONTAINS", "CONVERT_TIMEZONE", "COPY_HISTORY", "CORR",
            "COS", "COSH", "COT", "COUNT", "COUNT_IF", "COVAR_POP", "COVAR_SAMP", "CUME_DIST", "CURRENT_ACCOUNT", "CURRENT_AVAILABLE_ROLES",
            "CURRENT_CLIENT", "CURRENT_DATABASE", "CURRENT_DATE", "CURRENT_IP_ADDRESS", "CURRENT_ORGANIZATION_NAME", "CURRENT_REGION",
            "CURRENT_ROLE", "CURRENT_ROLE_TYPE", "CURRENT_SCHEMA", "CURRENT_SCHEMAS", "CURRENT_SECONDARY_ROLES", "CURRENT_SESSION",
            "CURRENT_STATEMENT", "CURRENT_TASK_GRAPHS", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSACTION", "CURRENT_USER",
            "CURRENT_VERSION", "CURRENT_WAREHOUSE", "DATA_TRANSFER_HISTORY", "DATABASE_REFRESH_HISTORY", "DATABASE_REFRESH_PROGRESS",
            "DATABASE_REFRESH_PROGRESS_BY_JOB", "DATABASE_REPLICATION_USAGE_HISTORY", "DATABASE_STORAGE_USAGE_HISTORY", "DATE_FROM_PARTS",
            "DATE_PART", "DATE_TRUNC", "DATEADD", "DATEDIFF", "DAYNAME", "DECODE", "DECOMPRESS_BINARY", "DECOMPRESS_STRING", "DECRYPT",
            "DECRYPT_RAW", "DEGREES", "DENSE_RANK", "DIV0", "DIV0NULL", "DYNAMIC_TABLE_GRAPH_HISTORY", "DYNAMIC_TABLE_REFRESH_HISTORY",
            "EDITDISTANCE", "ENCRYPT", "ENCRYPT_RAW", "ENDSWITH", "EQUAL_NULL", "EXP", "EXPLAIN_JSON", "EXTERNAL_FUNCTIONS_HISTORY",
            "EXTERNAL_TABLE_FILES", "EXTERNAL_TABLE_FILE_REGISTRATION_HISTORY", "EXTRACT", "EXTRACT_SEMANTIC_CATEGORIES", "FACTORIAL",
            "FIRST_VALUE", "FLATTEN", "FLOOR", "GENERATE_COLUMN_DESCRIPTION", "GENERATOR", "GET", "GET_ABSOLUTE_PATH", "GET_DDL",
            "GET_IGNORE_CASE", "GET_OBJECT_REFERENCES", "GET_PATH", "GET_PRESIGNED_URL", "GET_QUERY_OPERATOR_STATS", "GET_RELATIVE_PATH",
            "GET_STAGE_LOCATION", "GETBIT", "GETVARIABLE", "GREATEST", "GROUPING", "GROUPING_ID", "HASH", "HASH_AGG", "HAVERSINE",
            "HEX_DECODE_BINARY", "HEX_DECODE_STRING", "HEX_ENCODE", "HLL", "HLL_ACCUMULATE", "HLL_COMBINE", "HLL_ESTIMATE", "HLL_EXPORT",
            "HLL_IMPORT", "HOUR", "MINUTE", "SECOND", "IFF", "IFNULL", "ILIKE", "ILIKE ANY", "IN", "INFER_SCHEMA", "INITCAP", "INSERT",
            "INVOKER_ROLE", "INVOKER_SHARE", "IS", "IS_ARRAY", "IS_BINARY", "IS_BOOLEAN", "IS_CHAR", "IS_VARCHAR",
            "IS_DATABASE_ROLE_IN_SESSION", "IS_DATE", "IS_DATE_VALUE", "IS_DECIMAL", "IS_DOUBLE", "IS_REAL", "IS_GRANTED_TO_INVOKER_ROLE",
            "IS_INTEGER", "IS_NULL_VALUE", "IS_OBJECT", "IS_ROLE_IN_SESSION", "IS_TIME", "IS_TIMESTAMP", "JAROWINKLER_SIMILARITY",
            "JSON_EXTRACT_PATH_TEXT", "KURTOSIS", "LAG", "LAST_DAY", "LAST_QUERY_ID", "LAST_SUCCESSFUL_SCHEDULED_TIME", "LAST_TRANSACTION",
            "LAST_VALUE", "LEAD", "LEAST", "LEFT", "LENGTH", "LEN", "LIKE", "LIKE ALL", "LIKE ANY", "LISTAGG", "LN", "LOCALTIME",
            "LOCALTIMESTAMP", "LOG", "LOGIN_HISTORY", "LOGIN_HISTORY_BY_USER", "LOWER", "LPAD", "LTRIM",
            "MATERIALIZED_VIEW_REFRESH_HISTORY", "MD5", "MD5_HEX", "MD5_BINARY", "MD5_NUMBER_LOWER64", "MD5_NUMBER_UPPER64", "MEDIAN",
            "MIN", "MAX", "MIN_BY", "MAX_BY", "MINHASH", "MINHASH_COMBINE", "MOD", "MODE", "MONTHNAME", "MONTHS_BETWEEN", "NEXT_DAY",
            "NORMAL", "NOTIFICATION_HISTORY", "NTH_VALUE", "NTILE", "NULLIF", "NULLIFZERO", "NVL", "NVL2", "OBJECT_AGG", "OBJECT_CONSTRUCT",
            "OBJECT_CONSTRUCT_KEEP_NULL", "OBJECT_DELETE", "OBJECT_INSERT", "OBJECT_KEYS", "OBJECT_PICK", "OCTET_LENGTH", "PARSE_IP",
            "PARSE_JSON", "PARSE_URL", "PARSE_XML", "PERCENT_RANK", "PERCENTILE_CONT", "PERCENTILE_DISC", "PI", "PIPE_USAGE_HISTORY",
            "POLICY_CONTEXT", "POLICY_REFERENCES", "POSITION", "POW", "POWER", "PREVIOUS_DAY", "QUERY_ACCELERATION_HISTORY",
            "QUERY_HISTORY", "RADIANS", "RANDOM", "RANDSTR", "RANK", "RATIO_TO_REPORT", "REGEXP", "REGEXP_COUNT", "REGEXP_INSTR",
            "REGEXP_LIKE", "REGEXP_REPLACE", "REGEXP_SUBSTR", "REGEXP_SUBSTR_ALL", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT",
            "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "REGR_VALX", "REGR_VALY", "REPEAT", "REPLACE",
            "REPLICATION_GROUP_REFRESH_HISTORY", "REPLICATION_GROUP_REFRESH_PROGRESS", "REPLICATION_GROUP_REFRESH_PROGRESS_BY_JOB",
            "REPLICATION_GROUP_USAGE_HISTORY", "REPLICATION_USAGE_HISTORY", "REST_EVENT_HISTORY", "RESULT_SCAN", "REVERSE", "RIGHT",
            "RLIKE", "ROUND", "ROW_NUMBER", "RPAD", "RTRIM", "RTRIMMED_LENGTH", "SCHEDULED_TIME", "SEARCH_OPTIMIZATION_HISTORY", "SEQ1",
            "SEQ2", "SEQ4", "SEQ8", "SERVERLESS_TASK_HISTORY", "SHA1", "SHA1_HEX", "SHA1_BINARY", "SHA2", "SHA2_HEX", "SHA2_BINARY", "SIGN",
            "SIN", "SINH", "SKEW", "SOUNDEX", "SOUNDEX_P123", "SPACE", "SPLIT", "SPLIT_PART", "SPLIT_TO_TABLE", "SQRT", "SQUARE", "ST_AREA",
            "ST_ASEWKB", "ST_ASEWKT", "ST_ASGEOJSON", "ST_ASWKB", "ST_ASBINARY", "ST_ASWKT", "ST_ASTEXT", "ST_AZIMUTH", "ST_BUFFER",
            "ST_CENTROID", "ST_COLLECT", "ST_CONTAINS", "ST_COVEREDBY", "ST_COVERS", "ST_DIFFERENCE", "ST_DIMENSION", "ST_DISJOINT",
            "ST_DISTANCE", "ST_DWITHIN", "ST_ENDPOINT", "ST_ENVELOPE", "ST_GEOGFROMGEOHASH", "ST_GEOGPOINTFROMGEOHASH",
            "ST_GEOGRAPHYFROMWKB", "ST_GEOGRAPHYFROMWKT", "ST_GEOHASH", "ST_GEOMETRYFROMWKB", "ST_GEOMETRYFROMWKT", "ST_HAUSDORFFDISTANCE",
            "ST_INTERSECTION", "ST_INTERSECTS", "ST_ISVALID", "ST_LENGTH", "ST_MAKEGEOMPOINT", "ST_GEOM_POINT", "ST_MAKELINE",
            "ST_MAKEPOINT", "ST_POINT", "ST_MAKEPOLYGON", "ST_POLYGON", "ST_MAKEPOLYGONORIENTED", "ST_NPOINTS", "ST_NUMPOINTS",
            "ST_PERIMETER", "ST_POINTN", "ST_SETSRID", "ST_SIMPLIFY", "ST_SRID", "ST_STARTPOINT", "ST_SYMDIFFERENCE", "ST_TRANSFORM",
            "ST_UNION", "ST_WITHIN", "ST_X", "ST_XMAX", "ST_XMIN", "ST_Y", "ST_YMAX", "ST_YMIN",
            "STAGE_DIRECTORY_FILE_REGISTRATION_HISTORY", "STAGE_STORAGE_USAGE_HISTORY", "STARTSWITH", "STDDEV", "STDDEV_POP", "STDDEV_SAMP",
            "STRIP_NULL_VALUE", "STRTOK", "STRTOK_SPLIT_TO_TABLE", "STRTOK_TO_ARRAY", "SUBSTR", "SUBSTRING", "SUM", "SYSDATE",
            "SYSTEM$ABORT_SESSION", "SYSTEM$ABORT_TRANSACTION", "SYSTEM$ADD_EVENT", "SYSTEM$ALLOWLIST", "SYSTEM$ALLOWLIST_PRIVATELINK",
            "SYSTEM$AUTHORIZE_PRIVATELINK", "SYSTEM$AUTHORIZE_STAGE_PRIVATELINK_ACCESS", "SYSTEM$BEHAVIOR_CHANGE_BUNDLE_STATUS",
            "SYSTEM$BLOCK_INTERNAL_STAGES_PUBLIC_ACCESS", "SYSTEM$CANCEL_ALL_QUERIES", "SYSTEM$CANCEL_QUERY", "SYSTEM$CLUSTERING_DEPTH",
            "SYSTEM$CLUSTERING_INFORMATION", "SYSTEM$CREATE_BILLING_EVENT", "SYSTEM$CURRENT_USER_TASK_NAME",
            "SYSTEM$DISABLE_BEHAVIOR_CHANGE_BUNDLE", "SYSTEM$DISABLE_DATABASE_REPLICATION", "SYSTEM$ENABLE_BEHAVIOR_CHANGE_BUNDLE",
            "SYSTEM$ESTIMATE_QUERY_ACCELERATION", "SYSTEM$ESTIMATE_SEARCH_OPTIMIZATION_COSTS", "SYSTEM$EXPLAIN_JSON_TO_TEXT",
            "SYSTEM$EXPLAIN_PLAN_JSON", "SYSTEM$EXTERNAL_TABLE_PIPE_STATUS", "SYSTEM$GENERATE_SAML_CSR",
            "SYSTEM$GENERATE_SCIM_ACCESS_TOKEN", "SYSTEM$GET_AWS_SNS_IAM_POLICY", "SYSTEM$GET_CMK_AKV_CONSENT_URL",
            "SYSTEM$GET_CMK_KMS_KEY_POLICY", "SYSTEM$GET_GCP_KMS_CMK_GRANT_ACCESS_CMD", "SYSTEM$GET_LOGIN_FAILURE_DETAILS",
            "SYSTEM$GET_PREDECESSOR_RETURN_VALUE", "SYSTEM$GET_PRIVATELINK", "SYSTEM$GET_PRIVATELINK_AUTHORIZED_ENDPOINTS",
            "SYSTEM$GET_PRIVATELINK_CONFIG", "SYSTEM$GET_SNOWFLAKE_PLATFORM_INFO", "SYSTEM$GET_TAG", "SYSTEM$GET_TAG_ALLOWED_VALUES",
            "SYSTEM$GET_TAG_ON_CURRENT_COLUMN", "SYSTEM$GET_TAG_ON_CURRENT_TABLE", "SYSTEM$GET_TASK_GRAPH_CONFIG",
            "SYSTEM$GLOBAL_ACCOUNT_SET_PARAMETER", "SYSTEM$INTERNAL_STAGES_PUBLIC_ACCESS_STATUS", "SYSTEM$LAST_CHANGE_COMMIT_TIME",
            "SYSTEM$LINK_ACCOUNT_OBJECTS_BY_NAME", "SYSTEM$LOG", "SYSTEM$MIGRATE_SAML_IDP_REGISTRATION", "SYSTEM$PIPE_FORCE_RESUME",
            "SYSTEM$PIPE_STATUS", "SYSTEM$QUERY_REFERENCE", "SYSTEM$REFERENCE", "SYSTEM$REVOKE_PRIVATELINK",
            "SYSTEM$REVOKE_STAGE_PRIVATELINK_ACCESS", "SYSTEM$SET_RETURN_VALUE", "SYSTEM$SET_SPAN_ATTRIBUTES",
            "SYSTEM$SHOW_OAUTH_CLIENT_SECRETS", "SYSTEM$STREAM_BACKLOG", "SYSTEM$STREAM_GET_TABLE_TIMESTAMP", "SYSTEM$STREAM_HAS_DATA",
            "SYSTEM$TASK_DEPENDENTS_ENABLE", "SYSTEM$TYPEOF", "SYSTEM$UNBLOCK_INTERNAL_STAGES_PUBLIC_ACCESS",
            "SYSTEM$USER_TASK_CANCEL_ONGOING_EXECUTIONS", "SYSTEM$VERIFY_EXTERNAL_OAUTH_TOKEN", "SYSTEM$WAIT", "TAG_REFERENCES",
            "TAG_REFERENCES_ALL_COLUMNS", "TAG_REFERENCES_WITH_LINEAGE", "TAN", "TANH", "TASK_DEPENDENTS", "TASK_HISTORY",
            "TIME_FROM_PARTS", "TIME_SLICE", "TIMEADD", "TIMEDIFF", "TIMESTAMP_FROM_PARTS", "TIMESTAMPADD", "TIMESTAMPDIFF", "TO_ARRAY",
            "TO_BINARY", "TO_BOOLEAN", "TO_CHAR", "TO_VARCHAR", "TO_DATE", "DATE", "TO_DECIMAL", "TO_NUMBER", "TO_NUMERIC", "TO_DOUBLE",
            "TO_GEOGRAPHY", "TO_GEOMETRY", "TO_JSON", "TO_OBJECT", "TO_TIME", "TIME", "TO_TIMESTAMP", "TO_VARIANT", "TO_XML",
            "TOP_INSIGHTS", "TRANSLATE", "TRIM", "TRUNCATE", "TRUNC", "TRY_BASE64_DECODE_BINARY", "TRY_BASE64_DECODE_STRING", "TRY_CAST",
            "TRY_HEX_DECODE_BINARY", "TRY_HEX_DECODE_STRING", "TRY_PARSE_JSON", "TRY_TO_BINARY", "TRY_TO_BOOLEAN", "TRY_TO_DATE",
            "TRY_TO_DECIMAL", "TRY_TO_NUMBER", "TRY_TO_NUMERIC", "TRY_TO_DOUBLE", "TRY_TO_GEOGRAPHY", "TRY_TO_GEOMETRY", "TRY_TO_TIME",
            "TRY_TO_TIMESTAMP", "TYPEOF", "UNICODE", "UNIFORM", "UPPER", "UUID_STRING", "VALIDATE", "VALIDATE_PIPE_LOAD", "VAR_POP",
            "VAR_SAMP", "VARIANCE", "VARIANCE_SAMP", "VARIANCE_POP", "WAREHOUSE_LOAD_HISTORY", "WAREHOUSE_METERING_HISTORY", "WIDTH_BUCKET",
            "XMLGET", "YEAR", "DAY", "WEEK", "MONTH", "QUARTER", "ZEROIFNULL", "ZIPF", "SYSTEM$WAIT", "TAG_REFERENCES",
            "TAG_REFERENCES_ALL_COLUMNS", "TAG_REFERENCES_WITH_LINEAGE", "TAN", "TANH", "TASK_DEPENDENTS", "TASK_HISTORY",
            "TIME_FROM_PARTS", "TIME_SLICE", "TIMEADD", "TIMEDIFF", "TIMESTAMP_FROM_PARTS", "TIMESTAMPADD", "TIMESTAMPDIFF", "TO_ARRAY",
            "TO_BINARY", "TO_BOOLEAN", "TO_CHAR", "TO_VARCHAR", "TO_DATE", "DATE", "TO_DECIMAL", "TO_NUMBER", "TO_NUMERIC", "TO_DOUBLE",
            "TO_GEOGRAPHY", "TO_GEOMETRY", "TO_JSON", "TO_OBJECT", "TO_TIME", "TIME", "TO_TIMESTAMP", "TO_VARIANT", "TO_XML",
            "TOP_INSIGHTS", "TRANSLATE", "TRIM", "TRUNCATE", "TRUNC", "TRY_BASE64_DECODE_BINARY", "TRY_BASE64_DECODE_STRING", "TRY_CAST",
            "TRY_HEX_DECODE_BINARY", "TRY_HEX_DECODE_STRING", "TRY_PARSE_JSON", "TRY_TO_BINARY", "TRY_TO_BOOLEAN", "TRY_TO_DATE",
            "TRY_TO_DECIMAL", "TRY_TO_NUMBER", "TRY_TO_NUMERIC", "TRY_TO_DOUBLE", "TRY_TO_GEOGRAPHY", "TRY_TO_GEOMETRY", "TRY_TO_TIME",
            "TRY_TO_TIMESTAMP", "TYPEOF", "UNICODE", "UNIFORM", "UPPER", "UUID_STRING", "VALIDATE", "VALIDATE_PIPE_LOAD", "VAR_POP",
            "VAR_SAMP", "VARIANCE", "VARIANCE_SAMP", "VARIANCE_POP", "WAREHOUSE_LOAD_HISTORY", "WAREHOUSE_METERING_HISTORY", "WIDTH_BUCKET",
            "XMLGET", "YEAR", "DAY", "WEEK", "MONTH", "QUARTER", "ZEROIFNULL", "ZIPF"})));

    /**
     * Nextval.
     *
     * @param sequence the sequence
     * @return the h 2 next value sequence builder
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect#nextval(java.lang.String)
     */
    @Override
    public SnowflakeNextValueSequenceBuilder nextval(String sequence) {
        return new SnowflakeNextValueSequenceBuilder(this, sequence);
    }

    /**
     * Lastval.
     *
     * @param args the args
     * @return the h 2 last value identity builder
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect#nextval(java.lang.String)
     */
    @Override
    public SnowflakeLastValueIdentityBuilder lastval(String... args) {
        return new SnowflakeLastValueIdentityBuilder(this);
    }

    /**
     * Checks if is synonym supported.
     *
     * @return true, if is synonym supported
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#isSynonymSupported()
     */
    @Override
    public boolean isSynonymSupported() {
        return false;
    }

    /**
     * Gets the functions names.
     *
     * @return the functions names
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#getFunctionsNames()
     */
    @Override
    public Set<String> getFunctionsNames() {
        return FUNCTIONS;
    }

    /**
     * Creates the.
     *
     * @return the h 2 create branching builder
     */
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect#create()
     */
    @Override
    public SnowflakeCreateBranchingBuilder create() {
        return new SnowflakeCreateBranchingBuilder(this);
    }

    /**
     * Exists schema.
     *
     * @param connection the connection
     * @param schema the schema
     * @return true, if successful
     * @throws SQLException the SQL exception
     */
    @Override
    public boolean existsSchema(Connection connection, String schema) throws SQLException {
        String sql = new SelectBuilder(this).column("*")
                                            .schema("INFORMATION_SCHEMA")
                                            .from("SCHEMATA")
                                            .where("SCHEMA_NAME = ?")
                                            .build();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, schema);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return true;
        }
        return false;
    }

}
