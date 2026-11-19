package io.github.protasm.jvmud.engine.instance.lpc;

import io.github.protasm.jvmud.language.runtime.Truth;

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
import java.util.function.Supplier;

/**
 * Owns authored command actions, registration lifetimes and nested command scopes for one mudlib.
 *
 * <p>The active command actor is independent of the lexical LPC object. Event delivery, captured
 * input and timed callbacks can also enter an actor scope without dispatching a command. Dispatch
 * creates its own verb and failure scopes, while the caller explicitly selects the actor scope.</p>
 *
 * <p>Action registrations retain stable actor and handler identities across reload. The existing
 * method invoker selects the handler's current receiver when an action runs. Persistent actions
 * survive interaction refreshes; scoped actions belong to the current interaction. Enablement is
 * lifecycle metadata and does not gate command dispatch.</p>
 *
 * <p>The host supplies world lookup, activity tracking, private output and the selected dispatch
 * conventions. This owner does not own world containment, sessions or the LPC invocation stack.
 * Hosted mutations remain confined to the instance's execution queue. Thread-local scopes retain
 * the existing nested-call behavior for hosted execution and standalone tests.</p>
 */
final class LPCCommands {
    private final LPCObjectLifecycle objectLifecycle;
    private final LPCInvocationStack invocationStack;
    private final LPCMethodInvoker methodInvoker;
    private final Host host;
    private final Set<Object> commandEnabledEntities =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private long commandActionSequence;
    private final Map<Object, Map<String, List<CommandAction>>> commandActions = new IdentityHashMap<>();
    private final Map<String, String> commandAliases = new LinkedHashMap<>();
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

    /** Shares existing invocation owners and a narrow bridge to hosted services. */
    LPCCommands(LPCObjectLifecycle objectLifecycle, LPCInvocationStack invocationStack,
            LPCMethodInvoker methodInvoker, Host host) {
        this.objectLifecycle = Objects.requireNonNull(objectLifecycle, "objectLifecycle");
        this.invocationStack = Objects.requireNonNull(invocationStack, "invocationStack");
        this.methodInvoker = Objects.requireNonNull(methodInvoker, "methodInvoker");
        this.host = Objects.requireNonNull(host, "host");
    }

    /** Returns lifecycle enablement metadata without imposing a dispatch gate. */
    boolean isEnabled(Object object) {
        return commandEnabledEntities.contains(object);
    }

    /**
     * Removes both ownership roles at the host's destruction boundary: actions for this actor and
     * actions supplied by this handler. Active dispatch snapshots and scopes retain their lifetime.
     */
    void discardObject(Object object) {
        commandEnabledEntities.remove(object);
        commandActions.remove(object);
        removeCommandHandler(object);
    }

    /** Marks an object as command-enabled for lifecycle and mudlib queries. */
    void enable(Object object) {
        if (object != null) {
            commandEnabledEntities.add(object);
        }
    }

    /** Clears enablement metadata without discarding the object's registered actions. */
    void disable(Object object) {
        if (object != null) {
            commandEnabledEntities.remove(object);
        }
    }

    /** Returns the innermost actor identity, or {@code null} outside an explicit actor scope. */
    Object currentActor() {
        return commandActorStack.get().peek();
    }

