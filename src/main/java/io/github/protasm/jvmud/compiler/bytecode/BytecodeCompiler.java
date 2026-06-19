package io.github.protasm.jvmud.compiler.bytecode;

import static org.objectweb.asm.Opcodes.*;

import io.github.protasm.jvmud.compiler.ir.*;
import io.github.protasm.jvmud.compiler.parser.type.BinaryOpType;
import io.github.protasm.jvmud.compiler.parser.type.UnaryOpType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeComparison;
import io.github.protasm.jvmud.compiler.runtime.RuntimeCoercions;
import io.github.protasm.jvmud.compiler.runtime.RuntimeArray;
import io.github.protasm.jvmud.compiler.runtime.RuntimeEquality;
import io.github.protasm.jvmud.compiler.runtime.RuntimeForeach;
import io.github.protasm.jvmud.compiler.runtime.RuntimeFunctionLiteral;
import io.github.protasm.jvmud.compiler.runtime.RuntimeIndex;
import io.github.protasm.jvmud.compiler.runtime.RuntimeTypes;
import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeValueKind;
import io.github.protasm.jvmud.compiler.runtime.RuntimeScanf;
import io.github.protasm.jvmud.compiler.runtime.RuntimeString;
import io.github.protasm.jvmud.compiler.runtime.Truth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Emits JVM bytecode from the typed IR.
 *
 * <p>This emitter assumes the IR has already been semantically validated; it does not perform
 * additional semantic checks or fallbacks.</p>
 */
