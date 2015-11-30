package net.digitalid.database.core.configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.annotations.Locked;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.operation.FailedClosingException;
import net.digitalid.database.core.exceptions.operation.FailedCommitException;
import net.digitalid.database.core.exceptions.operation.FailedConnectionException;
import net.digitalid.database.core.exceptions.operation.FailedOperationException;
import net.digitalid.database.core.exceptions.operation.noncommitting.FailedKeyGenerationException;
import net.digitalid.database.core.exceptions.operation.noncommitting.FailedPreparedStatementCreationException;
import net.digitalid.database.core.exceptions.operation.noncommitting.FailedSavepointCreationException;
import net.digitalid.database.core.exceptions.operation.noncommitting.FailedSavepointRollbackException;
import net.digitalid.database.core.exceptions.operation.noncommitting.FailedStatementCreationException;
import net.digitalid.database.core.exceptions.operation.noncommitting.FailedUpdateExecutionException;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.state.Initialized;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Stateless;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.system.logger.Log;
import net.digitalid.utility.system.thread.Threading;

/**
 * This class provides connections to the database.
 * <p>
 * <em>Important:</em> The table names without the prefix may consist of at most 22 characters!
 * Moreover, if a host is run with SQLite as database, its identifier may not contain a hyphen.
 * 
 * @see Configuration
 */
@Stateless
public final class Database {
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the configuration of the database.
     */
    private static @Nullable Configuration configuration;
    
    /**
     * Returns the configuration of the database.
     * <p>
     * <em>Important:</em> Do not store the configuration
     * permanently because it may change during testing!
     * 
     * @return the configuration of the database.
     */
    @Pure
    @Initialized
    public static @Nonnull Configuration getConfiguration() {
        assert configuration != null : "The database is initialized.";
        
        return configuration;
    }
    
    /* -------------------------------------------------- Single-Access -------------------------------------------------- */
    
    /**
     * Stores whether the database is set up for single-access.
     * In case of single-access, only one process accesses the
     * database, which allows to keep the objects in memory up
     * to date with no need to reload them all the time.
     * (Clients on hosts are run in multi-access mode.)
     */
    private static boolean singleAccess;
    
    /**
     * Returns whether the database is set up for single-access.
     * 
     * @return whether the database is set up for single-access.
     */
    @Pure
    public static boolean isSingleAccess() {
        return singleAccess;
    }
    
    /**
     * Returns whether the database is set up for multi-access.
     * 
     * @return whether the database is set up for multi-access.
     */
    @Pure
    public static boolean isMultiAccess() {
        return !singleAccess;
    }
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    /**
     * Initializes the database with the given configuration.
     * 
     * @param configuration the configuration of the database.
     * @param singleAccess whether the database is accessed by a single process.
     */
    public static void initialize(@Nonnull Configuration configuration, boolean singleAccess) {
        Database.configuration = configuration;
        Database.singleAccess = singleAccess;
        connection.remove();
        
        Log.information("The database has been initialized for " + (singleAccess ? "single" : "multi") + "-access with a " + configuration.getClass().getSimpleName() + ".");
    }
    
    /**
     * Initializes the database with the given configuration.
     * 
     * @param configuration the configuration of the database.
     */
    public static void initialize(@Nonnull Configuration configuration) {
        initialize(configuration, true);
    }
    
    /**
     * Returns whether the database has been initialized.
     * 
     * @return whether the database has been initialized.
     */
    @Pure
    public static boolean isInitialized() { return configuration != null; }
    
    /* -------------------------------------------------- Connection -------------------------------------------------- */
    
    /**
     * TODO: Instead of a second thread local static field, make the other one a pair, where either the connection or the SQLException is set.
     */
    private static final @Nonnull ThreadLocal<FailedConnectionException> problem = new ThreadLocal<FailedConnectionException>() {
        @Override protected @Nullable FailedConnectionException initialValue() { return null; }
    };
    
    /**
     * Stores the open connection to the database that is associated with the current thread.
     */
    private static final @Nonnull ThreadLocal<Connection> connection = new ThreadLocal<Connection>() {
        @Override protected @Nullable Connection initialValue() {
            assert configuration != null : "The database is initialized.";
            try {
                return configuration.getConnection();
            } catch (@Nonnull FailedConnectionException exception) {
                problem.set(exception);
                return null;
            }
        }
    };
    
