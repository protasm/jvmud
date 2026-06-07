package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/** Human-oriented formatter for typed IR inspection. */
public final class IRPrettyPrinter {
    private final StringBuilder out = new StringBuilder();

    private IRPrettyPrinter() {
    }

    public static String format(TypedIR typedIr) {
        Objects.requireNonNull(typedIr, "typedIr");
        IRPrettyPrinter printer = new IRPrettyPrinter();
        printer.write(typedIr);
        return printer.out.toString();
    }

    private void write(TypedIR typedIr) {
        IRObject object = typedIr.object();
        line("object " + object.name() + " extends " + object.parentInternalName());
        section("fields", object.fields(), this::field);
        section("methods", object.methods(), this::method);
    }

    private <T> void section(String title, List<T> items, ItemPrinter<T> printer) {
        line(title + ":");
        if (items.isEmpty()) {
            line(1, "(none)");
            return;
        }

        for (T item : items) {
            printer.print(item);
        }
    }

    private void field(IRField field) {
        String initializer = field.initializer() == null ? "" : " = " + expression(field.initializer());
        line(1, type(field.type()) + " " + field.name() + initializer);
    }

    private void method(IRMethod method) {
        line(1, "method " + type(method.returnType()) + " " + method.name() + "("
                + join(method.parameters(), this::parameter) + ")");
        if (method.overridesParent()) {
            line(2, "overrides " + method.overriddenOwnerInternalName());
        }
        line(2, "entry " + method.entryBlockLabel());
        locals(method.locals());
        line(2, "blocks:");
        for (IRBlock block : method.blocks()) {
            block(block);
        }
    }

    private void locals(List<IRLocal> locals) {
        if (locals.isEmpty()) {
            return;
        }

        line(2, "locals:");
        for (IRLocal local : locals) {
            line(3, local(local));
        }
    }

    private void block(IRBlock block) {
        line(3, block.label() + ":");
        for (IRStatement statement : block.statements()) {
            line(4, statement(statement));
        }
        line(4, statement(block.terminator()));
    }

    private String statement(IRStatement statement) {
        if (statement instanceof IRExpressionStatement expressionStatement) {
            return "eval " + expression(expressionStatement.expression());
        }
        if (statement instanceof IRReturn returnStatement) {
            return returnStatement.returnValue() == null ? "return" : "return " + expression(returnStatement.returnValue());
        }
        if (statement instanceof IRJump jump) {
            return "jump " + jump.targetLabel();
        }
        if (statement instanceof IRConditionalJump jump) {
            return "branch " + expression(jump.condition()) + " ? " + jump.trueLabel() + " : " + jump.falseLabel();
        }
        return statement.toString();
    }

