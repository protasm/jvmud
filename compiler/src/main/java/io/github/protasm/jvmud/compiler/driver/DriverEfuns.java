package io.github.protasm.jvmud.compiler.driver;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Registers the first JVMud driver efuns needed by compiled mudlib objects. */
public final class DriverEfuns {
    private DriverEfuns() {}

    public static void registerCore(RuntimeContext context) {
        Objects.requireNonNull(context, "context");

        for (Efun efun : coreEfuns()) {
            context.registerEfun(efun);
        }
    }

    public static void registerCore(LpcRuntime runtime) {
        Objects.requireNonNull(runtime, "runtime");

        for (Efun efun : coreEfuns()) {
            runtime.registerEfun(efun);
        }
    }

    private static List<Efun> coreEfuns() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(efun("write", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.writeOutput(args[0]);
                    return null;
                }));
        efuns.add(efun("say", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    // Until room/session routing exists, broadcasts are captured in the same sink
                    // as direct writes so tests and the admin CLI can still observe output.
                    runtime.writeOutput(args[0]);
                    return null;
                }));
        efuns.add(efun("tell_object", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.writeOutput(args[1]);
                    return null;
                }));
        efuns.add(efun("this_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentObject()));
        efuns.add(efun("this_player", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentObject()));
        efuns.add(efun("call_other", LPCType.LPCMIXED,
                List.of(LPCType.LPCOBJECT, LPCType.LPCSTRING),
                (runtime, args) -> callOther(args[0], String.valueOf(args[1]))));
        efuns.add(efun("call_other", LPCType.LPCMIXED,
                List.of(LPCType.LPCOBJECT, LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> callOther(args[0], String.valueOf(args[1]), args[2])));
        efuns.add(efun("clone_object", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.cloneObject(String.valueOf(args[0]))));
        efuns.add(efun("move_object", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.moveObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(efun("present", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.present(args[0], null)));
        efuns.add(efun("present", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> runtime.present(args[0], args[1])));
        efuns.add(efun("first_inventory", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.firstInventory(args[0])));
        efuns.add(efun("next_inventory", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.nextInventory(args[0])));
        efuns.add(efun("set_light", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> args[0]));
        efuns.add(efun("set_heart_beat", LPCType.LPCVOID, List.of(LPCType.LPCINT),
                (runtime, args) -> null));
        efuns.add(efun("add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> null));
        efuns.add(efun("add_verb", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> null));
        efuns.add(efun("environment", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.environment(null)));
        efuns.add(efun("destruct", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT),
                (runtime, args) -> {
                    runtime.destructObject(args[0]);
                    return null;
                }));
        return efuns;
    }

    private static Object callOther(Object target, String methodName) {
        return callOther(target, methodName, null);
    }

    private static Object callOther(Object target, String methodName, Object argument) {
        if (target == null) {
            return 0;
        }

        try {
            Object[] args = isNoArgumentSentinel(argument) ? new Object[0] : new Object[] {argument};
            Method method = findMethod(target.getClass(), methodName, args.length);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("call_other failed for method '" + methodName + "'", e);
        }
    }

    private static boolean isNoArgumentSentinel(Object argument) {
        return argument == null || Integer.valueOf(0).equals(argument);
    }

    private static Method findMethod(Class<?> targetClass, String methodName, int arity) throws NoSuchMethodException {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return method;
            }
        }
        throw new NoSuchMethodException(targetClass.getName() + "." + methodName + "/" + arity);
    }

    private static Efun efun(String name, LPCType returnType, List<LPCType> parameters, EfunBody body) {
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return new EfunSignature(new Symbol(returnType, name), parameters);
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                return body.call(context, args);
            }
        };
    }

    @FunctionalInterface
    private interface EfunBody {
        Object call(RuntimeContext context, Object[] args);
    }
}
