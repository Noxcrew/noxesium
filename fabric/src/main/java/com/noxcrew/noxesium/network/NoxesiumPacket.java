package com.noxcrew.noxesium.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;

/**
 * The basis for a fabric packet as used by Noxesium, these always contain
 * a version varint which allows different versions of the mod to communicate.
 */
public abstract class NoxesiumPacket implements FabricPacket {

    /** The version of this packet. */
    public final int version;

    /**
     * Creates a new Noxesium packet with the given version.
     *
     * @param version The version of this packet, this is always
     *                the first varint of any Noxesium packet and
     *                allows the contents of packets to change
     *                over time without much issue.
     */
    public NoxesiumPacket(int version) {
        this.version = version;
    }
}
