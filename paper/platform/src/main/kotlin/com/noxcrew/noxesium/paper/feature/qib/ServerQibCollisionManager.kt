package com.noxcrew.noxesium.paper.feature.qib

import com.noxcrew.noxesium.api.feature.qib.QibEffect
import com.noxcrew.noxesium.core.nms.feature.qib.QibCollisionManager
import com.noxcrew.noxesium.core.nms.feature.qib.SpatialTree
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.paper.component.getNoxesiumComponent
import net.kyori.adventure.key.Key
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.random.WeightedList
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import java.util.Optional

/** Extends the qib collision manager with server-sided logic. */
public class ServerQibCollisionManager(player: Player, spatialTree: SpatialTree) : QibCollisionManager(player, spatialTree) {
    override fun getQibBehavior(entity: Entity): Key? = entity.bukkitEntity.getNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR)

    override fun executeBehavior(entity: Entity?, effect: QibEffect) {
        when (effect) {
            is QibEffect.PlaySound -> {
                (player as? ServerPlayer)?.connection?.send(
                    ClientboundSoundEntityPacket(
                        Holder.Direct(
                            SoundEvent.createVariableRangeEvent(
                                Identifier.fromNamespaceAndPath(effect.namespace, effect.path),
                            ),
                        ),
                        SoundSource.PLAYERS,
                        player,
                        effect.volume,
                        effect.pitch,
                        0,
                    ),
                )
            }

            is QibEffect.GivePotionEffect -> {
                val type =
                    BuiltInRegistries.MOB_EFFECT
                        .get(Identifier.fromNamespaceAndPath(effect.namespace, effect.path))
                        .orElse(null)
                player.addEffect(
                    MobEffectInstance(
                        type,
                        effect.duration,
                        effect.amplifier,
                        effect.ambient,
                        effect.visible,
                        effect.showIcon,
                    ),
                )
            }

            is QibEffect.RemovePotionEffect -> {
                player.removeEffect(
                    BuiltInRegistries.MOB_EFFECT
                        .get(Identifier.fromNamespaceAndPath(effect.namespace, effect.path))
                        .orElse(null),
                )
            }

            is QibEffect.RemoveAllPotionEffects -> {
                player.removeAllEffects()
            }

            is QibEffect.AddVelocity -> {
                // Fake an explosion packet which makes the client add the velocity to itself!
                (player as? ServerPlayer)?.connection?.send(
                    ClientboundExplodePacket(
                        Vec3(0.0, -Double.MAX_VALUE, 0.0),
                        0f,
                        0,
                        Optional.of(Vec3(effect.x, effect.y, effect.z)),
                        ParticleTypes.EXPLOSION,
                        SoundEvents.GENERIC_EXPLODE,
                        WeightedList.of(),
                    ),
                )
            }

            else -> super.executeBehavior(entity, effect)
        }
    }
}
