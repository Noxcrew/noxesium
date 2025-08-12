package com.noxcrew.noxesium.api.fabric.network.handshake;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores information on an entrypoint's protocol.
 */
public record EntrypointProtocol(String id, int protocolVersion, String rawVersion) {
    /**
     * A codec for writing this protocol to a packet codec stream.
     */
    public static final StreamCodec<ByteBuf, EntrypointProtocol> STREAM_CODEC =
            StreamCodec.recursive(codec -> StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    EntrypointProtocol::id,
                    ByteBufCodecs.VAR_INT,
                    EntrypointProtocol::protocolVersion,
                    ByteBufCodecs.STRING_UTF8,
                    EntrypointProtocol::rawVersion,
                    EntrypointProtocol::new));
}
