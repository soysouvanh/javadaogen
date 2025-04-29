package com.test.dao;

import com.test.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;


public class CustomerAuthenticationDao {
	private String dbUrl;
	private String dbUser;
	private String dbPassword;
	private Properties dbProps; // Pour stocker les propriétés chargées
	
	// Constructeur pour charger la configuration
	public CustomerAuthenticationDao() {
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
	 * Inserts a new record into the customer_authentication table.
	 * @param data The data object containing values to insert.
	 * @throws SQLException if a database access error occurs.
	 */
	public void insert(CustomerAuthenticationData data) throws SQLException {
		String sql = "INSERT INTO `customer_authentication` (`customerId`, `pseudonym`, `emailId`, `password`, `active`, `updated`) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, data.getCustomerid());
			pstmt.setObject(2, data.getPseudonym());
			pstmt.setObject(3, data.getEmailid());
			pstmt.setObject(4, data.getPassword());
			pstmt.setObject(5, data.getActive());
			pstmt.setObject(6, data.getUpdated());
			
			pstmt.executeUpdate();
		}
	}

	/**
	 * Updates an existing record in the customer_authentication table.
	 * The record is identified by its primary key columns, which are also present in the data object.
	 * @param data The data object containing the new values and the primary key.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
	 */
	public int update(CustomerAuthenticationData data) throws SQLException {
		final String sql = "UPDATE `customer_authentication` SET `pseudonym` = ?, `emailId` = ?, `password` = ?, `active` = ?, `updated` = ? WHERE `customerId` = ?";
		try (Connection conn = getConnection();
		 	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, data.getPseudonym());
			pstmt.setObject(2, data.getEmailid());
			pstmt.setObject(3, data.getPassword());
			pstmt.setObject(4, data.getActive());
			pstmt.setObject(5, data.getUpdated());
			pstmt.setObject(6, data.getCustomerid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Deletes a record from the customer_authentication table based on its primary key.
	 * @param pkData The object containing the primary key values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
	 */
	public int delete(CustomerAuthenticationPkData pkData) throws SQLException {
		final String sql = "DELETE FROM `customer_authentication` WHERE `customerId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getCustomerid());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Retrieves records from the customer_authentication table based on the primary key.
	 * Since it's a primary key lookup, this will return an array of 0 or 1 element.
	 * @param pkData The object containing the primary key values.
	 * @return An array containing the matching record, or an empty array if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerAuthenticationData[] get(CustomerAuthenticationPkData pkData) throws SQLException {
		List<CustomerAuthenticationData> results = new ArrayList<>();
		String sql = "SELECT * FROM `customer_authentication` WHERE `customerId` = ?";
		
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getCustomerid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					results.add(mapRowToCustomerAuthenticationData(rs));
				}
			}
		}
		return results.toArray(new CustomerAuthenticationData[0]);
	}

	/**
	 * Retrieves a single record from customer_authentication based on the unique index columns: emailId.
	 * This method uses the unique index 'emailId'.
	 * @param uniqueData The object containing the unique index values.
	 * @return The matching data object, or null if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerAuthenticationData getByUniqueEmailid(CustomerAuthenticationUniqueEmailidData uniqueData) throws SQLException {
		final String sql = "SELECT * FROM `customer_authentication` WHERE `emailId` = ?";
		CustomerAuthenticationData result = null;
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getEmailid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					result = mapRowToCustomerAuthenticationData(rs);
				}
			}
		}
		return result;
	}

	/**
	 * Deletes records from customer_authentication based on the unique index columns: emailId.
	 * This method uses the unique index 'emailId'. Should affect 0 or 1 row.
	 * @param uniqueData The object containing the unique index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 0 or 1).
	 */
	public int deleteByUniqueEmailid(CustomerAuthenticationUniqueEmailidData uniqueData) throws SQLException {
		final String sql = "DELETE FROM `customer_authentication` WHERE `emailId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getEmailid());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer_authentication table
	 * matching the index columns: emailId.
	 * This method checks based on index 'emailId'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByUniqueEmailid(CustomerAuthenticationUniqueEmailidData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer_authentication WHERE `emailId` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getEmailid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves a single record from customer_authentication based on the unique index columns: pseudonym.
	 * This method uses the unique index 'pseudonym'.
	 * @param uniqueData The object containing the unique index values.
	 * @return The matching data object, or null if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerAuthenticationData getByUniquePseudonym(CustomerAuthenticationUniquePseudonymData uniqueData) throws SQLException {
		final String sql = "SELECT * FROM `customer_authentication` WHERE `pseudonym` = ?";
		CustomerAuthenticationData result = null;
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getPseudonym());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					result = mapRowToCustomerAuthenticationData(rs);
				}
			}
		}
		return result;
	}

	/**
	 * Deletes records from customer_authentication based on the unique index columns: pseudonym.
	 * This method uses the unique index 'pseudonym'. Should affect 0 or 1 row.
	 * @param uniqueData The object containing the unique index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 0 or 1).
	 */
	public int deleteByUniquePseudonym(CustomerAuthenticationUniquePseudonymData uniqueData) throws SQLException {
		final String sql = "DELETE FROM `customer_authentication` WHERE `pseudonym` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getPseudonym());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer_authentication table
	 * matching the index columns: pseudonym.
	 * This method checks based on index 'pseudonym'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByUniquePseudonym(CustomerAuthenticationUniquePseudonymData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer_authentication WHERE `pseudonym` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getPseudonym());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from customer_authentication based on the index columns: active.
	 * This method uses the index 'active'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerAuthenticationData[] getByIndexActive(CustomerAuthenticationIndexActiveData indexData) throws SQLException {
		List<CustomerAuthenticationData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `customer_authentication` WHERE `active` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getActive());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToCustomerAuthenticationData(rs));
				}
			}
		}
		
		return results.toArray(new CustomerAuthenticationData[0]);
	}

	/**
	 * Deletes records from customer_authentication based on the index columns: active.
	 * This method uses the index 'active'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexActive(CustomerAuthenticationIndexActiveData indexData) throws SQLException {
		final String sql = "DELETE FROM `customer_authentication` WHERE `active` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getActive());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer_authentication table
	 * matching the index columns: active.
	 * This method checks based on index 'active'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexActive(CustomerAuthenticationIndexActiveData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer_authentication WHERE `active` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getActive());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Helper method to map a ResultSet row to a CustomerAuthenticationData object.
	 * @param rs The ResultSet positioned at the row to map.
	 * @return A populated CustomerAuthenticationData object.
	 * @throws SQLException if a database access error occurs during mapping.
	 */
	private CustomerAuthenticationData mapRowToCustomerAuthenticationData(ResultSet rs) throws SQLException {
		CustomerAuthenticationData data = new CustomerAuthenticationData();
		data.setCustomerid(rs.getObject("customerId", Integer.class));
		data.setPseudonym(rs.getObject("pseudonym", String.class));
		data.setEmailid(rs.getObject("emailId", Integer.class));
		data.setPassword(rs.getObject("password", String.class));
		data.setActive(rs.getObject("active", Integer.class));
		data.setUpdated(rs.getObject("updated", java.sql.Timestamp.class));

		return data;
	}
}