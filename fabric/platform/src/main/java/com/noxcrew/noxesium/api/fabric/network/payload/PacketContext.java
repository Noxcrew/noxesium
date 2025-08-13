package com.noxcrew.noxesium.api.fabric.network.payload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Stores the context of a received packet. */
public record PacketContext(Minecraft client, LocalPlayer player) {}
