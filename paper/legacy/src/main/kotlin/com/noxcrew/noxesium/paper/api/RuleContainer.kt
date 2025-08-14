package com.noxcrew.noxesium.paper.api

import com.noxcrew.noxesium.paper.api.rule.RemoteServerRule
import java.util.concurrent.ConcurrentHashMap

/** An alias for a function that provides a server rule. */
public data class RuleFunction<T : Any>(
    public val index: Int,
    public val constructor: (Int) -> RemoteServerRule<T>,
)

/** Stores rule objects for an object. */
public data class RuleHolder(
    /** All rules sent to this client. */
    public val rules: MutableMap<Int, RemoteServerRule<*>> = ConcurrentHashMap(),
) : MutableMap<Int, RemoteServerRule<*>> by rules {
    /** Whether this profile has pending updates. */
    public val needsUpdate: Boolean
        get() = rules.values.any { it.changePending }

    /** Marks all rules as having been updated. */
    public fun markAllUpdated() {
        rules.values.forEach { it.changePending = false }
    }
}

/** Provides a container that holds rule types. */
public data class RuleContainer(
    private val rules: MutableMap<Int, RuleFunction<*>> = ConcurrentHashMap(),
    private val minimumProtocols: MutableMap<Int, Int> = ConcurrentHashMap(),
) {
    /** All contents of this container. */
    public val contents: Map<Int, RuleFunction<*>>
        get() = rules

    /** Returns whether rule [index] is available on [version]. */
    public fun isAvailable(index: Int, version: Int,): Boolean =
        version >= (minimumProtocols[index] ?: throw IllegalArgumentException("Cannot find rule with index $index"))

    /** Registers a new rule with the given [index] and [ruleSupplier]. */
    public fun register(index: Int, minimumProtocol: Int, ruleSupplier: RuleFunction<*>,) {
        require(!rules.containsKey(index)) { "Can't double register index $index" }
        rules[index] = ruleSupplier
        minimumProtocols[index] = minimumProtocol
    }

    /** Creates a new rule object, to be stored in the given map. */
    public fun <T : Any> create(index: Int, storage: MutableMap<Int, RemoteServerRule<*>>, version: Int? = null,): RemoteServerRule<T>? {
        // Ensure that this player has the required protocol version, otherwise return `null`.
        if (version != null &&
            version < (minimumProtocols[index] ?: throw IllegalArgumentException("Cannot find rule with index $index"))
        ) {
            return null
        }

        return storage.computeIfAbsent(index) {
            val function = rules[index] ?: throw IllegalArgumentException("Cannot find rule with index $index")
            function.constructor(index)
        } as RemoteServerRule<T>?
    }
}
