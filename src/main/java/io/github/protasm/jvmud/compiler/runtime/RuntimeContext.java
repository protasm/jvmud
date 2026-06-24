package io.github.protasm.jvmud.compiler.runtime;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunRegistry;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.parser.ast.ASTInherit;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolution;
import io.github.protasm.jvmud.compiler.preproc.IncludeResolver;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.compiler.scanner.Scanner;
import io.github.protasm.jvmud.compiler.token.TokenList;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final Map<String, String> commandAliases = new LinkedHashMap<>();
    private final Map<Object, ScheduledTask> recurringTickTasks = new IdentityHashMap<>();
    private final Map<Object, Map<String, ScheduledTask>> deferredCallbackTasks = new IdentityHashMap<>();
    private final Map<PlayerId, PlayerRecord> players = new LinkedHashMap<>();
    private final Map<SessionId, SessionBinding> sessions = new LinkedHashMap<>();
    private final Map<PersonaId, PersonaRecord> personas = new LinkedHashMap<>();
    private final Map<Object, SessionBinding> sessionsByPersona = new IdentityHashMap<>();
    private final Map<Object, PersonaId> personaIdsByProjection = new IdentityHashMap<>();
    private final Map<Object, PendingSessionInput> pendingInputsByPersona = new IdentityHashMap<>();
    private final Map<Object, StringBuilder> pendingTargetedOutputByPersona = new IdentityHashMap<>();
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
    private BiFunction<String, Integer, Object> mudlibPathLister = (path, flags) -> List.of();
    private BiFunction<String, Object, Integer> mudlibTextAppender = (path, text) -> 0;
    private Function<String, Integer> mudlibTextRemover = path -> 0;
    private BiFunction<String, String, Integer> mudlibTextCopier = (source, destination) -> -1;
    private BiFunction<String, String, Integer> mudlibTextRenamer = (source, destination) -> -1;
    private Function<String, Integer> mudlibDirectoryCreator = path -> 0;
    private Function<String, Integer> mudlibDirectoryRemover = path -> 0;
    private Function<String, Boolean> mudlibObjectSourceExists = path -> true;
    private BiFunction<Object, String, Integer> playerTransferHandler = (actor, gameId) -> 0;
    private BiFunction<String, Object, Integer> lpcObjectStateSaver = (path, object) -> 0;
    private BiFunction<String, Object, Integer> lpcObjectStateRestorer = (path, object) -> 0;
    private TimedRuntimeErrorHandler timedRuntimeErrorHandler = (target, context, operation, error) -> {};
    private Function<Object, Object> objectDestructionRequestedHandler = target -> 0;
    private MudlibBoundary mudlibBoundary = MudlibBoundary.empty();
    private WorldScheduler scheduler = new WorldScheduler();
    private String mudlibGlobalObjectPath;
    private String compatibilityGlobalObjectPath;
    private final Map<String, Optional<ASTObject>> globalObjectDeclarations = new LinkedHashMap<>();
    private final Map<String, GlobalObjectSource> inMemoryGlobalObjectSources = new LinkedHashMap<>();

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

    /** Sets the host resolver used to check whether a mudlib object source path exists. */
    public void setMudlibObjectSourceExists(Function<String, Boolean> mudlibObjectSourceExists) {
        this.mudlibObjectSourceExists = (mudlibObjectSourceExists != null) ? mudlibObjectSourceExists : path -> true;
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

    /**
     * Registers source text for an in-memory LPC object that may later serve as a global helper.
     *
     * <p>Host APIs such as {@code LPCRuntime.loadSource(...)} can compile an LPC object without
     * writing a corresponding {@code .c} file. Declaration-backed global lookup still needs source
     * text to recover function signatures, so those hosts register the source here under the
     * object's normalized mudlib path.</p>
     */
    public void registerInMemoryObjectSource(String objectPath, String source, String displayPath) {
        String normalizedPath = normalizeMudlibPath(objectPath);
        if (normalizedPath == null || source == null) {
            return;
        }
        inMemoryGlobalObjectSources.put(
                normalizedPath,
                new GlobalObjectSource(source, null, displayPath != null ? displayPath : "/" + normalizedPath + ".c"));
        globalObjectDeclarations.remove(normalizedPath);
    }

    /**
     * Sets the JVMud compatibility global object path used by older callers.
     *
     * @deprecated use {@link #setMudlibBoundary(MudlibBoundary)} with
     *     {@link MudlibBoundary#compatibilityGlobalObjectPath()} metadata.
     */
    @Deprecated(forRemoval = false)
    public void setMfunObjectPath(String mfunObjectPath) {
        this.compatibilityGlobalObjectPath = normalizeMudlibPath(mfunObjectPath);
        globalObjectDeclarations.clear();
    }

    /** Sets active mudlib boundary metadata for generated-code helpers and compatibility lookup. */
    public void setMudlibBoundary(MudlibBoundary mudlibBoundary) {
        this.mudlibBoundary = mudlibBoundary != null ? mudlibBoundary : MudlibBoundary.empty();
        this.mudlibGlobalObjectPath = this.mudlibBoundary.mudlibGlobalObjectPath().orElse(null);
        this.compatibilityGlobalObjectPath = this.mudlibBoundary.compatibilityGlobalObjectPath().orElse(null);
        globalObjectDeclarations.clear();
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
        SessionBinding binding = target != null ? sessionsByPersona.get(target) : null;
        if (binding != null && receivesPlayerBoundMessages(target)) {
            writeToSession(binding, value);
        } else if (target != null && receivesPlayerBoundMessages(target)) {
            pendingTargetedOutputByPersona
                    .computeIfAbsent(target, ignored -> new StringBuilder())
                    .append(formattedMessageText(value));
        }
    }

    /** Writes engine control-plane or transport text to one bound Session. */
    public boolean writeToSession(SessionId sessionId, Object value) {
        Objects.requireNonNull(sessionId, "sessionId");
        SessionBinding binding = sessions.get(sessionId);
        if (binding == null) {
            return false;
        }
        writeToSession(binding, value);
        return true;
    }

    /** Writes engine control-plane text to all active Sessions for one Player. */
    public boolean writeToPlayer(PlayerId playerId, Object value) {
        Objects.requireNonNull(playerId, "playerId");
        PlayerRecord player = players.get(playerId);
        if (player == null) {
            return false;
        }
        boolean delivered = false;
        for (SessionId sessionId : player.activeSessionIds()) {
            delivered |= writeToSession(sessionId, value);
        }
        return delivered;
    }

    /** Writes engine gameplay text to the bound Session for one Persona. */
    public boolean writeToPersona(PersonaId personaId, Object value) {
        Objects.requireNonNull(personaId, "personaId");
        PersonaRecord persona = personas.get(personaId);
        if (persona == null || persona.mudlibBehaviorProjection().isEmpty()) {
            return false;
        }
        SessionBinding binding = sessionsByPersona.get(projectionObject(persona.mudlibBehaviorProjection().orElseThrow()));
        if (binding == null) {
            return false;
        }
        writeToSession(binding, value);
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
                writeToProjection(target, value, true);
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

    private void writeToProjection(Object target, Object value, boolean playerBound) {
        SessionBinding binding = target != null ? sessionsByPersona.get(target) : null;
        if (binding != null) {
            if (!playerBound || receivesPlayerBoundMessages(target)) {
                writeToSession(binding, value);
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

    private void writeToSession(SessionBinding binding, Object value) {
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
        Efun global = resolveGlobalFunction(name, arity);
        return global != null ? global : lookupEngineEfun(name, arity);
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

    /**
     * Invokes a mudlib-visible function through the active compatibility boundary.
     *
     * <p>Mudlib globals normally shadow engine efuns. When the mudlib profile declares a direct
     * efun alias and the call carries a first-class callable, the alias is allowed to reach the
     * engine before a string-typed mudlib wrapper can coerce the callable into display text. This
     * models LDMud-style simulated efun wrappers that forward closure replacements to driver efuns.
     * </p>
     */
    public Object invokeEfun(String name, int arity, Object[] args) {
        Efun callableAlias = resolveCallableDirectAlias(name, arity, args);
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

    private Efun resolveCallableDirectAlias(String name, int arity, Object[] args) {
        String engineName = directEfunName(name);
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
        Efun mudlibGlobal = resolveGlobalFunction(mudlibGlobalObjectPath, name, arity);
        if (mudlibGlobal != null) {
            return mudlibGlobal;
        }
        return resolveGlobalFunction(compatibilityGlobalObjectPath, name, arity);
    }

    private Efun resolveGlobalFunction(String objectPath, String name, int arity) {
        if (objectPath == null) {
            return null;
        }
        ASTMethod method = declaredGlobalMethod(objectPath, name, arity);
        if (method == null) {
            return null;
        }
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return globalMethodSignature(method, arity);
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                Object globalObject = loadGlobalObject(objectPath);
                return invokeObjectPreservingCurrentObject(globalObject, name, args);
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

    private ASTMethod declaredGlobalMethod(String objectPath, String name, int arity) {
        return declaredGlobalMethod(objectPath, name, arity, new HashSet<>());
    }

    private ASTMethod declaredGlobalMethod(String objectPath, String name, int arity, Set<String> visitedPaths) {
        String normalizedPath = normalizeMudlibPath(objectPath);
        if (normalizedPath == null || !visitedPaths.add(normalizedPath)) {
            return null;
        }
        Optional<ASTObject> declaration = globalObjectDeclarations.computeIfAbsent(
                normalizedPath,
                this::parseGlobalObjectDeclaration);
        if (declaration.isEmpty()) {
            return null;
        }
        ASTObject object = declaration.orElseThrow();
        ASTMethod directMethod = object.methods().getAll(name).stream()
                .filter(method -> acceptsArity(method, arity))
                .min(Comparator.comparingInt(this::parameterCount))
                .orElse(null);
        if (directMethod != null) {
            return directMethod;
        }
        for (ASTInherit inherit : object.inherits()) {
            ASTMethod inheritedMethod = declaredGlobalMethod(inherit.path(), name, arity, visitedPaths);
            if (inheritedMethod != null) {
                return inheritedMethod;
            }
        }
        return null;
    }

    private boolean acceptsArity(ASTMethod method, int arity) {
        int parameterCount = parameterCount(method);
        return parameterCount >= arity || (method.modifiers().isVarargs() && arity >= parameterCount);
    }

    private Optional<ASTObject> parseGlobalObjectDeclaration(String objectPath) {
        try {
            GlobalObjectSource source = globalObjectSource(objectPath);
            if (source == null) {
                return Optional.empty();
            }
            Scanner scanner = new Scanner(newPreprocessor());
            TokenList tokens = scanner.scan(source.sourcePath(), source.source(), source.displayPath());
            Parser parser = new Parser(this, ParserOptions.defaults());
            return Optional.of(parser.parse(objectPath, tokens));
        } catch (IOException e) {
            return Optional.empty();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Could not parse global function object: " + objectPath, e);
        }
    }

    private GlobalObjectSource globalObjectSource(String objectPath) throws IOException {
        GlobalObjectSource inMemory = inMemoryGlobalObjectSources.get(objectPath);
        if (inMemory != null) {
            return inMemory;
        }

        if (objectPath.equals(compatibilityGlobalObjectPath)
                && mudlibBoundary.compatibilityGlobalObjectSourcePath().isPresent()) {
            Path sourcePath = mudlibBoundary.compatibilityGlobalObjectSourcePath().orElseThrow();
            if (Files.isRegularFile(sourcePath)) {
                return new GlobalObjectSource(
                        Files.readString(sourcePath),
                        sourcePath,
                        "/" + objectPath + ".c");
            }
        }

        String includePath = "/" + objectPath + ".c";
        IncludeResolution resolution = includeResolver.resolve(null, includePath, false);
        return new GlobalObjectSource(resolution.source(), resolution.resolvedPath(), resolution.displayPath());
    }

    private EfunSignature globalMethodSignature(ASTMethod method, int arity) {
        List<LPCType> parameterTypes = new ArrayList<>();
        if (method.parameters() != null && method.parameters().size() > 0) {
            for (int i = 0; i < arity; i++) {
                ASTParameter parameter = method.parameters().get(Math.min(i, method.parameters().size() - 1));
                parameterTypes.add(lpcTypeOrMixed(parameter.symbol()));
            }
        }
        Symbol returnSymbol = method.symbol();
        return new EfunSignature(
                new Symbol(lpcTypeOrMixed(returnSymbol), returnSymbol.name()),
                parameterTypes);
    }

    private int parameterCount(ASTMethod method) {
        return method.parameters() != null ? method.parameters().size() : 0;
    }

    private LPCType lpcTypeOrMixed(Symbol symbol) {
        return symbol != null && symbol.lpcType() != null ? symbol.lpcType() : LPCType.LPCMIXED;
    }

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

    private record GlobalObjectSource(String source, Path sourcePath, String displayPath) {}

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
            Method method = object.getClass().getMethod("$jvmud$inherited_programs");
            Object value = method.invoke(object);
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
        return destroyedObjects.contains(object);
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
        return sessions.values().stream()
                .map(SessionBinding::personaProjection)
                .filter(Objects::nonNull)
                .filter(persona -> !isSelfCapturedInputPersona(persona))
                .toList();
    }

    private boolean isSelfCapturedInputPersona(Object persona) {
        PendingSessionInput pendingInput = pendingInputsByPersona.get(persona);
        return pendingInput != null && pendingInput.handler() == persona;
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
        Optional<Object> mudlibProfileProjection = existing != null
                ? existing.playerRecord().mudlibProfileProjection()
                : Optional.ofNullable(mudlibProjection);
        Instant connectedAt = existing != null ? existing.sessionRecord().connectedAt() : null;
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
        flushPendingTargetedOutput(persona, binding);
    }

    /**
     * Rebinds an existing host session from one LPC object projection to another.
     *
     * <p>This models legacy driver handoff operations such as RealmsMUD's use of {@code exec}:
     * the network session, output sink, Player record, and connection metadata stay in place while
     * the mudlib-facing interactive object changes from a login object to a player object.</p>
     */
    public boolean rebindSessionLpcObject(Object newObject, Object oldObject) {
        if (newObject == null || oldObject == null) {
            return false;
        }
        SessionBinding binding = sessionsByPersona.get(oldObject);
        if (binding == null) {
            return false;
        }
        bindSession(
                binding.sessionRecord().id().value(),
                newObject,
                binding.sessionRecord().remoteAddress().orElse(null),
                binding.outputSink(),
                MudlibProjection.personaBehavior(
                        Objects.requireNonNullElse(objectId(newObject), objectReference(newObject)),
                        newObject));
        return true;
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

    /** Returns the LPC object projection currently attached to a host session, when one exists. */
    public Optional<Object> lpcObjectForSession(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        SessionBinding binding = sessions.get(new SessionId(sessionId));
        return binding != null ? Optional.ofNullable(binding.personaProjection()) : Optional.empty();
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

    /**
     * Delivers one line to a pending {@code input_to()} callback while preserving that pending state
     * for runtime queries made during the callback.
     */
    public Object deliverCapturedSessionInput(Object persona, String line) {
        Objects.requireNonNull(persona, "persona");
        Objects.requireNonNull(line, "line");
        touchPersona(persona);
        PendingSessionInput pendingInput = pendingInputsByPersona.get(persona);
        if (pendingInput == null) {
            return 0;
        }
        Object[] extraArgs = pendingInput.extraArgs();
        Object[] invocationArgs = new Object[extraArgs.length + 1];
        invocationArgs[0] = line;
        System.arraycopy(extraArgs, 0, invocationArgs, 1, extraArgs.length);
        try {
            return withCommandActor(persona, () ->
                    invokeObject(pendingInput.handler(), pendingInput.methodName(), invocationArgs));
        } finally {
            pendingInputsByPersona.computeIfPresent(persona, (ignored, current) ->
                    current == pendingInput ? null : current);
        }
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
        pendingTargetedOutputByPersona.remove(object);
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

    /**
     * Installs driver-level command aliases used before LPC action lookup.
     *
     * <p>LDMud mudlibs commonly configure {@code H_MODIFY_COMMAND} with mappings such as
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
                        pendingAction.persistent()));
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
        if (environment == null || !hasMethod(environment.getClass(), "move", 1)) {
            return false;
        }
        Object exits = invokeOptionalObject(environment, "exits");
        if (!(exits instanceof List<?> exitList) || !exitList.contains(verb)) {
            return false;
        }
        Object environmentBefore = environment(actor);
        Optional<String> destination = movementDestination(environment, verb);
        Object result = invokeObject(environment, "move", argument);
        if (Truth.isTruthy(result) && environment(actor) == environmentBefore
                && destination.isPresent() && !mudlibObjectSourceExists.apply(destination.orElseThrow())) {
            throw new IllegalStateException("Movement destination does not exist: " + destination.orElseThrow());
        }
        return Truth.isTruthy(result) || environment(actor) != environmentBefore;
    }

    private Optional<String> movementDestination(Object environment, String verb) {
        if (!hasMethod(environment.getClass(), "getExitDirections", 0)) {
            return Optional.empty();
        }
        Object exits = invokeOptionalObject(environment, "getExitDirections");
        if (!(exits instanceof Map<?, ?> exitMap)) {
            return Optional.empty();
        }
        Object state = hasMethod(environment.getClass(), "currentState", 0)
                ? invokeOptionalObject(environment, "currentState")
                : "default";
        Optional<String> destination = movementDestination(exitMap, state, verb);
        return destination.isPresent() ? destination : movementDestination(exitMap, "default", verb);
    }

    private Optional<String> movementDestination(Map<?, ?> exits, Object state, String verb) {
        Object stateExits = exits.get(state);
        if (!(stateExits instanceof Map<?, ?> directions)) {
            return Optional.empty();
        }
        Object entry = directions.get(verb);
        if (entry instanceof String destination) {
            return Optional.of(destination);
        }
        if (entry instanceof Map<?, ?> mappedExit && !Truth.isTruthy(mappedExit.get("region"))
                && mappedExit.get("destination") instanceof String destination) {
            return Optional.of(destination);
        }
        return Optional.empty();
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

    private Object invokeCommandAction(CommandAction action, String commandLine, String argument) {
        String actionArgument = action.verb().isEmpty() ? commandLine : argument;
        boolean hasOneArgument = hasMethod(action.handler().getClass(), action.methodName(), 1);
        boolean hasNoArguments = hasMethod(action.handler().getClass(), action.methodName(), 0);
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
        String id = identifier.toString();
        if (id.isBlank()) {
            return false;
        }

        try {
            InvocationPlan invocation = findInvocation(object.getClass(), "id", new Object[] {id});
            return Truth.isTruthy(invocation.method().invoke(object, invocation.arguments()));
        } catch (NoSuchMethodException e) {
            // Fall back to exact object id matching below.
        } catch (ReflectiveOperationException e) {
            return false;
        }

        return id.equals(objectId(object));
    }

    private record PendingAction(String methodName, boolean persistent) {}

    private record CommandAction(String verb, Object handler, String methodName, boolean prefixMatch,
            boolean persistent) {}

    private record PendingSessionInput(Object handler, String methodName, boolean noEcho, Object[] extraArgs) {
        private PendingSessionInput {
            extraArgs = extraArgs == null ? new Object[0] : extraArgs.clone();
        }

        @Override
        public Object[] extraArgs() {
            return extraArgs.clone();
        }
    }

    private void flushPendingTargetedOutput(Object persona, SessionBinding binding) {
        StringBuilder pendingOutput = pendingTargetedOutputByPersona.remove(persona);
        if (pendingOutput != null && !pendingOutput.isEmpty()) {
            writeToSession(binding, pendingOutput.toString());
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
