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
		String sql = "INSERT INTO `test` (`testId`, `language`, `label`, `active`) VALUES (?, ?, ?, ?)";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, data.getTestid());
			pstmt.setObject(2, data.getLanguage());
			pstmt.setObject(3, data.getLabel());
			pstmt.setObject(4, data.getActive());
			
			pstmt.executeUpdate();
		}
	}

	/**
	 * Updates an existing record in the test table.
	 * The record is identified by its primary key columns, which are also present in the data object.
	 * @param data The data object containing the new values and the primary key.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the update was successful and the PK exists).
	 */
	public int update(TestData data) throws SQLException {
		final String sql = "UPDATE `test` SET `language` = ?, `label` = ?, `active` = ? WHERE `testId` = ?";
		try (Connection conn = getConnection();
		 	PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, data.getLanguage());
			pstmt.setObject(2, data.getLabel());
			pstmt.setObject(3, data.getActive());
			pstmt.setObject(4, data.getTestid());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Deletes a record from the test table based on its primary key.
	 * @param pkData The object containing the primary key values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 1 if the delete was successful and the PK exists).
	 */
	public int delete(TestPkData pkData) throws SQLException {
		final String sql = "DELETE FROM `test` WHERE `testId` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getTestid());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Retrieves records from the test table based on the primary key.
	 * Since it's a primary key lookup, this will return an array of 0 or 1 element.
	 * @param pkData The object containing the primary key values.
	 * @return An array containing the matching record, or an empty array if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public TestData[] get(TestPkData pkData) throws SQLException {
		List<TestData> results = new ArrayList<>();
		String sql = "SELECT * FROM `test` WHERE `testId` = ?";
		
		try (Connection conn = getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, pkData.getTestid());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					results.add(mapRowToTestData(rs));
				}
			}
		}
		return results.toArray(new TestData[0]);
	}

	/**
	 * Retrieves a single record from test based on the unique index columns: language, label.
	 * This method uses the unique index 'languageLabel'.
	 * @param uniqueData The object containing the unique index values.
	 * @return The matching data object, or null if not found.
	 * @throws SQLException if a database access error occurs.
	 */
	public TestData getByUniqueLanguageLabel(TestUniqueLanguageLabelData uniqueData) throws SQLException {
		final String sql = "SELECT * FROM `test` WHERE `language` = ? AND `label` = ?";
		TestData result = null;
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getLanguage());
			pstmt.setObject(2, uniqueData.getLabel());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					result = mapRowToTestData(rs);
				}
			}
		}
		return result;
	}

	/**
	 * Deletes records from test based on the unique index columns: language, label.
	 * This method uses the unique index 'languageLabel'. Should affect 0 or 1 row.
	 * @param uniqueData The object containing the unique index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected (should be 0 or 1).
	 */
	public int deleteByUniqueLanguageLabel(TestUniqueLanguageLabelData uniqueData) throws SQLException {
		final String sql = "DELETE FROM `test` WHERE `language` = ? AND `label` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, uniqueData.getLanguage());
			pstmt.setObject(2, uniqueData.getLabel());
			
			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the test table
	 * matching the index columns: language, label.
	 * This method checks based on index 'languageLabel'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByUniqueLanguageLabel(TestUniqueLanguageLabelData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM test WHERE `language` = ? AND `label` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getLanguage());
			pstmt.setObject(2, indexData.getLabel());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from test based on the index columns: active.
	 * This method uses the index 'active'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public TestData[] getByIndexActive(TestIndexActiveData indexData) throws SQLException {
		List<TestData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `test` WHERE `active` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getActive());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToTestData(rs));
				}
			}
		}
		
		return results.toArray(new TestData[0]);
	}

	/**
	 * Deletes records from test based on the index columns: active.
	 * This method uses the index 'active'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexActive(TestIndexActiveData indexData) throws SQLException {
		final String sql = "DELETE FROM `test` WHERE `active` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getActive());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the test table
	 * matching the index columns: active.
	 * This method checks based on index 'active'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexActive(TestIndexActiveData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM test WHERE `active` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getActive());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from test based on the index columns: label.
	 * This method uses the index 'label'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public TestData[] getByIndexLabel(TestIndexLabelData indexData) throws SQLException {
		List<TestData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `test` WHERE `label` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getLabel());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToTestData(rs));
				}
			}
		}
		
		return results.toArray(new TestData[0]);
	}

	/**
	 * Deletes records from test based on the index columns: label.
	 * This method uses the index 'label'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexLabel(TestIndexLabelData indexData) throws SQLException {
		final String sql = "DELETE FROM `test` WHERE `label` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getLabel());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the test table
	 * matching the index columns: label.
	 * This method checks based on index 'label'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexLabel(TestIndexLabelData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM test WHERE `label` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getLabel());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves records from test based on the index columns: language.
	 * This method uses the index 'language'.
	 * @param indexData The object containing the index values.
	 * @return An array of matching data objects, potentially empty.
	 * @throws SQLException if a database access error occurs.
	 */
	public TestData[] getByIndexLanguage(TestIndexLanguageData indexData) throws SQLException {
		List<TestData> results = new ArrayList<>();
		final String sql = "SELECT * FROM `test` WHERE `language` = ?";
		
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setObject(1, indexData.getLanguage());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapRowToTestData(rs));
				}
			}
		}
		
		return results.toArray(new TestData[0]);
	}

	/**
	 * Deletes records from test based on the index columns: language.
	 * This method uses the index 'language'. Can affect multiple rows.
	 * @param indexData The object containing the index values.
	 * @throws SQLException if a database access error occurs.
	 * @return The number of rows affected.
	 */
	public int deleteByIndexLanguage(TestIndexLanguageData indexData) throws SQLException {
		final String sql = "DELETE FROM `test` WHERE `language` = ?";
		try (Connection conn = getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setObject(1, indexData.getLanguage());

			return pstmt.executeUpdate();
		}
	}

	/**
	 * Checks if at least one record exists in the test table
	 * matching the index columns: language.
	 * This method checks based on index 'language'.
	 * @param indexData The object containing the index values.
	 * @return true if at least one matching record exists, false otherwise.
	 * @throws SQLException if a database access error occurs.
	 */
	public boolean existsByIndexLanguage(TestIndexLanguageData indexData) throws SQLException {
		final String sql = "SELECT 1 FROM test WHERE `language` = ? LIMIT 1";
		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setObject(1, indexData.getLanguage());
			
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/**
	 * Helper method to map a ResultSet row to a TestData object.
	 * @param rs The ResultSet positioned at the row to map.
	 * @return A populated TestData object.
	 * @throws SQLException if a database access error occurs during mapping.
	 */
	private TestData mapRowToTestData(ResultSet rs) throws SQLException {
		TestData data = new TestData();
		data.setTestid(rs.getObject("testId", Integer.class));
		data.setLanguage(rs.getObject("language", String.class));
		data.setLabel(rs.getObject("label", String.class));
		data.setActive(rs.getObject("active", Integer.class));

		return data;
	}
}