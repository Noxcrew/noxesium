package com.noxcrew.noxesium.mixin.rules.entity;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.entity.ExtraEntityData;
import java.awt.Color;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

import com.noxcrew.noxesium.feature.entity.ExtraEntityDataHolder;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TESTING ONLY: Makes cows glow a random colour :)
 */
@Mixin(Cow.class)
public class RandomCowGlowingMixin {

    private static final Random RANDOM = new Random();

    @Inject(
            method = "finalizeSpawn",
            at = @At("RETURN")
    )
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Cow cow = (Cow) (Object) this;

        Color randomColor = new Color(
                RANDOM.nextInt(256),
                RANDOM.nextInt(256),
                RANDOM.nextInt(256)
        );

        NoxesiumMod.getInstance().getLogger().warn("[1] Glow color: " + randomColor);
        cow.noxesium$setExtraData(
                ExtraEntityData.CUSTOM_GLOW_COLOR,
                Optional.of(randomColor)
        );
        NoxesiumMod.getInstance().getLogger().warn("[2] Cow spawned with glow color: " + randomColor);
        NoxesiumMod.getInstance().getLogger().warn("[4] Cow has rules: " + cow.noxesium$getExtraData(ExtraEntityData.CUSTOM_GLOW_COLOR));
        NoxesiumMod.getInstance().getLogger().warn("[4.5] Cow has ID: " + cow.getId());


    }
}