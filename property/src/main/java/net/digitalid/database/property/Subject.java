package net.digitalid.database.property;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.interfaces.Site;
import net.digitalid.database.interfaces.SiteBuilder;

/**
 * A subject belongs to a {@link Site site} and can have {@link PersistentProperty persistent properties}.
 */
@Immutable
public interface Subject extends RootInterface {
    
    /* -------------------------------------------------- Site -------------------------------------------------- */
    
    /**
     * Stores the default site for subjects.
     */
    public static final @Nonnull Site DEFAULT_SITE = SiteBuilder.withName("default").build();
    
    /**
     * Returns the site of this subject.
     */
    @Pure
    @Provided
    @Default("net.digitalid.database.property.Subject.DEFAULT_SITE")
    public @Nonnull Site getSite();
    
}
