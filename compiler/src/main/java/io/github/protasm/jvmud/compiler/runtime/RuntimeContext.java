package io.github.protasm.jvmud.compiler.runtime;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunRegistry;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Encapsulates runtime state required by compiled LPC code.
 *
 * <p>This context owns efun registration, object lifecycle tracking, and include resolution
 * configuration so the compiler and runtime no longer depend on global singletons.</p>
 */
public final class RuntimeContext {
    private final EfunRegistry efunRegistry;
    private final IncludeResolver includeResolver;
    private final Map<String, Object> objects = new LinkedHashMap<>();
    private final Map<Object, String> objectIds = new IdentityHashMap<>();
    private final Map<Object, Object> environments = new IdentityHashMap<>();
    private final Map<Object, List<Object>> inventories = new IdentityHashMap<>();
    private final Map<Object, Integer> lightLevels = new IdentityHashMap<>();
    private final Map<Object, Map<String, List<CommandAction>>> commandActions = new IdentityHashMap<>();
    private final StringBuilder outputTranscript = new StringBuilder();
    private final ThreadLocal<Deque<Object>> currentObjectStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Object>> commandActorStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<String>> pendingActionStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private Consumer<String> outputSink = ignored -> {};
    private Function<String, Object> objectFactory = path -> null;

    public RuntimeContext(IncludeResolver includeResolver) {
        this(includeResolver, new EfunRegistry());
    }

    public RuntimeContext(IncludeResolver includeResolver, EfunRegistry efunRegistry) {
        this.includeResolver = (includeResolver != null) ? includeResolver : Preprocessor.rejectingResolver();
        this.efunRegistry = Objects.requireNonNull(efunRegistry, "efunRegistry");
    }

    public IncludeResolver includeResolver() {
        return includeResolver;
    }

    public EfunRegistry efunRegistry() {
        return efunRegistry;
    }

    public Preprocessor newPreprocessor() {
        return new Preprocessor(includeResolver);
    }

    public void registerEfun(Efun efun) {
        efunRegistry.register(efun);
    }

    public void setObjectFactory(Function<String, Object> objectFactory) {
        this.objectFactory = (objectFactory != null) ? objectFactory : path -> null;
    }

    public void setOutputSink(Consumer<String> outputSink) {
        this.outputSink = (outputSink != null) ? outputSink : ignored -> {};
    }

    public void writeOutput(Object value) {
        String text = String.valueOf(value).replace("\\n", "\n");
        outputTranscript.append(text);
        outputSink.accept(text);
    }

    public String outputTranscript() {
        return outputTranscript.toString();
    }

    public void clearOutputTranscript() {
        outputTranscript.setLength(0);
    }

    public Efun resolveEfun(String name, int arity) {
        return efunRegistry.lookup(name, arity);
    }

    public Object invokeEfun(String name, int arity, Object[] args) {
        Efun efun = efunRegistry.lookup(name, arity);

        if (efun == null)
            throw new IllegalArgumentException("Unknown efun '" + name + "' with arity " + arity);

        return efun.invoke(this, args);
    }

