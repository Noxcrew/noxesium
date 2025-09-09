package com.noxcrew.noxesium.legacy.paper.api.network.clientbound

import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPackets
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import org.joml.Vector3f

/**
 * Sent by a server to start a Noxesium custom sound. If a sound with the same id
 * is already playing, that sound will be stopped.
 */
public data class ClientboundCustomSoundStartPacket(
    public val id: Int,
    public val sound: ResourceLocation,
    public val source: SoundSource,
    public val looping: Boolean,
    /**
     * Whether this sound has attenuation. If `false`, the sound is played at the same
     * volume regardless of distance to the position. Should be `true` for most sounds.
     */
    public val attenuation: Boolean,
    /** Whether to ignore playing the sound if the id is already playing another sound. */
    public val ignoreIfPlaying: Boolean,
    public val volume: Float,
    public val pitch: Float,
    /** The position where the sound is playing, can be null. */
    public val position: Vector3f,
    /** The entity that the sound is playing relative to, can be null. */
    public val entityId: Int? = null,
    public val unix: Long? = null,
    /** The offset of the sound in seconds. */
    public val offset: Float? = null,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_START_SOUND)
