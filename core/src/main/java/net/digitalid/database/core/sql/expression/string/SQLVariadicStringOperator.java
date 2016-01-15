package net.digitalid.database.core.sql.expression.string;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.internal.InternalException;
import net.digitalid.utility.validation.reference.NonCapturable;
import net.digitalid.utility.validation.state.Immutable;

import net.digitalid.database.core.SQLDialect;
import net.digitalid.database.core.sql.expression.SQLVariadicOperator;
import net.digitalid.database.core.table.Site;

/**
 * This class enumerates the supported variadic string operators.
 */
@Immutable
public enum SQLVariadicStringOperator implements SQLVariadicOperator {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * This operator concatenates the strings.
     */
    CONCAT(),
    
    /**
     * This operator returns the greatest string.
     */
    GREATEST(),
    
    /**
     * This operator returns the first non-null string.
     */
    COALESCE();
    
    /* -------------------------------------------------- SQLNode -------------------------------------------------- */
    
    @Override
    public void transcribe(@Nonnull SQLDialect dialect, @Nonnull Site site, @NonCapturable @Nonnull StringBuilder string) throws InternalException {
        dialect.transcribe(site, string, this);
    }
    
}
