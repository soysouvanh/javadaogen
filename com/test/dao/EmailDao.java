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
		String sql = "INSERT INTO `email` (`emailId`, `email`) VALUES (?, ?)";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, data.getEmailid());
			pstmt.setObject(2, data.getEmail());
			
			pstmt.executeUpdate();
		}
	}

	/**
	 * Updates an existing record in the email table.
	 * The record is identified by its primary key columns, which are also present in the data object.
	 * @param data The data object containing the new values and the primary key.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
	 */
	public int update(EmailData data) throws SQLException {
		final String sql = "UPDATE `email` SET `email` = ? WHERE `emailId` = ?";
		try (Connection conn = getConnection();
		 	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, data.getEmail());
			pstmt.setObject(2, data.getEmailid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Deletes a record from the email table based on its primary key.
	 * @param pkData The object containing the primary key values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
	 */
	public int delete(EmailPkData pkData) throws SQLException {
		final String sql = "DELETE FROM `email` WHERE `emailId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getEmailid());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Retrieves records from the email table based on the primary key.
	 * Since it's a primary key lookup, this will return an array of 0 or 1 element.
	 * @param pkData The object containing the primary key values.
	 * @return An array containing the matching record, or an empty array if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public EmailData[] get(EmailPkData pkData) throws SQLException {
		List<EmailData> results = new ArrayList<>();
		String sql = "SELECT * FROM `email` WHERE `emailId` = ?";
		
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getEmailid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					results.add(mapRowToEmailData(rs));
				}
			}
		}
		return results.toArray(new EmailData[0]);
	}

	/**
	 * Retrieves a single record from email based on the unique index columns: email.
	 * This method uses the unique index 'email'.
	 * @param uniqueData The object containing the unique index values.
	 * @return The matching data object, or null if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public EmailData getByUniqueEmail(EmailUniqueEmailData uniqueData) throws SQLException {
		final String sql = "SELECT * FROM `email` WHERE `email` = ?";
		EmailData result = null;
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
{parameter_setting_block}
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					result = mapRowToEmailData(rs);
				}
			}
		}
		return result;
	}

	/**
	 * Deletes records from email based on the unique index columns: email.
	 * This method uses the unique index 'email'. Should affect 0 or 1 row.
	 * @param uniqueData The object containing the unique index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 0 or 1).
	 */
	public int deleteByUniqueEmail(EmailUniqueEmailData uniqueData) throws SQLException {
		final String sql = "DELETE FROM `email` WHERE `email` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getEmail());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the email table
	 * matching the index columns: email.
	 * This method checks based on index 'email'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByUniqueEmail(EmailUniqueEmailData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM email WHERE `email` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getEmail());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Helper method to map a ResultSet row to a EmailData object.
	 * @param rs The ResultSet positioned at the row to map.
	 * @return A populated EmailData object.
	 * @throws SQLException if a database access error occurs during mapping.
	 */
	private EmailData mapRowToEmailData(ResultSet rs) throws SQLException {
		EmailData data = new EmailData();
		data.setEmailid(rs.getObject("emailId", Integer.class));
		data.setEmail(rs.getObject("email", String.class));

		return data;
	}
}