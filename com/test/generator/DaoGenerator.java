package com.test.generator;

import com.test.generator.util.InfoHolder.ColumnInfo;
import com.test.generator.util.InfoHolder.IndexInfo;
import com.test.generator.util.NameUtils;
import com.test.generator.util.TypeUtils;

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
 * Générateur de code pour les classes POJO et DAO à partir d'une base de données MySQL.
 * Lit la configuration de la base de données depuis un fichier 'database.properties'.
 * Génère le code en utilisant des fichiers template.
 */
public class DaoGenerator {

    // --- Configuration ---
    /** Nom du fichier de configuration à chercher dans le classpath. */
    private static final String CONFIG_FILE = "configuration/database.properties";
    /** Répertoire racine de sortie pour les fichiers générés. */
    private static final String OUTPUT_DIR = "src";
    /** Package Java cible pour les classes POJO générées. */
    private static final String POJO_PACKAGE = "com.test.model";
    /** Package Java cible pour les classes DAO générées. */
    private static final String DAO_PACKAGE = "com.test.dao";
    /** Sous-répertoire de sortie pour les POJOs, basé sur le package. */
    private static final String POJO_SUBDIR = POJO_PACKAGE.replace('.', '/');
    /** Sous-répertoire de sortie pour les DAOs, basé sur le package. */
    private static final String DAO_SUBDIR = DAO_PACKAGE.replace('.', '/');
    /** Chemin vers le répertoire des templates dans le classpath. */
    private static final String TEMPLATE_DIR = "/resources/templates";
    /** Pattern Regex pour trouver les placeholders de type ${key} dans les templates. */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    // --- Fin Configuration ---

    // Informations de connexion pour le générateur (lues depuis CONFIG_FILE)
    private String generatorDbUrl;
    private String generatorDbUser;
    private String generatorDbPassword;

    // Objets JDBC
    private Connection connection;
    private DatabaseMetaData metaData;

    // Chemins de sortie calculés
    private final Path outputBaseDir;
    private final Path pojoOutputDir;
    private final Path daoOutputDir;

    /** Cache pour éviter de recharger les templates du disque à chaque fois. */
    private final Map<String, String> templateCache = new HashMap<>();

    /**
     * Constructeur. Charge la configuration, établit la connexion BDD et initialise les chemins de sortie.
     *
     * @throws SQLException Si une erreur de connexion BDD survient.
     * @throws IOException Si le fichier de configuration ou un template ne peut être lu.
     */
    public DaoGenerator() throws SQLException, IOException {
        loadGeneratorConfig(); // Charge db.url, db.username, db.password pour le générateur

        this.outputBaseDir = Paths.get(OUTPUT_DIR);
        this.pojoOutputDir = outputBaseDir.resolve(POJO_SUBDIR);
        this.daoOutputDir = outputBaseDir.resolve(DAO_SUBDIR);

        try {
            if (this.generatorDbUrl == null || this.generatorDbUrl.trim().isEmpty()) {
                throw new SQLException("L'URL de la base de données (db.url) pour le générateur est manquante ou vide dans " + CONFIG_FILE);
            }
            // NOTE: DriverManager.registerDriver() n'est plus nécessaire avec JDBC 4+ si le driver est dans le classpath.
            // DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            this.connection = DriverManager.getConnection(this.generatorDbUrl, this.generatorDbUser, this.generatorDbPassword);
            this.metaData = connection.getMetaData();
            System.out.println("Connecté à la base de données: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données lors de l'initialisation du générateur.");
            System.err.println("URL: " + this.generatorDbUrl + ", User: " + this.generatorDbUser);
            System.err.println("Vérifier le fichier '" + CONFIG_FILE + "' et l'accessibilité de la base.");
            throw e;
        }
    }

