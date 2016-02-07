package net.digitalid.database.dialect.ast.statement.table.create;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.reference.NonCapturable;

import net.digitalid.database.core.interfaces.ValueCollector;
import net.digitalid.database.dialect.ast.expression.SQLExpression;
import net.digitalid.database.dialect.ast.expression.bool.SQLComparisonOperator;
import net.digitalid.database.dialect.ast.expression.bool.SQLNumberComparisonBooleanExpression;
import net.digitalid.database.dialect.ast.expression.number.SQLNumberLiteral;
import net.digitalid.database.dialect.ast.expression.number.SQLNumberReference;
import net.digitalid.database.exceptions.operation.FailedValueStoringException;

/**
 *
 */
public class SQLCheckNonNegativeConstraint extends SQLCheckConstraint {
    
    private final @Nonnull SQLExpression checkConstraint;
    
    SQLCheckNonNegativeConstraint(@Nonnull Field field) {
        Require.that(field.isAnnotationPresent(NonNegative.class)).orThrow("The annotation @NonNegative is present.");
        checkConstraint = SQLNumberComparisonBooleanExpression.get(SQLComparisonOperator.GREATER_OR_EQUAL, SQLNumberReference.get(field), SQLNumberLiteral.get(0L));
    }
    
    @Override
    protected @Nonnull SQLExpression<?> getCheckConstraint() {
        return checkConstraint;
    }
    
    @Override
    public void storeValues(@NonCapturable @Nonnull ValueCollector collector) throws FailedValueStoringException {
        collector.setInteger64(0L);
    }
    
}
