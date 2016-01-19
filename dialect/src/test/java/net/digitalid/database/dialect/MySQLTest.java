package net.digitalid.database.dialect;

import java.io.IOException;

import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.database.core.configuration.MySQLConfiguration;

import org.junit.BeforeClass;

/**
 * Unit testing of the {@link Database} with the {@link MySQLConfiguration}.
 */
public final class MySQLTest extends DatabaseTest {
    
    @Pure
    @Override
    protected boolean isSubclass() {
        return true;
    }
    
    @BeforeClass
    @Committing
    public static void configureDatabase() throws DatabaseException, IOException {
        Database.initialize(new MySQLConfiguration(true), false);
        createTables();
    }
    
}
