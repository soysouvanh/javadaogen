package com.test.generator.util;

import java.util.LinkedHashSet; // Used to store index columns while preserving order.
import java.util.Objects;       // Used for Objects.requireNonNull and Objects.hash if needed (currently only for equals).
import java.util.Set;           // Interface type for columnDbNames.

/**
 * A container class holding nested static classes that represent database schema information.
 * This class itself is not instantiated; it serves only as a namespace for {@link ColumnInfo}
 * and {@link IndexInfo}.
 * Make class final as it's not designed for extension.
 */
public final class InfoHolder {
	/**
	 * Private constructor to prevent instantiation of the container class.
	 */
    private InfoHolder() {
        throw new IllegalStateException("Utility class InfoHolder should not be instantiated.");
    }

    /**
     * Holds information about a single database column.
     * Instances are intended to be immutable after creation.
     */
    public static final class ColumnInfo { // Make class final
        /**
         * The exact name of the column in the database (e.g., "user_id").
         */
        public final String dbName;
        
        /**
         * The corresponding Java field name (e.g., "userId").
         */
        public final String javaName;
        
        /**
         * The fully qualified Java type mapped from the SQL type (e.g., "java.lang.Integer", "java.lang.String").
         */
        public final String javaType;
        
        /**
         * The JDBC SQL type code from {@link java.sql.Types}.
         */
        public final int sqlType;
        
        /**
         * Flag indicating if this column is part of the primary key.
         */
        public final boolean isPrimaryKey;

        /**
         * Constructs a new ColumnInfo instance.
         * Consider adding null checks here if this class were used more broadly outside the generator.
         *
         * @param dbName       The database name of the column (non-null).
         * @param javaName     The generated Java field name (non-null).
         * @param javaType     The mapped Java type name (non-null).
         * @param sqlType      The JDBC type code (from {@code java.sql.Types}).
         * @param isPrimaryKey True if this column is part of the primary key, false otherwise.
         */
        public ColumnInfo(String dbName, String javaName, String javaType, int sqlType, boolean isPrimaryKey) {
            // Preconditions (optional but recommended for robustness if used outside controlled generator)
            // Objects.requireNonNull(dbName, "dbName cannot be null");
            // Objects.requireNonNull(javaName, "javaName cannot be null");
            // Objects.requireNonNull(javaType, "javaType cannot be null");
            this.dbName = dbName;
            this.javaName = javaName;
            this.javaType = javaType;
            this.sqlType = sqlType;
            this.isPrimaryKey = isPrimaryKey;
        }

        /**
         * Returns a string representation of the column information, useful for debugging.
         * Format: "dbName (javaType[, PK])"
         *
         * @return A descriptive string for this column.
         */
        @Override
        public String toString() {
            return dbName + " (" + javaType + (isPrimaryKey ? ", PK" : "") + ")";
        }

        // No hashCode/equals needed currently, as instances are typically held in lists
        // and equality is not checked based on value within the generator for ColumnInfo.
    }

    /**
     * Holds information about a single database index (unique or non-unique).
     * Instances are built in two stages: initial creation with basic info,
     * then columns are added, and finally, {@link #buildWithColumns()} is called
     * to create a new instance with calculated name suffixes. The final built instance
     * is effectively immutable regarding its data content.
     * Make class final as it's not designed for extension.
     */
    public static final class IndexInfo {
        /**
         * The name of the index as defined in the database (e.g., "idx_user_email", "PRIMARY").
         * */
        public final String indexName;
        
        /**
         * True if the index enforces uniqueness, false otherwise.
         * */
        public final boolean isUnique;
        
        /**
         * An ordered set of database column names included in this index.
         * Uses {@link LinkedHashSet} to preserve the order returned by JDBC metadata,
         * which usually corresponds to the key sequence.
         * This field is mutable during the initial discovery phase but final in the built instance.
         */
        public final Set<String> columnDbNames;
        
        /**
         * The calculated suffix for POJO class names based on the index columns (e.g., "UserIdEmail").
         * Calculated by {@link #buildWithColumns()}.
         */
        public final String pojoNameSuffix;
        
