package net.digitalid.database.dialect.ast.statement.table.create;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.conversion.converter.CustomAnnotation;
import net.digitalid.utility.validation.annotations.math.modulo.MultipleOf;

import net.digitalid.database.dialect.ast.expression.SQLExpression;
import net.digitalid.database.dialect.ast.expression.bool.SQLComparisonOperator;
import net.digitalid.database.dialect.ast.expression.bool.SQLNumberComparisonBooleanExpression;
import net.digitalid.database.dialect.ast.expression.number.SQLBinaryNumberExpression;
import net.digitalid.database.dialect.ast.expression.number.SQLBinaryNumberOperator;
import net.digitalid.database.dialect.ast.expression.number.SQLNumberLiteral;
import net.digitalid.database.dialect.ast.expression.number.SQLNumberReference;
import net.digitalid.database.exceptions.operation.FailedSQLValueConversionException;
import net.digitalid.database.interfaces.SQLValueCollector;

/**
 * This SQL node represents the multiple-of constraint for a database column.
 */
public class SQLCheckMultipleOfConstraint extends SQLCheckConstraint {
    
    /**
     * The check constraint expression.
     */
    private final @Nonnull SQLExpression checkConstraint;
    
    /**
     * The multiple-of value.
     */
    private final long multipleOfValue;
    
    /**
     * Creates a new multiple-of constraint instance.
     */
    SQLCheckMultipleOfConstraint(@Nonnull CustomAnnotation annotation, @Nonnull String columnName) {
        Require.that(annotation.getAnnotationType().isAssignableFrom(MultipleOf.class)).orThrow("The annotation @MultipleOf is present.");
        
        this.multipleOfValue = annotation.get("value", long.class);
        checkConstraint = SQLNumberComparisonBooleanExpression.get(SQLComparisonOperator.EQUAL,
                SQLBinaryNumberExpression.get(SQLBinaryNumberOperator.MODULO, SQLNumberReference.get(columnName), SQLNumberLiteral.get(multipleOfValue)), SQLNumberLiteral.get(0L));
    }
    
    @Pure
    @Override
    protected @Nonnull SQLExpression<?> getCheckConstraint() {
        return checkConstraint;
    }
    
    @Pure
    @Override
    public void storeValues(@Nonnull @NonCaptured @Modified SQLValueCollector collector) throws FailedSQLValueConversionException {
        collector.setInteger64(multipleOfValue);
        collector.setInteger64(0L);
    }
    
}