    /**
     * Checks that the connection to the database is still valid.
     * 
     * @param recurse whether the check is to be repeated or not.
     */
    @Pure
    @Locked
    @Initialized
    @NonCommitting
    static void checkConnection(boolean recurse) throws FailedConnectionException {
        final @Nullable Connection connection = Database.connection.get();
        
        if (connection == null) {
            Database.connection.remove();
            throw Database.problem.get();
        }
        
        try {
            if (!connection.isValid(1)) {
                Log.information("The database connection is no longer valid and is thus replaced.");
                Database.connection.remove();
                if (recurse) { Database.checkConnection(false); }
                else { throw new SQLException("The database connection remains invalid."); }
            }
        } catch (@Nonnull SQLException exception) {
            throw FailedConnectionException.get(exception);
        }
    }
    
    /**
     * Returns the open connection to the database that is associated with the current thread.
     * <p>
     * <em>Important:</em> Do not commit or close the connection as it will be reused later on!
     * 
     * @return the open connection to the database that is associated with the current thread.
     */
    @Pure
    @Locked
    @Initialized
    @NonCommitting
    static @Nonnull Connection getConnection() {
        assert Database.isLocked() : "The database is locked.";
        
        final @Nullable Connection connection = Database.connection.get();
        if (connection != null) {
            return connection;
        } else {
            Database.connection.remove();
            throw ShouldNeverHappenError.get("The connection should have been checked by the locking method.");
        }
    }
    
    /* -------------------------------------------------- Transactions -------------------------------------------------- */
    
    /**
     * Commits all changes of the current thread since the last commit or rollback.
     * (On the server, this method should only be called by the worker.)
     */
    @Locked
    @Committing
    @Initialized
    public static void commit() throws FailedCommitException {
        assert Database.isLocked() : "The database is locked.";
        
        try {
            getConnection().commit();
        } catch (@Nonnull SQLException exception) {
            throw FailedCommitException.get(exception);
        }
    }
    
    /**
     * Rolls back all changes of the current thread since the last commit or rollback.
     * (On the server, this method should only be called by the worker.)
     */
    @Locked
    @Committing
    @Initialized
    public static void rollback() {
        assert Database.isLocked() : "The database is locked.";
        
        try {
            getConnection().rollback();
        } catch (@Nonnull SQLException exception) {
            Log.error("Could not roll back the transaction.", exception);
        }
    }
    
    /**
     * Closes the connection of the current thread.
     */
    @Committing
    @Initialized
    static void close() throws FailedClosingException {
        try {
            getConnection().close();
        } catch (@Nonnull SQLException exception) {
            throw FailedClosingException.get(exception);
        }
    }
    
    /* -------------------------------------------------- Savepoints -------------------------------------------------- */
    
    /**
     * Returns a savepoint for the connection of the current thread or null if not supported or required.
     * 
     * @return a savepoint for the connection of the current thread or null if not supported or required.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nullable Savepoint setSavepoint() throws FailedSavepointCreationException {
        return getConfiguration().setSavepoint(getConnection());
    }
    
    /**
     * Rolls back the connection of the current thread to the given savepoint and releases the savepoint afterwards.
     * 
     * @param savepoint the savepoint to roll the connection back to or null if not supported or required.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void rollback(@Nullable Savepoint savepoint) throws FailedSavepointRollbackException {
        getConfiguration().rollback(getConnection(), savepoint);
    }
    
    /* -------------------------------------------------- Statements -------------------------------------------------- */
    
    /**
     * Creates a new statement on the connection of the current thread.
     * 
     * @return a new statement on the connection of the current thread.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nonnull Statement createStatement() throws FailedStatementCreationException {
        try {
            return getConnection().createStatement();
        } catch (@Nonnull SQLException exception) {
            throw FailedStatementCreationException.get(exception);
        }
    }
    
    /**
     * Prepares the statement on the connection of the current thread.
     * 
     * @param SQL the statement which is to be prepared for later use.
     * 
     * @return a new statement on the connection of the current thread.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nonnull PreparedStatement prepareStatement(@Nonnull String SQL) throws FailedPreparedStatementCreationException {
        try {
            return getConnection().prepareStatement(SQL);
        } catch (@Nonnull SQLException exception) {
            throw FailedPreparedStatementCreationException.get(exception);
        }
    }
    
    /* -------------------------------------------------- Conversions -------------------------------------------------- */
    
