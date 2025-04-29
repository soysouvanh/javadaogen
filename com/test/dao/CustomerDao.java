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
		String sql = "INSERT INTO `customer` (`customerId`, `customerCode`, `civilityId`, `lastName`, `updated`, `createdDate`, `createdTime`) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, data.getCustomerid());
			pstmt.setObject(2, data.getCustomercode());
			pstmt.setObject(3, data.getCivilityid());
			pstmt.setObject(4, data.getLastname());
			pstmt.setObject(5, data.getUpdated());
			pstmt.setObject(6, data.getCreateddate());
			pstmt.setObject(7, data.getCreatedtime());
			
			pstmt.executeUpdate();
		}
	}

	/**
	 * Updates an existing record in the customer table.
	 * The record is identified by its primary key columns, which are also present in the data object.
	 * @param data The data object containing the new values and the primary key.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
	 */
	public int update(CustomerData data) throws SQLException {
		final String sql = "UPDATE `customer` SET `customerCode` = ?, `civilityId` = ?, `lastName` = ?, `updated` = ?, `createdDate` = ?, `createdTime` = ? WHERE `customerId` = ?";
		try (Connection conn = getConnection();
		 	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, data.getCustomercode());
			pstmt.setObject(2, data.getCivilityid());
			pstmt.setObject(3, data.getLastname());
			pstmt.setObject(4, data.getUpdated());
			pstmt.setObject(5, data.getCreateddate());
			pstmt.setObject(6, data.getCreatedtime());
			pstmt.setObject(7, data.getCustomerid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Deletes a record from the customer table based on its primary key.
	 * @param pkData The object containing the primary key values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
	 */
	public int delete(CustomerPkData pkData) throws SQLException {
		final String sql = "DELETE FROM `customer` WHERE `customerId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getCustomerid());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Retrieves records from the customer table based on the primary key.
	 * Since it's a primary key lookup, this will return an array of 0 or 1 element.
	 * @param pkData The object containing the primary key values.
	 * @return An array containing the matching record, or an empty array if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerData[] get(CustomerPkData pkData) throws SQLException {
		List<CustomerData> results = new ArrayList<>();
		String sql = "SELECT * FROM `customer` WHERE `customerId` = ?";
		
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getCustomerid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					results.add(mapRowToCustomerData(rs));
				}
			}
		}
		return results.toArray(new CustomerData[0]);
	}

	/**
	 * Retrieves a single record from customer based on the unique index columns: customerCode.
	 * This method uses the unique index 'customerCode'.
	 * @param uniqueData The object containing the unique index values.
	 * @return The matching data object, or null if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerData getByUniqueCustomercode(CustomerUniqueCustomercodeData uniqueData) throws SQLException {
		final String sql = "SELECT * FROM `customer` WHERE `customerCode` = ?";
		CustomerData result = null;
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getCustomercode());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					result = mapRowToCustomerData(rs);
				}
			}
		}
		return result;
	}

	/**
	 * Deletes records from customer based on the unique index columns: customerCode.
	 * This method uses the unique index 'customerCode'. Should affect 0 or 1 row.
	 * @param uniqueData The object containing the unique index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 0 or 1).
	 */
	public int deleteByUniqueCustomercode(CustomerUniqueCustomercodeData uniqueData) throws SQLException {
		final String sql = "DELETE FROM `customer` WHERE `customerCode` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getCustomercode());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer table
	 * matching the index columns: customerCode.
	 * This method checks based on index 'customerCode'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByUniqueCustomercode(CustomerUniqueCustomercodeData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer WHERE `customerCode` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getCustomercode());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from customer based on the index columns: civilityId.
	 * This method uses the index 'civilityId'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerData[] getByIndexCivilityid(CustomerIndexCivilityidData indexData) throws SQLException {
		List<CustomerData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `customer` WHERE `civilityId` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getCivilityid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToCustomerData(rs));
				}
			}
		}
		
		return results.toArray(new CustomerData[0]);
	}

	/**
	 * Deletes records from customer based on the index columns: civilityId.
	 * This method uses the index 'civilityId'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexCivilityid(CustomerIndexCivilityidData indexData) throws SQLException {
		final String sql = "DELETE FROM `customer` WHERE `civilityId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getCivilityid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer table
	 * matching the index columns: civilityId.
	 * This method checks based on index 'civilityId'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexCivilityid(CustomerIndexCivilityidData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer WHERE `civilityId` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getCivilityid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from customer based on the index columns: createdDate.
	 * This method uses the index 'createdDate'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerData[] getByIndexCreateddate(CustomerIndexCreateddateData indexData) throws SQLException {
		List<CustomerData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `customer` WHERE `createdDate` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getCreateddate());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToCustomerData(rs));
				}
			}
		}
		
		return results.toArray(new CustomerData[0]);
	}

	/**
	 * Deletes records from customer based on the index columns: createdDate.
	 * This method uses the index 'createdDate'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexCreateddate(CustomerIndexCreateddateData indexData) throws SQLException {
		final String sql = "DELETE FROM `customer` WHERE `createdDate` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getCreateddate());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer table
	 * matching the index columns: createdDate.
	 * This method checks based on index 'createdDate'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexCreateddate(CustomerIndexCreateddateData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer WHERE `createdDate` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getCreateddate());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from customer based on the index columns: createdTime.
	 * This method uses the index 'createdTime'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerData[] getByIndexCreatedtime(CustomerIndexCreatedtimeData indexData) throws SQLException {
		List<CustomerData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `customer` WHERE `createdTime` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getCreatedtime());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToCustomerData(rs));
				}
			}
		}
		
		return results.toArray(new CustomerData[0]);
	}

	/**
	 * Deletes records from customer based on the index columns: createdTime.
	 * This method uses the index 'createdTime'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexCreatedtime(CustomerIndexCreatedtimeData indexData) throws SQLException {
		final String sql = "DELETE FROM `customer` WHERE `createdTime` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getCreatedtime());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer table
	 * matching the index columns: createdTime.
	 * This method checks based on index 'createdTime'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexCreatedtime(CustomerIndexCreatedtimeData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer WHERE `createdTime` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getCreatedtime());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from customer based on the index columns: lastName.
	 * This method uses the index 'lastName'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public CustomerData[] getByIndexLastname(CustomerIndexLastnameData indexData) throws SQLException {
		List<CustomerData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `customer` WHERE `lastName` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getLastname());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToCustomerData(rs));
				}
			}
		}
		
		return results.toArray(new CustomerData[0]);
	}

	/**
	 * Deletes records from customer based on the index columns: lastName.
	 * This method uses the index 'lastName'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexLastname(CustomerIndexLastnameData indexData) throws SQLException {
		final String sql = "DELETE FROM `customer` WHERE `lastName` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getLastname());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the customer table
	 * matching the index columns: lastName.
	 * This method checks based on index 'lastName'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexLastname(CustomerIndexLastnameData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM customer WHERE `lastName` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getLastname());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Helper method to map a ResultSet row to a CustomerData object.
	 * @param rs The ResultSet positioned at the row to map.
	 * @return A populated CustomerData object.
	 * @throws SQLException if a database access error occurs during mapping.
	 */
	private CustomerData mapRowToCustomerData(ResultSet rs) throws SQLException {
		CustomerData data = new CustomerData();
		data.setCustomerid(rs.getObject("customerId", Integer.class));
		data.setCustomercode(rs.getObject("customerCode", String.class));
		data.setCivilityid(rs.getObject("civilityId", Integer.class));
		data.setLastname(rs.getObject("lastName", String.class));
		data.setUpdated(rs.getObject("updated", java.sql.Timestamp.class));
		data.setCreateddate(rs.getObject("createdDate", java.sql.Date.class));
		data.setCreatedtime(rs.getObject("createdTime", java.sql.Time.class));

		return data;
	}
}