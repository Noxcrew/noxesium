package com.noxcrew.noxesium.mixin.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

/**
 * Update the culling bounding box for armor stands to include the hitbox of the model on their head or arms.
 * <p>
 * This patch will be dropped in the future as usage of display entities is preferred over armor stands. Display
 * entities allow you to modify these culling hitboxes on the server-side which allows for finer control and wider
 * adoption. This patch only works for servers that specifically use head items on armor stands which makes it
 * ill-suited for wide adoption.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private AABB noxesium$cullingBoundingBox;
    @Unique
    private float noxesium$lastYBodyRot = 0f;

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

    @Inject(method = "getBoundingBoxForCulling", at = @At("HEAD"), cancellable = true)
    public void extendedBoundingBoxToIncludeNoxesiumModel(CallbackInfoReturnable<AABB> cir) {
        if (((Entity) (Object) this) instanceof ArmorStand armorStand) {
            // Invalidate if the y rotation changed
            if (noxesium$lastYBodyRot != armorStand.yBodyRot) {
                noxesium$cullingBoundingBox = null;
            }

            // Re-calculate the bounding box if necessary
            if (noxesium$cullingBoundingBox == null) {
                noxesium$lastYBodyRot = armorStand.yBodyRot;
                noxesium$cullingBoundingBox = noxesium$updateBoundingBox(armorStand);
            }
            if (noxesium$cullingBoundingBox != null) {
                cir.setReturnValue(noxesium$cullingBoundingBox.move(((Entity) (Object) this).position()));
            }
        }
    }

    /**
     * Recalculates the bounding box for [armorStand].
     */
    @Unique
    private AABB noxesium$updateBoundingBox(ArmorStand armorStand) {
        // Only go through this entity if it has some item in its hand or head slot
        if (armorStand.hasItemInSlot(EquipmentSlot.HEAD) ||
                armorStand.hasItemInSlot(EquipmentSlot.MAINHAND) ||
                armorStand.hasItemInSlot(EquipmentSlot.OFFHAND)) {

            var boundingBox = AABB.ofSize(Vec3.ZERO, 0, 0, 0);
            var itemRenderer = Minecraft.getInstance().getItemRenderer();
            var randomSource = RandomSource.create(42);
            var poseStack = new PoseStack();

            // Set up the pose stack
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - armorStand.yBodyRot));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0D, (double) -1.501F, 0.0D);

            // Determine which items are being held
            var flag = armorStand.getMainArm() == HumanoidArm.LEFT;
            var leftItem = flag ? armorStand.getMainHandItem() : armorStand.getOffhandItem();
            var rightItem = flag ? armorStand.getOffhandItem() : armorStand.getMainHandItem();

            // Determine which item model we're using
            BakedModel itemModel = null;

            // Try out the head item
            if (armorStand.hasItemInSlot(EquipmentSlot.HEAD)) {
                // Set up the pose stack for drawing the model
                var item = armorStand.getItemBySlot(EquipmentSlot.HEAD);
                itemModel = itemRenderer.getModel(item, armorStand.level(), armorStand, 0);

                // Rotate the bounding box following the rotation of the entity
                var pose = armorStand.getEntityData().get(ArmorStand.DATA_HEAD_POSE);
                noxesium$setRotations(poseStack, pose);
                CustomHeadLayer.translateToHead(poseStack, false);
                itemModel.getTransforms().getTransform(ItemDisplayContext.HEAD).apply(false, poseStack);
                poseStack.translate(-0.5D, -0.5D, -0.5D);
            }

            // Try the hand items
            else if (!leftItem.isEmpty() || !rightItem.isEmpty()) {
                // Rotate the bounding box following the rotation of the entity
                var flag2 = rightItem.isEmpty();
                var pose = armorStand.getEntityData().get(flag2 ? ArmorStand.DATA_LEFT_ARM_POSE : ArmorStand.DATA_RIGHT_ARM_POSE);
                if (flag2) {
                    poseStack.translate(5.0 / 16.0, 0.0, 0.0);
                } else {
                    poseStack.translate(-5.0 / 16.0, 0.0, 0.0);
                }
                noxesium$setRotations(poseStack, pose);

                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate((float) (flag2 ? -1 : 1) / 16.0F, 0.125D, -0.625D);
                itemModel = itemRenderer.getModel(flag2 ? leftItem : rightItem, armorStand.level(), armorStand, 0);
            }

            // If there is no item model we return
            if (itemModel == null) return boundingBox;

            var memorystack = MemoryStack.stackPush();
            var bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            var intbuffer = bytebuffer.asIntBuffer();

            // Go through all quads and extend the bounding box
            for (var direction : Direction.values()) {
                var quads = itemModel.getQuads(null, direction, randomSource);
                boundingBox = noxesium$iterateQuads(poseStack, boundingBox, bytebuffer, intbuffer, quads);
            }
            var quads = itemModel.getQuads(null, null, randomSource);
            boundingBox = noxesium$iterateQuads(poseStack, boundingBox, bytebuffer, intbuffer, quads);
            memorystack.close();

            return boundingBox;
        }
        return null;
    }

    @Unique
    private void noxesium$setRotations(PoseStack poseStack, Rotations pose) {
        var xRot = ((float) Math.PI / 180F) * pose.getX();
        var yRot = ((float) Math.PI / 180F) * pose.getY();
        var zRot = ((float) Math.PI / 180F) * pose.getZ();
        if (zRot != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotation(zRot));
        }
        if (yRot != 0.0F) {
            poseStack.mulPose(Axis.YP.rotation(yRot));
        }
        if (xRot != 0.0F) {
            poseStack.mulPose(Axis.XP.rotation(xRot));
        }
    }

    /**
     * Iterate through all quads in the int buffer to update the bounding box.
     */
    @Unique
    private static AABB noxesium$iterateQuads(PoseStack poseStack, AABB boundingBox, ByteBuffer bytebuffer, IntBuffer intbuffer, List<BakedQuad> quads) {
        var matrix4f = poseStack.last().pose();
        for (var quad : quads) {
            int j = quad.getVertices().length / 8;
            for (int k = 0; k < j; ++k) {
                intbuffer.clear();
                intbuffer.put(quad.getVertices(), k * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                boundingBox = noxesium$expandToInclude(boundingBox, matrix4f.transform(new Vector4f(f, f1, f2, 1.0F)));
            }
        }
        return boundingBox;
    }

    /**
     * Expands the given [boundingBox] if necessary to include [vector].
     */
    @Unique
    private static AABB noxesium$expandToInclude(AABB boundingBox, Vector4f vector) {
        // Avoid creating a new object if possible
        if (boundingBox.contains(vector.x(), vector.y(), vector.z())) return boundingBox;

        double d = Math.min(boundingBox.minX, vector.x());
        double e = Math.min(boundingBox.minY, vector.y());
        double f = Math.min(boundingBox.minZ, vector.z());
        double g = Math.max(boundingBox.maxX, vector.x());
        double h = Math.max(boundingBox.maxY, vector.y());
        double i = Math.max(boundingBox.maxZ, vector.z());
        return new AABB(d, e, f, g, h, i);
    }
}
