package io.github.protasm.jvmud.compiler.runtime;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunRegistry;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.ScheduledTask;
import io.github.protasm.jvmud.runtime.WorldScheduler;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Encapsulates runtime state required by compiled LPC code.
 *
 * <p>This context owns engine function registration, object lifecycle tracking, and include resolution
 * configuration so the compiler and runtime no longer depend on global singletons.</p>
 */
public final class RuntimeContext {
    private final EfunRegistry efunRegistry;
    private final IncludeResolver includeResolver;
    private final Map<String, Object> objects = new LinkedHashMap<>();
    private final Map<Object, String> objectIds = new IdentityHashMap<>();
    private final Map<Object, Object> environments = new IdentityHashMap<>();
    private final Map<Object, List<Object>> inventories = new IdentityHashMap<>();
    private final Map<String, Map<String, Object>> entityAliases = new LinkedHashMap<>();
    private final Map<Object, Map<String, String>> aliasesByEntity = new IdentityHashMap<>();
    private final Map<Object, Integer> lightLevels = new IdentityHashMap<>();
    private final Set<Object> commandEnabledEntities =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, Map<String, List<CommandAction>>> commandActions = new IdentityHashMap<>();
    private final Map<Object, ScheduledTask> recurringTickTasks = new IdentityHashMap<>();
    private final Map<String, SessionBinding> sessions = new LinkedHashMap<>();
    private final Map<Object, SessionBinding> sessionsByPersona = new IdentityHashMap<>();
    private final Map<Object, PendingSessionInput> pendingInputsByPersona = new IdentityHashMap<>();
    private final StringBuilder outputTranscript = new StringBuilder();
    private final ThreadLocal<Deque<Object>> currentObjectStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Object>> commandActorStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<String>> commandVerbStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<String>> pendingActionStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private Consumer<String> outputSink = ignored -> {};
    private Function<String, Object> objectFactory = path -> null;
    private Function<String, Object> objectLoader = path -> null;
    private Function<String, Object> mudlibTextReader = path -> 0;
    private MudlibBoundary mudlibBoundary = MudlibBoundary.empty();
    private WorldScheduler scheduler = new WorldScheduler();
    private String mfunObjectPath;

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

    public void setObjectLoader(Function<String, Object> objectLoader) {
        this.objectLoader = (objectLoader != null) ? objectLoader : path -> null;
    }

    public void setMudlibTextReader(Function<String, Object> mudlibTextReader) {
        this.mudlibTextReader = (mudlibTextReader != null) ? mudlibTextReader : path -> 0;
    }

    public void setMfunObjectPath(String mfunObjectPath) {
        this.mfunObjectPath = normalizeMudlibPath(mfunObjectPath);
    }

    public void setMudlibBoundary(MudlibBoundary mudlibBoundary) {
        this.mudlibBoundary = mudlibBoundary != null ? mudlibBoundary : MudlibBoundary.empty();
    }

    public void setScheduler(WorldScheduler scheduler) {
        this.scheduler = scheduler != null ? scheduler : new WorldScheduler();
    }

    public void setOutputSink(Consumer<String> outputSink) {
        this.outputSink = (outputSink != null) ? outputSink : ignored -> {};
    }

    public void writeOutput(Object value) {
        writeOutputTo(outputTarget(), value);
    }

    public void tellObject(Object target, Object value) {
        writeOutputTo(target, value);
    }

    private void writeOutputTo(Object target, Object value) {
        String text = String.valueOf(value).replace("\\n", "\n");
        outputTranscript.append(text);
        SessionBinding binding = target != null ? sessionsByPersona.get(target) : null;
        if (binding != null) {
            binding.outputSink().accept(text);
        } else {
            outputSink.accept(text);
        }
    }

    public String outputTranscript() {
        return outputTranscript.toString();
    }

    public void clearOutputTranscript() {
        outputTranscript.setLength(0);
    }

    public Efun resolveEfun(String name, int arity) {
        Efun mfun = resolveMfun(name, arity);
        return mfun != null ? mfun : efunRegistry.lookup(name, arity);
    }

    public Object invokeEfun(String name, int arity, Object[] args) {
        Efun efun = resolveMfun(name, arity);

        if (efun == null)
            efun = efunRegistry.lookup(name, arity);

        if (efun == null)
            throw new IllegalArgumentException("Unknown function '" + name + "' with arity " + arity);

        return efun.invoke(this, args);
    }

