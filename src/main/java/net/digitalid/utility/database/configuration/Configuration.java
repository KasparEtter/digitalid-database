package net.digitalid.utility.database.configuration;

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
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.system.logger.Log;

/**
 * This class is used to configure various databases.
 * 
 * TODO: Split this class into various subconfigurations.
 * 
 * @see MySQLConfiguration
 * @see PostgreSQLConfiguration
 * @see SQLiteConfiguration
 * 
 * @see Database
 */
@Immutable
public abstract class Configuration {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * The pattern that valid database names have to match.
     */
    private static final @Nonnull Pattern PATTERN = Pattern.compile("[a-z0-9_]+", Pattern.CASE_INSENSITIVE);
    
    /**
     * Returns whether the given name is valid for a database.
     * 
     * @param name the database name to check for validity.
     * 
     * @return whether the given name is valid for a database.
     */
    @Pure
    public static boolean isValidName(@Nonnull String name) {
        return name.length() <= 40 && PATTERN.matcher(name).matches();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new configuration with the given driver.
     * 
     * @param driver the JDBC driver of this configuration.
     */
    @Committing
    protected Configuration(@Nonnull String driver) throws SQLException {
        try {
            Class.forName(driver);
        } catch (@Nonnull ClassNotFoundException exception) {
            throw new InitializationError("Could not load the database driver.", exception);
        }
    }
    
    /* -------------------------------------------------- Database -------------------------------------------------- */
    
    /**
     * Returns the database URL of this configuration.
     * 
     * @return the database URL of this configuration.
     */
    @Pure
    protected abstract @Nonnull String getURL();
    
    /**
     * Returns the properties of this configuration.
     * <p>
     * <em>Important:</em> Do not modify them!
     * 
     * @return the properties of this configuration.
     */
    @Pure
    protected abstract @Nonnull Properties getProperties();
    
    /**
     * Returns a new connection to the database.
     * 
     * @return a new connection to the database.
     */
    @Pure
    protected @Nonnull Connection getConnection() throws SQLException {
        final @Nonnull Connection connection = DriverManager.getConnection(getURL(), getProperties());
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        connection.setAutoCommit(false);
        return connection;
    }
    
    /**
     * Drops the configured database.
     */
    @Locked
    @Committing
    public abstract void dropDatabase() throws SQLException;
    
    /**
     * Returns the open connection to the database that is associated with the current thread.
     * <p>
     * <em>Important:</em> Do not commit or close the connection as it will be reused later on!
     * 
     * @return the open connection to the database that is associated with the current thread.
     */
    @Pure
    @Locked
    @NonCommitting
    protected final static @Nonnull Connection getCurrentConnection() throws SQLException {
        return Database.getConnection();
    }

    /**
     * Returns the maximum length that a database identifier can have.
     * @return
     */
    @Pure
    public abstract int getMaximumIdentifierLength();
   
    /* -------------------------------------------------- Syntax -------------------------------------------------- */
    
    /**
     * Returns whether the given identifier is valid.
     * 
     * @param identifier the string to be checked.
     * 
     * @return whether the given identifier is valid.
     */
    @Pure
    public abstract boolean isValidIdentifier(@Nonnull String identifier);
    
    /**
     * Returns the syntax for declaring an auto-incrementing primary key.
     * 
     * @return the syntax for declaring an auto-incrementing primary key.
     */
    @Pure
    public abstract @Nonnull String PRIMARY_KEY();
    
    /**
     * Returns the syntax for declaring a tiny integer.
     * 
     * @return the syntax for declaring a tiny integer.
     */
    @Pure
    public abstract @Nonnull String TINYINT();
    
    /**
     * Returns the syntax for declaring a binary collation.
     * 
     * @return the syntax for declaring a binary collation.
     */
    @Pure
    public abstract @Nonnull String BINARY();
    
    /**
     * Returns the syntax for declaring a case-insensitive collation.
     * 
     * @return the syntax for declaring a case-insensitive collation.
     */
    @Pure
    public abstract @Nonnull String NOCASE();
    
    /**
     * Returns the syntax for declaring a case-insensitive text.
     * 
     * @return the syntax for declaring a case-insensitive text.
     */
    @Pure
    public abstract @Nonnull String CITEXT();
    
    /**
     * Returns the syntax for declaring a binary large object.
     * 
     * @return the syntax for declaring a binary large object.
     */
    @Pure
    public abstract @Nonnull String BLOB();
    
    /**
     * Returns the syntax for declaring a 256-bit hash.
     * 
     * @return the syntax for declaring a 256-bit hash.
     */
    @Pure
    public abstract @Nonnull String HASH();
    
    /**
     * Returns the syntax for declaring a 128-bit vector.
     * 
     * @return the syntax for declaring a 128-bit vector.
     */
    @Pure
    public abstract @Nonnull String VECTOR();
    
    /**
     * Returns the syntax for declaring a 32-bit float.
     * 
     * @return the syntax for declaring a 32-bit float.
     */
    @Pure
    public abstract @Nonnull String FLOAT();
    
    /**
     * Returns the syntax for declaring a 64-bit float.
     * 
     * @return the syntax for declaring a 64-bit float.
     */
    @Pure
    public abstract @Nonnull String DOUBLE();
    
    /**
     * Returns the syntax for replacing existing entries during inserts.
     * 
     * @return the syntax for replacing existing entries during inserts.
     */
    @Pure
    public abstract @Nonnull String REPLACE();
    
    /**
     * Returns the syntax for ignoring database errors during updates.
     * 
     * @return the syntax for ignoring database errors during updates.
     */
    @Pure
    public abstract @Nonnull String IGNORE();
    
    /**
     * Returns the syntax for retrieving the greatest argument.
     * 
     * @return the syntax for retrieving the greatest argument.
     */
    @Pure
    public abstract @Nonnull String GREATEST();
    
    /**
     * Returns the syntax for retrieving the current time in milliseconds.
     * 
     * @return the syntax for retrieving the current time in milliseconds.
     */
    @Pure
    public abstract @Nonnull String CURRENT_TIME();
    
    /**
     * Returns the syntax for storing a boolean value.
     * 
     * @return the syntax for storing a boolean value.
     */
    @Pure
    public abstract @Nonnull String BOOLEAN(boolean value);
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the syntax for creating an index inside a table declaration.
     * 
     * @param columns the columns for which the index is to be created.
     * 
     * @return the syntax for creating an index inside a table declaration.
     * 
     * @require columns.length > 0 : "The columns are not empty.";
     */
    @Pure
    public abstract @Nonnull String INDEX(@Nonnull String... columns);
    
    /**
     * Creates an index outside a table declaration or does nothing.
     * 
     * @param statement the statement on which the creation is executed.
     * @param table the table on whose columns the index is to be created.
     * @param columns the columns for which the index is to be created.
     * 
     * @require columns.length > 0 : "The columns are not empty.";
     */
    @NonCommitting
    public abstract void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException;
    
    /* -------------------------------------------------- Supports -------------------------------------------------- */
    
    /**
     * Returns whether binary streams are supported.
     * 
     * @return the syntax for retrieving the current time in milliseconds.
     */
    @Pure
    public boolean supportsBinaryStream() {
        return true;
    }
    
    /* -------------------------------------------------- Insertions -------------------------------------------------- */
    
    /**
     * Executes the given insertion and returns the generated key.
     * 
     * @param statement a statement to execute the insertion.
     * @param SQL an SQL statement that inserts an entry.
     * 
     * @return the key generated for the inserted entry.
     */
    @NonCommitting
    protected long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws SQLException {
        statement.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
        try (@Nonnull ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) { return resultSet.getLong(1); }
            else { throw new SQLException("The given SQL statement did not generate a key."); }
        }
    }
    