    private String expression(IRExpression expression) {
        if (expression instanceof IRConstant constant) {
            return "const " + value(constant.value()) + ":" + type(constant.type());
        }
        if (expression instanceof IRLocalLoad load) {
            return "load " + local(load.local());
        }
        if (expression instanceof IRLocalStore store) {
            return "store " + local(store.local()) + " = " + expression(store.value());
        }
        if (expression instanceof IRFieldLoad load) {
            return "field " + fieldRef(load.field());
        }
        if (expression instanceof IRFieldStore store) {
            return "field " + fieldRef(store.field()) + " = " + expression(store.value());
        }
        if (expression instanceof IRBinaryOperation binary) {
            return "(" + expression(binary.left()) + " " + binary.operator() + " " + expression(binary.right())
                    + "):" + type(binary.type());
        }
        if (expression instanceof IRUnaryOperation unary) {
            return "(" + unary.operator() + " " + expression(unary.operand()) + "):" + type(unary.type());
        }
        if (expression instanceof IRCoerce coerce) {
            return "coerce " + expression(coerce.value()) + " -> " + type(coerce.targetType());
        }
        if (expression instanceof IRConditionalExpression conditional) {
            return "if " + expression(conditional.condition()) + " then " + expression(conditional.thenBranch())
                    + " else " + expression(conditional.elseBranch()) + ":" + type(conditional.type());
        }
        if (expression instanceof IRSequence sequence) {
            return "seq(" + join(sequence.expressions(), this::expression) + "):" + type(sequence.type());
        }
        if (expression instanceof IRArrayLiteral array) {
            return "[" + join(array.elements(), this::expression) + "]:" + type(array.type());
        }
        if (expression instanceof IRArrayConcat concat) {
            return "array_concat(" + expression(concat.left()) + ", " + expression(concat.right()) + "):"
                    + type(concat.type());
        }
        if (expression instanceof IRArrayGet get) {
            return expression(get.array()) + "[" + expression(get.index()) + "]:" + type(get.type());
        }
        if (expression instanceof IRArraySet set) {
            return expression(set.array()) + "[" + expression(set.index()) + "] = " + expression(set.value())
                    + ":" + type(set.type());
        }
        if (expression instanceof IRStringGet get) {
            return expression(get.string()) + "[" + expression(get.index()) + "]:" + type(get.type());
        }
        if (expression instanceof IRMappingLiteral mapping) {
            return "mapping{" + join(mapping.entries(), this::mappingEntry) + "}:" + type(mapping.type());
        }
        if (expression instanceof IRMappingMerge merge) {
            return "mapping_merge(" + expression(merge.left()) + ", " + expression(merge.right()) + "):"
                    + type(merge.type());
        }
        if (expression instanceof IRMappingGet get) {
            return expression(get.mapping()) + "[" + expression(get.key()) + "]:" + type(get.type());
        }
        if (expression instanceof IRMappingSet set) {
            return expression(set.mapping()) + "[" + expression(set.key()) + "] = " + expression(set.value())
                    + ":" + type(set.type());
        }
        if (expression instanceof IREfunCall call) {
            return "efun " + call.name() + "(" + join(call.arguments(), this::expression) + "):" + type(call.type());
        }
        if (expression instanceof IRInstanceCall call) {
            String target = call.parentDispatch() ? "parent" : call.ownerInternalName();
            return "call " + target + "." + call.methodName() + "(" + join(call.arguments(), this::expression)
                    + "):" + type(call.type());
        }
        if (expression instanceof IRDynamicInvoke invoke) {
            return "invoke " + local(invoke.targetLocal()) + "." + invoke.methodName() + "("
                    + join(invoke.arguments(), this::expression) + "):" + type(invoke.type());
        }
        if (expression instanceof IRDynamicInvokeField invoke) {
            return "invoke " + fieldRef(invoke.targetField()) + "." + invoke.methodName() + "("
                    + join(invoke.arguments(), this::expression) + "):" + type(invoke.type());
        }
        if (expression instanceof IRDynamicInvokeExpression invoke) {
            return "invoke " + expression(invoke.target()) + "." + invoke.methodName() + "("
                    + join(invoke.arguments(), this::expression) + "):" + type(invoke.type());
        }
        return expression.toString();
    }

    private String mappingEntry(IRMappingEntry entry) {
        return expression(entry.key()) + ": " + expression(entry.value());
    }

    private String parameter(IRParameter parameter) {
        return type(parameter.type()) + " " + parameter.name() + "#" + parameter.local().slot();
    }

    private String local(IRLocal local) {
        return local.name() + "#" + local.slot() + ":" + type(local.type());
    }

    private String fieldRef(IRField field) {
        return field.ownerInternalName() + "." + field.name() + ":" + type(field.type());
    }

    private String type(RuntimeType type) {
        if (type == null) {
            return "unknown";
        }
        if (type.elementType() != null) {
            return type.kind().name().toLowerCase() + "<" + type(type.elementType()) + ">";
        }
        return type.kind().name().toLowerCase();
    }

    private String value(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return value.toString();
    }

    private <T> String join(List<T> items, Formatter<T> formatter) {
        StringJoiner joiner = new StringJoiner(", ");
        for (T item : items) {
            joiner.add(formatter.format(item));
        }
        return joiner.toString();
    }

    private void line(String text) {
        line(0, text);
    }

    private void line(int indent, String text) {
        out.append("  ".repeat(indent)).append(text).append(System.lineSeparator());
    }

    @FunctionalInterface
    private interface ItemPrinter<T> {
        void print(T item);
    }

    @FunctionalInterface
    private interface Formatter<T> {
        String format(T item);
    }
}
