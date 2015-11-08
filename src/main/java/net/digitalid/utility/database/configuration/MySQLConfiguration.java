package net.digitalid.utility.database.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.system.console.Console;
import net.digitalid.utility.system.directory.Directory;

/**
 * This class configures a MySQL database.
 */
@Immutable
public final class MySQLConfiguration extends Configuration {
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns whether a MySQL configuration exists.
     * 
     * @return whether a MySQL configuration exists.
     */
    @Pure
    public static boolean exists() {
        return new File(Directory.getDataDirectory().getPath() + File.separator + "MySQL.conf").exists();
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
     * Creates a new MySQL configuration by reading the properties from the indicated file or from the user's input.
     * 
     * @param name the name of the database configuration file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     */
    @Committing
    private MySQLConfiguration(@Nonnull @Validated String name, boolean reset) throws SQLException, IOException {
        super("com.mysql.jdbc.Driver");
        
        assert Configuration.isValidName(name) : "The name is valid for a database.";
        
        final @Nonnull File file = new File(Directory.getDataDirectory().getPath() + File.separator + name + ".conf");
        if (file.exists()) {
            try (@Nonnull FileInputStream stream = new FileInputStream(file); @Nonnull InputStreamReader reader = new InputStreamReader(stream, "UTF-8")) {
                properties.load(reader);
                server = properties.getProperty("Server", "localhost");
                port = properties.getProperty("Port", "3306");
                database = properties.getProperty("Database", "digitalid");
                user = properties.getProperty("User", "root");
                password = properties.getProperty("Password", "");
            }
        } else {
            Console.write();
            Console.write("The MySQL database is not yet configured. Please provide the following information:");
            server = Console.readString("- Server (the default is \"localhost\"): ", "localhost");
            port = Console.readString("- Port (the default is 3306): ", "3306");
            database = Console.readString("- Database (the default is \"digitalid\"): ", "digitalid");
            user = Console.readString("- User (the default is \"root\"): ", "root");
            password = Console.readString("- Password (the default is empty): ", null);
            
            properties.setProperty("Server", server);
            properties.setProperty("Port", port);
            properties.setProperty("Database", database);
            properties.setProperty("User", user);
            properties.setProperty("Password", password);
            
            try (@Nonnull FileOutputStream stream = new FileOutputStream(file); @Nonnull OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {
                properties.store(writer, "Configuration of the MySQL database");
            }
        }
        
        properties.clear();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        
        try (@Nonnull Connection connection = DriverManager.getConnection("jdbc:mysql://" + server + ":" + port, properties); @Nonnull Statement statement = connection.createStatement()) {
            if (reset) statement.executeUpdate("DROP DATABASE IF EXISTS " + database);
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database);
        }
    }
    
    /**
     * Creates a new MySQL configuration by reading the properties from the indicated file or from the user's input.
     * 
     * @param name the name of the database configuration file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @return a new MySQL configuration with the given name and potential reset.
     */
    @Pure
    @Committing
    public static @Nonnull MySQLConfiguration get(@Nonnull @Validated String name, boolean reset) throws SQLException, IOException {
        return new MySQLConfiguration(name, reset);
    }
    
    /**
     * Creates a new MySQL configuration by reading the properties from the indicated file or from the user's input.
     * 
     * @param name the name of the database configuration file (without the suffix).
     * 
     * @return a new MySQL configuration with the given name.
     */
    @Pure
    @Committing
    public static @Nonnull MySQLConfiguration get(@Nonnull @Validated String name) throws SQLException, IOException {
        return new MySQLConfiguration(name, false);
    }
    
    /**
     * Creates a new MySQL configuration by reading the properties from the default file or from the user's input.
     * 
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @return a new MySQL configuration with the potential reset.
     */
    @Pure
    @Committing
    public static @Nonnull MySQLConfiguration get(boolean reset) throws SQLException, IOException {
        return new MySQLConfiguration("MySQL", reset);
    }
    
    /**
     * Creates a new MySQL configuration by reading the properties from the default file or from the user's input.
     * 
     * @return a new MySQL configuration.
     */
    @Pure
    @Committing
    public static @Nonnull MySQLConfiguration get() throws SQLException, IOException {
        return new MySQLConfiguration("MySQL", false);
    }
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull String getURL() {
        return "jdbc:mysql://" + server + ":" + port + "/" + database + "?rewriteBatchedStatements=true";
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
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("DROP DATABASE IF EXISTS " + database);
        }
        Database.commit();
    }

    @Pure
    @Override
    public int getMaximumIdentifierLength() {
        return 64;
    }
    
    /* -------------------------------------------------- Syntax -------------------------------------------------- */
    
    /**
     * The pattern that valid database identifiers have to match.
     * Identifiers may in principle begin with a digit but unless quoted may not consist solely of digits.
     */
    private static final @Nonnull Pattern PATTERN = Pattern.compile("[a-z_][a-z0-9_$]*", Pattern.CASE_INSENSITIVE);
    
    @Pure
    @Override
    public boolean isValidIdentifier(@Nonnull String identifier) {
        return identifier.length() <= 64 && PATTERN.matcher(identifier).matches();
    }
    
    @Pure
    @Override
    public @Nonnull String PRIMARY_KEY() {
        return "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY";
    }
    
    @Pure
    @Override
    public @Nonnull String TINYINT() {
        return "TINYINT";
    }
    
    @Pure
    @Override
    public @Nonnull String BINARY() {
        return "utf16_bin";
    }
    
    @Pure
    @Override
    public @Nonnull String NOCASE() {
        return "utf16_general_ci";
    }
    
    @Pure
    @Override
    public @Nonnull String CITEXT() {
        return "TEXT";
    }
    
    @Pure
    @Override
    public @Nonnull String BLOB() {
        return "LONGBLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String HASH() {
        return "BINARY(33)";
    }
    
    @Pure
    @Override
    public @Nonnull String VECTOR() {
        return "BINARY(17)";
    }
    
    @Pure
    @Override
    public @Nonnull String FLOAT() {
        return "FLOAT";
    }
    
    @Pure
    @Override
    public @Nonnull String DOUBLE() {
        return "DOUBLE";
    }
    
    @Pure
    @Override
    public @Nonnull String REPLACE() {
        return "REPLACE";
    }
    
    @Pure
    @Override
    public @Nonnull String IGNORE() {
        return " IGNORE";
    }
    
    @Pure
    @Override
    public @Nonnull String GREATEST() {
        return "GREATEST";
    }
    
    @Pure
    @Override
    public @Nonnull String CURRENT_TIME() {
        return "UNIX_TIMESTAMP(SYSDATE()) * 1000 + MICROSECOND(SYSDATE(3)) DIV 1000";
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
        
        final @Nonnull StringBuilder string = new StringBuilder(", INDEX(");
        for (final @Nonnull String column : columns) {
            if (string.length() != 8) string.append(", ");
            string.append(column);
        }
        return string.append(")").toString();
    }
    
    @Pure
    @Override
    public void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) {
        assert columns.length > 0 : "The columns are not empty.";
    }
    
}
