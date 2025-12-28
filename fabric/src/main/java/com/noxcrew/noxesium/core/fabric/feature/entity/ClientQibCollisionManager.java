package com.noxcrew.noxesium.core.fabric.feature.entity;

import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.qib.QibEffect;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundQibTriggeredPacket;
import com.noxcrew.noxesium.core.qib.QibCollisionManager;
import com.noxcrew.noxesium.core.qib.SpatialTree;
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes;
import net.kyori.adventure.key.Key;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adds client-specific qib behavior.
 */
public class ClientQibCollisionManager extends QibCollisionManager {
    public ClientQibCollisionManager(@NotNull Player player, @NotNull SpatialTree spatialTree) {
        super(player, spatialTree);
    }

    @Override
    @Nullable
    protected Key getQibBehavior(Entity entity) {
        return entity.noxesium$getComponent(CommonEntityComponentTypes.QIB_BEHAVIOR);
    }

    @Override
    protected void onQibTriggered(Key behavior, ServerboundQibTriggeredPacket.Type type, int entityId) {
        super.onQibTriggered(behavior, type, entityId);

        // Inform the server whenever a qib is triggered!
        NoxesiumServerboundNetworking.send(new ServerboundQibTriggeredPacket(behavior, type, entityId));
    }

    @Override
    protected void executeBehavior(Entity entity, QibEffect effect) {
        switch (effect) {
            case QibEffect.PlaySound playSound -> {
                player.level()
                        .playLocalSound(
                                player,
                                SoundEvent.createVariableRangeEvent(
                                        Identifier.fromNamespaceAndPath(playSound.namespace(), playSound.path())),
                                SoundSource.PLAYERS,
                                playSound.volume(),
                                playSound.pitch());
            }
            case QibEffect.GivePotionEffect giveEffect -> {
                var type = BuiltInRegistries.MOB_EFFECT
                        .get(Identifier.fromNamespaceAndPath(giveEffect.namespace(), giveEffect.path()))
                        .orElse(null);
                player.noxesium$addClientsidePotionEffect(new MobEffectInstance(
                        type,
                        giveEffect.duration(),
                        giveEffect.amplifier(),
                        giveEffect.ambient(),
                        giveEffect.visible(),
                        giveEffect.showIcon()));
            }
            case QibEffect.RemovePotionEffect removeEffect -> {
                player.noxesium$removeClientsidePotionEffect(BuiltInRegistries.MOB_EFFECT
                        .get(Identifier.fromNamespaceAndPath(removeEffect.namespace(), removeEffect.path()))
                        .orElse(null));
            }
            case QibEffect.RemoveAllPotionEffects ignored -> {
                player.noxesium$clearClientsidePotionEffects();
            }
            case QibEffect.AddVelocity addVelocity -> {
                player.push(addVelocity.x(), addVelocity.y(), addVelocity.z());
            }
            default -> super.executeBehavior(entity, effect);
        }
    }
}
