package io.github.protasm.jvmud.compiler.runtime;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunRegistry;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
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
    private final StringBuilder outputTranscript = new StringBuilder();
    private final ThreadLocal<Deque<Object>> currentObjectStack =
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
        String text = String.valueOf(value);
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

    public void moveObject(Object object, Object destination) {
        Objects.requireNonNull(object, "object");
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
        String id = objectIds.remove(object);
        if (id != null) {
            objects.remove(id);
        }
    }

    public Object currentObject() {
        return currentObjectStack.get().peek();
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

    private List<Object> inventoryFor(Object object) {
        return inventories.computeIfAbsent(object, ignored -> new ArrayList<>());
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
}
