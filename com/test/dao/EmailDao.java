package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class EmailDao {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Properties dbProps; // Pour stocker les propriétés chargées

    // Constructeur pour charger la configuration
    public EmailDao() {
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
     * Inserts a new record into the email table.
     * @param data The data object containing values to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void insert(EmailData data) throws SQLException {
        String sql = "INSERT INTO email (emailId, email) VALUES (?, ?)";
        try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getEmailid()); // Type: Integer
            pstmt.setObject(2, data.getEmail()); // Type: String

            pstmt.executeUpdate();
        }
    }    /**
     * Updates an existing record in the email table.
     * The record is identified by its primary key columns, which are also present in the data object.
     * @param data The data object containing the new values and the primary key.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
     */
    public int update(EmailData data) throws SQLException {
        final String sql = "UPDATE email SET email = ? WHERE emailId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getEmail()); // Type: String
            pstmt.setObject(2, data.getEmailid()); // Type: Integer // This block should contain setters for non-PK columns first, then PK columns for WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Deletes a record from the email table based on its primary key.
     * @param pkData The object containing the primary key values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
     */
    public int delete(EmailPkData pkData) throws SQLException {
        final String sql = "DELETE FROM email WHERE emailId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getEmailid()); // Type: Integer // This block should contain setters for PK columns for the WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Retrieves records from the email table based on the primary key.
     * Since it's a primary key lookup, this will return an array of 0 or 1 element.
     * @param pkData The object containing the primary key values.
     * @return An array containing the matching record, or an empty array if not found.
     * @throws SQLException if a database access error occurs.
     */
    public EmailData[] get(EmailPkData pkData) throws SQLException {
        List<EmailData> results = new ArrayList<>();
        String sql = "SELECT * FROM email WHERE emailId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getEmailid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for PK
                    results.add(mapRowToEmailData(rs));
                }
            }
        }
        return results.toArray(new EmailData[0]);
    }    /**
     * Retrieves a single record from email based on the unique index columns: email.
     * This method uses the unique index 'email'.
     * @param uniData The object containing the unique index values.
     * @return The matching data object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public EmailData getByEmail(EmailEmailUniqueData uniData) throws SQLException {
        final String sql = "SELECT * FROM email WHERE email = ?"; // SELECT * FROM ... WHERE unique_cols = ? ...
        EmailData result = null;

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			{parameter_setting_block} // Set parameters for unique index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for unique index
                    result = mapRowToEmailData(rs);
                }
            }
        }
        return result;
    }    /**
     * Deletes records from email based on the unique index columns: email.
     * This method uses the unique index 'email'. Should affect 0 or 1 row.
     * @param uniData The object containing the unique index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 0 or 1).
     */
    public int deleteByEmail(EmailEmailUniqueData uniData) throws SQLException {
        final String sql = "DELETE FROM email WHERE email = ?"; // DELETE FROM ... WHERE unique_cols = ? ...
        try (Connection conn = getConnection();
        	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, uniData.getEmail()); // Type: String // Set parameters for unique index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the email table
     * matching the index columns: email.
     * This method checks based on index 'email'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByEmail(EmailEmailUniqueData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM email WHERE email = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getEmail()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }
	
	    /**
     * Helper method to map a ResultSet row to a EmailData object.
     * @param rs The ResultSet positioned at the row to map.
     * @return A populated EmailData object.
     * @throws SQLException if a database access error occurs during mapping.
     */
    private EmailData mapRowToEmailData(ResultSet rs) throws SQLException {
        EmailData data = new EmailData();
		        data.setEmailid(rs.getObject("emailId", Integer.class)); // SQL Type: 4
        data.setEmail(rs.getObject("email", String.class)); // SQL Type: 12
 // This block contains data.setXxx(rs.getObject(...)); lines
        return data;
    }
}