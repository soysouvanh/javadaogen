package com.test.generator.util;

import java.util.Objects; // For null checks

/**
 * Provides utility methods for converting database naming conventions (like snake_case)
 * to Java naming conventions (PascalCase for classes, camelCase for fields/methods).
 * This class is intended to be used statically.
 * Make class final as it's not designed for extension.
 */
public final class Name {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Name() {
        throw new IllegalStateException("Utility class NameUtils should not be instantiated.");
    }

    /**
     * Converts a database name (assumed to be in snake_case, e.g., "user_profile" or "COLUMN_NAME")
     * into a Java class name convention (PascalCase, e.g., "UserProfile").
     * The conversion process involves:
     * <ol>
     *     <li>Converting the input name to lower case.</li>
     *     <li>Capitalizing the first character.</li>
     *     <li>Capitalizing any character that follows an underscore ('_').</li>
     *     <li>Removing the underscores.</li>
     * </ol>
     * Handles null or empty input strings gracefully by returning them as is.
     *
     * @param dbName The database name (table or column) to convert, typically in snake_case.
     * @return The converted name in PascalCase, or the original input if it was null or empty.
     */
    public static String toClassName(String dbName) {
        // --- Input Validation ---
        // Handle null or empty input: return as is.
        if (dbName == null || dbName.isEmpty()) {
            return dbName;
        }

        // --- Conversion Logic ---
        // Use StringBuilder for efficient string construction.
        StringBuilder result = new StringBuilder();
        
        // Flag to indicate if the next character should be capitalized. Starts true for the first char.
        boolean capitalizeNext = true;

        // Iterate through the characters of the lowercased input string.
        for (char c : dbName.toLowerCase().toCharArray()) {
            if (c == '_') {
                // If an underscore is encountered, set the flag to capitalize the next character.
                // The underscore itself is skipped (not appended).
                capitalizeNext = true;
            } else if (capitalizeNext) {
                // If the flag is set, capitalize the character and append it.
                result.append(Character.toUpperCase(c));
                // Reset the flag.
                capitalizeNext = false;
            } else {
                // Otherwise, append the character as is (it's already lowercase).
                result.append(c);
            }
        }

        // --- Return Result ---
        return result.toString();
    }

    /**
     * Converts a database name (assumed to be in snake_case, e.g., "user_profile" or "COLUMN_NAME")
     * into a Java field or variable name convention (camelCase, e.g., "userProfile").
     * <p>
     * This is achieved by first converting the name to PascalCase using {@link #toClassName(String)}
     * and then converting the first character of the result to lower case.
     * </p>
     * Handles null or empty input strings gracefully.
     *
     * @param dbName The database name (table or column) to convert, typically in snake_case.
     * @return The converted name in camelCase, or the original input if it was null or empty.
     */
    public static String toFieldName(String dbName) {
        // --- Input Validation (handled by toClassName) ---
        // Reuse toClassName to get the PascalCase version.
        String className = toClassName(dbName);

        // --- Conversion Logic ---
        // If the PascalCase result is null or empty, return it directly.
        if (className == null || className.isEmpty()) {
            return className;
        }
        
        // Convert the first character to lowercase and concatenate the rest of the string.
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * Generates a name suffix suitable for methods or POJOs, based on a sequence of database column names.
     * The suffix is created by converting each column name to PascalCase using {@link #toClassName(String)}
     * and concatenating the results.
     * <p>
     * Example: Input ["user_id", "email_address"] results in "UserIdEmailAddress".
     * This is often prefixed with "By" for DAO methods (e.g., "findByUserIdEmailAddress").
     * </p>
     *
     * @param columnNames An iterable collection of database column names (typically snake_case). Must not be null.
     * @return A concatenated string of PascalCase column names, or an empty string if the input iterable is empty or null.
     * @throws NullPointerException if {@code columnNames} is null.
     */
    public static String generateMethodSuffix(Iterable<String> columnNames) {
        // --- Input Validation ---
        Objects.requireNonNull(columnNames, "columnNames iterable cannot be null");

        // --- Conversion Logic ---
        StringBuilder suffix = new StringBuilder();
        
        // Iterate through the provided column names.
        for (String columnName : columnNames) {
            // Convert each column name to PascalCase and append it to the suffix.
            // toClassName handles null/empty column names within the loop gracefully.
            suffix.append(toClassName(columnName));
        }

        // --- Return Result ---
        return suffix.toString();
    }
}