    /**
     * Returns the syntax for storing a boolean value.
     * 
     * @param value the value which is to be stored.
     * 
     * @return the syntax for storing a boolean value.
     */
    @Pure
    @Initialized
    public static @Nonnull String toBoolean(boolean value) {
        return getConfiguration().BOOLEAN(value);
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
    @Locked
    @Initialized
    @NonCommitting
    public static long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws FailedKeyGenerationException, FailedUpdateExecutionException {
        return getConfiguration().executeInsert(statement, SQL);
    }
    
    /**
     * Returns a prepared statement that can be used to insert values and retrieve their key.
     * 
     * @param SQL the insert statement which is to be prepared for returning the generated keys.
     * 
     * @return a prepared statement that can be used to insert values and retrieve their key.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static @Nonnull PreparedStatement prepareInsertStatement(@Nonnull String SQL) throws FailedPreparedStatementCreationException {
        try {
            return getConnection().prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        } catch (@Nonnull SQLException exception) {
            throw FailedPreparedStatementCreationException.get(exception);
        }
    }
    
    /**
     * Returns the key generated by the given prepared statement.
     * 
     * @param preparedStatement an executed prepared statement that has generated a key.
     * 
     * @return the key generated by the given prepared statement.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static long getGeneratedKey(@Nonnull PreparedStatement preparedStatement) throws FailedKeyGenerationException {
        try (@Nonnull ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) { return resultSet.getLong(1); }
            else { throw new SQLException("The given SQL statement did not generate a key."); }
        } catch (@Nonnull SQLException exception) {
            throw FailedKeyGenerationException.get(exception);
        }
    }
    
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
    @Initialized
    @NonCommitting
    public static void onInsertIgnore(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws FailedUpdateExecutionException {
        assert columns.length > 0 : "The columns are not empty.";
        
        getConfiguration().onInsertIgnore(statement, table, columns);
    }
    
    /**
     * Drops the rule to ignore duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void onInsertNotIgnore(@Nonnull Statement statement, @Nonnull String table) throws FailedUpdateExecutionException {
        getConfiguration().onInsertNotIgnore(statement, table);
    }
    
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
    @Initialized
    @NonCommitting
    public static void onInsertUpdate(@Nonnull Statement statement, @Nonnull String table, @Positive int key, @Nonnull String... columns) throws FailedUpdateExecutionException {
        getConfiguration().onInsertUpdate(statement, table, key, columns);
    }
    
    /**
     * Drops the rule to update duplicate insertions.
     * 
     * @param statement a statement to drop the rule with.
     * @param table the table from which the rule is dropped.
     */
    @Locked
    @Initialized
    @NonCommitting
    public static void onInsertNotUpdate(@Nonnull Statement statement, @Nonnull String table) throws FailedUpdateExecutionException {
        getConfiguration().onInsertNotUpdate(statement, table);
    }
    
    /* -------------------------------------------------- Locking -------------------------------------------------- */
    
    /**
     * Locks the database if its access should be serialized.
     */
    @Initialized
    public static void lock() throws FailedConnectionException {
        getConfiguration().lock();
    }
    
    /**
     * Unlocks the database if its access has been serialized.
     */
    @Initialized
    public static void unlock() {
        getConfiguration().unlock();
    }
    
    /**
     * Returns whether the database is locked by the current thread.
     * It is safe to assume that the database is locked for methods
     * that run on the {@link Threading#isMainThread() main thread}.
     * 
     * @return whether the database is locked by the current thread.
     */
    @Pure
    @Initialized
    public static boolean isLocked() {
        return getConfiguration().isLocked();
    }
    
    /* -------------------------------------------------- Purging -------------------------------------------------- */
    
    /**
     * Stores the timer to schedule tasks.
     */
    private static final @Nonnull Timer timer = new Timer();
    
    /**
     * Stores the tables which are to be purged regularly.
     */
    private static final @Nonnull ConcurrentMap<String, Long> tables = new ConcurrentHashMap<>();
    
    /**
     * Adds the given table to the list for regular purging.
     * 
     * @param table the name of the table which is to be purged regularly.
     * @param time the time after which entries in the given table can be purged.
     */
    public static void addRegularPurging(@Nonnull String table, long time) {
        tables.put(table, time);
    }
    
    /**
     * Removes the given table from the list for regular purging.
     * 
     * @param table the name of the table which is no longer to be purged.
     */
    public static void removeRegularPurging(@Nonnull String table) {
        tables.remove(table);
    }
    
    /**
     * Starts the timer for purging.
     */
    public static void startPurging() {
        // TODO: If several processes access the database, it's enough when one of them does the purging.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Database.lock();
                    try (@Nonnull Statement statement = createStatement()) {
                        final long time = System.currentTimeMillis();
                        for (final @Nonnull Map.Entry<String, Long> entry : tables.entrySet()) {
                            statement.executeUpdate("DELETE FROM " + entry.getKey() + " WHERE time < " + (time - entry.getValue()));
                            commit();
                        }
                    }
                } catch (@Nonnull FailedOperationException | SQLException exception) {
                    Log.warning("Could not prune a table.", exception);
                    rollback();
                } finally {
                    Database.unlock();
                }
            }
        }, 60_000l, 3_600_000l);
    }
    
    /**
     * Stops the timer for purging.
     */
    public static void stopPurging() {
        timer.cancel();
    }
    
}
