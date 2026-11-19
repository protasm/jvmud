package io.github.protasm.jvmud.engine.instance.lpc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Owns the lexical LPC object stack for one hosted runtime.
 *
 * <p>Frames contain stable object identities, even when a call enters a replacement or historical
 * behavior receiver. {@link LPCObjectLifecycle} owns that receiver selection; this stack answers
 * which LPC object is current, which object called it, and whether an identity has an active
 * lexical frame. The latter preserves the runtime's active-object reload guard.</p>
 *
 * <p>Global compatibility functions can preserve their caller's lexical frame while executing on
 * another receiver. Their Java receiver is therefore deliberately absent from this stack. Command
 * actors, verbs and action-registration scopes have separate ownership.</p>
 *
 * <p>Thread-local frames support nested host calls and isolated tests. Hosted mutation remains
 * confined to the owning instance's execution queue.</p>
 */
final class LPCInvocationStack {
    private final LPCObjectLifecycle objectLifecycle;
    private final ThreadLocal<Deque<Object>> objects = ThreadLocal.withInitial(ArrayDeque::new);

    /** Uses the lifecycle owner's canonical identities without owning receiver or revision state. */
    LPCInvocationStack(LPCObjectLifecycle objectLifecycle) {
        this.objectLifecycle = Objects.requireNonNull(objectLifecycle, "objectLifecycle");
    }

    /** Returns the innermost lexical object identity, or {@code null} outside an object scope. */
    Object currentObject() {
        return objects.get().peek();
    }

    /**
     * Returns the enclosing lexical identity. For compatibility, a single frame reports its own
     * identity as the previous object; an empty stack reports {@code null}.
     */
    Object previousObject() {
        Deque<Object> stack = objects.get();
        if (stack.size() < 2) {
            return stack.peek();
        }
        var iterator = stack.iterator();
        iterator.next();
        return iterator.next();
    }

    /** Reports whether an identity or one of its behavior receivers has an active lexical frame. */
    boolean isExecuting(Object object) {
        return objects.get().contains(objectLifecycle.identityOf(object));
    }

    /** Enters a canonical lexical frame; host adapters using manual scopes must pair it with {@link #pop()}. */
    void push(Object object) {
        Objects.requireNonNull(object, "object");
        objects.get().push(objectLifecycle.identityOf(object));
    }

    /** Leaves the current frame, reporting an unbalanced host scope explicitly. */
    void pop() {
        Deque<Object> stack = objects.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("No current LPC object is available to pop.");
        }
        stack.pop();
    }

    /** Runs within a lexical frame and restores its caller even when authored or host code throws. */
    <T> T withCurrentObject(Object object, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        push(object);
        try {
            return action.get();
        } finally {
            pop();
        }
    }
}
