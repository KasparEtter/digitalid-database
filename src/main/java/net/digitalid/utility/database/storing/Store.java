package net.digitalid.utility.database.storing;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Stateless;

/**
 * This is a utility class to store storable objects.
 */
@Stateless
public final class Store {
    
    /**
     * Sets the parameters starting from the given index of the prepared statement to the given non-nullable storable.
     * 
     * @param storable the non-nullable storable which is to be stored in the database.
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param parameterIndex the starting index of the parameters which are to be set.
     */
    public static <V extends Storable<V, ?>> void nonNullable(@Nonnull V storable, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        storable.getStoringFactory().storeNonNullable(storable, preparedStatement, parameterIndex);
    }
    
}
