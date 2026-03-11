//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.noxcrew.noxesium.core.fabric.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public class ExplosionPrediction implements Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private static final float LARGE_EXPLOSION_RADIUS = 2.0F;
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final Level level;
    private final Vec3 center;
    private final @Nullable Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final Map<Player, Vec3> hitPlayers = new HashMap();

    public ExplosionPrediction(Level serverLevel, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, Vec3 vec3, float f, boolean bl, Explosion.BlockInteraction blockInteraction) {
        this.level = serverLevel;
        this.source = entity;
        this.radius = f;
        this.center = vec3;
        this.fire = bl;
        this.blockInteraction = blockInteraction;
        this.damageSource = damageSource == null ? serverLevel.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return (ExplosionDamageCalculator)(entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity));
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity) {
        AABB aABB = entity.getBoundingBox();
        double d = (double)1.0F / ((aABB.maxX - aABB.minX) * (double)2.0F + (double)1.0F);
        double e = (double)1.0F / ((aABB.maxY - aABB.minY) * (double)2.0F + (double)1.0F);
        double f = (double)1.0F / ((aABB.maxZ - aABB.minZ) * (double)2.0F + (double)1.0F);
        double g = ((double)1.0F - Math.floor((double)1.0F / d) * d) / (double)2.0F;
        double h = ((double)1.0F - Math.floor((double)1.0F / f) * f) / (double)2.0F;
        if (!(d < (double)0.0F) && !(e < (double)0.0F) && !(f < (double)0.0F)) {
            int i = 0;
            int j = 0;

            for(double k = (double)0.0F; k <= (double)1.0F; k += d) {
                for(double l = (double)0.0F; l <= (double)1.0F; l += e) {
                    for(double m = (double)0.0F; m <= (double)1.0F; m += f) {
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
                        double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
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
                                h -= ((Float)optional.get() + 0.3F) * 0.3F;
                            }

                            if (h > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, h)) {
                                set.add(blockPos);
                            }

                            m += d * (double)0.3F;
                            n += e * (double)0.3F;
                            o += f * (double)0.3F;
                        }
                    }
                }
            }
        }

        return new ObjectArrayList(set);
    }

    private void hurtEntities() {
        if (!(this.radius < 1.0E-5F)) {
            float f = this.radius * 2.0F;
            int i = Mth.floor(this.center.x - (double)f - (double)1.0F);
            int j = Mth.floor(this.center.x + (double)f + (double)1.0F);
            int k = Mth.floor(this.center.y - (double)f - (double)1.0F);
            int l = Mth.floor(this.center.y + (double)f + (double)1.0F);
            int m = Mth.floor(this.center.z - (double)f - (double)1.0F);
            int n = Mth.floor(this.center.z + (double)f + (double)1.0F);

            for(Entity entity : this.level.getEntities(this.source, new AABB((double)i, (double)k, (double)m, (double)j, (double)l, (double)n))) {
                if (!entity.ignoreExplosion(this)) {
                    double d = Math.sqrt(entity.distanceToSqr(this.center)) / (double)f;
                    if (!(d > (double)1.0F)) {
                        Vec3 vec3 = entity instanceof PrimedTnt ? entity.position() : entity.getEyePosition();
                        Vec3 vec32 = vec3.subtract(this.center).normalize();
                        boolean bl = this.damageCalculator.shouldDamageEntity(this, entity);
                        float g = this.damageCalculator.getKnockbackMultiplier(entity);
                        float h = !bl && g == 0.0F ? 0.0F : getSeenPercent(this.center, entity);

                        double var10000;
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity)entity;
                            var10000 = livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
                        } else {
                            var10000 = (double)0.0F;
                        }

                        double e = var10000;
                        double o = ((double)1.0F - d) * (double)h * (double)g * ((double)1.0F - e);
                        Vec3 vec33 = vec32.scale(o);
                        entity.push(vec33);
                        if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile) {
                            Projectile projectile = (Projectile)entity;
                            projectile.setOwner(this.damageSource.getEntity());
                        } else if (entity instanceof Player) {
                            Player player = (Player)entity;
                            if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                                this.hitPlayers.put(player, vec33);
                            }
                        }

                        entity.onExplosionHit(this.source);
                    }
                }
            }

        }
    }

    private void interactWithBlocks(List<BlockPos> list) {
        List<StackCollector> list2 = new ArrayList();
        Util.shuffle(list, this.level.random);

        for(BlockPos blockPos : list) {
            //this.level.getBlockState(blockPos).onExplosionHit(this.level, blockPos, this, (itemStack, blockPosx) -> addOrAppendStack(list2, itemStack, blockPosx));
        }

        for(StackCollector stackCollector : list2) {
            net.minecraft.world.level.block.Block.popResource(this.level, stackCollector.pos, stackCollector.stack);
        }

    }

    private void createFire(List<BlockPos> list) {
        for(BlockPos blockPos : list) {
            if (this.level.random.nextInt(3) == 0 && this.level.getBlockState(blockPos).isAir() && this.level.getBlockState(blockPos.below()).isSolidRender()) {
                this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
            }
        }

    }

    public int explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, this.center);
        List<BlockPos> list = this.calculateExplodedPositions();
        this.hurtEntities();
        if (this.interactsWithBlocks()) {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("explosion_blocks");
            this.interactWithBlocks(list);
            profilerFiller.pop();
        }

        if (this.fire) {
            this.createFire(list);
        }

        return list.size();
    }

    private static void addOrAppendStack(List<StackCollector> list, ItemStack itemStack, BlockPos blockPos) {
        for(StackCollector stackCollector : list) {
            stackCollector.tryMerge(itemStack);
            if (itemStack.isEmpty()) {
                return;
            }
        }

        list.add(new StackCollector(blockPos, itemStack));
    }

    private boolean interactsWithBlocks() {
        return this.blockInteraction != BlockInteraction.KEEP;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
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

    public DamageSource getDamageSource() {
        return this.damageSource;
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

    public boolean isSmall() {
        return this.radius < 2.0F || !this.interactsWithBlocks();
    }

    static class StackCollector {
        final BlockPos pos;
        ItemStack stack;

        StackCollector(BlockPos blockPos, ItemStack itemStack) {
            this.pos = blockPos;
            this.stack = itemStack;
        }

        public void tryMerge(ItemStack itemStack) {
            if (ItemEntity.areMergable(this.stack, itemStack)) {
                this.stack = ItemEntity.merge(this.stack, itemStack, 16);
            }

        }
    }
}
