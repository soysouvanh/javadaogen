package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class AddressDao {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Properties dbProps; // Pour stocker les propriétés chargées

    // Constructeur pour charger la configuration
    public AddressDao() {
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
     * Inserts a new record into the address table.
     * @param data The data object containing values to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void insert(AddressData data) throws SQLException {
        String sql = "INSERT INTO address (addressId, address, place, zipCode, cityId) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getAddressid()); // Type: Long
            pstmt.setObject(2, data.getAddress()); // Type: String
            pstmt.setObject(3, data.getPlace()); // Type: String
            pstmt.setObject(4, data.getZipcode()); // Type: String
            pstmt.setObject(5, data.getCityid()); // Type: String

            pstmt.executeUpdate();
        }
    }    /**
     * Updates an existing record in the address table.
     * The record is identified by its primary key columns, which are also present in the data object.
     * @param data The data object containing the new values and the primary key.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
     */
    public int update(AddressData data) throws SQLException {
        final String sql = "UPDATE address SET address = ?, place = ?, zipCode = ?, cityId = ? WHERE addressId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getAddress()); // Type: String
            pstmt.setObject(2, data.getPlace()); // Type: String
            pstmt.setObject(3, data.getZipcode()); // Type: String
            pstmt.setObject(4, data.getCityid()); // Type: String
            pstmt.setObject(5, data.getAddressid()); // Type: Long // This block should contain setters for non-PK columns first, then PK columns for WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Deletes a record from the address table based on its primary key.
     * @param pkData The object containing the primary key values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
     */
    public int delete(AddressPkData pkData) throws SQLException {
        final String sql = "DELETE FROM address WHERE addressId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getAddressid()); // Type: Long // This block should contain setters for PK columns for the WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Retrieves records from the address table based on the primary key.
     * Since it's a primary key lookup, this will return an array of 0 or 1 element.
     * @param pkData The object containing the primary key values.
     * @return An array containing the matching record, or an empty array if not found.
     * @throws SQLException if a database access error occurs.
     */
    public AddressData[] get(AddressPkData pkData) throws SQLException {
        List<AddressData> results = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE addressId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getAddressid()); // Type: Long

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for PK
                    results.add(mapRowToAddressData(rs));
                }
            }
        }
        return results.toArray(new AddressData[0]);
    }    /**
     * Retrieves records from address based on the index columns: cityId.
     * This method uses the index 'cityId'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public AddressData[] getByCityid(AddressCityidIdxData idxData) throws SQLException {
        List<AddressData> results = new ArrayList<>();
        final String sql = "SELECT * FROM address WHERE cityId = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCityid()); // Type: String // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToAddressData(rs));
                }
            }
        }
        return results.toArray(new AddressData[0]);
    }    /**
     * Deletes records from address based on the index columns: cityId.
     * This method uses the index 'cityId'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByCityid(AddressCityidIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM address WHERE cityId = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCityid()); // Type: String // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the address table
     * matching the index columns: cityId.
     * This method checks based on index 'cityId'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCityid(AddressCityidIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM address WHERE cityId = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCityid()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from address based on the index columns: zipCode.
     * This method uses the index 'zipCode'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public AddressData[] getByZipcode(AddressZipcodeIdxData idxData) throws SQLException {
        List<AddressData> results = new ArrayList<>();
        final String sql = "SELECT * FROM address WHERE zipCode = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getZipcode()); // Type: String // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToAddressData(rs));
                }
            }
        }
        return results.toArray(new AddressData[0]);
    }    /**
     * Deletes records from address based on the index columns: zipCode.
     * This method uses the index 'zipCode'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByZipcode(AddressZipcodeIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM address WHERE zipCode = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getZipcode()); // Type: String // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the address table
     * matching the index columns: zipCode.
     * This method checks based on index 'zipCode'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByZipcode(AddressZipcodeIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM address WHERE zipCode = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getZipcode()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }
	
	    /**
     * Helper method to map a ResultSet row to a AddressData object.
     * @param rs The ResultSet positioned at the row to map.
     * @return A populated AddressData object.
     * @throws SQLException if a database access error occurs during mapping.
     */
    private AddressData mapRowToAddressData(ResultSet rs) throws SQLException {
        AddressData data = new AddressData();
		        data.setAddressid(rs.getObject("addressId", Long.class)); // SQL Type: -5
        data.setAddress(rs.getObject("address", String.class)); // SQL Type: 12
        data.setPlace(rs.getObject("place", String.class)); // SQL Type: 12
        data.setZipcode(rs.getObject("zipCode", String.class)); // SQL Type: 1
        data.setCityid(rs.getObject("cityId", String.class)); // SQL Type: 1
 // This block contains data.setXxx(rs.getObject(...)); lines
        return data;
    }
}