//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.noxcrew.noxesium.core.fabric.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public class ExplosionPrediction implements Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private final Explosion.BlockInteraction blockInteraction;
    private final Level level;
    private final Vec3 center;
    private final @Nullable Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;

    public ExplosionPrediction(Level serverLevel, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float f, Explosion.BlockInteraction blockInteraction) {
        this.level = serverLevel;
        this.source = entity;
        this.radius = f;
        this.center = vec3;
        this.blockInteraction = blockInteraction;
        this.damageSource = damageSource == null ? serverLevel.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity) {
        AABB aABB = entity.getBoundingBox();
        double d = 1 / ((aABB.maxX - aABB.minX) * 2 + 1);
        double e = 1 / ((aABB.maxY - aABB.minY) * 2 + 1);
        double f = 1 / ((aABB.maxZ - aABB.minZ) * 2 + 1);
        double g = (1 - Math.floor(1 / d) * d) / 2;
        double h = (1 - Math.floor(1 / f) * f) / 2;
        if (!(d < 0) && !(e < 0) && !(f < 0)) {
            int i = 0;
            int j = 0;

            for(double k = 0; k <= 1; k += d) {
                for(double l = 0; l <= 1; l += e) {
                    for(double m = 0; m <= 1; m += f) {
                        double n = Mth.lerp(k, aABB.minX, aABB.maxX);
                        double o = Mth.lerp(l, aABB.minY, aABB.maxY);
                        double p = Mth.lerp(m, aABB.minZ, aABB.maxZ);
                        Vec3 vec32 = new Vec3(n + g, o, p + h);
                        if (entity.level().clip(new ClipContext(vec32, vec3, Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }

    public float radius() {
        return this.radius;
    }

    public Vec3 center() {
        return this.center;
    }

    private List<BlockPos> calculateExplodedPositions() {
        Set<BlockPos> set = new HashSet();
        int i = 16;

        for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
                for(int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d = (float)j / 15.0F * 2.0F - 1.0F;
                        double e = (float)k / 15.0F * 2.0F - 1.0F;
                        double f = (float)l / 15.0F * 2.0F - 1.0F;
                        double g = Math.sqrt(d * d + e * e + f * f);
                        d /= g;
                        e /= g;
                        f /= g;
                        float h = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double m = this.center.x;
                        double n = this.center.y;
                        double o = this.center.z;

                        for(float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                            BlockPos blockPos = BlockPos.containing(m, n, o);
                            BlockState blockState = this.level.getBlockState(blockPos);
                            FluidState fluidState = this.level.getFluidState(blockPos);
                            if (!this.level.isInWorldBounds(blockPos)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
                            if (optional.isPresent()) {
                                h -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }

                            m += d * 0.3;
                            n += e * 0.3;
                            o += f * 0.3;
                        }
                    }
                }
            }
        }

        return new ObjectArrayList<>(set);
    }

    private void hurtEntities() {
        if (!(this.radius < 1.0E-5F)) {
            float f = this.radius * 2.0F;
            int i = Mth.floor(this.center.x - (double)f - 1);
            int j = Mth.floor(this.center.x + (double)f + 1);
            int k = Mth.floor(this.center.y - (double)f - 1);
            int l = Mth.floor(this.center.y + (double)f + 1);
            int m = Mth.floor(this.center.z - (double)f - 1);
            int n = Mth.floor(this.center.z + (double)f + 1);

            LocalPlayer player = Minecraft.getInstance().player;
            if (player.getBoundingBox().intersects(new AABB(i, k, m, j, l, n)) && !player.ignoreExplosion(this)) {
                double d = Math.sqrt(player.distanceToSqr(this.center)) / (double)f;
                if (d <= 1) {
                    Vec3 vec3 = player.getEyePosition();
                    Vec3 vec32 = vec3.subtract(this.center).normalize();
                    boolean bl = this.damageCalculator.shouldDamageEntity(this, player);
                    float g = this.damageCalculator.getKnockbackMultiplier(player);
                    float h = !bl && g == 0.0F ? 0.0F : getSeenPercent(this.center, player);

                    double e = player.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
                    double o = (1 - d) * (double)h * (double)g * (1 - e);
                    Vec3 vec33 = vec32.scale(o);
                    player.push(vec33);
                }
            }
        }
    }

    public int explode() {
        List<BlockPos> list = this.calculateExplodedPositions();
        this.hurtEntities();

        return list.size();
    }

    public ServerLevel level() {
        return null;
    }

    public @Nullable LivingEntity getIndirectSourceEntity() {
        return Explosion.getIndirectSourceEntity(this.source);
    }

    public @Nullable Entity getDirectSourceEntity() {
        return this.source;
    }

    public Explosion.BlockInteraction getBlockInteraction() {
        return this.blockInteraction;
    }

    public boolean canTriggerBlocks() {
        return false;
    }

    public boolean shouldAffectBlocklikeEntities() {
        return false;
    }
}
