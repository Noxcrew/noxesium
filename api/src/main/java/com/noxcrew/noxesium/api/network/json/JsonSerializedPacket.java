package com.noxcrew.noxesium.api.network.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class should use JSON for serialization of this packet instead
 * of requiring a platform-specific serializer to be registered.
 * <p>
 * This can be used for infrequent packets which would otherwise be cumbersome to
 * serialize using platform-specific serialization into bytes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonSerializedPacket {

    /**
     * Returns the id of the JSON serializer implementation to use for this packet.
     */
    String value() default JsonSerializerRegistry.DEFAULT_SERIALIZER;
}