public final class BytecodeCompiler {
    private static final String INIT_METHOD_NAME = "$lpc$init";
    private static final String INIT_METHOD_DESCRIPTOR = "()V";
    private static final String INIT_GUARD_FIELD = "$lpc$initialized";
    private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);
    private final String defaultParentInternalName;

    public BytecodeCompiler(String defaultParentInternalName) {
        this.defaultParentInternalName =
                Objects.requireNonNull(defaultParentInternalName, "defaultParentInternalName");
    }

    public byte[] compile(TypedIR typedIr) {
        if (typedIr == null)
            throw new BytecodeCompileException("TypedIR cannot be null.");

        IRObject object = typedIr.object();
        ClassWriter cw = new SafeClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String internalName = object.name();
        String parentName =
                (object.parentInternalName() != null) ? object.parentInternalName() : defaultParentInternalName;

        // Map LPC inheritance onto Java's single-inheritance model by wiring the resolved parent
        // class into the generated superclass slot.
        cw.visit(V21, ACC_SUPER | ACC_PUBLIC, internalName, null, parentName, null);

        emitEngineManagedFields(cw);
        emitFields(cw, object);
        emitPrivateInitializer(cw, internalName, parentName, object.fields());
        emitDefaultConstructor(cw, internalName, parentName);

        for (IRMethod method : object.methods())
            emitMethod(cw, internalName, method);

        return cw.toByteArray();
    }

    private void emitFields(ClassWriter cw, IRObject object) {
        for (IRField field : object.fields()) {
            // Only materialize fields owned by this object. Inheritance is handled by the JVM's
            // class hierarchy so parent storage remains in the parent class.
            cw.visitField(ACC_PROTECTED, field.name(), descriptor(field.type()), null, null).visitEnd();
        }
    }

    private void emitEngineManagedFields(ClassWriter cw) {
        // Engine-managed lifecycle state never surfaces in LPC; it is synthetic and private to keep
        // mudlib policy separate from the compiler's lifecycle wiring.
        cw.visitField(ACC_PRIVATE | ACC_SYNTHETIC, INIT_GUARD_FIELD, "Z", null, null).visitEnd();
    }

    private void emitDefaultConstructor(ClassWriter cw, String internalName, String parentName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, parentName, "<init>", "()V", false);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, internalName, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitPrivateInitializer(ClassWriter cw, String internalName, String parentName, List<IRField> fields) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PROTECTED | ACC_SYNTHETIC, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, null, null);
        mv.visitCode();

        Label alreadyInitialized = new Label();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, internalName, INIT_GUARD_FIELD, "Z");
        mv.visitJumpInsn(IFNE, alreadyInitialized);

        // Mark initialization as started up front so re-entrant calls from nested constructors or
        // reflection never repeat initialization work on the same instance.
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, internalName, INIT_GUARD_FIELD, "Z");

        if (!OBJECT_INTERNAL_NAME.equals(parentName)) {
            // Engine lifecycle: chain to the parent initializer before touching child state so that
            // inheritance order remains deterministic. The mudlib retains full control of *what*
            // initialization policy executes; the engine merely ensures *when* the chain runs.
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, parentName, INIT_METHOD_NAME, INIT_METHOD_DESCRIPTOR, false);
        }

        emitFieldInitializers(mv, internalName, fields);

        mv.visitLabel(alreadyInitialized);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitFieldInitializers(MethodVisitor mv, String internalName, List<IRField> fields) {
        for (IRField field : fields) {
            if (field.initializer() == null && field.type().kind() != RuntimeValueKind.MIXED)
                continue;

            // Field initializers are part of the object definition and run exactly once per
            // instance under the engine-managed lifecycle gate. The engine deliberately avoids
            // invoking any mudlib-defined hooks here; mudlib policy remains explicit and
            // user-controlled.
            mv.visitVarInsn(ALOAD, 0);
            if (field.initializer() == null) {
                emitMixedZero(mv);
            } else {
                emitExpression(mv, internalName, null, field.initializer());
            }
            mv.visitFieldInsn(PUTFIELD, field.ownerInternalName(), field.name(), descriptor(field.type()));
        }
    }

    private void emitMethod(ClassWriter cw, String internalName, IRMethod method) {
        if (method.overridesParent() && method.overriddenOwnerInternalName() == null)
            throw new BytecodeCompileException("Override for '" + method.name() + "' is missing parent metadata.");

        String descriptor = methodDescriptor(method);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.name(), descriptor, null, null);
        mv.visitCode();
        emitDefaultLocalInitializers(mv, method);

        Map<String, Label> labels = new HashMap<>();
        for (IRBlock block : method.blocks())
            labels.put(block.label(), new Label());

        for (IRBlock block : method.blocks()) {
            mv.visitLabel(labels.get(block.label()));
            for (IRStatement statement : block.statements())
                emitStatement(mv, internalName, method, statement);

            emitTerminator(mv, internalName, method, block.terminator(), labels);
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitDefaultLocalInitializers(MethodVisitor mv, IRMethod method) {
        for (IRLocal local : method.locals()) {
            if (local.parameter() || local.slot() < 0) {
                continue;
            }

            switch (local.type().kind()) {
            case INT, STATUS -> {
                pushInt(mv, 0);
                mv.visitVarInsn(ISTORE, local.slot());
            }
            case FLOAT -> {
                mv.visitInsn(FCONST_0);
                mv.visitVarInsn(FSTORE, local.slot());
            }
            case MIXED -> {
                emitMixedZero(mv);
                mv.visitVarInsn(ASTORE, local.slot());
            }
            case VOID -> {}
            default -> {
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, local.slot());
            }
            }
        }
    }

    private void emitStatement(MethodVisitor mv, String internalName, IRMethod method, IRStatement statement) {
        if (statement instanceof IRExpressionStatement exprStmt)
            emitExpressionStatement(mv, internalName, method, exprStmt);
    }

    private void emitExpressionStatement(
            MethodVisitor mv, String internalName, IRMethod method, IRExpressionStatement exprStmt) {
        IRExpression expression = exprStmt.expression();
        emitExpression(mv, internalName, method, expression);

        RuntimeType type = expression != null ? expression.type() : null;
        if (type != null && type.kind() != RuntimeValueKind.VOID)
            mv.visitInsn(type.jvmType() == null || type.isReferenceLike() ? POP : POP);
    }

    private void emitTerminator(
            MethodVisitor mv,
            String internalName,
            IRMethod method,
            IRTerminator terminator,
            Map<String, Label> labels) {
        if (terminator instanceof IRReturn irReturn) {
            emitReturn(mv, internalName, method, irReturn);
            return;
        }

        if (terminator instanceof IRJump irJump) {
            mv.visitJumpInsn(GOTO, labels.get(irJump.targetLabel()));
            return;
        }

        if (terminator instanceof IRConditionalJump irConditional) {
            emitConditionalJump(mv, internalName, method, irConditional, labels);
        }
    }

    private void emitConditionalJump(
            MethodVisitor mv,
            String internalName,
            IRMethod method,
            IRConditionalJump irConditional,
            Map<String, Label> labels) {
        Label trueLabel = labels.get(irConditional.trueLabel());
        Label falseLabel = labels.get(irConditional.falseLabel());

        emitBooleanValue(mv, internalName, method, irConditional.condition());
        mv.visitJumpInsn(IFNE, trueLabel);
        mv.visitJumpInsn(GOTO, falseLabel);
    }

    private void emitReturn(MethodVisitor mv, String internalName, IRMethod method, IRReturn irReturn) {
        IRExpression returnValue = irReturn.returnValue();
        RuntimeType returnType = method.returnType();

        if (returnValue != null)
            emitExpression(mv, internalName, method, returnValue);

        switch (returnType.kind()) {
        case INT, STATUS:
            mv.visitInsn(IRETURN);
            break;
        case FLOAT:
            mv.visitInsn(FRETURN);
            break;
        case VOID:
            mv.visitInsn(RETURN);
            break;
        default:
            mv.visitInsn(ARETURN);
            break;
        }
    }

    private void emitExpression(MethodVisitor mv, String internalName, IRMethod method, IRExpression expression) {
        if (expression == null)
            return;

        if (expression instanceof IRConstant constant) {
            emitConstant(mv, constant);
            return;
        }

        if (expression instanceof IRCollectionTransform transform) {
            emitCollectionTransform(mv, internalName, method, transform);
            return;
        }

        if (expression instanceof IRSortArray sortArray) {
            emitSortArray(mv, internalName, method, sortArray);
            return;
        }

        if (expression instanceof IRTypedFunctionLiteral typedFunctionLiteral) {
            emitTypedFunctionLiteral(mv, typedFunctionLiteral);
            return;
        }

        if (expression instanceof IRLocalLoad localLoad) {
            emitLocalLoad(mv, localLoad.local());
            return;
        }

        if (expression instanceof IRLocalStore localStore) {
            emitLocalStore(mv, internalName, method, localStore);
            return;
        }

        if (expression instanceof IRLocalMutation localMutation) {
            emitLocalMutation(mv, localMutation);
            return;
        }

        if (expression instanceof IRFieldLoad fieldLoad) {
            emitFieldLoad(mv, internalName, fieldLoad.field());
            return;
        }

        if (expression instanceof IRFieldStore fieldStore) {
            emitFieldStore(mv, internalName, method, fieldStore);
            return;
        }

        if (expression instanceof IRFieldMutation fieldMutation) {
            emitFieldMutation(mv, fieldMutation);
            return;
        }

        if (expression instanceof IRUnaryOperation unary) {
            emitUnaryOperation(mv, internalName, method, unary);
            return;
        }

        if (expression instanceof IRBinaryOperation binary) {
            emitBinaryOperation(mv, internalName, method, binary);
            return;
        }

        if (expression instanceof IRConditionalExpression conditionalExpression) {
            emitConditionalExpression(mv, internalName, method, conditionalExpression);
            return;
        }

        if (expression instanceof IRArrayLiteral arrayLiteral) {
            emitArrayLiteral(mv, internalName, method, arrayLiteral);
            return;
        }

        if (expression instanceof IRArrayConcat arrayConcat) {
            emitArrayConcat(mv, internalName, method, arrayConcat);
            return;
        }

        if (expression instanceof IRArrayDifference arrayDifference) {
            emitArrayDifference(mv, internalName, method, arrayDifference);
            return;
        }

        if (expression instanceof IRArrayGet arrayGet) {
            emitArrayGet(mv, internalName, method, arrayGet);
            return;
        }

        if (expression instanceof IRArraySet arraySet) {
            emitArraySet(mv, internalName, method, arraySet);
            return;
        }

        if (expression instanceof IRArraySliceSet arraySliceSet) {
            emitArraySliceSet(mv, internalName, method, arraySliceSet);
            return;
        }

        if (expression instanceof IRArrayMutation arrayMutation) {
            emitArrayMutation(mv, internalName, method, arrayMutation);
            return;
        }

        if (expression instanceof IRMappingLiteral mappingLiteral) {
            emitMappingLiteral(mv, internalName, method, mappingLiteral);
            return;
        }

        if (expression instanceof IRMappingMerge mappingMerge) {
            emitMappingMerge(mv, internalName, method, mappingMerge);
            return;
        }

        if (expression instanceof IRMappingGet mappingGet) {
            emitMappingGet(mv, internalName, method, mappingGet);
            return;
        }

        if (expression instanceof IRMappingSet mappingSet) {
            emitMappingSet(mv, internalName, method, mappingSet);
            return;
        }

        if (expression instanceof IRSequence sequence) {
            emitSequence(mv, internalName, method, sequence);
            return;
        }

        if (expression instanceof IRStringDifference stringDifference) {
            emitStringDifference(mv, internalName, method, stringDifference);
            return;
        }

        if (expression instanceof IRStringGet stringGet) {
            emitStringGet(mv, internalName, method, stringGet);
            return;
        }

        if (expression instanceof IRSlice slice) {
            emitSlice(mv, internalName, method, slice);
            return;
        }

        if (expression instanceof IREfunCall efunCall) {
            emitEfunCall(mv, internalName, method, efunCall);
            return;
        }

        if (expression instanceof IRInstanceCall instanceCall) {
            emitInstanceCall(mv, internalName, method, instanceCall);
            return;
        }

        if (expression instanceof IRDynamicInvoke dynamicInvoke) {
            emitDynamicInvoke(mv, internalName, method, dynamicInvoke);
            return;
        }

        if (expression instanceof IRDynamicInvokeExpression dynamicInvokeExpression) {
            emitDynamicInvokeExpression(mv, internalName, method, dynamicInvokeExpression);
            return;
        }

        if (expression instanceof IRDynamicInvokeField dynamicInvokeField) {
            emitDynamicInvokeField(mv, internalName, method, dynamicInvokeField);
            return;
        }

        if (expression instanceof IRProtectedEval protectedEval) {
            emitProtectedEval(mv, internalName, method, protectedEval);
            return;
        }

        if (expression instanceof IRForeachItems foreachItems) {
            emitForeachItems(mv, internalName, method, foreachItems);
            return;
        }

        if (expression instanceof IRForeachSize foreachSize) {
            emitForeachSize(mv, internalName, method, foreachSize);
            return;
        }

        if (expression instanceof IRForeachValue foreachValue) {
            emitForeachValue(mv, internalName, method, foreachValue);
            return;
        }

        if (expression instanceof IRFromEndIndex fromEndIndex) {
            emitFromEndIndex(mv, internalName, method, fromEndIndex);
            return;
        }

        if (expression instanceof IRCoerce coerce) {
            emitCoerce(mv, internalName, method, coerce);
        }
    }

    private void emitConstant(MethodVisitor mv, IRConstant constant) {
        Object value = constant.value();
        RuntimeType type = constant.type();

        if (value == null) {
            mv.visitInsn(ACONST_NULL);
            return;
        }

        switch (type.kind()) {
        case INT, STATUS -> pushInt(mv, ((Number) value).intValue());
        case FLOAT -> mv.visitLdcInsn(((Number) value).floatValue());
        case STRING -> mv.visitLdcInsn(value);
        default -> mv.visitLdcInsn(value);
        }
    }

    private void emitTypedFunctionLiteral(MethodVisitor mv, IRTypedFunctionLiteral literal) {
        mv.visitTypeInsn(NEW, Type.getInternalName(RuntimeFunctionLiteral.class));
        mv.visitInsn(DUP);
        mv.visitLdcInsn(literal.signature());
        mv.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(RuntimeFunctionLiteral.class),
                "<init>",
                "(Ljava/lang/String;)V",
                false);
    }

    private void emitLocalLoad(MethodVisitor mv, IRLocal local) {
        switch (kindToOpcode(local.type(), true)) {
        case ILOAD -> mv.visitVarInsn(ILOAD, local.slot());
        case FLOAD -> mv.visitVarInsn(FLOAD, local.slot());
        default -> mv.visitVarInsn(ALOAD, local.slot());
        }
    }

    private void emitLocalStore(MethodVisitor mv, String internalName, IRMethod method, IRLocalStore localStore) {
        emitExpression(mv, internalName, method, localStore.value());

        RuntimeType type = localStore.local().type();
        dupForStore(mv, type);

        switch (kindToOpcode(type, false)) {
        case ISTORE -> mv.visitVarInsn(ISTORE, localStore.local().slot());
        case FSTORE -> mv.visitVarInsn(FSTORE, localStore.local().slot());
        default -> mv.visitVarInsn(ASTORE, localStore.local().slot());
        }
    }

    private void emitLocalMutation(MethodVisitor mv, IRLocalMutation mutation) {
        RuntimeType type = mutation.local().type();
        if (type.kind() == RuntimeValueKind.MIXED) {
            emitLocalMutationMixed(mv, mutation);
            return;
        }
        if (type.kind() == RuntimeValueKind.FLOAT) {
            emitLocalMutationFloat(mv, mutation);
            return;
        }
        if (type.kind() != RuntimeValueKind.INT && type.kind() != RuntimeValueKind.STATUS)
            throw new BytecodeCompileException("Local increment expects primitive numeric target.");

        mv.visitVarInsn(ILOAD, mutation.local().slot());
        if (!mutation.isPrefix())
            mv.visitInsn(DUP);
        pushInt(mv, mutation.delta());
        mv.visitInsn(IADD);
        if (mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitVarInsn(ISTORE, mutation.local().slot());
    }

    private void emitLocalMutationMixed(MethodVisitor mv, IRLocalMutation mutation) {
        mv.visitVarInsn(ALOAD, mutation.local().slot());
        if (!mutation.isPrefix())
            mv.visitInsn(DUP);
        pushInt(mv, mutation.delta());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeCoercions.class),
                "incrementNumber",
                "(Ljava/lang/Object;I)Ljava/lang/Object;",
                false);
        if (mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, mutation.local().slot());
    }

    private void emitLocalMutationFloat(MethodVisitor mv, IRLocalMutation mutation) {
        mv.visitVarInsn(FLOAD, mutation.local().slot());
        if (!mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitLdcInsn((float) mutation.delta());
        mv.visitInsn(FADD);
        if (mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitVarInsn(FSTORE, mutation.local().slot());
    }

    private void emitFieldLoad(MethodVisitor mv, String internalName, IRField field) {
        mv.visitVarInsn(ALOAD, 0);
        // Target the owning class so inherited fields resolve through the JVM hierarchy while
        // allowing shadowed child fields to live alongside parent storage.
        mv.visitFieldInsn(GETFIELD, field.ownerInternalName(), field.name(), descriptor(field.type()));
    }

    private void emitFieldStore(MethodVisitor mv, String internalName, IRMethod method, IRFieldStore fieldStore) {
        emitExpression(mv, internalName, method, fieldStore.value());
        dupForStore(mv, fieldStore.field().type());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        // Writes route to the declared owner to keep parent storage distinct from any child
        // shadows without reintroducing initialization here (managed by the engine).
        mv.visitFieldInsn(
                PUTFIELD,
                fieldStore.field().ownerInternalName(),
                fieldStore.field().name(),
                descriptor(fieldStore.field().type()));
    }

    private void emitFieldMutation(MethodVisitor mv, IRFieldMutation mutation) {
        RuntimeType type = mutation.field().type();
        if (type.kind() == RuntimeValueKind.MIXED) {
            emitFieldMutationMixed(mv, mutation);
            return;
        }
        if (type.kind() == RuntimeValueKind.FLOAT) {
            emitFieldMutationFloat(mv, mutation);
            return;
        }
        if (type.kind() != RuntimeValueKind.INT && type.kind() != RuntimeValueKind.STATUS)
            throw new BytecodeCompileException("Field increment expects primitive numeric target.");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(
                GETFIELD,
                mutation.field().ownerInternalName(),
                mutation.field().name(),
                descriptor(type));
        if (!mutation.isPrefix())
            mv.visitInsn(DUP);
        pushInt(mv, mutation.delta());
        mv.visitInsn(IADD);
        if (mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(
                PUTFIELD,
                mutation.field().ownerInternalName(),
                mutation.field().name(),
                descriptor(type));
    }

    private void emitFieldMutationMixed(MethodVisitor mv, IRFieldMutation mutation) {
        RuntimeType type = mutation.field().type();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(
                GETFIELD,
                mutation.field().ownerInternalName(),
                mutation.field().name(),
                descriptor(type));
        if (!mutation.isPrefix())
            mv.visitInsn(DUP);
        pushInt(mv, mutation.delta());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeCoercions.class),
                "incrementNumber",
                "(Ljava/lang/Object;I)Ljava/lang/Object;",
                false);
        if (mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(
                PUTFIELD,
                mutation.field().ownerInternalName(),
                mutation.field().name(),
                descriptor(type));
    }

    private void emitFieldMutationFloat(MethodVisitor mv, IRFieldMutation mutation) {
        RuntimeType type = mutation.field().type();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(
                GETFIELD,
                mutation.field().ownerInternalName(),
                mutation.field().name(),
                descriptor(type));
        if (!mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitLdcInsn((float) mutation.delta());
        mv.visitInsn(FADD);
        if (mutation.isPrefix())
            mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(
                PUTFIELD,
                mutation.field().ownerInternalName(),
                mutation.field().name(),
                descriptor(type));
    }

    private void emitUnaryOperation(MethodVisitor mv, String internalName, IRMethod method, IRUnaryOperation unary) {
        emitExpression(mv, internalName, method, unary.operand());

        if (unary.operator() == UnaryOpType.UOP_NOT) {
            emitBooleanFlip(mv, unary.operand().type());
            return;
        }

        switch (unary.operator()) {
        case UOP_BIT_NOT:
            coerceValue(mv, unary.operand().type(), RuntimeTypes.INT);
            mv.visitInsn(ICONST_M1);
            mv.visitInsn(IXOR);
            break;
        case UOP_NEGATE:
            coerceValue(mv, unary.operand().type(), unary.type().kind() == RuntimeValueKind.FLOAT
                    ? RuntimeTypes.FLOAT
                    : RuntimeTypes.INT);
            switch (unary.type().kind()) {
            case FLOAT -> mv.visitInsn(FNEG);
            default -> mv.visitInsn(INEG);
            }
            break;
        default:
            throw new UnsupportedOperationException("Unsupported unary operator: " + unary.operator());
        }
    }

    private void emitBinaryOperation(
            MethodVisitor mv, String internalName, IRMethod method, IRBinaryOperation binary) {
        BinaryOpType op = binary.operator();

        if (op == BinaryOpType.BOP_AND || op == BinaryOpType.BOP_OR) {
            emitLogicalBinary(mv, internalName, method, binary);
            return;
        }

        if (op == BinaryOpType.BOP_ADD && binary.type().kind() == RuntimeValueKind.STRING) {
            emitStringConcat(mv, internalName, method, binary.left(), binary.right());
            return;
        }

        if ((op == BinaryOpType.BOP_EQ || op == BinaryOpType.BOP_NE)
                && (binary.left().type().isReferenceLike() || binary.right().type().isReferenceLike())) {
            emitReferenceEquality(mv, internalName, method, binary);
            return;
        }

        if (isRelationalOperator(op)
                && (binary.left().type().isReferenceLike() || binary.right().type().isReferenceLike())) {
            emitDynamicComparison(mv, internalName, method, binary);
            return;
        }

        emitIntOperand(mv, internalName, method, binary.left());
        emitIntOperand(mv, internalName, method, binary.right());

        switch (op) {
        case BOP_ADD -> mv.visitInsn(IADD);
        case BOP_SUB -> mv.visitInsn(ISUB);
        case BOP_MULT -> mv.visitInsn(IMUL);
        case BOP_DIV -> mv.visitInsn(IDIV);
        case BOP_MOD -> mv.visitInsn(IREM);
        case BOP_BIT_OR, BOP_BIT_AND, BOP_BIT_XOR, BOP_SHL, BOP_SHR -> mv.visitInsn(op.opcode());
        case BOP_GT, BOP_GE, BOP_LT, BOP_LE, BOP_EQ, BOP_NE -> emitComparison(mv, op);
        default -> throw new UnsupportedOperationException("Unsupported operator: " + op);
        }
    }

    private boolean isRelationalOperator(BinaryOpType op) {
        return op == BinaryOpType.BOP_GT
                || op == BinaryOpType.BOP_GE
                || op == BinaryOpType.BOP_LT
                || op == BinaryOpType.BOP_LE;
    }

    private void emitDynamicComparison(
            MethodVisitor mv, String internalName, IRMethod method, IRBinaryOperation binary) {
        emitExpression(mv, internalName, method, binary.left());
        boxIfNeeded(mv, binary.left().type());
        emitExpression(mv, internalName, method, binary.right());
        boxIfNeeded(mv, binary.right().type());

        String helperName = switch (binary.operator()) {
        case BOP_GT -> "greaterThan";
        case BOP_GE -> "greaterThanOrEqual";
        case BOP_LT -> "lessThan";
        case BOP_LE -> "lessThanOrEqual";
        default -> throw new UnsupportedOperationException("Unsupported dynamic comparison: " + binary.operator());
        };

        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeComparison.class),
                helperName,
                "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                false);
    }

    private void emitLogicalBinary(
            MethodVisitor mv, String internalName, IRMethod method, IRBinaryOperation binary) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        Label end = new Label();

        emitBooleanValue(mv, internalName, method, binary.left());
        if (binary.operator() == BinaryOpType.BOP_OR)
            mv.visitJumpInsn(IFNE, trueLabel);
        else
            mv.visitJumpInsn(IFEQ, falseLabel);

        emitBooleanValue(mv, internalName, method, binary.right());
        if (binary.operator() == BinaryOpType.BOP_OR)
            mv.visitJumpInsn(IFNE, trueLabel);
        else
            mv.visitJumpInsn(IFEQ, falseLabel);

        if (binary.operator() == BinaryOpType.BOP_OR)
            mv.visitJumpInsn(GOTO, falseLabel);

        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, end);

        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(end);
    }

    private void emitComparison(MethodVisitor mv, BinaryOpType op) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        mv.visitJumpInsn(op.opcode(), trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitReferenceEquality(
            MethodVisitor mv, String internalName, IRMethod method, IRBinaryOperation binary) {
        emitExpression(mv, internalName, method, binary.left());
        boxIfNeeded(mv, binary.left().type());
        emitExpression(mv, internalName, method, binary.right());
        boxIfNeeded(mv, binary.right().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeEquality.class),
                "equals",
                "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                false);

        if (binary.operator() == BinaryOpType.BOP_NE) {
            emitBooleanInvert(mv);
        }
    }

    private void emitIntOperand(MethodVisitor mv, String internalName, IRMethod method, IRExpression operand) {
        emitExpression(mv, internalName, method, operand);
        coerceValue(mv, operand.type(), RuntimeTypes.INT);
    }

    private void emitEfunCall(MethodVisitor mv, String internalName, IRMethod method, IREfunCall efunCall) {
        if ("sscanf".equals(efunCall.name()) && efunCall.arguments().size() >= 2) {
            emitSscanfCall(mv, internalName, method, efunCall);
            return;
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContextHolder",
                "requireCurrent",
                "()Lio/github/protasm/jvmud/compiler/runtime/RuntimeContext;",
                false);
        mv.visitLdcInsn(efunCall.name());
        pushInt(mv, efunCall.arguments().size());
        emitArgumentsArray(mv, internalName, method, efunCall.arguments());

        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContext",
                "invokeEfun",
                "(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/Object;",
                false);

        if (efunCall.type() != null && efunCall.type().kind() == RuntimeValueKind.VOID) {
            mv.visitInsn(POP);
            return;
        }

        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.MIXED, efunCall.type());
    }

    private void emitSscanfCall(MethodVisitor mv, String internalName, IRMethod method, IREfunCall efunCall) {
        List<IRExpression> args = efunCall.arguments();
        int captureCount = args.size() - 2;
        int resultSlot = scratchObjectSlot(method);

        emitExpression(mv, internalName, method, args.get(0));
        boxIfNeeded(mv, args.get(0).type());
        emitExpression(mv, internalName, method, args.get(1));
        boxIfNeeded(mv, args.get(1).type());
        pushInt(mv, captureCount);
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeScanf.class),
                "scan",
                "(Ljava/lang/Object;Ljava/lang/Object;I)[Ljava/lang/Object;",
                false);
        mv.visitVarInsn(ASTORE, resultSlot);

        for (int i = 0; i < captureCount; i++) {
            emitSscanfCaptureStore(mv, args.get(i + 2), resultSlot, i + 1);
        }

        mv.visitVarInsn(ALOAD, resultSlot);
        pushInt(mv, 0);
        mv.visitInsn(AALOAD);
        coerceValue(mv, RuntimeTypes.MIXED, RuntimeTypes.INT);
        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.INT, efunCall.type());
    }

    private void emitSscanfCaptureStore(MethodVisitor mv, IRExpression target, int resultSlot, int captureIndex) {
        Label noCapture = new Label();
        mv.visitVarInsn(ALOAD, resultSlot);
        pushInt(mv, 0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Number.class));
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Number.class),
                "intValue",
                "()I",
                false);
        pushInt(mv, captureIndex);
        mv.visitJumpInsn(IF_ICMPLT, noCapture);

        if (target instanceof IRLocalLoad localLoad) {
            mv.visitVarInsn(ALOAD, resultSlot);
            pushInt(mv, captureIndex);
            mv.visitInsn(AALOAD);
            coerceValue(mv, RuntimeTypes.MIXED, localLoad.local().type());
            switch (kindToOpcode(localLoad.local().type(), false)) {
            case ISTORE -> mv.visitVarInsn(ISTORE, localLoad.local().slot());
            case FSTORE -> mv.visitVarInsn(FSTORE, localLoad.local().slot());
            default -> mv.visitVarInsn(ASTORE, localLoad.local().slot());
            }
            mv.visitLabel(noCapture);
            return;
        }

        if (target instanceof IRFieldLoad fieldLoad) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, resultSlot);
            pushInt(mv, captureIndex);
            mv.visitInsn(AALOAD);
            coerceValue(mv, RuntimeTypes.MIXED, fieldLoad.field().type());
            mv.visitFieldInsn(
                    PUTFIELD,
                    fieldLoad.field().ownerInternalName(),
                    fieldLoad.field().name(),
                    descriptor(fieldLoad.field().type()));
            mv.visitLabel(noCapture);
            return;
        }

        throw new BytecodeCompileException("sscanf output argument must be a local or field.");
    }

    private void emitInstanceCall(MethodVisitor mv, String internalName, IRMethod method, IRInstanceCall call) {
        String descriptor = buildCallDescriptor(call);

        if (call.parentDispatch()) {
            if (call.ownerInternalName() == null)
                throw new BytecodeCompileException("Unresolved parent dispatch for '" + call.methodName() + "'.");

            // Explicit LPC parent calls lower to direct super calls so dispatch stays anchored on
            // the inherited implementation rather than re-entering overrides on this class.
            mv.visitVarInsn(ALOAD, 0);
            emitCallArguments(mv, internalName, method, call.arguments(), call.parameterTypes());

            mv.visitMethodInsn(INVOKESPECIAL, call.ownerInternalName(), call.methodName(), descriptor, false);
            return;
        }

        mv.visitVarInsn(ALOAD, 0);
        emitCallArguments(mv, internalName, method, call.arguments(), call.parameterTypes());

        mv.visitMethodInsn(INVOKEVIRTUAL, call.ownerInternalName(), call.methodName(), descriptor, false);
    }

    private void emitCallArguments(
            MethodVisitor mv,
            String internalName,
            IRMethod method,
            List<IRExpression> arguments,
            List<RuntimeType> parameterTypes) {
        for (int i = 0; i < arguments.size(); i++) {
            IRExpression argument = arguments.get(i);
            emitExpression(mv, internalName, method, argument);
            if (parameterTypes != null && i < parameterTypes.size()) {
                coerceValue(mv, argument.type(), parameterTypes.get(i));
            }
        }

        if (parameterTypes != null) {
            for (int i = arguments.size(); i < parameterTypes.size(); i++) {
                emitDefaultArgument(mv, parameterTypes.get(i));
            }
        }
    }

    private void emitDefaultArgument(MethodVisitor mv, RuntimeType type) {
        switch (type.kind()) {
        case INT, STATUS -> pushInt(mv, 0);
        case FLOAT -> mv.visitInsn(FCONST_0);
        case VOID -> throw new BytecodeCompileException("Cannot pass a default value for void parameter.");
        default -> mv.visitInsn(ACONST_NULL);
        }
    }

    private void emitDynamicInvoke(
            MethodVisitor mv, String internalName, IRMethod method, IRDynamicInvoke dynamicInvoke) {
        mv.visitMethodInsn(
                INVOKESTATIC,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContextHolder",
                "requireCurrent",
                "()Lio/github/protasm/jvmud/compiler/runtime/RuntimeContext;",
                false);
        emitLocalLoad(mv, dynamicInvoke.targetLocal());
        mv.visitLdcInsn(dynamicInvoke.methodName());
        emitArgumentsArray(mv, internalName, method, dynamicInvoke.arguments());
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContext",
                "invokeOptionalObject",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        if (dynamicInvoke.type() != null && dynamicInvoke.type().kind() == RuntimeValueKind.VOID) {
            mv.visitInsn(POP);
            return;
        }
        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.MIXED, dynamicInvoke.type());
    }

    private void emitDynamicInvokeExpression(
            MethodVisitor mv, String internalName, IRMethod method, IRDynamicInvokeExpression dynamicInvoke) {
        mv.visitMethodInsn(
                INVOKESTATIC,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContextHolder",
                "requireCurrent",
                "()Lio/github/protasm/jvmud/compiler/runtime/RuntimeContext;",
                false);
        emitExpression(mv, internalName, method, dynamicInvoke.target());
        boxIfNeeded(mv, dynamicInvoke.target().type());
        mv.visitLdcInsn(dynamicInvoke.methodName());
        emitArgumentsArray(mv, internalName, method, dynamicInvoke.arguments());
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContext",
                "invokeOptionalObject",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        if (dynamicInvoke.type() != null && dynamicInvoke.type().kind() == RuntimeValueKind.VOID) {
            mv.visitInsn(POP);
            return;
        }
        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.MIXED, dynamicInvoke.type());
    }

    private void emitDynamicInvokeField(
            MethodVisitor mv, String internalName, IRMethod method, IRDynamicInvokeField dynamicInvokeField) {
        mv.visitMethodInsn(
                INVOKESTATIC,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContextHolder",
                "requireCurrent",
                "()Lio/github/protasm/jvmud/compiler/runtime/RuntimeContext;",
                false);
        emitFieldLoad(mv, internalName, dynamicInvokeField.targetField());
        mv.visitLdcInsn(dynamicInvokeField.methodName());
        emitArgumentsArray(mv, internalName, method, dynamicInvokeField.arguments());
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "io/github/protasm/jvmud/compiler/runtime/RuntimeContext",
                "invokeOptionalObject",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        if (dynamicInvokeField.type() != null && dynamicInvokeField.type().kind() == RuntimeValueKind.VOID) {
            mv.visitInsn(POP);
            return;
        }
        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.MIXED, dynamicInvokeField.type());
    }

    private void emitCoerce(MethodVisitor mv, String internalName, IRMethod method, IRCoerce coerce) {
        emitExpression(mv, internalName, method, coerce.value());
        coerceValue(mv, coerce.value().type(), coerce.targetType());
    }

    private void emitArgumentsArray(MethodVisitor mv, String internalName, IRMethod method, List<IRExpression> args) {
        pushInt(mv, args.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP);
            pushInt(mv, i);
            IRExpression arg = args.get(i);
            emitExpression(mv, internalName, method, arg);
            boxIfNeeded(mv, arg.type());
            mv.visitInsn(AASTORE);
        }
    }

    private void emitParamTypesArray(
            MethodVisitor mv, String internalName, IRMethod method, List<IRExpression> args) {
        pushInt(mv, args.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP);
            pushInt(mv, i);
            RuntimeType type = args.get(i).type();
            switch (type != null ? type.kind() : RuntimeValueKind.MIXED) {
            case INT, STATUS -> mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            case FLOAT -> mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            case STRING -> mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            default -> mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
            }
            mv.visitInsn(AASTORE);
        }
    }

    private void emitBooleanValue(MethodVisitor mv, String internalName, IRMethod method, IRExpression condition) {
        emitExpression(mv, internalName, method, condition);
        RuntimeType type = condition.type();

        if (type != null && type.kind() == RuntimeValueKind.STATUS && type.jvmType() != null) {
            return; // already boolean/int
        }

        boxIfNeeded(mv, type);
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Truth.class),
                "isTruthy",
                "(Ljava/lang/Object;)Z",
                false);
    }

    private void emitBooleanFlip(MethodVisitor mv, RuntimeType operandType) {
        boxIfNeeded(mv, operandType);
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Truth.class),
                "isTruthy",
                "(Ljava/lang/Object;)Z",
                false);

        Label trueLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IFEQ, trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitBooleanInvert(MethodVisitor mv) {
        Label trueLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IFEQ, trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitStringConcat(
            MethodVisitor mv, String internalName, IRMethod method, IRExpression left, IRExpression right) {
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        emitExpression(mv, internalName, method, left);
        boxIfNeeded(mv, left.type());
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);

        emitExpression(mv, internalName, method, right);
        boxIfNeeded(mv, right.type());
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);

        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }

    private void emitStringDifference(
            MethodVisitor mv, String internalName, IRMethod method, IRStringDifference difference) {
        emitExpression(mv, internalName, method, difference.left());
        boxIfNeeded(mv, difference.left().type());
        emitExpression(mv, internalName, method, difference.right());
        boxIfNeeded(mv, difference.right().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeString.class),
                "difference",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String;",
                false);
    }

    private void emitCoerceToRuntimeTypeIfNeeded(MethodVisitor mv, RuntimeType source, RuntimeType target) {
        if (target == null)
            return;

        coerceValue(mv, source, target);
    }

    private void emitConditionalExpression(
            MethodVisitor mv, String internalName, IRMethod method, IRConditionalExpression conditionalExpression) {
        Label elseLabel = new Label();
        Label endLabel = new Label();

        emitBooleanValue(mv, internalName, method, conditionalExpression.condition());
        mv.visitJumpInsn(IFEQ, elseLabel);

        emitExpression(mv, internalName, method, conditionalExpression.thenBranch());
        mv.visitJumpInsn(GOTO, endLabel);

        mv.visitLabel(elseLabel);
        emitExpression(mv, internalName, method, conditionalExpression.elseBranch());
        mv.visitLabel(endLabel);
    }

    private void emitArrayLiteral(MethodVisitor mv, String internalName, IRMethod method, IRArrayLiteral literal) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);

        for (IRExpression element : literal.elements()) {
            mv.visitInsn(DUP);
            emitExpression(mv, internalName, method, element);
            boxIfNeeded(mv, element.type());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
            mv.visitInsn(POP);
        }
    }

    private void emitCollectionTransform(
            MethodVisitor mv, String internalName, IRMethod method, IRCollectionTransform transform) {
        emitExpression(mv, internalName, method, transform.source());
        boxIfNeeded(mv, transform.source().type());
        mv.visitVarInsn(ASTORE, transform.sourceLocal().slot());

        mv.visitVarInsn(ALOAD, transform.sourceLocal().slot());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeForeach.class),
                "items",
                "(Ljava/lang/Object;)Ljava/util/List;",
                false);
        mv.visitVarInsn(ASTORE, transform.itemsLocal().slot());

        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, transform.resultLocal().slot());

        for (int i = 0; i < transform.extraArguments().size(); i++) {
            IRExpression extra = transform.extraArguments().get(i);
            IRLocal local = transform.callbackArgumentLocals().get(i + 1);
            emitExpression(mv, internalName, method, extra);
            boxIfNeeded(mv, extra.type());
            mv.visitVarInsn(ASTORE, local.slot());
        }

        pushInt(mv, 0);
        mv.visitVarInsn(ISTORE, transform.indexLocal().slot());

        Label conditionLabel = new Label();
        Label endLabel = new Label();
        mv.visitLabel(conditionLabel);
        mv.visitVarInsn(ILOAD, transform.indexLocal().slot());
        mv.visitVarInsn(ALOAD, transform.itemsLocal().slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
        mv.visitJumpInsn(IF_ICMPGE, endLabel);

        mv.visitVarInsn(ALOAD, transform.itemsLocal().slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        mv.visitVarInsn(ILOAD, transform.indexLocal().slot());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
        mv.visitVarInsn(ASTORE, transform.callbackArgumentLocals().get(0).slot());

        if (transform.operation() == IRCollectionTransform.Operation.FILTER) {
            emitExpression(mv, internalName, method, transform.callbackBody());
            boxIfNeeded(mv, transform.callbackBody().type());
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Truth.class),
                    "isTruthy",
                    "(Ljava/lang/Object;)Z",
                    false);
            Label skipAdd = new Label();
            mv.visitJumpInsn(IFEQ, skipAdd);
            mv.visitVarInsn(ALOAD, transform.resultLocal().slot());
            mv.visitTypeInsn(CHECKCAST, "java/util/List");
            mv.visitVarInsn(ALOAD, transform.callbackArgumentLocals().get(0).slot());
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);
            mv.visitLabel(skipAdd);
        } else {
            mv.visitVarInsn(ALOAD, transform.resultLocal().slot());
            mv.visitTypeInsn(CHECKCAST, "java/util/List");
            emitExpression(mv, internalName, method, transform.callbackBody());
            boxIfNeeded(mv, transform.callbackBody().type());
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(POP);
        }

        mv.visitIincInsn(transform.indexLocal().slot(), 1);
        mv.visitJumpInsn(GOTO, conditionLabel);
        mv.visitLabel(endLabel);

        mv.visitVarInsn(ALOAD, transform.resultLocal().slot());
        if (transform.type().kind() == RuntimeValueKind.ARRAY)
            mv.visitTypeInsn(CHECKCAST, "java/util/List");
    }

    private void emitSortArray(MethodVisitor mv, String internalName, IRMethod method, IRSortArray sortArray) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, sortArray.source());
        boxIfNeeded(mv, sortArray.source().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeForeach.class),
                "items",
                "(Ljava/lang/Object;)Ljava/util/List;",
                false);
        mv.visitMethodInsn(
                INVOKESPECIAL, "java/util/ArrayList", "<init>", "(Ljava/util/Collection;)V", false);
        mv.visitVarInsn(ASTORE, sortArray.itemsLocal().slot());

        for (int i = 0; i < sortArray.extraArguments().size(); i++) {
            IRExpression extra = sortArray.extraArguments().get(i);
            IRLocal local = sortArray.comparatorArgumentLocals().get(i + 2);
            emitExpression(mv, internalName, method, extra);
            boxIfNeeded(mv, extra.type());
            mv.visitVarInsn(ASTORE, local.slot());
        }

        pushInt(mv, 0);
        mv.visitVarInsn(ISTORE, sortArray.indexLocal().slot());

        Label outerCondition = new Label();
        Label outerEnd = new Label();
        Label innerCondition = new Label();
        Label innerEnd = new Label();
        Label skipSwap = new Label();

        mv.visitLabel(outerCondition);
        mv.visitVarInsn(ILOAD, sortArray.indexLocal().slot());
        emitListSize(mv, sortArray.itemsLocal());
        mv.visitJumpInsn(IF_ICMPGE, outerEnd);

        mv.visitVarInsn(ILOAD, sortArray.indexLocal().slot());
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, sortArray.innerIndexLocal().slot());

        mv.visitLabel(innerCondition);
        mv.visitVarInsn(ILOAD, sortArray.innerIndexLocal().slot());
        emitListSize(mv, sortArray.itemsLocal());
        mv.visitJumpInsn(IF_ICMPGE, innerEnd);

        emitListGet(mv, sortArray.itemsLocal(), sortArray.indexLocal());
        mv.visitVarInsn(ASTORE, sortArray.comparatorArgumentLocals().get(0).slot());
        emitListGet(mv, sortArray.itemsLocal(), sortArray.innerIndexLocal());
        mv.visitVarInsn(ASTORE, sortArray.comparatorArgumentLocals().get(1).slot());

        emitExpression(mv, internalName, method, sortArray.comparatorBody());
        boxIfNeeded(mv, sortArray.comparatorBody().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Truth.class),
                "isTruthy",
                "(Ljava/lang/Object;)Z",
                false);
        mv.visitJumpInsn(IFEQ, skipSwap);

        emitListGet(mv, sortArray.itemsLocal(), sortArray.indexLocal());
        mv.visitVarInsn(ASTORE, sortArray.swapLocal().slot());

        mv.visitVarInsn(ALOAD, sortArray.itemsLocal().slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        mv.visitVarInsn(ILOAD, sortArray.indexLocal().slot());
        emitListGet(mv, sortArray.itemsLocal(), sortArray.innerIndexLocal());
        mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, sortArray.itemsLocal().slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        mv.visitVarInsn(ILOAD, sortArray.innerIndexLocal().slot());
        mv.visitVarInsn(ALOAD, sortArray.swapLocal().slot());
        mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitInsn(POP);

        mv.visitLabel(skipSwap);
        mv.visitIincInsn(sortArray.innerIndexLocal().slot(), 1);
        mv.visitJumpInsn(GOTO, innerCondition);

        mv.visitLabel(innerEnd);
        mv.visitIincInsn(sortArray.indexLocal().slot(), 1);
        mv.visitJumpInsn(GOTO, outerCondition);

        mv.visitLabel(outerEnd);
        mv.visitVarInsn(ALOAD, sortArray.itemsLocal().slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
    }

    private void emitListSize(MethodVisitor mv, IRLocal listLocal) {
        mv.visitVarInsn(ALOAD, listLocal.slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
    }

    private void emitListGet(MethodVisitor mv, IRLocal listLocal, IRLocal indexLocal) {
        mv.visitVarInsn(ALOAD, listLocal.slot());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        mv.visitVarInsn(ILOAD, indexLocal.slot());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
    }

    private void emitArrayConcat(MethodVisitor mv, String internalName, IRMethod method, IRArrayConcat concat) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, concat.left());
        mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "addAll", "(Ljava/util/Collection;)Z", false);
        mv.visitInsn(POP);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, concat.right());
        mv.visitTypeInsn(CHECKCAST, "java/util/Collection");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "addAll", "(Ljava/util/Collection;)Z", false);
        mv.visitInsn(POP);
    }

    private void emitArrayDifference(MethodVisitor mv, String internalName, IRMethod method, IRArrayDifference difference) {
        emitExpression(mv, internalName, method, difference.left());
        boxIfNeeded(mv, difference.left().type());
        emitExpression(mv, internalName, method, difference.right());
        boxIfNeeded(mv, difference.right().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeArray.class),
                "difference",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;",
                false);
    }

    private void emitArrayGet(MethodVisitor mv, String internalName, IRMethod method, IRArrayGet arrayGet) {
        emitExpression(mv, internalName, method, arrayGet.array());
        boxIfNeeded(mv, arrayGet.array().type());
        emitExpression(mv, internalName, method, arrayGet.index());
        boxIfNeeded(mv, arrayGet.index().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                "get",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                false);
    }

    private void emitStringGet(MethodVisitor mv, String internalName, IRMethod method, IRStringGet stringGet) {
        emitExpression(mv, internalName, method, stringGet.string());
        coerceValue(mv, stringGet.string().type(), RuntimeTypes.STRING);
        emitExpression(mv, internalName, method, stringGet.index());
        boxIfNeeded(mv, stringGet.index().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                "stringCharCode",
                "(Ljava/lang/String;Ljava/lang/Object;)I",
                false);
    }

    private void emitSlice(MethodVisitor mv, String internalName, IRMethod method, IRSlice slice) {
        emitExpression(mv, internalName, method, slice.target());
        boxIfNeeded(mv, slice.target().type());
        emitExpression(mv, internalName, method, slice.start());
        boxIfNeeded(mv, slice.start().type());
        emitExpression(mv, internalName, method, slice.end());
        boxIfNeeded(mv, slice.end().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                "slice",
                "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        coerceValue(mv, RuntimeTypes.MIXED, slice.type());
    }

    private void emitArraySet(MethodVisitor mv, String internalName, IRMethod method, IRArraySet arraySet) {
        emitExpression(mv, internalName, method, arraySet.array());
        boxIfNeeded(mv, arraySet.array().type());
        emitExpression(mv, internalName, method, arraySet.index());
        boxIfNeeded(mv, arraySet.index().type());
        emitExpression(mv, internalName, method, arraySet.value());
        boxIfNeeded(mv, arraySet.value().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                "set",
                "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                false);
    }

    private void emitArraySliceSet(MethodVisitor mv, String internalName, IRMethod method, IRArraySliceSet arraySliceSet) {
        emitExpression(mv, internalName, method, arraySliceSet.array());
        boxIfNeeded(mv, arraySliceSet.array().type());
        emitExpression(mv, internalName, method, arraySliceSet.start());
        boxIfNeeded(mv, arraySliceSet.start().type());
        emitExpression(mv, internalName, method, arraySliceSet.end());
        boxIfNeeded(mv, arraySliceSet.end().type());
        emitExpression(mv, internalName, method, arraySliceSet.value());
        boxIfNeeded(mv, arraySliceSet.value().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                "replaceSlice",
                "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        coerceValue(mv, RuntimeTypes.MIXED, arraySliceSet.type());
    }

    private void emitArrayMutation(MethodVisitor mv, String internalName, IRMethod method, IRArrayMutation mutation) {
        emitExpression(mv, internalName, method, mutation.array());
        boxIfNeeded(mv, mutation.array().type());
        emitExpression(mv, internalName, method, mutation.index());
        boxIfNeeded(mv, mutation.index().type());
        pushInt(mv, mutation.delta());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                mutation.isPrefix() ? "mutateNumberPrefix" : "mutateNumber",
                "(Ljava/lang/Object;Ljava/lang/Object;I)Ljava/lang/Object;",
                false);
    }

    private void emitFromEndIndex(MethodVisitor mv, String internalName, IRMethod method, IRFromEndIndex fromEndIndex) {
        emitExpression(mv, internalName, method, fromEndIndex.distance());
        coerceValue(mv, fromEndIndex.distance().type(), RuntimeTypes.INT);
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeIndex.class),
                "fromEnd",
                "(I)Ljava/lang/Object;",
                false);
    }

    private void emitSequence(MethodVisitor mv, String internalName, IRMethod method, IRSequence sequence) {
        List<IRExpression> expressions = sequence.expressions();
        for (int i = 0; i < expressions.size(); i++) {
            IRExpression expression = expressions.get(i);
            emitExpression(mv, internalName, method, expression);
            if (i < expressions.size() - 1)
                mv.visitInsn(POP);
        }
    }

    private void emitProtectedEval(
            MethodVisitor mv, String internalName, IRMethod method, IRProtectedEval protectedEval) {
        Label start = new Label();
        Label end = new Label();
        Label handler = new Label();
        Label done = new Label();
        int throwableSlot = scratchObjectSlot(method);

        mv.visitTryCatchBlock(start, end, handler, Type.getInternalName(Throwable.class));

        mv.visitLabel(start);
        emitExpression(mv, internalName, method, protectedEval.body());
        if (protectedEval.body().type().kind() != RuntimeValueKind.VOID)
            mv.visitInsn(POP);
        mv.visitLabel(end);
        pushInt(mv, 0);
        boxIfNeeded(mv, RuntimeTypes.INT);
        mv.visitJumpInsn(GOTO, done);

        mv.visitLabel(handler);
        mv.visitVarInsn(ASTORE, throwableSlot);
        mv.visitVarInsn(ALOAD, throwableSlot);
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Throwable.class),
                "getMessage",
                "()Ljava/lang/String;",
                false);

        mv.visitLabel(done);
    }

    private void emitForeachItems(MethodVisitor mv, String internalName, IRMethod method, IRForeachItems foreachItems) {
        emitExpression(mv, internalName, method, foreachItems.source());
        boxIfNeeded(mv, foreachItems.source().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeForeach.class),
                foreachItems.keys() ? "keys" : "items",
                "(Ljava/lang/Object;)Ljava/util/List;",
                false);
    }

    private void emitForeachSize(MethodVisitor mv, String internalName, IRMethod method, IRForeachSize foreachSize) {
        emitExpression(mv, internalName, method, foreachSize.source());
        boxIfNeeded(mv, foreachSize.source().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeForeach.class),
                "size",
                "(Ljava/lang/Object;)I",
                false);
    }

    private void emitForeachValue(MethodVisitor mv, String internalName, IRMethod method, IRForeachValue foreachValue) {
        emitExpression(mv, internalName, method, foreachValue.source());
        boxIfNeeded(mv, foreachValue.source().type());
        emitExpression(mv, internalName, method, foreachValue.key());
        boxIfNeeded(mv, foreachValue.key().type());
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeForeach.class),
                "value",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                false);
    }

    private void emitMappingLiteral(MethodVisitor mv, String internalName, IRMethod method, IRMappingLiteral literal) {
        mv.visitTypeInsn(NEW, "java/util/HashMap");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);

        for (IRMappingEntry entry : literal.entries()) {
            mv.visitInsn(DUP);
            emitExpression(mv, internalName, method, entry.key());
            boxIfNeeded(mv, entry.key().type());
            emitExpression(mv, internalName, method, entry.value());
            boxIfNeeded(mv, entry.value().type());
            mv.visitMethodInsn(
                    INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
        }
    }

    private void emitMappingMerge(MethodVisitor mv, String internalName, IRMethod method, IRMappingMerge merge) {
        mv.visitTypeInsn(NEW, "java/util/HashMap");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, merge.left());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V", true);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, merge.right());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V", true);
    }

    private void emitMappingGet(MethodVisitor mv, String internalName, IRMethod method, IRMappingGet mappingGet) {
        emitExpression(mv, internalName, method, mappingGet.mapping());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        emitExpression(mv, internalName, method, mappingGet.key());
        boxIfNeeded(mv, mappingGet.key().type());
        mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
    }

    private void emitMappingSet(MethodVisitor mv, String internalName, IRMethod method, IRMappingSet mappingSet) {
        emitExpression(mv, internalName, method, mappingSet.mapping());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        emitExpression(mv, internalName, method, mappingSet.key());
        boxIfNeeded(mv, mappingSet.key().type());
        emitExpression(mv, internalName, method, mappingSet.value());
        boxIfNeeded(mv, mappingSet.value().type());
        mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
    }

    private void coerceValue(MethodVisitor mv, RuntimeType source, RuntimeType target) {
        if (target == null || target.kind() == RuntimeValueKind.VOID)
            return;

        if (source != null && target.equals(source))
            return;

        switch (target.kind()) {
        case STATUS:
            boxIfNeeded(mv, source);
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Truth.class),
                    "isTruthy",
                    "(Ljava/lang/Object;)Z",
                    false);
            return;
        case INT:
            if (source != null && source.kind() == RuntimeValueKind.FLOAT) {
                mv.visitInsn(F2I);
                return;
            }
            if (source != null && !source.isReferenceLike())
                return;

            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
            return;
        case FLOAT:
            if (source != null && source.kind() == RuntimeValueKind.INT) {
                mv.visitInsn(I2F);
                return;
            }
            if (source != null && !source.isReferenceLike())
                return;

            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
            return;
        default:
            if (source != null && !source.isReferenceLike())
                boxIfNeeded(mv, source);

            if (target.kind() != RuntimeValueKind.MIXED) {
                mv.visitMethodInsn(
                        INVOKESTATIC,
                        Type.getInternalName(RuntimeCoercions.class),
                        "zeroToNullReference",
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        false);
            }

            if (target.objectInternalName() != null)
                mv.visitTypeInsn(CHECKCAST, target.objectInternalName());
        }
    }

    private void boxIfNeeded(MethodVisitor mv, RuntimeType type) {
        if (type == null)
            return;

        switch (type.kind()) {
        case INT, STATUS -> mv.visitMethodInsn(
                INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        case FLOAT -> mv.visitMethodInsn(
                INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        default -> {}
        }
    }

    private void dupForStore(MethodVisitor mv, RuntimeType type) {
        if (type == null)
            mv.visitInsn(DUP);
        else
            mv.visitInsn(DUP);
    }

    private int scratchObjectSlot(IRMethod method) {
        int slot = 1;
        for (IRParameter parameter : method.parameters()) {
            slot = Math.max(slot, parameter.local().slot() + 1);
        }
        for (IRLocal local : method.locals()) {
            slot = Math.max(slot, local.slot() + 1);
        }
        return slot;
    }

    private int kindToOpcode(RuntimeType type, boolean load) {
        if (type == null)
            return load ? ALOAD : ASTORE;

        return switch (type.kind()) {
        case INT, STATUS -> load ? ILOAD : ISTORE;
        case FLOAT -> load ? FLOAD : FSTORE;
        default -> load ? ALOAD : ASTORE;
        };
    }

    private String descriptor(RuntimeType type) {
        return (type != null) ? type.descriptor() : "Ljava/lang/Object;";
    }

    private String methodDescriptor(IRMethod method) {
        StringBuilder sb = new StringBuilder("(");
        for (IRParameter parameter : method.parameters())
            sb.append(descriptor(parameter.type()));

        sb.append(")");
        sb.append(descriptor(method.returnType()));
        return sb.toString();
    }

    private String buildCallDescriptor(IRInstanceCall call) {
        if (call.parameterTypes() != null && !call.parameterTypes().isEmpty())
            return buildCallDescriptor(call.parameterTypes(), call.type());

        return buildCallDescriptorFromArgs(call.arguments(), call.type());
    }

    private String buildCallDescriptor(List<RuntimeType> parameterTypes, RuntimeType returnType) {
        StringBuilder sb = new StringBuilder("(");
        for (RuntimeType parameterType : parameterTypes)
            sb.append(descriptor(parameterType));
        sb.append(")");
        sb.append(descriptor(returnType));
        return sb.toString();
    }

    private String buildCallDescriptorFromArgs(List<IRExpression> args, RuntimeType returnType) {
        StringBuilder sb = new StringBuilder("(");
        for (IRExpression arg : args)
            sb.append(descriptor(arg.type()));
        sb.append(")");
        sb.append(descriptor(returnType));
        return sb.toString();
    }

    private void pushInt(MethodVisitor mv, int value) {
        if ((value >= -1) && (value <= 5))
            mv.visitInsn(ICONST_0 + value);
        else if ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE))
            mv.visitIntInsn(BIPUSH, value);
        else if ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE))
            mv.visitIntInsn(SIPUSH, value);
        else
            mv.visitLdcInsn(value);
    }

    private void emitMixedZero(MethodVisitor mv) {
        pushInt(mv, 0);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    }

    private static final class SafeClassWriter extends ClassWriter {
        SafeClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                return super.getCommonSuperClass(type1, type2);
            } catch (TypeNotPresentException | LinkageError e) {
                return OBJECT_INTERNAL_NAME;
            }
        }
    }
}