    /**
     * Charge les informations de connexion (URL, utilisateur, mot de passe)
     * depuis le fichier CONFIG_FILE situé dans le classpath.
     *
     * @throws IOException Si le fichier de configuration ne peut être trouvé ou lu.
     */
    private void loadGeneratorConfig() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration '" + CONFIG_FILE + "' non trouvé dans le classpath.");
            }
            props.load(input);
            this.generatorDbUrl = props.getProperty("db.url");
            this.generatorDbUser = props.getProperty("db.username");
            this.generatorDbPassword = props.getProperty("db.password"); // Peut être null ou vide

            // Log un avertissement si des infos semblent manquer (utile pour le debug)
            if (this.generatorDbUrl == null || this.generatorDbUrl.trim().isEmpty()) {
                System.err.println("WARN: La propriété 'db.url' est manquante ou vide dans " + CONFIG_FILE);
            }
            if (this.generatorDbUser == null || this.generatorDbUser.trim().isEmpty()) {
                 System.err.println("WARN: La propriété 'db.username' est manquante ou vide dans " + CONFIG_FILE);
            }
             if (this.generatorDbPassword == null) {
                 System.err.println("WARN: La propriété 'db.password' est manquante dans " + CONFIG_FILE + ". Utilisation d'un mot de passe null.");
                 // Une chaîne vide est valide pour certains setups MySQL, null pourrait poser problème.
                 this.generatorDbPassword = "";
             }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la configuration BDD pour le générateur depuis " + CONFIG_FILE);
            throw e;
        }
    }

    /**
     * Charge le contenu d'un fichier template depuis le classpath.
     * Utilise un cache simple pour éviter les lectures disque répétées.
     *
     * @param templateName Le nom du fichier template (ex: "pojo_class.template").
     * @return Le contenu du template sous forme de String.
     * @throws IOException Si le template n'est pas trouvé ou ne peut être lu.
     */
    private String loadTemplate(String templateName) throws IOException {
        // Vérifie le cache d'abord
        if (templateCache.containsKey(templateName)) {
            return templateCache.get(templateName);
        }
        // Construit le chemin complet dans le classpath
        String path = TEMPLATE_DIR + "/" + templateName;
        try (InputStream is = DaoGenerator.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Template non trouvé dans le classpath: " + path + ". Vérifier le nom et l'emplacement du fichier.");
            }
            // Lit tous les bytes et convertit en String UTF-8
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Met en cache avant de retourner
            templateCache.put(templateName, content);
            return content;
        } catch (NullPointerException e) {
             // Peut arriver si getResourceAsStream est appelé dans un contexte statique sans classe de référence correcte
             throw new IOException("Erreur lors de la recherche du template (chemin ou contexte invalide?): " + path, e);
        }
    }

    /**
     * Remplace les placeholders de la forme ${key} dans une chaîne template
     * par les valeurs correspondantes d'une Map.
     * Si une clé n'est pas trouvée dans la map, le placeholder n'est pas remplacé (ou remplacé par une chaine vide).
     *
     * @param template Le contenu du template avec des placeholders.
     * @param values   Une Map où les clés sont les noms des placeholders (sans ${}) et les valeurs sont les chaînes de remplacement.
     * @return La chaîne template avec les placeholders remplacés.
     */
    private String replacePlaceholders(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            // Récupère la valeur ou utilise une chaîne vide si non trouvée pour éviter ${key} dans le code généré.
            String value = values.getOrDefault(key, "");
            // Echappe les caractères spéciaux ($ et \) dans la valeur de remplacement
            // pour éviter les erreurs avec appendReplacement.
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        // Ajoute la partie restante du template après le dernier match.
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Méthode principale qui orchestre la génération du code.
     *
     * @throws SQLException Si une erreur BDD survient pendant la lecture des métadonnées.
     * @throws IOException Si une erreur d'écriture de fichier ou de lecture de template survient.
     */
    public void generate() throws SQLException, IOException {
        System.out.println("Création des répertoires de sortie...");
        createOutputDirs();

        System.out.println("Récupération de la liste des tables...");
        List<String> tableNames = getTableNames();
        System.out.println("Tables trouvées ("+ tableNames.size() +"): " + tableNames);

        if(tableNames.isEmpty()){
            System.out.println("Aucune table trouvée. Vérifier la base de données et les permissions.");
            return;
        }

        for (String tableName : tableNames) {
            System.out.println("\n--- Génération pour la table: " + tableName + " ---");
            try {
                generateForTable(tableName);
            } catch(Exception e) {
                // Attrape les erreurs spécifiques à une table pour ne pas bloquer toute la génération
                System.err.println("ERREUR lors de la génération pour la table '" + tableName + "': " + e.getMessage());
                e.printStackTrace(); // Affiche la stack trace pour le debug
            }
        }

        closeConnection();
        System.out.println("\n--- Génération terminée. Fichiers générés dans: " + outputBaseDir.toAbsolutePath() + " ---");
    }

    /**
     * Crée les répertoires de sortie pour les POJOs et les DAOs s'ils n'existent pas.
     * @throws IOException Si une erreur survient lors de la création des répertoires.
     */
    private void createOutputDirs() throws IOException {
         try {
             Files.createDirectories(pojoOutputDir);
             Files.createDirectories(daoOutputDir);
         } catch (IOException e) {
             System.err.println("Impossible de créer les répertoires de sortie: " + pojoOutputDir + " ou " + daoOutputDir);
             throw e;
         }
     }

    /**
     * Récupère la liste des noms de tables de la base de données connectée.
     * @return Une liste de noms de tables.
     * @throws SQLException Si une erreur BDD survient.
     */
    private List<String> getTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        // Arguments: catalog, schemaPattern, tableNamePattern, types (ici, seulement les TABLES)
        try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        return tableNames;
    }

    /**
     * Récupère les informations détaillées sur les colonnes d'une table spécifique.
     * @param tableName Le nom de la table.
     * @return Une liste d'objets ColumnInfo décrivant chaque colonne.
     * @throws SQLException Si une erreur BDD survient.
     */
    private List<ColumnInfo> getColumns(String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        Set<String> pkColumnNames = getPrimaryKeyColumnNames(tableName); // Optimisation: obtenir les PKs une seule fois
        // Arguments: catalog, schemaPattern, tableNamePattern, columnNamePattern
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, "%")) {
            while (rs.next()) {
                String dbName = rs.getString("COLUMN_NAME");
                int sqlType = rs.getInt("DATA_TYPE");
                // Mappe le type SQL en type Java (String) en utilisant notre utilitaire
                String javaType = TypeUtils.toJavaType(sqlType);
                // Convertit le nom de colonne DB (snake_case) en nom de champ Java (camelCase)
                String javaName = NameUtils.toFieldName(dbName);
                // Vérifie si cette colonne fait partie de la clé primaire
                boolean isPk = pkColumnNames.contains(dbName);
                columns.add(new ColumnInfo(dbName, javaName, javaType, sqlType, isPk));
            }
        }
        if(columns.isEmpty()){
            System.err.println("WARN: Aucune colonne trouvée pour la table '" + tableName + "'. Vérifier les permissions ou si la table est vide.");
        }
        return columns;
    }

    /**
     * Récupère les noms des colonnes composant la clé primaire d'une table.
     * @param tableName Le nom de la table.
     * @return Un Set contenant les noms des colonnes de la clé primaire.
     * @throws SQLException Si une erreur BDD survient.
     */
     private Set<String> getPrimaryKeyColumnNames(String tableName) throws SQLException {
         Set<String> pkColumnNames = new LinkedHashSet<>(); // LinkedHashSet pour garder l'ordre si jamais important
         // Arguments: catalog, schema, table
         try (ResultSet rs = metaData.getPrimaryKeys(connection.getCatalog(), null, tableName)) {
             // Trier par KEY_SEQ pour garantir l'ordre des colonnes dans les PK composites
             List<Map.Entry<Short, String>> pkList = new ArrayList<>();
             while (rs.next()) {
                pkList.add(Map.entry(rs.getShort("KEY_SEQ"), rs.getString("COLUMN_NAME")));
             }
             pkList.sort(Map.Entry.comparingByKey());
             for(Map.Entry<Short, String> entry : pkList) {
                 pkColumnNames.add(entry.getValue());
             }
         }
         return pkColumnNames;
     }

    /**
     * Récupère les informations sur tous les index (uniques et non-uniques) d'une table.
     * Regroupe les colonnes par nom d'index et détermine le nom des méthodes/POJOs associés.
     *
     * @param tableName Le nom de la table.
     * @return Une Map où la clé est le nom logique de l'index (souvent le nom DB)
     *         et la valeur est un objet IndexInfo contenant les détails.
     * @throws SQLException Si une erreur BDD survient.
     */
    private Map<String, IndexInfo> getIndexes(String tableName) throws SQLException {
        // Utilise LinkedHashMap pour préserver l'ordre de découverte des index (peut être utile).
        Map<String, IndexInfo> indexMap = new LinkedHashMap<>();
        // Arguments: catalog, schema, table, unique (false pour tous), approximate (true pour performance si stats dispo)
        try (ResultSet rs = metaData.getIndexInfo(connection.getCatalog(), null, tableName, false, true)) {
             while (rs.next()) {
                 // Ignorer les statistiques de table qui peuvent être retournées (TYPE = tableIndexStatistic)
                 short indexType = rs.getShort("TYPE");
                 if (indexType == DatabaseMetaData.tableIndexStatistic) {
                     continue;
                 }

                 String indexName = rs.getString("INDEX_NAME");
                 // Peut être null pour certains types d'index ou DBs, on l'ignore si c'est le cas.
                 // L'index de la clé primaire est souvent nommé 'PRIMARY'.
                 if (indexName == null) continue;

                 boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                 String columnName = rs.getString("COLUMN_NAME");

                 // Une colonne d'index doit avoir un nom.
                 if (columnName == null) continue;

                 // Récupère ou crée l'objet IndexInfo pour cet index.
                 // La clé de la map est le nom de l'index pour regrouper les colonnes.
                 IndexInfo indexInfo = indexMap.computeIfAbsent(indexName, k -> new IndexInfo(k, !nonUnique));

                 // Ajoute le nom de la colonne DB à la liste des colonnes de cet index.
                 // L'ordre d'ajout dépend de l'ordre retourné par getIndexInfo,
                 // qui est généralement trié par ORDINAL_POSITION. LinkedHashSet préserve cet ordre d'insertion.
                 indexInfo.columnDbNames.add(columnName);
             }
         }

        // Finalise les informations de chaque index (notamment les noms de méthodes/POJOs)
        // après avoir collecté toutes ses colonnes.
        Map<String, IndexInfo> finalIndexes = new LinkedHashMap<>();
        for (Map.Entry<String, IndexInfo> entry : indexMap.entrySet()) {
            // buildWithColumns calcule les suffixes Pojo/Method à partir des noms de colonnes collectés.
            finalIndexes.put(entry.getKey(), entry.getValue().buildWithColumns());
        }

        return finalIndexes;
    }

    /**
     * Génère les fichiers POJO et DAO pour une table spécifique.
     *
     * @param tableName Le nom de la table.
     * @throws SQLException Si une erreur BDD survient lors de la lecture des métadonnées.
     * @throws IOException Si une erreur d'écriture de fichier ou de lecture de template survient.
     */
    private void generateForTable(String tableName) throws SQLException, IOException {
        // Convertit le nom de table en nom de classe Java (ex: user_profiles -> UserProfiles)
        String classNamePrefix = NameUtils.toClassName(tableName);

        // Récupère les infos colonnes/PK/Index
        List<ColumnInfo> columns = getColumns(tableName);
        if (columns.isEmpty()) {
            // Pas de colonnes = pas de génération possible.
            System.out.println("  -> Table '" + tableName + "' ignorée (aucune colonne trouvée).");
            return;
        }
        List<ColumnInfo> primaryKeys = columns.stream().filter(c -> c.isPrimaryKey).collect(Collectors.toList());
        Map<String, IndexInfo> indexes = getIndexes(tableName);

        // Avertissement si pas de PK (impacte Update/Delete/Get by PK)
        if (primaryKeys.isEmpty()) {
            System.err.println("  WARN: Table '" + tableName + "' n'a pas de clé primaire. Opérations Update/Delete/Get par PK non générées.");
        }

        // 1. Générer les POJOs (Data, PkData, IndexData)
        System.out.println("  Génération des POJOs...");
        // POJO principal (toutes colonnes)
        generatePojoFromTemplate(classNamePrefix + "Data", POJO_PACKAGE, columns, pojoOutputDir);
        // POJO pour la clé primaire (si elle existe)
        if (!primaryKeys.isEmpty()) {
            generatePojoFromTemplate(classNamePrefix + "PkData", POJO_PACKAGE, primaryKeys, pojoOutputDir);
        }
        // POJOs pour chaque index
        for (IndexInfo index : indexes.values()) {
             // Recréer la liste ordonnée des colonnes pour cet index
             List<ColumnInfo> indexColumns = columns.stream()
                 .filter(c -> index.columnDbNames.contains(c.dbName))
                 // Trie les ColumnInfo selon l'ordre des noms dans index.columnDbNames
                 .sorted(Comparator.comparingInt(c -> new ArrayList<>(index.columnDbNames).indexOf(c.dbName)))
                 .collect(Collectors.toList());

             if (indexColumns.isEmpty()) continue; // Ne devrait pas arriver si getIndexes fonctionne

             // Nom du POJO basé sur les colonnes et unicité (ex: UserProfileByUserIdEmailUniqueData)
             String pojoName = classNamePrefix + index.pojoNameSuffix + (index.isUnique ? "UniqueData" : "IdxData");
             generatePojoFromTemplate(pojoName, POJO_PACKAGE, indexColumns, pojoOutputDir);
        }

        // 2. Générer le DAO
        System.out.println("  Génération du DAO...");
        generateDaoFromTemplate(tableName, classNamePrefix, DAO_PACKAGE, columns, primaryKeys, indexes, daoOutputDir);
        System.out.println("  Génération pour '" + tableName + "' terminée.");
    }

    // --- Génération POJO ---

    /**
     * Génère le fichier source Java pour une classe POJO à partir d'un template.
     *
     * @param className Le nom de la classe POJO à générer.
     * @param packageName Le package Java de la classe.
     * @param columns La liste des colonnes (ColumnInfo) à inclure comme champs dans le POJO.
     * @param outputDir Le répertoire de sortie où écrire le fichier .java.
     * @throws IOException Si une erreur de lecture de template ou d'écriture de fichier survient.
     */
    private void generatePojoFromTemplate(String className, String packageName, List<ColumnInfo> columns, Path outputDir) throws IOException {
        if (columns == null || columns.isEmpty()) {
            System.err.println("  -> Tentative de génération du POJO '" + className + "' sans colonnes. Ignoré.");
            return;
        }
        String template = loadTemplate("pojo_class.template");

        // Prépare les valeurs pour les placeholders du template POJO
        Map<String, String> values = new HashMap<>();
        values.put("packageName", packageName);
        values.put("className", className);
        values.put("imports_block", generatePojoImports(columns));
        values.put("field_declarations", generatePojoFields(columns));
        values.put("constructor_params", generatePojoConstructorParams(columns));
        values.put("constructor_assignments", generatePojoConstructorAssignments(columns));
        values.put("getters_setters", generatePojoGettersSetters(columns));
        values.put("toString_content", generatePojoToStringContent(columns));

        // Remplace les placeholders et écrit le fichier
        String generatedCode = replacePlaceholders(template, values);
        Path filePath = outputDir.resolve(className + ".java");
        writeFile(filePath, generatedCode);
        System.out.println("    -> POJO généré: " + filePath.getFileName());
    }

    /** Génère la section des imports pour un POJO. */
    private String generatePojoImports(List<ColumnInfo> columns) {
        // Utilise un TreeSet pour trier et éviter les doublons
        Set<String> imports = new TreeSet<>();
        if (columns.stream().anyMatch(c -> "java.math.BigDecimal".equals(c.javaType))) imports.add("import java.math.BigDecimal;");
        if (columns.stream().anyMatch(c -> "java.sql.Date".equals(c.javaType))) imports.add("import java.sql.Date;");
        if (columns.stream().anyMatch(c -> "java.sql.Time".equals(c.javaType))) imports.add("import java.sql.Time;");
        if (columns.stream().anyMatch(c -> "java.sql.Timestamp".equals(c.javaType))) imports.add("import java.sql.Timestamp;");
        // Ajoute un saut de ligne après les imports s'il y en a.
        return String.join("\n", imports) + (imports.isEmpty() ? "" : "\n");
    }

    /** Génère les déclarations de champs privés pour un POJO. */
    private String generatePojoFields(List<ColumnInfo> columns) {
        return columns.stream()
                .map(c -> String.format("    private %s %s;", c.javaType, c.javaName))
                .collect(Collectors.joining("\n"));
    }

    /** Génère la liste des paramètres pour le constructeur complet d'un POJO. */
     private String generatePojoConstructorParams(List<ColumnInfo> columns) {
        return columns.stream()
                 .map(c -> String.format("%s %s", c.javaType, c.javaName))
                 .collect(Collectors.joining(", "));
    }

    /** Génère les assignations dans le corps du constructeur complet d'un POJO. */
    private String generatePojoConstructorAssignments(List<ColumnInfo> columns) {
         return columns.stream()
                 .map(c -> String.format("        this.%s = %s;", c.javaName, c.javaName))
                 .collect(Collectors.joining("\n"));
    }

    /** Génère les méthodes getter et setter publiques pour un POJO. */
    private String generatePojoGettersSetters(List<ColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        for (ColumnInfo col : columns) {
            String capitalizedJavaName = NameUtils.toClassName(col.javaName); // Ex: userId
            // Getter
            sb.append(String.format("    public %s get%s() {\n", col.javaType, capitalizedJavaName));
            sb.append(String.format("        return %s;\n", col.javaName));
            sb.append("    }\n\n");
            // Setter
            sb.append(String.format("    public void set%s(%s %s) {\n", capitalizedJavaName, col.javaType, col.javaName));
            sb.append(String.format("        this.%s = %s;\n", col.javaName, col.javaName));
            sb.append("    }\n\n");
        }
        return sb.toString().trim(); // Enlève le dernier \n\n superflu
    }

    /** Génère le contenu de la méthode toString() pour un POJO. */
     private String generatePojoToStringContent(List<ColumnInfo> columns) {
         // Format: "fieldName='" + fieldName + '\'' + ", ..."
         return columns.stream()
                .map(c -> String.format("                \"%s='\" + %s + '\\''", c.javaName, c.javaName))
                .collect(Collectors.joining(" + \", \" +\n"));
    }

    // --- Génération DAO ---

    /**
     * Génère le fichier source Java pour une classe DAO à partir d'un template.
     * Assemble le contenu en générant chaque méthode individuellement via leurs templates respectifs.
     *
     * @param tableName Le nom de la table associée à ce DAO.
     * @param classNamePrefix Le préfixe du nom de classe (ex: "UserProfile").
     * @param packageName Le package Java de la classe DAO.
     * @param allColumns La liste de toutes les colonnes de la table.
     * @param primaryKeys La liste des colonnes de la clé primaire.
     * @param indexes La Map des index de la table.
     * @param outputDir Le répertoire de sortie où écrire le fichier .java.
     * @throws IOException Si une erreur de lecture de template ou d'écriture de fichier survient.
     */
    private void generateDaoFromTemplate(String tableName, String classNamePrefix, String packageName,
                             List<ColumnInfo> allColumns, List<ColumnInfo> primaryKeys,
                             Map<String, IndexInfo> indexes, Path outputDir) throws IOException {

        String daoClassName = classNamePrefix + "Dao";
        String dataPojoName = classNamePrefix + "Data";
        String mapRowMethodName = "mapRowTo" + dataPojoName;

        // Utilise un StringBuilder pour assembler le code de toutes les méthodes.
        StringBuilder methodsBlock = new StringBuilder();

        // --- Génération des Méthodes ---
        // Insert (toujours généré)
        methodsBlock.append(generateInsertMethodFromTemplate(tableName, dataPojoName, allColumns));

        // Méthodes basées sur PK (si PK existe)
        if (!primaryKeys.isEmpty()) {
            String pkPojoName = classNamePrefix + "PkData";
            methodsBlock.append(generateUpdateMethodFromTemplate(tableName, dataPojoName, allColumns, primaryKeys));
            methodsBlock.append(generateDeleteByPkMethodFromTemplate(tableName, pkPojoName, primaryKeys));
            methodsBlock.append(generateGetByPkMethodFromTemplate(tableName, dataPojoName, pkPojoName, primaryKeys, mapRowMethodName));
        } else {
            // Ajoute un commentaire si pas de PK
            methodsBlock.append(String.format("\n    // Note: Update, Delete by PK, Get by PK methods not generated because no primary key was found for table '%s'.\n", tableName));
        }

        // Méthodes basées sur les Index (Get, Delete, Exists)
        for (IndexInfo index : indexes.values()) {
             // Ne pas traiter si l'index n'a pas de colonnes (peu probable)
             // ou si c'est l'index implicite de la clé primaire (déjà géré par les méthodes PK).
             if (index.columnDbNames.isEmpty() || (index.indexName.equalsIgnoreCase("PRIMARY") && !primaryKeys.isEmpty())) {
                 continue;
             }

             // Recrée la liste ordonnée des ColumnInfo pour cet index
             List<ColumnInfo> indexColumns = allColumns.stream()
                 .filter(c -> index.columnDbNames.contains(c.dbName))
                 .sorted(Comparator.comparingInt(c -> new ArrayList<>(index.columnDbNames).indexOf(c.dbName)))
                 .collect(Collectors.toList());

             if (indexColumns.isEmpty()) continue; // Sécurité

             String indexPojoName = classNamePrefix + index.pojoNameSuffix + (index.isUnique ? "UniqueData" : "IdxData");
             String methodNameSuffix = index.methodNameSuffix; // Ex: ByUserIdEmail

             // Générer Get et Delete (spécifiques si unique)
             if (index.isUnique) {
                 methodsBlock.append(generateGetByUniqueIndexMethodFromTemplate(tableName, dataPojoName, indexPojoName, methodNameSuffix, indexColumns, mapRowMethodName, index.indexName));
                 methodsBlock.append(generateDeleteByUniqueIndexMethodFromTemplate(tableName, indexPojoName, methodNameSuffix, indexColumns, index.indexName));
             } else {
                 methodsBlock.append(generateGetByIndexMethodFromTemplate(tableName, dataPojoName, indexPojoName, methodNameSuffix, indexColumns, mapRowMethodName, index.indexName));
                 methodsBlock.append(generateDeleteByIndexMethodFromTemplate(tableName, indexPojoName, methodNameSuffix, indexColumns, index.indexName));
             }

             // Générer la méthode Exists (toujours utile)
             methodsBlock.append(generateExistsByIndexMethodFromTemplate(tableName, indexPojoName, methodNameSuffix, indexColumns, index.indexName));
        }

        // Générer la méthode utilitaire de mapping ResultSet -> POJO
        String mapRowMethodBlock = generateMapRowMethodFromTemplate(dataPojoName, allColumns, mapRowMethodName);

        // --- Assemblage final du DAO ---
        String daoTemplate = loadTemplate("dao_class.template");
        Map<String, String> daoValues = new HashMap<>();
        daoValues.put("packageName", packageName); // Package du DAO
        daoValues.put("pojoPackage", POJO_PACKAGE); // Package des POJOs (pour l'import)
        daoValues.put("daoClassName", daoClassName);
        daoValues.put("extra_imports", generateDaoExtraImports(allColumns)); // Imports additionnels (ex: BigDecimal)
        daoValues.put("methods_block", methodsBlock.toString()); // Contenu de toutes les méthodes générées
        daoValues.put("map_row_method_block", mapRowMethodBlock); // Contenu de la méthode de mapping

        // Remplace les placeholders et écrit le fichier DAO
        String generatedCode = replacePlaceholders(daoTemplate, daoValues);
        Path filePath = outputDir.resolve(daoClassName + ".java");
        writeFile(filePath, generatedCode);
        System.out.println("    -> DAO généré: " + filePath.getFileName());
    }


    // --- Méthodes de Génération Spécifiques (utilisant les templates) ---

    /** Génère le code pour la méthode 'insert'. */
    private String generateInsertMethodFromTemplate(String tableName, String dataPojoName, List<ColumnInfo> allColumns) throws IOException {
        String template = loadTemplate("dao_method_insert.template");
        Map<String, String> values = new HashMap<>();
        String columnsClause = allColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", "));
        String valuesClause = allColumns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnsClause, valuesClause);

        values.put("tableName", tableName);
        values.put("dataPojoName", dataPojoName);
        values.put("sqlQuery", sql);
        values.put("parameter_setting_block", generateParameterSettingBlock(allColumns, "data", "            ")); // Indentation 12 espaces

        return replacePlaceholders(template, values);
    }

    /** Génère le code pour la méthode 'update'. */
    private String generateUpdateMethodFromTemplate(String tableName, String dataPojoName, List<ColumnInfo> allColumns, List<ColumnInfo> primaryKeys) throws IOException {
         List<ColumnInfo> nonPkColumns = allColumns.stream().filter(c -> !c.isPrimaryKey).collect(Collectors.toList());
         // Ne génère pas de méthode update si toutes les colonnes font partie de la PK.
         if (nonPkColumns.isEmpty()) {
             return String.format("\n    // Note: Update method not generated as there are no non-primary key columns to update in table '%s'.\n", tableName);
         }
        String template = loadTemplate("dao_method_update.template");
        Map<String, String> values = new HashMap<>();
        String setClause = nonPkColumns.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(", "));
        String whereClause = primaryKeys.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
        String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, setClause, whereClause);

        // Le bloc de paramètres doit setter les colonnes non-PK puis les colonnes PK pour le WHERE.
        StringBuilder paramBlock = new StringBuilder();
        paramBlock.append(generateParameterSettingBlock(nonPkColumns, "data", "            ", 1)); // Commence à 1
        paramBlock.append("\n"); // Saut de ligne entre les deux blocs de set
        paramBlock.append(generateParameterSettingBlock(primaryKeys, "data", "            ", nonPkColumns.size() + 1)); // Continue la numérotation

        values.put("tableName", tableName);
        values.put("dataPojoName", dataPojoName);
        values.put("sqlQuery", sql);
        values.put("parameter_setting_block", paramBlock.toString());

        return replacePlaceholders(template, values);
    }

    /** Génère le code pour la méthode 'delete' par clé primaire. */
     private String generateDeleteByPkMethodFromTemplate(String tableName, String pkPojoName, List<ColumnInfo> primaryKeys) throws IOException {
         String template = loadTemplate("dao_method_delete_pk.template");
         Map<String, String> values = new HashMap<>();
         String whereClause = primaryKeys.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
         String sql = String.format("DELETE FROM %s WHERE %s", tableName, whereClause);

         values.put("tableName", tableName);
         values.put("pkPojoName", pkPojoName);
         values.put("sqlQuery", sql);
         values.put("parameter_setting_block", generateParameterSettingBlock(primaryKeys, "pkData", "            "));

         return replacePlaceholders(template, values);
     }

    /** Génère le code pour la méthode 'get' par clé primaire. */
     private String generateGetByPkMethodFromTemplate(String tableName, String dataPojoName, String pkPojoName, List<ColumnInfo> primaryKeys, String mapRowMethodName) throws IOException {
         String template = loadTemplate("dao_method_get_pk.template");
         Map<String, String> values = new HashMap<>();
         String whereClause = primaryKeys.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
         // SELECT * est simple, mais sélectionner explicitement les colonnes est souvent mieux.
         // String columnsClause = allColumns.stream().map(c -> c.dbName).collect(Collectors.joining(", "));
         // String sql = String.format("SELECT %s FROM %s WHERE %s", columnsClause, tableName, whereClause);
         String sql = String.format("SELECT * FROM %s WHERE %s", tableName, whereClause); // Version simple

         values.put("tableName", tableName);
         values.put("dataPojoName", dataPojoName);
         values.put("pkPojoName", pkPojoName);
         values.put("sqlQuery", sql);
         values.put("parameter_setting_block", generateParameterSettingBlock(primaryKeys, "pkData", "            "));
         values.put("mapRowMethodName", mapRowMethodName); // Nom de la méthode de mapping à appeler

         return replacePlaceholders(template, values);
     }

    /** Génère le code pour la méthode 'get' par index unique. */
    private String generateGetByUniqueIndexMethodFromTemplate(String tableName, String dataPojoName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String mapRowMethodName, String indexName) throws IOException {
         String template = loadTemplate("dao_method_get_unique.template");
         Map<String, String> values = new HashMap<>();
         String whereClause = indexColumns.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
         String sql = String.format("SELECT * FROM %s WHERE %s", tableName, whereClause);
         String methodName = "get" + methodNameSuffix; // Ex: getByUserIdEmail

         values.put("tableName", tableName);
         values.put("dataPojoName", dataPojoName);
         values.put("indexPojoName", indexPojoName);
         values.put("methodNameSuffix", methodNameSuffix); // Suffixe pour nom méthode (pas utilisé dans template?)
         values.put("methodName", methodName); // Nom complet de la méthode
         values.put("indexColumnsList", indexColumns.stream().map(c->c.dbName).collect(Collectors.joining(", "))); // Pour Javadoc
         values.put("indexName", indexName); // Pour Javadoc
         values.put("sqlQuery", sql);
         values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "uniData", "            ")); // Nom var: uniData
         values.put("mapRowMethodName", mapRowMethodName);

         return replacePlaceholders(template, values);
    }

    /** Génère le code pour la méthode 'delete' par index unique. */
    private String generateDeleteByUniqueIndexMethodFromTemplate(String tableName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String indexName) throws IOException {
        String template = loadTemplate("dao_method_delete_unique.template");
        Map<String, String> values = new HashMap<>();
        String whereClause = indexColumns.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
        String sql = String.format("DELETE FROM %s WHERE %s", tableName, whereClause);
        String methodName = "delete" + methodNameSuffix; // Ex: deleteByUserIdEmail

        values.put("tableName", tableName);
        values.put("indexPojoName", indexPojoName);
        values.put("methodNameSuffix", methodNameSuffix);
        values.put("methodName", methodName);
        values.put("indexColumnsList", indexColumns.stream().map(c->c.dbName).collect(Collectors.joining(", ")));
        values.put("indexName", indexName);
        values.put("sqlQuery", sql);
        values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "uniData", "            ")); // Nom var: uniData

        return replacePlaceholders(template, values);
    }

    /** Génère le code pour la méthode 'get' (tableau) par index non-unique. */
    private String generateGetByIndexMethodFromTemplate(String tableName, String dataPojoName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String mapRowMethodName, String indexName) throws IOException {
        String template = loadTemplate("dao_method_get_index.template");
        Map<String, String> values = new HashMap<>();
        String whereClause = indexColumns.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
        String sql = String.format("SELECT * FROM %s WHERE %s", tableName, whereClause);
        String methodName = "get" + methodNameSuffix; // Ex: getByStatus

        values.put("tableName", tableName);
        values.put("dataPojoName", dataPojoName);
        values.put("indexPojoName", indexPojoName);
        values.put("methodNameSuffix", methodNameSuffix);
        values.put("methodName", methodName);
        values.put("indexColumnsList", indexColumns.stream().map(c->c.dbName).collect(Collectors.joining(", ")));
        values.put("indexName", indexName);
        values.put("sqlQuery", sql);
        values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "idxData", "            ")); // Nom var: idxData
        values.put("mapRowMethodName", mapRowMethodName);

        return replacePlaceholders(template, values);
     }

    /** Génère le code pour la méthode 'delete' par index non-unique. */
     private String generateDeleteByIndexMethodFromTemplate(String tableName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String indexName) throws IOException {
         String template = loadTemplate("dao_method_delete_index.template");
         Map<String, String> values = new HashMap<>();
         String whereClause = indexColumns.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
         String sql = String.format("DELETE FROM %s WHERE %s", tableName, whereClause);
         String methodName = "delete" + methodNameSuffix; // Ex: deleteByStatus

         values.put("tableName", tableName);
         values.put("indexPojoName", indexPojoName);
         values.put("methodNameSuffix", methodNameSuffix);
         values.put("methodName", methodName);
         values.put("indexColumnsList", indexColumns.stream().map(c->c.dbName).collect(Collectors.joining(", ")));
         values.put("indexName", indexName);
         values.put("sqlQuery", sql);
         values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "idxData", "            ")); // Nom var: idxData

         return replacePlaceholders(template, values);
     }

    /** Génère le code pour la méthode 'exists' par index (unique ou non). */
    private String generateExistsByIndexMethodFromTemplate(String tableName, String indexPojoName, String methodNameSuffix, List<ColumnInfo> indexColumns, String indexName) throws IOException {
        String template = loadTemplate("dao_method_exists_by_index.template");
        Map<String, String> values = new HashMap<>();
        String whereClause = indexColumns.stream().map(c -> c.dbName + " = ?").collect(Collectors.joining(" AND "));
        // SELECT 1 est standard, LIMIT 1 est une optimisation courante
        String sql = String.format("SELECT 1 FROM %s WHERE %s LIMIT 1", tableName, whereClause);

        values.put("tableName", tableName);
        values.put("indexPojoName", indexPojoName);
        values.put("methodNameSuffix", methodNameSuffix); // Sera préfixé par 'exists' dans le template
        values.put("indexColumnsList", indexColumns.stream().map(c->c.dbName).collect(Collectors.joining(", ")));
        values.put("indexName", indexName);
        values.put("sqlQuery", sql); // Pas utilisé directement par template actuel, mais pourrait l'être
        values.put("where_clause", whereClause); // Ajouté pour le template dao_method_exists_by_index
        values.put("parameter_setting_block", generateParameterSettingBlock(indexColumns, "idxData", "            ")); // Nom var: idxData

        return replacePlaceholders(template, values);
    }

    /** Génère le code pour la méthode privée de mapping 'mapRowToXxxData'. */
     private String generateMapRowMethodFromTemplate(String dataPojoName, List<ColumnInfo> allColumns, String mapRowMethodName) throws IOException {
         String template = loadTemplate("dao_method_map_row.template");
         Map<String, String> values = new HashMap<>();

         // Construit le bloc de code pour mapper chaque colonne du ResultSet au champ POJO correspondant.
         StringBuilder mappingBlock = new StringBuilder();
         for (ColumnInfo col : allColumns) {
             // Utilise rs.getObject(colName, Type.class) pour une gestion robuste des NULLs SQL
             // vers les types wrapper Java (Integer, Long, Boolean, etc.).
             mappingBlock.append(String.format("        data.set%s(rs.getObject(\"%s\", %s.class)); // SQL Type: %d\n",
                                               NameUtils.toClassName(col.javaName), // setUserId
                                               col.dbName,                          // "user_id"
                                               col.javaType,                        // Integer
                                               col.sqlType));                       // java.sql.Types.INTEGER
         }

         values.put("dataPojoName", dataPojoName);
         values.put("mapRowMethodName", mapRowMethodName);
         values.put("resultSetMappingBlock", mappingBlock.toString());

         return replacePlaceholders(template, values);
     }


    // --- Helpers Généraux ---

    /** Génère la section des imports additionnels pour un DAO (ex: BigDecimal). */
    private String generateDaoExtraImports(List<ColumnInfo> columns) {
        Set<String> imports = new TreeSet<>();
        if (columns.stream().anyMatch(c -> "java.math.BigDecimal".equals(c.javaType))) {
            imports.add("import java.math.BigDecimal;");
        }
        // Ajouter d'autres imports nécessaires ici si besoin (ex: types spécifiques)
        return String.join("\n", imports) + (imports.isEmpty() ? "" : "\n");
    }

    /**
     * Génère un bloc de code Java pour setter les paramètres d'un PreparedStatement
     * à partir des valeurs d'un objet POJO.
     *
     * @param columns La liste des ColumnInfo correspondant aux paramètres à setter (dans l'ordre).
     * @param pojoVariableName Le nom de la variable Java contenant l'objet POJO source.
     * @param indentation La chaîne d'indentation à ajouter devant chaque ligne générée.
     * @return Le bloc de code généré (ex: "pstmt.setObject(1, data.getUserId());\n...")
     */
    private String generateParameterSettingBlock(List<ColumnInfo> columns, String pojoVariableName, String indentation) {
        // Par défaut, commence la numérotation des paramètres JDBC à 1.
        return generateParameterSettingBlock(columns, pojoVariableName, indentation, 1);
    }

    /**
     * Génère un bloc de code Java pour setter les paramètres d'un PreparedStatement
     * à partir des valeurs d'un objet POJO, en commençant à un index spécifié.
     *
     * @param columns La liste des ColumnInfo correspondant aux paramètres à setter (dans l'ordre).
     * @param pojoVariableName Le nom de la variable Java contenant l'objet POJO source.
     * @param indentation La chaîne d'indentation à ajouter devant chaque ligne générée.
     * @param startIndex L'index JDBC (base 1) du premier paramètre à setter.
     * @return Le bloc de code généré.
     */
    private String generateParameterSettingBlock(List<ColumnInfo> columns, String pojoVariableName, String indentation, int startIndex) {
        StringBuilder sb = new StringBuilder();
        int paramIndex = startIndex;
        for (ColumnInfo col : columns) {
            // Construit la ligne : indentation + pstmt.setObject(index, pojoVar.getNomGetter()); // Type: typeJava
            sb.append(String.format("%spstmt.setObject(%d, %s.get%s()); // Type: %s\n",
                                    indentation,
                                    paramIndex++,
                                    pojoVariableName,
                                    NameUtils.toClassName(col.javaName), // Getter name
                                    col.javaType));
        }
        // Enlève le dernier saut de ligne s'il existe pour éviter ligne vide à la fin du bloc.
        if (sb.length() > 0) {
             sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Écrit le contenu généré dans un fichier sur le disque.
     * Crée les répertoires parents si nécessaire.
     *
     * @param filePath Le chemin complet du fichier à écrire.
     * @param content Le contenu à écrire dans le fichier.
     * @throws IOException Si une erreur d'écriture survient.
     */
    private void writeFile(Path filePath, String content) throws IOException {
        try {
            // S'assure que le répertoire parent existe avant d'écrire.
            Files.createDirectories(filePath.getParent());
            // Écrit le fichier en utilisant l'encodage UTF-8.
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier généré: " + filePath);
            throw e;
        }
    }

    /**
     * Ferme la connexion à la base de données si elle est ouverte.
     * Logue un message d'erreur si la fermeture échoue.
     */
     private void closeConnection() {
         if (connection != null) {
             try {
                 connection.close();
                 System.out.println("\nConnexion à la base de données fermée.");
             } catch (SQLException e) {
                 // Log l'erreur mais ne la propage pas forcément (on est en fin de process).
                 System.err.println("Erreur lors de la fermeture de la connexion BDD: " + e.getMessage());
             } finally {
                 connection = null; // Aide le GC
             }
         }
     }

    /**
     * Point d'entrée principal du programme de génération.
     *
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        System.out.println(">>> Démarrage du générateur DAO/POJO <<<");
        long startTime = System.currentTimeMillis();
        try {
            DaoGenerator generator = new DaoGenerator();
            generator.generate();
        } catch (SQLException | IOException e) {
            // Erreur critique (connexion BDD, lecture config/template initiale)
            System.err.println("\n*** ERREUR CRITIQUE PENDANT LA GÉNÉRATION ***");
            e.printStackTrace();
            System.exit(1); // Quitte avec un code d'erreur
        } catch (Exception e) {
             // Autres erreurs inattendues
            System.err.println("\n*** ERREUR INATTENDUE PENDANT LA GÉNÉRATION ***");
            e.printStackTrace();
            System.exit(2);
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println(">>> Générateur terminé en " + (endTime - startTime) + " ms <<<");
        }
    }
}