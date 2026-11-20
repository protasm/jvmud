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
import io.github.protasm.jvmud.engine.model.time.WorldScheduler;
import io.github.protasm.jvmud.engine.model.world.MudlibWorldProjection;
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
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeContextHolder;
import io.github.protasm.jvmud.language.runtime.Truth;
import io.github.protasm.jvmud.storage.jdbc.RuntimeDatabaseService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
 * <p>Object registration and configured storage integration live here. Connection and
 * Player/Persona attachment state belongs to {@link InstanceSessions}; this context adapts LPC calls
 * to that owner. Generated code sees only the language's {@link RuntimeContext} contract. Compilation uses a
 * separate {@link LPCCompilerContext}; this adapter shares callable signatures with it without
 * allowing compiler passes to access live engine state.</p>
 *
 * <p>{@code LPCInvocationStack} owns lexical object scopes and execution membership.
 * {@code LPCMethodInvoker} owns method selection, argument adaptation and diagnostics, with named
 * policies for required, optional, lifecycle and caller-preserving global calls.
 * {@code LPCCommands} owns command actors, registration, dispatch and failure scopes.
 * {@code LPCWorldInteractions} coordinates authored movement, nearby interaction and perception
 * over the native world projection, including presence queries and world metadata.
 * {@code LPCScheduledCallbacks} owns recurring and deferred registrations, cancellation, delivery
 * and timed-failure policy, using the host's runtime binding and error reporter.
 * {@link LPCObjectLifecycle} retains ownership of live identities and behavior receivers.</p>
 *
 * <p>A running instance confines mutations to its execution thread. The isolated defaults also make
 * this host usable by deterministic runtime tests before the instance starts that thread.</p>
 */
public final class MudlibRuntimeContext extends RuntimeContext implements LPCCompilationEnvironment {
    private final LPCObjectLifecycle objectLifecycle = new LPCObjectLifecycle();
    private final LPCInvocationStack invocationStack = new LPCInvocationStack(objectLifecycle);
    private final LPCMethodInvoker methodInvoker = new LPCMethodInvoker(
            invocationStack, this::resolveInvocationTarget, this::objectIdOrDescription);
    private final LPCCommands commands;
    private final LPCWorldInteractions worldInteractions;
    private final LPCScheduledCallbacks scheduledCallbacks;
    private final EfunRegistry efunRegistry;
    private final LPCCompilerContext compilerContext;
    private final Map<String, Object> objects = new LinkedHashMap<>();
    private final Map<Object, String> objectIds = new IdentityHashMap<>();
    private final Set<Object> destroyedObjects =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final InstanceSessions instanceSessions;
    private final RuntimeDatabaseService databaseService = new RuntimeDatabaseService();
    private final StringBuilder outputTranscript = new StringBuilder();
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

    public MudlibRuntimeContext(IncludeResolver includeResolver) {
        this(includeResolver, new EfunRegistry());
    }

    public MudlibRuntimeContext(IncludeResolver includeResolver, EfunRegistry efunRegistry) {
        this.efunRegistry = Objects.requireNonNull(efunRegistry, "efunRegistry");
        this.compilerContext = new LPCCompilerContext(includeResolver, efunRegistry);
        this.instanceSessions = new InstanceSessions(this::objectReference, outputTranscript::append);
        this.commands = new LPCCommands(objectLifecycle, invocationStack, methodInvoker, new LPCCommands.Host() {
            @Override
            public Object environmentOf(Object actor) {
                return MudlibRuntimeContext.this.environment(actor);
            }

            @Override
            public void touchActor(Object actor) {
                touchPersona(actor);
            }

            @Override
            public void writeToActor(Object actor, Object message) {
                writeToLpcObject(actor, message);
            }

            @Override
            public boolean newestActionsFirst() {
                return configuration.commandActionsNewestFirst();
            }

            @Override
            public boolean catchAllArgumentsOnly() {
                return configuration.commandActionsArgumentsOnly();
            }
        });
        this.worldInteractions = new LPCWorldInteractions(
                objectLifecycle, invocationStack, methodInvoker, commands, new LPCWorldInteractions.Host() {
            @Override
            public Object loadOrGetObject(String path) {
                return MudlibRuntimeContext.this.loadOrGetObject(path);
            }

            @Override
            public String lifecycleMethod(MudlibLifecycleEvent event) {
                return configuration.lifecycleMethod(event).orElse(null);
            }

            @Override
            public Object perceptionBoundaryObject() {
                return configuration.boundaryObjectPath().map(MudlibRuntimeContext.this::loadOrGetObject).orElse(null);
            }

            @Override
            public String objectId(Object object) {
                return MudlibRuntimeContext.this.objectId(object);
            }

            @Override
            public boolean isInteractive(Object object) {
                return instanceSessions.isInteractive(object);
            }

            @Override
            public boolean isDestroyedIdentity(Object object) {
                return destroyedObjects.contains(object);
            }

            @Override
            public String messageText(Object value) {
                return MudlibRuntimeContext.this.messageText(value);
            }

            @Override
            public void writePerceptionText(Object observer, Object value) {
                writeToProjection(observer, value, true);
            }

            @Override
            public void withRuntimeContext(Runnable action) {
                runWithRuntimeContext(action);
            }
        });
        this.scheduledCallbacks = new LPCScheduledCallbacks(
                objectLifecycle, invocationStack, methodInvoker, commands, new LPCScheduledCallbacks.Host() {
            @Override
            public String temporalTickMethod() {
                return configuration.temporalTickMethod().orElse(null);
            }

            @Override
            public boolean isRegisteredTarget(Object target) {
                return objectIds.containsKey(target);
            }

            @Override
            public void withRuntimeContext(Runnable action) {
                runWithRuntimeContext(action);
            }

            @Override
            public void reportError(Object target, String context, String operation, Throwable error) {
                timedRuntimeErrorHandler.onError(target, context, operation, error);
            }
        });
    }

