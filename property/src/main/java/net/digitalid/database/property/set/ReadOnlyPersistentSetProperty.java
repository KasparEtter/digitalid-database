package net.digitalid.database.property.set;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.property.set.ReadOnlySetProperty;
import net.digitalid.utility.validation.annotations.type.Functional;
import net.digitalid.utility.validation.annotations.type.Mutable;
import net.digitalid.utility.validation.annotations.type.ReadOnly;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.subject.Subject;

/**
 * This read-only property stores a set of values in the persistent database.
 * 
 * @see WritablePersistentSetProperty
 * @see ReadOnlyPersistentSimpleSetProperty
 */
@ThreadSafe
@ReadOnly(WritablePersistentSetProperty.class)
public interface ReadOnlyPersistentSetProperty<S extends Subject, V, R extends ReadOnlySet<@Nonnull @Valid V>> extends ReadOnlySetProperty<V, R, DatabaseException, ReadOnlyPersistentSetProperty.Observer<S, V, R>, ReadOnlyPersistentSetProperty<S, V, R>>, PersistentProperty<S, PersistentSetPropertyEntry<S, V>, ReadOnlyPersistentSetProperty.Observer<S, V, R>> {
    
    /* -------------------------------------------------- Observer -------------------------------------------------- */
    
    /**
     * Objects that implement this interface can be used to {@link #register(net.digitalid.utility.property.Property.Observer) observe} {@link ReadOnlyPersistentSetProperty read-only persistent set properties}.
     */
    @Mutable
    @Functional
    public static interface Observer<S extends Subject, V, R extends ReadOnlySet<@Nonnull @Valid V>> extends ReadOnlySetProperty.Observer<V, R, DatabaseException, ReadOnlyPersistentSetProperty.Observer<S, V, R>, ReadOnlyPersistentSetProperty<S, V, R>> {}
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull @NonFrozen R get() throws DatabaseException;
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull PersistentSetPropertyTable<S, V, ?> getTable();
    
}