    public void registerObject(String name, Object object) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(object, "object");
        objects.put(name, object);
        objectIds.put(object, name);
    }

    public Object getObject(String name) {
        return objects.get(name);
    }

    public Map<String, Object> objectsView() {
        return Collections.unmodifiableMap(objects);
    }

    public Map<String, Object> objects() {
        return objects;
    }

    public String objectId(Object object) {
        return objectIds.get(object);
    }

    public Object cloneObject(String sourcePath) {
        return objectFactory.apply(sourcePath);
    }

    public Object invokeObject(Object target, String methodName, Object... args) {
        if (target == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            Method method = findMethod(target.getClass(), methodName, actualArgs.length);
            return withCurrentObject(target, () -> {
                try {
                    return method.invoke(target, actualArgs);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + methodName + " on " + objectIdOrDescription(target), e);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to call " + methodName + " on " + objectIdOrDescription(target), e);
        }
    }

    public void moveObject(Object object, Object destination) {
        Objects.requireNonNull(object, "object");
        if (destination != null && wouldCreateContainmentCycle(object, destination)) {
            throw new IllegalArgumentException(
                    "Cannot move " + objectIdOrDescription(object) + " into "
                            + objectIdOrDescription(destination) + " because it would create a containment cycle.");
        }

        Object oldEnvironment = environments.remove(object);
        if (oldEnvironment != null) {
            inventoryFor(oldEnvironment).remove(object);
        }

        if (destination != null) {
            environments.put(object, destination);
            inventoryFor(destination).add(object);
        }
    }

    public Object environment(Object object) {
        Object target = (object != null) ? object : currentObject();
        return (target != null) ? environments.get(target) : null;
    }

    public Object present(Object identifier, Object container) {
        Object targetContainer = (container != null) ? container : currentObject();
        if (targetContainer == null) {
            return null;
        }

        for (Object object : inventoryFor(targetContainer)) {
            if (object == identifier || matchesIdentifier(object, identifier)) {
                return object;
            }
        }
        return null;
    }

    public Object firstInventory(Object container) {
        List<Object> inventory = inventoryFor(container);
        return inventory.isEmpty() ? null : inventory.get(0);
    }

    public Object nextInventory(Object object) {
        Object container = environment(object);
        if (container == null) {
            return null;
        }

        List<Object> inventory = inventoryFor(container);
        int index = inventory.indexOf(object);
        if (index == -1 || index + 1 >= inventory.size()) {
            return null;
        }
        return inventory.get(index + 1);
    }

    public void destructObject(Object object) {
        if (object == null) {
            return;
        }

        List<Object> contents = new ArrayList<>(inventoryFor(object));
        for (Object child : contents) {
            moveObject(child, null);
        }

        moveObject(object, null);
        inventories.remove(object);
        lightLevels.remove(object);
        commandActions.remove(object);
        removeCommandHandler(object);
        String id = objectIds.remove(object);
        if (id != null) {
            objects.remove(id);
        }
    }

    public Object currentObject() {
        return currentObjectStack.get().peek();
    }

    public Object currentCommandActor() {
        return commandActorStack.get().peek();
    }

    public void pushCurrentObject(Object object) {
        Objects.requireNonNull(object, "object");
        currentObjectStack.get().push(object);
    }

    public void popCurrentObject() {
        Deque<Object> stack = currentObjectStack.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("No current LPC object is available to pop.");
        }
        stack.pop();
    }

    public <T> T withCurrentObject(Object object, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        pushCurrentObject(object);
        try {
            return action.get();
        } finally {
            popCurrentObject();
        }
    }

    public void runWithCurrentObject(Object object, Runnable action) {
        Objects.requireNonNull(action, "action");
        withCurrentObject(object, () -> {
            action.run();
            return null;
        });
    }

    public <T> T withCommandActor(Object actor, Supplier<T> action) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(action, "action");
        commandActorStack.get().push(actor);
        try {
            return action.get();
        } finally {
            commandActorStack.get().pop();
        }
    }

    public void rememberActionMethod(String methodName) {
        pendingActionStack.get().push(Objects.requireNonNull(methodName, "methodName"));
    }

    public void registerVerb(String verb) {
        Objects.requireNonNull(verb, "verb");
        Deque<String> pendingActions = pendingActionStack.get();
        if (pendingActions.isEmpty()) {
            return;
        }

        Object actor = currentCommandActor();
        Object handler = currentObject();
        if (actor == null || handler == null) {
            return;
        }

        String methodName = pendingActions.pop();
        commandActions
                .computeIfAbsent(actor, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(verb, ignored -> new ArrayList<>())
                .add(new CommandAction(handler, methodName));
    }

    public void clearCommandActions(Object actor) {
        commandActions.remove(actor);
    }

    public void clearPendingActionMethods() {
        pendingActionStack.get().clear();
    }

    public Object dispatchCommand(Object actor, String commandLine) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(commandLine, "commandLine");
        String trimmed = commandLine.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }

        String verb = trimmed;
        String argument = null;
        int space = trimmed.indexOf(' ');
        if (space != -1) {
            verb = trimmed.substring(0, space);
            argument = trimmed.substring(space + 1).trim();
            if (argument.isEmpty()) {
                argument = null;
            }
        }

        List<CommandAction> actions = commandActions
                .getOrDefault(actor, Map.of())
                .getOrDefault(verb, List.of());
        for (CommandAction action : actions) {
            Object result = invokeCommandAction(action, argument);
            if (Truth.isTruthy(result)) {
                return result;
            }
        }
        return 0;
    }

    public int setLight(int delta) {
        Object object = currentObject();
        if (object == null) {
            return delta;
        }
        int light = lightLevels.getOrDefault(object, 0);
        if (delta != 0) {
            light += delta;
            lightLevels.put(object, light);
        }
        return light;
    }

    private List<Object> inventoryFor(Object object) {
        return inventories.computeIfAbsent(object, ignored -> new ArrayList<>());
    }

    private boolean wouldCreateContainmentCycle(Object object, Object destination) {
        Object current = destination;
        while (current != null) {
            if (current == object) {
                return true;
            }
            current = environments.get(current);
        }
        return false;
    }

    private Object invokeCommandAction(CommandAction action, String argument) {
        if (argument != null || hasMethod(action.handler().getClass(), action.methodName(), 1)) {
            return invokeObject(action.handler(), action.methodName(), argument);
        }
        return invokeObject(action.handler(), action.methodName());
    }

    private Method findMethod(Class<?> targetClass, String methodName, int arity) throws NoSuchMethodException {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return method;
            }
        }
        throw new NoSuchMethodException(targetClass.getName() + "." + methodName + "/" + arity);
    }

    private boolean hasMethod(Class<?> targetClass, String methodName, int arity) {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return true;
            }
        }
        return false;
    }

    private String objectIdOrDescription(Object object) {
        String id = objectId(object);
        return id != null ? id : String.valueOf(object);
    }

    private void removeCommandHandler(Object handler) {
        for (Map<String, List<CommandAction>> actionsByVerb : commandActions.values()) {
            actionsByVerb.values().forEach(actions -> actions.removeIf(action -> action.handler() == handler));
            actionsByVerb.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
        commandActions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private boolean matchesIdentifier(Object object, Object identifier) {
        if (identifier == null) {
            return false;
        }

        try {
            for (var method : object.getClass().getMethods()) {
                if (method.getName().equals("id") && method.getParameterCount() == 1) {
                    return Truth.isTruthy(method.invoke(object, identifier.toString()));
                }
            }
        } catch (ReflectiveOperationException e) {
            return false;
        }

        return identifier.toString().equals(objectId(object));
    }

    private record CommandAction(Object handler, String methodName) {}
}