    private Efun resolveMfun(String name, int arity) {
        if (mfunObjectPath == null) {
            return null;
        }
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return mfunSignature(name, arity);
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                Object mfunObject = loadMfunObject();
                if (!hasMethod(mfunObject, name, args.length)) {
                    Efun efun = efunRegistry.lookup(name, arity);
                    if (efun != null) {
                        return efun.invoke(context, args);
                    }
                    throw new IllegalArgumentException("Unknown function '" + name + "' with arity " + arity);
                }
                return invokeObjectPreservingCurrentObject(mfunObject, name, args);
            }
        };
    }

    private EfunSignature mfunSignature(String name, int arity) {
        if ("sizeof".equals(name) && arity == 1) {
            return new EfunSignature(
                    new Symbol(LPCType.LPCINT, name),
                    List.of(LPCType.LPCMIXED));
        }
        if ("users".equals(name) && arity == 0) {
            return new EfunSignature(
                    new Symbol(LPCType.LPCARRAY, name),
                    List.of());
        }
        if ("query_idle".equals(name) && arity == 1) {
            return new EfunSignature(
                    new Symbol(LPCType.LPCINT, name),
                    List.of(LPCType.LPCMIXED));
        }
        if ("query_ip_number".equals(name) && arity == 1) {
            return new EfunSignature(
                    new Symbol(LPCType.LPCMIXED, name),
                    List.of(LPCType.LPCMIXED));
        }
        return new EfunSignature(
                new Symbol(LPCType.LPCMIXED, name),
                Collections.nCopies(arity, LPCType.LPCMIXED));
    }

    private Object loadMfunObject() {
        Object existing = getObject(mfunObjectPath);
        if (existing != null) {
            return existing;
        }
        Object loaded = objectLoader.apply(mfunObjectPath);
        if (loaded == null) {
            throw new IllegalArgumentException(
                    "Mudlib function object is not available: " + mfunObjectPath);
        }
        return loaded;
    }

    private boolean hasMethod(Object target, String methodName, int arity) {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return true;
            }
        }
        return false;
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

    public Object loadOrGetObject(String name) {
        String normalized = normalizeMudlibPath(name);
        if (normalized == null) {
            return null;
        }
        Object existing = getObject(normalized);
        if (existing != null) {
            return existing;
        }
        return objectLoader.apply(normalized);
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

    public List<Object> users() {
        return sessions.values().stream()
                .map(SessionBinding::persona)
                .toList();
    }

    public void bindSession(String sessionId, Object persona, String remoteAddress, Consumer<String> sessionOutputSink) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(persona, "persona");
        Consumer<String> sink = sessionOutputSink != null ? sessionOutputSink : ignored -> {};
        SessionBinding existing = sessions.get(sessionId);
        if (existing != null) {
            sessionsByPersona.remove(existing.persona());
        }
        SessionBinding previousPersonaBinding = sessionsByPersona.get(persona);
        if (previousPersonaBinding != null) {
            sessions.remove(previousPersonaBinding.sessionId());
        }
        SessionBinding binding = new SessionBinding(
                sessionId,
                persona,
                normalizeSessionText(remoteAddress),
                sink,
                System.currentTimeMillis());
        sessions.put(sessionId, binding);
        sessionsByPersona.put(persona, binding);
    }

    public void unbindSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        SessionBinding binding = sessions.remove(sessionId);
        if (binding != null) {
            sessionsByPersona.remove(binding.persona());
            pendingInputsByPersona.remove(binding.persona());
        }
    }

    public void captureSessionInput(String methodName, boolean noEcho) {
        Objects.requireNonNull(methodName, "methodName");
        Object persona = outputTarget();
        Object handler = currentObject();
        if (persona == null || handler == null || !sessionsByPersona.containsKey(persona)) {
            return;
        }
        pendingInputsByPersona.put(persona, new PendingSessionInput(handler, methodName, noEcho));
    }

    public boolean hasCapturedSessionInput(Object persona) {
        return pendingInputsByPersona.containsKey(persona);
    }

    public Object deliverCapturedSessionInput(Object persona, String line) {
        Objects.requireNonNull(persona, "persona");
        Objects.requireNonNull(line, "line");
        touchPersona(persona);
        PendingSessionInput pendingInput = pendingInputsByPersona.remove(persona);
        if (pendingInput == null) {
            return 0;
        }
        return withCommandActor(persona, () ->
                invokeObject(pendingInput.handler(), pendingInput.methodName(), line));
    }

    public int queryIdle(Object persona) {
        SessionBinding binding = sessionsByPersona.get(persona);
        if (binding == null) {
            return 0;
        }
        long elapsedMillis = Math.max(0L, System.currentTimeMillis() - binding.lastActivityMillis());
        return (int) (elapsedMillis / 1000L);
    }

    public Object queryIpNumber(Object persona) {
        SessionBinding binding = sessionsByPersona.get(persona);
        if (binding == null || binding.remoteAddress() == null) {
            return 0;
        }
        return binding.remoteAddress();
    }

    public Object readMudlibText(String path) {
        return mudlibTextReader.apply(path);
    }

    public void touchPersona(Object persona) {
        SessionBinding binding = sessionsByPersona.get(persona);
        if (binding == null) {
            return;
        }
        SessionBinding touched = binding.touch(System.currentTimeMillis());
        sessions.put(touched.sessionId(), touched);
        sessionsByPersona.put(touched.persona(), touched);
    }

    private String normalizeMudlibPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith(".c")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        return normalized.isBlank() ? null : normalized;
    }

    public Object invokeObject(Object target, String methodName, Object... args) {
        if (target == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findInvocation(target.getClass(), methodName, actualArgs);
            return withCurrentObject(target, () -> {
                try {
                    return invocation.method().invoke(target, invocation.arguments());
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(target)
                                    + causeSummary(e),
                            e);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to call " + methodName + " on " + objectIdOrDescription(target)
                            + causeSummary(e),
                    e);
        }
    }

    private Object invokeObjectPreservingCurrentObject(Object target, String methodName, Object... args) {
        if (target == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findInvocation(target.getClass(), methodName, actualArgs);
            try {
                return invocation.method().invoke(target, invocation.arguments());
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(
                        "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(target)
                                + causeSummary(e),
                        e);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to call " + methodName + " on " + objectIdOrDescription(target)
                            + causeSummary(e),
                    e);
        }
    }

    public void moveObject(Object object, Object destination) {
        Objects.requireNonNull(object, "object");
        if (destination instanceof String path) {
            destination = loadOrGetObject(path);
        }
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

    public void bindEntityAlias(Object object, Object namespace, Object alias) {
        if (object == null) {
            return;
        }

        String normalizedNamespace = normalizeRegistryText(namespace);
        if (normalizedNamespace == null) {
            return;
        }

        Map<String, String> aliases = aliasesByEntity.computeIfAbsent(object, ignored -> new LinkedHashMap<>());
        String existing = aliases.remove(normalizedNamespace);
        if (existing != null) {
            Map<String, Object> namespaceAliases = entityAliases.get(normalizedNamespace);
            if (namespaceAliases != null) {
                namespaceAliases.remove(existing);
                if (namespaceAliases.isEmpty()) {
                    entityAliases.remove(normalizedNamespace);
                }
            }
        }

        String normalizedAlias = normalizeRegistryText(alias);
        if (normalizedAlias == null) {
            if (aliases.isEmpty()) {
                aliasesByEntity.remove(object);
            }
            return;
        }

        aliases.put(normalizedNamespace, normalizedAlias);
        entityAliases
                .computeIfAbsent(normalizedNamespace, ignored -> new LinkedHashMap<>())
                .put(normalizedAlias, object);
    }

    public Object findEntityAlias(Object namespace, Object alias) {
        String normalizedNamespace = normalizeRegistryText(namespace);
        String normalizedAlias = normalizeRegistryText(alias);
        if (normalizedNamespace == null || normalizedAlias == null) {
            return null;
        }
        return entityAliases.getOrDefault(normalizedNamespace, Map.of()).get(normalizedAlias);
    }

    public int entityHasAlias(Object object, Object namespace) {
        String normalizedNamespace = normalizeRegistryText(namespace);
        if (object == null || normalizedNamespace == null) {
            return 0;
        }
        return aliasesByEntity.getOrDefault(object, Map.of()).containsKey(normalizedNamespace) ? 1 : 0;
    }

    public void enableEntityCommands(Object object) {
        if (object != null) {
            commandEnabledEntities.add(object);
        }
    }

    public int entityCommandsEnabled(Object object) {
        return commandEnabledEntities.contains(object) ? 1 : 0;
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

        cancelRecurringTick(object);
        List<Object> contents = new ArrayList<>(inventoryFor(object));
        for (Object child : contents) {
            moveObject(child, null);
        }

        moveObject(object, null);
        inventories.remove(object);
        lightLevels.remove(object);
        removeEntityAliases(object);
        commandEnabledEntities.remove(object);
        commandActions.remove(object);
        pendingInputsByPersona.remove(object);
        removeCommandHandler(object);
        String id = objectIds.remove(object);
        if (id != null) {
            objects.remove(id);
        }
    }

    public void scheduleRecurringTick(int enabled, int intervalSeconds) {
        if (intervalSeconds < 0) {
            throw new IllegalArgumentException("intervalSeconds cannot be negative.");
        }

        Object target = currentObject();
        if (target == null) {
            return;
        }

        cancelRecurringTick(target);
        if (enabled == 0) {
            return;
        }

        long intervalTicks = intervalSeconds > 0
                ? intervalSeconds
                : Math.max(1, mudlibBoundary.temporalTickIntervalSeconds());
        ScheduledTask task = scheduler.scheduleRecurring(
                intervalTicks,
                intervalTicks,
                () -> deliverRecurringTick(target));
        recurringTickTasks.put(target, task);
    }

    private void cancelRecurringTick(Object target) {
        ScheduledTask task = recurringTickTasks.remove(target);
        if (task != null) {
            task.cancel();
        }
    }

    private void deliverRecurringTick(Object target) {
        if (!recurringTickTasks.containsKey(target)) {
            return;
        }

        String methodName = mudlibBoundary.temporalTickMethod().orElse(null);
        if (methodName == null || !hasMethod(target.getClass(), methodName, 0)) {
            return;
        }

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            invokeObject(target, methodName);
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    public Object currentObject() {
        return currentObjectStack.get().peek();
    }

    public Object currentCommandActor() {
        return commandActorStack.get().peek();
    }

    public String currentCommandVerb() {
        return commandVerbStack.get().peek();
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
        touchPersona(actor);
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
        commandVerbStack.get().push(verb);
        try {
            for (CommandAction action : actions) {
                Object environmentBefore = environment(actor);
                Object result = invokeCommandAction(action, argument);
                if (Truth.isTruthy(result)) {
                    return result;
                }
                if (environment(actor) != environmentBefore) {
                    return 1;
                }
            }
        } finally {
            commandVerbStack.get().pop();
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
        return visibleLight(object);
    }

    private int visibleLight(Object object) {
        int light = 0;
        Object current = object;
        while (current != null) {
            light += lightLevels.getOrDefault(current, 0);
            current = environments.get(current);
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

    private InvocationPlan findInvocation(Class<?> targetClass, String methodName, Object[] args)
            throws NoSuchMethodException {
        try {
            return new InvocationPlan(findMethod(targetClass, methodName, args.length), args);
        } catch (NoSuchMethodException exactMiss) {
            Method best = null;
            for (Method method : targetClass.getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() < args.length) {
                    continue;
                }
                if (best == null || method.getParameterCount() < best.getParameterCount()) {
                    best = method;
                }
            }
            if (best == null) {
                throw exactMiss;
            }
            return new InvocationPlan(best, padMissingArguments(best, args));
        }
    }

    private Object[] padMissingArguments(Method method, Object[] args) {
        Object[] padded = new Object[method.getParameterCount()];
        System.arraycopy(args, 0, padded, 0, args.length);
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = args.length; i < padded.length; i++) {
            padded[i] = defaultArgumentValue(parameterTypes[i]);
        }
        return padded;
    }

    private Object defaultArgumentValue(Class<?> parameterType) {
        if (!parameterType.isPrimitive()) {
            return null;
        }
        if (parameterType == boolean.class) {
            return false;
        }
        if (parameterType == float.class) {
            return 0.0f;
        }
        if (parameterType == double.class) {
            return 0.0d;
        }
        if (parameterType == long.class) {
            return 0L;
        }
        if (parameterType == byte.class) {
            return (byte) 0;
        }
        if (parameterType == short.class) {
            return (short) 0;
        }
        if (parameterType == char.class) {
            return (char) 0;
        }
        return 0;
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

    private record InvocationPlan(Method method, Object[] arguments) {}

    private Object outputTarget() {
        Object actor = currentCommandActor();
        return actor != null ? actor : currentObject();
    }

    private String normalizeSessionText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void removeEntityAliases(Object object) {
        Map<String, String> aliases = aliasesByEntity.remove(object);
        if (aliases == null) {
            return;
        }
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            Map<String, Object> namespaceAliases = entityAliases.get(alias.getKey());
            if (namespaceAliases == null) {
                continue;
            }
            namespaceAliases.remove(alias.getValue());
            if (namespaceAliases.isEmpty()) {
                entityAliases.remove(alias.getKey());
            }
        }
    }

    private String normalizeRegistryText(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toString().trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String causeSummary(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getSimpleName();
        }
        return ": " + message;
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

    private record PendingSessionInput(Object handler, String methodName, boolean noEcho) {}

    private record SessionBinding(
            String sessionId,
            Object persona,
            String remoteAddress,
            Consumer<String> outputSink,
            long lastActivityMillis) {
        private SessionBinding touch(long nowMillis) {
            return new SessionBinding(sessionId, persona, remoteAddress, outputSink, nowMillis);
        }
    }
}
