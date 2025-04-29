package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class CustomerAddressDao {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Properties dbProps; // Pour stocker les propriétés chargées

    // Constructeur pour charger la configuration
    public CustomerAddressDao() {
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
     * Inserts a new record into the customer_address table.
     * @param data The data object containing values to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void insert(CustomerAddressData data) throws SQLException {
        String sql = "INSERT INTO customer_address (customerId, addressId) VALUES (?, ?)";
        try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getCustomerid()); // Type: Integer
            pstmt.setObject(2, data.getAddressid()); // Type: Integer

            pstmt.executeUpdate();
        }
    }
    // Note: Update, Delete by PK, Get by PK methods not generated because no primary key was found for table 'customer_address'.
    /**
     * Retrieves a single record from customer_address based on the unique index columns: customerId, addressId.
     * This method uses the unique index 'customerId_addressId'.
     * @param uniData The object containing the unique index values.
     * @return The matching data object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerAddressData getByCustomeridAddressid(CustomerAddressCustomeridAddressidUniqueData uniData) throws SQLException {
        final String sql = "SELECT * FROM customer_address WHERE customerId = ? AND addressId = ?"; // SELECT * FROM ... WHERE unique_cols = ? ...
        CustomerAddressData result = null;

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			{parameter_setting_block} // Set parameters for unique index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for unique index
                    result = mapRowToCustomerAddressData(rs);
                }
            }
        }
        return result;
    }    /**
     * Deletes records from customer_address based on the unique index columns: customerId, addressId.
     * This method uses the unique index 'customerId_addressId'. Should affect 0 or 1 row.
     * @param uniData The object containing the unique index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 0 or 1).
     */
    public int deleteByCustomeridAddressid(CustomerAddressCustomeridAddressidUniqueData uniData) throws SQLException {
        final String sql = "DELETE FROM customer_address WHERE customerId = ? AND addressId = ?"; // DELETE FROM ... WHERE unique_cols = ? ...
        try (Connection conn = getConnection();
        	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, uniData.getCustomerid()); // Type: Integer
            pstmt.setObject(2, uniData.getAddressid()); // Type: Integer // Set parameters for unique index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer_address table
     * matching the index columns: customerId, addressId.
     * This method checks based on index 'customerId_addressId'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCustomeridAddressid(CustomerAddressCustomeridAddressidUniqueData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer_address WHERE customerId = ? AND addressId = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCustomerid()); // Type: Integer
            pstmt.setObject(2, idxData.getAddressid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from customer_address based on the index columns: addressId.
     * This method uses the index 'addressId'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerAddressData[] getByAddressid(CustomerAddressAddressidIdxData idxData) throws SQLException {
        List<CustomerAddressData> results = new ArrayList<>();
        final String sql = "SELECT * FROM customer_address WHERE addressId = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getAddressid()); // Type: Integer // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToCustomerAddressData(rs));
                }
            }
        }
        return results.toArray(new CustomerAddressData[0]);
    }    /**
     * Deletes records from customer_address based on the index columns: addressId.
     * This method uses the index 'addressId'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByAddressid(CustomerAddressAddressidIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM customer_address WHERE addressId = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getAddressid()); // Type: Integer // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer_address table
     * matching the index columns: addressId.
     * This method checks based on index 'addressId'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByAddressid(CustomerAddressAddressidIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer_address WHERE addressId = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getAddressid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from customer_address based on the index columns: customerId.
     * This method uses the index 'customerId'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerAddressData[] getByCustomerid(CustomerAddressCustomeridIdxData idxData) throws SQLException {
        List<CustomerAddressData> results = new ArrayList<>();
        final String sql = "SELECT * FROM customer_address WHERE customerId = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCustomerid()); // Type: Integer // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToCustomerAddressData(rs));
                }
            }
        }
        return results.toArray(new CustomerAddressData[0]);
    }    /**
     * Deletes records from customer_address based on the index columns: customerId.
     * This method uses the index 'customerId'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByCustomerid(CustomerAddressCustomeridIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM customer_address WHERE customerId = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCustomerid()); // Type: Integer // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer_address table
     * matching the index columns: customerId.
     * This method checks based on index 'customerId'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCustomerid(CustomerAddressCustomeridIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer_address WHERE customerId = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCustomerid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }
	
	    /**
     * Helper method to map a ResultSet row to a CustomerAddressData object.
     * @param rs The ResultSet positioned at the row to map.
     * @return A populated CustomerAddressData object.
     * @throws SQLException if a database access error occurs during mapping.
     */
    private CustomerAddressData mapRowToCustomerAddressData(ResultSet rs) throws SQLException {
        CustomerAddressData data = new CustomerAddressData();
		        data.setCustomerid(rs.getObject("customerId", Integer.class)); // SQL Type: 4
        data.setAddressid(rs.getObject("addressId", Integer.class)); // SQL Type: 4
 // This block contains data.setXxx(rs.getObject(...)); lines
        return data;
    }
}