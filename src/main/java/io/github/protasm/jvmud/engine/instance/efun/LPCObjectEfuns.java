package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.Truth;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loading, invocation and inspection of authored LPC objects in their owning mudlib. */
final class LPCObjectEfuns {
    private LPCObjectEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_current_lpc_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentObject()));
        efuns.add(of("jvmud_previous_lpc_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.previousObject()));
        efuns.add(of("jvmud_lpc_object_id", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.objectId(args[0])));
        efuns.add(of("jvmud_method_exists", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> methodExists(runtime, runtime.currentObject(), String.valueOf(args[0])) ? 1 : 0));
        efuns.add(of("jvmud_method_exists", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> methodExists(runtime, args[1], String.valueOf(args[0])) ? 1 : 0));
        efuns.add(of("jvmud_lpc_object_methods", LPCType.LPCARRAY, List.of(LPCType.LPCMIXED),
                (runtime, args) -> lpcObjectMethods(runtime, args[0])));
        efuns.add(of("jvmud_inherited_programs", LPCType.LPCARRAY, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.inheritedPrograms(args[0])));
        efuns.add(of("jvmud_previous_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.previousObject()));
        // These legacy information queries retain their existing LPC-zero placeholder result.
        efuns.add(of("jvmud_lpc_object_info", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(of("jvmud_object_info", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(of("jvmud_configure_lpc_object", LPCType.LPCSTATUS,
                List.of(LPCType.LPCMIXED, LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> configureLpcObject(runtime, args[0], ((Number) args[1]).intValue(), args[2])));
        efuns.add(of("jvmud_configure_object", LPCType.LPCSTATUS,
                List.of(LPCType.LPCMIXED, LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> configureLpcObject(runtime, args[0], ((Number) args[1]).intValue(), args[2])));
        efuns.add(of("jvmud_find_lpc_object", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> findLpcObject(runtime, String.valueOf(args[0]))));
        efuns.add(of("jvmud_find_object", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> findLpcObject(runtime, String.valueOf(args[0]))));
        efuns.add(of("jvmud_to_lpc_object", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> toLpcObject(runtime, args[0])));
        efuns.add(of("jvmud_is_object", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.objectId(args[0]) != null ? 1 : 0));
        efuns.add(invokeLpcObjectEfun());
        efuns.add(of("jvmud_load_lpc_object", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.loadOrGetObject(String.valueOf(args[0]))));
        efuns.add(of("jvmud_clone_lpc_object", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.cloneObject(String.valueOf(args[0]))));
        efuns.add(of("jvmud_destroy_lpc_object", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT),
                (runtime, args) -> {
                    runtime.destructObject(args[0]);
                    return null;
                }));
        return List.copyOf(efuns);
    }

    /** Checks authored public methods on the object's current behavior, excluding generated helpers. */
    static boolean methodExists(MudlibRuntimeContext runtime, Object target, String methodName) {
        if (target == null || (target instanceof Number number && number.intValue() == 0) || methodName == null) {
            return false;
        }
        for (Method method : runtime.objectReceiver(target).getClass().getMethods()) {
            if (!isPublicMudlibMethod(method)) {
                continue;
            }
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> lpcObjectMethods(MudlibRuntimeContext runtime, Object target) {
        if (target == null || (target instanceof Number number && number.intValue() == 0)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (Method method : runtime.objectReceiver(target).getClass().getMethods()) {
            if (!isPublicMudlibMethod(method) || names.contains(method.getName())) {
                continue;
            }
            names.add(method.getName());
        }
        Collections.sort(names);
        return names;
    }

    private static boolean isPublicMudlibMethod(Method method) {
        return !method.isSynthetic()
                && !method.isBridge()
                && method.getDeclaringClass() != Object.class
                && Modifier.isPublic(method.getModifiers())
                && !method.getName().startsWith("$");
    }

    private static Object findLpcObject(MudlibRuntimeContext runtime, String path) {
        Object object = runtime.getObject(path);
        if (object == null) {
            object = runtime.getObject(stripLeadingSlash(path));
        }
        if (object == null) {
            object = runtime.getObject(stripExtension(stripLeadingSlash(path)));
        }
        return object != null ? object : 0;
    }

    private static Object toLpcObject(MudlibRuntimeContext runtime, Object value) {
        if (value == null || Integer.valueOf(0).equals(value)) {
            return 0;
        }
        if (runtime.objectId(value) != null) {
            return value;
        }
        return findLpcObject(runtime, String.valueOf(value));
    }

    private static int configureLpcObject(MudlibRuntimeContext runtime, Object object, int key, Object value) {
        if (key == 0) {
            if (Truth.isTruthy(value)) {
                runtime.enableEntityCommands(object);
            } else {
                runtime.disableEntityCommands(object);
            }
        }
        return 1;
    }

    private static EngineEfunDefinition invokeLpcObjectEfun() {
        return of("jvmud_invoke_lpc_object", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING), LPCType.LPCMIXED,
                (runtime, args) -> invokeLpcObject(runtime, args));
    }

    private static Object invokeLpcObject(MudlibRuntimeContext runtime, Object[] args) {
        Object[] invocationArgs = new Object[Math.max(0, args.length - 2)];
        System.arraycopy(args, 2, invocationArgs, 0, invocationArgs.length);
        return invokeLpcObject(runtime, args[0], String.valueOf(args[1]), invocationArgs);
    }

    private static Object invokeLpcObject(MudlibRuntimeContext runtime, Object target, String methodName, Object... arguments) {
        if (target instanceof List<?> targets) {
            Object result = 0;
            for (Object element : targets) {
                result = invokeLpcObject(runtime, element, methodName, arguments);
            }
            return result;
        }

        Object resolvedTarget = resolveTarget(runtime, target);
        if (resolvedTarget == null) {
            return 0;
        }

        if (arguments.length == 0) {
            return runtime.invokeOptionalObject(resolvedTarget, methodName);
        }

        if (arguments.length == 1 && isNoArgumentSentinel(arguments[0])) {
            if (runtime.hasObjectMethod(resolvedTarget, methodName, 0) && !runtime.hasObjectMethod(resolvedTarget, methodName, 1)) {
                return runtime.invokeOptionalObject(resolvedTarget, methodName);
            }
        }
        return runtime.invokeOptionalObject(resolvedTarget, methodName, arguments);
    }

    /** Resolves a path by loading its object; legacy optional-target operations treat load failure as absent. */
    static Object resolveTarget(MudlibRuntimeContext runtime, Object target) {
        if (target instanceof String path) {
            try {
                return runtime.loadOrGetObject(stripLeadingSlash(path));
            } catch (RuntimeException e) {
                return null;
            }
        }
        return target;
    }

    private static boolean isNoArgumentSentinel(Object argument) {
        return argument == null || Integer.valueOf(0).equals(argument);
    }

    private static String stripLeadingSlash(String path) {
        String stripped = path;
        while (stripped.startsWith("/")) {
            stripped = stripped.substring(1);
        }
        return stripped;
    }

    private static String stripExtension(String path) {
        if (path.endsWith(".c")) {
            return path.substring(0, path.length() - 2);
        }
        return path;
    }
}
