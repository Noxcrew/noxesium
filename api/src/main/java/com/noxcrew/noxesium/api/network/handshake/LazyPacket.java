package com.noxcrew.noxesium.api.network.handshake;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a packet is lazy and should only be sent if the other side has
 * registered some receiver for it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LazyPacket {}
