package net.digitalid.database.dialect.ast.expression.bool;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.reference.NonCapturable;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.dialect.SQLDialect;
import net.digitalid.database.dialect.ast.Transcriber;
import net.digitalid.database.dialect.ast.expression.SQLBinaryExpression;
import net.digitalid.database.dialect.ast.utility.binary.SQLBinaryExpressionTranscriber;
import net.digitalid.database.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.interfaces.ValueCollector;
import net.digitalid.database.core.table.Site;

/**
 * This class implements a binary boolean expression.
 */
@Immutable
public class SQLBinaryBooleanExpression extends SQLBooleanExpression implements SQLBinaryExpression<SQLBinaryBooleanOperator, SQLBooleanExpression> {
    
    /* -------------------------------------------------- Operator -------------------------------------------------- */
    
    /**
     * Stores the operator of this binary expression.
     */
    private final @Nonnull SQLBinaryBooleanOperator operator;
    
    @Pure
    @Override
    public final @Nonnull SQLBinaryBooleanOperator getOperator() {
        return operator;
    }
    
    /* -------------------------------------------------- Left Expression -------------------------------------------------- */
    
    /**
     * Stores the left child expression of this binary expression.
     */
    private final @Nonnull SQLBooleanExpression leftExpression;
    
    @Pure
    @Override
    public final @Nonnull SQLBooleanExpression getLeftExpression() {
        return leftExpression;
    }
    
    /* -------------------------------------------------- Right Expression -------------------------------------------------- */
    
    /**
     * Stores the right child expression of this binary expression.
     */
    private final @Nonnull SQLBooleanExpression rightExpression;
    
    @Pure
    @Override
    public final @Nonnull SQLBooleanExpression getRightExpression() {
        return rightExpression;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new binary boolean expression with the given parameters.
     * 
     * @param operator the operator of the new binary boolean expression.
     * @param leftExpression the left child expression of the new binary boolean expression.
     * @param rightExpression the right child expression of the new binary boolean expression.
     */
    protected SQLBinaryBooleanExpression(@Nonnull SQLBinaryBooleanOperator operator, @Nonnull SQLBooleanExpression leftExpression, @Nonnull SQLBooleanExpression rightExpression) {
        this.operator = operator;
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }
    
    /**
     * Returns a new binary boolean expression with the given parameters.
     * 
     * @param operator the operator of the new binary boolean expression.
     * @param leftExpression the left child expression of the new binary boolean expression.
     * @param rightExpression the right child expression of the new binary boolean expression.
     * 
     * @return a new binary boolean expression with the given parameters.
     */
    @Pure
    public static @Nonnull SQLBinaryBooleanExpression get(@Nonnull SQLBinaryBooleanOperator operator, @Nonnull SQLBooleanExpression leftExpression, @Nonnull SQLBooleanExpression rightExpression) {
        return new SQLBinaryBooleanExpression(operator, leftExpression, rightExpression);
    }
    
    /* -------------------------------------------------- SQLNode -------------------------------------------------- */
    
    /**
     * The transcriber that stores a string representation of this SQL node in the string builder.
     */
    private static final @Nonnull Transcriber<SQLBinaryBooleanExpression> transcriber = new Transcriber<SQLBinaryBooleanExpression>() {
        
        @Override
        protected void transcribe(@Nonnull SQLDialect dialect, @Nonnull SQLBinaryBooleanExpression node, @Nonnull Site site, @Nonnull @NonCapturable StringBuilder string) throws InternalException {
            SQLBinaryExpressionTranscriber.transcribeNode(node, dialect, site, string);
        }
        
    };
    
    @Override
    public @Nonnull Transcriber<SQLBinaryBooleanExpression> getTranscriber() {
        return transcriber;
    }
    
    /* -------------------------------------------------- SQLParameterizableNode -------------------------------------------------- */
    
    @Override
    public final void storeValues(@NonCapturable @Nonnull ValueCollector collector) throws FailedValueStoringException {
        leftExpression.storeValues(collector);
        rightExpression.storeValues(collector);
    }
    
}