    /* -------------------------------------------------- Savepoints -------------------------------------------------- */
    
    /**
     * Returns a savepoint for the given connection or null if not supported or required.
     * 
     * @param connection the connection for which a savepoint is to be returned.
     * 
     * @return a savepoint for the given connection or null if not supported or required.
     */
    @Locked
    @NonCommitting
    protected @Nullable Savepoint setSavepoint(@Nonnull Connection connection) throws SQLException {
        return null;
    }
    
    /**
     * Rolls back the given connection to the given savepoint and releases the savepoint afterwards.
     * 
     * @param connection the connection which is to be rolled back and whose savepoint is to be released.
     * @param savepoint the savepoint to roll the connection back to or null if not supported or required.
     */
    @Locked
    @NonCommitting
    protected void rollback(@Nonnull Connection connection, @Nullable Savepoint savepoint) throws SQLException {}
    
    
    /* -------------------------------------------------- Ignoring -------------------------------------------------- */
    
    /**
     * Creates a rule to ignore duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param columns the columns of the primary key.
     * 
     * @require columns.length > 0 : "The columns are not empty.";
     */
    @Locked
    @NonCommitting
    protected void onInsertIgnore(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws SQLException {}
    
    /**
     * Drops the rule to ignore duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @Locked
    @NonCommitting
    protected void onInsertNotIgnore(@Nonnull Statement statement, @Nonnull String table) throws SQLException {}
    
    
    /* -------------------------------------------------- Updating -------------------------------------------------- */
    
    /**
     * Creates a rule to update duplicate insertions.
     * 
     * @param statement a statement to create the rule with.
     * @param table the table to which the rule is applied.
     * @param key the number of columns in the primary key.
     * @param columns the columns which are inserted starting with the columns of the primary key.
     * 
     * @require columns.length >= key : "At least as many columns as in the primary key are provided.";
     */
    @Locked
    @NonCommitting
    protected void onInsertUpdate(@Nonnull Statement statement, @Nonnull String table, @Positive int key, @Nonnull String... columns) throws SQLException {}
    
    /**
     * Drops the rule to update duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @Locked
    @NonCommitting
    protected void onInsertNotUpdate(@Nonnull Statement statement, @Nonnull String table) throws SQLException {}
    
    
    /* -------------------------------------------------- Locking -------------------------------------------------- */
    
    /**
     * Stores a lock counter that is associated with the current thread.
     * This variable is needed for databases that do not require locking.
     */
    protected static final @Nonnull ThreadLocal<Integer> lockCounter = new ThreadLocal<Integer>() {
        @Override protected @Nullable Integer initialValue() { return 0; }
    };
    
    /**
     * Locks the database if its access should be serialized.
     */
    protected void lock() throws SQLException {
        lockCounter.set(lockCounter.get() + 1);
        
        if (!Database.getConnection().isValid(1)) {
            Log.warning("The connection is no longer valid and is thus replaced.");
            Database.connection.remove();
        }
    }
    
    /**
     * Unlocks the database if its access has been serialized.
     */
    protected void unlock() {
        int value = lockCounter.get();
        assert value > 0 : "The lock was released more often than it was acquired.";
        lockCounter.set(value - 1);
    }
    
    /**
     * Returns whether the database is locked by the current thread.
     * 
     * @return whether the database is locked by the current thread.
     */
    @Pure
    protected boolean isLocked() {
        return lockCounter.get() > 0;
    }
    
}
