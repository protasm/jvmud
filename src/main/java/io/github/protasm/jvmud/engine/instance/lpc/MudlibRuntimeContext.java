package io.github.protasm.jvmud.engine.instance.lpc;

import io.github.protasm.jvmud.communication.output.OutgoingTextFormatter;
import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.session.InstanceSession;
import io.github.protasm.jvmud.engine.instance.session.InstanceSessions;
import io.github.protasm.jvmud.engine.instance.session.PersonaAttachment;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedPersonaProfile;
import io.github.protasm.jvmud.engine.model.identity.PersonaID;
import io.github.protasm.jvmud.engine.model.identity.PersonaRecord;
import io.github.protasm.jvmud.engine.model.identity.PlayerID;
import io.github.protasm.jvmud.engine.model.identity.PlayerRecord;
import io.github.protasm.jvmud.engine.model.identity.SessionID;
import io.github.protasm.jvmud.engine.model.identity.SessionRecord;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibProjection;
import io.github.protasm.jvmud.engine.model.time.ScheduledTask;
import io.github.protasm.jvmud.engine.model.time.WorldScheduler;
import io.github.protasm.jvmud.engine.model.world.MudlibWorldProjection;
import io.github.protasm.jvmud.engine.model.world.PerceptionEvent;
import io.github.protasm.jvmud.engine.model.world.World;
import io.github.protasm.jvmud.engine.model.world.WorldRuntime;
import io.github.protasm.jvmud.language.compiler.LPCCompilationEnvironment;
import io.github.protasm.jvmud.language.compiler.LPCCompilationOptions;
import io.github.protasm.jvmud.language.compiler.LPCCompilerContext;
import io.github.protasm.jvmud.language.compiler.LPCGlobalFunction;
import io.github.protasm.jvmud.language.compiler.LPCGlobalFunctions;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.language.compiler.preprocessor.IncludeResolver;
import io.github.protasm.jvmud.language.compiler.preprocessor.Preprocessor;
import io.github.protasm.jvmud.language.compiler.token.TokenList;
import io.github.protasm.jvmud.language.efun.Efun;
import io.github.protasm.jvmud.language.efun.EfunRegistry;
import io.github.protasm.jvmud.language.efun.EfunSignature;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeCoercions;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;
import io.github.protasm.jvmud.language.runtime.Truth;
import io.github.protasm.jvmud.storage.jdbc.RuntimeDatabaseService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Engine-owned state and services for executing LPC inside one mudlib instance.
 *
 * <p>World projection, commands, scheduling and storage integration live here. Connection and
 * Player/Persona attachment state belongs to {@link InstanceSessions}; this context adapts LPC calls
 * to that owner. Generated code sees only the language's {@link RuntimeContext} contract. Compilation uses a
 * separate {@link LPCCompilerContext}; this adapter shares callable signatures with it without
 * allowing compiler passes to access live engine state.</p>
 *
 * <p>A running instance confines mutations to its execution thread. The isolated defaults also make
 * this host usable by deterministic runtime tests before the instance starts that thread.</p>
 */
public final class MudlibRuntimeContext extends RuntimeContext implements LPCCompilationEnvironment {
    private final LPCObjectLifecycle objectLifecycle = new LPCObjectLifecycle();
    private final EfunRegistry efunRegistry;
    private final LPCCompilerContext compilerContext;
    private final Map<String, Object> objects = new LinkedHashMap<>();
    private final Map<Object, String> objectIds = new IdentityHashMap<>();
    private final Set<Object> destroyedObjects =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private MudlibWorldProjection worldProjection = new MudlibWorldProjection(
            new WorldRuntime(new World("mudlib-runtime", "Mudlib Runtime")));
    private final Map<String, Map<String, Object>> entityAliases = new LinkedHashMap<>();
    private final Map<Object, Map<String, String>> aliasesByEntity = new IdentityHashMap<>();
    private final Map<Object, Integer> lightLevels = new IdentityHashMap<>();
    private final Set<Object> opaqueEntities =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Object> commandEnabledEntities =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private long commandActionSequence;
    private final Map<Object, Map<String, List<CommandAction>>> commandActions = new IdentityHashMap<>();
    private final Map<String, String> commandAliases = new LinkedHashMap<>();
    private final Map<Object, ScheduledTask> recurringTickTasks = new IdentityHashMap<>();
    private final Map<Object, Map<String, List<DeferredCallback>>> deferredCallbackTasks =
            new IdentityHashMap<>();
    private long nextDeferredCallbackId = 1;
    private final InstanceSessions instanceSessions;
    private final RuntimeDatabaseService databaseService = new RuntimeDatabaseService();
    private final StringBuilder outputTranscript = new StringBuilder();
    private final ThreadLocal<Deque<Object>> currentObjectStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Object>> commandActorStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<String>> commandVerbStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<String>> commandFailureStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<PendingAction>> pendingActionStack =
            ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Integer> scopedCommandRegistrationDepth =
            ThreadLocal.withInitial(() -> 0);
    private Consumer<String> outputSink = ignored -> {};
    private Function<String, Object> objectFactory = path -> null;
    private Function<String, Object> objectLoader = path -> null;
    private Function<String, Object> mudlibTextReader = path -> 0;
    private Function<String, Object> mudlibJsonReader = path -> 0;
    private MudlibJSONArrayReader mudlibJsonArrayReader = (path, pointer, offset, count) -> 0;
    private BiFunction<String, Integer, Object> mudlibPathLister = (path, flags) -> List.of();
    private BiFunction<String, Object, Integer> mudlibTextAppender = (path, text) -> 0;
    private Function<String, Integer> mudlibTextRemover = path -> 0;
    private BiFunction<String, String, Integer> mudlibTextCopier = (source, destination) -> -1;
    private BiFunction<String, String, Integer> mudlibTextRenamer = (source, destination) -> -1;
    private Function<String, Integer> mudlibDirectoryCreator = path -> 0;
    private Function<String, Integer> mudlibDirectoryRemover = path -> 0;
    private BiFunction<String, Object, Integer> lpcObjectStateSaver = (path, object) -> 0;
    private BiFunction<String, Object, Integer> lpcObjectStateRestorer = (path, object) -> 0;
    private TimedRuntimeErrorHandler timedRuntimeErrorHandler = (target, context, operation, error) -> {};
    private Function<Object, Object> objectDestructionRequestedHandler = target -> 0;
    private MudlibConfiguration configuration = MudlibConfiguration.empty();
    private WorldScheduler scheduler = new WorldScheduler();

    public MudlibRuntimeContext(IncludeResolver includeResolver) {
        this(includeResolver, new EfunRegistry());
    }

    public MudlibRuntimeContext(IncludeResolver includeResolver, EfunRegistry efunRegistry) {
        this.efunRegistry = Objects.requireNonNull(efunRegistry, "efunRegistry");
        this.compilerContext = new LPCCompilerContext(includeResolver, efunRegistry);
        this.instanceSessions = new InstanceSessions(this::objectReference, outputTranscript::append);
    }

    /** Returns the single owner of this instance's connection and Persona attachment state. */
    public InstanceSessions sessions() {
        return instanceSessions;
    }

    @Override
    public IncludeResolver includeResolver() {
        return compilerContext.includeResolver();
    }

