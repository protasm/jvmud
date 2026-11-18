package io.github.protasm.jvmud.engine.instance.lpc;

import io.github.protasm.jvmud.language.execution.LPCProgramRevision;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Keeps a live LPC object's identity stable while its executable receiver changes.
 *
 * <p>The first materialized receiver remains the identity used by LPC references and by engine
 * world, session, command and timer bindings. A reload prepares another receiver, copies compatible
 * field state, then installs its revision without destroying that identity. Creation hooks and LPC
 * field initializers are deliberately absent from this operation: replacing behavior must not
 * recreate residents, reset game state or repeat external effects.</p>
 *
 * <p>Historical receiver aliases use weak identity keys. An old closure keeps its captured receiver
 * available, but the alias registry alone does not retain every replaced receiver or revision. The
 * original identity and the currently installed receiver remain strongly owned while the object is
 * live; in particular, its original generated class is not reclaimable while that identity exists.</p>
 *
 * <p>This owner is confined to the mudlib's execution lane. It does not provide independent thread
 * safety or general rollback of arbitrary LPC execution.</p>
 */
public final class LPCObjectLifecycle {
    private final Map<Object, LiveObject> liveObjects = new IdentityHashMap<>();
    private final ReferenceQueue<Object> collectedReceivers = new ReferenceQueue<>();
    private final Map<ReceiverReference, Object> identitiesByReceiver = new HashMap<>();

    /** Registers a host object whose behavior has no compiled LPC revision. */
    public void register(Object identity) {
        register(identity, null);
    }

    /** Registers a newly materialized identity and its initial revision, if known. */
    public void register(Object identity, LPCProgramRevision revision) {
        Objects.requireNonNull(identity, "identity");
        LiveObject existing = liveObjects.get(identity);
        if (existing != null) {
            if (existing.revision == null && revision != null) {
                requireReceiverClass(identity, revision);
                existing.revision = revision;
            }
            return;
        }
        if (identityOf(identity) != identity) {
            throw new IllegalArgumentException("An LPC behavior receiver already belongs to another identity.");
        }
        if (revision != null) {
            requireReceiverClass(identity, revision);
        }
        liveObjects.put(identity, new LiveObject(identity, revision));
        registerReceiver(identity, identity);
    }

    /** Returns the stable identity for a known receiver, or the supplied value when unregistered. */
    public Object identityOf(Object receiver) {
        if (receiver == null) {
            return null;
        }
        discardCollectedReceivers();
        Object identity = identitiesByReceiver.get(new ReceiverReference(receiver, null));
        // Keep the lookup argument alive until the weak-key comparison has finished.
        Reference.reachabilityFence(receiver);
        return identity == null ? receiver : identity;
    }

    /** Returns the currently selected behavior receiver for an identity or historical receiver. */
    public Object receiverOf(Object object) {
        LiveObject live = liveObjects.get(identityOf(object));
        return live == null ? object : live.receiver;
    }

    /** Returns the installed compiled revision, when the object was materialized from LPC. */
    public Optional<LPCProgramRevision> revisionOf(Object object) {
        LiveObject live = liveObjects.get(identityOf(object));
        return live == null ? Optional.empty() : Optional.ofNullable(live.revision);
    }

    /**
     * Creates a replacement receiver and transfers state without changing the live object.
     *
     * <p>Every non-static, non-synthetic field must retain its declaring program, name and JVM
     * type. Rejecting schema changes makes migration deliberate rather than silently dropping or
     * reinterpreting live state. Generated constructors perform only Java allocation; LPC creation
     * hooks and field initialization are not run.</p>
     */
    public PreparedReload prepareReload(Object object, LPCProgramRevision revision) {
        Objects.requireNonNull(revision, "revision");
        Object identity = identityOf(Objects.requireNonNull(object, "object"));
        LiveObject live = requireLiveObject(identity);
        requireInactive(live);
        Object replacement = instantiate(revision);
        copyState(live.receiver, replacement);
        return new PreparedReload(this, identity, live.receiver, replacement, revision);
    }

    /**
     * Installs a prepared revision after verifying its receiver and refreshing the latest live state.
     * State validation and transfer finish before the live behavior mapping changes.
     */
    public void install(PreparedReload prepared) {
        Objects.requireNonNull(prepared, "prepared");
        if (prepared.owner != this) {
            throw new IllegalArgumentException("The prepared reload belongs to another lifecycle owner.");
        }
        LiveObject live = requireLiveObject(prepared.identity);
        requireInactive(live);
        if (live.receiver != prepared.previousReceiver) {
            throw new IllegalStateException("The LPC object changed after its reload was prepared.");
        }
        copyState(live.receiver, prepared.receiver);
        live.receiver = prepared.receiver;
        live.revision = prepared.revision;
        registerReceiver(prepared.receiver, prepared.identity);
    }

    /**
     * Runs code captured by a historical receiver against the live object's current field state.
     *
     * <p>Inline closures retain their original generated helper and class. During such a call, state
     * is copied into that receiver and nested calls on the same identity use its revision. The
     * resulting state is copied back even when the closure throws. Named-method callables instead
     * follow ordinary dispatch to the currently installed revision.</p>
     */
    public <T> T withReceiver(Object receiver, Supplier<T> action) {
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(action, "action");
        LiveObject live = liveObjects.get(identityOf(receiver));
        if (live == null) {
            return action.get();
        }
        Object previousReceiver = live.receiver;
        boolean historical = previousReceiver != receiver;
        if (historical) {
            copyState(previousReceiver, receiver);
            live.receiver = receiver;
        }
        live.activeScopes++;
        try {
            return action.get();
        } finally {
            live.activeScopes--;
            if (historical) {
                try {
                    copyState(receiver, previousReceiver);
                } finally {
                    live.receiver = previousReceiver;
                }
            }
        }
    }

