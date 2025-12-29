package com.noxcrew.noxesium.core.fabric.mixin.network;

import java.util.Map;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Allows fetching the currently registered packet types.
 */
@Mixin(value = PayloadTypeRegistryImpl.class, remap = false)
public interface PayloadTypeRegistryExt {
    @Accessor("packetTypes")
    Map<?, ?> getPacketTypes();
}
