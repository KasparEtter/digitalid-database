package net.digitalid.database.exceptions.operation;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception is thrown when an update could not be executed in the database.
 */
@Immutable
public class FailedUpdateExecutionException extends FailedNonCommittingOperationException {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new failed update execution exception.
     * 
     * @param cause the cause of the failed update execution.
     */
    protected FailedUpdateExecutionException(@Nonnull SQLException cause) {
        super(cause);
    }
    
    /**
     * Returns a new failed update execution exception.
     * 
     * @param cause the cause of the failed update execution.
     * 
     * @return a new failed update execution exception.
     */
    @Pure
    public static @Nonnull FailedUpdateExecutionException get(@Nonnull SQLException cause) {
        return new FailedUpdateExecutionException(cause);
    }
    
}
