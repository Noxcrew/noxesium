package com.noxcrew.noxesium.api.component;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.noxcrew.noxesium.api.NoxesiumApi;
import org.apache.commons.lang3.tuple.Pair;

/**
 * An object that can be listened to when a component updates.
 */
public class NoxesiumComponentListener<T, R> {

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Pair<WeakReference<?>, BiConsumer<?, ComponentChangeContext<T, R>>>> listeners =
            ConcurrentHashMap.newKeySet();

    /**
     * Registers a new listener to a change of this component type. Garbage collection
     * of this listener is tied to the lifetime of the reference. The reference object should
     * only ever be referenced from within the listener using the passed instance. This prevents
     * the listener from holding its own reference captive. If you do this the listener
     * will never be properly garbage collected.
     */
    public <F> void addListener(F reference, BiConsumer<F, ComponentChangeContext<T, R>> listener) {
        listeners.removeIf((it) -> it.getKey().get() == null);
        listeners.add(Pair.of(new WeakReference<>(reference), listener));
    }

    /**
     * Returns whether this type has listeners.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Triggers this listener with the given receiver and values.
     */
    public void trigger(Object receiver, Object oldValue, Object newValue) {
        try {
            var iterator = listeners.iterator();
            var context = new ComponentChangeContext<T, R>((T) oldValue, (T) newValue, (R) receiver);
            while (iterator.hasNext()) {
                var pair = iterator.next();
                var obj = pair.getKey().get();
                if (obj == null) {
                    iterator.remove();
                    continue;
                }
                acceptAny(pair.getValue(), obj, context);
            }
        } catch (Throwable x) {
            NoxesiumApi.getLogger().info("Caught exception while emitting component change event", x);
        }
    }

    /**
     * Casts [reference] to type [F] of [consumer].
     */
    private <F> void acceptAny(
            BiConsumer<F, ComponentChangeContext<T, R>> consumer,
            Object reference,
            ComponentChangeContext<T, R> context) {
        consumer.accept((F) reference, context);
    }
}
