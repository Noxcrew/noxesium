package com.noxcrew.noxesium.mixin.legacy.culling;

import com.noxcrew.noxesium.feature.entity.ArmorStandCullingExtension;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * Update the culling bounding box for armor stands to include the hitbox of the model on their head or arms.
 * <p>
 * Will be removed in a future version as using armor stands for models is
 * no longer recommended, use display entities instead which let you customise
 * the culling hitbox.
 */
@Mixin(LivingEntity.class)
@Deprecated
public abstract class ArmorStandCullingMixin implements ArmorStandCullingExtension {

    @Unique
    private AABB noxesium$cullingBoundingBox;

    @Override
    public AABB noxesium$getCullingBoundingBox() {
        return noxesium$cullingBoundingBox;
    }

    @Override
    public void noxesium$setCullingBoundingBox(AABB boundingBox) {
        noxesium$cullingBoundingBox = boundingBox;
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("RETURN"))
    public void updateBoundingBoxOnSyncedData(EntityDataAccessor<?> entityDataAccessor, CallbackInfo ci) {
        // Update the bounding box whenever any of the body poses change (called by ClientboundSetEntityDataPacket)
        if (Objects.equals(entityDataAccessor, ArmorStand.DATA_HEAD_POSE) ||
                Objects.equals(entityDataAccessor, ArmorStand.DATA_BODY_POSE) ||
                Objects.equals(entityDataAccessor, ArmorStand.DATA_LEFT_ARM_POSE) ||
                Objects.equals(entityDataAccessor, ArmorStand.DATA_RIGHT_ARM_POSE)) {

            noxesium$cullingBoundingBox = null;
        }
    }

    @Inject(method = "onEquipItem", at = @At("RETURN"))
    public void updateBoundingBoxOnEquip(EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2, CallbackInfo ci) {
        // Update the items in the head or hand slots change (called by ClientboundSetEquipmentPacket)
        if (Objects.equals(equipmentSlot, EquipmentSlot.HEAD) ||
                Objects.equals(equipmentSlot, EquipmentSlot.MAINHAND) ||
                Objects.equals(equipmentSlot, EquipmentSlot.OFFHAND)) {

            noxesium$cullingBoundingBox = null;
        }
    }
}
