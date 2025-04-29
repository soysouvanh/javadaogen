package com.test.generator.util;

import java.sql.Types;
//import java.math.BigDecimal;
//import java.sql.Date;
//import java.sql.Time;
//import java.sql.Timestamp;

public class TypeUtils {
	// Convertit un type SQL (de java.sql.Types) en type Java (chaîne)
	public static String toJavaType(int sqlType) {
		switch (sqlType) {
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.LONGVARCHAR:
		case Types.NVARCHAR:
		case Types.NCHAR:
		case Types.LONGNVARCHAR:
		case Types.CLOB:
		case Types.SQLXML: // Traité comme String pour la simplicité
			return "String";
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.TINYINT:
			return "Integer"; // Utiliser Integer pour la nullabilité
		case Types.BIGINT:
			return "Long";
		case Types.FLOAT:
		case Types.REAL:
			return "Float";
		case Types.DOUBLE:
			return "Double";
		case Types.DECIMAL:
		case Types.NUMERIC:
			return "java.math.BigDecimal";
		case Types.BOOLEAN:
		case Types.BIT: // Souvent mappé à boolean pour BIT(1)
			return "Boolean";
		case Types.DATE:
			return "java.sql.Date";
		case Types.TIME:
		case Types.TIME_WITH_TIMEZONE:
			return "java.sql.Time";
		case Types.TIMESTAMP:
		case Types.TIMESTAMP_WITH_TIMEZONE:
			return "java.sql.Timestamp";
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BLOB:
			return "byte[]";
		// Ajoutez d'autres types si nécessaire
		default:
			System.err.println("WARN: Type SQL non mappé : " + sqlType + ". Utilisation de Object.");
			return "Object"; // Type par défaut si inconnu
		}
	}
}
