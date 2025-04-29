package com.test.generator.util;

/**
 * Convertir les noms.
 */
public class NameUtils {
	/**
	 * Convertir un nom de table ou un nom de colonne en nom de classe (PascalCase).
	 * @param name Nom à convertir en nom de classe.
	 * @return String Nom de classe
	 */
    public static String toClassName(String name) {
    	// Case empty name
		if (name == null || name.isEmpty()) {
            return name;
        }
        
        // Convert name to class name
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : name.toLowerCase().toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        
        // Retourn class name
        return sb.toString();
    }

    // Convertit table_name ou column_name en tableName ou columnName (camelCase)
    public static String toFieldName(String dbName) {
        String className = toClassName(dbName);
        if (className == null || className.isEmpty()) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    // Génère un suffixe de méthode basé sur les noms de colonnes
    public static String generateMethodSuffix(Iterable<String> columnNames) {
         StringBuilder suffix = new StringBuilder();
         for(String col : columnNames) {
             suffix.append(toClassName(col));
         }
         return suffix.toString();
    }
}