    public void setIncludeResolver(IncludeResolver includeResolver) {
        compilerContext.setIncludeResolver(includeResolver);
    }

    public EfunRegistry efunRegistry() {
        return efunRegistry;
    }

    /**
     * Selects the native world projection that owns mudlib containment state.
     *
     * <p>Hosted instances set this during boot before loading world objects. Unstarted mudlib hosts
     * retain an isolated native world by default.</p>
     */
    public void setWorldProjection(MudlibWorldProjection worldProjection) {
        this.worldProjection = Objects.requireNonNull(worldProjection, "worldProjection");
    }

    /** Returns the native world projection currently used for LPC containment operations. */
    public MudlibWorldProjection worldProjection() {
        return worldProjection;
    }

    @Override
    public Preprocessor newPreprocessor() {
        return compilerContext.newPreprocessor();
    }

    /** Selects the parser profile used for runtime-loaded mudlib source. */
    public void setParserOptions(ParserOptions parserOptions) {
        compilerContext.setParserOptions(parserOptions);
    }

    /** Returns the parser profile used for runtime-loaded mudlib source. */
    public ParserOptions parserOptions() {
        return compilerContext.parserOptions();
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

    /**
     * Sets the host callback used to parse mudlib-owned JSON files.
     *
     * @param mudlibJsonReader host reader, or {@code null} to restore the unavailable reader
     */
    public void setMudlibJsonReader(Function<String, Object> mudlibJsonReader) {
        this.mudlibJsonReader = (mudlibJsonReader != null) ? mudlibJsonReader : path -> 0;
    }

    /**
     * Sets the host callback used for bounded, structural reads of mudlib JSON arrays.
     *
     * @param mudlibJsonArrayReader host reader, or {@code null} to restore the unavailable reader
     */
    public void setMudlibJsonArrayReader(MudlibJSONArrayReader mudlibJsonArrayReader) {
        this.mudlibJsonArrayReader = (mudlibJsonArrayReader != null)
                ? mudlibJsonArrayReader
                : (path, pointer, offset, count) -> 0;
    }

    /** Sets the host callback used by file-listing efuns to enumerate mudlib-rooted paths. */
    public void setMudlibPathLister(BiFunction<String, Integer, Object> mudlibPathLister) {
        this.mudlibPathLister = (mudlibPathLister != null) ? mudlibPathLister : (path, flags) -> List.of();
    }

    public void setMudlibTextAppender(BiFunction<String, Object, Integer> mudlibTextAppender) {
        this.mudlibTextAppender = (mudlibTextAppender != null) ? mudlibTextAppender : (path, text) -> 0;
    }

    public void setMudlibTextRemover(Function<String, Integer> mudlibTextRemover) {
        this.mudlibTextRemover = (mudlibTextRemover != null) ? mudlibTextRemover : path -> 0;
    }

    public void setMudlibTextCopier(BiFunction<String, String, Integer> mudlibTextCopier) {
        this.mudlibTextCopier = (mudlibTextCopier != null) ? mudlibTextCopier : (source, destination) -> -1;
    }

    public void setMudlibTextRenamer(BiFunction<String, String, Integer> mudlibTextRenamer) {
        this.mudlibTextRenamer = (mudlibTextRenamer != null) ? mudlibTextRenamer : (source, destination) -> -1;
    }

    public void setMudlibDirectoryCreator(Function<String, Integer> mudlibDirectoryCreator) {
        this.mudlibDirectoryCreator = (mudlibDirectoryCreator != null) ? mudlibDirectoryCreator : path -> 0;
    }

    public void setMudlibDirectoryRemover(Function<String, Integer> mudlibDirectoryRemover) {
        this.mudlibDirectoryRemover = (mudlibDirectoryRemover != null) ? mudlibDirectoryRemover : path -> 0;
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

    /**
     * Registers source text for an in-memory LPC object that may later serve as a global helper.
     *
     * <p>Host APIs such as {@code MudlibRuntime.loadSource(...)} can compile an LPC object without
     * writing a corresponding {@code .c} file. Declaration-backed global lookup still needs source
     * text to recover function signatures, so those hosts register the source here under the
     * object's normalized mudlib path.</p>
     */
    public void registerInMemoryObjectSource(String objectPath, String source, String displayPath) {
        compilerContext.registerInMemoryObjectSource(objectPath, source, displayPath);
    }

    /**
     * Sets the mudlib-owned global mfun object path used by older callers.
     *
     * @deprecated use {@link #configureMudlib(MudlibConfiguration)} with
     *     {@link MudlibConfiguration#mudlibGlobalObjectPath()} metadata.
     */
    @Deprecated(forRemoval = false)
    public void setMfunObjectPath(String mfunObjectPath) {
        compilerContext.setMfunObjectPath(mfunObjectPath);
    }

    @Override
    public boolean dynamicTypes() {
        return compilerContext.dynamicTypes();
    }

    /** Applies checked local types and method varargs declarations before semantic resolution. */
    @Override
    public void transpileSourceLocals(Path sourcePath, ASTObject object) {
        compilerContext.transpileSourceLocals(sourcePath, object);
    }

    /** Applies only the source translations explicitly selected by this mudlib's bridge. */
    @Override
    public TokenList transpileSourceTokens(Path sourcePath, TokenList tokens) {
        return compilerContext.transpileSourceTokens(sourcePath, tokens);
    }

    /** Sets active mudlib boundary metadata for generated-code helpers and compatibility lookup. */
    public void configureMudlib(MudlibConfiguration configuration) {
        this.configuration = configuration != null ? configuration : MudlibConfiguration.empty();
        MudlibConfiguration selected = this.configuration;
        compilerContext.setOptions(new LPCCompilationOptions(
                selected.mudlibRootPath().orElse(null),
                selected.dynamicTypes(),
                selected.transpileUntypedMethods(),
                selected.transpileImplicitSelfCalls(),
                selected.fieldTypeOverrides(),
                selected.localTypeOverrides(),
                selected.methodVarargsOverrides(),
                selected.compatibilityPredefines(),
                selected.compatibilityFunctionPredefines()));
        compilerContext.setGlobalFunctions(new LPCGlobalFunctions(
                selected.mudlibGlobalObjectPath().orElse(null),
                selected.mudlibGlobalObjectSourcePath().orElse(null),
                selected.compatibilityGlobalObjectPath().orElse(null),
                selected.compatibilityGlobalObjectSourcePath().orElse(null),
                selected.compatibilityGlobalOverrides(),
                selected.engineFunctionAliases()));
        databaseService.configure(
                selected.databaseJdbcUrl().orElse(null),
                selected.databaseUser().orElse(null),
                selected.databasePassword().orElse(null));
    }

    @Override
    public String engineFunctionName(String mudlibName) {
        return compilerContext.engineFunctionName(mudlibName);
    }

    public void setScheduler(WorldScheduler scheduler) {
        this.scheduler = scheduler != null ? scheduler : new WorldScheduler();
    }

    public void setOutputSink(Consumer<String> outputSink) {
        this.outputSink = (outputSink != null) ? outputSink : ignored -> {};
    }

    /** Writes text to the current execution recipient, or to the transcript in detached contexts. */
    public void write(Object value) {
        writeToProjection(outputTarget(), value, false);
    }

    /**
     * Writes text only to the target object's bound session.
     *
     * <p>Unlike {@link #write(Object)}, this targeted write deliberately does not fall back to the
     * ambient output transcript when the target is not interactive. If that target is later bound to
     * a Session, JVMud flushes the pending text to the target's own output sink.</p>
     */
    public void writeToLpcObject(Object target, Object value) {
        target = objectLifecycle.identityOf(target);
        if (isLiveBehaviorObject(target) && receivesPlayerBoundMessages(target)) {
            String text = formattedMessageText(value);
            if (!instanceSessions.writeToObject(target, text)) {
                instanceSessions.bufferOutput(target, text);
            }
        }
    }

    /** Writes engine control-plane or transport text to one bound Session. */
    public boolean writeToSession(SessionID sessionId, Object value) {
        return instanceSessions.writeToSession(Objects.requireNonNull(sessionId, "sessionId"), formattedMessageText(value));
    }

    /** Writes engine control-plane text to all active Sessions for one Player. */
    public boolean writeToPlayer(PlayerID playerId, Object value) {
        return instanceSessions.writeToPlayer(Objects.requireNonNull(playerId, "playerId"), formattedMessageText(value));
    }

    /** Writes engine gameplay text to the bound Session for one Persona. */
    public boolean writeToPersona(PersonaID personaId, Object value) {
        return instanceSessions.writeToPersona(Objects.requireNonNull(personaId, "personaId"), formattedMessageText(value));
    }

    public void emitPerceivable(Object emitter, Object value) {
        emitWorldEvent(emitter, "text", value, messageText(value));
    }

    public void emitPerceivableExcept(Object emitter, Object value, Object excluded) {
        emitAt(environment(emitter), new PerceptionEvent(currentCommandActor(), emitter,
                environment(emitter), "text", value, messageText(value), false), emitter, excluded);
    }

    public void emitPerceivableAt(Object location, Object value) {
        emitAt(location, new PerceptionEvent(currentCommandActor(), currentObject(), location,
                "text", value, messageText(value), false));
    }

    /** Emits a structured event to the location and its immediate contents, excluding the source. */
    public void emitWorldEvent(Object source, String kind, Object content, String text) {
        Object location = environment(source);
        emitAt(location, new PerceptionEvent(currentCommandActor(), source, location,
                kind, content, text, false), source);
    }

    /** Directed world communication, distinct from private interface output. */
    public void deliverWorldEvent(Object target, String kind, Object content, String text) {
        if (target != null) {
            perceive(target, new PerceptionEvent(currentCommandActor(), currentObject(), environment(target),
                    kind, content, text, true));
        }
    }

    private void emitAt(Object location, PerceptionEvent event, Object... excluded) {
        if (location == null) return;
        List<Object> observers = new ArrayList<>();
        observers.add(location);
        observers.addAll(List.copyOf(inventoryFor(location)));
        for (Object observer : observers) {
            if (!isExcluded(observer, excluded)) perceive(observer, event);
        }
    }

    // Nested speech is legitimate. Bound cycles explicitly instead of suppressing responses.
    private int perceptionDepth;

    private void perceive(Object observer, PerceptionEvent event) {
        if (destroyedObjects.contains(observer)) return;
        if (perceptionDepth >= 64) throw new IllegalStateException("Perception callback nesting exceeds 64");
        perceptionDepth++;
        try {
            if (instanceSessions.isInteractive(observer)) writeToProjection(observer, event.text(), true);
            Supplier<Object> delivery = () -> {
                String receiver = configuration.lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_RECEIVED).orElse(null);
                if (receiver != null) invokeOptionalObject(observer, receiver, event.toMapping());
                String adapter = configuration.lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_DELIVERY).orElse(null);
                if (adapter != null && !destroyedObjects.contains(observer)) {
                    Object boundary = configuration.boundaryObjectPath().map(this::loadOrGetObject).orElse(null);
                    if (boundary != null) invokeOptionalObject(boundary, adapter, observer, event.toMapping());
                }
                return null;
            };
            if (event.actor() == null) delivery.get();
            else withCommandActor(event.actor(), delivery);
        } finally {
            perceptionDepth--;
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

    private void writeToProjection(Object target, Object value, boolean playerBound) {
        target = objectLifecycle.identityOf(target);
        if (instanceSessions.isInteractive(target)) {
            if (!playerBound || receivesPlayerBoundMessages(target)) {
                instanceSessions.writeToObject(target, formattedMessageText(value));
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

    private String formattedMessageText(Object value) {
        return OutgoingTextFormatter.wrap(messageText(value), configuration.maxLineLength());
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

    /**
     * Returns whether a function name exists regardless of call arity, including mfuns and aliases.
     * Used to keep malformed calls to known functions out of implicit-self translation.
     */
    @Override
    public boolean hasFunctionNamed(String name) {
        return compilerContext.hasFunctionNamed(name);
    }

    /** Whether this runtime explicitly enables translation of unresolved bare calls to self. */
    @Override
    public boolean transpileImplicitSelfCalls() {
        return compilerContext.transpileImplicitSelfCalls();
    }

    @Override
    public EfunSignature resolveFunctionSignature(String name, int arity) {
        return compilerContext.resolveFunctionSignature(name, arity);
    }

    @Override
    public EfunSignature resolveEngineFunctionSignature(String name, int arity) {
        return compilerContext.resolveEngineFunctionSignature(name, arity);
    }

    public Efun resolveEfun(String name, int arity) {
        Efun global = resolveGlobalFunction(name, arity);
        return global != null ? global : lookupEngineEfun(name, arity);
    }

    /**
     * Resolves an engine efun without consulting mudlib compatibility functions.
     *
     * <p>This is used for LPC {@code efun::name(...)} calls, where the source explicitly asks to
     * bypass mudlib-level shadowing and invoke the JVMud-native engine function directly.
     */
    public Efun resolveEngineEfun(String name, int arity) {
        return efunRegistry.lookup(name, arity);
    }

    /**
     * Invokes a resolved native name for an explicit {@code efun::} or {@code jvmud::}
     * call, bypassing mudlib shadowing even when the mfun uses the native name itself.
     */
    public Object invokeEngineEfun(String name, int arity, Object[] args) {
        Efun efun = resolveEngineEfun(name, arity);
        if (efun == null)
            throw new IllegalArgumentException("Unknown engine function '" + name + "' with arity " + arity);
        return efun.invoke(this, args);
    }

    /**
     * Invokes a mudlib-visible function through the active compatibility boundary.
     *
     * <p>Mfuns normally shadow engine efuns. When the mudlib profile maps a mudlib-visible name to
     * an engine function and the call carries a first-class callable, the mapped engine function is
     * allowed to run before a string-typed mfun wrapper can coerce the callable into display text.
     * </p>
     */
    public Object invokeEfun(String name, int arity, Object[] args) {
        Efun callableAlias = resolveCallableEngineFunction(name, arity, args);
        if (callableAlias != null) {
            return callableAlias.invoke(this, args);
        }

        Efun efun = resolveGlobalFunction(name, arity);

        if (efun == null)
            efun = lookupEngineEfun(name, arity);

        if (efun == null)
            throw new IllegalArgumentException("Unknown function '" + name + "' with arity " + arity);

        return efun.invoke(this, args);
    }

    private Efun resolveCallableEngineFunction(String name, int arity, Object[] args) {
        String engineName = engineFunctionName(name);
        if (name.equals(engineName) || !hasCallableArgument(args)) {
            return null;
        }
        return efunRegistry.lookup(engineName, arity);
    }

    private static boolean hasCallableArgument(Object[] args) {
        if (args == null) {
            return false;
        }
        for (Object arg : args) {
            if (arg instanceof RuntimeCallable) {
                return true;
            }
        }
        return false;
    }

    private Efun resolveGlobalFunction(String name, int arity) {
        LPCGlobalFunction declaration = compilerContext.resolveGlobalFunction(name, arity);
        if (declaration == null) {
            return null;
        }
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return declaration.signature();
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                Object globalObject = loadGlobalObject(declaration.objectPath());
                return invokeObjectPreservingCurrentObject(globalObject, name, args);
            }
        };
    }

    private Efun lookupEngineEfun(String name, int arity) {
        Efun efun = efunRegistry.lookup(name, arity);
        if (efun != null) {
            return efun;
        }

        String engineName = engineFunctionName(name);
        return name.equals(engineName) ? null : efunRegistry.lookup(engineName, arity);
    }

    /** Loads the global receiver only when its declared function is actually invoked. */
    private Object loadGlobalObject(String objectPath) {
        Object existing = getObject(objectPath);
        if (existing != null) {
            return existing;
        }
        Object loaded = objectLoader.apply(objectPath);
        if (loaded == null) {
            throw new IllegalArgumentException(
                    "Global function object is not available: " + objectPath);
        }
        return loaded;
    }

    private boolean hasMethod(Object target, String methodName, int arity) {
        for (Method method : objectReceiver(target).getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return true;
            }
        }
        return false;
    }

    /** Owns the stable identity and replaceable behavior of this instance's LPC objects. */
    LPCObjectLifecycle objectLifecycle() {
        return objectLifecycle;
    }

    /** Returns the current Java behavior receiver for an LPC identity or historical receiver. */
    public Object objectReceiver(Object object) {
        return objectLifecycle.receiverOf(object);
    }

    /** Reports whether an object's code is currently on this instance's execution stack. */
    boolean isObjectExecuting(Object object) {
        return currentObjectStack.get().contains(objectLifecycle.identityOf(object));
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
        objectLifecycle.register(object);
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
        return objectIds.get(objectLifecycle.identityOf(object));
    }

    /**
     * Returns the LPC program path and transitive inherited program paths for a generated LPC
     * object.
     *
     * <p>JVMud exposes this as the backing data for LPMud {@code inherit_list} compatibility.
     * Generated bytecode exposes inherited compiler metadata through a synthetic method. The
     * object registry supplies the object's own program path. Host objects and older generated
     * classes that do not expose the method are treated as having no LPC inherits rather than
     * failing runtime execution.</p>
     */
    public List<String> inheritedPrograms(Object object) {
        if (object == null) {
            return List.of();
        }
        List<String> programs = new ArrayList<>();
        String ownProgram = objectProgramPath(object);
        if (ownProgram != null) {
            programs.add(ownProgram);
        }
        try {
            Object receiver = objectReceiver(object);
            Method method = receiver.getClass().getMethod("$jvmud$inherited_programs");
            Object value = method.invoke(receiver);
            if (value instanceof String[] paths) {
                for (String path : paths) {
                    if (!programs.contains(path)) {
                        programs.add(path);
                    }
                }
            }
            return List.copyOf(programs);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return List.copyOf(programs);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not read LPC inherit metadata", e.getCause());
        }
    }

    private String objectProgramPath(Object object) {
        String id = objectId(object);
        if (id == null || id.isBlank()) {
            return null;
        }
        int cloneMarker = id.indexOf('#');
        if (cloneMarker >= 0) {
            id = id.substring(0, cloneMarker);
        }
        while (id.startsWith("/")) {
            id = id.substring(1);
        }
        if (id.isBlank()) {
            return null;
        }
        if (!id.endsWith(".c")) {
            id = id + ".c";
        }
        return "/" + id;
    }

    public boolean isDestroyedObject(Object object) {
        return destroyedObjects.contains(objectLifecycle.identityOf(object));
    }

    public Object cloneObject(String sourcePath) {
        return objectFactory.apply(sourcePath);
    }

    /**
     * Returns mudlib-facing interactive personas that are in normal command mode.
     *
     * <p>Login controllers waiting for their own {@code input_to()} callbacks are still bound to
     * the socket for input delivery, but legacy {@code users()} output should not count them as
     * logged-in players. Gameplay objects that are temporarily waiting on another object's input
     * callback, such as a pager command, remain visible.</p>
     */
    public List<Object> users() {
        return instanceSessions.users();
    }

    /** Returns true when the supplied LPC object is currently bound to an active JVMud session. */
    public boolean isInteractive(Object user) {
        return instanceSessions.isInteractive(objectLifecycle.identityOf(user));
    }

    public void bindPlayerSession(String sessionId, String remoteAddress, Consumer<String> sessionOutputSink) {
        instanceSessions.open(sessionId, remoteAddress, sessionOutputSink);
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
        instanceSessions.bind(sessionId, remoteAddress, sessionOutputSink, attachmentFor(persona, mudlibProjection));
    }

    private PersonaAttachment attachmentFor(Object behavior, MudlibProjection projection) {
        behavior = objectLifecycle.identityOf(behavior);
        if (!isLiveBehaviorObject(behavior)) {
            throw new IllegalArgumentException("Persona attachment requires a live authored object.");
        }
        Object identity = behavior;
        var entity = worldProjection.projectedEntity(identity).orElseGet(() -> worldProjection.realizeEntity(identity));
        MudlibProjection canonicalProjection = projection == null ? null : new MudlibProjection(
                projection.sourcePath(), objectLifecycle.identityOf(projection.object()), projection.roles());
        PersonaRecord record = new PersonaRecord(instanceSessions.personaId(behavior), Optional.of(entity),
                Optional.empty(), Optional.of(canonicalProjection != null ? canonicalProjection : behavior));
        return new PersonaAttachment(record, objectReference(behavior), behavior);
    }

    /** Distinguishes opaque authored/host objects from ordinary LPC values before world realization. */
    private boolean isLiveBehaviorObject(Object behavior) {
        return behavior != null && !isDestroyedObject(behavior)
                && !(behavior instanceof Number) && !(behavior instanceof Boolean)
                && !(behavior instanceof CharSequence) && !(behavior instanceof List<?>)
                && !(behavior instanceof Map<?, ?>) && !(behavior instanceof RuntimeCallable)
                && !behavior.getClass().isArray();
    }

    /** Rebinds authored behavior, retaining the connection and protocols while clearing its old input capture. */
    public boolean rebindSessionLpcObject(Object newObject, Object oldObject) {
        newObject = objectLifecycle.identityOf(newObject);
        oldObject = objectLifecycle.identityOf(oldObject);
        if (!isLiveBehaviorObject(newObject) || !isLiveBehaviorObject(oldObject)) return false;
        Optional<InstanceSession> session = instanceSessions.sessionFor(oldObject);
        if (session.isEmpty()) return false;
        PersonaAttachment attachment = attachmentFor(newObject, MudlibProjection.personaBehavior(
                Objects.requireNonNullElse(objectId(newObject), objectReference(newObject)), newObject));
        Optional<ManagedPersonaProfile> profile =
                session.orElseThrow().attachment().flatMap(PersonaAttachment::profile);
        if (profile.isPresent()) attachment = attachment.withProfile(profile.orElseThrow());
        instanceSessions.attach(session.orElseThrow().sessionId(), attachment);
        return true;
    }

    /** Installs the host transport sink used for out-of-band messages to an interactive Persona. */
    public void bindSessionProtocolSink(Object persona, BiConsumer<String, String> protocolOutputSink) {
        instanceSessions.bindProtocolSink(objectLifecycle.identityOf(persona), protocolOutputSink);
    }

    /** Records whether a negotiated client protocol is active for an interactive Persona. */
    public void setSessionProtocolEnabled(Object persona, String protocol, boolean enabled) {
        instanceSessions.setProtocolEnabled(objectLifecycle.identityOf(persona), protocol, enabled);
    }

    /** Returns whether a client protocol has been negotiated for an interactive Persona. */
    public boolean isSessionProtocolEnabled(Object persona, String protocol) {
        return instanceSessions.isProtocolEnabled(objectLifecycle.identityOf(persona), protocol);
    }

    /** Sends one already encoded out-of-band message through a negotiated client protocol. */
    public boolean sendSessionProtocolMessage(Object persona, String protocol, String message) {
        return instanceSessions.sendProtocolMessage(objectLifecycle.identityOf(persona), protocol, message);
    }

    /** Runs disconnect/save policy before shutdown; a failed save prevents a clean-shutdown record. */
    public void disconnectPlayerSessions(String method) {
        RuntimeException failure = null;
        for (InstanceSession session : instanceSessions.openSessions()) {
            try {
                Optional<PersonaAttachment> attachment = session.attachment();
                if (method != null && attachment.isPresent()) {
                    Object persona = attachment.orElseThrow().behavior();
                    withCommandActor(persona, () -> invokeOptionalObject(persona, method));
                }
                instanceSessions.close(session.sessionId());
            } catch (RuntimeException e) {
                if (failure == null) failure = new IllegalStateException("Player shutdown/save failed");
                failure.addSuppressed(e);
            }
        }
        if (failure != null) throw failure;
    }

    public void unbindSession(String sessionId) {
        instanceSessions.close(sessionId);
    }

    public Optional<SessionRecord> sessionRecord(String sessionId) {
        return instanceSessions.sessionRecord(sessionId);
    }

    /** Returns the LPC object currently attached to a host session, when attachment has completed. */
    public Optional<Object> lpcObjectForSession(String sessionId) {
        return instanceSessions.objectForSession(sessionId);
    }

    public Optional<PlayerRecord> playerRecordForSession(String sessionId) {
        return instanceSessions.playerRecordForSession(sessionId);
    }

    public Optional<PersonaRecord> personaRecordForProjection(Object persona) {
        return instanceSessions.personaRecordForProjection(objectLifecycle.identityOf(persona));
    }

    /** Captures the next input line for the current authored handler and active connection. */
    public void captureSessionInput(String methodName, boolean noEcho, Object... extraArgs) {
        Object persona = outputTarget();
        Object handler = currentObject();
        if (persona != null && handler != null) {
            instanceSessions.captureInput(objectLifecycle.identityOf(persona), objectLifecycle.identityOf(handler), methodName, noEcho, extraArgs);
        }
    }

    public boolean hasCapturedSessionInput(Object persona) {
        return instanceSessions.pendingInput(objectLifecycle.identityOf(persona)).isPresent();
    }

    public boolean capturedSessionInputNoEcho(Object persona) {
        return instanceSessions.pendingInput(objectLifecycle.identityOf(persona)).map(InstanceSessions.CapturedInput::noEcho).orElse(false);
    }

    /** Delivers a captured callback without clearing a successor installed during its execution. */
    public Object deliverCapturedSessionInput(Object persona, String line) {
        Objects.requireNonNull(persona, "persona");
        Objects.requireNonNull(line, "line");
        Object identity = objectLifecycle.identityOf(persona);
        touchPersona(identity);
        Optional<InstanceSession> session = instanceSessions.sessionFor(identity);
        Optional<InstanceSessions.CapturedInput> pending = instanceSessions.pendingInput(identity);
        if (session.isEmpty() || pending.isEmpty()) return 0;
        InstanceSessions.CapturedInput input = pending.orElseThrow();
        Object[] extraArgs = input.extraArgs();
        Object[] invocationArgs = new Object[extraArgs.length + 1];
        invocationArgs[0] = line;
        System.arraycopy(extraArgs, 0, invocationArgs, 1, extraArgs.length);
        try {
            return withCommandActor(identity, () -> invokeObject(input.handler(), input.methodName(), invocationArgs));
        } finally {
            instanceSessions.completeInput(session.orElseThrow(), input);
        }
    }

    public int queryIdle(Object persona) {
        return instanceSessions.idleSeconds(objectLifecycle.identityOf(persona));
    }

    public Object queryIpNumber(Object persona) {
        return instanceSessions.remoteAddress(objectLifecycle.identityOf(persona)).<Object>map(address -> address).orElse(0);
    }

    public Object readMudlibText(String path) {
        return mudlibTextReader.apply(path);
    }

    /**
     * Parses an ordinary JSON file rooted inside the mudlib.
     *
     * @param path mudlib-relative JSON or gzip-compressed JSON path
     * @return LPC-compatible JSON value, or LPC false when unavailable
     */
    public Object readMudlibJson(String path) {
        return mudlibJsonReader.apply(path);
    }

    /**
     * Reads a bounded slice from a JSON array inside a mudlib-rooted file.
     *
     * @param path mudlib-relative JSON file path
     * @param pointer RFC 6901 JSON Pointer selecting an array
     * @param offset zero-based first array entry to return
     * @param count maximum number of entries to return
     * @return LPC-compatible array slice, or LPC false when the reader cannot access the file
     */
    public Object readMudlibJsonArray(String path, String pointer, int offset, int count) {
        return mudlibJsonArrayReader.read(path, pointer, offset, count);
    }

    /** Lists mudlib-rooted paths using an LP-style flag word. */
    public Object listMudlibPaths(String path, int flags) {
        return mudlibPathLister.apply(path, flags);
    }

    public int appendMudlibText(String path, Object text) {
        return mudlibTextAppender.apply(path, text);
    }

    public int removeMudlibText(String path) {
        return mudlibTextRemover.apply(path);
    }

    public int copyMudlibText(String source, String destination) {
        return mudlibTextCopier.apply(source, destination);
    }

    public int renameMudlibText(String source, String destination) {
        return mudlibTextRenamer.apply(source, destination);
    }

    public int createMudlibDirectory(String path) {
        return mudlibDirectoryCreator.apply(path);
    }

    public int removeMudlibDirectory(String path) {
        return mudlibDirectoryRemover.apply(path);
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
        instanceSessions.touch(objectLifecycle.identityOf(persona));
    }

    private String normalizeMudlibPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = path.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
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
        return invokeOptionalObject(target, methodName, args, false);
    }

    /**
     * Invokes a configured lifecycle hook on its most-derived declaring class.
     * Lifecycle arguments are padded or trimmed as with optional calls, but an
     * inherited exact-arity method must not bypass a descendant's hook.
     */
    public Object invokeLifecycleObject(Object target, String methodName, Object... args) {
        return invokeOptionalObject(target, methodName, args, true);
    }

    private Object invokeOptionalObject(Object target, String methodName, Object[] args, boolean lifecycle) {
        Object resolvedTarget = resolveInvocationTarget(target);
        if (resolvedTarget == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = lifecycle
                    ? findLifecycleInvocation(resolvedTarget.getClass(), methodName, actualArgs)
                    : findOptionalInvocation(resolvedTarget.getClass(), methodName, actualArgs);
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

    /**
     * Reports whether a generated LPC object exposes a public method with the requested arity.
     *
     * <p>Runtime callable values use this to decide whether a quoted function reference names a
     * method on its lexical object or should fall through to a global efun alias.</p>
     *
     * @param target generated LPC object to inspect
     * @param methodName LPC method name
     * @param arity number of arguments expected by the caller
     * @return {@code true} when the object can receive that method call
     */
    public boolean hasObjectMethod(Object target, String methodName, int arity) {
        Object resolvedTarget = resolveInvocationTarget(target);
        return resolvedTarget != null && hasMethod(resolvedTarget.getClass(), methodName, arity);
    }

    private Object invokeObjectPreservingCurrentObject(Object target, String methodName, Object... args) {
        target = resolveInvocationTarget(target);
        if (target == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findOptionalInvocation(target.getClass(), methodName, actualArgs);
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

    /**
     * Moves one LPC object and immediately starts the destination interaction scope for a connected
     * command actor.
     *
     * <p>Movement is itself a lifecycle boundary. A room and the nearby objects must observe a
     * connected actor's arrival before that actor can issue another command; deferring their
     * lifecycle methods until the next command can let the actor leave before entrance events and
     * temporary interactions are installed.</p>
     */
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

        Object previousLocation = worldProjection.environment(object);
        if (previousLocation == destination) {
            return;
        }

        worldProjection.move(object, destination);
        if (previousLocation != null
                && (instanceSessions.isInteractive(object) || commandEnabledEntities.contains(object))) {
            invokeDepartureLifecycle(object, previousLocation);
        }
        // Departure cleanup may destroy or redirect the actor. Do not deliver a stale arrival.
        if (destination != null && !destroyedObjects.contains(object)
                && worldProjection.environment(object) == destination) {
            invokeArrivalLifecycle(object, destination);
        }
    }

    private void invokeDepartureLifecycle(Object actor, Object previousLocation) {
        String methodName = configuration.lifecycleMethod(MudlibLifecycleEvent.ENTITY_DEPARTED_FROM_PLACE)
                .orElse(null);
        if (methodName == null) return;
        boolean acceptsActor = hasMethod(objectReceiver(previousLocation).getClass(), methodName, 1);
        if (!acceptsActor && !hasMethod(objectReceiver(previousLocation).getClass(), methodName, 0)) return;

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            withCommandActor(actor, () -> acceptsActor
                    ? invokeObject(previousLocation, methodName, actor)
                    : invokeObject(previousLocation, methodName));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    private void invokeArrivalLifecycle(Object object, Object destination) {
        if (instanceSessions.isInteractive(destination)) {
            invokeInteractionLifecycle(destination, object);
        }

        for (Object nearby : List.copyOf(inventoryFor(destination))) {
            if (nearby != object && instanceSessions.isInteractive(nearby)) {
                invokeInteractionLifecycle(nearby, object);
            }
        }

        if (instanceSessions.isInteractive(object)) {
            clearCommandActions(object);
            clearPendingActionMethods();
            try {
                invokeInteractionLifecycle(object, object);
                invokeInteractionLifecycle(object, destination);
                for (Object nearby : List.copyOf(inventoryFor(destination))) {
                    if (nearby != object) {
                        invokeInteractionLifecycle(object, nearby);
                    }
                }
                for (Object carried : List.copyOf(inventoryFor(object))) {
                    invokeInteractionLifecycle(object, carried);
                }
            } finally {
                clearPendingActionMethods();
            }
        }
    }

    private void invokeInteractionLifecycle(Object actor, Object handler) {
        String methodName = configuration.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElse(null);
        if (methodName == null) {
            return;
        }

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            withCommandActor(actor, () -> withScopedCommandRegistration(() -> {
                invokeLifecycleObject(handler, methodName);
                return null;
            }));
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    public Object environment(Object object) {
        Object target = (object != null) ? object : currentObject();
        return (target != null) ? worldProjection.environment(target) : null;
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
        Object previousOwner = entityAliases
                .computeIfAbsent(normalizedNamespace, ignored -> new LinkedHashMap<>())
                .put(normalizedAlias, object);
        // A replaced object must no longer own this alias, including during later cleanup.
        if (previousOwner != null && previousOwner != object) {
            Map<String, String> previousAliases = aliasesByEntity.get(previousOwner);
            if (previousAliases != null) {
                previousAliases.remove(normalizedNamespace);
                if (previousAliases.isEmpty()) {
                    aliasesByEntity.remove(previousOwner);
                }
            }
        }
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

    public void disableEntityCommands(Object object) {
        if (object != null) {
            commandEnabledEntities.remove(object);
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
        object = objectLifecycle.identityOf(object);
        if (object == null) {
            return;
        }
        Object preparation = objectDestructionRequestedHandler.apply(object);
        if (!isLpcFalse(preparation)) {
            return;
        }

        instanceSessions.discardObject(object);
        cancelRecurringTick(object);
        cancelDeferredCallbacks(object);
        List<Object> contents = new ArrayList<>(inventoryFor(object));
        for (Object child : contents) {
            moveObject(child, null);
        }

        moveObject(object, null);
        worldProjection.remove(object);
        lightLevels.remove(object);
        opaqueEntities.remove(object);
        removeEntityAliases(object);
        commandEnabledEntities.remove(object);
        commandActions.remove(object);
        removeCommandHandler(object);
        String id = objectIds.remove(object);
        if (id != null) {
            objects.remove(id);
        }
        destroyedObjects.add(object);
        objectLifecycle.remove(object);
    }

    private boolean isLpcFalse(Object value) {
        return value == null || Integer.valueOf(0).equals(value);
    }

    /**
     * Schedules a one-shot callback on the current object without replacing other callbacks that
     * happen to target the same method.
     *
     * <p>Method names are not task identities: mudlibs commonly fan out an event by scheduling the
     * same delivery method once per subscriber. Each registration therefore remains independently
     * cancellable and deliverable. The active command actor is also captured per registration so
     * delayed work observes the same actor that initiated it.</p>
     */
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

        Map<String, List<DeferredCallback>> tasks =
                deferredCallbackTasks.computeIfAbsent(target, ignored -> new LinkedHashMap<>());
        long callbackId = nextDeferredCallbackId++;
        long dueTick = scheduler.currentTick() + delaySeconds;
        Object commandActor = currentCommandActor();
        Object[] invocationArgs = args == null ? new Object[0] : args.clone();
        ScheduledTask task = scheduler.scheduleAfter(
                delaySeconds,
                () -> deliverDeferredCallback(
                        target, methodName, callbackId, commandActor, invocationArgs));
        tasks.computeIfAbsent(methodName, ignored -> new ArrayList<>())
                .add(new DeferredCallback(callbackId, dueTick, commandActor, task));
    }

    /** Cancels the earliest pending callback for the named method on the current object. */
    public int cancelDeferredCallback(String methodName) {
        if (methodName == null) {
            return -1;
        }

        Object target = currentObject();
        if (target == null) {
            return -1;
        }

        Map<String, List<DeferredCallback>> tasks = deferredCallbackTasks.get(target);
        if (tasks == null) {
            return -1;
        }

        List<DeferredCallback> callbacks = tasks.get(methodName);
        if (callbacks == null || callbacks.isEmpty()) {
            return -1;
        }

        DeferredCallback callback = callbacks.stream()
                .min(Comparator.comparingLong(DeferredCallback::dueTick)
                        .thenComparingLong(DeferredCallback::id))
                .orElseThrow();
        callbacks.remove(callback);
        callback.task().cancel();
        if (callbacks.isEmpty()) {
            tasks.remove(methodName);
        }
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
        Map<String, List<DeferredCallback>> tasks = deferredCallbackTasks.remove(target);
        if (tasks == null) {
            return;
        }

        for (List<DeferredCallback> callbacks : tasks.values()) {
            for (DeferredCallback callback : callbacks) {
                callback.task().cancel();
            }
        }
    }

    private void deliverRecurringTick(Object target) {
        if (!recurringTickTasks.containsKey(target)) {
            return;
        }

        String methodName = configuration.temporalTickMethod().orElse(null);
        if (methodName == null || !hasMethod(objectReceiver(target).getClass(), methodName, 0)) {
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

    private void deliverDeferredCallback(
            Object target,
            String methodName,
            long callbackId,
            Object commandActor,
            Object[] args) {
        Map<String, List<DeferredCallback>> tasks = deferredCallbackTasks.get(target);
        if (tasks != null) {
            List<DeferredCallback> callbacks = tasks.get(methodName);
            if (callbacks != null) {
                callbacks.removeIf(callback -> callback.id() == callbackId);
                if (callbacks.isEmpty()) {
                    tasks.remove(methodName);
                }
            }
            if (tasks.isEmpty()) {
                deferredCallbackTasks.remove(target);
            }
        }

        if (!objectIds.containsKey(target)
                || !hasMethodAcceptingMissingArguments(objectReceiver(target).getClass(), methodName, args.length)) {
            return;
        }

        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            if (commandActor == null) {
                invokeObject(target, methodName, args);
            } else {
                withCommandActor(commandActor, () -> invokeObject(target, methodName, args));
            }
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

    private record DeferredCallback(long id, long dueTick, Object commandActor, ScheduledTask task) {}

    @Override
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

    /**
     * Installs host-level command aliases used before LPC action lookup.
     *
     * <p>Compatibility mudlibs may configure a command-modification hook with mappings such as
     * {@code ([ "n": "north" ])}. JVMud keeps that compatibility at the runtime command boundary:
     * the first word is rewritten before registered {@code add_action} verbs are selected, and
     * {@code query_verb()} sees the expanded verb.</p>
     */
    public void configureCommandAliases(Object aliases) {
        commandAliases.clear();
        if (!(aliases instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String alias = String.valueOf(entry.getKey()).trim();
            String command = String.valueOf(entry.getValue()).trim();
            if (!alias.isEmpty() && !command.isEmpty()) {
                commandAliases.put(alias, command);
            }
        }
    }

    /**
     * Records the command failure text to display when no command action accepts the line.
     *
     * <p>Legacy mudlibs use {@code notify_fail()} inside an action that returns false. JVMud stores
     * the latest message for the active dispatch and writes it only after all candidate actions have
     * declined the command.</p>
     */
    public int notifyCommandFailure(Object message) {
        Deque<String> failures = commandFailureStack.get();
        if (failures.isEmpty()) {
            return 0;
        }
        failures.pop();
        failures.push(String.valueOf(message));
        return 0;
    }

    public void pushCurrentObject(Object object) {
        Objects.requireNonNull(object, "object");
        currentObjectStack.get().push(objectLifecycle.identityOf(object));
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

    /**
     * Runs a captured inline closure on its original code while sharing the object's live fields.
     * Nested calls on the same identity use that receiver until the closure returns.
     */
    @Override
    public <T> T withObjectReceiver(Object receiver, Supplier<T> action) {
        Object identity = objectLifecycle.identityOf(receiver);
        if (isDestroyedObject(identity)) {
            return null;
        }
        return objectLifecycle.withReceiver(receiver, () -> withCurrentObject(identity, action));
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
        commandActorStack.get().push(objectLifecycle.identityOf(actor));
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

    /**
     * Registers a pending LPC command action for the active command actor.
     *
     * <p>Agent-owned actions may be declared while an LPC object is initializing, before any
     * command is being dispatched. In that persistent case, the action is bound to the current LPC
     * object itself so it remains available after initialization. Scoped interaction registrations,
     * such as location-specific {@code init()} actions, still require the command actor supplied by
     * the refresh pass.</p>
     */
    public void registerVerb(String verb, boolean prefixMatch) {
        Objects.requireNonNull(verb, "verb");
        Deque<PendingAction> pendingActions = pendingActionStack.get();
        if (pendingActions.isEmpty()) {
            return;
        }

        PendingAction pendingAction = pendingActions.pop();
        Object actor = currentCommandActor();
        Object handler = currentObject();
        if (actor == null && pendingAction.persistent()) {
            actor = handler;
        }
        if (actor == null || handler == null) {
            return;
        }

        commandActions
                .computeIfAbsent(actor, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(verb, ignored -> new ArrayList<>())
                .add(new CommandAction(verb, handler, pendingAction.methodName(), prefixMatch,
                        pendingAction.persistent(), ++commandActionSequence));
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

    /**
     * Removes command actions owned by a handler from one actor's current command table.
     *
     * <p>This backs compatibility {@code remove_action()} calls used by temporary selectors and
     * conversations. The mudlib names the actor whose command table should be edited; JVMud uses
     * the current LPC object as the owning action handler.</p>
     */
    public int removeCommandActions(Object actor, Object handler) {
        if (actor == null || handler == null) {
            return 0;
        }
        Map<String, List<CommandAction>> actionsByVerb = commandActions.get(actor);
        if (actionsByVerb == null) {
            return 0;
        }
        int removed = 0;
        for (List<CommandAction> actions : actionsByVerb.values()) {
            int before = actions.size();
            actions.removeIf(action -> action.handler() == handler);
            removed += before - actions.size();
        }
        actionsByVerb.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        if (actionsByVerb.isEmpty()) {
            commandActions.remove(actor);
        }
        return removed;
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

    /** Dispatches a snapshot of matching actions. The boundary may select newest-first order
     * and parsed arguments for catch-alls; default exact-first and whole-line behavior is retained.
     * Nested commands restore the enclosing verb and failure context on return.
     */
    public Object dispatchCommand(Object actor, String commandLine) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(commandLine, "commandLine");
        touchPersona(actor);
        String trimmed = commandLine.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }

        trimmed = applyCommandAlias(trimmed);

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
        commandFailureStack.get().push("");
        try {
            for (CommandAction action : actions) {
                Object environmentBefore = environment(actor);
                Object result = invokeCommandAction(action, trimmed, argument);
                if (Truth.isTruthy(result)) {
                    return result;
                }
                if (environment(actor) != environmentBefore) {
                    return 1;
                }
            }
            if (tryEnvironmentMovementAction(actor, verb, argument)) {
                return 1;
            }
            String failureMessage = commandFailureStack.get().peek();
            if (failureMessage != null && !failureMessage.isEmpty()) {
                writeToLpcObject(actor, failureMessage);
            }
        } finally {
            commandFailureStack.get().pop();
            commandVerbStack.get().pop();
        }
        return 0;
    }

    private boolean tryEnvironmentMovementAction(Object actor, String verb, String argument) {
        Object environment = environment(actor);
        if (environment == null || !hasMethod(objectReceiver(environment).getClass(), "move", 1)) {
            return false;
        }
        Object exits = invokeOptionalObject(environment, "exits");
        if (!(exits instanceof List<?> exitList) || !exitList.contains(verb)) {
            return false;
        }
        Object environmentBefore = environment(actor);
        Object result = invokeObject(environment, "move", argument);
        return Truth.isTruthy(result) || environment(actor) != environmentBefore;
    }

    private String applyCommandAlias(String commandLine) {
        int space = commandLine.indexOf(' ');
        String verb = space == -1 ? commandLine : commandLine.substring(0, space);
        String replacement = commandAliases.get(verb);
        if (replacement == null) {
            return commandLine;
        }
        return space == -1 ? replacement : replacement + commandLine.substring(space);
    }

    private List<CommandAction> commandActionsFor(Object actor, String verb) {
        Map<String, List<CommandAction>> actionsByVerb = commandActions.getOrDefault(actor, Map.of());
        List<CommandAction> actions = new ArrayList<>(actionsByVerb.getOrDefault(verb, List.of()));
        for (Map.Entry<String, List<CommandAction>> entry : actionsByVerb.entrySet()) {
            if (entry.getKey().equals(verb) || !verb.startsWith(entry.getKey())) {
                continue;
            }
            List<CommandAction> prefixActions = entry.getValue();
            for (int i = prefixActions.size() - 1; i >= 0; i--) {
                CommandAction action = prefixActions.get(i);
                if (action.prefixMatch()) {
                    actions.add(action);
                }
            }
        }
        if (configuration.commandActionsNewestFirst())
            actions.sort(java.util.Comparator.comparingLong(CommandAction::sequence).reversed());
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
        Object current = worldProjection.environment(object);
        while (current != null) {
            root = current;
            current = worldProjection.environment(current);
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
        return worldProjection.inventory(object);
    }

    private boolean wouldCreateContainmentCycle(Object object, Object destination) {
        Object current = destination;
        while (current != null) {
            if (current == object) {
                return true;
            }
            current = worldProjection.environment(current);
        }
        return false;
    }

    private Object invokeCommandAction(CommandAction action, String commandLine, String argument) {
        String actionArgument = action.verb().isEmpty() && !configuration.commandActionsArgumentsOnly()
                ? commandLine : argument;
        boolean hasOneArgument = hasMethodAcceptingMissingArguments(
                objectReceiver(action.handler()).getClass(), action.methodName(), 1);
        boolean hasNoArguments = hasMethod(objectReceiver(action.handler()).getClass(), action.methodName(), 0);
        if (actionArgument != null && hasOneArgument) {
            return invokeObject(action.handler(), action.methodName(), actionArgument);
        }
        if (actionArgument == null && hasOneArgument && !hasNoArguments) {
            return invokeObject(action.handler(), action.methodName(), actionArgument);
        }
        if (hasNoArguments) {
            return invokeObject(action.handler(), action.methodName());
        }
        return 0;
    }

    /**
     * Reports whether an LPC method can receive the supplied leading arguments after omitted
     * trailing parameters are padded with their LPC zero values.
     */
    private boolean hasMethodAcceptingMissingArguments(Class<?> targetClass, String methodName, int arity) {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() >= arity) {
                return true;
            }
        }
        return false;
    }

    private Method findMethod(Class<?> targetClass, String methodName, int arity) throws NoSuchMethodException {
        Method best = null;
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                if (best == null || declaredCloserToTarget(method, best, targetClass)) {
                    best = method;
                }
            }
        }
        if (best != null) {
            return best;
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
                if (best == null || method.getParameterCount() < best.getParameterCount()
                        || (method.getParameterCount() == best.getParameterCount()
                                && declaredCloserToTarget(method, best, targetClass))) {
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
                if (best == null || method.getParameterCount() > best.getParameterCount()
                        || (method.getParameterCount() == best.getParameterCount()
                                && declaredCloserToTarget(method, best, targetClass))) {
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

    private InvocationPlan findLifecycleInvocation(Class<?> targetClass, String methodName, Object[] args)
            throws NoSuchMethodException {
        Method method = java.util.Arrays.stream(targetClass.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .min(java.util.Comparator
                        .comparingInt((Method candidate) -> inheritanceDistance(targetClass, candidate.getDeclaringClass()))
                        .thenComparingInt(candidate -> candidate.getParameterCount() >= args.length ? 0 : 1)
                        .thenComparingInt(candidate -> Math.abs(candidate.getParameterCount() - args.length)))
                .orElseThrow(() -> new NoSuchMethodException(targetClass.getName() + "." + methodName));
        Object[] supplied = method.getParameterCount() >= args.length
                ? padMissingArguments(method, args)
                : java.util.Arrays.copyOf(args, method.getParameterCount());
        return new InvocationPlan(method, adaptArguments(method, supplied));
    }

    private boolean declaredCloserToTarget(Method candidate, Method current, Class<?> targetClass) {
        return inheritanceDistance(targetClass, candidate.getDeclaringClass())
                < inheritanceDistance(targetClass, current.getDeclaringClass());
    }

    private int inheritanceDistance(Class<?> targetClass, Class<?> declaringClass) {
        int distance = 0;
        Class<?> cursor = targetClass;
        while (cursor != null) {
            if (cursor.equals(declaringClass)) {
                return distance;
            }
            cursor = cursor.getSuperclass();
            distance++;
        }
        return Integer.MAX_VALUE;
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
        if (List.class.isAssignableFrom(parameterType)) {
            return RuntimeCoercions.toArrayValue(arg);
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
            target = loadOrGetObject(path);
        }
        if (isDestroyedObject(target)) {
            return null;
        }
        return objectReceiver(target);
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

    private String objectReference(Object object) {
        String id = objectId(object);
        if (id != null && !id.isBlank()) {
            return id;
        }
        return object.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(object));
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
        String id = identifier.toString();
        if (id.isBlank()) {
            return false;
        }

        try {
            Object receiver = objectReceiver(object);
            InvocationPlan invocation = findInvocation(receiver.getClass(), "id", new Object[] {id});
            return withCurrentObject(object, () -> {
                try {
                    return Truth.isTruthy(invocation.method().invoke(receiver, invocation.arguments()));
                } catch (ReflectiveOperationException e) {
                    return false;
                }
            });
        } catch (NoSuchMethodException e) {
            // Fall back to exact object id matching below.
        }

        return id.equals(objectId(object));
    }

    private record PendingAction(String methodName, boolean persistent) {}

    private record CommandAction(String verb, Object handler, String methodName, boolean prefixMatch,
            boolean persistent, long sequence) {}

}
