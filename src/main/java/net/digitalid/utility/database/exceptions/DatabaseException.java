package net.digitalid.utility.database.exceptions;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception indicates that an unexpected event or error happened during the processing of an action.
 * This includes exceptions during storing or restoring of an object in the database.
 * 
 * @see SQLException
 */
@Immutable
public class DatabaseException extends Exception {
    
    /**
     * Creates a new abort exception.
     * 
     * @param message the message of the new exception.
     * @param cause the cause of the new exception.
     */
    protected DatabaseException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new abort exception with the given parameters.
     * 
     * @param message the message of the new exception.
     * @param throwable the cause of the new exception.
     * 
     * @return a new abort exception with the given parameters.
     */
    @Pure
    public static @Nonnull DatabaseException get(@Nullable String message, @Nullable Throwable throwable) {
        return new DatabaseException(message, throwable);
    }
    
    /**
     * Creates a new abort exception with the given message.
     * 
     * @param message the message of the new exception.
     * 
     * @return a new abort exception with the given message.
     */
    @Pure
    public static @Nonnull DatabaseException get(@Nullable String message) {
        return get(message, null);
    }
    
    /**
     * Creates a new abort exception with the given cause.
     * 
     * @param throwable the cause of the new exception.
     * 
     * @return a new abort exception with the given cause.
     */
    @Pure
    public static @Nonnull DatabaseException get(@Nullable Throwable throwable) {
        return get(null, throwable);
    }
    
}