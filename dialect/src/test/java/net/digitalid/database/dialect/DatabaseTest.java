package net.digitalid.database.dialect;

import net.digitalid.database.interfaces.DatabaseUtility;

/**
 * Unit testing of the class {@link DatabaseUtility}.
 * 
 * @see MySQLTest
 * @see PostgreSQLTest
 * @see SQLiteTest
 * TODO: Remove this class
 */
public class DatabaseTest {
    
//    @Pure
//    protected boolean isSubclass() {
//        return false;
//    }
//    
//    @Committing
//    protected static void createTables() throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_identity (identity " + Database.getConfiguration().PRIMARY_KEY() + ", category " + Database.getConfiguration().TINYINT() + " NOT NULL, address VARCHAR(100) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ")");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_identifier (identifier VARCHAR(100) NOT NULL COLLATE " + Database.getConfiguration().BINARY() + ", identity BIGINT NOT NULL, value BIGINT, PRIMARY KEY (identifier), FOREIGN KEY (identity) REFERENCES test_identity (identity))");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_block (block " + Block.FORMAT + " NOT NULL)");
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_batch (a BIGINT NOT NULL, b BIGINT NOT NULL)");
//            Database.commit();
//        }
//    }
//    
//    @After
//    @Committing
//    public void commit() throws DatabaseException {
//        if (isSubclass()) { Database.commit(); }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _01_testKeyInsertWithStatement() throws DatabaseException {
//        if (isSubclass()) {
//            try (@Nonnull Statement statement = Database.createStatement()) {
//                Assert.assertEquals(1L, Database.executeInsert(statement, "INSERT INTO test_identity (category, address) VALUES (1, 'a@test.digitalid.net')"));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _02_testKeyInsertWithPreparedStatement() throws DatabaseException {
//        if (isSubclass()) {
//            final @Nonnull String SQL = "INSERT INTO test_identity (category, address) VALUES (?, ?)";
//            try (@Nonnull PreparedStatement preparedStatement = Database.prepareInsertStatement(SQL)) {
//                preparedStatement.setByte(1, (byte) 2);
//                preparedStatement.setString(2, "b@test.digitalid.net");
//                preparedStatement.executeUpdate();
//                Assert.assertEquals(2L, Database.getGeneratedKey(preparedStatement));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _03_testInsertWithForeignKeyConstraint() throws DatabaseException {
//        if (isSubclass()) {
//            try (@Nonnull Statement statement = Database.createStatement()) {
//                statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('a@test.digitalid.net', 1, 3)");
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _04_testLocalRollbackWithSavepoint() throws DatabaseException {
//        if (isSubclass()) {
//            try (@Nonnull Statement statement = Database.createStatement()) {
//                statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('b@test.digitalid.net', 2, 4)");
//                
//                final @Nullable Savepoint savepoint = Database.setSavepoint();
//                try {
//                    statement.executeUpdate("INSERT INTO test_identifier (identifier, identity, value) VALUES ('a@test.digitalid.net', 1, 5)");
//                    Assert.fail("An SQLException should have been thrown because a duplicate key was inserted.");
//                } catch (SQLException exception) {
//                    Database.rollback(savepoint);
//                }
//                
//                final @Nonnull ResultSet resultSet = statement.executeQuery("SELECT identity FROM test_identifier WHERE identifier = 'b@test.digitalid.net'");
//                Assert.assertTrue(resultSet.next());
//                Assert.assertEquals(2L, resultSet.getLong(1));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _05_testOnInsertIgnore() throws DatabaseException {
//        if (isSubclass()) {
//            final @Nonnull String SQL = "INSERT" + Database.getConfiguration().IGNORE() + " INTO test_identifier (identifier, identity, value) VALUES ('a@test.digitalid.net', 1, 6)";
//            try (@Nonnull Statement statement = Database.createStatement()) {
//                Database.onInsertIgnore(statement, "test_identifier", "identifier");
//                Assert.assertEquals(0, statement.executeUpdate(SQL));
//                Database.onInsertNotIgnore(statement, "test_identifier");
//                
//                final @Nonnull ResultSet resultSet = statement.executeQuery("SELECT value FROM test_identifier WHERE identifier = 'a@test.digitalid.net'");
//                Assert.assertTrue(resultSet.next());
//                Assert.assertEquals(3L, resultSet.getLong(1));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _06_testOnInsertUpdate() throws DatabaseException {
//        if (isSubclass()) {
//            final @Nonnull String SQL = Database.getConfiguration().REPLACE() + " INTO test_identifier (identifier, identity, value) VALUES ('a@test.digitalid.net', 1, 7)";
//            try (@Nonnull Statement statement = Database.createStatement()) {
//                Database.onInsertUpdate(statement, "test_identifier", 1, "identifier", "identity", "value");
//                statement.executeUpdate(SQL);
//                Database.onInsertNotUpdate(statement, "test_identifier");
//                
//                final @Nonnull ResultSet resultSet = statement.executeQuery("SELECT value FROM test_identifier WHERE identifier = 'a@test.digitalid.net'");
//                Assert.assertTrue(resultSet.next());
//                Assert.assertEquals(7L, resultSet.getLong(1));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _07_testSimpleJoin() throws DatabaseException {
//        if (isSubclass()) {
//            final @Nonnull String SQL = "SELECT test_identity.category FROM test_identifier JOIN test_identity ON test_identifier.identity = test_identity.identity WHERE identifier = 'a@test.digitalid.net'";
//            try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
//                Assert.assertTrue(resultSet.next());
//                Assert.assertEquals(1L, resultSet.getLong(1));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _08_testParallelQueries() throws DatabaseException {
//        if (isSubclass()) {
//            final @Nonnull String SQL = "SELECT identity FROM test_identity WHERE address = ";
//            try (@Nonnull Statement statement1 = Database.createStatement(); @Nonnull Statement statement2 = Database.createStatement()) {
//                try (@Nonnull ResultSet resultSet1 = statement1.executeQuery(SQL + "'a@test.digitalid.net'"); @Nonnull ResultSet resultSet2 = statement2.executeQuery(SQL + "'b@test.digitalid.net'")) {
//                    Assert.assertTrue(resultSet1.next());
//                    Assert.assertEquals(1L, resultSet1.getLong(1));
//                    Assert.assertTrue(resultSet2.next());
//                    Assert.assertEquals(2L, resultSet2.getLong(1));
//                }
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _09_testParallelUpdates() throws DatabaseException {
//        if (isSubclass()) {
//            try (@Nonnull Statement statement1 = Database.createStatement(); @Nonnull Statement statement2 = Database.createStatement()) {
//                statement1.executeUpdate("UPDATE test_identity SET category = 3 WHERE address = 'a@test.digitalid.net'");
//                final @Nonnull ResultSet resultSet1 = statement1.executeQuery("SELECT category FROM test_identity WHERE address = 'a@test.digitalid.net'");
//                
//                statement2.executeUpdate("UPDATE test_identity SET category = 4 WHERE address = 'b@test.digitalid.net'");
//                final @Nonnull ResultSet resultSet2 = statement2.executeQuery("SELECT category FROM test_identity WHERE address = 'b@test.digitalid.net'");
//                
//                Assert.assertTrue(resultSet1.next());
//                Assert.assertEquals(3, resultSet1.getByte(1));
//                
//                Assert.assertTrue(resultSet2.next());
//                Assert.assertEquals(4, resultSet2.getByte(1));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _10_testCurrentTime() throws DatabaseException {
//        if (isSubclass()) {
//            final long before = System.currentTimeMillis();
//            try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery("SELECT " + Database.getConfiguration().CURRENT_TIME())) {
//                Assert.assertTrue(resultSet.next());
//                final long time = resultSet.getLong(1);
//                final long after = System.currentTimeMillis();
//                Assert.assertTrue(time >= before && time <= after);
//            }
//        }
//    }
//    
//    /*
//    @Test
//    @NonCommitting
//    public void _11_testHandlingBlocks() throws DatabaseException, InvalidEncodingException {
//        if (isSubclass()) {
//            final @Nonnull SemanticType STRING1 = SemanticType.create("string1.tuple@test.digitalid.net").load(StringWrapper.TYPE);
//            final @Nonnull SemanticType STRING2 = SemanticType.create("string2.tuple@test.digitalid.net").load(StringWrapper.TYPE);
//            final @Nonnull SemanticType TUPLE = SemanticType.create("tuple@test.digitalid.net").load(TupleWrapper.TYPE, STRING1, STRING2);
//            
//            final @Nonnull String string1 = "Hello";
//            final @Nonnull String string2 = "World";
//            
//            try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement("INSERT INTO test_block (block) VALUES (?)")) {
//                final @Nonnull Block block = TupleWrapper.encode(TUPLE, StringWrapper.encodeNonNullable(STRING1, string1), StringWrapper.encodeNonNullable(STRING2, string2));
//                block.set(preparedStatement, 1);
//                preparedStatement.executeUpdate();
//            }
//            
//            try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement("SELECT block FROM test_block")) {
//                final @Nonnull ResultSet resultSet = preparedStatement.executeQuery();
//                Assert.assertTrue(resultSet.next());
//                final @Nonnull Block block = Block.getNotNull(TUPLE, resultSet, 1);
//                final @Nonnull ReadonlyArray<Block> elements = TupleWrapper.decode(block).getElementsNotNull(2);
//                Assert.assertEquals(string1, StringWrapper.decodeNonNullable(elements.getNotNull(0)));
//                Assert.assertEquals(string2, StringWrapper.decodeNonNullable(elements.getNotNull(1)));
//            }
//        }
//    }
//    */
//    
//    @Test
//    @NonCommitting
//    public void _12_testBatchInsertWithPreparedStatement() throws DatabaseException {
//        if (isSubclass()) {
//            final @Nonnull String SQL = "INSERT INTO test_batch (a, b) VALUES (?, ?)";
//            try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
//                preparedStatement.setLong(1, 3l);
//                // MySQL and SQLite have the same bad performance when batched as with direct updates.
//                for (int i = 0; i < 1000; i++) {
//                    preparedStatement.setLong(2, i);
////                    preparedStatement.executeUpdate();
//                    preparedStatement.addBatch();
//                }
//                preparedStatement.executeBatch();
//            }
//            try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery("SELECT a FROM test_batch WHERE b = 99")) {
//                Assert.assertTrue(resultSet.next());
//                Assert.assertEquals(3l, resultSet.getLong(1));
//            }
//        }
//    }
//    
//    @Test
//    @NonCommitting
//    public void _13_testIndexCreation() throws DatabaseException {
//        if (isSubclass()) {
//            try (@Nonnull Statement statement = Database.createStatement()) {
//                statement.executeUpdate("CREATE TABLE IF NOT EXISTS test_index (time BIGINT NOT NULL" + Database.getConfiguration().INDEX("time") + ")");
//                Database.getConfiguration().createIndex(statement, "test_index", "time");
//                Database.commit();
//            } catch (@Nonnull SQLException exception) {
//                exception.printStackTrace();
//                throw exception;
//            }
//        }
//    }
    
}
