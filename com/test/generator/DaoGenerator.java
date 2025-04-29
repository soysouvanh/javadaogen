package com.test.generator;

import com.test.generator.util.InfoHolder.ColumnInfo;
import com.test.generator.util.InfoHolder.IndexInfo;
import com.test.generator.util.Name;
import com.test.generator.util.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generates Plain Old Java Object (POJO) and Data Access Object (DAO) classes
 * based on a MySQL database schema.
 * <p>
 * It connects to the database using credentials specified in a configuration
 * file, reads table metadata (columns, primary keys, indexes), and generates
 * Java source files using text templates.
 * </p>
 * <p>
 * Configuration is read from {@value #CONFIG_FILE} located in the classpath.
 * Templates are read from the directory specified by {@value #TEMPLATE_DIR} in
 * the classpath. Generated files are written to the directory specified by
 * {@value #OUTPUT_DIR}, organized into packages defined by
 * {@value #POJO_PACKAGE} and {@value #DAO_PACKAGE}.
 * </p>
 *
 * @see #CONFIG_FILE
 * @see #TEMPLATE_DIR
 * @see #OUTPUT_DIR
 * @see #POJO_PACKAGE
 * @see #DAO_PACKAGE
 */
public class DaoGenerator {

	// --- Configuration Constants ---

	/**
	 * The path (relative to the classpath root) of the database configuration file.
	 * This file should contain 'db.url', 'db.username', and 'db.password' properties.
	 * Default: "configuration/database.properties"
	 */
	private static final String CONFIG_FILE = "configuration/database.properties";

	/**
	 * The root output directory for generated source files.
	 * Warning: Setting this to "src" mixes generated code with manual code, which is generally discouraged.
	 * Consider using a separate directory like "generated-sources/java".
	 * Default: "src" (as per user request)
	 */
	private static final String OUTPUT_DIR = "src";

	/**
	 * The target Java package for the generated POJO classes.
	 * Default: "com.test.model"
	 */
	private static final String POJO_PACKAGE = "com.test.model";

	/**
	 * The target Java package for the generated DAO classes.
	 * Default: "com.test.dao"
	 */
	private static final String DAO_PACKAGE = "com.test.dao";

	/**
	 * The output subdirectory path for POJOs, derived from {@link #POJO_PACKAGE}.
	 */
	private static final String POJO_SUBDIR = POJO_PACKAGE.replace('.', '/');

	/**
	 * The output subdirectory path for DAOs, derived from {@link #DAO_PACKAGE}.
	 */
	private static final String DAO_SUBDIR = DAO_PACKAGE.replace('.', '/');

	/**
	 * The resource path (in the classpath) where template files are located.
	 * Must start with '/' for an absolute classpath lookup.
	 * Default: "/resources/templates"
	 */
	private static final String TEMPLATE_DIR = "/resources/templates";

	/**
	 * Compiled regular expression pattern to find placeholders in templates (e.g., ${key}).
	 * This is pre-compiled for slightly better performance in {@link #replacePlaceholders}.
	 */
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

	// --- Instance Members ---

	/**
	 * Database connection URL used by the generator itself to read metadata. Loaded from {@value #CONFIG_FILE}.
	 */
	private String generatorDbUrl;
	
	/**
	 * Database username used by the generator. Loaded from {@value #CONFIG_FILE}.
	 */
	private String generatorDbUser;
	
	/**
	 * Database password used by the generator. Loaded from {@value #CONFIG_FILE}.
	 */
	private String generatorDbPassword;

	/**
	 * The active JDBC connection to the database.
	 */
	private Connection connection;

	/**
	 * Provides access to database metadata (tables, columns, etc.).
	 */
	private DatabaseMetaData metaData;

	/**
	 * Calculated absolute base path for the output directory.
	 */
	private final Path outputBaseDir;

	/**
	 * Calculated absolute path for the POJO output directory.
	 */
	private final Path pojoOutputDir;

	/**
	 * Calculated absolute path for the DAO output directory.
	 */
	private final Path daoOutputDir;

	/**
	 * A simple cache to store template file content, avoiding repeated file reads.
	 * Maps template names (e.g., "pojo_class.template") to their string content.
	 */
	private final Map<String, String> templateCache = new HashMap<>();

	/**
	 * Initializes the generator. Loads database configuration, establishes a JDBC
	 * connection, retrieves database metadata, and calculates output directory paths.
	 *
	 * @throws SQLException if a database access error occurs during connection or metadata retrieval.
	 * @throws IOException  if the configuration file cannot be found or read.
	 */
	public DaoGenerator() throws SQLException, IOException {
		// Load database credentials for the generator.
		loadGeneratorConfig();

		// Calculate and store output paths based on configuration.
		this.outputBaseDir = Paths.get(OUTPUT_DIR);
		this.pojoOutputDir = outputBaseDir.resolve(POJO_SUBDIR);
		this.daoOutputDir = outputBaseDir.resolve(DAO_SUBDIR);

		// Establish the database connection.
		try {
			// Validate required configuration.
			if (this.generatorDbUrl == null || this.generatorDbUrl.trim().isEmpty()) {
				throw new SQLException("Database connection URL ('db.url') is missing or empty in " + CONFIG_FILE);
			}

			// Obtain the connection.
			this.connection = DriverManager.getConnection(this.generatorDbUrl, this.generatorDbUser, this.generatorDbPassword);

			// Get metadata object for schema inspection.
			this.metaData = connection.getMetaData();
			System.out.println("Successfully connected to database: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());

		} catch (SQLException e) {
			// Provide helpful error context if connection fails.
			System.err.println("FATAL: Database connection failed during generator initialization.");
			System.err.println("Attempted URL: " + this.generatorDbUrl + ", User: " + this.generatorDbUser);
			System.err.println("Please check database accessibility and configuration in '" + CONFIG_FILE + "'.");
			throw e; // Re-throw to signal failure.
		}
	}

	/**
	 * Loads database connection properties (URL, user, password) for the generator from the configuration file specified by {@link #CONFIG_FILE}.
	 * The configuration file must be accessible from the classpath.
	 *
	 * @throws IOException if the configuration file cannot be found or read.
	 */
	private void loadGeneratorConfig() throws IOException {
		// Use Properties class for easy loading.
		Properties props = new Properties();

		// Use try-with-resources to ensure the InputStream is closed.
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			// Case configuration not found
			if (input == null) {
				throw new IOException("Configuration file '" + CONFIG_FILE + "' not found in classpath.");
			}

			// Load properties from the input stream.
			props.load(input);

			// Read properties into member variables.
			this.generatorDbUrl = props.getProperty("db.url");
			this.generatorDbUser = props.getProperty("db.username");
			this.generatorDbPassword = props.getProperty("db.password"); // Password can be null or empty.

			// Log warnings for potentially missing configuration (useful for debugging).
			if (this.generatorDbUrl == null || this.generatorDbUrl.trim().isEmpty()) {
				System.err.println("WARN: Property 'db.url' is missing or empty in " + CONFIG_FILE);
				// Could throw an exception here for stricter validation if URL is mandatory.
			}
			if (this.generatorDbUser == null || this.generatorDbUser.trim().isEmpty()) {
				System.err.println("WARN: Property 'db.username' is missing or empty in " + CONFIG_FILE);
			}

			// An empty password might be valid, but null usually isn't for DriverManager.
			if (this.generatorDbPassword == null) {
				System.err.println("WARN: Property 'db.password' is missing in " + CONFIG_FILE + ". Using empty password instead of null.");
				this.generatorDbPassword = "";
			}
		} catch (IOException e) {
			System.err.println("Error loading database configuration for generator from " + CONFIG_FILE);
			throw e; // Re-throw to signal failure.
		}
	}

	/**
	 * Loads the content of a template file from the classpath directory {@link #TEMPLATE_DIR}.
	 * Uses a simple in-memory cache ({@link #templateCache}) to avoid redundant file reads.
	 *
	 * @param templateName The name of the template file (e.g., "pojo_class.template").
	 * @return The content of the template as a String.
	 * @throws IOException if the template file cannot be found or read.
	 */
	private String loadTemplate(String templateName) throws IOException {
		// Check cache first for performance.
		if (templateCache.containsKey(templateName)) {
			return templateCache.get(templateName);
		}

		// Construct the full classpath resource path.
		String path = TEMPLATE_DIR + "/" + templateName;

		// Read the template file content using try-with-resources.
		try (InputStream is = DaoGenerator.class.getResourceAsStream(path)) {
			if (is == null) {
				throw new IOException("Template file not found in classpath: " + path + ". Please ensure it exists and the TEMPLATE_DIR configuration is correct.");
			}

			// Read all bytes and convert to a String using UTF-8 encoding.
			String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

			// Store in cache before returning.
			templateCache.put(templateName, content);
			return content;
		} catch (NullPointerException e) {
			// This might happen if getResourceAsStream is called in an unexpected context.
			throw new IOException("Error locating template resource (invalid path or context?): " + path, e);
		} catch (IOException e) {
			System.err.println("Error reading template file: " + path);
			throw e;
		}
	}

	/**
	 * Replaces placeholders of the form <code>${key}</code> within a template
	 * string with corresponding values from the provided map. Uses the pre-compiled
	 * {@link #PLACEHOLDER_PATTERN}. If a key is not found in the map, the
	 * placeholder is replaced with an empty string to avoid leaving literal
	 * "${...}" in the generated code.
	 *
	 * @param template The template string containing placeholders.
	 * @param values   A Map where keys are placeholder names (without ${}) and
	 *                 values are the replacement strings.
	 * @return The template string with all recognized placeholders replaced.
	 */
	private String replacePlaceholders(String template, Map<String, String> values) {
		// Use Matcher for efficient finding and replacement.
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
		
		// Use StringBuilder for efficient string construction.
		StringBuilder sb = new StringBuilder();

		// Loop through all occurrences of the pattern.
		while (matcher.find()) {
			// Extract the key name from the capturing group.
			String key = matcher.group(1);

			// Get the replacement value from the map, defaulting to empty string if not
			// found.
			String value = values.getOrDefault(key, "");

			// Append the portion of the template before the match, and the escaped replacement value.
			// Matcher.quoteReplacement handles escaping of special characters like '$' and '\' in the value.
			matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
		}

		// Append the rest of the template string after the last match.
		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * The main entry point for the code generation process. It orchestrates the
	 * steps: creating output directories, fetching table names, iterating through
	 * tables to generate POJOs and DAOs, and closing the database connection.
	 *
	 * @throws SQLException if a database error occurs while fetching metadata.
	 * @throws IOException  if an error occurs during file I/O (reading templates or
	 *                      writing generated files).
	 */
	public void generate() throws SQLException, IOException {
		System.out.println("Initializing code generation...");

		// Ensure output directories exist.
		System.out.println("Creating output directories (if they don't exist)...");
		createOutputDirs();

		// Retrieve the list of tables to process.
		System.out.println("Fetching table list from database...");
		List<String> tableNames = getTableNames();
		System.out.println("Found " + tableNames.size() + " table(s): " + tableNames);

		// Check if there are any tables to process.
		if (tableNames.isEmpty()) {
			System.out.println("No tables found in the specified database/schema. Nothing to generate.");

			// Close connection early if nothing to do.
			closeConnection();
			return;
		}

		// Process each table individually.
		int successCount = 0;
		int errorCount = 0;
		for (String tableName : tableNames) {
			System.out.println("\n--- Processing table: '" + tableName + "' ---");
			try {
				// Generate POJOs and DAO for the current table.
				generateForTable(tableName);
				successCount++;
			} catch (Exception e) {
				// Catch exceptions per table to allow the generator to continue with other
				// tables.
				System.err.println("ERROR: Failed to generate code for table '" + tableName + "'. Skipping.");

				// Log the exception stack trace for detailed debugging.
				e.printStackTrace();
				errorCount++;
			}
		}

		// Close the database connection after processing all tables.
		closeConnection();

		// Final summary message.
		System.out.println("\n--- Generation Summary ---");
		System.out.println("Successfully processed: " + successCount + " table(s)");
		System.out.println("Failed to process:    " + errorCount + " table(s)");
		System.out.println("Generated files output to base directory: " + outputBaseDir.toAbsolutePath());
		if (OUTPUT_DIR.equals("src")) {
			System.out.println("WARNING: Files were generated directly into the '" + OUTPUT_DIR + "' directory. Consider using a separate output directory for generated sources.");
		} else {
			System.out.println("Remember to add '" + OUTPUT_DIR + "' as a source folder in your IDE/build tool if needed.");
		}
	}

	/**
	 * Creates the output directories for POJOs and DAOs based on the configuration,
	 * if they do not already exist.
	 *
	 * @throws IOException if an error occurs during directory creation.
	 */
	private void createOutputDirs() throws IOException {
		try {
			// Files.createDirectories creates parent directories as needed and doesn't fail
			// if they already exist.
			Files.createDirectories(pojoOutputDir);
			Files.createDirectories(daoOutputDir);
		} catch (IOException e) {
			// Provide specific paths in the error message.
			System.err.println("Failed to create output directories: " + pojoOutputDir + " or " + daoOutputDir);
			throw e;
		}
	}

	/**
	 * Retrieves a list of all table names from the connected database schema.
	 *
	 * @return A List of table name strings.
	 * @throws SQLException if a database access error occurs.
	 */
	private List<String> getTableNames() throws SQLException {
		List<String> tableNames = new ArrayList<>();
		// Use try-with-resources for automatic ResultSet closing.
		// Arguments: catalog, schemaPattern, tableNamePattern, types (here, only
		// "TABLE" type)
		try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, "%", new String[] { "TABLE" })) {
			while (rs.next()) {
				// Get the table name from the "TABLE_NAME" column.
				tableNames.add(rs.getString("TABLE_NAME"));
			}
		}
		return tableNames;
	}

	/**
	 * Retrieves detailed information about all columns for a specific table. It
	 * determines the Java type mapping, Java field name, and primary key status for
	 * each column.
	 *
	 * @param tableName The name of the table to inspect.
	 * @return A List of {@link ColumnInfo} objects, one for each column in the table.
	 * @throws SQLException if a database access error occurs.
	 */
	private List<ColumnInfo> getColumns(String tableName) throws SQLException {
		List<ColumnInfo> columns = new ArrayList<>();

		// Optimization: Get the set of primary key column names once for this table.
		Set<String> pkColumnNames = getPrimaryKeyColumnNames(tableName);

		// Use try-with-resources for automatic ResultSet closing.
		// Arguments: catalog, schemaPattern, tableNamePattern, columnNamePattern ("%"
		// means all columns)
		try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, "%")) {
			while (rs.next()) {
				// Extract column metadata from the ResultSet.
				String dbName = rs.getString("COLUMN_NAME");
				int sqlType = rs.getInt("DATA_TYPE"); // e.g., java.sql.Types.VARCHAR

				// Map the JDBC SQL type to a Java type string (e.g., "String", "Integer").
				String javaType = Type.toJavaType(sqlType);

				// Convert the database column name (e.g., "user_id") to a Java field name
				// (e.g., "userId").
				String javaName = Name.toFieldName(dbName);

				// Check if this column is part of the primary key.
				boolean isPk = pkColumnNames.contains(dbName);

				// Create and add the ColumnInfo object to the list.
				columns.add(new ColumnInfo(dbName, javaName, javaType, sqlType, isPk));
			}
		}

		// Warn if a table appears to have no columns.
		if (columns.isEmpty()) {
			System.err.println("WARN: No columns found for table '" + tableName + "'. Check table definition and database permissions.");
		}

		return columns;
	}

	/**
	 * Retrieves the names of the columns that constitute the primary key for a
	 * given table. The column names are returned in the order defined by the
	 * primary key constraint (KEY_SEQ).
	 *
	 * @param tableName The name of the table.
	 * @return An ordered Set (LinkedHashSet) containing the names of the primary
	 *         key columns. Returns an empty set if the table has no primary key.
	 * @throws SQLException if a database access error occurs.
	 */
	private Set<String> getPrimaryKeyColumnNames(String tableName) throws SQLException {
		// Use LinkedHashSet to maintain the order of columns as defined by KEY_SEQ.
		Set<String> pkColumnNames = new LinkedHashSet<>();

		// Use try-with-resources for automatic ResultSet closing.
		// Arguments: catalog, schema, table
		try (ResultSet rs = metaData.getPrimaryKeys(connection.getCatalog(), null, tableName)) {
			// Store results temporarily to allow sorting by KEY_SEQ.
			List<Map.Entry<Short, String>> pkList = new ArrayList<>();
			while (rs.next()) {
				// Add entry with sequence number and column name.
				pkList.add(Map.entry(rs.getShort("KEY_SEQ"), rs.getString("COLUMN_NAME")));
			}

			// Sort the list based on the sequence number (KEY_SEQ).
			pkList.sort(Map.Entry.comparingByKey());

			// Add the sorted column names to the LinkedHashSet.
			for (Map.Entry<Short, String> entry : pkList) {
				pkColumnNames.add(entry.getValue());
			}
		}

		return pkColumnNames;
	}

	/**
	 * Retrieves information about all indexes (unique and non-unique) for a given
	 * table. It groups columns by index name and calculates derived information
	 * like POJO/method name suffixes.
	 *
	 * @param tableName The name of the table to inspect.
	 * @return A Map where the key is the index name (String) and the value is the
	 *         corresponding {@link IndexInfo} object. The map preserves the order
	 *         in which indexes were retrieved from the database metadata.
	 * @throws SQLException if a database access error occurs.
	 */
	private Map<String, IndexInfo> getIndexes(String tableName) throws SQLException {
		// Use LinkedHashMap to preserve the order of indexes as returned by the driver.
		Map<String, IndexInfo> indexMap = new LinkedHashMap<>();

		// Use try-with-resources for automatic ResultSet closing.
		// Arguments: catalog, schema, table, unique=false (get all), approximate=true
		// (allow stats if available)
		try (ResultSet rs = metaData.getIndexInfo(connection.getCatalog(), null, tableName, false, true)) {
			while (rs.next()) {
				// Ignore table statistics pseudo-index entries.
				short indexType = rs.getShort("TYPE");
				if (indexType == DatabaseMetaData.tableIndexStatistic) {
					continue;
				}

				// Get index name and column name.
				String indexName = rs.getString("INDEX_NAME");
				String columnName = rs.getString("COLUMN_NAME");

				// Skip if index name or column name is null (shouldn't happen for real
				// indexes).
				// Note: The primary key index is often named "PRIMARY".
				if (indexName == null || columnName == null) {
					continue;
				}

				// Determine if the index is unique.
				boolean nonUnique = rs.getBoolean("NON_UNIQUE");

				// Get or create the IndexInfo object for this index name.
				// computeIfAbsent ensures we create only one IndexInfo per index name.
				IndexInfo indexInfo = indexMap.computeIfAbsent(indexName, k -> new IndexInfo(k, !nonUnique));

				// Add the column name to this index's set of columns.
				// LinkedHashSet preserves the insertion order, which usually corresponds to
				// ORDINAL_POSITION.
				indexInfo.columnDbNames.add(columnName);
			}
		}

		// Post-process: Calculate derived names (method/pojo suffixes) for each index
		// now that all columns for each index have been collected.
		Map<String, IndexInfo> finalIndexes = new LinkedHashMap<>();
		for (Map.Entry<String, IndexInfo> entry : indexMap.entrySet()) {
			// buildWithColumns calculates names like "ByUserIdEmail" based on column names.
			finalIndexes.put(entry.getKey(), entry.getValue().buildWithColumns());
		}

		return finalIndexes;
	}

	/**
	 * Generates all necessary POJO and DAO source files for a single database
	 * table.
	 *
	 * @param tableName The name of the table to process.
	 * @throws SQLException if a database error occurs while fetching metadata for
	 *                      this table.
	 * @throws IOException  if an error occurs during file I/O for this table's
	 *                      generated files.
	 */
	private void generateForTable(String tableName) throws SQLException, IOException {
		// Convert DB table name to Java class name prefix (e.g., "user_profiles" ->
		// "UserProfiles").
		String classNamePrefix = Name.toClassName(tableName);

		// Fetch metadata for this table.
		List<ColumnInfo> columns = getColumns(tableName);

		// Case no columns found, skip generation for this table.
		if (columns.isEmpty()) {
			System.out.println("  Skipping table '" + tableName + "' because no columns were found.");
			return;
		}

		// Filter primary key columns from the full list.
		List<ColumnInfo> primaryKeys = columns.stream().filter(c -> c.isPrimaryKey).collect(Collectors.toList());

		// Get index information.
		Map<String, IndexInfo> indexes = getIndexes(tableName);

		// Log a warning if no primary key is found, as it limits generated DAO
		// functionality.
		if (primaryKeys.isEmpty()) {
			System.err.println("  WARN: No primary key found for table '" + tableName + "'. PK-based get/update/delete methods will not be generated for its DAO.");
		}

		// Generate POJO files.
		System.out.println("  Generating POJOs for '" + tableName + "'...");

		// Generate the main data POJO (contains all columns).
		generatePojoFromTemplate(classNamePrefix + "Data", POJO_PACKAGE, columns, pojoOutputDir);

		// Generate the primary key POJO (if a PK exists).
		if (!primaryKeys.isEmpty()) {
			generatePojoFromTemplate(classNamePrefix + "PkData", POJO_PACKAGE, primaryKeys, pojoOutputDir);
		}

		// Generate POJOs for each index (used as parameters for index-based DAO
		// methods).
		for (IndexInfo index : indexes.values()) {
			// Reconstruct the ordered list of ColumnInfo objects for this specific index.
			List<ColumnInfo> indexColumns = columns.stream().filter(c -> index.columnDbNames.contains(c.dbName))
				// Sort the ColumnInfo objects based on the order stored in index.columnDbNames.
				.sorted(Comparator.comparingInt(c -> new ArrayList<>(index.columnDbNames).indexOf(c.dbName)))
				.collect(Collectors.toList());

			// Skip if, somehow, no columns were resolved for this index.
			if (indexColumns.isEmpty()) {
				System.err.println("  WARN: Skipping index POJO generation for index '" + index.indexName + "' in table '" + tableName + "' because no matching columns were found.");
				continue;
			}

			// Construct the POJO name (e.g., "UserByUniqueEmailData", "OrderByIndexIdxStatusData").
			String pojoName = classNamePrefix + (index.isUnique ? "Unique" : "Index") + index.pojoNameSuffix + "Data";
			generatePojoFromTemplate(pojoName, POJO_PACKAGE, indexColumns, pojoOutputDir);
		}

		// Generate the DAO file.
		System.out.println("  Generating DAO for '" + tableName + "'...");
		generateDaoFromTemplate(tableName, classNamePrefix, DAO_PACKAGE, columns, primaryKeys, indexes, daoOutputDir);

		System.out.println("  Successfully generated code for table '" + tableName + "'.");
	}

	// --- POJO Generation Helpers ---

	/**
	 * Generates the Java source file content for a POJO class using a template.
	 *
	 * @param className   The desired simple name of the POJO class (e.g.,
	 *                    "UserData").
	 * @param packageName The target package for the POJO (e.g., "com.test.model").
	 * @param columns     A list of {@link ColumnInfo} objects representing the
	 *                    fields of the POJO.
	 * @param outputDir   The directory where the generated ".java" file will be
	 *                    written.
	 * @throws IOException If template reading or file writing fails.
	 */
	private void generatePojoFromTemplate(String className, String packageName, List<ColumnInfo> columns, Path outputDir) throws IOException {
		// Basic validation: cannot generate a POJO without columns.
		if (columns == null || columns.isEmpty()) {
			System.err.println("  Skipping POJO generation for '" + className + "' because no columns were provided.");
			return;
		}

		// Load the POJO class template content.
		String template = loadTemplate("pojo_class.template");

		// Prepare the map of values to replace placeholders in the template.
		Map<String, String> values = new HashMap<>();
		values.put("packageName", packageName);
		values.put("className", className);

		// Generate specific code blocks for different parts of the POJO template.
		values.put("imports_block", generatePojoImports(columns));
		values.put("field_declarations", generatePojoFields(columns));
		values.put("constructor_params", generatePojoConstructorParams(columns));
		values.put("constructor_assignments", generatePojoConstructorAssignments(columns));
		values.put("getters_setters", generatePojoGettersSetters(columns));
		values.put("toString_content", generatePojoToStringContent(columns));

		// Perform placeholder replacement.
		String generatedCode = replacePlaceholders(template, values);

		// Construct the final output file path.
		Path filePath = outputDir.resolve(className + ".java");

		// Write the generated code to the file.
		writeFile(filePath, generatedCode);
		System.out.println("    -> Generated POJO: " + filePath.getFileName());
	}

	/**
	 * Generates the necessary import statements for a POJO based on its column
	 * types. Includes imports for types like BigDecimal, Date, Time, Timestamp if
	 * needed.
	 *
	 * @param columns The list of columns for the POJO.
	 * @return A string containing the required import statements, sorted
	 *         alphabetically. Includes a trailing newline if imports exist.
	 */
	private String generatePojoImports(List<ColumnInfo> columns) {
		// Use a TreeSet to automatically sort imports and avoid duplicates.
		Set<String> imports = new TreeSet<>();
		
		// Check if any column requires a specific import.
		if (columns.stream().anyMatch(c -> "java.math.BigDecimal".equals(c.javaType))) {
			imports.add("import java.math.BigDecimal;");
		}
		if (columns.stream().anyMatch(c -> "java.sql.Date".equals(c.javaType))) {
			imports.add("import java.sql.Date;");
		}
		if (columns.stream().anyMatch(c -> "java.sql.Time".equals(c.javaType))) {
			imports.add("import java.sql.Time;");
		}
		if (columns.stream().anyMatch(c -> "java.sql.Timestamp".equals(c.javaType))) {
			imports.add("import java.sql.Timestamp;");
		}
		
		// Join the imports with newlines. Add an extra newline at the end if there are any imports.
		return String.join("\n", imports) + (imports.isEmpty() ? "" : "\n");
	}

	/**
	 * Generates the private field declarations for a POJO class.
	 *
	 * @param columns The list of columns representing the fields.
	 * @return A string containing the field declarations, one per line, properly
	 *         indented.
	 */
	private String generatePojoFields(List<ColumnInfo> columns) {
		// Format: " private JavaType javaName;"
		return columns.stream().map(c -> String.format("    private %s %s;", c.javaType, c.javaName)).collect(Collectors.joining("\n"));
	}

	/**
	 * Generates the parameter list string for a constructor that accepts all
	 * fields.
	 *
	 * @param columns The list of columns representing the constructor parameters.
	 * @return A string like "Type1 name1, Type2 name2, ...".
	 */
	private String generatePojoConstructorParams(List<ColumnInfo> columns) {
		// Format: "JavaType javaName"
		return columns.stream().map(c -> String.format("%s %s", c.javaType, c.javaName)).collect(Collectors.joining(", "));
	}

	/**
	 * Generates the assignment statements within the body of a constructor that
	 * accepts all fields.
	 *
	 * @param columns The list of columns representing the fields to assign.
	 * @return A string containing assignment lines like " this.name1 = name1;",
	 *         properly indented.
	 */
	private String generatePojoConstructorAssignments(List<ColumnInfo> columns) {
		// Format: " this.javaName = javaName;"
		return columns.stream().map(c -> String.format("this.%s = %s;", c.javaName, c.javaName)).collect(Collectors.joining("\n"));
	}

	/**
	 * Generates standard public getter and setter methods for all fields in a POJO.
	 *
	 * @param columns The list of columns for which to generate accessors.
	 * @return A string containing the complete source code for all getters and
	 *         setters.
	 */
	private String generatePojoGettersSetters(List<ColumnInfo> columns) {
		StringBuilder sb = new StringBuilder();
		for (ColumnInfo col : columns) {
			// Convert field name (camelCase) to Capitalized name for methods (e.g., userId
			// -> UserId).
			String capitalizedJavaName = Name.toClassName(col.javaName);
			
			// Generate Getter
			sb.append(String.format("\tpublic %s get%s() {\n", col.javaType, capitalizedJavaName));
			sb.append(String.format("\t\treturn %s;\n", col.javaName));
			sb.append("    }\n\n"); // Add blank line between methods
			
			// Generate Setter
			sb.append(String.format("\tpublic void set%s(%s %s) {\n", capitalizedJavaName, col.javaType, col.javaName));
			sb.append(String.format("\t\tthis.%s = %s;\n", col.javaName, col.javaName));
			sb.append("\t}\n\n"); // Add blank line between methods
		}
		
		// Remove the last trailing blank line for cleaner output.
		return sb.toString().trim();
	}

	/**
	 * Generates the content string for the {@code toString()} method of a POJO.
	 * Produces output like "fieldName1='value1', fieldName2='value2'".
	 *
	 * @param columns The list of columns to include in the toString output.
	 * @return A string formatted for concatenation within the toString method body.
	 */
	private String generatePojoToStringContent(List<ColumnInfo> columns) {
		// Format: " \"javaName='\" + javaName + '\\''"
		return columns.stream()
			// Add single quotes around the value, useful for string representation.
			.map(c -> String.format("\"%s='\" + %s + '\\''", c.javaName, c.javaName))
			// Join with ", " separator.
			.collect(Collectors.joining(" + \", \" +\n"));
	}

	// --- DAO Generation Helpers ---

	/**
	 * Generates the Java source file content for a DAO class using a template. It
	 * assembles the final DAO code by generating individual method blocks using
	 * their respective templates.
	 *
	 * @param tableName       The name of the database table this DAO interacts
	 *                        with.
	 * @param classNamePrefix The base name derived from the table name (e.g.,
	 *                        "UserProfile").
	 * @param packageName     The target package for the DAO class (e.g.,
	 *                        "com.test.dao").
	 * @param allColumns      A list of all {@link ColumnInfo} for the table.
	 * @param primaryKeys     A list of {@link ColumnInfo} representing the primary
	 *                        key.
	 * @param indexes         A map of index names to {@link IndexInfo} for the
	 *                        table.
	 * @param outputDir       The directory where the generated ".java" file will be
	 *                        written.
	 * @throws IOException If template reading or file writing fails.
	 */
	private void generateDaoFromTemplate(String tableName, String classNamePrefix, String packageName, List<ColumnInfo> allColumns, List<ColumnInfo> primaryKeys, Map<String, IndexInfo> indexes, Path outputDir) throws IOException {
		// Determine names used within the generated code.
		String daoClassName = classNamePrefix + "Dao";
		String dataPojoName = classNamePrefix + "Data"; // e.g., UserProfileData
		String mapRowMethodName = "mapRowTo" + dataPojoName; // e.g., mapRowToUserProfileData

		// Use a StringBuilder to efficiently collect the source code of all generated
		// methods.
		StringBuilder methodsBlock = new StringBuilder();

		// --- Generate Methods Block ---

		// Insert Method (always generated)
		methodsBlock.append(generateInsertMethodFromTemplate(tableName, dataPojoName, allColumns));

		// Primary Key Based Methods (only if PK exists)
		if (!primaryKeys.isEmpty()) {
			String pkPojoName = classNamePrefix + "PkData"; // e.g., UserProfilePkData
			methodsBlock.append(generateUpdateMethodFromTemplate(tableName, dataPojoName, allColumns, primaryKeys));
			methodsBlock.append(generateDeleteByPkMethodFromTemplate(tableName, pkPojoName, primaryKeys));
			methodsBlock.append(generateGetByPkMethodFromTemplate(tableName, dataPojoName, pkPojoName, primaryKeys, mapRowMethodName));
//		} else {
//			// Add a comment indicating why PK methods are missing.
//			methodsBlock.append(String.format(
//				"\n    // Note: Update, Delete by PK, Get by PK methods were not generated because no primary key was found for table '%s'.\n",
//				tableName
//			));
		}

		// Index Based Methods (Get, Delete, Exists for each suitable index)
		for (IndexInfo index : indexes.values()) {
			// Skip processing if the index has no columns or if it's the implicit PK index (already handled).
			if (index.columnDbNames.isEmpty() || (index.indexName.equalsIgnoreCase("PRIMARY") && !primaryKeys.isEmpty())) {
				continue;
			}

			// Reconstruct the ordered list of ColumnInfo objects for this specific index.
			List<ColumnInfo> indexColumns = allColumns.stream().filter(c -> index.columnDbNames.contains(c.dbName))
				.sorted(Comparator.comparingInt(c -> new ArrayList<>(index.columnDbNames).indexOf(c.dbName)))
				.collect(Collectors.toList());

			// Skip if column resolution failed.
			if (indexColumns.isEmpty()) {
				continue;
			}

			// Determine names for the index-specific POJO and method suffix.
			String indexPojoName = classNamePrefix + (index.isUnique ? "Unique" : "Index") + index.pojoNameSuffix + "Data";
			String methodNameSuffix = index.methodNameSuffix; // e.g., "ByUserIdEmail"

			// Generate Get and Delete methods (specific template for unique vs non-unique).
			if (index.isUnique) {
				methodsBlock.append(generateGetByUniqueIndexMethodFromTemplate(tableName, dataPojoName, indexPojoName, methodNameSuffix, indexColumns, mapRowMethodName, index.indexName));
				methodsBlock.append(generateDeleteByUniqueIndexMethodFromTemplate(tableName, indexPojoName, methodNameSuffix, indexColumns, index.indexName));
			} else {
				methodsBlock.append(generateGetByIndexMethodFromTemplate(tableName, dataPojoName, indexPojoName, methodNameSuffix, indexColumns, mapRowMethodName, index.indexName));
				methodsBlock.append(generateDeleteByIndexMethodFromTemplate(tableName, indexPojoName, methodNameSuffix, indexColumns, index.indexName));
			}

			// Generate the 'existsBy...' method for this index (applicable to both unique and non-unique).
			methodsBlock.append(generateExistsByIndexMethodFromTemplate(tableName, indexPojoName, methodNameSuffix, indexColumns, index.indexName));
		}

		// Generate the private row mapping helper method.
		String mapRowMethodBlock = generateMapRowMethodFromTemplate(dataPojoName, allColumns, mapRowMethodName);

		// --- Assemble Final DAO Class ---

		// Load the main DAO class template.
		String daoTemplate = loadTemplate("dao_class.template");

		// Prepare values for the DAO class template placeholders.
		Map<String, String> daoValues = new HashMap<>();
		daoValues.put("packageName", packageName); // DAO package
		daoValues.put("pojoPackage", POJO_PACKAGE); // POJO package (for imports)
		daoValues.put("daoClassName", daoClassName); // DAO class name
		daoValues.put("extra_imports", generateDaoExtraImports(allColumns)); // e.g., BigDecimal import
		daoValues.put("methods_block", methodsBlock.toString()); // All generated public/private methods
		daoValues.put("map_row_method_block", mapRowMethodBlock); // The mapRowTo... method

		// Perform placeholder replacement for the main DAO template.
		String generatedCode = replacePlaceholders(daoTemplate, daoValues);

		// Write the final DAO source file.
		Path filePath = outputDir.resolve(daoClassName + ".java");
		writeFile(filePath, generatedCode);
		System.out.println("    -> Generated DAO: " + filePath.getFileName());
	}

	// --- Specific DAO Method Generation Helpers ---

	/**
	 * Generates the source code for the 'insert' method using its template.
	 *
	 * @param tableName    Name of the database table.
	 * @param dataPojoName Name of the POJO class representing table data.
	 * @param allColumns   List of all columns in the table.
	 * @return The generated source code string for the insert method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateInsertMethodFromTemplate(String tableName, String dataPojoName, List<ColumnInfo> allColumns) throws IOException {
		// Load the specific template for the insert method.
		String template = loadTemplate("dao_method_insert.template");
		
		// Prepare values needed by this template.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts: column names and parameter placeholders.
		String columnsClause = allColumns.stream().map(c -> "`" + c.dbName + "`").collect(Collectors.joining(", ")); // Add backticks for safety
		String valuesClause = allColumns.stream().map(c -> "?").collect(Collectors.joining(", "));
		String sql = String.format("INSERT INTO `%s` (%s) VALUES (%s)", tableName, columnsClause, valuesClause); // Add backticks

		values.put("tableName", tableName);
		values.put("dataPojoName", dataPojoName);
		values.put("sqlQuery", sql);
		
		// Generate the block of code for setting PreparedStatement parameters.
		values.put("parameter_setting_block", generateParameterSettingBlock(allColumns, "data", "\t\t\t")); // Indentation: 12 spaces

		// Replace placeholders and return the generated method code.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for the 'update' method using its template. Updates
	 * a row based on its primary key.
	 *
	 * @param tableName    Name of the database table.
	 * @param dataPojoName Name of the main data POJO class.
	 * @param allColumns   List of all columns in the table.
	 * @param primaryKeys  List of columns composing the primary key.
	 * @return The generated source code string for the update method, or a comment
	 *         if no update is possible.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateUpdateMethodFromTemplate(String tableName, String dataPojoName, List<ColumnInfo> allColumns, List<ColumnInfo> primaryKeys) throws IOException {
		// Identify columns that are NOT part of the primary key (these are the ones to update).
		List<ColumnInfo> nonPkColumns = allColumns.stream().filter(c -> !c.isPrimaryKey).collect(Collectors.toList());

		// If all columns are part of the PK, an update doesn't make sense.
		if (nonPkColumns.isEmpty()) {
			return String.format(
				"\n    // Note: Update method was not generated because table '%s' has no non-primary-key columns to update.\n",
				tableName
			);
		}

		// Load the update method template.
		String template = loadTemplate("dao_method_update.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts: SET clause and WHERE clause. Use backticks.
		String setClause = nonPkColumns.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(", "));
		String whereClause = primaryKeys.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		String sql = String.format("UPDATE `%s` SET %s WHERE %s", tableName, setClause, whereClause); // Add backticks

		// Generate parameter setting block: first non-PK cols, then PK cols.
		StringBuilder paramBlock = new StringBuilder();
		paramBlock.append(generateParameterSettingBlock(nonPkColumns, "data", "\t\t\t", 1)); // Start index 1
		paramBlock.append("\n"); // Newline between SET and WHERE parameters for readability
		paramBlock.append(generateParameterSettingBlock(primaryKeys, "data", "\t\t\t", nonPkColumns.size() + 1)); // Continue index

		values.put("tableName", tableName);
		values.put("dataPojoName", dataPojoName);
		values.put("sqlQuery", sql);
		values.put("parameter_setting_block", paramBlock.toString());

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for the 'delete' method (by primary key) using its
	 * template.
	 *
	 * @param tableName   Name of the database table.
	 * @param pkPojoName  Name of the POJO class representing the primary key.
	 * @param primaryKeys List of columns composing the primary key.
	 * @return The generated source code string for the delete method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateDeleteByPkMethodFromTemplate(String tableName, String pkPojoName, List<ColumnInfo> primaryKeys) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_delete_pk.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL WHERE clause. Use backticks.
		String whereClause = primaryKeys.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		String sql = String.format("DELETE FROM `%s` WHERE %s", tableName, whereClause); // Add backticks

		values.put("tableName", tableName);
		values.put("pkPojoName", pkPojoName);
		values.put("sqlQuery", sql);
		
		// Generate parameter setting block for the PK columns.
		values.put("parameter_setting_block", generateParameterSettingBlock(primaryKeys, "pkData", "\t\t\t"));

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for the 'get' method (by primary key) using its
	 * template. Expects to return 0 or 1 result.
	 *
	 * @param tableName        Name of the database table.
	 * @param dataPojoName     Name of the main data POJO class.
	 * @param pkPojoName       Name of the primary key POJO class.
	 * @param primaryKeys      List of columns composing the primary key.
	 * @param mapRowMethodName The name of the private helper method used to map a
	 *                         ResultSet row to the data POJO.
	 * @return The generated source code string for the get-by-PK method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateGetByPkMethodFromTemplate(String tableName, String dataPojoName, String pkPojoName, List<ColumnInfo> primaryKeys, String mapRowMethodName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_get_pk.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL WHERE clause. Use backticks.
		String whereClause = primaryKeys.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		
		// Simple SELECT * for brevity. Consider selecting explicit columns for production code.
		String sql = String.format("SELECT * FROM `%s` WHERE %s", tableName, whereClause); // Add backticks

		values.put("tableName", tableName);
		values.put("dataPojoName", dataPojoName);
		values.put("pkPojoName", pkPojoName);
		values.put("sqlQuery", sql);
		
		// Generate parameter setting block for the PK columns.
		values.put("parameter_setting_block", generateParameterSettingBlock(primaryKeys, "pkData", "\t\t\t"));
		
		// Pass the name of the mapping method.
		values.put("mapRowMethodName", mapRowMethodName);

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for a 'get' method based on a unique index using
	 * its template. Expects to return 0 or 1 result.
	 *
	 * @param tableName        Name of the database table.
	 * @param dataPojoName     Name of the main data POJO class.
	 * @param indexPojoName    Name of the POJO representing the unique index
	 *                         columns.
	 * @param methodNameSuffix Suffix for the method name (e.g., "ByUserIdEmail").
	 * @param indexColumns     List of columns composing the unique index.
	 * @param mapRowMethodName Name of the row mapping helper method.
	 * @param indexName        The actual name of the index in the database (for
	 *                         comments).
	 * @return The generated source code string for the get-by-unique-index method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateGetByUniqueIndexMethodFromTemplate(String tableName, String dataPojoName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String mapRowMethodName, String indexName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_get_unique.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts. Use backticks.
		String whereClause = indexColumns.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		String sql = String.format("SELECT * FROM `%s` WHERE %s", tableName, whereClause); // Add backticks
		
		// Construct the full method name.
		String methodName = "get" + methodNameSuffix;

		values.put("tableName", tableName);
		values.put("dataPojoName", dataPojoName);
		values.put("indexPojoName", indexPojoName);
		values.put("methodNameSuffix", methodNameSuffix); // Keep suffix if needed by template logic
		values.put("methodName", methodName); // Full method name
		values.put("indexColumnsList", indexColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", "))); // For Javadoc
		values.put("indexName", indexName); // For Javadoc
		values.put("sqlQuery", sql);
		
		// Generate parameter setting block for index columns, using "uniqueData" as variable name.
		values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "uniqueData", "\t\t\t"));
		values.put("mapRowMethodName", mapRowMethodName);

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for a 'delete' method based on a unique index using
	 * its template.
	 *
	 * @param tableName        Name of the database table.
	 * @param indexPojoName    Name of the POJO representing the unique index
	 *                         columns.
	 * @param methodNameSuffix Suffix for the method name (e.g., "ByUserIdEmail").
	 * @param indexColumns     List of columns composing the unique index.
	 * @param indexName        The actual name of the index in the database (for
	 *                         comments).
	 * @return The generated source code string for the delete-by-unique-index
	 *         method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateDeleteByUniqueIndexMethodFromTemplate(String tableName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String indexName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_delete_unique.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts. Use backticks.
		String whereClause = indexColumns.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		String sql = String.format("DELETE FROM `%s` WHERE %s", tableName, whereClause); // Add backticks
		
		// Construct the full method name.
		String methodName = "delete" + methodNameSuffix;

		values.put("tableName", tableName);
		values.put("indexPojoName", indexPojoName);
		values.put("methodNameSuffix", methodNameSuffix);
		values.put("methodName", methodName);
		values.put("indexColumnsList", indexColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", ")));
		values.put("indexName", indexName);
		values.put("sqlQuery", sql);
		
		// Generate parameter setting block for index columns, using "uniqueData" as variable name.
		values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "uniqueData", "\t\t\t"));

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for a 'get' method based on a non-unique index
	 * using its template. Returns an array of results.
	 *
	 * @param tableName        Name of the database table.
	 * @param dataPojoName     Name of the main data POJO class.
	 * @param indexPojoName    Name of the POJO representing the index columns.
	 * @param methodNameSuffix Suffix for the method name (e.g., "ByStatus").
	 * @param indexColumns     List of columns composing the index.
	 * @param mapRowMethodName Name of the row mapping helper method.
	 * @param indexName        The actual name of the index in the database (for
	 *                         comments).
	 * @return The generated source code string for the get-by-index method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateGetByIndexMethodFromTemplate(String tableName, String dataPojoName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String mapRowMethodName, String indexName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_get_index.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts. Use backticks.
		String whereClause = indexColumns.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		String sql = String.format("SELECT * FROM `%s` WHERE %s", tableName, whereClause); // Add backticks
		
		// Construct the full method name.
		String methodName = "get" + methodNameSuffix;

		values.put("tableName", tableName);
		values.put("dataPojoName", dataPojoName);
		values.put("indexPojoName", indexPojoName);
		values.put("methodNameSuffix", methodNameSuffix);
		values.put("methodName", methodName);
		values.put("indexColumnsList", indexColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", ")));
		values.put("indexName", indexName);
		values.put("sqlQuery", sql);
		
		// Generate parameter setting block for index columns, using "indexData" as variable name.
		values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "indexData", "\t\t\t"));
		values.put("mapRowMethodName", mapRowMethodName);

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for a 'delete' method based on a non-unique index
	 * using its template. Can affect multiple rows.
	 *
	 * @param tableName        Name of the database table.
	 * @param indexPojoName    Name of the POJO representing the index columns.
	 * @param methodNameSuffix Suffix for the method name (e.g., "ByStatus").
	 * @param indexColumns     List of columns composing the index.
	 * @param indexName        The actual name of the index in the database (for
	 *                         comments).
	 * @return The generated source code string for the delete-by-index method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateDeleteByIndexMethodFromTemplate(String tableName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String indexName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_delete_index.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts. Use backticks.
		String whereClause = indexColumns.stream().map(c -> "`" + c.dbName + "` = ?").collect(Collectors.joining(" AND "));
		String sql = String.format("DELETE FROM `%s` WHERE %s", tableName, whereClause); // Add backticks
		
		// Construct the full method name.
		String methodName = "delete" + methodNameSuffix;

		values.put("tableName", tableName);
		values.put("indexPojoName", indexPojoName);
		values.put("methodNameSuffix", methodNameSuffix);
		values.put("methodName", methodName);
		values.put("indexColumnsList", indexColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", ")));
		values.put("indexName", indexName);
		values.put("sqlQuery", sql);
		
		// Generate parameter setting block for index columns, using "indexData" as variable name.
		values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "indexData", "\t\t\t"));

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for an 'exists' check method based on index columns
	 * using its template.
	 *
	 * @param tableName        Name of the database table.
	 * @param indexPojoName    Name of the POJO representing the index columns.
	 * @param methodNameSuffix Suffix for the method name (e.g., "ByUserIdEmail").
	 * @param indexColumns     List of columns composing the index.
	 * @param indexName        The actual name of the index in the database (for
	 *                         comments).
	 * @return The generated source code string for the exists-by-index method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateExistsByIndexMethodFromTemplate(String tableName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String indexName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_exists_by_index.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();
		
		// Construct SQL parts. Use backticks.
		String whereClause = indexColumns.stream().map(c -> "`" + c.dbName + "` = ?") .collect(Collectors.joining(" AND "));
		
		// Use "SELECT 1" for efficiency, common optimization for existence checks.
		// LIMIT 1 is also helpful.
		String sql = String.format("SELECT 1 FROM `%s` WHERE %s LIMIT 1", tableName, whereClause); // Add backticks

		values.put("tableName", tableName);
		values.put("indexPojoName", indexPojoName);
		
		// Method name in template is constructed like "exists" + Suffix.
		values.put("methodNameSuffix", methodNameSuffix);
		values.put("indexColumnsList", indexColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", ")));
		values.put("indexName", indexName);
		values.put("sqlQuery", sql); // Pass SQL though template might not use ${sqlQuery} directly
		values.put("where_clause", whereClause); // Pass where clause separately if needed by template
		
		// Generate parameter setting block for index columns, using "indexData" as variable name.
		values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "indexData", "\t\t\t"));

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	/**
	 * Generates the source code for the private helper method that maps a
	 * {@link ResultSet} row to a data POJO instance.
	 *
	 * @param dataPojoName     Name of the main data POJO class (e.g.,
	 *                         "UserProfileData").
	 * @param allColumns       List of all columns to map from the ResultSet.
	 * @param mapRowMethodName The desired name for the generated mapping method.
	 * @return The generated source code string for the mapping method.
	 * @throws IOException If the template cannot be read.
	 */
	private String generateMapRowMethodFromTemplate(String dataPojoName, List<ColumnInfo> allColumns, String mapRowMethodName) throws IOException {
		// Load template.
		String template = loadTemplate("dao_method_map_row.template");
		
		// Prepare values.
		Map<String, String> values = new HashMap<>();

		// Build the block of mapping code (data.setField(rs.getObject(...))).
		StringBuilder mappingBlock = new StringBuilder();
		for (ColumnInfo col : allColumns) {
			// Use rs.getObject(columnName, TargetType.class) for robust handling of SQL NULLs when mapping to Java wrapper types (Integer, Long, Boolean, etc.).
			mappingBlock.append(String.format(
				"\t\tdata.set%s(rs.getObject(\"%s\", %s.class));\n",
				Name.toClassName(col.javaName), // setUserId
				col.dbName, // "user_id"
				col.javaType, // Integer
				JDBCType.valueOf(col.sqlType).getName(), // VARCHAR / INT / etc.
				col.sqlType // java.sql.Types.INTEGER
			));
		}

		values.put("dataPojoName", dataPojoName);
		values.put("mapRowMethodName", mapRowMethodName);
		values.put("resultSetMappingBlock", mappingBlock.toString());

		// Replace and return.
		return replacePlaceholders(template, values);
	}

	// --- General Utility Helpers ---

	/**
	 * Generates necessary import statements for a DAO class, currently only for
	 * {@code java.math.BigDecimal}.
	 *
	 * @param columns The list of all columns in the table associated with the DAO.
	 * @return A string containing required import statements, or an empty string if
	 *         none are needed. Includes a trailing newline if imports exist.
	 */
	private String generateDaoExtraImports(List<ColumnInfo> columns) {
		Set<String> imports = new TreeSet<>(); // Sorted and unique imports.
		
		// Check if BigDecimal is used by any column.
		if (columns.stream().anyMatch(c -> "java.math.BigDecimal".equals(c.javaType))) {
			imports.add("import java.math.BigDecimal;");
		}
		
		// Add checks for other potential imports here if needed in the future.

		// Join imports and add trailing newline if needed.
		return String.join("\n", imports) + (imports.isEmpty() ? "" : "\n");
	}

	/**
	 * Generates a block of Java code to set parameters on a
	 * {@link PreparedStatement} using values obtained from getter methods of a
	 * specified POJO variable. Starts parameter indexing at 1.
	 *
	 * @param columns          A List of {@link ColumnInfo} objects defining the
	 *                         parameters to set (in order).
	 * @param pojoVariableName The name of the Java variable holding the POJO
	 *                         instance containing the values.
	 * @param indentation      A string used for indenting each generated line
	 *                         (e.g., " ").
	 * @return A string containing multiple lines of
	 *         {@code pstmt.setObject(index, pojo.get...());}.
	 */
	private String generateParameterSettingBlock(List<ColumnInfo> columns, String pojoVariableName, String indentation) {
		// Delegate to the more general version starting index at 1.
		return generateParameterSettingBlock(columns, pojoVariableName, indentation, 1);
	}

	/**
	 * Generates a block of Java code to set parameters on a
	 * {@link PreparedStatement} using values obtained from getter methods of a
	 * specified POJO variable, allowing a custom starting index.
	 *
	 * @param columns          A List of {@link ColumnInfo} objects defining the
	 *                         parameters to set (in order).
	 * @param pojoVariableName The name of the Java variable holding the POJO
	 *                         instance containing the values.
	 * @param indentation      A string used for indenting each generated line
	 *                         (e.g., " ").
	 * @param startIndex       The JDBC parameter index (1-based) to start numbering
	 *                         from.
	 * @return A string containing multiple lines of
	 *         {@code pstmt.setObject(index, pojo.get...());}.
	 */
	private String generateParameterSettingBlock(List<ColumnInfo> columns, String pojoVariableName, String indentation, int startIndex) {
		StringBuilder sb = new StringBuilder();
		int paramIndex = startIndex; // Start parameter index as specified.
		for (ColumnInfo col : columns) {
			// Construct the line: indentation + pstmt.setObject(index, pojoVar.getGetterName());
			sb.append(String.format(
				"%spstmt.setObject(%d, %s.get%s());\n", indentation, paramIndex++, // Increment index for next parameter
				pojoVariableName, Name.toClassName(col.javaName), // Generate getter name (e.g., UserId)
				col.javaType // Add Java type as comment
			));
		}
		// Remove the last newline character if the block is not empty.
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * Writes the given string content to the specified file path using UTF-8
	 * encoding. Creates parent directories if they do not exist.
	 *
	 * @param filePath The full {@link Path} to the output file.
	 * @param content  The string content to write to the file.
	 * @throws IOException if an error occurs during directory creation or file
	 *                     writing.
	 */
	private void writeFile(Path filePath, String content) throws IOException {
		try {
			// Ensure the parent directory exists. Create it if necessary.
			Path parentDir = filePath.getParent();
			if (parentDir != null) { // Check for null in case filePath is a root element
				Files.createDirectories(parentDir);
			}
			
			// Write the content to the file using UTF-8 encoding. Overwrites if file
			// exists.
			Files.writeString(filePath, content, StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Error writing generated file: " + filePath);
			// Re-throw the exception to be handled by the caller.
			throw e;
		}
	}

	/**
	 * Closes the database {@link Connection} if it is currently open. Logs any
	 * {@link SQLException} that occurs during closing but does not propagate it.
	 * Sets the connection member variable to null afterwards.
	 */
	private void closeConnection() {
		// Check if the connection exists and is potentially open.
		if (connection != null) {
			try {
				// Attempt to close the connection.
				connection.close();
				System.out.println("\nDatabase connection closed.");
			} catch (SQLException e) {
				// Log error if closing fails, but don't stop the application.
				System.err.println("Error closing the database connection: " + e.getMessage());
				// Optionally log the stack trace for debugging: e.printStackTrace();
			} finally {
				// Set to null regardless of whether close succeeded or failed,
				// to prevent potential reuse attempts.
				connection = null;
			}
		} else {
			System.out.println("\nDatabase connection was already closed or never opened.");
		}
	}

	/**
	 * The main entry point for executing the DAO/POJO generator from the command
	 * line. Creates an instance of {@link DaoGenerator}, runs the generation
	 * process, and handles top-level exceptions. Measures and prints the execution
	 * time.
	 *
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		System.out.println(">>> Starting DAO/POJO Generator <<<");
		long startTime = System.currentTimeMillis(); // Record start time for duration measurement.
		int exitCode = 0; // Default exit code 0 for success.

		try {
			// Create and run the generator instance.
			DaoGenerator generator = new DaoGenerator();
			generator.generate();

		} catch (SQLException | IOException e) {
			// Catch critical errors during initialization or core processing.
			System.err.println("\n*** CRITICAL ERROR DURING GENERATION ***");
			e.printStackTrace(); // Print stack trace for detailed diagnosis.
			exitCode = 1; // Set non-zero exit code for critical failure.

		} catch (Exception e) {
			// Catch any other unexpected runtime exceptions.
			System.err.println("\n*** UNEXPECTED ERROR DURING GENERATION ***");
			e.printStackTrace();
			exitCode = 2; // Different exit code for unexpected errors.

		} finally {
			// Calculate and print execution duration.
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.println(">>> Generator finished in " + duration + " ms <<<");

			// Exit with the appropriate code.
			if (exitCode != 0) {
				System.exit(exitCode);
			}
		}
	}
}