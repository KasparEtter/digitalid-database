package net.digitalid.database.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * Description.
 */
@Immutable
public interface PropertyEntry<O, V> {
    
    /**
     * Returns the object to which the property belongs.
     */
    @Pure
    public @Nonnull O getObject();
    
    /**
     * Returns the time of the last modification.
     */
    @Pure
    public @Nonnull Time getTime();
    
}