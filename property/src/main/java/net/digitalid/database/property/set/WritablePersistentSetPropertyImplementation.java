/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.database.property.set;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.annotations.ownership.Captured;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.annotations.type.ThreadSafe;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.functional.interfaces.Predicate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.property.set.WritableSetPropertyImplementation;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.lock.LockNotHeldByCurrentThread;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.conversion.WhereCondition;
import net.digitalid.database.conversion.WhereConditionBuilder;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.subject.Subject;
import net.digitalid.database.property.subject.SubjectUtility;

/**
 * This class implements the {@link WritablePersistentSetProperty}.
 * 
 * @see WritablePersistentSimpleSetPropertyImplementation
 */
@ThreadSafe
@GenerateBuilder
@GenerateSubclass
public abstract class WritablePersistentSetPropertyImplementation<@Unspecifiable UNIT extends Unit, @Unspecifiable SUBJECT extends Subject<UNIT>, @Unspecifiable VALUE, @Unspecifiable READONLY_SET extends ReadOnlySet<@Nonnull @Valid VALUE>, @Unspecifiable FREEZABLE_SET extends FreezableSet<@Nonnull @Valid VALUE>> extends WritableSetPropertyImplementation<VALUE, READONLY_SET, DatabaseException, RecoveryException, PersistentSetObserver<SUBJECT, VALUE, READONLY_SET>, ReadOnlyPersistentSetProperty<SUBJECT, VALUE, READONLY_SET>> implements WritablePersistentSetProperty<SUBJECT, VALUE, READONLY_SET, FREEZABLE_SET> {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Predicate<? super VALUE> getValueValidator() {
        return getTable().getValueValidator();
    }
    
    /* -------------------------------------------------- Set -------------------------------------------------- */
    
    @Pure
    protected abstract @Nonnull @NonFrozen @NonNullableElements FREEZABLE_SET getSet();
    
    /* -------------------------------------------------- Table -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull PersistentSetPropertyTable<UNIT, SUBJECT, VALUE, ?> getTable();
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    protected boolean loaded = false;
    
    /**
     * Loads the values of this property from the database.
     * 
     * @param locking whether this method acquires the non-reentrant lock.
     */
    @Pure
    @NonCommitting
    protected void load(final boolean locking) throws DatabaseException, RecoveryException {
        if (locking) { lock.lock(); }
        try {
            getSet().clear();
            final @Nonnull String prefix = getTable().getParentModule().getSubjectTable().getTypeName().toLowerCase();
            final @Nonnull WhereCondition<SUBJECT> whereCondition = WhereConditionBuilder.withConverter(getTable().getParentModule().getSubjectTable()).withObject(getSubject()).withPrefix(prefix).build();
            final @Nonnull @NonNullableElements FreezableList<PersistentSetPropertyEntry<SUBJECT, VALUE>> entries = SQL.selectAll(getTable(), getSubject().getUnit(), getSubject().getUnit(), whereCondition);
            for (@Nonnull PersistentSetPropertyEntry<SUBJECT, VALUE> entry : entries) {
                getSet().add(entry.getValue());
            }
            this.loaded = true;
        } finally {
            if (locking) { lock.unlock(); }
        }
    }
    
    /* -------------------------------------------------- Getter -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    @SuppressWarnings("unchecked")
    public @Nonnull @NonFrozen @NonNullableElements READONLY_SET get() throws DatabaseException, RecoveryException {
        if (!loaded) { load(true); } // This should never trigger a reentrance exception as add(value), remove(value) and reset() that call external code ensure that the set is loaded.
        return (READONLY_SET) getSet();
    }
    
    /* -------------------------------------------------- Operations -------------------------------------------------- */
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public boolean add(@Captured @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getSet().contains(value)) {
                Database.commit();
                return false;
            } else {
                final @Nonnull PersistentSetPropertyEntry<SUBJECT, VALUE> entry = new PersistentSetPropertyEntrySubclass<>(getSubject(), value);
                SQL.insertOrAbort(getTable(), entry, getSubject().getUnit());
                getSet().add(value);
                Database.commit();
                notifyObservers(value, true);
                return true;
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Impure
    @Override
    @Committing
    @LockNotHeldByCurrentThread
    public boolean remove(@NonCaptured @Unmodified @Nonnull @Valid VALUE value) throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (!loaded) { load(false); }
            if (getSet().contains(value)) {
                final @Nonnull PersistentSetPropertyEntry<SUBJECT, VALUE> entry = new PersistentSetPropertyEntrySubclass<>(getSubject(), value);
                SQL.delete(getTable(), getSubject().getUnit(), WhereConditionBuilder.withConverter(getTable()).withObject(entry).build());
                getSet().remove(value);
                Database.commit();
                notifyObservers(value, false);
                return true;
            } else {
                Database.commit();
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Reset -------------------------------------------------- */
    
    @Impure
    @Override
    @NonCommitting
    @LockNotHeldByCurrentThread
    public void reset() throws DatabaseException, RecoveryException {
        lock.lock();
        try {
            if (loaded) {
                if (observers.isEmpty()) {
                    this.loaded = false;
                } else {
                    final @Nonnull FreezableSet<VALUE> oldSet = getSet().clone();
                    load(false);
                    final @Nonnull FreezableSet<VALUE> newSet = getSet();
                    for (@Nonnull @Valid VALUE value : newSet.exclude(oldSet)) {
                        notifyObservers(value, true);
                    }
                    for (@Nonnull @Valid VALUE value : oldSet.exclude(newSet)) {
                        notifyObservers(value, false);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    /* -------------------------------------------------- Validate -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        
        Validate.that(!getSet().containsNull()).orThrow("None of the values may be null.");
        Validate.that(getSet().matchAll(getValueValidator())).orThrow("Each value has to be valid.");
    }
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Override
    @CallSuper
    @PureWithSideEffects
    protected void initialize() {
        super.initialize();
        
        SubjectUtility.add(getSubject(), this);
    }
    
}
