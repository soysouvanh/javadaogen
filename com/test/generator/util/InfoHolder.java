package com.test.generator.util;

//import java.util.ArrayList;
//import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet; // Pour garder l'ordre d'insertion

public class InfoHolder {
	public static class ColumnInfo {
		public final String dbName;
		public final String javaName;
		public final String javaType;
		public final int sqlType;
		public final boolean isPrimaryKey;

		public ColumnInfo(String dbName, String javaName, String javaType, int sqlType, boolean isPrimaryKey) {
			this.dbName = dbName;
			this.javaName = javaName;
			this.javaType = javaType;
			this.sqlType = sqlType;
			this.isPrimaryKey = isPrimaryKey;
		}

		@Override
		public String toString() {
			return dbName + " (" + javaType + (isPrimaryKey ? ", PK" : "") + ")";
		}
	}

	public static class IndexInfo {
		public final String indexName;
		public final boolean isUnique;
		// Utilise LinkedHashSet pour garder l'ordre des colonnes tel que retourné par
		// getIndexInfo
		public final Set<String> columnDbNames = new LinkedHashSet<>();
		public final String pojoNameSuffix; // Suffixe pour le nom du POJO (ex: UserIdEmail)
		public final String methodNameSuffix; // Suffixe pour le nom de la méthode (ex: ByUserIdEmail)

		public IndexInfo(String indexName, boolean isUnique) {
			this.indexName = indexName == null ? "" : indexName; // PRIMARY est souvent null
			this.isUnique = isUnique;
			// Sera défini après ajout des colonnes
			this.pojoNameSuffix = "";
			this.methodNameSuffix = "";
		}

		// Constructeur utilisé pour la mise à jour une fois les colonnes connues
		private IndexInfo(String indexName, boolean isUnique, Set<String> columnDbNames) {
			this.indexName = indexName;
			this.isUnique = isUnique;
			this.columnDbNames.addAll(columnDbNames);
			this.pojoNameSuffix = NameUtils.generateMethodSuffix(this.columnDbNames);
			this.methodNameSuffix = "By" + this.pojoNameSuffix;
		}

		public IndexInfo buildWithColumns() {
			return new IndexInfo(this.indexName, this.isUnique, this.columnDbNames);
		}

		@Override
		public String toString() {
			return indexName + " (" + (isUnique ? "Unique" : "Index") + "): " + columnDbNames;
		}

		// Important pour utiliser comme clé dans une Map si on veut regrouper par nom
		// d'index
		@Override
		public int hashCode() {
			return indexName.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			IndexInfo other = (IndexInfo) obj;
			return indexName.equals(other.indexName);
		}
	}
}
