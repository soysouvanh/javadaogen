package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class TestDao {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Properties dbProps; // Pour stocker les propriétés chargées

    // Constructeur pour charger la configuration
    public TestDao() {
        loadConfig();
    }

    private void loadConfig() {
        dbProps = new Properties();
        // Cherche config.properties dans le classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.err.println("ERREUR: Impossible de trouver database.properties dans le classpath.");
                // @todo Gérer l'erreur
                return;
            }
            dbProps.load(input);
            this.dbUrl = dbProps.getProperty("db.url");
            this.dbUser = dbProps.getProperty("db.username");
            this.dbPassword = dbProps.getProperty("db.password");

            if (this.dbUrl == null || this.dbUser == null || this.dbPassword == null) {
                 System.err.println("ERREUR: Les propriétés db.url, db.username, ou db.password sont manquantes dans database.properties.");
                 // @todo Gérer l'erreur
            }

        } catch (IOException ex) {
            System.err.println("ERREUR: Impossible de charger config.properties.");
            ex.printStackTrace();
             // @todo Gérer l'erreur
        }
    }


    private Connection getConnection() throws SQLException {
        if (this.dbUrl == null) {
             throw new SQLException("La configuration de la base de données n'a pas été chargée correctement.");
        }
        // Utilise les variables membres chargées depuis le fichier
        return DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword);
    }

	    /**
     * Inserts a new record into the test table.
     * @param data The data object containing values to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void insert(TestData data) throws SQLException {
        String sql = "INSERT INTO test (testId, language, label, active) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getTestid()); // Type: Integer
            pstmt.setObject(2, data.getLanguage()); // Type: String
            pstmt.setObject(3, data.getLabel()); // Type: String
            pstmt.setObject(4, data.getActive()); // Type: Integer

            pstmt.executeUpdate();
        }
    }    /**
     * Updates an existing record in the test table.
     * The record is identified by its primary key columns, which are also present in the data object.
     * @param data The data object containing the new values and the primary key.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
     */
    public int update(TestData data) throws SQLException {
        final String sql = "UPDATE test SET language = ?, label = ?, active = ? WHERE testId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getLanguage()); // Type: String
            pstmt.setObject(2, data.getLabel()); // Type: String
            pstmt.setObject(3, data.getActive()); // Type: Integer
            pstmt.setObject(4, data.getTestid()); // Type: Integer // This block should contain setters for non-PK columns first, then PK columns for WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Deletes a record from the test table based on its primary key.
     * @param pkData The object containing the primary key values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
     */
    public int delete(TestPkData pkData) throws SQLException {
        final String sql = "DELETE FROM test WHERE testId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getTestid()); // Type: Integer // This block should contain setters for PK columns for the WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Retrieves records from the test table based on the primary key.
     * Since it's a primary key lookup, this will return an array of 0 or 1 element.
     * @param pkData The object containing the primary key values.
     * @return An array containing the matching record, or an empty array if not found.
     * @throws SQLException if a database access error occurs.
     */
    public TestData[] get(TestPkData pkData) throws SQLException {
        List<TestData> results = new ArrayList<>();
        String sql = "SELECT * FROM test WHERE testId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getTestid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for PK
                    results.add(mapRowToTestData(rs));
                }
            }
        }
        return results.toArray(new TestData[0]);
    }    /**
     * Retrieves a single record from test based on the unique index columns: language, label.
     * This method uses the unique index 'languageLabel'.
     * @param uniData The object containing the unique index values.
     * @return The matching data object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public TestData getByLanguageLabel(TestLanguageLabelUniqueData uniData) throws SQLException {
        final String sql = "SELECT * FROM test WHERE language = ? AND label = ?"; // SELECT * FROM ... WHERE unique_cols = ? ...
        TestData result = null;

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			{parameter_setting_block} // Set parameters for unique index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for unique index
                    result = mapRowToTestData(rs);
                }
            }
        }
        return result;
    }    /**
     * Deletes records from test based on the unique index columns: language, label.
     * This method uses the unique index 'languageLabel'. Should affect 0 or 1 row.
     * @param uniData The object containing the unique index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 0 or 1).
     */
    public int deleteByLanguageLabel(TestLanguageLabelUniqueData uniData) throws SQLException {
        final String sql = "DELETE FROM test WHERE language = ? AND label = ?"; // DELETE FROM ... WHERE unique_cols = ? ...
        try (Connection conn = getConnection();
        	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, uniData.getLanguage()); // Type: String
            pstmt.setObject(2, uniData.getLabel()); // Type: String // Set parameters for unique index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the test table
     * matching the index columns: language, label.
     * This method checks based on index 'languageLabel'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByLanguageLabel(TestLanguageLabelUniqueData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM test WHERE language = ? AND label = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLanguage()); // Type: String
            pstmt.setObject(2, idxData.getLabel()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from test based on the index columns: active.
     * This method uses the index 'active'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public TestData[] getByActive(TestActiveIdxData idxData) throws SQLException {
        List<TestData> results = new ArrayList<>();
        final String sql = "SELECT * FROM test WHERE active = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getActive()); // Type: Integer // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToTestData(rs));
                }
            }
        }
        return results.toArray(new TestData[0]);
    }    /**
     * Deletes records from test based on the index columns: active.
     * This method uses the index 'active'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByActive(TestActiveIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM test WHERE active = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getActive()); // Type: Integer // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the test table
     * matching the index columns: active.
     * This method checks based on index 'active'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByActive(TestActiveIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM test WHERE active = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getActive()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from test based on the index columns: label.
     * This method uses the index 'label'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public TestData[] getByLabel(TestLabelIdxData idxData) throws SQLException {
        List<TestData> results = new ArrayList<>();
        final String sql = "SELECT * FROM test WHERE label = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLabel()); // Type: String // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToTestData(rs));
                }
            }
        }
        return results.toArray(new TestData[0]);
    }    /**
     * Deletes records from test based on the index columns: label.
     * This method uses the index 'label'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByLabel(TestLabelIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM test WHERE label = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLabel()); // Type: String // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the test table
     * matching the index columns: label.
     * This method checks based on index 'label'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByLabel(TestLabelIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM test WHERE label = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLabel()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from test based on the index columns: language.
     * This method uses the index 'language'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public TestData[] getByLanguage(TestLanguageIdxData idxData) throws SQLException {
        List<TestData> results = new ArrayList<>();
        final String sql = "SELECT * FROM test WHERE language = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLanguage()); // Type: String // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToTestData(rs));
                }
            }
        }
        return results.toArray(new TestData[0]);
    }    /**
     * Deletes records from test based on the index columns: language.
     * This method uses the index 'language'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByLanguage(TestLanguageIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM test WHERE language = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLanguage()); // Type: String // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the test table
     * matching the index columns: language.
     * This method checks based on index 'language'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByLanguage(TestLanguageIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM test WHERE language = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLanguage()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }
	
	    /**
     * Helper method to map a ResultSet row to a TestData object.
     * @param rs The ResultSet positioned at the row to map.
     * @return A populated TestData object.
     * @throws SQLException if a database access error occurs during mapping.
     */
    private TestData mapRowToTestData(ResultSet rs) throws SQLException {
        TestData data = new TestData();
		        data.setTestid(rs.getObject("testId", Integer.class)); // SQL Type: 4
        data.setLanguage(rs.getObject("language", String.class)); // SQL Type: 1
        data.setLabel(rs.getObject("label", String.class)); // SQL Type: 12
        data.setActive(rs.getObject("active", Integer.class)); // SQL Type: -6
 // This block contains data.setXxx(rs.getObject(...)); lines
        return data;
    }
}