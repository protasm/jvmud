package io.github.protasm.jvmud.compiler.runtime;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunRegistry;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.mudlib.MudlibProjection;
import io.github.protasm.jvmud.engine.output.OutgoingTextFormatter;
import io.github.protasm.jvmud.engine.identity.PersonaId;
import io.github.protasm.jvmud.engine.identity.PersonaRecord;
import io.github.protasm.jvmud.engine.identity.PlayerId;
import io.github.protasm.jvmud.engine.identity.PlayerRecord;
import io.github.protasm.jvmud.engine.time.ScheduledTask;
import io.github.protasm.jvmud.engine.identity.SessionId;
import io.github.protasm.jvmud.engine.identity.SessionRecord;
import io.github.protasm.jvmud.engine.time.WorldScheduler;
import io.github.protasm.jvmud.persistence.jdbc.RuntimeDatabaseService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
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
    private IncludeResolver includeResolver;
    private final Map<String, Object> objects = new LinkedHashMap<>();
    private final Map<Object, String> objectIds = new IdentityHashMap<>();
    private final Set<Object> destroyedObjects =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, Object> environments = new IdentityHashMap<>();
    private final Map<Object, List<Object>> inventories = new IdentityHashMap<>();
    private final Map<String, Map<String, Object>> entityAliases = new LinkedHashMap<>();
    private final Map<Object, Map<String, String>> aliasesByEntity = new IdentityHashMap<>();
    private final Map<Object, Integer> lightLevels = new IdentityHashMap<>();
    private final Set<Object> opaqueEntities =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Object> commandEnabledEntities =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, Map<String, List<CommandAction>>> commandActions = new IdentityHashMap<>();
    private final Map<Object, ScheduledTask> recurringTickTasks = new IdentityHashMap<>();
    private final Map<Object, Map<String, ScheduledTask>> deferredCallbackTasks = new IdentityHashMap<>();
    private final Map<PlayerId, PlayerRecord> players = new LinkedHashMap<>();
    private final Map<SessionId, SessionBinding> sessions = new LinkedHashMap<>();
    private final Map<PersonaId, PersonaRecord> personas = new LinkedHashMap<>();
    private final Map<Object, SessionBinding> sessionsByPersona = new IdentityHashMap<>();
    private final Map<Object, PersonaId> personaIdsByProjection = new IdentityHashMap<>();
    private final Map<Object, PendingSessionInput> pendingInputsByPersona = new IdentityHashMap<>();
    private final RuntimeDatabaseService databaseService = new RuntimeDatabaseService();
    private final StringBuilder outputTranscript = new StringBuilder();
    private final ThreadLocal<Deque<Object>> currentObjectStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Object>> commandActorStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<String>> commandVerbStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<PendingAction>> pendingActionStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Integer> scopedCommandRegistrationDepth =
            ThreadLocal.withInitial(() -> 0);
    private Consumer<String> outputSink = ignored -> {};
    private Function<String, Object> objectFactory = path -> null;
    private Function<String, Object> objectLoader = path -> null;
    private Function<String, Object> mudlibTextReader = path -> 0;
    private BiFunction<String, Integer, Object> mudlibPathLister = (path, flags) -> List.of();
    private BiFunction<String, Object, Integer> mudlibTextAppender = (path, text) -> 0;
    private BiFunction<Object, String, Integer> playerTransferHandler = (actor, gameId) -> 0;
    private BiFunction<String, Object, Integer> lpcObjectStateSaver = (path, object) -> 0;
    private BiFunction<String, Object, Integer> lpcObjectStateRestorer = (path, object) -> 0;
    private TimedRuntimeErrorHandler timedRuntimeErrorHandler = (target, context, operation, error) -> {};
    private Function<Object, Object> objectDestructionRequestedHandler = target -> 0;
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

    public void setIncludeResolver(IncludeResolver includeResolver) {
        this.includeResolver = (includeResolver != null) ? includeResolver : Preprocessor.rejectingResolver();
    }

    public EfunRegistry efunRegistry() {
        return efunRegistry;
    }

    public Preprocessor newPreprocessor() {
        return new Preprocessor(
                includeResolver,
                mudlibBoundary.compatibilityPredefines(),
                mudlibBoundary.compatibilityFunctionPredefines());
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

    /** Sets the host callback used by file-listing efuns to enumerate mudlib-rooted paths. */
    public void setMudlibPathLister(BiFunction<String, Integer, Object> mudlibPathLister) {
        this.mudlibPathLister = (mudlibPathLister != null) ? mudlibPathLister : (path, flags) -> List.of();
    }

    public void setMudlibTextAppender(BiFunction<String, Object, Integer> mudlibTextAppender) {
        this.mudlibTextAppender = (mudlibTextAppender != null) ? mudlibTextAppender : (path, text) -> 0;
    }

    public void setPlayerTransferHandler(BiFunction<Object, String, Integer> playerTransferHandler) {
        this.playerTransferHandler = (playerTransferHandler != null)
                ? playerTransferHandler
                : (actor, gameId) -> 0;
    }

    public void setLPCObjectStateSaver(BiFunction<String, Object, Integer> lpcObjectStateSaver) {
        this.lpcObjectStateSaver = (lpcObjectStateSaver != null) ? lpcObjectStateSaver : (path, object) -> 0;
    }

    public void setLPCObjectStateRestorer(BiFunction<String, Object, Integer> lpcObjectStateRestorer) {
        this.lpcObjectStateRestorer = (lpcObjectStateRestorer != null) ? lpcObjectStateRestorer : (path, object) -> 0;
    }

    public void setTimedRuntimeErrorHandler(TimedRuntimeErrorHandler timedRuntimeErrorHandler) {
        this.timedRuntimeErrorHandler = timedRuntimeErrorHandler != null
                ? timedRuntimeErrorHandler
                : (target, context, operation, error) -> {};
    }

    public void setObjectDestructionRequestedHandler(Function<Object, Object> objectDestructionRequestedHandler) {
        this.objectDestructionRequestedHandler = objectDestructionRequestedHandler != null
                ? objectDestructionRequestedHandler
                : target -> 0;
    }

    public void setMfunObjectPath(String mfunObjectPath) {
        this.mfunObjectPath = normalizeMudlibPath(mfunObjectPath);
    }

    public void setMudlibBoundary(MudlibBoundary mudlibBoundary) {
        this.mudlibBoundary = mudlibBoundary != null ? mudlibBoundary : MudlibBoundary.empty();
        databaseService.configure(
                this.mudlibBoundary.databaseJdbcUrl().orElse(null),
                this.mudlibBoundary.databaseUser().orElse(null),
                this.mudlibBoundary.databasePassword().orElse(null));
    }

    public String directEfunName(String mudlibName) {
        return mudlibBoundary.directEfunAlias(mudlibName).orElse(mudlibName);
    }

    public void setScheduler(WorldScheduler scheduler) {
        this.scheduler = scheduler != null ? scheduler : new WorldScheduler();
    }

    public void setOutputSink(Consumer<String> outputSink) {
        this.outputSink = (outputSink != null) ? outputSink : ignored -> {};
    }

    public void writeOutput(Object value) {
        writeOutputToProjection(outputTarget(), value, false);
    }

    public void tellObject(Object target, Object value) {
        writeOutputToProjection(target, value, true);
    }

    public boolean messageSession(SessionId sessionId, Object value) {
        Objects.requireNonNull(sessionId, "sessionId");
        SessionBinding binding = sessions.get(sessionId);
        if (binding == null) {
            return false;
        }
        deliverToSession(binding, value);
        return true;
    }

    public boolean messagePlayer(PlayerId playerId, Object value) {
        Objects.requireNonNull(playerId, "playerId");
        PlayerRecord player = players.get(playerId);
        if (player == null) {
            return false;
        }
        boolean delivered = false;
        for (SessionId sessionId : player.activeSessionIds()) {
            delivered |= messageSession(sessionId, value);
        }
        return delivered;
    }

    public boolean messagePersona(PersonaId personaId, Object value) {
        Objects.requireNonNull(personaId, "personaId");
        PersonaRecord persona = personas.get(personaId);
        if (persona == null || persona.mudlibBehaviorProjection().isEmpty()) {
            return false;
        }
        SessionBinding binding = sessionsByPersona.get(projectionObject(persona.mudlibBehaviorProjection().orElseThrow()));
        if (binding == null) {
            return false;
        }
        deliverToSession(binding, value);
        return true;
    }

    public void emitPerceivable(Object emitter, Object value) {
        emitPerceivableAtExcept(environment(emitter), value, emitter);
    }

    public void emitPerceivableExcept(Object emitter, Object value, Object excluded) {
        emitPerceivableAtExcept(environment(emitter), value, emitter, excluded);
    }

    public void emitPerceivableAt(Object location, Object value) {
        emitPerceivableAtExcept(location, value);
    }

    private void emitPerceivableAtExcept(Object location, Object value, Object... excluded) {
        if (location == null) {
            return;
        }

        for (Object target : List.copyOf(inventoryFor(location))) {
            if (!isExcluded(target, excluded) && sessionsByPersona.containsKey(target)) {
                writeOutputToProjection(target, value, true);
            }
        }
    }

    private boolean isExcluded(Object target, Object... excluded) {
        for (Object object : excluded) {
            if (target == object) {
                return true;
            }
        }
        return false;
    }

    private void writeOutputToProjection(Object target, Object value, boolean playerBound) {
        SessionBinding binding = target != null ? sessionsByPersona.get(target) : null;
        if (binding != null) {
            if (!playerBound || receivesPlayerBoundMessages(target)) {
                deliverToSession(binding, value);
            }
        } else {
            String text = formattedMessageText(value);
            outputTranscript.append(text);
            outputSink.accept(text);
        }
    }

    private boolean receivesPlayerBoundMessages(Object target) {
        Object resolvedTarget = resolveInvocationTarget(target);
        if (resolvedTarget == null) {
            return true;
        }

        try {
            InvocationPlan invocation = findOptionalInvocation(
                    resolvedTarget.getClass(),
                    "receives_player_bound_messages",
                    new Object[0]);
            Object allowed = withCurrentObject(resolvedTarget, () -> {
                try {
                    return invocation.method().invoke(resolvedTarget, invocation.arguments());
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on "
                                    + objectIdOrDescription(resolvedTarget)
                                    + causeSummary(e),
                            e);
                }
            });
            return Truth.isTruthy(allowed);
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    private void deliverToSession(SessionBinding binding, Object value) {
        String text = formattedMessageText(value);
        outputTranscript.append(text);
        binding.outputSink().accept(text);
    }

    private String formattedMessageText(Object value) {
        return OutgoingTextFormatter.wrap(messageText(value), mudlibBoundary.maxLineLength());
    }

    private String messageText(Object value) {
        return String.valueOf(value).replace("\\n", "\n").replace("\\t", "\t");
    }

    public String outputTranscript() {
        return outputTranscript.toString();
    }

    public void clearOutputTranscript() {
        outputTranscript.setLength(0);
    }

    public Efun resolveEfun(String name, int arity) {
        Efun mfun = resolveMfun(name, arity);
        return mfun != null ? mfun : lookupEngineEfun(name, arity);
    }

    /**
     * Resolves an engine efun without consulting mudlib compatibility functions.
     *
     * <p>This is used for LPC {@code efun::name(...)} calls, where the source explicitly asks to
     * bypass mudlib-level shadowing and invoke the driver/engine function directly.
     */
    public Efun resolveEngineEfun(String name, int arity) {
        return efunRegistry.lookup(name, arity);
    }

    public Object invokeEfun(String name, int arity, Object[] args) {
        Efun efun = resolveMfun(name, arity);

        if (efun == null)
            efun = lookupEngineEfun(name, arity);

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
                    Efun efun = lookupEngineEfun(name, arity);
                    if (efun != null) {
                        return efun.invoke(context, args);
                    }
                    throw new IllegalArgumentException("Unknown function '" + name + "' with arity " + arity);
                }
                return invokeObjectPreservingCurrentObject(mfunObject, name, args);
            }
        };
    }

    private Efun lookupEngineEfun(String name, int arity) {
        Efun efun = efunRegistry.lookup(name, arity);
        if (efun != null) {
            return efun;
        }

        String engineName = directEfunName(name);
        return name.equals(engineName) ? null : efunRegistry.lookup(engineName, arity);
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

    /**
     * Binds an engine object to a mudlib object path.
     *
     * <p>An object has one canonical mudlib path for APIs such as {@code file_name()}.
     * Rebinding removes the object's previous canonical path, and replacing an existing
     * path clears the old occupant's reverse lookup. This lets a mudlib-created object
     * serve a requested missing source path without retaining its temporary clone id as
     * the canonical identity.</p>
     */
    public void registerObject(String name, Object object) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(object, "object");
        String previousName = objectIds.get(object);
        if (previousName != null && !previousName.equals(name) && objects.get(previousName) == object) {
            objects.remove(previousName);
        }
        Object previousObject = objects.get(name);
        if (previousObject != null && previousObject != object) {
            objectIds.remove(previousObject);
        }
        destroyedObjects.remove(object);
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

    /**
     * Returns the transitive LPC program paths inherited by a generated LPC object.
     *
     * <p>Generated bytecode exposes this compiler metadata through a synthetic method. Host objects
     * and older generated classes that do not expose the method are treated as having no LPC
     * inherits rather than failing runtime execution.</p>
     */
    public List<String> inheritedPrograms(Object object) {
        if (object == null) {
            return List.of();
        }
        try {
            Method method = object.getClass().getMethod("$jvmud$inherited_programs");
            Object value = method.invoke(object);
            if (value instanceof String[] paths) {
                return List.of(paths);
            }
            return List.of();
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return List.of();
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not read LPC inherit metadata", e.getCause());
        }
    }

    public boolean isDestroyedObject(Object object) {
        return destroyedObjects.contains(object);
    }

    public Object cloneObject(String sourcePath) {
        return objectFactory.apply(sourcePath);
    }

    public List<Object> users() {
        return sessions.values().stream()
                .map(SessionBinding::personaProjection)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Returns true when the supplied LPC object is currently bound to an active JVMud session. */
    public boolean isInteractive(Object user) {
        return user != null && sessionsByPersona.containsKey(user);
    }

    public void bindPlayerSession(String sessionId, String remoteAddress, Consumer<String> sessionOutputSink) {
        SessionId engineSessionId = new SessionId(sessionId);
        PlayerId playerId = playerIdForSession(engineSessionId);
        Consumer<String> sink = sessionOutputSink != null ? sessionOutputSink : ignored -> {};
        SessionBinding existing = sessions.remove(engineSessionId);
        Optional<Object> mudlibProfileProjection = Optional.empty();
        if (existing != null) {
            mudlibProfileProjection = existing.playerRecord().mudlibProfileProjection();
            detachSessionFromRecords(existing);
        }

        Instant now = Instant.ofEpochMilli(System.currentTimeMillis());
        SessionRecord sessionRecord = new SessionRecord(
                engineSessionId,
                playerId,
                Optional.ofNullable(normalizeSessionText(remoteAddress)),
                now,
                now,
                Optional.empty());
        PlayerRecord playerRecord = new PlayerRecord(
                playerId,
                addSessionId(players.get(playerId), engineSessionId),
                Optional.empty(),
                mudlibProfileProjection);
        SessionBinding binding = new SessionBinding(
                sessionRecord,
                playerRecord,
                Optional.empty(),
                sink);
        players.put(playerId, playerRecord);
        sessions.put(engineSessionId, binding);
    }

    public void bindSession(String sessionId, Object persona, String remoteAddress, Consumer<String> sessionOutputSink) {
        bindSession(sessionId, persona, remoteAddress, sessionOutputSink, null);
    }

    public void bindSession(
            String sessionId,
            Object persona,
            String remoteAddress,
            Consumer<String> sessionOutputSink,
            MudlibProjection mudlibProjection) {
        Objects.requireNonNull(persona, "persona");
        SessionId engineSessionId = new SessionId(sessionId);
        PersonaId personaId = legacyPersonaIdFor(persona);
        Consumer<String> sink = sessionOutputSink != null ? sessionOutputSink : ignored -> {};
        SessionBinding existing = sessions.get(engineSessionId);
        PlayerId playerId = existing != null ? existing.sessionRecord().playerId() : legacyPlayerIdFor(persona);
        Optional<Object> mudlibProfileProjection = Optional.ofNullable(mudlibProjection);
        Instant connectedAt = existing != null ? existing.sessionRecord().connectedAt() : null;
        if (existing != null && mudlibProfileProjection.isEmpty()) {
            mudlibProfileProjection = existing.playerRecord().mudlibProfileProjection();
        }
        if (existing != null) {
            detachSessionFromRecords(existing);
        }
        SessionBinding previousPersonaBinding = sessionsByPersona.get(persona);
        if (previousPersonaBinding != null) {
            sessions.remove(previousPersonaBinding.sessionRecord().id());
            detachSessionFromRecords(previousPersonaBinding);
        }

        long nowMillis = System.currentTimeMillis();
        Instant now = Instant.ofEpochMilli(nowMillis);
        SessionRecord sessionRecord = new SessionRecord(
                engineSessionId,
                playerId,
                Optional.ofNullable(normalizeSessionText(remoteAddress)),
                connectedAt != null ? connectedAt : now,
                now,
                Optional.of(personaId));
        PlayerRecord playerRecord = new PlayerRecord(
                playerId,
                addSessionId(players.get(playerId), engineSessionId),
                Optional.of(personaId),
                mudlibProfileProjection);
        PersonaRecord personaRecord = new PersonaRecord(
                personaId,
                Optional.empty(),
                Optional.of(playerId),
                Optional.ofNullable(mudlibProjection != null ? mudlibProjection : persona));
        SessionBinding binding = new SessionBinding(
                sessionRecord,
                playerRecord,
                Optional.of(personaRecord),
                sink);
        players.put(playerId, playerRecord);
        personas.put(personaId, personaRecord);
        sessions.put(engineSessionId, binding);
        sessionsByPersona.put(persona, binding);
        personaIdsByProjection.put(persona, personaId);
    }

    public void unbindSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        SessionBinding binding = sessions.remove(new SessionId(sessionId));
        if (binding != null) {
            detachSessionFromRecords(binding);
        }
    }

    public Optional<SessionRecord> sessionRecord(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        SessionBinding binding = sessions.get(new SessionId(sessionId));
        return binding != null ? Optional.of(binding.sessionRecord()) : Optional.empty();
    }

    public Optional<PlayerRecord> playerRecordForSession(String sessionId) {
        return sessionRecord(sessionId)
                .map(SessionRecord::playerId)
                .map(players::get);
    }

    public Optional<PersonaRecord> personaRecordForProjection(Object persona) {
        PersonaId personaId = personaIdsByProjection.get(persona);
        return personaId != null ? Optional.ofNullable(personas.get(personaId)) : Optional.empty();
    }

    /**
     * Captures the next line of interactive session input for the current output persona.
     *
     * <p>The line is delivered to {@code methodName} on the current LPC object. Any supplied extra
     * arguments are appended after the typed line, matching legacy {@code input_to()} callback
     * shapes such as {@code input_to("method", flags, arg1, arg2)}.</p>
     */
    public void captureSessionInput(String methodName, boolean noEcho, Object... extraArgs) {
        Objects.requireNonNull(methodName, "methodName");
        Object persona = outputTarget();
        Object handler = currentObject();
        if (persona == null || handler == null || !sessionsByPersona.containsKey(persona)) {
            return;
        }
        Object[] capturedArgs = extraArgs == null ? new Object[0] : extraArgs.clone();
        pendingInputsByPersona.put(persona, new PendingSessionInput(handler, methodName, noEcho, capturedArgs));
    }

    public boolean hasCapturedSessionInput(Object persona) {
        return pendingInputsByPersona.containsKey(persona);
    }

    public boolean capturedSessionInputNoEcho(Object persona) {
        PendingSessionInput pendingInput = pendingInputsByPersona.get(persona);
        return pendingInput != null && pendingInput.noEcho();
    }

    public Object deliverCapturedSessionInput(Object persona, String line) {
        Objects.requireNonNull(persona, "persona");
        Objects.requireNonNull(line, "line");
        touchPersona(persona);
        PendingSessionInput pendingInput = pendingInputsByPersona.remove(persona);
        if (pendingInput == null) {
            return 0;
        }
        Object[] extraArgs = pendingInput.extraArgs();
        Object[] invocationArgs = new Object[extraArgs.length + 1];
        invocationArgs[0] = line;
        System.arraycopy(extraArgs, 0, invocationArgs, 1, extraArgs.length);
        return withCommandActor(persona, () ->
                invokeObject(pendingInput.handler(), pendingInput.methodName(), invocationArgs));
    }

    public int queryIdle(Object persona) {
        SessionBinding binding = sessionsByPersona.get(persona);
        if (binding == null) {
            return 0;
        }
        long elapsedMillis = Math.max(0L, System.currentTimeMillis()
                - binding.sessionRecord().lastActivityAt().toEpochMilli());
        return (int) (elapsedMillis / 1000L);
    }

    public Object queryIpNumber(Object persona) {
        SessionBinding binding = sessionsByPersona.get(persona);
        if (binding == null || binding.sessionRecord().remoteAddress().isEmpty()) {
            return 0;
        }
        return binding.sessionRecord().remoteAddress().orElseThrow();
    }

    public Object readMudlibText(String path) {
        return mudlibTextReader.apply(path);
    }

    /** Lists mudlib-rooted paths using an LP-style flag word. */
    public Object listMudlibPaths(String path, int flags) {
        return mudlibPathLister.apply(path, flags);
    }

    public int appendMudlibText(String path, Object text) {
        return mudlibTextAppender.apply(path, text);
    }

    public int transferCurrentPlayerToGame(String gameId) {
        String normalizedGameId = normalizeRegistryText(gameId);
        if (normalizedGameId == null) {
            return 0;
        }
        Object actor = outputTarget();
        return actor != null ? playerTransferHandler.apply(actor, normalizedGameId) : 0;
    }

    /** Opens a configured JDBC database connection and returns a JVMud database handle. */
    public int dbConnect(String databaseName) {
        return databaseService.connect(databaseName, null, null);
    }

    /** Opens a JDBC database connection with mudlib-supplied credentials. */
    public int dbConnect(String databaseName, String user, String password) {
        return databaseService.connect(databaseName, user, password);
    }

    /** Executes SQL for a JVMud database handle and returns the handle on success. */
    public int dbExec(int handle, String sql) {
        return databaseService.execute(handle, sql);
    }

    /** Fetches one row from a JVMud database handle's current result cursor. */
    public Object dbFetch(int handle) {
        return databaseService.fetch(handle);
    }

    /** Returns the last JDBC error for a JVMud database handle, or LPC false when clear. */
    public Object dbError(int handle) {
        return databaseService.error(handle);
    }

    /** Closes a JVMud database handle. */
    public int dbClose(int handle) {
        return databaseService.close(handle);
    }

    /** Returns the currently open JVMud database handles. */
    public List<Integer> dbHandles() {
        return databaseService.handles();
    }

    /** Escapes a string for interpolation into mudlib-generated SQL. */
    public String dbEscape(String value) {
        return databaseService.escape(value);
    }

    public int saveCurrentLPCObjectState(String path) {
        Object object = currentObject();
        return object != null ? lpcObjectStateSaver.apply(path, object) : 0;
    }

    public int restoreCurrentLPCObjectState(String path) {
        Object object = currentObject();
        return object != null ? lpcObjectStateRestorer.apply(path, object) : 0;
    }

    public void touchPersona(Object persona) {
        SessionBinding binding = sessionsByPersona.get(persona);
        if (binding == null) {
            return;
        }
        SessionBinding touched = binding.touch(Instant.ofEpochMilli(System.currentTimeMillis()));
        sessions.put(touched.sessionRecord().id(), touched);
        sessionsByPersona.put(touched.personaProjection(), touched);
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
        Object resolvedTarget = resolveInvocationTarget(target);
        if (resolvedTarget == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findInvocation(resolvedTarget.getClass(), methodName, actualArgs);
            return withCurrentObject(resolvedTarget, () -> {
                try {
                    return invocation.method().invoke(resolvedTarget, invocation.arguments());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + describeInvocation(invocation, resolvedTarget)
                                    + causeSummary(e),
                            e);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof LinkageError linkageError) {
                        throw new IllegalStateException("Failed to call generated LPC method", linkageError);
                    }
                    throw new IllegalArgumentException("Failed to call generated LPC method" + causeSummary(targetException), e);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(resolvedTarget)
                                    + causeSummary(e),
                            e);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to call " + methodName + " on " + objectIdOrDescription(resolvedTarget)
                            + causeSummary(e),
                    e);
        }
    }

    public Object invokeOptionalObject(Object target, String methodName, Object... args) {
        Object resolvedTarget = resolveInvocationTarget(target);
        if (resolvedTarget == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findOptionalInvocation(resolvedTarget.getClass(), methodName, actualArgs);
            return withCurrentObject(resolvedTarget, () -> {
                try {
                    return invocation.method().invoke(resolvedTarget, invocation.arguments());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + describeInvocation(invocation, resolvedTarget)
                                    + causeSummary(e),
                            e);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    String message = new StringBuilder("Failed to call ")
                            .append(safeDescribeInvocation(invocation, resolvedTarget))
                            .append(safeCauseSummary(targetException))
                            .toString();
                    if (targetException instanceof LinkageError linkageError) {
                        throw new IllegalStateException(message, linkageError);
                    }
                    throw new IllegalArgumentException(message, e);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(resolvedTarget)
                                    + causeSummary(e),
                            e);
                } catch (LinkageError e) {
                    throw new IllegalStateException(
                            "Failed to link " + describeInvocation(invocation, resolvedTarget)
                                    + causeSummary(e),
                            e);
                }
            });
        } catch (NoSuchMethodException e) {
            return 0;
        }
    }

    private Object invokeObjectPreservingCurrentObject(Object target, String methodName, Object... args) {
        target = resolveInvocationTarget(target);
        if (target == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findInvocation(target.getClass(), methodName, actualArgs);
            try {
                return invocation.method().invoke(target, invocation.arguments());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Failed to call " + describeInvocation(invocation, target)
                                + causeSummary(e),
                        e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof LinkageError linkageError) {
                    throw new IllegalStateException("Failed to call generated LPC method", linkageError);
                }
                throw new IllegalArgumentException("Failed to call generated LPC method" + causeSummary(targetException), e);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(
                        "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(target)
                                + causeSummary(e),
                        e);
            } catch (LinkageError e) {
                throw new IllegalStateException(
                        "Failed to link " + describeInvocation(invocation, target)
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
            invokeArrivalLifecycle(object, destination);
        }
    }

    private void invokeArrivalLifecycle(Object object, Object destination) {
        if (!sessionsByPersona.containsKey(destination)) {
            return;
        }

        String methodName = mudlibBoundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElse(null);
        if (methodName == null || !hasMethod(object.getClass(), methodName, 0)) {
            return;
        }

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            withCommandActor(destination, () -> withScopedCommandRegistration(() -> {
                invokeObject(object, methodName);
                return null;
            }));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
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

        Object found = findPresent(identifier, targetContainer);
        if (found != null) {
            return found;
        }

        if (container == null) {
            Object ambientContainer = environment(targetContainer);
            if (ambientContainer != null && ambientContainer != targetContainer) {
                return findPresent(identifier, ambientContainer);
            }
        }

        return null;
    }

    private Object findPresent(Object identifier, Object container) {
        for (Object object : inventoryFor(container)) {
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

    public void setEntityTranslucent(Object object, boolean translucent) {
        if (object == null) {
            return;
        }
        if (translucent) {
            opaqueEntities.remove(object);
        } else {
            opaqueEntities.add(object);
        }
    }

    public boolean entityTranslucent(Object object) {
        return object == null || !opaqueEntities.contains(object);
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
        Object preparation = objectDestructionRequestedHandler.apply(object);
        if (!isLpcFalse(preparation)) {
            return;
        }

        SessionBinding binding = sessionsByPersona.get(object);
        if (binding != null) {
            sessions.remove(binding.sessionRecord().id());
            detachSessionFromRecords(binding);
        }
        cancelRecurringTick(object);
        cancelDeferredCallbacks(object);
        List<Object> contents = new ArrayList<>(inventoryFor(object));
        for (Object child : contents) {
            moveObject(child, null);
        }

        moveObject(object, null);
        inventories.remove(object);
        lightLevels.remove(object);
        opaqueEntities.remove(object);
        removeEntityAliases(object);
        commandEnabledEntities.remove(object);
        commandActions.remove(object);
        pendingInputsByPersona.remove(object);
        removeCommandHandler(object);
        String id = objectIds.remove(object);
        if (id != null) {
            objects.remove(id);
        }
        destroyedObjects.add(object);
    }

    private boolean isLpcFalse(Object value) {
        return value == null || Integer.valueOf(0).equals(value);
    }

    public void scheduleDeferredCallback(String methodName, int delaySeconds, Object... args) {
        if (methodName == null || methodName.isBlank()) {
            return;
        }
        if (delaySeconds < 0) {
            throw new IllegalArgumentException("delaySeconds cannot be negative.");
        }

        Object target = currentObject();
        if (target == null) {
            return;
        }

        Map<String, ScheduledTask> tasks = deferredCallbackTasks.computeIfAbsent(target, ignored -> new LinkedHashMap<>());
        ScheduledTask previous = tasks.remove(methodName);
        if (previous != null) {
            previous.cancel();
        }

        Object[] invocationArgs = args == null ? new Object[0] : args.clone();
        ScheduledTask task = scheduler.scheduleAfter(delaySeconds, () -> deliverDeferredCallback(target, methodName, invocationArgs));
        tasks.put(methodName, task);
    }

    public int cancelDeferredCallback(String methodName) {
        if (methodName == null) {
            return -1;
        }

        Object target = currentObject();
        if (target == null) {
            return -1;
        }

        Map<String, ScheduledTask> tasks = deferredCallbackTasks.get(target);
        if (tasks == null) {
            return -1;
        }

        ScheduledTask task = tasks.remove(methodName);
        if (task == null) {
            return -1;
        }

        task.cancel();
        if (tasks.isEmpty()) {
            deferredCallbackTasks.remove(target);
        }
        return 0;
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

        long intervalTicks = intervalSeconds > 0 ? intervalSeconds : 1;
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

    private void cancelDeferredCallbacks(Object target) {
        Map<String, ScheduledTask> tasks = deferredCallbackTasks.remove(target);
        if (tasks == null) {
            return;
        }

        for (ScheduledTask task : tasks.values()) {
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
        } catch (RuntimeException | LinkageError e) {
            cancelRecurringTick(target);
            timedRuntimeErrorHandler.onError(target, "scheduled_tick", methodName, e);
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    private void deliverDeferredCallback(Object target, String methodName, Object[] args) {
        Map<String, ScheduledTask> tasks = deferredCallbackTasks.get(target);
        if (tasks != null) {
            tasks.remove(methodName);
            if (tasks.isEmpty()) {
                deferredCallbackTasks.remove(target);
            }
        }

        if (!objectIds.containsKey(target) || !hasMethod(target.getClass(), methodName, args.length)) {
            return;
        }

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            invokeObject(target, methodName, args);
        } catch (RuntimeException | LinkageError e) {
            timedRuntimeErrorHandler.onError(target, "deferred_callback", methodName, e);
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    @FunctionalInterface
    public interface TimedRuntimeErrorHandler {
        void onError(Object target, String context, String operation, Throwable error);
    }

    public Object currentObject() {
        return currentObjectStack.get().peek();
    }

    public Object previousObject() {
        Deque<Object> stack = currentObjectStack.get();
        if (stack.size() < 2) {
            return stack.peek();
        }

        java.util.Iterator<Object> iterator = stack.iterator();
        iterator.next();
        return iterator.next();
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
        pendingActionStack.get().push(new PendingAction(
                Objects.requireNonNull(methodName, "methodName"),
                scopedCommandRegistrationDepth.get() == 0));
    }

    public void registerVerb(String verb) {
        registerVerb(verb, false);
    }

    public void registerVerb(String verb, boolean prefixMatch) {
        Objects.requireNonNull(verb, "verb");
        Deque<PendingAction> pendingActions = pendingActionStack.get();
        if (pendingActions.isEmpty()) {
            return;
        }

        Object actor = currentCommandActor();
        Object handler = currentObject();
        if (actor == null || handler == null) {
            return;
        }

        PendingAction pendingAction = pendingActions.pop();
        commandActions
                .computeIfAbsent(actor, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(verb, ignored -> new ArrayList<>())
                .add(new CommandAction(handler, pendingAction.methodName(), prefixMatch, pendingAction.persistent()));
    }

    public void clearCommandActions(Object actor) {
        Map<String, List<CommandAction>> actionsByVerb = commandActions.get(actor);
        if (actionsByVerb == null) {
            return;
        }
        for (List<CommandAction> actions : actionsByVerb.values()) {
            actions.removeIf(action -> !action.persistent());
        }
        actionsByVerb.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        if (actionsByVerb.isEmpty()) {
            commandActions.remove(actor);
        }
    }

    public void clearPendingActionMethods() {
        pendingActionStack.get().clear();
    }

    public <T> T withScopedCommandRegistration(Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        scopedCommandRegistrationDepth.set(scopedCommandRegistrationDepth.get() + 1);
        try {
            return action.get();
        } finally {
            scopedCommandRegistrationDepth.set(scopedCommandRegistrationDepth.get() - 1);
        }
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

        List<CommandAction> actions = commandActionsFor(actor, verb);
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

    private List<CommandAction> commandActionsFor(Object actor, String verb) {
        Map<String, List<CommandAction>> actionsByVerb = commandActions.getOrDefault(actor, Map.of());
        List<CommandAction> actions = new ArrayList<>(actionsByVerb.getOrDefault(verb, List.of()));
        for (Map.Entry<String, List<CommandAction>> entry : actionsByVerb.entrySet()) {
            if (entry.getKey().equals(verb) || !verb.startsWith(entry.getKey())) {
                continue;
            }
            for (CommandAction action : entry.getValue()) {
                if (action.prefixMatch()) {
                    actions.add(action);
                }
            }
        }
        return actions;
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
        return visibleLight(object, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private int visibleLight(Object object, Set<Object> visited) {
        Object root = outermostEnvironment(object);
        return visibleLightIn(root, visited);
    }

    private Object outermostEnvironment(Object object) {
        Object root = object;
        Object current = environments.get(object);
        while (current != null) {
            root = current;
            current = environments.get(current);
        }
        return root;
    }

    private int visibleLightIn(Object object, Set<Object> visited) {
        if (object == null || !visited.add(object)) {
            return 0;
        }

        int light = lightLevels.getOrDefault(object, 0);
        if (!entityTranslucent(object)) {
            return light;
        }
        for (Object child : inventoryFor(object)) {
            light += visibleLightIn(child, visited);
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
        boolean hasOneArgument = hasMethod(action.handler().getClass(), action.methodName(), 1);
        boolean hasNoArguments = hasMethod(action.handler().getClass(), action.methodName(), 0);
        if (argument != null && hasOneArgument) {
            return invokeObject(action.handler(), action.methodName(), argument);
        }
        if (argument == null && hasOneArgument && !hasNoArguments) {
            return invokeObject(action.handler(), action.methodName(), argument);
        }
        if (hasNoArguments) {
            return invokeObject(action.handler(), action.methodName());
        }
        return 0;
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
            Method method = findMethod(targetClass, methodName, args.length);
            return new InvocationPlan(method, adaptArguments(method, args));
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
            return new InvocationPlan(best, adaptArguments(best, padMissingArguments(best, args)));
        }
    }

    private InvocationPlan findOptionalInvocation(Class<?> targetClass, String methodName, Object[] args)
            throws NoSuchMethodException {
        try {
            return findInvocation(targetClass, methodName, args);
        } catch (NoSuchMethodException exactMiss) {
            Method best = null;
            for (Method method : targetClass.getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() > args.length) {
                    continue;
                }
                if (best == null || method.getParameterCount() > best.getParameterCount()) {
                    best = method;
                }
            }
            if (best == null) {
                throw exactMiss;
            }
            Object[] trimmed = new Object[best.getParameterCount()];
            System.arraycopy(args, 0, trimmed, 0, trimmed.length);
            return new InvocationPlan(best, adaptArguments(best, trimmed));
        }
    }

    private Object[] adaptArguments(Method method, Object[] args) {
        Object[] adapted = new Object[args.length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            adapted[i] = adaptArgument(parameterTypes[i], args[i]);
        }
        return adapted;
    }

    private Object adaptArgument(Class<?> parameterType, Object arg) {
        if (arg == null) {
            return defaultArgumentValue(parameterType);
        }
        if (parameterType == String.class) {
            if (arg instanceof Number number && number.intValue() == 0) {
                return null;
            }
            return String.valueOf(arg);
        }
        if (parameterType == boolean.class && arg instanceof Number number) {
            return number.intValue() != 0;
        }
        if (parameterType == int.class && arg instanceof Number number) {
            return number.intValue();
        }
        if (parameterType == long.class && arg instanceof Number number) {
            return number.longValue();
        }
        if (parameterType == float.class && arg instanceof Number number) {
            return number.floatValue();
        }
        if (parameterType == double.class && arg instanceof Number number) {
            return number.doubleValue();
        }
        if (parameterType == byte.class && arg instanceof Number number) {
            return number.byteValue();
        }
        if (parameterType == short.class && arg instanceof Number number) {
            return number.shortValue();
        }
        if (parameterType == char.class && arg instanceof Number number) {
            return (char) number.intValue();
        }
        if (!parameterType.isPrimitive()) {
            return arg;
        }
        return arg;
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

    private Object resolveInvocationTarget(Object target) {
        if (target instanceof String path) {
            return loadOrGetObject(path);
        }
        if (destroyedObjects.contains(target)) {
            return null;
        }
        return target;
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

    private PlayerId legacyPlayerIdFor(Object persona) {
        return new PlayerId("legacy-player/" + objectReference(persona));
    }

    private PlayerId playerIdForSession(SessionId sessionId) {
        return new PlayerId("session-player/" + sessionId.value());
    }

    private PersonaId legacyPersonaIdFor(Object persona) {
        PersonaId existing = personaIdsByProjection.get(persona);
        if (existing != null) {
            return existing;
        }
        return new PersonaId("legacy-persona/" + objectReference(persona));
    }

    private String objectReference(Object object) {
        String id = objectId(object);
        if (id != null && !id.isBlank()) {
            return id;
        }
        return object.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(object));
    }

    private Object projectionObject(Object projection) {
        return projection instanceof MudlibProjection mudlibProjection ? mudlibProjection.object() : projection;
    }

    private Set<SessionId> addSessionId(PlayerRecord playerRecord, SessionId sessionId) {
        Set<SessionId> sessionIds = new HashSet<>();
        if (playerRecord != null) {
            sessionIds.addAll(playerRecord.activeSessionIds());
        }
        sessionIds.add(sessionId);
        return sessionIds;
    }

    private void detachSessionFromRecords(SessionBinding binding) {
        Object persona = binding.personaProjection();
        if (persona != null) {
            sessionsByPersona.remove(persona);
            pendingInputsByPersona.remove(persona);
        }

        SessionRecord sessionRecord = binding.sessionRecord();
        PlayerRecord playerRecord = players.get(sessionRecord.playerId());
        if (playerRecord != null) {
            Set<SessionId> remainingSessions = new HashSet<>(playerRecord.activeSessionIds());
            remainingSessions.remove(sessionRecord.id());
            PlayerRecord updatedPlayer = new PlayerRecord(
                    playerRecord.id(),
                    remainingSessions,
                    Optional.empty(),
                    playerRecord.mudlibProfileProjection());
            players.put(updatedPlayer.id(), updatedPlayer);
        }

        binding.personaRecord().ifPresent(boundPersona -> {
            PersonaRecord personaRecord = personas.get(boundPersona.id());
            if (personaRecord != null) {
                personas.put(personaRecord.id(), new PersonaRecord(
                        personaRecord.id(),
                        personaRecord.entity(),
                        Optional.empty(),
                        personaRecord.mudlibBehaviorProjection()));
            }
        });
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
        int depth = 0;
        while (cause.getCause() != null && depth < 25) {
            cause = cause.getCause();
            depth++;
        }
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getSimpleName();
        }
        return new StringBuilder(": ").append(message).toString();
    }

    /**
     * Summarizes a reflected failure while preserving the original throwable if cause inspection
     * itself trips over a linkage problem.
     */
    private String safeCauseSummary(Throwable throwable) {
        try {
            return causeSummary(throwable);
        } catch (LinkageError | RuntimeException e) {
            return ": " + throwable.getClass().getSimpleName();
        }
    }

    private String describeInvocation(InvocationPlan invocation, Object target) {
        Method method = invocation.method();
        StringBuilder builder = new StringBuilder(method.getName())
                .append("/")
                .append(method.getParameterCount())
                .append(" on ")
                .append(objectIdOrDescription(target))
                .append(" with parameter types [");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterTypes[i].getSimpleName());
        }
        builder.append("] and argument types [");
        Object[] args = invocation.arguments();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(args[i] == null ? "null" : args[i].getClass().getSimpleName());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Describes a reflective LPC call without letting diagnostic formatting hide the original
     * invocation failure.
     */
    private String safeDescribeInvocation(InvocationPlan invocation, Object target) {
        try {
            return describeInvocation(invocation, target);
        } catch (LinkageError | RuntimeException e) {
            return invocation.method().getName() + "/" + invocation.method().getParameterCount()
                    + " on " + target.getClass().getName();
        }
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
            InvocationPlan invocation = findInvocation(object.getClass(), "id", new Object[] {identifier.toString()});
            return Truth.isTruthy(invocation.method().invoke(object, invocation.arguments()));
        } catch (NoSuchMethodException e) {
            // Fall back to exact object id matching below.
        } catch (ReflectiveOperationException e) {
            return false;
        }

        return identifier.toString().equals(objectId(object));
    }

    private record PendingAction(String methodName, boolean persistent) {}

    private record CommandAction(Object handler, String methodName, boolean prefixMatch, boolean persistent) {}

    private record PendingSessionInput(Object handler, String methodName, boolean noEcho, Object[] extraArgs) {
        private PendingSessionInput {
            extraArgs = extraArgs == null ? new Object[0] : extraArgs.clone();
        }

        @Override
        public Object[] extraArgs() {
            return extraArgs.clone();
        }
    }

    private record SessionBinding(
            SessionRecord sessionRecord,
            PlayerRecord playerRecord,
            Optional<PersonaRecord> personaRecord,
            Consumer<String> outputSink) {
        private Object personaProjection() {
            return personaRecord
                    .flatMap(PersonaRecord::mudlibBehaviorProjection)
                    .map(projection -> projection instanceof MudlibProjection mudlibProjection
                            ? mudlibProjection.object()
                            : projection)
                    .orElse(null);
        }

        private SessionBinding touch(Instant now) {
            SessionRecord touchedSession = new SessionRecord(
                    sessionRecord.id(),
                    sessionRecord.playerId(),
                    sessionRecord.remoteAddress(),
                    sessionRecord.connectedAt(),
                    now,
                    sessionRecord.attachedPersonaId());
            return new SessionBinding(touchedSession, playerRecord, personaRecord, outputSink);
        }
    }
}
