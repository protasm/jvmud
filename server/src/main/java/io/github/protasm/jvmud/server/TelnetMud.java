package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.Location;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.MudlibProjection;
import io.github.protasm.jvmud.runtime.OutgoingTextFormatter;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Shared runtime state for a persistent Telnet mud process. */
final class TelnetMud implements TelnetHost {
    private static final String CONNECTED_BANNER = "JVMud telnet. Type /help for commands or /quit to disconnect.\n";
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final int PASSWORD_ITERATIONS = 210_000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_HASH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final LPCRuntime runtime;
    private final WorldRuntime worldRuntime;
    private final String gameId;
    private final Path mudlibRoot;
    private final String startingPlacePath;
    private final Object startingPlaceObject;
    private final String playerObjectPath;
    private final String playerPrompt;
    private final int maxLineLength;
    private final boolean showRuler;
    private final String playerSessionConnectedMethod;
    private final String playerSessionDisconnectedMethod;
    private final String runtimeErrorMethod;
    private final MudlibBootResult bootResult;
    private final Map<Object, String> requestedTransfers = new IdentityHashMap<>();
    private TransferHandler transferHandler = (mud, actor, gameId) -> 0;
    private int nextPersonaId = 1;

    private TelnetMud(
            LPCRuntime runtime,
            WorldRuntime worldRuntime,
            String gameId,
            Path mudlibRoot,
            String startingPlacePath,
            Object startingPlaceObject,
            String playerObjectPath,
            String playerPrompt,
            int maxLineLength,
            boolean showRuler,
            String playerSessionConnectedMethod,
            String playerSessionDisconnectedMethod,
            String runtimeErrorMethod,
            MudlibBootResult bootResult) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
        this.gameId = Objects.requireNonNull(gameId, "gameId");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.startingPlacePath = Objects.requireNonNull(startingPlacePath, "startingPlacePath");
        this.startingPlaceObject = Objects.requireNonNull(startingPlaceObject, "startingPlaceObject");
        this.playerObjectPath = playerObjectPath;
        this.playerPrompt = playerPrompt;
        this.maxLineLength = maxLineLength;
        this.showRuler = showRuler;
        this.playerSessionConnectedMethod = playerSessionConnectedMethod;
        this.playerSessionDisconnectedMethod = playerSessionDisconnectedMethod;
        this.runtimeErrorMethod = runtimeErrorMethod;
        this.bootResult = Objects.requireNonNull(bootResult, "bootResult");
    }

    static TelnetMud boot(Path mudlibRoot, String configObjectPath) {
        Path normalizedRoot = mudlibRoot.toAbsolutePath().normalize();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(normalizedRoot)
                .build());
        EngineEfuns.registerCore(runtime);

        MudlibBootResult result =
                new MudlibBoot(runtime, normalizedRoot, configObjectPath, false).boot();
        if (result.startingRoom() == null) {
            throw new IllegalStateException("Mudlib boot did not provide a starting place.");
        }

        Object startingPlaceObject = runtime.loadOrGetObject(result.startingRoom());
        MudlibBoundary boundary = result.mudlibBoundary();
        String gameId = boundary.gameId().orElse(normalizedRoot.getFileName().toString());
        runtime.clearOutputTranscript();
        TelnetMud mud = new TelnetMud(
                runtime,
                result.worldRuntime(),
                gameId,
                normalizedRoot,
                result.startingRoom(),
                startingPlaceObject,
                boundary.playerObjectPath().orElse(null),
                boundary.playerPrompt().orElse(null),
                boundary.maxLineLength(),
                boundary.showRuler(),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_DISCONNECTED).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR).orElse(null),
                result);
        runtime.setPlayerTransferHandler((actor, targetGameId) ->
                mud.transferHandler.requestTransfer(mud, actor, targetGameId));
        return mud;
    }

    String gameId() {
        return gameId;
    }

    void setTransferHandler(TransferHandler transferHandler) {
        this.transferHandler = transferHandler != null ? transferHandler : (mud, actor, gameId) -> 0;
    }

    void requestTransfer(Object actor, String gameId) {
        requestedTransfers.put(actor, gameId);
    }

    String consumeRequestedTransfer(TelnetPersona persona) {
        return persona != null ? requestedTransfers.remove(persona.actor()) : null;
    }

    @Override
    public Path mudlibRoot() {
        return mudlibRoot;
    }

    @Override
    public MudlibBootResult bootResult() {
        return bootResult;
    }

    @Override
    public Duration worldTickInterval() {
        return runtime.mudlibBoundary().temporalTickInterval();
    }

    @Override
    public synchronized void advanceWorldTick() {
        worldRuntime.scheduler().advanceBy(1);
        runtime.clearOutputTranscript();
    }

    @Override
    public synchronized void shutdown(Object reason) {
        String methodName = bootResult.mudlibBoundary().lifecycleMethod(MudlibLifecycleEvent.SERVER_SHUTDOWN).orElse(null);
        if (methodName == null) {
            return;
        }
        Object handler = errorHandlerObject(null);
        if (handler == null) {
            return;
        }
        try {
            runtime.invokeOptionalObject(handler, methodName, reason);
        } catch (RuntimeException | LinkageError e) {
            System.err.println("Ignoring mudlib shutdown lifecycle failure: " + e.getMessage());
        } finally {
            runtime.clearOutputTranscript();
        }
    }

    String startingPlacePath() {
        return startingPlacePath;
    }

    @Override
    public synchronized TelnetPersona attachPersona(PrintWriter out, String remoteAddress) {
        return attachPersona("telnet/" + nextPersonaId++, out, remoteAddress, true);
    }

    synchronized TelnetPersona attachPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        if (usesLpmuseumPlayerLogin()) {
            return attachLpmuseumLoginSession(sessionId, out, remoteAddress, announceConnection);
        }

        int id = nextPersonaId++;
        TelnetPersona persona = attachMudlibPlayer(id, sessionId, out, remoteAddress, announceConnection, null, "");
        if (persona != null) {
            return persona;
        }
        return attachHostPersona(id, sessionId, out, remoteAddress, announceConnection);
    }

    synchronized TelnetPersona attachVisitingPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            String userId,
            String gender) {
        int id = nextPersonaId++;
        TelnetPersona persona = attachMudlibPlayer(id, sessionId, out, remoteAddress, false, userId, gender);
        if (persona != null) {
            return persona;
        }
        return attachHostPersona(id, sessionId, out, remoteAddress, userId, false);
    }

    synchronized void suspendPersonaForTransfer(TelnetPersona persona) {
        if (persona != null && isAttached(persona)) {
            runtime.unbindSession(persona.sessionId());
        }
    }

    synchronized TelnetPersona resumePersona(TelnetPersona suspended, PrintWriter out, String remoteAddress) {
        MudlibProjection projection = new LegacyPlayerObjectAdapter(playerObjectPath)
                .combinedProjection(suspended.actor());
        runtime.bindSession(suspended.sessionId(), suspended.actor(), remoteAddress, text -> {
            out.print(text);
            out.flush();
        }, projection);
        runtime.clearOutputTranscript();
        runtime.invokeOptionalObject(suspended.actor(), "return_from_exhibit");
        runtime.clearOutputTranscript();
        return new TelnetPersona(
                this,
                suspended.sessionId(),
                suspended.objectId(),
                suspended.name(),
                suspended.userId(),
                suspended.gender(),
                suspended.actor(),
                remoteAddress);
    }

    private boolean usesLpmuseumPlayerLogin() {
        return "lpmuseum".equals(gameId) && "persona/visitor".equals(playerObjectPath);
    }

    private TelnetPersona attachLpmuseumLoginSession(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        runtime.bindPlayerSession(sessionId, remoteAddress, text -> {
            out.print(text);
            out.flush();
        });
        runtime.clearOutputTranscript();
        if (announceConnection) {
            messagePlayerForSession(sessionId, CONNECTED_BANNER);
        }
        LpmuseumLoginSession login = new LpmuseumLoginSession(this, sessionId, remoteAddress);
        TelnetPersona persona = new TelnetPersona(
                this,
                sessionId,
                "player/" + sessionId,
                "login player",
                login,
                remoteAddress);
        login.start();
        return persona;
    }

    private TelnetPersona attachAuthenticatedLpmuseumPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            LpmuseumAccount account) {
        int id = nextPersonaId++;
        Object actor = runtime.cloneObject(playerObjectPath);
        String objectId = Objects.requireNonNullElse(runtime.objectId(actor), playerObjectPath + "#" + id);
        Place startingPlace = placeFor(startingPlacePath);
        worldRuntime.createEntity(
                objectId,
                account.personaName(),
                startingPlace,
                Capability.ACTOR,
                Capability.PERCEPTIVE);
        runtime.moveObject(actor, startingPlaceObject);
        MudlibProjection projection = new LegacyPlayerObjectAdapter(playerObjectPath)
                .combinedProjection(actor);
        runtime.bindSession(sessionId, actor, remoteAddress, text -> {
            out.print(text);
            out.flush();
        }, projection);
        runtime.clearOutputTranscript();
        runtime.invokeObject(
                actor,
                "configure_account",
                account.accountId(),
                account.personaName(),
                account.gender(),
                account.email(),
                account.passwordHash());
        runtime.invokeObject(actor, "save_account");
        runtime.invokeObject(actor, "enter_museum");
        forceLookCommand(actor);
        runtime.clearOutputTranscript();
        return new TelnetPersona(this, sessionId, objectId, account.personaName(), account.accountId(), account.gender(), actor,
                remoteAddress);
    }

    private TelnetPersona attachMudlibPlayer(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection,
            String visitingUserId,
            String visitingGender) {
        if (playerObjectPath == null) {
            return null;
        }

        try {
            Object actor = runtime.cloneObject(playerObjectPath);
            String objectId = Objects.requireNonNullElse(runtime.objectId(actor), playerObjectPath + "#" + id);
            String name = "player " + id;
            Place startingPlace = placeFor(startingPlacePath);
            worldRuntime.createEntity(
                    objectId,
                    name,
                    startingPlace,
                    Capability.ACTOR,
                    Capability.PERCEPTIVE);
            runtime.moveObject(actor, startingPlaceObject);
            MudlibProjection projection = new LegacyPlayerObjectAdapter(playerObjectPath)
                    .combinedProjection(actor);
            runtime.bindSession(sessionId, actor, remoteAddress, text -> {
                out.print(text);
                out.flush();
            }, projection);
            runtime.clearOutputTranscript();
            if (announceConnection) {
                messagePlayerForSession(sessionId, CONNECTED_BANNER);
                messagePlayerForSession(sessionId, "Attached " + name + " as " + objectId
                        + " in " + startingPlacePath + ".\n");
            }
            if (visitingUserId != null) {
                runtime.withCommandActor(actor, () ->
                        runtime.invokeOptionalObject(actor, "jvmud_exhibit_logon", visitingUserId, visitingGender));
                runtime.clearOutputTranscript();
                name = visitingUserId;
            } else {
                invokePlayerSessionConnected(actor);
            }
            return new TelnetPersona(this, sessionId, objectId, name, visitingUserId != null ? visitingUserId : name,
                    visitingGender, actor, remoteAddress);
        } catch (RuntimeException | LinkageError e) {
            System.err.println("Could not attach mudlib player object " + playerObjectPath
                    + "; falling back to host persona: " + e.getMessage());
            runtime.clearOutputTranscript();
            return null;
        }
    }

    private TelnetPersona attachHostPersona(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        return attachHostPersona(id, sessionId, out, remoteAddress, "player " + id, announceConnection);
    }

    private TelnetPersona attachHostPersona(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            String name,
            boolean announceConnection) {
        String objectId = "persona/" + id;
        Place startingPlace = placeFor(startingPlacePath);
        Entity entity = worldRuntime.createEntity(
                objectId,
                name,
                startingPlace,
                Capability.ACTOR,
                Capability.PERCEPTIVE);
        LocalSessionActor actor = new LocalSessionActor(runtime, worldRuntime, entity, name);
        runtime.registerHostObject(objectId, actor);
        runtime.moveObject(actor, startingPlaceObject);
        runtime.bindSession(sessionId, actor, remoteAddress, text -> {
            out.print(text);
            out.flush();
        });
        runtime.clearOutputTranscript();
        if (announceConnection) {
            messagePlayerForSession(sessionId, CONNECTED_BANNER);
            messagePlayerForSession(sessionId, "Attached " + name + " in " + startingPlacePath + ".\n");
        }
        return new TelnetPersona(this, sessionId, objectId, name, actor, remoteAddress);
    }

    private void messagePlayerForSession(String sessionId, String text) {
        runtime.playerRecordForSession(sessionId)
                .ifPresent(player -> runtime.messagePlayer(player.id(), text));
    }

    private void invokePlayerSessionConnected(Object actor) {
        if (playerSessionConnectedMethod == null) {
            return;
        }
        runtime.invokeObject(actor, playerSessionConnectedMethod);
        runtime.clearOutputTranscript();
    }

    private void forceLookCommand(Object actor) {
        runtime.refreshCommandActions(actor);
        runtime.dispatchCommand(actor, "look");
        runtime.clearOutputTranscript();
    }

    @Override
    public synchronized void detachPersona(TelnetPersona persona) {
        detachPersona(persona, true);
    }

    synchronized void detachPersona(TelnetPersona persona, boolean invokeDisconnectLifecycle) {
        if (persona != null) {
            if (persona.actor() instanceof LpmuseumLoginSession) {
                runtime.unbindSession(persona.sessionId());
                return;
            }
            if (isAttached(persona)) {
                if (invokeDisconnectLifecycle) {
                    invokePlayerSessionDisconnected(persona.actor());
                }
                runtime.unbindSession(persona.sessionId());
            }
            removeWorldEntity(persona);
        }
    }

    private void invokePlayerSessionDisconnected(Object actor) {
        if (playerSessionDisconnectedMethod == null) {
            return;
        }
        try {
            runtime.invokeOptionalObject(actor, playerSessionDisconnectedMethod);
        } catch (RuntimeException e) {
            System.err.println("Ignoring player disconnect lifecycle failure: " + e.getMessage());
        } finally {
            runtime.clearOutputTranscript();
        }
    }

    @Override
    public synchronized Object dispatch(TelnetPersona persona, PrintWriter out, String commandLine) {
        try {
            return dispatchUnchecked(persona, out, commandLine);
        } catch (RuntimeException | LinkageError e) {
            return handleRuntimeError(persona, out, "command", commandLine, e);
        }
    }

    private Object dispatchUnchecked(TelnetPersona persona, PrintWriter out, String commandLine) {
        if (persona.actor() instanceof LpmuseumLoginSession login) {
            LpmuseumLoginSession.Result result = login.handle(commandLine, out);
            if (result.replacement().isPresent()) {
                persona.replaceWith(result.replacement().orElseThrow());
                return 1;
            }
            if (result.shouldDisconnect()) {
                runtime.unbindSession(persona.sessionId());
                return 1;
            }
            return 1;
        }

        if (runtime.hasCapturedSessionInput(persona.actor())) {
            runtime.clearOutputTranscript();
            Object result = runtime.deliverCapturedSessionInput(persona.actor(), commandLine);
            runtime.clearOutputTranscript();
            if (!isAttached(persona)) {
                removeWorldEntity(persona);
                return 1;
            }
            return result;
        }

        runtime.clearOutputTranscript();
        runtime.refreshCommandActions(persona.actor());
        Object result = runtime.dispatchCommand(persona.actor(), commandLine);
        if (Integer.valueOf(0).equals(result) && isLookCommand(commandLine)) {
            runtime.clearOutputTranscript();
            Object environment = runtime.environment(persona.actor());
            if (environment != null) {
                lookAt(environment, out);
                result = 1;
            }
            printRuntimeOutput(out);
        } else {
            runtime.clearOutputTranscript();
        }
        return result;
    }

    private Object handleRuntimeError(
            TelnetPersona persona,
            PrintWriter out,
            String context,
            String operation,
            Throwable error) {
        runtime.clearOutputTranscript();
        if (runtimeErrorMethod != null && invokeRuntimeErrorHandler(persona, context, operation, error)) {
            return 1;
        }
        out.println("Something goes wrong.");
        System.err.println("Unhandled mudlib runtime error during " + context + " '" + operation + "': "
                + error.getMessage());
        return 1;
    }

    private boolean invokeRuntimeErrorHandler(
            TelnetPersona persona,
            String context,
            String operation,
            Throwable error) {
        try {
            Object handler = errorHandlerObject(persona);
            if (handler == null) {
                return false;
            }
            runtime.invokeOptionalObject(
                    handler,
                    runtimeErrorMethod,
                    persona.actor(),
                    context,
                    operation,
                    error.getMessage());
            runtime.clearOutputTranscript();
            return true;
        } catch (RuntimeException | LinkageError handlerFailure) {
            System.err.println("Mudlib error handler failed: " + handlerFailure.getMessage());
            runtime.clearOutputTranscript();
            return false;
        }
    }

    private Object errorHandlerObject(TelnetPersona persona) {
        String boundaryObjectPath = bootResult.mudlibBoundary().boundaryObjectPath().orElse(null);
        if (boundaryObjectPath != null) {
            return runtime.loadOrGetObject(boundaryObjectPath);
        }
        return persona != null ? persona.actor() : null;
    }

    @Override
    public synchronized void printPromptIfReady(TelnetPersona persona, PrintWriter out) {
        if (persona.actor() instanceof LpmuseumLoginSession) {
            return;
        }
        if (playerPrompt == null || !isAttached(persona) || runtime.hasCapturedSessionInput(persona.actor())) {
            return;
        }
        if (showRuler) {
            out.println(OutgoingTextFormatter.ruler(maxLineLength));
        }
        out.print(playerPrompt);
        out.flush();
    }

    @Override
    public synchronized boolean isCapturingInput(TelnetPersona persona) {
        if (persona != null && persona.actor() instanceof LpmuseumLoginSession) {
            return true;
        }
        return isAttached(persona) && runtime.hasCapturedSessionInput(persona.actor());
    }

    @Override
    public synchronized boolean isCapturingNoEchoInput(TelnetPersona persona) {
        if (persona != null && persona.actor() instanceof LpmuseumLoginSession login) {
            return login.noEcho();
        }
        return isAttached(persona) && runtime.capturedSessionInputNoEcho(persona.actor());
    }

    @Override
    public synchronized boolean isAttached(TelnetPersona persona) {
        return persona != null && runtime.sessionRecord(persona.sessionId()).isPresent();
    }

    private void removeWorldEntity(TelnetPersona persona) {
        if (persona != null) {
            worldRuntime.removeEntity(persona.objectId());
        }
    }

    private void lookAt(Object object, PrintWriter out) {
        try {
            printReturnedDescription(runtime.invokeObject(object, "long", new Object[] {null}), out);
        } catch (RuntimeException e) {
            try {
                printReturnedDescription(runtime.invokeObject(object, "long"), out);
            } catch (RuntimeException ignored) {
                printReturnedDescription(runtime.invokeObject(object, "short"), out);
            }
        }
    }

    private void printReturnedDescription(Object description, PrintWriter out) {
        if (description instanceof String text && !text.isEmpty()) {
            out.println(OutgoingTextFormatter.wrap(text, maxLineLength));
        }
    }

    private void printRuntimeOutput(PrintWriter out) {
        String output = runtime.outputTranscript();
        runtime.clearOutputTranscript();
        if (!output.isEmpty()) {
            out.print(output);
            if (!output.endsWith("\n")) {
                out.println();
            }
        }
    }

    private Place placeFor(String path) {
        Location existing = worldRuntime.findLocation(path);
        if (existing instanceof Place place) {
            return place;
        }
        if (existing != null) {
            throw new IllegalArgumentException("Starting location is not a place: " + path);
        }
        return worldRuntime.createPlace(path, path);
    }

    private boolean isLookCommand(String commandLine) {
        String trimmed = commandLine.trim();
        return "look".equals(trimmed) || "l".equals(trimmed) || trimmed.startsWith("look ");
    }

    private void messageLoginPlayer(String sessionId, String text) {
        messagePlayerForSession(sessionId, text);
    }

    private Optional<LpmuseumAccount> loadLpmuseumAccount(String accountId) {
        return LpmuseumAccountStore.load(mudlibRoot, accountId);
    }

    private void saveLpmuseumAccount(LpmuseumAccount account) {
        LpmuseumAccountStore.save(mudlibRoot, account);
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[PASSWORD_SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, PASSWORD_ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2-sha256$" + PASSWORD_ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$"
                + encoder.encodeToString(hash);
    }

    private boolean verifyPassword(String password, String encodedHash) {
        String[] parts = encodedHash.split("\\$");
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }

    @FunctionalInterface
    interface TransferHandler {
        int requestTransfer(TelnetMud sourceMud, Object actor, String gameId);
    }

    private static final class LpmuseumLoginSession {
        private final TelnetMud mud;
        private final String sessionId;
        private final String remoteAddress;
        private State state = State.ACCOUNT_ID;
        private String accountId = "";
        private String pendingPassword = "";
        private String email = "";
        private String personaName = "";
        private String passwordHash = "";
        private int passwordAttempts;

        private LpmuseumLoginSession(TelnetMud mud, String sessionId, String remoteAddress) {
            this.mud = mud;
            this.sessionId = sessionId;
            this.remoteAddress = remoteAddress;
        }

        private void start() {
            message("Please enter your user ID: ");
        }

        private boolean noEcho() {
            return state == State.LOGIN_PASSWORD
                    || state == State.NEW_PASSWORD
                    || state == State.CONFIRM_PASSWORD;
        }

        private Result handle(String line, PrintWriter out) {
            return switch (state) {
                case ACCOUNT_ID -> handleAccountId(line);
                case CREATE_CONFIRMATION -> handleCreateConfirmation(line);
                case LOGIN_PASSWORD -> handleLoginPassword(line, out);
                case NEW_PASSWORD -> handleNewPassword(line);
                case CONFIRM_PASSWORD -> handleConfirmPassword(line);
                case EMAIL -> handleEmail(line);
                case PERSONA_NAME -> handlePersonaName(line);
                case GENDER -> handleGender(line, out);
            };
        }

        private Result handleAccountId(String line) {
            String normalized = normalizeAccountId(line);
            if (!validAccountId(normalized)) {
                message("Use letters, numbers, underscore, or dash for your user ID.\n");
                message("Please enter your user ID: ");
                return Result.continueLogin();
            }

            accountId = normalized;
            Optional<LpmuseumAccount> account = mud.loadLpmuseumAccount(accountId);
            if (account.isPresent() && !account.orElseThrow().passwordHash().isEmpty()) {
                passwordAttempts = 0;
                passwordHash = account.orElseThrow().passwordHash();
                personaName = account.orElseThrow().personaName();
                email = account.orElseThrow().email();
                message("Password: ");
                state = State.LOGIN_PASSWORD;
                return Result.continueLogin();
            }

            message("No LPMuseum account exists for " + accountId + ". Create it? (yes/no) ");
            state = State.CREATE_CONFIRMATION;
            return Result.continueLogin();
        }

        private Result handleCreateConfirmation(String line) {
            String answer = line.toLowerCase();
            if ("yes".equals(answer) || "y".equals(answer)) {
                message("Password: ");
                state = State.NEW_PASSWORD;
                return Result.continueLogin();
            }
            if ("no".equals(answer) || "n".equals(answer)) {
                message("No account was created. Please visit LPMuseum again when you are ready.\n");
                return Result.disconnectSession();
            }
            message("Please answer yes or no: ");
            return Result.continueLogin();
        }

        private Result handleLoginPassword(String line, PrintWriter out) {
            Optional<LpmuseumAccount> account = mud.loadLpmuseumAccount(accountId);
            if (account.isPresent() && mud.verifyPassword(line, account.orElseThrow().passwordHash())) {
                return enter(out, account.orElseThrow());
            }

            passwordAttempts++;
            if (passwordAttempts < 3) {
                message("That password did not match. Please try again.\n");
                message("Password: ");
                return Result.continueLogin();
            }

            message("That password did not match. Please reconnect when you are ready to try again.\n");
            return Result.disconnectSession();
        }

        private Result handleNewPassword(String line) {
            String problem = passwordProblem(line);
            if (problem != null) {
                message(problem + "\n");
                message("Password: ");
                return Result.continueLogin();
            }

            pendingPassword = line;
            message("Password again: ");
            state = State.CONFIRM_PASSWORD;
            return Result.continueLogin();
        }

        private Result handleConfirmPassword(String line) {
            if (!line.equals(pendingPassword)) {
                pendingPassword = "";
                message("Those passwords did not match.\n");
                message("Password: ");
                state = State.NEW_PASSWORD;
                return Result.continueLogin();
            }

            passwordHash = mud.hashPassword(line);
            pendingPassword = "";
            message("Email address (optional): ");
            state = State.EMAIL;
            return Result.continueLogin();
        }

        private Result handleEmail(String line) {
            if (line.isEmpty()) {
                email = "";
            } else if (!validEmail(line)) {
                message("That email address does not look valid. Enter one address, or leave it blank.\n");
                message("Email address (optional): ");
                return Result.continueLogin();
            } else {
                email = line;
            }

            message("Persona name: ");
            state = State.PERSONA_NAME;
            return Result.continueLogin();
        }

        private Result handlePersonaName(String line) {
            if (!validPersonaName(line)) {
                message("Use 2-24 letters, numbers, spaces, apostrophes, or dashes for your Persona name.\n");
                message("Persona name: ");
                return Result.continueLogin();
            }

            personaName = capitalize(line.toLowerCase());
            message("Gender (female/male/neutral/none/other): ");
            state = State.GENDER;
            return Result.continueLogin();
        }

        private Result handleGender(String line, PrintWriter out) {
            String normalized = line.toLowerCase();
            if (!("female".equals(normalized) || "male".equals(normalized) || "neutral".equals(normalized)
                    || "none".equals(normalized) || "other".equals(normalized))) {
                message("Please choose female, male, neutral, none, or other: ");
                return Result.continueLogin();
            }

            LpmuseumAccount account = new LpmuseumAccount(accountId, personaName, normalized, email, passwordHash);
            mud.saveLpmuseumAccount(account);
            return enter(out, account);
        }

        private Result enter(PrintWriter out, LpmuseumAccount account) {
            TelnetPersona replacement = mud.attachAuthenticatedLpmuseumPersona(
                    sessionId,
                    out,
                    remoteAddress,
                    account);
            return Result.replaceWith(replacement);
        }

        private void message(String text) {
            mud.messageLoginPlayer(sessionId, text);
        }

        private static String normalizeAccountId(String value) {
            return value == null ? "" : value.toLowerCase();
        }

        private static boolean validAccountId(String value) {
            if (value.length() < 3 || value.length() > 24) {
                return false;
            }
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (!((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-')) {
                    return false;
                }
            }
            return true;
        }

        private static String passwordProblem(String value) {
            if (value.length() < 6) {
                return "Password must be at least 6 characters.";
            }
            if (value.length() > 72) {
                return "Password must be 72 characters or fewer.";
            }

            boolean upper = false;
            boolean lower = false;
            boolean number = false;
            boolean special = false;
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    upper = true;
                } else if (ch >= 'a' && ch <= 'z') {
                    lower = true;
                } else if (ch >= '0' && ch <= '9') {
                    number = true;
                } else if ("!@#$%^&*_.?+-".indexOf(ch) >= 0) {
                    special = true;
                } else {
                    return "Password may use letters, numbers, and ! @ # $ % ^ & * _ . ? + - only.";
                }
            }
            if (!upper) {
                return "Password must include an uppercase letter.";
            }
            if (!lower) {
                return "Password must include a lowercase letter.";
            }
            if (!number) {
                return "Password must include a number.";
            }
            if (!special) {
                return "Password must include a special character.";
            }
            return null;
        }

        private static boolean validEmail(String value) {
            int at = value.indexOf('@');
            int dot = value.lastIndexOf('.');
            if (at <= 0 || dot <= at + 1 || dot >= value.length() - 1 || value.indexOf('@', at + 1) >= 0) {
                return false;
            }
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (!((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
                        || (ch >= '0' && ch <= '9') || ch == '@' || ch == '.'
                        || ch == '_' || ch == '%' || ch == '+' || ch == '-')) {
                    return false;
                }
            }
            return true;
        }

        private static boolean validPersonaName(String value) {
            if (value.length() < 2 || value.length() > 24) {
                return false;
            }
            boolean sawLetterOrNumber = false;
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                    sawLetterOrNumber = true;
                } else if (ch != ' ' && ch != '\'' && ch != '-') {
                    return false;
                }
            }
            return sawLetterOrNumber;
        }

        private static String capitalize(String value) {
            return value.isEmpty() ? value : value.substring(0, 1).toUpperCase() + value.substring(1);
        }

        private enum State {
            ACCOUNT_ID,
            CREATE_CONFIRMATION,
            LOGIN_PASSWORD,
            NEW_PASSWORD,
            CONFIRM_PASSWORD,
            EMAIL,
            PERSONA_NAME,
            GENDER
        }

        private record Result(boolean shouldDisconnect, Optional<TelnetPersona> replacement) {
            private static Result continueLogin() {
                return new Result(false, Optional.empty());
            }

            private static Result disconnectSession() {
                return new Result(true, Optional.empty());
            }

            private static Result replaceWith(TelnetPersona persona) {
                return new Result(false, Optional.of(persona));
            }
        }
    }

    private record LpmuseumAccount(
            String accountId,
            String personaName,
            String gender,
            String email,
            String passwordHash) {}

    private static final class LpmuseumAccountStore {
        private LpmuseumAccountStore() {}

        private static Optional<LpmuseumAccount> load(Path mudlibRoot, String accountId) {
            Path path = accountPath(mudlibRoot, accountId);
            if (!Files.isRegularFile(path)) {
                return Optional.empty();
            }
            try {
                JsonNode fields = JSON.readTree(path.toFile()).path("fields");
                String personaName = field(fields, "persona_name", "visitor");
                return Optional.of(new LpmuseumAccount(
                        field(fields, "account_id", accountId),
                        personaName.isBlank() ? "visitor" : personaName,
                        field(fields, "gender", "none"),
                        field(fields, "email", ""),
                        field(fields, "password_hash", "")));
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        private static void save(Path mudlibRoot, LpmuseumAccount account) {
            Path path = accountPath(mudlibRoot, account.accountId());
            ObjectNode root = JSON.createObjectNode();
            ObjectNode fields = JSON.createObjectNode();
            root.put("format", "jvmud.lpc-object-state");
            root.put("version", 1);
            root.set("fields", fields);
            putString(fields, "lpmuseum.account.account_id", account.accountId());
            putString(fields, "lpmuseum.account.password_hash", account.passwordHash());
            putString(fields, "lpmuseum.account.email", account.email());
            putString(fields, "lpmuseum.account.gender", account.gender());
            putString(fields, "lpmuseum.account.persona_name", account.personaName().toLowerCase());
            putInt(fields, "lpmuseum.account.account_created", 1);
            try {
                Files.createDirectories(path.getParent());
                JSON.writeValue(path.toFile(), root);
            } catch (IOException e) {
                throw new IllegalStateException("Could not save LPMuseum account " + account.accountId(), e);
            }
        }

        private static Path accountPath(Path mudlibRoot, String accountId) {
            return mudlibRoot.resolve("accounts").resolve(accountId + ".o").normalize();
        }

        private static String field(JsonNode fields, String suffix, String fallback) {
            if (!fields.isObject()) {
                return fallback;
            }
            var names = fields.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                if (name.endsWith("." + suffix)) {
                    JsonNode value = fields.path(name).path("value");
                    if (value.isTextual()) {
                        return value.asText();
                    }
                    if (value.isInt()) {
                        return Integer.toString(value.asInt());
                    }
                }
            }
            return fallback;
        }

        private static void putString(ObjectNode fields, String name, String value) {
            ObjectNode field = JSON.createObjectNode();
            field.put("type", "string");
            field.put("value", value != null ? value : "");
            fields.set(name, field);
        }

        private static void putInt(ObjectNode fields, String name, int value) {
            ObjectNode field = JSON.createObjectNode();
            field.put("type", "int");
            field.put("value", value);
            fields.set(name, field);
        }
    }
}
