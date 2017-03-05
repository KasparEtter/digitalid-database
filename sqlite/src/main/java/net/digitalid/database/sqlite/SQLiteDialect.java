package net.digitalid.database.sqlite;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.sql.SQLFraction;
import net.digitalid.database.dialect.SQLDialect;
import net.digitalid.database.dialect.SQLNode;
import net.digitalid.database.unit.Unit;

/**
 * This class implements the SQLite dialect.
 */
@Stateless
@GenerateSubclass
public abstract class SQLiteDialect extends SQLDialect {
    
    /**
     * Initializes the dialect.
     */
    @PureWithSideEffects
    @Initialize(target = SQLDialect.class)
    public static void initializeDialect() {
        SQLDialect.instance.set(new SQLiteDialectSubclass());
    }
    
    @Pure
    @Override
    @TODO(task = "Write the implementation.", date = "2017-03-05", author = Author.KASPAR_ETTER)
    public void unparse(@Nonnull SQLNode node, @Nonnull Unit unit, @NonCaptured @Modified @Nonnull @SQLFraction StringBuilder string) {
        
    }
    
//    /* -------------------------------------------------- Syntax -------------------------------------------------- */
//    
//    /**
//     * The pattern that valid database identifiers have to match.
//     */
//    private static final @Nonnull Pattern PATTERN = Pattern.compile("[a-z_][a-z0-9_]*", Pattern.CASE_INSENSITIVE);
//    
//    @Pure
//    @Override
//    public boolean isValidIdentifier(@Nonnull String identifier) {
//        return PATTERN.matcher(identifier).matches();
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String PRIMARY_KEY() {
//        return "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String TINYINT() {
//        return "TINYINT";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String BINARY() {
//        return "BINARY";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String NOCASE() {
//        return "NOCASE";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String CITEXT() {
//        return "TEXT";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String BLOB() {
//        return "BLOB";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String HASH() {
//        return "BLOB";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String VECTOR() {
//        return "BLOB";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String FLOAT() {
//        return "REAL";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String DOUBLE() {
//        return "REAL";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String REPLACE() {
//        return "REPLACE";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String IGNORE() {
//        return " OR IGNORE";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String GREATEST() {
//        return "MAX";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String CURRENT_TIME() {
//        return "CAST((JULIANDAY('NOW') - 2440587.5)*86400000 AS INTEGER)";
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String BOOLEAN(boolean value) {
//        return value ? "1" : "0";
//    }
//    
//    /* -------------------------------------------------- Index -------------------------------------------------- */
//    
//    @Pure
//    @Override
//    public @Nonnull String INDEX(@Nonnull String... columns) {
//        Require.that(columns.length > 0).orThrow("The columns are not empty.");
//        
//        return "";
//    }
//    
//    @Pure
//    @Locked
//    @Override
//    @SuppressWarnings("StringEquality")
//    public void createIndex(@Nonnull Statement statement, @Nonnull String table, @Nonnull String... columns) throws FailedUpdateExecutionException {
//        Require.that(columns.length > 0).orThrow("The columns are not empty.");
//        
//        final @Nonnull StringBuilder string = new StringBuilder("CREATE INDEX IF NOT EXISTS ").append(table).append("_index ON ").append(table).append(" (");
//        for (final @Nonnull String column : columns) {
//            if (column != columns[0]) { string.append(", "); }
//            string.append(column);
//        }
//        try { statement.executeUpdate(string.append(")").toString()); } catch (@Nonnull SQLException exception) { throw FailedUpdateExecutionException.get(exception); }
//    }
//    
//    /* -------------------------------------------------- Insertions -------------------------------------------------- */
//    
//    @Locked
//    @Override
//    @NonCommitting
//    protected long executeInsert(@Nonnull Statement statement, @Nonnull String SQL) throws FailedKeyGenerationException, FailedUpdateExecutionException {
//        try {
//            statement.executeUpdate(SQL);
//        } catch (@Nonnull SQLException exception) {
//            throw FailedUpdateExecutionException.get(exception);
//        }
//        
//        try (@Nonnull ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
//            if (resultSet.next()) { return resultSet.getLong(1); }
//            else { throw new SQLException("The given SQL statement did not generate any keys."); }
//        } catch (@Nonnull SQLException exception) {
//            throw FailedKeyGenerationException.get(exception);
//        }
//    }
//    
//    /* -------------------------------------------------- Locking -------------------------------------------------- */
//    
//    /**
//     * Stores a reentrant lock to serialize database access.
//     */
//    private final @Nonnull ReentrantLock lock = new ReentrantLock(true);
//    
//    @Override
//    protected void lock() {
//        lock.lock();
//    }
//    
//    @Override
//    protected void unlock() {
//        if (journalExists()) {
//            Log.warning("A database journal exists! The connection might not have been committed properly.", new Exception());
//        }
//        lock.unlock();
//    }
//    
//    @Pure
//    @Override
//    protected boolean isLocked() {
//        return lock.isHeldByCurrentThread();
//    }
//    
}
