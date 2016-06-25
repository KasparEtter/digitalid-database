package net.digitalid.database.conversion.collectors;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.map.FreezableHashMap;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.conversion.converter.CustomAnnotation;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.Declaration;
import net.digitalid.utility.conversion.converter.types.CustomType;

import net.digitalid.database.conversion.exceptions.ConformityViolationException;
import net.digitalid.database.core.table.Site;
import net.digitalid.database.dialect.annotations.Embedd;
import net.digitalid.database.dialect.annotations.References;
import net.digitalid.database.dialect.ast.identifier.SQLQualifiedColumnName;
import net.digitalid.database.dialect.ast.identifier.SQLQualifiedTableName;

/**
 *
 */
public class SQLInsertDeclaration implements Declaration {
    
    /* -------------------------------------------------- Table Name -------------------------------------------------- */
    
    private final @Nonnull String tableName;
    
    private final @Nonnull Site site;
    
    /* -------------------------------------------------- Main Table Columns -------------------------------------------------- */
    
    private final @Nonnull FreezableArrayList<@Nonnull SQLQualifiedColumnName> columnNamesMainTable = FreezableArrayList.withNoElements();
    
    @Pure
    public @Nonnull ReadOnlyList<@Nonnull SQLQualifiedColumnName> getColumnNames() {
        return columnNamesMainTable.freeze();
    }
    
    private final @Nonnull FreezableArrayList<SQLInsertDeclaration> orderedInsertDeclarations = FreezableArrayList.withNoElements();
    
    /* -------------------------------------------------- Dependent tables -------------------------------------------------- */
    
    /**
     * Keeps data for dependent tables.
     */
    private final @Nonnull FreezableHashMap<@Nonnull String, @Nonnull SQLInsertDeclaration> dependentTables = FreezableHashMap.withDefaultCapacity();
    
    /**
     * Returns data for dependent tables. The foreign key columns of the tables are not yet set, which must be regarded when creating the dependent tables.
     */
    @Pure
    public @Nonnull ReadOnlyMap<@Nonnull String, @Nonnull SQLInsertDeclaration> getDependentTables() {
        return dependentTables;
    }
    
    /* -------------------------------------------------- Referenced Tables -------------------------------------------------- */
    
    /**
     * Keeps data for referenced tables.
     */
    private final @Nonnull FreezableHashMap<@Nonnull String, @Nonnull SQLInsertDeclaration> referencedTables = FreezableHashMap.withDefaultCapacity();
    
    /**
     * Returns data for referenced tables. Referenced tables must exist before the main table can be created.
     */
    @Pure
    public @Nonnull ReadOnlyMap<@Nonnull String, @Nonnull SQLInsertDeclaration> getReferencedTables() {
        return referencedTables.freeze();
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    private SQLInsertDeclaration(@Nonnull String tableName, @Nonnull Site site) {
        this.tableName = tableName;
        this.site = site;
    }
    
    @Pure
    public static @Nonnull SQLInsertDeclaration get(@Nonnull String tableName, @Nonnull Site site) {
        return new SQLInsertDeclaration(tableName, site);
    }
    
    /* -------------------------------------------------- Declaration -------------------------------------------------- */
    
    @Impure
    @Override
    public void setField(@Nonnull CustomField field) {
        if (field.getCustomType().isCompositeType()) {
            if (field.isAnnotatedWith(Embedd.class)) {
                columnNamesMainTable.add(SQLQualifiedColumnName.get(field.getName(), tableName));
                orderedInsertDeclarations.add(this);
            } else {
                final @Nonnull SQLInsertDeclaration dependentTable = SQLInsertDeclaration.get(tableName + "_" + field.getName(), site);
                dependentTable.setField(field);
                orderedInsertDeclarations.addAll(dependentTable.orderedInsertDeclarations);
                dependentTables.put(tableName + "_" + field.getName(), dependentTable);
            } // else: if is referenced, a new table will be created that links back to the current table.
        } else if (field.getCustomType().isObjectType()) {
            final CustomType.@Nonnull TupleType tupleType = (CustomType.@Nonnull TupleType) field.getCustomType();
            if (field.isAnnotatedWith(Embedd.class)) {
                ((CustomType.CustomConverterType) tupleType).getConverter().declare(this);
            } else {
                if (field.isAnnotatedWith(References.class)) {
                    final @Nonnull CustomAnnotation references = field.getAnnotation(References.class);
                    columnNamesMainTable.add(SQLQualifiedColumnName.get(field.getName(), tableName));
                    orderedInsertDeclarations.add(this);
                    final @Nonnull SQLInsertDeclaration referencedTable = SQLInsertDeclaration.get(references.get("foreignTable", String.class), site);
                    ((CustomType.CustomConverterType) tupleType).getConverter().declare(referencedTable);
                    orderedInsertDeclarations.addAll(referencedTable.orderedInsertDeclarations);
                    referencedTables.put(references.get("foreignTable", String.class), referencedTable);
                } else {
                    throw ConformityViolationException.with("Expected @" + Embedd.class.getSimpleName() + " or @" + References.class.getSimpleName() + " annotation on non-primitive field type");
                }
            }
        } else {
            columnNamesMainTable.add(SQLQualifiedColumnName.get(field.getName(), tableName));
            orderedInsertDeclarations.add(this);
        }
    }
    
    @Pure
    public @Nonnull SQLOrderedInsertStatements getOrderedInsertStatements() {
        final @Nonnull SQLOrderedInsertStatements orderedInsertStatements = new SQLOrderedInsertStatements(orderedInsertDeclarations, SQLQualifiedTableName.get(tableName, site));
        return orderedInsertStatements;
    }
    
}