    /** Returns the expanded verb of the innermost dispatch, or {@code null} outside dispatch. */
    String currentVerb() {
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
    void configureAliases(Object aliases) {
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
    int notifyFailure(Object message) {
        Deque<String> failures = commandFailureStack.get();
        if (failures.isEmpty()) {
            return 0;
        }
        failures.pop();
        failures.push(String.valueOf(message));
        return 0;
    }

    /** Enters a canonical actor scope independently of lexical object or command dispatch scopes. */
    <T> T withActor(Object actor, Supplier<T> action) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(action, "action");
        commandActorStack.get().push(objectLifecycle.identityOf(actor));
        try {
            return action.get();
        } finally {
            commandActorStack.get().pop();
        }
    }

    /** Pushes a pending method and captures its persistent or temporary registration lifetime now. */
    void rememberActionMethod(String methodName) {
        pendingActionStack.get().push(new PendingAction(
                Objects.requireNonNull(methodName, "methodName"),
                scopedCommandRegistrationDepth.get() == 0));
    }

    /** Binds the latest pending method to an exact verb using the current actor and lexical handler. */
    void registerVerb(String verb) {
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
    void registerVerb(String verb, boolean prefixMatch) {
        Objects.requireNonNull(verb, "verb");
        Deque<PendingAction> pendingActions = pendingActionStack.get();
        if (pendingActions.isEmpty()) {
            return;
        }

        PendingAction pendingAction = pendingActions.pop();
        Object actor = currentActor();
        Object handler = invocationStack.currentObject();
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

    /** Removes the actor's interaction-specific actions, retaining persistent declarations. */
    void clearTemporaryActions(Object actor) {
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
    int removeActionsForHandler(Object actor, Object handler) {
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

    /** Clears all unbound methods on this thread, as required at an interaction-refresh boundary. */
    void clearPendingActionMethods() {
        pendingActionStack.get().clear();
    }

    /**
     * Marks methods remembered during this scope as temporary and restores that policy on failure.
     * Pending methods retain their own captured lifetime; leaving the scope does not clear them.
     */
    <T> T withScopedRegistration(Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        scopedCommandRegistrationDepth.set(scopedCommandRegistrationDepth.get() + 1);
        try {
            return action.get();
        } finally {
            scopedCommandRegistrationDepth.set(scopedCommandRegistrationDepth.get() - 1);
        }
    }

    /**
     * Dispatches a snapshot using the caller's separately selected actor scope.
     *
     * <p>A truthy action returns its original result; movement after a declined action also counts
     * as success. Only a normally declined command emits its latest failure message. Exceptions
     * propagate through the invoker while nested verb and failure scopes restore their callers.</p>
     */
    Object dispatch(Object actor, String commandLine) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(commandLine, "commandLine");
        host.touchActor(actor);
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
                Object environmentBefore = host.environmentOf(actor);
                Object result = invokeCommandAction(action, trimmed, argument);
                if (Truth.isTruthy(result)) {
                    return result;
                }
                if (host.environmentOf(actor) != environmentBefore) {
                    return 1;
                }
            }
            if (tryEnvironmentMovementAction(actor, verb, argument)) {
                return 1;
            }
            String failureMessage = commandFailureStack.get().peek();
            if (failureMessage != null && !failureMessage.isEmpty()) {
                host.writeToActor(actor, failureMessage);
            }
        } finally {
            commandFailureStack.get().pop();
            commandVerbStack.get().pop();
        }
        return 0;
    }

    /** Keeps legacy room exits/move policy in command handling, using host-owned containment. */
    private boolean tryEnvironmentMovementAction(Object actor, String verb, String argument) {
        Object environment = host.environmentOf(actor);
        if (environment == null || !methodInvoker.hasMethod(objectLifecycle.receiverOf(environment).getClass(), "move", 1)) {
            return false;
        }
        Object exits = methodInvoker.invokeOptional(environment, "exits");
        if (!(exits instanceof List<?> exitList) || !exitList.contains(verb)) {
            return false;
        }
        Object environmentBefore = host.environmentOf(actor);
        Object result = methodInvoker.invokeRequired(environment, "move", argument);
        return Truth.isTruthy(result) || host.environmentOf(actor) != environmentBefore;
    }

    /** Expands the first literal-space-delimited token once, retaining case-sensitive lookup. */
    private String applyCommandAlias(String commandLine) {
        int space = commandLine.indexOf(' ');
        String verb = space == -1 ? commandLine : commandLine.substring(0, space);
        String replacement = commandAliases.get(verb);
        if (replacement == null) {
            return commandLine;
        }
        return space == -1 ? replacement : replacement + commandLine.substring(space);
    }

    /**
     * Copies exact matches oldest-first, then prefix groups in key order with each group newest-first.
     * The selected newest-first convention instead orders the complete snapshot by registration.
     */
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
        if (host.newestActionsFirst())
            actions.sort(java.util.Comparator.comparingLong(CommandAction::sequence).reversed());
        return actions;
    }

    /** Selects whole-line versus parsed arguments, retaining omitted-argument compatibility. */
    private Object invokeCommandAction(CommandAction action, String commandLine, String argument) {
        String actionArgument = action.verb().isEmpty() && !host.catchAllArgumentsOnly()
                ? commandLine : argument;
        boolean hasOneArgument = methodInvoker.hasMethodAcceptingMissingArguments(
                objectLifecycle.receiverOf(action.handler()).getClass(), action.methodName(), 1);
        boolean hasNoArguments = methodInvoker.hasMethod(objectLifecycle.receiverOf(action.handler()).getClass(), action.methodName(), 0);
        if (actionArgument != null && hasOneArgument) {
            return methodInvoker.invokeRequired(action.handler(), action.methodName(), actionArgument);
        }
        if (actionArgument == null && hasOneArgument && !hasNoArguments) {
            return methodInvoker.invokeRequired(action.handler(), action.methodName(), actionArgument);
        }
        if (hasNoArguments) {
            return methodInvoker.invokeRequired(action.handler(), action.methodName());
        }
        return 0;
    }

    /** Removes the destroyed handler's registrations across all actor tables. */
    private void removeCommandHandler(Object handler) {
        for (Map<String, List<CommandAction>> actionsByVerb : commandActions.values()) {
            actionsByVerb.values().forEach(actions -> actions.removeIf(action -> action.handler() == handler));
            actionsByVerb.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
        commandActions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /** The lifetime is captured when the method is remembered, before actor and verb binding. */
    private record PendingAction(String methodName, boolean persistent) {}

    /** Immutable registration retained in an actor table and in any active dispatch snapshot. */
    private record CommandAction(String verb, Object handler, String methodName, boolean prefixMatch,
            boolean persistent, long sequence) {}

    /** Hosted facilities used by command policy, with world and session ownership left to the host. */
    interface Host {
        /** Returns the actor's current containing location for movement and success checks. */
        Object environmentOf(Object actor);

        /** Records activity even for an empty command, preserving session idle semantics. */
        void touchActor(Object actor);

        /** Delivers declined-command failure text through the host's normal private-output policy. */
        void writeToActor(Object actor, Object message);

        /** Reads the currently selected action ordering; configuration can change between calls. */
        boolean newestActionsFirst();

        /** Reads whether catch-all handlers receive parsed arguments instead of the whole line. */
        boolean catchAllArgumentsOnly();
    }
}
