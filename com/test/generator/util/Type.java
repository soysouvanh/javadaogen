package com.test.generator.util;

import java.sql.JDBCType;
import java.sql.Types;

/**
 * Provides utility methods for handling JDBC type conversions.
 * Currently includes mapping from JDBC {@link java.sql.Types} constants
 * to their corresponding Java type names as Strings.
 * This class is intended to be used statically.
 * Make class final as it's not designed for extension.
 */
public final class Type {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Type() {
        throw new IllegalStateException("Utility class TypeUtils should not be instantiated.");
    }

    /**
     * Converts a JDBC SQL type constant (from {@link java.sql.Types}) into a
     * suitable fully qualified or simple Java type name represented as a String.
     * <p>
     * This mapping uses Java wrapper types (e.g., {@code Integer}, {@code Long}, {@code Boolean})
     * instead of primitives (e.g., {@code int}, {@code long}, {@code boolean})
     * primarily to better handle potential {@code NULL} values retrieved from the database
     * when using methods like {@code ResultSet.getObject()}.
     * </p>
     * <p>
     * If the provided {@code sqlType} is not recognized in the explicit mappings,
     * a warning is printed to standard error, and the String "Object" is returned
     * as a fallback.
     * </p>
     *
     * @param sqlType An integer constant from {@link java.sql.Types} representing the JDBC type.
     * @return A String representing the corresponding Java type name (e.g., "java.lang.String", "java.lang.Integer", "java.math.BigDecimal").
     *         Returns "Object" for unmapped types.
     */
    public static String toJavaType(int sqlType) {
        // Use a switch statement for efficient mapping based on the integer type code.
        switch (sqlType) {
            // String types
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.SQLXML:
                //return "java.lang.String";
            	return "String";

            // Integer types (using wrapper class)
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                // Often mapped to Integer as well, as 'short' or 'byte' can be less convenient
                // and Integer handles potential nulls gracefully with getObject().
                return "Integer";

            // Long integer type (using wrapper class)
            case Types.BIGINT:
                return "Long";

            // Floating point types (using wrapper classes)
            case Types.FLOAT: // Note: JDBC FLOAT often corresponds to Java double
            case Types.REAL:  // Often mapped to Float in Java
                return "Float";
            
            case Types.DOUBLE:
                return "Double";

            // Exact decimal types
            case Types.DECIMAL:
            case Types.NUMERIC:
                return "java.math.BigDecimal";

            // Boolean types (using wrapper class)
            case Types.BOOLEAN:
            case Types.BIT:
                // BIT type mapping can be ambiguous. Often used for boolean flags (BIT(1)).
                // Mapping to Boolean (wrapper) is a common and safe choice.
                return "Boolean";

            // Date and Time types (using java.sql specific types)
            case Types.DATE:
                return "java.sql.Date";
            
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE: // Map TIME_WITH_TIMEZONE to java.sql.Time for simplicity here
                return "java.sql.Time";
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE: // Map TIMESTAMP_WITH_TIMEZONE to java.sql.Timestamp for simplicity
                return "java.sql.Timestamp";

            // Binary types
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB: // Binary Large Object
                return "byte[]";
        }
        
        // --- Add mappings for other java.sql.Types constants if needed ---
        // e.g., Types.ARRAY, Types.STRUCT, Types.REF, Types.DATALINK, Types.ROWID, Types.DISTINCT, etc.
        // These often require more complex handling than simple type name mapping.
        String typeName;
        try {
            // Try to get the standard JDBC type name for a better warning message (Java 8+)
            typeName = JDBCType.valueOf(sqlType).getName();
        } catch (IllegalArgumentException e) {
            // If the integer value doesn't correspond to a known JDBCType enum constant
            typeName = "Unknown";
        }
        // Log a warning indicating an unhandled type.
        System.err.printf("WARN: Unmapped JDBC SQL Type: %d (%s). Defaulting to 'Object'. Consider adding a mapping in TypeUtils.%n",
                          sqlType, typeName);
        // Fallback to Object for unknown types.
        return "Object";
    }
}