    /**
     * Removes executable state when an object is destroyed, retaining its historical identity.
     *
     * <p>Stored closures and references can outlive destruction. Their receivers must still resolve
     * to the destroyed identity so the host's tombstone checks cannot mistake them for live objects.
     * The host already retains destroyed identities for that same reason.</p>
     */
    public void remove(Object object) {
        Object identity = identityOf(object);
        liveObjects.remove(identity);
    }

    private void registerReceiver(Object receiver, Object identity) {
        discardCollectedReceivers();
        identitiesByReceiver.put(new ReceiverReference(receiver, collectedReceivers), identity);
    }

    private void discardCollectedReceivers() {
        Reference<?> receiver;
        while ((receiver = collectedReceivers.poll()) != null) {
            identitiesByReceiver.remove(receiver);
        }
    }

    private LiveObject requireLiveObject(Object identity) {
        LiveObject live = liveObjects.get(identity);
        if (live == null) {
            throw new IllegalArgumentException("The LPC object is not registered with this lifecycle owner.");
        }
        return live;
    }

    private static void requireInactive(LiveObject live) {
        if (live.activeScopes != 0) {
            throw new IllegalStateException("Cannot reload an LPC object while its code is executing.");
        }
    }

    private static void requireReceiverClass(Object receiver, LPCProgramRevision revision) {
        if (receiver.getClass() != revision.objectClass()) {
            throw new IllegalArgumentException("The LPC program revision does not define this receiver's class.");
        }
    }

    private static Object instantiate(LPCProgramRevision revision) {
        try {
            var constructor = revision.objectClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw new LPCRuntimeException("Failed to allocate reload receiver for " + revision.sourceObjectId(),
                    e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw new LPCRuntimeException("Failed to allocate reload receiver for " + revision.sourceObjectId(), e);
        }
    }

    private static void copyState(Object source, Object destination) {
        Map<String, Field> sourceFields = stateFields(source.getClass());
        Map<String, Field> destinationFields = stateFields(destination.getClass());
        if (!sourceFields.keySet().equals(destinationFields.keySet())) {
            throw new LPCRuntimeException("Cannot reload " + source.getClass().getName()
                    + ": live field declarations changed. Reload requires the same declaring programs,"
                    + " field names and types; restart the object explicitly to change its state schema.");
        }
        try {
            // Validate every value before copying any: generated object types can share a binary
            // name while belonging to incompatible class-loading generations.
            for (String key : sourceFields.keySet()) {
                Field destinationField = destinationFields.get(key);
                Object value = sourceFields.get(key).get(source);
                if (value != null && !destinationField.getType().isPrimitive()
                        && !destinationField.getType().isInstance(value)) {
                    throw new LPCRuntimeException("Cannot reload " + source.getClass().getName()
                            + ": field " + key + " holds an incompatible program revision.");
                }
            }
            for (String key : sourceFields.keySet()) {
                destinationFields.get(key).set(destination, sourceFields.get(key).get(source));
            }
        } catch (IllegalAccessException e) {
            throw new LPCRuntimeException("Failed to transfer live LPC state for " + source.getClass().getName(), e);
        }
    }

    private static Map<String, Field> stateFields(Class<?> objectClass) {
        Map<String, Field> fields = new TreeMap<>();
        for (Class<?> type = objectClass; type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                String key = type.getName() + "." + field.getName() + ":" + field.getType().getName();
                fields.put(key, field);
            }
        }
        return fields;
    }

    private static final class LiveObject {
        private Object receiver;
        private LPCProgramRevision revision;
        private int activeScopes;

        private LiveObject(Object receiver, LPCProgramRevision revision) {
            this.receiver = receiver;
            this.revision = revision;
        }
    }

    /** Weak keys compare receiver identity, independently of mudlib or host equality behavior. */
    private static final class ReceiverReference extends WeakReference<Object> {
        private final int identityHash;

        private ReceiverReference(Object receiver, ReferenceQueue<Object> queue) {
            super(receiver, queue);
            identityHash = System.identityHashCode(receiver);
        }

        @Override
        public int hashCode() {
            return identityHash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ReceiverReference reference)) {
                return false;
            }
            Object receiver = get();
            return receiver != null && receiver == reference.get();
        }
    }

    /** A validated replacement that has not yet changed the live object or its engine bindings. */
    public static final class PreparedReload {
        private final LPCObjectLifecycle owner;
        private final Object identity;
        private final Object previousReceiver;
        private final Object receiver;
        private final LPCProgramRevision revision;

        private PreparedReload(LPCObjectLifecycle owner, Object identity, Object previousReceiver,
                Object receiver, LPCProgramRevision revision) {
            this.owner = owner;
            this.identity = identity;
            this.previousReceiver = previousReceiver;
            this.receiver = receiver;
            this.revision = revision;
        }

        /** Returns the live identity that will retain its world and reference bindings. */
        public Object identity() { return identity; }

        /** Returns the prepared executable receiver with the transferred live field state. */
        public Object receiver() { return receiver; }

        /** Returns the linked revision to install with the receiver. */
        public LPCProgramRevision revision() { return revision; }
    }
}
