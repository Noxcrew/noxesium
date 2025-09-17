package com.noxcrew.packet

/**
 * When registered via [MinecraftPacketApi.registerListener], all functions annotated with [PacketHandler]
 * are registered as handlers for the respective packet type.
 *
 * @see PacketHandler
 */
public interface PacketListener

/**
 * When a [PacketListener] is registered via [MinecraftPacketApi.registerListener], all of its functions
 * annotated with [PacketHandler] are registered as handlers for the respective packet type.
 *
 * @param priority The priority of the packet handler being run. Handlers with a lower priority are
 * run before those with a higher priority. This value should not be negative.
 *
 * @see PacketListener
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class PacketHandler(val priority: Int = MinecraftPacketApi.DEFAULT_HANDLER_PRIORITY)
