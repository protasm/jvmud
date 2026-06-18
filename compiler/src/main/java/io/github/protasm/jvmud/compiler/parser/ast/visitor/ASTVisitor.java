package io.github.protasm.jvmud.compiler.parser.ast.visitor;

import io.github.protasm.jvmud.compiler.parser.ast.ASTArgument;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.ast.ASTFields;
import io.github.protasm.jvmud.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethods;
import io.github.protasm.jvmud.compiler.parser.ast.ASTNode;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameters;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFunctionReference;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprNull;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSymbolLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedParentCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedQualifiedCall;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBreak;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtContinue;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtWhile;

/**
 * Unified visitor entry point for walking AST nodes. Implementors override the granular {@code
 * visit*} methods they care about; the default {@link #visit(ASTNode)} dispatches to the correct
 * specialization using pattern matching.
 */
public interface ASTVisitor {
    default void visit(ASTNode node) {
        if (node == null)
            return;

        switch (node) {
        case ASTArgument argument -> visitArgument(argument);
        case ASTArguments arguments -> visitArguments(arguments);
        case ASTField field -> visitField(field);
        case ASTFields fields -> visitFields(fields);
        case ASTInherit inherit -> visitInherit(inherit);
        case ASTLocal local -> visitLocal(local);
        case ASTMethod method -> visitMethod(method);
        case ASTMethods methods -> visitMethods(methods);
        case ASTObject object -> visitObject(object);
        case ASTParameter parameter -> visitParameter(parameter);
        case ASTParameters parameters -> visitParameters(parameters);
        case ASTExprCallEfun exprCallEfun -> visitExprCallEfun(exprCallEfun);
        case ASTExprArrayMutation exprArrayMutation -> visitExprArrayMutation(exprArrayMutation);
        case ASTExprCallMethod exprCallMethod -> visitExprCallMethod(exprCallMethod);
        case ASTExprDynamicInvoke exprDynamicInvoke -> visitExprDynamicInvoke(exprDynamicInvoke);
        case ASTExprFieldAccess exprFieldAccess -> visitExprFieldAccess(exprFieldAccess);
        case ASTExprFieldStore exprFieldStore -> visitExprFieldStore(exprFieldStore);
        case ASTExprFunctionReference exprFunctionReference -> visitExprFunctionReference(exprFunctionReference);
        case ASTExprInvokeField exprInvokeField -> visitExprInvokeField(exprInvokeField);
        case ASTExprInvokeLocal exprInvokeLocal -> visitExprInvokeLocal(exprInvokeLocal);
        case ASTExprLiteralFalse exprLiteralFalse -> visitExprLiteralFalse(exprLiteralFalse);
        case ASTExprLiteralInteger exprLiteralInteger -> visitExprLiteralInteger(exprLiteralInteger);
        case ASTExprLiteralString exprLiteralString -> visitExprLiteralString(exprLiteralString);
        case ASTExprLiteralTrue exprLiteralTrue -> visitExprLiteralTrue(exprLiteralTrue);
        case ASTExprLocalAccess exprLocalAccess -> visitExprLocalAccess(exprLocalAccess);
        case ASTExprLocalStore exprLocalStore -> visitExprLocalStore(exprLocalStore);
        case ASTExprNull exprNull -> visitExprNull(exprNull);
        case ASTExprOpBinary exprOpBinary -> visitExprOpBinary(exprOpBinary);
        case ASTExprOpUnary exprOpUnary -> visitExprOpUnary(exprOpUnary);
        case ASTExprSymbolLiteral exprSymbolLiteral -> visitExprSymbolLiteral(exprSymbolLiteral);
        case ASTExprUnresolvedAssignment exprUnresolvedAssignment -> visitExprUnresolvedAssignment(exprUnresolvedAssignment);
        case ASTExprUnresolvedCall exprUnresolvedCall -> visitExprUnresolvedCall(exprUnresolvedCall);
        case ASTExprUnresolvedIdentifier exprUnresolvedIdentifier -> visitExprUnresolvedIdentifier(exprUnresolvedIdentifier);
        case ASTExprUnresolvedInvoke exprUnresolvedInvoke -> visitExprUnresolvedInvoke(exprUnresolvedInvoke);
        case ASTExprUnresolvedParentCall exprUnresolvedParentCall -> visitExprUnresolvedParentCall(exprUnresolvedParentCall);
        case ASTExprUnresolvedQualifiedCall exprUnresolvedQualifiedCall -> visitExprUnresolvedQualifiedCall(exprUnresolvedQualifiedCall);
        case ASTExpression expression -> visitExpression(expression);
        case ASTStmtBlock stmtBlock -> visitStmtBlock(stmtBlock);
        case ASTStmtBreak stmtBreak -> visitStmtBreak(stmtBreak);
        case ASTStmtContinue stmtContinue -> visitStmtContinue(stmtContinue);
        case ASTStmtExpression stmtExpression -> visitStmtExpression(stmtExpression);
        case ASTStmtFor stmtFor -> visitStmtFor(stmtFor);
        case ASTStmtIfThenElse stmtIfThenElse -> visitStmtIfThenElse(stmtIfThenElse);
        case ASTStmtReturn stmtReturn -> visitStmtReturn(stmtReturn);
        case ASTStmtWhile stmtWhile -> visitStmtWhile(stmtWhile);
        case ASTStatement statement -> visitStatement(statement);
        default -> throw new IllegalStateException("Unhandled AST node: " + node.getClass().getName());
        }
    }

