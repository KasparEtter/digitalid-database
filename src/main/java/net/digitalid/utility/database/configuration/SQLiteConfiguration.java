package net.digitalid.utility.database.configuration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.exceptions.operation.FailedConnectionException;
import net.digitalid.utility.database.exceptions.operation.FailedKeyGenerationException;
import net.digitalid.utility.database.exceptions.operation.FailedUpdateException;
import net.digitalid.utility.system.directory.Directory;
import net.digitalid.utility.system.logger.Log;

/**
 * This class configures a SQLite database.
 */
@Immutable
public final class SQLiteConfiguration extends Configuration {
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns whether a SQLite database exists.
     * 
     * @return whether a SQLite database exists.
     */
    @Pure
    public static boolean exists() {
        return new File(Directory.getDataDirectory().getPath() + File.separator + "SQLite.db").exists();
    }
    
    /**
     * Returns whether a SQLite journal exists.
     * 
     * @return whether a SQLite journal exists.
     */
    @Pure
    public boolean journalExists() {
        return new File(Directory.getDataDirectory().getPath() + File.separator + name + ".db-journal").exists();
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Stores the name of the database.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the user and the password as properties.
     */
    private final @Nonnull Properties properties = new Properties();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new SQLite configuration with the given name.
     * 
     * @param name the name of the database file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     */
    @Committing
    private SQLiteConfiguration(@Nonnull @Validated String name, boolean reset) throws FailedConnectionException {
        super("org.sqlite.JDBC");
        
        assert Configuration.isValidName(name) : "The name is valid for a database.";
        
        this.name = name;
        if (reset) {
            Database.lock();
            dropDatabase();
            Database.unlock();
        }
        
        // TODO: The following code has to be called by the client (if at all):
        // new SQLiteConfig().setSharedCache(true);
    }
    
    /**
     * Creates a new SQLite configuration with the given name.
     * 
     * @param name the name of the database file (without the suffix).
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @return a new SQLite configuration with the given name and potential reset.
     */
    @Pure
    @Committing
    public static @Nonnull SQLiteConfiguration get(@Nonnull @Validated String name, boolean reset) throws FailedConnectionException {
        return new SQLiteConfiguration(name, reset);
    }
    
    /**
     * Creates a new SQLite configuration with the given name.
     * 
     * @param name the name of the database file (without the suffix).
     * 
     * @return a new SQLite configuration with the given name.
     */
    @Pure
    @Committing
    public static @Nonnull SQLiteConfiguration get(@Nonnull @Validated String name) throws FailedConnectionException {
        return new SQLiteConfiguration(name, false);
    }
    
    /**
     * Creates a new SQLite configuration with the default name.
     * 
     * @param reset whether the database is to be dropped first before creating it again.
     * 
     * @return a new SQLite configuration with the potential reset.
     */
    @Pure
    @Committing
    public static @Nonnull SQLiteConfiguration get(boolean reset) throws FailedConnectionException {
        return new SQLiteConfiguration("SQLite", reset);
    }
    
    /**
     * Creates a new SQLite configuration with the default name.
     * 
     * @return a new SQLite configuration with the default name.
     */
    @Pure
    @Committing
    public static @Nonnull SQLiteConfiguration get() throws FailedConnectionException {
        return new SQLiteConfiguration("SQLite", false);
    }
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    @Pure
    @Override
    protected @Nonnull String getURL() {
        return "jdbc:sqlite:" + Directory.getDataDirectory().getPath() + File.separator + name + ".db";
    }
    
    @Pure
    @Override
    protected @Nonnull Properties getProperties() {
        return properties;
    }
    
    @Pure
    @Override
    protected @Nonnull Connection getConnection() throws FailedConnectionException {
        try {
            final @Nonnull Connection connection = DriverManager.getConnection(getURL(), getProperties());
            connection.setAutoCommit(false);
            return connection;
        } catch (@Nonnull SQLException exception) {
            throw FailedConnectionException.get(exception);
        }
    }
    
    @Locked
    @Override
    public void dropDatabase() {
        assert Database.isLocked() : "The database is locked.";
        
        new File(Directory.getDataDirectory().getPath() + File.separator + name + ".db").delete();
        new File(Directory.getDataDirectory().getPath() + File.separator + name + ".db-journal").delete();
    }

    @Pure
    @Override
    public int getMaximumIdentifierLength() {
        return Integer.MAX_VALUE;
    }
    
    /* -------------------------------------------------- Syntax -------------------------------------------------- */
    
    /**
     * The pattern that valid database identifiers have to match.
     */
    private static final @Nonnull Pattern PATTERN = Pattern.compile("[a-z_][a-z0-9_]*", Pattern.CASE_INSENSITIVE);
    
    @Pure
    @Override
    public boolean isValidIdentifier(@Nonnull String identifier) {
        return PATTERN.matcher(identifier).matches();
    }
    
    @Pure
    @Override
    public @Nonnull String PRIMARY_KEY() {
        return "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT";
    }
    
    @Pure
    @Override
    public @Nonnull String TINYINT() {
        return "TINYINT";
    }
    
    @Pure
    @Override
    public @Nonnull String BINARY() {
        return "BINARY";
    }
    
    @Pure
    @Override
    public @Nonnull String NOCASE() {
        return "NOCASE";
    }
    
    @Pure
    @Override
    public @Nonnull String CITEXT() {
        return "TEXT";
    }
    
    @Pure
    @Override
    public @Nonnull String BLOB() {
        return "BLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String HASH() {
        return "BLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String VECTOR() {
        return "BLOB";
    }
    
    @Pure
    @Override
    public @Nonnull String FLOAT() {
        return "REAL";
    }
    
    @Pure
    @Override
    public @Nonnull String DOUBLE() {
        return "REAL";
    }
    
    @Pure
    @Override
    public @Nonnull String REPLACE() {
        return "REPLACE";
    }
    
    @Pure
    @Override
    public @Nonnull String IGNORE() {
        return " OR IGNORE";
    }
    
    @Pure
    @Override
    public @Nonnull String GREATEST() {
        return "MAX";
    }
    
    @Pure
    @Override
    public @Nonnull String CURRENT_TIME() {
        return "CAST((JULIANDAY('NOW') - 2440587.5)*86400000 AS INTEGER)";
    }
    
    @Pure
    @Override
    public @Nonnull String BOOLEAN(boolean value) {
        return value ? "1" : "0";
    }
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String INDEX(@Nonnull String... columns) {
        assert columns.length > 0 : "The columns are not empty.";
        
        return "";
    }
    
    @Pure
    @Locked
    @Override
    @SuppressWarnings("StringEquality")
    public void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws FailedUpdateException {
        assert columns.length > 0 : "The columns are not empty.";
        
        final @Nonnull StringBuilder string = new StringBuilder("CREATE INDEX IF NOT EXISTS ").append(table).append("_index ON ").append(table).append(" (");
        for (final @Nonnull String column : columns) {
            if (column != columns[0]) { string.append(", "); }
            string.append(column);
        }
        try { statement.executeUpdate(string.append(")").toString()); } catch (@Nonnull SQLException exception) { throw FailedUpdateException.get(exception); }
    }
    
    /* -------------------------------------------------- Supports -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean supportsBinaryStream() {
        return false;
    }
    
    /* -------------------------------------------------- Insertions -------------------------------------------------- */
    
    @Locked
    @Override
    @NonCommitting
    protected long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws FailedKeyGenerationException, FailedUpdateException {
        try {
            statement.executeUpdate(SQL);
        } catch (@Nonnull SQLException exception) {
            throw FailedUpdateException.get(exception);
        }
        
        try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (resultSet.next()) { return resultSet.getLong(1); }
            else { throw new SQLException("The given SQL statement did not generate any keys."); }
        } catch (@Nonnull SQLException exception) {
            throw FailedKeyGenerationException.get(exception);
        }
    }
    
    /* -------------------------------------------------- Locking -------------------------------------------------- */
    
    /**
     * Stores a reentrant lock to serialize database access.
     */
    private final @Nonnull ReentrantLock lock = new ReentrantLock(true);
    
    @Override
    protected void lock() {
        lock.lock();
    }
    
    @Override
    protected void unlock() {
        if (journalExists()) {
            Log.warning("A database journal exists! The connection might not have been committed properly.", new Exception());
        }
        lock.unlock();
    }
    
    @Pure
    @Override
    protected boolean isLocked() {
        return lock.isHeldByCurrentThread();
    }
    
}