    /** Binds host-entered lifecycle or timed work; callers that inherit a binding do not enter here. */
    private void runWithRuntimeContext(Runnable action) {
        RuntimeContext previous = RuntimeContextHolder.current();
        RuntimeContextHolder.setCurrent(this);
        try {
            action.run();
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    /** Delegates explicit nearby-object refresh while the runtime retains hook selection and binding. */
    void refreshCommandActions(Object actor, Consumer<Object> interactionHook) {
        worldInteractions.refreshCommandActions(actor, interactionHook);
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
        worldInteractions.setProjection(worldProjection);
    }

    /** Returns the native world projection currently used for LPC containment operations. */
    public MudlibWorldProjection worldProjection() {
        return worldInteractions.projection();
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

    /**
     * Installs the host's timed-failure reporter; null restores the standalone no-op reporter.
     * Reporting occurs after object and captured-actor scopes unwind, inside the runtime binding.
     */
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

    /**
     * Selects the clock for future registrations, or a fresh isolated clock when null.
     * Pending handles stay on their original scheduler; this does not migrate or reset registrations.
     */
    public void setScheduler(WorldScheduler scheduler) {
        scheduledCallbacks.setScheduler(scheduler);
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

    /** Emits text to the emitter's location and immediate neighbors, excluding the emitter. */
    public void emitPerceivable(Object emitter, Object value) {
        worldInteractions.emitPerceivable(emitter, value);
    }

    /** Emits text while excluding the emitter and one supplied object reference. */
    public void emitPerceivableExcept(Object emitter, Object value, Object excluded) {
        worldInteractions.emitPerceivableExcept(emitter, value, excluded);
    }

    /** Emits text to the location and its immediate contents without a source exclusion. */
    public void emitPerceivableAt(Object location, Object value) {
        worldInteractions.emitPerceivableAt(location, value);
    }

    /** Emits a structured event to the location and its immediate contents, excluding the source. */
    public void emitWorldEvent(Object source, String kind, Object content, String text) {
        worldInteractions.emitEvent(source, kind, content, text);
    }

    /** Directed world communication, distinct from private interface output. */
    public void deliverWorldEvent(Object target, String kind, Object content, String text) {
        worldInteractions.deliverEvent(target, kind, content, text);
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

    /** Message permission defaults to allowed when absent, while a failing hook reports its error. */
    private boolean receivesPlayerBoundMessages(Object target) {
        Object resolvedTarget = resolveInvocationTarget(target);
        if (resolvedTarget == null) {
            return true;
        }

        try {
            LPCMethodInvoker.InvocationPlan invocation = methodInvoker.findOptionalInvocation(
                    resolvedTarget.getClass(),
                    "receives_player_bound_messages",
                    new Object[0]);
            Object allowed = withCurrentObject(resolvedTarget, () -> {
                try {
                    return invocation.invoke(resolvedTarget);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on "
                                    + objectIdOrDescription(resolvedTarget)
                                    + LPCMethodInvoker.causeSummary(e),
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
                return methodInvoker.invokePreservingCaller(globalObject, name, args);
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

    /** Owns the stable identity and replaceable behavior of this instance's LPC objects. */
    LPCObjectLifecycle objectLifecycle() {
        return objectLifecycle;
    }

    /** Returns the current Java behavior receiver for an LPC identity or historical receiver. */
    public Object objectReceiver(Object object) {
        return objectLifecycle.receiverOf(object);
    }

    /** Reports lexical execution membership used to reject reload of an active object. */
    boolean isObjectExecuting(Object object) {
        return invocationStack.isExecuting(object);
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
        var entity = worldProjection().projectedEntity(identity).orElseGet(() -> worldProjection().realizeEntity(identity));
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

    /** Invokes a required authored method in the target's lexical object scope. */
    @Override
    public Object invokeObject(Object target, String methodName, Object... args) {
        return methodInvoker.invokeRequired(target, methodName, args);
    }

    /** Invokes an optional authored method, allowing argument trimming and returning zero when absent. */
    @Override
    public Object invokeOptionalObject(Object target, String methodName, Object... args) {
        return methodInvoker.invokeOptional(target, methodName, args);
    }

    /**
     * Invokes a configured lifecycle hook on its most-derived declaring class.
     * Lifecycle arguments are padded or trimmed as with optional calls, but an
     * inherited exact-arity method must not bypass a descendant's hook.
     */
    public Object invokeLifecycleObject(Object target, String methodName, Object... args) {
        return methodInvoker.invokeLifecycle(target, methodName, args);
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
        return resolvedTarget != null && methodInvoker.hasMethod(resolvedTarget.getClass(), methodName, arity);
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
        worldInteractions.move(object, destination);
    }

    /** Returns immediate containment, using the lexical object when no object is supplied. */
    public Object environment(Object object) {
        return worldInteractions.environment(object);
    }

    /** Searches immediate inventory; an omitted container also permits the lexical object's environment. */
    public Object present(Object identifier, Object container) {
        return worldInteractions.present(identifier, container);
    }

    /** Replaces an object's namespaced alias, retaining the existing normalization and ownership rules. */
    public void bindEntityAlias(Object object, Object namespace, Object alias) {
        worldInteractions.bindAlias(object, namespace, alias);
    }

    /** Looks up a normalized namespaced alias without loading or realizing an object. */
    public Object findEntityAlias(Object namespace, Object alias) {
        return worldInteractions.findAlias(namespace, alias);
    }

    /** Returns LPC status for an object's alias in the normalized namespace. */
    public int entityHasAlias(Object object, Object namespace) {
        return worldInteractions.entityHasAlias(object, namespace);
    }

    /** Marks an object as command-enabled for lifecycle and mudlib queries. */
    public void enableEntityCommands(Object object) {
        commands.enable(object);
    }

    /** Clears command enablement metadata without removing registered actions. */
    public void disableEntityCommands(Object object) {
        commands.disable(object);
    }

    /** Returns LPC status for enablement metadata; dispatch itself is not gated by this flag. */
    public int entityCommandsEnabled(Object object) {
        return commands.isEnabled(object) ? 1 : 0;
    }

    /** Selects whether the object's contents contribute to LPC ambient light. */
    public void setEntityTranslucent(Object object, boolean translucent) {
        worldInteractions.setTranslucent(object, translucent);
    }

    /** Reports LPC light transparency; null and unconfigured objects are translucent. */
    public boolean entityTranslucent(Object object) {
        return worldInteractions.isTranslucent(object);
    }

    /** Returns the first object in the native projection's immediate inventory, or null. */
    public Object firstInventory(Object container) {
        return worldInteractions.firstInventory(container);
    }

    /** Returns the next inventory entry in the containing location, or null. */
    public Object nextInventory(Object object) {
        return worldInteractions.nextInventory(object);
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
        scheduledCallbacks.discardObject(object);
        List<Object> contents = new ArrayList<>(worldInteractions.inventory(object));
        for (Object child : contents) {
            moveObject(child, null);
        }

        moveObject(object, null);
        worldInteractions.discardObject(object);
        commands.discardObject(object);
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
     *
     * @param methodName authored method name; null or blank names are ignored
     * @param delaySeconds non-negative delay in scheduler ticks, retaining the legacy parameter name
     * @param args callback values, retained through a shallow copy of the argument array
     */
    public void scheduleDeferredCallback(String methodName, int delaySeconds, Object... args) {
        scheduledCallbacks.scheduleDeferred(methodName, delaySeconds, args);
    }

    /**
     * Cancels the earliest matching registration by due tick, then registration order.
     * Returns zero on cancellation or minus one when absent, preserving the LPC compatibility result.
     */
    public int cancelDeferredCallback(String methodName) {
        return scheduledCallbacks.cancelDeferred(methodName);
    }

    /**
     * Replaces or disables the current object's recurring tick.
     *
     * @param enabled zero to disable; any nonzero value enables the recurring callback
     * @param intervalSeconds interval in scheduler ticks despite the legacy name; zero uses one tick
     */
    public void scheduleRecurringTick(int enabled, int intervalSeconds) {
        scheduledCallbacks.scheduleRecurring(enabled, intervalSeconds);
    }

    /** Host observer for timed invocation failures, called with the active generated-code runtime bound. */
    @FunctionalInterface
    public interface TimedRuntimeErrorHandler {
        /** Reports a scheduled_tick or deferred_callback failure; reporter exceptions propagate to the clock caller. */
        void onError(Object target, String context, String operation, Throwable error);
    }

    @Override
    public Object currentObject() {
        return invocationStack.currentObject();
    }

    /** Returns the enclosing lexical object, or the current object when it is the only frame. */
    public Object previousObject() {
        return invocationStack.previousObject();
    }

    /** Returns the active canonical command actor independently of the lexical LPC object. */
    public Object currentCommandActor() {
        return commands.currentActor();
    }

    /** Returns the expanded verb of the active dispatch, or {@code null} outside dispatch. */
    public String currentCommandVerb() {
        return commands.currentVerb();
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
        commands.configureAliases(aliases);
    }

    /**
     * Records the command failure text to display when no command action accepts the line.
     *
     * <p>Legacy mudlibs use {@code notify_fail()} inside an action that returns false. JVMud stores
     * the latest message for the active dispatch and writes it only after all candidate actions have
     * declined the command.</p>
     */
    public int notifyCommandFailure(Object message) {
        return commands.notifyFailure(message);
    }

    /** Enters a canonical lexical frame; prefer {@link #withCurrentObject(Object, Supplier)} for scoped calls. */
    public void pushCurrentObject(Object object) {
        invocationStack.push(object);
    }

    /** Leaves a manually entered lexical frame, rejecting an unbalanced pop. */
    public void popCurrentObject() {
        invocationStack.pop();
    }

    /** Runs within an object's lexical frame and restores the caller on return or failure. */
    public <T> T withCurrentObject(Object object, Supplier<T> action) {
        return invocationStack.withCurrentObject(object, action);
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

    /** Runs within a canonical actor scope, restoring its caller on normal return or failure. */
    public <T> T withCommandActor(Object actor, Supplier<T> action) {
        return commands.withActor(actor, action);
    }

    /** Remembers a pending action method with its current registration lifetime. */
    public void rememberActionMethod(String methodName) {
        commands.rememberActionMethod(methodName);
    }

    /** Binds the latest pending method to an exact verb for the current actor and handler. */
    public void registerVerb(String verb) {
        commands.registerVerb(verb);
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
        commands.registerVerb(verb, prefixMatch);
    }

    /** Clears temporary interaction actions for the actor, retaining persistent declarations. */
    public void clearCommandActions(Object actor) {
        commands.clearTemporaryActions(actor);
    }

    /**
     * Removes command actions owned by a handler from one actor's current command table.
     *
     * <p>This backs compatibility {@code remove_action()} calls used by temporary selectors and
     * conversations. The mudlib names the actor whose command table should be edited; JVMud uses
     * the current LPC object as the owning action handler.</p>
     */
    public int removeCommandActions(Object actor, Object handler) {
        return commands.removeActionsForHandler(actor, handler);
    }

    /** Clears all pending unbound action methods on the current thread. */
    public void clearPendingActionMethods() {
        commands.clearPendingActionMethods();
    }

    /** Marks newly remembered action methods as temporary for an interaction lifecycle scope. */
    public <T> T withScopedCommandRegistration(Supplier<T> action) {
        return commands.withScopedRegistration(action);
    }

    /** Dispatches a snapshot of matching actions within the caller's explicit actor scope.
     * The boundary may select newest-first order
     * and parsed arguments for catch-alls; default exact-first and whole-line behavior is retained.
     * Nested commands restore the enclosing verb and failure context on return.
     */
    public Object dispatchCommand(Object actor, String commandLine) {
        return commands.dispatch(actor, commandLine);
    }

    /** Updates the lexical object's light and returns visible light from its outermost container. */
    public int setLight(int delta) {
        return worldInteractions.setLight(delta);
    }

    /** Resolves paths and tombstones through host policy, then selects the lifecycle's live receiver. */
    private Object resolveInvocationTarget(Object target) {
        if (target instanceof String path) {
            target = loadOrGetObject(path);
        }
        if (isDestroyedObject(target)) {
            return null;
        }
        return objectReceiver(target);
    }

    private String objectIdOrDescription(Object object) {
        String id = objectId(object);
        return id != null ? id : String.valueOf(object);
    }

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

}