    default void visitArgument(ASTArgument argument) {}

    default void visitArguments(ASTArguments arguments) {
        arguments.forEach(this::visit);
    }

    default void visitField(ASTField field) {}

    default void visitFields(ASTFields fields) {
        fields.forEach(this::visit);
    }

    default void visitInherit(ASTInherit inherit) {}

    default void visitLocal(ASTLocal local) {}

    default void visitMethod(ASTMethod method) {}

    default void visitMethods(ASTMethods methods) {
        methods.forEach(this::visit);
    }

    default void visitObject(ASTObject object) {}

    default void visitParameter(ASTParameter parameter) {}

    default void visitParameters(ASTParameters parameters) {
        parameters.forEach(this::visit);
    }

    default void visitExpression(ASTExpression expression) {}

    default void visitStatement(ASTStatement statement) {}

    default void visitExprCallEfun(ASTExprCallEfun expr) {}

    default void visitExprArrayMutation(ASTExprArrayMutation expr) {}

    default void visitExprCallMethod(ASTExprCallMethod expr) {}

    default void visitExprDynamicInvoke(ASTExprDynamicInvoke expr) {}

    default void visitExprFieldAccess(ASTExprFieldAccess expr) {}

    default void visitExprFieldStore(ASTExprFieldStore expr) {}

    default void visitExprFunctionReference(ASTExprFunctionReference expr) {}

    default void visitExprInvokeField(ASTExprInvokeField expr) {}

    default void visitExprInvokeLocal(ASTExprInvokeLocal expr) {}

    default void visitExprLiteralFalse(ASTExprLiteralFalse expr) {}

    default void visitExprLiteralInteger(ASTExprLiteralInteger expr) {}

    default void visitExprLiteralString(ASTExprLiteralString expr) {}

    default void visitExprLiteralTrue(ASTExprLiteralTrue expr) {}

    default void visitExprLocalAccess(ASTExprLocalAccess expr) {}

    default void visitExprLocalStore(ASTExprLocalStore expr) {}

    default void visitExprNull(ASTExprNull expr) {}

    default void visitExprOpBinary(ASTExprOpBinary expr) {}

    default void visitExprOpUnary(ASTExprOpUnary expr) {}

    default void visitExprSymbolLiteral(ASTExprSymbolLiteral expr) {}

    default void visitExprUnresolvedAssignment(ASTExprUnresolvedAssignment expr) {}

    default void visitExprUnresolvedCall(ASTExprUnresolvedCall expr) {}

    default void visitExprUnresolvedIdentifier(ASTExprUnresolvedIdentifier expr) {}

    default void visitExprUnresolvedInvoke(ASTExprUnresolvedInvoke expr) {}

    default void visitExprUnresolvedParentCall(ASTExprUnresolvedParentCall expr) {}

    default void visitExprUnresolvedQualifiedCall(ASTExprUnresolvedQualifiedCall expr) {}

    default void visitStmtBlock(ASTStmtBlock stmt) {}

    default void visitStmtBreak(ASTStmtBreak stmt) {}

    default void visitStmtContinue(ASTStmtContinue stmt) {}

    default void visitStmtExpression(ASTStmtExpression stmt) {}

    default void visitStmtFor(ASTStmtFor stmt) {}

    default void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {}

    default void visitStmtReturn(ASTStmtReturn stmt) {}

    default void visitStmtWhile(ASTStmtWhile stmt) {}
}