        /**
         * The calculated suffix for DAO method names based on the index columns (e.g., "ByUserIdEmail").
         * Calculated by {@link #buildWithColumns()}.
         */
        public final String methodNameSuffix;

        /**
         * Initial constructor used during index discovery.
         * Creates an instance with basic information; column names are added subsequently.
         * Suffixes are initialized as empty strings.
         *
         * @param indexName The name of the index from database metadata (can be null, typically for PRIMARY).
         * @param isUnique True if the index is unique, false otherwise.
         */
        public IndexInfo(String indexName, boolean isUnique) {
            // Handle potential null indexName (common for PRIMARY key index).
            this.indexName = (indexName == null) ? "PRIMARY" : indexName; // Default to "PRIMARY" if null
            this.isUnique = isUnique;
            this.columnDbNames = new LinkedHashSet<>(); // Initialize empty set for adding columns
            
            // Suffixes will be calculated later in buildWithColumns().
            this.pojoNameSuffix = "";
            this.methodNameSuffix = "";
        }

        /**
         * Private constructor used exclusively by {@link #buildWithColumns()} to create the final,
         * effectively immutable instance with calculated suffixes.
         *
         * @param indexName The index name.
         * @param isUnique Uniqueness flag.
         * @param columnDbNames The final, ordered set of column names for this index.
         */
        private IndexInfo(String indexName, boolean isUnique, Set<String> columnDbNames) {
            // Objects.requireNonNull(indexName, "indexName cannot be null in built IndexInfo");
            // Objects.requireNonNull(columnDbNames, "columnDbNames cannot be null in built IndexInfo");
            this.indexName = indexName;
            this.isUnique = isUnique;
            
            // Create a new LinkedHashSet to ensure immutability of the passed set reference and maintain order.
            this.columnDbNames = new LinkedHashSet<>(columnDbNames);
            
            // Calculate suffixes based on the final set of columns.
            this.pojoNameSuffix = Name.generateMethodSuffix(this.columnDbNames);
            this.methodNameSuffix = (isUnique ? "ByUnique" : "ByIndex") + this.pojoNameSuffix;
        }

        /**
         * Creates a new {@code IndexInfo} instance containing the collected column names
         * and the calculated POJO and method name suffixes. This method effectively
         * finalizes the index information after all columns have been added.
         *
         * @return A new {@code IndexInfo} instance with calculated suffixes based on the current {@code columnDbNames}.
         */
        public IndexInfo buildWithColumns() {
            // Creates a new instance using the private constructor, which calculates the suffixes.
            return new IndexInfo(this.indexName, this.isUnique, this.columnDbNames);
        }

        /**
         * Returns a string representation of the index information, useful for debugging.
         * Format: "indexName (Unique|Index): [col1, col2, ...]"
         *
         * @return A descriptive string for this index.
         */
        @Override
        public String toString() {
            return indexName + " (" + (isUnique ? "Unique" : "Index") + "): " + columnDbNames;
        }

        /**
         * Calculates the hash code based *only* on the {@code indexName}.
         * This is specifically designed for grouping {@code IndexInfo} objects by name
         * in a Map during the metadata processing phase in {@code DaoGenerator}.
         * WARNING: This means two {@code IndexInfo} objects with the same name but different
         * columns or uniqueness will have the same hash code.
         *
         * @return The hash code of the {@code indexName}.
         */
        @Override
        public int hashCode() {
            // Base hashCode solely on indexName for grouping purposes in DaoGenerator.
             return Objects.hashCode(indexName);
        }

        /**
         * Compares this {@code IndexInfo} to another object for equality based *only*
         * on the {@code indexName}.
         * Like {@link #hashCode()}, this is tailored for the grouping logic in {@code DaoGenerator}.
         * WARNING: Do not use this equality check if you need to compare indexes based on
         * all their properties (columns, uniqueness).
         *
         * @param obj The object to compare with.
         * @return True if the other object is an {@code IndexInfo} with the same {@code indexName}, false otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            // Standard equality checks.
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            
            // Compare based only on indexName.
            IndexInfo other = (IndexInfo) obj;
            return Objects.equals(indexName, other.indexName); // Use Objects.equals for null safety
        }
    }
}