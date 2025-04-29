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
		String sql = "INSERT INTO `address` (`addressId`, `address`, `place`, `zipCode`, `cityId`) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, data.getAddressid());
			pstmt.setObject(2, data.getAddress());
			pstmt.setObject(3, data.getPlace());
			pstmt.setObject(4, data.getZipcode());
			pstmt.setObject(5, data.getCityid());
			
			pstmt.executeUpdate();
		}
	}

	/**
	 * Updates an existing record in the address table.
	 * The record is identified by its primary key columns, which are also present in the data object.
	 * @param data The data object containing the new values and the primary key.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
	 */
	public int update(AddressData data) throws SQLException {
		final String sql = "UPDATE `address` SET `address` = ?, `place` = ?, `zipCode` = ?, `cityId` = ? WHERE `addressId` = ?";
		try (Connection conn = getConnection();
		 	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, data.getAddress());
			pstmt.setObject(2, data.getPlace());
			pstmt.setObject(3, data.getZipcode());
			pstmt.setObject(4, data.getCityid());
			pstmt.setObject(5, data.getAddressid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Deletes a record from the address table based on its primary key.
	 * @param pkData The object containing the primary key values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
	 */
	public int delete(AddressPkData pkData) throws SQLException {
		final String sql = "DELETE FROM `address` WHERE `addressId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getAddressid());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Retrieves records from the address table based on the primary key.
	 * Since it's a primary key lookup, this will return an array of 0 or 1 element.
	 * @param pkData The object containing the primary key values.
	 * @return An array containing the matching record, or an empty array if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public AddressData[] get(AddressPkData pkData) throws SQLException {
		List<AddressData> results = new ArrayList<>();
		String sql = "SELECT * FROM `address` WHERE `addressId` = ?";
		
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getAddressid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					results.add(mapRowToAddressData(rs));
				}
			}
		}
		return results.toArray(new AddressData[0]);
	}

	/**
	 * Retrieves records from address based on the index columns: cityId.
	 * This method uses the index 'cityId'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public AddressData[] getByIndexCityid(AddressIndexCityidData indexData) throws SQLException {
		List<AddressData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `address` WHERE `cityId` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getCityid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToAddressData(rs));
				}
			}
		}
		
		return results.toArray(new AddressData[0]);
	}

	/**
	 * Deletes records from address based on the index columns: cityId.
	 * This method uses the index 'cityId'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexCityid(AddressIndexCityidData indexData) throws SQLException {
		final String sql = "DELETE FROM `address` WHERE `cityId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getCityid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the address table
	 * matching the index columns: cityId.
	 * This method checks based on index 'cityId'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexCityid(AddressIndexCityidData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM address WHERE `cityId` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getCityid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from address based on the index columns: zipCode.
	 * This method uses the index 'zipCode'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public AddressData[] getByIndexZipcode(AddressIndexZipcodeData indexData) throws SQLException {
		List<AddressData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `address` WHERE `zipCode` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getZipcode());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToAddressData(rs));
				}
			}
		}
		
		return results.toArray(new AddressData[0]);
	}

	/**
	 * Deletes records from address based on the index columns: zipCode.
	 * This method uses the index 'zipCode'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexZipcode(AddressIndexZipcodeData indexData) throws SQLException {
		final String sql = "DELETE FROM `address` WHERE `zipCode` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getZipcode());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the address table
	 * matching the index columns: zipCode.
	 * This method checks based on index 'zipCode'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexZipcode(AddressIndexZipcodeData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM address WHERE `zipCode` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getZipcode());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Helper method to map a ResultSet row to a AddressData object.
	 * @param rs The ResultSet positioned at the row to map.
	 * @return A populated AddressData object.
	 * @throws SQLException if a database access error occurs during mapping.
	 */
	private AddressData mapRowToAddressData(ResultSet rs) throws SQLException {
		AddressData data = new AddressData();
		data.setAddressid(rs.getObject("addressId", Long.class));
		data.setAddress(rs.getObject("address", String.class));
		data.setPlace(rs.getObject("place", String.class));
		data.setZipcode(rs.getObject("zipCode", String.class));
		data.setCityid(rs.getObject("cityId", String.class));

		return data;
	}
}