package io.github.protasm.jvmud.compiler.parser.ast.visitor;

import java.io.PrintWriter;

import io.github.protasm.jvmud.compiler.parser.ast.ASTArgument;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.ast.ASTFields;
import io.github.protasm.jvmud.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethods;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameters;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprError;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.ASTStatement;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBreak;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtContinue;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtDoWhile;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtSwitch;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtWhile;

public final class PrintVisitor implements ASTVisitor {
    private int indentLvl;
    private final PrintWriter out;

    public PrintVisitor() {
        this(new PrintWriter(System.out, true));
    }

    public PrintVisitor(PrintWriter out) {
        indentLvl = 0;
        this.out = out;
    }

    private void doOutput(String str) {
        out.println(String.format("%s%s", "  ".repeat(indentLvl), str));
    }

    @Override
    public void visitArgument(ASTArgument argument) {
        doOutput(argument.className());
        indentLvl++;
        argument.expression().accept(this);
        indentLvl--;
    }

    @Override
    public void visitArguments(ASTArguments arguments) {
        if (arguments.size() == 0) {
            doOutput("[No Arguments]");
            return;
        }

        doOutput("[ARGUMENTS]");
        indentLvl++;
        for (ASTArgument argument : arguments)
            argument.accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprCallEfun(ASTExprCallEfun expr) {
        doOutput(String.format("%s%s", expr.className(), expr.efun().symbol()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprCallMethod(ASTExprCallMethod expr) {
        doOutput(String.format("%s%s", expr.className(), expr.method().symbol()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprUnresolvedCall(ASTExprUnresolvedCall expr) {
        doOutput(String.format("%s%s", expr.className(), expr.name()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprFieldAccess(ASTExprFieldAccess expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.field().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprFieldStore(ASTExprFieldStore expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.field().accept(this);
        expr.value().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprUnresolvedAssignment(ASTExprUnresolvedAssignment expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.name()));
        indentLvl++;
        expr.value().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprUnresolvedIdentifier(ASTExprUnresolvedIdentifier expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.name()));
    }

    @Override
    public void visitExprUnresolvedInvoke(ASTExprUnresolvedInvoke expr) {
        doOutput(String.format("%s[target=%s, method=%s]", expr.className(), expr.targetName(), expr.methodName()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprDynamicInvoke(ASTExprDynamicInvoke expr) {
        doOutput(String.format("%s[method=%s]", expr.className(), expr.methodName()));
        indentLvl++;
        expr.target().accept(this);
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprInvokeLocal(ASTExprInvokeLocal expr) {
        doOutput(
                String.format("%s([%s] slot=%d, methodName=%s)", expr.className(), expr.lpcType(), expr.slot(),
                        expr.methodName()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprInvokeField(ASTExprInvokeField expr) {
        doOutput(String.format("%s([%s] field=%s, methodName=%s)", expr.className(), expr.lpcType(),
                expr.field().symbol().name(), expr.methodName()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprLiteralFalse(ASTExprLiteralFalse expr) {
        doOutput(expr.className());
    }

    @Override
    public void visitExprLiteralInteger(ASTExprLiteralInteger expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.value()));
    }

    @Override
    public void visitExprLiteralString(ASTExprLiteralString expr) {
        doOutput(String.format("%s[\"%s\"],", expr.className(), expr.value()));
    }

    @Override
    public void visitExprLiteralTrue(ASTExprLiteralTrue expr) {
        doOutput(expr.className());
    }

    @Override
    public void visitExprLocalAccess(ASTExprLocalAccess expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.local().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprLocalStore(ASTExprLocalStore expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.local().accept(this);
        expr.value().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprError(ASTExprError expr) {
        doOutput(expr.className());
    }

    @Override
    public void visitExprOpBinary(ASTExprOpBinary expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.operator()));
        indentLvl++;
        expr.left().accept(this);
        expr.right().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprOpUnary(ASTExprOpUnary expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.operator()));
        indentLvl++;
        expr.right().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprArrayMutation(io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation expr) {
        doOutput(String.format("%s[%+d]", expr.className(), expr.delta()));
        indentLvl++;
        expr.target().accept(this);
        expr.index().accept(this);
        indentLvl--;
    }

    @Override
    public void visitField(ASTField field) {
        doOutput(String.format("%s%s", field.className(), field.symbol()));
        indentLvl++;
        if (field.initializer() != null)
            field.initializer().accept(this);
        indentLvl--;
    }

    @Override
    public void visitFields(ASTFields fields) {
        if (fields.size() == 0) {
            doOutput("[No Fields]");
            out.println();
            return;
        }

        doOutput("[FIELDS]");
        indentLvl++;
        for (ASTField field : fields) {
            field.accept(this);
            out.println();
        }
        indentLvl--;
    }

    @Override
    public void visitLocal(ASTLocal local) {
        doOutput(String.format("%s[%s, slot=%d, depth=%d]", local.className(), local.symbol(), local.slot(),
                local.scopeDepth()));
    }

    @Override
    public void visitMethod(ASTMethod method) {
        doOutput(String.format("%s%s", method.className(), method.symbol()));
        indentLvl++;
        if (method.parameters() != null)
            method.parameters().accept(this);
        method.body().accept(this);
        indentLvl--;
    }

    @Override
    public void visitMethods(ASTMethods methods) {
        if (methods.size() == 0) {
            doOutput("[No Methods]");
            return;
        }

        doOutput("[METHODS]");
        indentLvl++;
        int count = 0;
        for (ASTMethod method : methods) {
            method.accept(this);
            if (++count < methods.size())
                out.println();
        }
        indentLvl--;
    }

    @Override
    public void visitObject(ASTObject object) {
        if (object.parentName() != null)
            doOutput(String.format("%s(%s inherits %s)", object.className(), object.name(), object.parentName()));
        else
            doOutput(String.format("%s(%s)", object.className(), object.name()));

        indentLvl++;
        if (!object.inherits().isEmpty()) {
            doOutput("[INHERITS]");
            indentLvl++;
            object.inherits().forEach(inherit -> inherit.accept(this));
            indentLvl--;
        }
        object.fields().accept(this);
        object.methods().accept(this);
        indentLvl--;
        doOutput("End Object");
    }

    @Override
    public void visitParameter(ASTParameter parameter) {
        doOutput(String.format("%s%s", parameter.className(), parameter.symbol()));
    }

    @Override
    public void visitParameters(ASTParameters parameters) {
        if (parameters.size() == 0)
            doOutput("[No Parameters]");
        else
            for (ASTParameter param : parameters)
                param.accept(this);

        out.println();
    }

    @Override
    public void visitInherit(ASTInherit inherit) {
        doOutput(String.format("%s(%s%s)", inherit.className(), inherit.isVirtual() ? "virtual " : "", inherit.path()));
    }

    @Override
    public void visitStmtBlock(ASTStmtBlock stmt) {
        doOutput(String.format("%s[%d stmt(s)]", stmt.className(), stmt.size()));
        indentLvl++;
        int count = 0;
        for (ASTStatement statement : stmt) {
            statement.accept(this);
            if (++count < stmt.size())
                out.println();
        }
        indentLvl--;
    }

    @Override
    public void visitStmtBreak(ASTStmtBreak stmt) {
        doOutput(stmt.className());
    }

    @Override
    public void visitStmtContinue(ASTStmtContinue stmt) {
        doOutput(stmt.className());
    }

    @Override
    public void visitStmtExpression(ASTStmtExpression stmt) {
        doOutput(stmt.className());
        indentLvl++;
        stmt.expression().accept(this);
        indentLvl--;
    }

    @Override
    public void visitStmtFor(ASTStmtFor stmt) {
        doOutput(stmt.className());
        indentLvl++;
        doOutput("[INIT]");
        if (stmt.initializer() != null)
            stmt.initializer().accept(this);
        else
            doOutput("[No Initializer]");
        out.println();
        doOutput("[CONDITION]");
        if (stmt.condition() != null)
            stmt.condition().accept(this);
        else
            doOutput("[No Condition]");
        out.println();
        doOutput("[UPDATE]");
        if (stmt.update() != null)
            stmt.update().accept(this);
        else
            doOutput("[No Update]");
        out.println();
        doOutput("[BODY]");
        stmt.body().accept(this);
        indentLvl--;
    }

    @Override
    public void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {
        doOutput(stmt.className());
        indentLvl++;
        doOutput("[IF]");
        stmt.condition().accept(this);
        out.println();
        doOutput("[THEN]");
        stmt.thenBranch().accept(this);
        out.println();
        if (stmt.elseBranch() != null) {
            doOutput("[ELSE]");
            stmt.elseBranch().accept(this);
        } else
            doOutput("[No Else Condition]");
        indentLvl--;
    }

    @Override
    public void visitStmtWhile(ASTStmtWhile stmt) {
        doOutput(stmt.className());
        indentLvl++;
        doOutput("[CONDITION]");
        stmt.condition().accept(this);
        out.println();
        doOutput("[BODY]");
        stmt.body().accept(this);
        indentLvl--;
    }

    @Override
    public void visitStmtDoWhile(ASTStmtDoWhile stmt) {
        doOutput(stmt.className());
        indentLvl++;
        doOutput("[BODY]");
        stmt.body().accept(this);
        out.println();
        doOutput("[CONDITION]");
        stmt.condition().accept(this);
        indentLvl--;
    }

    @Override
    public void visitStmtSwitch(ASTStmtSwitch stmt) {
        doOutput(stmt.className());
        indentLvl++;
        doOutput("[EXPRESSION]");
        stmt.expression().accept(this);
        for (ASTStmtSwitch.SwitchCase switchCase : stmt.cases()) {
            out.println();
            doOutput(switchCase.isDefault() ? "[DEFAULT]" : "[CASE]");
            indentLvl++;
            if (!switchCase.isDefault()) {
                switchCase.expression().accept(this);
                if (switchCase.isRange()) {
                    out.println();
                    doOutput("[RANGE_END]");
                    switchCase.rangeEndExpression().accept(this);
                }
            }
            for (ASTStatement statement : switchCase.statements()) {
                out.println();
                statement.accept(this);
            }
            indentLvl--;
        }
        indentLvl--;
    }

    @Override
    public void visitStmtReturn(ASTStmtReturn stmt) {
        doOutput(stmt.className());
        indentLvl++;
        if (stmt.returnValue() != null)
            stmt.returnValue().accept(this);
        else
            doOutput("[No Return Value]");
        indentLvl--;
    }
}
