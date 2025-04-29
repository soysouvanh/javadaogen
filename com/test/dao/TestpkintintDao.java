package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class TestpkintintDao {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Properties dbProps; // Pour stocker les propriétés chargées

    // Constructeur pour charger la configuration
    public TestpkintintDao() {
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
     * Inserts a new record into the testpkintint table.
     * @param data The data object containing values to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void insert(TestpkintintData data) throws SQLException {
        String sql = "INSERT INTO testpkintint (pk1int, pk2int, label, language, idx12, idx22) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getPk1int()); // Type: Integer
            pstmt.setObject(2, data.getPk2int()); // Type: Integer
            pstmt.setObject(3, data.getLabel()); // Type: String
            pstmt.setObject(4, data.getLanguage()); // Type: String
            pstmt.setObject(5, data.getIdx12()); // Type: Integer
            pstmt.setObject(6, data.getIdx22()); // Type: Integer

            pstmt.executeUpdate();
        }
    }    /**
     * Updates an existing record in the testpkintint table.
     * The record is identified by its primary key columns, which are also present in the data object.
     * @param data The data object containing the new values and the primary key.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
     */
    public int update(TestpkintintData data) throws SQLException {
        final String sql = "UPDATE testpkintint SET label = ?, language = ?, idx12 = ?, idx22 = ? WHERE pk1int = ? AND pk2int = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getLabel()); // Type: String
            pstmt.setObject(2, data.getLanguage()); // Type: String
            pstmt.setObject(3, data.getIdx12()); // Type: Integer
            pstmt.setObject(4, data.getIdx22()); // Type: Integer
            pstmt.setObject(5, data.getPk1int()); // Type: Integer
            pstmt.setObject(6, data.getPk2int()); // Type: Integer // This block should contain setters for non-PK columns first, then PK columns for WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Deletes a record from the testpkintint table based on its primary key.
     * @param pkData The object containing the primary key values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
     */
    public int delete(TestpkintintPkData pkData) throws SQLException {
        final String sql = "DELETE FROM testpkintint WHERE pk1int = ? AND pk2int = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getPk1int()); // Type: Integer
            pstmt.setObject(2, pkData.getPk2int()); // Type: Integer // This block should contain setters for PK columns for the WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Retrieves records from the testpkintint table based on the primary key.
     * Since it's a primary key lookup, this will return an array of 0 or 1 element.
     * @param pkData The object containing the primary key values.
     * @return An array containing the matching record, or an empty array if not found.
     * @throws SQLException if a database access error occurs.
     */
    public TestpkintintData[] get(TestpkintintPkData pkData) throws SQLException {
        List<TestpkintintData> results = new ArrayList<>();
        String sql = "SELECT * FROM testpkintint WHERE pk1int = ? AND pk2int = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getPk1int()); // Type: Integer
            pstmt.setObject(2, pkData.getPk2int()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for PK
                    results.add(mapRowToTestpkintintData(rs));
                }
            }
        }
        return results.toArray(new TestpkintintData[0]);
    }    /**
     * Retrieves a single record from testpkintint based on the unique index columns: label, language.
     * This method uses the unique index 'uniqueLabelLanguage'.
     * @param uniData The object containing the unique index values.
     * @return The matching data object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public TestpkintintData getByLabelLanguage(TestpkintintLabelLanguageUniqueData uniData) throws SQLException {
        final String sql = "SELECT * FROM testpkintint WHERE label = ? AND language = ?"; // SELECT * FROM ... WHERE unique_cols = ? ...
        TestpkintintData result = null;

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			{parameter_setting_block} // Set parameters for unique index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for unique index
                    result = mapRowToTestpkintintData(rs);
                }
            }
        }
        return result;
    }    /**
     * Deletes records from testpkintint based on the unique index columns: label, language.
     * This method uses the unique index 'uniqueLabelLanguage'. Should affect 0 or 1 row.
     * @param uniData The object containing the unique index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 0 or 1).
     */
    public int deleteByLabelLanguage(TestpkintintLabelLanguageUniqueData uniData) throws SQLException {
        final String sql = "DELETE FROM testpkintint WHERE label = ? AND language = ?"; // DELETE FROM ... WHERE unique_cols = ? ...
        try (Connection conn = getConnection();
        	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, uniData.getLabel()); // Type: String
            pstmt.setObject(2, uniData.getLanguage()); // Type: String // Set parameters for unique index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the testpkintint table
     * matching the index columns: label, language.
     * This method checks based on index 'uniqueLabelLanguage'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByLabelLanguage(TestpkintintLabelLanguageUniqueData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM testpkintint WHERE label = ? AND language = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLabel()); // Type: String
            pstmt.setObject(2, idxData.getLanguage()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from testpkintint based on the index columns: idx12, idx22.
     * This method uses the index 'IndexIdx99'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public TestpkintintData[] getByIdx12Idx22(TestpkintintIdx12Idx22IdxData idxData) throws SQLException {
        List<TestpkintintData> results = new ArrayList<>();
        final String sql = "SELECT * FROM testpkintint WHERE idx12 = ? AND idx22 = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getIdx12()); // Type: Integer
            pstmt.setObject(2, idxData.getIdx22()); // Type: Integer // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToTestpkintintData(rs));
                }
            }
        }
        return results.toArray(new TestpkintintData[0]);
    }    /**
     * Deletes records from testpkintint based on the index columns: idx12, idx22.
     * This method uses the index 'IndexIdx99'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByIdx12Idx22(TestpkintintIdx12Idx22IdxData idxData) throws SQLException {
        final String sql = "DELETE FROM testpkintint WHERE idx12 = ? AND idx22 = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getIdx12()); // Type: Integer
            pstmt.setObject(2, idxData.getIdx22()); // Type: Integer // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the testpkintint table
     * matching the index columns: idx12, idx22.
     * This method checks based on index 'IndexIdx99'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByIdx12Idx22(TestpkintintIdx12Idx22IdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM testpkintint WHERE idx12 = ? AND idx22 = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getIdx12()); // Type: Integer
            pstmt.setObject(2, idxData.getIdx22()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }
	
	    /**
     * Helper method to map a ResultSet row to a TestpkintintData object.
     * @param rs The ResultSet positioned at the row to map.
     * @return A populated TestpkintintData object.
     * @throws SQLException if a database access error occurs during mapping.
     */
    private TestpkintintData mapRowToTestpkintintData(ResultSet rs) throws SQLException {
        TestpkintintData data = new TestpkintintData();
		        data.setPk1int(rs.getObject("pk1int", Integer.class)); // SQL Type: 4
        data.setPk2int(rs.getObject("pk2int", Integer.class)); // SQL Type: 4
        data.setLabel(rs.getObject("label", String.class)); // SQL Type: 12
        data.setLanguage(rs.getObject("language", String.class)); // SQL Type: 1
        data.setIdx12(rs.getObject("idx12", Integer.class)); // SQL Type: 4
        data.setIdx22(rs.getObject("idx22", Integer.class)); // SQL Type: 4
 // This block contains data.setXxx(rs.getObject(...)); lines
        return data;
    }
}