package net.digitalid.utility.database.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.system.console.Console;
import net.digitalid.utility.system.directory.Directory;

/**
 * This class configures a PostgreSQL database.
 */
@Immutable
public final class PostgreSQLConfiguration extends Configuration {
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns whether a PostgreSQL configuration exists.
     * 
     * @return whether a PostgreSQL configuration exists.
     */
    public static boolean exists() {
        return new File(Directory.getDataDirectory().getPath() + File.separator + "PostgreSQL.conf").exists();
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Stores the server address of the database.
     */
    private final @Nonnull String server;
    
    /**
     * Stores the port number of the database.
     */
    private final @Nonnull String port;
    
    /**
     * Stores the name of the database.
     */
    private final @Nonnull String database;
    
    /**
     * Stores the user of the database.
     */
    private final @Nonnull String user;
    
    /**
     * Stores the password of the database.
     */
    private final @Nonnull String password;
    
    /**
     * Stores the user and the password as properties.
     */
    private final @Nonnull Properties properties = new Properties();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new PostgreSQL configuration by reading the properties from the indicated file or from the user's input.
     * 
     * @param name the name of the database configuration file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     */
    @Committing
    private PostgreSQLConfiguration(@Nonnull @Validated String name, boolean reset) throws SQLException, IOException {
        super("org.postgresql.Driver");
        
        assert Configuration.isValidName(name) : "The name is valid for a database.";
        
        final @Nonnull File file = new File(Directory.getDataDirectory().getPath() + File.separator + name + ".conf");
        if (file.exists()) {
            try (@Nonnull FileInputStream stream = new FileInputStream(file); @Nonnull InputStreamReader reader = new InputStreamReader(stream, "UTF-8")) {
                properties.load(reader);
                server = properties.getProperty("Server", "localhost");
                port = properties.getProperty("Port", "5432");
                database = properties.getProperty("Database", "digitalid");
                user = properties.getProperty("User", "root");
                password = properties.getProperty("Password", "");
            }
        } else {
            Console.write();
            Console.write("The PostgreSQL database is not yet configured. Please provide the following information:");
            server = Console.readString("- Server (the default is \"localhost\"): ", "localhost");
            port = Console.readString("- Port (the default is 5432): ", "5432");
            database = Console.readString("- Database (the default is \"digitalid\"): ", "digitalid");
            user = Console.readString("- User (the default is \"root\"): ", "root");
            password = Console.readString("- Password (the default is empty): ", null);
            
            properties.setProperty("Server", server);
            properties.setProperty("Port", port);
            properties.setProperty("Database", database);
            properties.setProperty("User", user);
            properties.setProperty("Password", password);
            
            try (@Nonnull FileOutputStream stream = new FileOutputStream(file); @Nonnull OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {
                properties.store(writer, "Configuration of the PostgreSQL database");
            }
        }
        
        properties.clear();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        
        try (@Nonnull Connection connection = DriverManager.getConnection("jdbc:postgresql://" + server + ":" + port + "/", properties); @Nonnull Statement statement = connection.createStatement()) {
            if (reset) statement.executeUpdate("DROP DATABASE IF EXISTS " + database);
            final @Nonnull ResultSet resultSet = statement.executeQuery("SELECT EXISTS (SELECT * FROM pg_catalog.pg_database WHERE datname = '" + database + "')");
            if (resultSet.next() && !resultSet.getBoolean(1)) statement.executeUpdate("CREATE DATABASE " + database);
        }
    }
    
    /**
     * Creates a new PostgreSQL configuration by reading the properties from the indicated file or from the user's input.
     * 
     * @param name the name of the database configuration file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @return a new PostgreSQL configuration with the given name and potential reset.
     */
    @Pure
    @Committing
    public static @Nonnull PostgreSQLConfiguration get(@Nonnull @Validated String name, boolean reset) throws SQLException, IOException {
        return new PostgreSQLConfiguration(name, reset);
    }
    
    /**
     * Creates a new PostgreSQL configuration by reading the properties from the indicated file or from the user's input.
     * 
     * @param name the name of the database configuration file (without the suffix).
     * 
     * @return a new PostgreSQL configuration with the given name.
     */
    @Pure
    @Committing
    public static @Nonnull PostgreSQLConfiguration get(@Nonnull @Validated String name) throws SQLException, IOException {
        return new PostgreSQLConfiguration(name, false);
    }
    
    /**
     * Creates a new PostgreSQL configuration by reading the properties from the default file or from the user's input.
     * 
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @return a new PostgreSQL configuration with the potential reset.
     */
    @Pure
    @Committing
    public static @Nonnull PostgreSQLConfiguration get(boolean reset) throws SQLException, IOException {
        return new PostgreSQLConfiguration("PostgreSQL", reset);
    }
    
    /**
     * Creates a new PostgreSQL configuration by reading the properties from the default file or from the user's input.
     * 
     * @return a new PostgreSQL configuration.
     */
    @Pure
    @Committing
    public static @Nonnull PostgreSQLConfiguration get() throws SQLException, IOException {
        return new PostgreSQLConfiguration("PostgreSQL", false);
    }
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull String getURL() {
        return "jdbc:postgresql://" + server + ":" + port + "/" + database;
    }
    
    @Pure
    @Override
    protected @Nonnull Properties getProperties() {
        return properties;
    }
    
    @Locked
    @Override
    @Committing
    public void dropDatabase() throws SQLException {
        Database.close();
        try (@Nonnull Connection connection = DriverManager.getConnection("jdbc:postgresql://" + server + ":" + port + "/", properties); @Nonnull Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP DATABASE IF EXISTS " + database);
        }
        Database.commit();
    }

    @Pure
    @Override
    public int getMaximumIdentifierLength() {
        return 63;
    }
    
    /* -------------------------------------------------- Syntax -------------------------------------------------- */
    
    /**
     * The pattern that valid database identifiers have to match.
     */
    private static final @Nonnull Pattern PATTERN = Pattern.compile("[a-z_][a-z0-9_$]*", Pattern.CASE_INSENSITIVE);
    
    @Pure
    @Override
    public boolean isValidIdentifier(@Nonnull String identifier) {
        return identifier.length() <= 63 && PATTERN.matcher(identifier).matches();
    }
    
    @Pure
    @Override
    public @Nonnull String PRIMARY_KEY() {
        return "BIGSERIAL PRIMARY KEY";
    }
    
    @Pure
    @Override
    public @Nonnull String TINYINT() {
        return "SMALLINT";
    }
    
    @Pure
    @Override
    public @Nonnull String BINARY() {
        return "\"en_US.UTF-8\"";
    }
    
    @Pure
    @Override
    public @Nonnull String NOCASE() {
        return "\"en_US.UTF-8\"";
    }
    
    @Pure
    @Override
    public @Nonnull String CITEXT() {
        return "CITEXT";
    }
    
    @Pure
    @Override
    public @Nonnull String BLOB() {
        return "BYTEA";
    }
    
    @Pure
    @Override
    public @Nonnull String HASH() {
        return "BYTEA";
    }
    
    @Pure
    @Override
    public @Nonnull String VECTOR() {
        return "BYTEA";
    }
    
    @Pure
    @Override
    public @Nonnull String FLOAT() {
        return "FLOAT4";
    }
    
    @Pure
    @Override
    public @Nonnull String DOUBLE() {
        return "FLOAT8";
    }
    
    @Pure
    @Override
    public @Nonnull String REPLACE() {
        return "INSERT";
    }
    
    @Pure
    @Override
    public @Nonnull String IGNORE() {
        return "";
    }
    
    @Pure
    @Override
    public @Nonnull String GREATEST() {
        return "GREATEST";
    }
    
    @Pure
    @Override
    public @Nonnull String CURRENT_TIME() {
        return "ROUND(EXTRACT(EPOCH FROM CLOCK_TIMESTAMP()) * 1000)";
    }
    
    @Pure
    @Override
    public @Nonnull String BOOLEAN(boolean value) {
        return Boolean.toString(value);
    }
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String INDEX(@Nonnull String... columns) {
        assert columns.length > 0 : "The columns are not empty.";
        
        return "";
    }
    
    @Locked
    @Override
    @NonCommitting
    @SuppressWarnings("StringEquality")
    public void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException {
        assert columns.length > 0 : "The columns are not empty.";
        
        final @Nonnull StringBuilder string = new StringBuilder("DO $$ DECLARE counter INTEGER; BEGIN ");
        string.append("SELECT COUNT(*) INTO counter FROM pg_indexes WHERE schemaname = 'public' AND tablename = '").append(table).append("' AND indexname = '").append(table).append("_index").append("';");
        string.append("IF counter = 0 THEN EXECUTE 'CREATE INDEX ").append(table).append("_index ON ").append(table).append(" (");
        for (final @Nonnull String column : columns) {
            if (column != columns[0]) string.append(", ");
            string.append(column);
        }
        string.append(")'; END IF; END; $$");
        statement.execute(string.toString());
    }
    
    /* -------------------------------------------------- Savepoints -------------------------------------------------- */
    
    @Locked
    @Override
    @NonCommitting
    protected @Nonnull Savepoint setSavepoint(@Nonnull Connection connection) throws SQLException {
        return connection.setSavepoint();
    }
    
    @Locked
    @Override
    @NonCommitting
    protected void rollback(@Nonnull Connection connection, @Nullable Savepoint savepoint) throws SQLException {
        connection.rollback(savepoint);
        connection.releaseSavepoint(savepoint);
    }
    
    /* -------------------------------------------------- Ignoring -------------------------------------------------- */
    
    @Locked
    @Override
    @NonCommitting
    protected void onInsertIgnore(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException {
        assert columns.length > 0 : "The columns are not empty.";
        
        final @Nonnull StringBuilder string = new StringBuilder("CREATE OR REPLACE RULE ").append(table).append("_on_insert_ignore ");
        string.append("AS ON INSERT TO ").append(table).append(" WHERE EXISTS(SELECT 1 FROM ").append(table).append(" WHERE (");
        boolean first = true;
        for (@Nonnull String column : columns) {
            if (first) first = false;
            else string.append(", ");
            string.append(column);
        }
        string.append(") = (");
        first = true;
        for (@Nonnull String column : columns) {
            if (first) first = false;
            else string.append(", ");
            string.append("NEW.").append(column);
        }
        string.append(")) DO INSTEAD NOTHING");
        statement.executeUpdate(string.toString());
    }
    
    @Locked
    @Override
    @NonCommitting
    protected void onInsertNotIgnore(@Nonnull Statement statement, @Nonnull String table) throws SQLException {
        statement.executeUpdate("DROP RULE IF EXISTS " + table + "_on_insert_ignore ON " + table);
    }
    
    /* -------------------------------------------------- Updating -------------------------------------------------- */
    
    @Locked
    @Override
    @NonCommitting
    protected void onInsertUpdate(@Nonnull Statement statement, @Nonnull String table, int key, @Nonnull String... columns) throws SQLException {
        assert key > 0 : "The number of columns in the primary key is positive.";
        assert columns.length >= key : "At least as many columns as in the primary key are provided.";
        
        final @Nonnull StringBuilder string = new StringBuilder("CREATE OR REPLACE RULE ").append(table).append("_on_insert_update ");
        string.append("AS ON INSERT TO ").append(table).append(" WHERE EXISTS(SELECT 1 FROM ").append(table);
        
        final @Nonnull StringBuilder condition = new StringBuilder(" WHERE (");
        for (int i = 0; i < key; i++) {
            if (i > 0) condition.append(", ");
            condition.append(columns[i]);
        }
        condition.append(") = (");
        for (int i = 0; i < key; i++) {
            if (i > 0) condition.append(", ");
            condition.append("NEW.").append(columns[i]);
        }
        condition.append(")");
        
        string.append(condition).append(") DO INSTEAD UPDATE ").append(table).append(" SET ");
        for (int i = key; i < columns.length; i++) {
            if (i > key) string.append(", ");
            string.append(columns[i]).append(" = NEW.").append(columns[i]);
        }
        string.append(condition);
        statement.executeUpdate(string.toString());
    }
    
    @Locked
    @Override
    @NonCommitting
    protected void onInsertNotUpdate(@Nonnull Statement statement, @Nonnull String table) throws SQLException {
        statement.executeUpdate("DROP RULE IF EXISTS " + table + "_on_insert_update ON " + table);
    }
    
}
