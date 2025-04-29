package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class CustomerDao {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Properties dbProps; // Pour stocker les propriétés chargées

    // Constructeur pour charger la configuration
    public CustomerDao() {
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
     * Inserts a new record into the customer table.
     * @param data The data object containing values to insert.
     * @throws SQLException if a database access error occurs.
     */
    public void insert(CustomerData data) throws SQLException {
        String sql = "INSERT INTO customer (customerId, customerCode, civilityId, lastName, updated, createdDate, createdTime) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getCustomerid()); // Type: Integer
            pstmt.setObject(2, data.getCustomercode()); // Type: String
            pstmt.setObject(3, data.getCivilityid()); // Type: Integer
            pstmt.setObject(4, data.getLastname()); // Type: String
            pstmt.setObject(5, data.getUpdated()); // Type: java.sql.Timestamp
            pstmt.setObject(6, data.getCreateddate()); // Type: java.sql.Date
            pstmt.setObject(7, data.getCreatedtime()); // Type: java.sql.Time

            pstmt.executeUpdate();
        }
    }    /**
     * Updates an existing record in the customer table.
     * The record is identified by its primary key columns, which are also present in the data object.
     * @param data The data object containing the new values and the primary key.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
     */
    public int update(CustomerData data) throws SQLException {
        final String sql = "UPDATE customer SET customerCode = ?, civilityId = ?, lastName = ?, updated = ?, createdDate = ?, createdTime = ? WHERE customerId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, data.getCustomercode()); // Type: String
            pstmt.setObject(2, data.getCivilityid()); // Type: Integer
            pstmt.setObject(3, data.getLastname()); // Type: String
            pstmt.setObject(4, data.getUpdated()); // Type: java.sql.Timestamp
            pstmt.setObject(5, data.getCreateddate()); // Type: java.sql.Date
            pstmt.setObject(6, data.getCreatedtime()); // Type: java.sql.Time
            pstmt.setObject(7, data.getCustomerid()); // Type: Integer // This block should contain setters for non-PK columns first, then PK columns for WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Deletes a record from the customer table based on its primary key.
     * @param pkData The object containing the primary key values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
     */
    public int delete(CustomerPkData pkData) throws SQLException {
        final String sql = "DELETE FROM customer WHERE customerId = ?";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getCustomerid()); // Type: Integer // This block should contain setters for PK columns for the WHERE clause

            return pstmt.executeUpdate();
        }
    }    /**
     * Retrieves records from the customer table based on the primary key.
     * Since it's a primary key lookup, this will return an array of 0 or 1 element.
     * @param pkData The object containing the primary key values.
     * @return An array containing the matching record, or an empty array if not found.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerData[] get(CustomerPkData pkData) throws SQLException {
        List<CustomerData> results = new ArrayList<>();
        String sql = "SELECT * FROM customer WHERE customerId = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, pkData.getCustomerid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for PK
                    results.add(mapRowToCustomerData(rs));
                }
            }
        }
        return results.toArray(new CustomerData[0]);
    }    /**
     * Retrieves a single record from customer based on the unique index columns: customerCode.
     * This method uses the unique index 'customerCode'.
     * @param uniData The object containing the unique index values.
     * @return The matching data object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerData getByCustomercode(CustomerCustomercodeUniqueData uniData) throws SQLException {
        final String sql = "SELECT * FROM customer WHERE customerCode = ?"; // SELECT * FROM ... WHERE unique_cols = ? ...
        CustomerData result = null;

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			{parameter_setting_block} // Set parameters for unique index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Expecting 0 or 1 for unique index
                    result = mapRowToCustomerData(rs);
                }
            }
        }
        return result;
    }    /**
     * Deletes records from customer based on the unique index columns: customerCode.
     * This method uses the unique index 'customerCode'. Should affect 0 or 1 row.
     * @param uniData The object containing the unique index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected (should be 0 or 1).
     */
    public int deleteByCustomercode(CustomerCustomercodeUniqueData uniData) throws SQLException {
        final String sql = "DELETE FROM customer WHERE customerCode = ?"; // DELETE FROM ... WHERE unique_cols = ? ...
        try (Connection conn = getConnection();
        	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, uniData.getCustomercode()); // Type: String // Set parameters for unique index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer table
     * matching the index columns: customerCode.
     * This method checks based on index 'customerCode'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCustomercode(CustomerCustomercodeUniqueData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer WHERE customerCode = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCustomercode()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from customer based on the index columns: civilityId.
     * This method uses the index 'civilityId'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerData[] getByCivilityid(CustomerCivilityidIdxData idxData) throws SQLException {
        List<CustomerData> results = new ArrayList<>();
        final String sql = "SELECT * FROM customer WHERE civilityId = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCivilityid()); // Type: Integer // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToCustomerData(rs));
                }
            }
        }
        return results.toArray(new CustomerData[0]);
    }    /**
     * Deletes records from customer based on the index columns: civilityId.
     * This method uses the index 'civilityId'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByCivilityid(CustomerCivilityidIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM customer WHERE civilityId = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCivilityid()); // Type: Integer // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer table
     * matching the index columns: civilityId.
     * This method checks based on index 'civilityId'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCivilityid(CustomerCivilityidIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer WHERE civilityId = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCivilityid()); // Type: Integer

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from customer based on the index columns: createdDate.
     * This method uses the index 'createdDate'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerData[] getByCreateddate(CustomerCreateddateIdxData idxData) throws SQLException {
        List<CustomerData> results = new ArrayList<>();
        final String sql = "SELECT * FROM customer WHERE createdDate = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCreateddate()); // Type: java.sql.Date // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToCustomerData(rs));
                }
            }
        }
        return results.toArray(new CustomerData[0]);
    }    /**
     * Deletes records from customer based on the index columns: createdDate.
     * This method uses the index 'createdDate'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByCreateddate(CustomerCreateddateIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM customer WHERE createdDate = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCreateddate()); // Type: java.sql.Date // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer table
     * matching the index columns: createdDate.
     * This method checks based on index 'createdDate'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCreateddate(CustomerCreateddateIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer WHERE createdDate = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCreateddate()); // Type: java.sql.Date

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from customer based on the index columns: createdTime.
     * This method uses the index 'createdTime'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerData[] getByCreatedtime(CustomerCreatedtimeIdxData idxData) throws SQLException {
        List<CustomerData> results = new ArrayList<>();
        final String sql = "SELECT * FROM customer WHERE createdTime = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCreatedtime()); // Type: java.sql.Time // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToCustomerData(rs));
                }
            }
        }
        return results.toArray(new CustomerData[0]);
    }    /**
     * Deletes records from customer based on the index columns: createdTime.
     * This method uses the index 'createdTime'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByCreatedtime(CustomerCreatedtimeIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM customer WHERE createdTime = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCreatedtime()); // Type: java.sql.Time // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer table
     * matching the index columns: createdTime.
     * This method checks based on index 'createdTime'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByCreatedtime(CustomerCreatedtimeIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer WHERE createdTime = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getCreatedtime()); // Type: java.sql.Time

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }    /**
     * Retrieves records from customer based on the index columns: lastName.
     * This method uses the index 'lastName'.
     * @param idxData The object containing the index values.
     * @return An array of matching data objects, potentially empty.
     * @throws SQLException if a database access error occurs.
     */
    public CustomerData[] getByLastname(CustomerLastnameIdxData idxData) throws SQLException {
        List<CustomerData> results = new ArrayList<>();
        final String sql = "SELECT * FROM customer WHERE lastName = ?"; // SELECT * FROM ... WHERE index_cols = ? ...

        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLastname()); // Type: String // Set parameters for index columns

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToCustomerData(rs));
                }
            }
        }
        return results.toArray(new CustomerData[0]);
    }    /**
     * Deletes records from customer based on the index columns: lastName.
     * This method uses the index 'lastName'. Can affect multiple rows.
     * @param idxData The object containing the index values.
     * @throws SQLException if a database access error occurs.
     * @return The number of rows affected.
     */
    public int deleteByLastname(CustomerLastnameIdxData idxData) throws SQLException {
        final String sql = "DELETE FROM customer WHERE lastName = ?"; // DELETE FROM ... WHERE index_cols = ? ...
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLastname()); // Type: String // Set parameters for index columns

            return pstmt.executeUpdate();
        }
    }    /**
     * Checks if at least one record exists in the customer table
     * matching the index columns: lastName.
     * This method checks based on index 'lastName'.
     * @param idxData The object containing the index values.
     * @return true if at least one matching record exists, false otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public boolean existsByByLastname(CustomerLastnameIdxData idxData) throws SQLException {
        // Using SELECT 1 is generally efficient for checking existence
        // We add LIMIT 1 for databases that optimize this better.
        final String sql = "SELECT 1 FROM customer WHERE lastName = ? LIMIT 1";
        try (Connection conn = getConnection();
         	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			            pstmt.setObject(1, idxData.getLastname()); // Type: String

            try (ResultSet rs = pstmt.executeQuery()) {
                // If rs.next() is true, it means the SELECT found at least one row.
                return rs.next();
            }
        }
        // If any SQLException occurs, it will be thrown.
        // If no rows are found, rs.next() returns false, which is correctly returned.
    }
	
	    /**
     * Helper method to map a ResultSet row to a CustomerData object.
     * @param rs The ResultSet positioned at the row to map.
     * @return A populated CustomerData object.
     * @throws SQLException if a database access error occurs during mapping.
     */
    private CustomerData mapRowToCustomerData(ResultSet rs) throws SQLException {
        CustomerData data = new CustomerData();
		        data.setCustomerid(rs.getObject("customerId", Integer.class)); // SQL Type: 4
        data.setCustomercode(rs.getObject("customerCode", String.class)); // SQL Type: 12
        data.setCivilityid(rs.getObject("civilityId", Integer.class)); // SQL Type: -6
        data.setLastname(rs.getObject("lastName", String.class)); // SQL Type: 12
        data.setUpdated(rs.getObject("updated", java.sql.Timestamp.class)); // SQL Type: 93
        data.setCreateddate(rs.getObject("createdDate", java.sql.Date.class)); // SQL Type: 91
        data.setCreatedtime(rs.getObject("createdTime", java.sql.Time.class)); // SQL Type: 92
 // This block contains data.setXxx(rs.getObject(...)); lines
        return data;
    }
}