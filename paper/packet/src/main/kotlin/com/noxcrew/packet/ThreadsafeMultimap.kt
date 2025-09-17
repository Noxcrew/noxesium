package com.noxcrew.packet

import com.google.common.collect.HashMultiset
import com.google.common.collect.Multimap
import com.google.common.collect.Multiset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/** Implements a simple multimap that is thread-safe. */
public class ThreadsafeMultimap<K, V>(
    private val backing: MutableMap<K, MutableCollection<V>> = ConcurrentHashMap(),
) : Multimap<K, V> {
    override fun put(key: K, value: V): Boolean = backing.computeIfAbsent(key) { Values(key) }.add(value)

    override fun size(): Int = backing.values.sumOf { it.size }

    override fun isEmpty(): Boolean = backing.isEmpty()

    override fun containsKey(key: Any?): Boolean = backing.containsKey(key)

    override fun containsValue(value: Any?): Boolean = backing.containsValue(value)

    override fun containsEntry(key: Any?, value: Any?): Boolean = backing[key]?.contains(value) ?: false

    override fun replaceValues(key: K, values: MutableIterable<V>): MutableCollection<V> {
        val newList = Values(key).also { it.addAll(values) }
        backing[key] = newList
        return newList
    }

    override fun putAll(multimap: Multimap<out K, out V>): Boolean {
        var changed = false
        multimap.forEach { key, value ->
            if (put(key, value)) {
                changed = true
            }
        }
        return changed
    }

    override fun putAll(key: K, values: MutableIterable<V>): Boolean {
        var changed = false
        values.forEach { value ->
            if (put(key, value)) {
                changed = true
            }
        }
        return changed
    }

    override fun remove(key: Any?, value: Any?): Boolean {
        val result = backing[key]?.remove(value) ?: return false
        backing.values.removeIf { it.isEmpty() }
        return result
    }

    override fun removeAll(key: Any?): MutableCollection<V> = backing.remove(key)?.toMutableSet() ?: mutableSetOf()

    override fun clear() {
        backing.clear()
    }

    override fun get(key: K): MutableCollection<V> = backing[key] ?: Values(key)

    override fun keySet(): MutableSet<K> = backing.keys

    override fun keys(): Multiset<K> = HashMultiset.create(backing.keys)

    override fun values(): MutableCollection<V> = MultimapValueCollection()

    override fun entries(): MutableCollection<MutableMap.MutableEntry<K, V>> =
        throw UnsupportedOperationException("Iterating over entries of threadsafe multimap is not supported")

    override fun asMap(): MutableMap<K, MutableCollection<V>> = backing

    /** The type used for the internal values. */
    private inner class Values(val key: K, val inner: MutableList<V> = CopyOnWriteArrayList()) : MutableCollection<V> {
        override val size: Int
            get() = inner.size

        override fun add(element: V): Boolean {
            if (isEmpty) {
                // If this value is currently empty it is not registered on the parent!
                require(key in backing) { "Fetching two mutable values and editing both is not allowed in the ThreadsafeMultimap" }
                backing[key] = this
            }
            return inner.add(element)
        }

        override fun clear() {
            inner.clear()
        }

        override fun isEmpty(): Boolean = inner.isEmpty()

        override fun iterator(): MutableIterator<V> = InnerIterator()

        override fun retainAll(elements: Collection<V>): Boolean {
            var changed = false
            for (element in inner) {
                if (element !in elements && remove(element)) changed = true
            }
            return changed
        }

        override fun removeAll(elements: Collection<V>): Boolean {
            var changed = false
            for (element in elements) {
                if (remove(element)) changed = true
            }
            return changed
        }

        override fun remove(element: V): Boolean = inner.remove(element).also {
            // If we removed an element and the list is now empty we remove it
            if (it && isEmpty) {
                backing.remove(key)
            }
        }

        override fun containsAll(elements: Collection<V>): Boolean = inner.containsAll(elements)

        override fun contains(element: V): Boolean = inner.contains(element)

        override fun addAll(elements: Collection<V>): Boolean {
            var changed = false
            for (element in elements) {
                if (add(element)) changed = true
            }
            return changed
        }

        /** An iterator for editing the values sublist. */
        private inner class InnerIterator : MutableIterator<V> {
            private val iterator = inner.iterator()
            private var last: V? = null

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): V {
                val next = iterator.next()
                last = next
                return next
            }

            override fun remove() {
                inner.remove(last)
            }
        }
    }

    /** Returns the values of a multimap. */
    private inner class MultimapValueCollection : MutableCollection<V> {
        override val size: Int
            get() = backing.values.sumOf { it.size }

        override fun clear() {
            backing.clear()
        }

        override fun isEmpty(): Boolean = backing.values.all { it.isEmpty() }

        override fun iterator(): MutableIterator<V> = InnerIterator()

        override fun retainAll(elements: Collection<V>): Boolean {
            var changed = false
            for (element in iterator()) {
                if (element !in elements && remove(element)) changed = true
            }
            return changed
        }

        override fun removeAll(elements: Collection<V>): Boolean {
            var changed = false
            for (element in elements) {
                if (remove(element)) changed = true
            }
            return changed
        }

        override fun remove(element: V): Boolean {
            var changed = false
            for (key in backing.keys.toSet()) {
                val values = backing[key] ?: continue
                if (values.remove(element)) {
                    changed = true

                    if (values.isEmpty()) {
                        backing -= key
                    }
                }
            }
            return changed
        }

        override fun containsAll(elements: Collection<V>): Boolean = elements.all { contains(it) }

        override fun addAll(elements: Collection<V>): Boolean =
            throw UnsupportedOperationException("Cannot add to a multimap values() object")

        override fun add(element: V): Boolean = throw UnsupportedOperationException("Cannot add to a multimap values() object")

        override fun contains(element: V): Boolean = backing.values.any { element in it }

        /** An iterator for editing the values sublist. */
        private inner class InnerIterator : MutableIterator<V> {
            private val iterator = backing.iterator()
            private var currentSubIterator: Iterator<V>? = null

            private var lastKey: K? = null
            private var lastValue: V? = null

            override fun hasNext(): Boolean {
                // If the sub-iterator has a result we can go!
                if (currentSubIterator?.hasNext() == true) {
                    return true
                }

                // If the sub-iterator is done we want to go to the
                // next entry on the main iterator, or stop if we have nothing
                if (!iterator.hasNext()) return false
                val (key, next) = iterator.next()
                lastKey = key
                currentSubIterator = next.iterator()
                return hasNext()
            }

            override fun next(): V {
                // Determine if the sub-iterator has another value
                if (currentSubIterator?.hasNext() == true) {
                    lastValue = currentSubIterator!!.next()
                    if (currentSubIterator?.hasNext() == false) {
                        currentSubIterator = null
                    }
                    return lastValue!!
                }

                val (key, next) = iterator.next()
                lastKey = key
                currentSubIterator = next.iterator()
                return next()
            }

            override fun remove() {
                remove(lastKey, lastValue)
            }
        }
    }
}
