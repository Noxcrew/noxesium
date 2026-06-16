package com.noxcrew.noxesium.core.fabric.mixin.feature.entity.trident;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.core.fabric.util.InventoryHelper;
import com.noxcrew.noxesium.core.registry.CommonGameComponentTypes;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides the item shown as being held in the main hand and
 * removes flickering on the charging animation of the trident.
 * <p>
 * Applied at low priority so it modifies the main hand item before other mods such as Axiom who
 * want to clear it.
 */
@Mixin(value = ItemInHandRenderer.class, priority = -2000)
public abstract class TridentHandModelMixin {

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

    @Shadow
    public abstract void renderItem(
            LivingEntity livingEntity,
            ItemStack itemStack,
            ItemDisplayContext itemDisplayContext,
            PoseStack poseStack,
            SubmitNodeCollector multiBufferSource,
            int i);

    @WrapOperation(
            method = "tick()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack getSelected(LocalPlayer instance, Operation<ItemStack> original) {
        return InventoryHelper.getRealSelected(instance.getInventory());
    }

    @Inject(method = "submitArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    public void submitArmWithItem(
            AbstractClientPlayer player,
            float frameInterp,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack itemStack,
            float inverseArmHeight,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            CallbackInfo ci) {
        if (!GameComponents.getInstance()
                .noxesium$hasComponent(CommonGameComponentTypes.CLIENT_AUTHORITATIVE_RIPTIDE_TRIDENTS)) return;

        // We specifically want to prioritise the spin attack animation over the first 50% of the charging animation of
        // the trident because during that time the hand height rapidly changes.
        if (player.isScoping()) return;
        if (!player.isAutoSpinAttack()) return;

        // Ignore if charging an item beyond the first 50%
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            if (itemStack.getUseAnimation() != ItemUseAnimation.SPEAR) return;

            float f7 = (float) itemStack.getUseDuration(player)
                    - ((float) player.getUseItemRemainingTicks() - frameInterp + 1.0F);
            float f11 = f7 / 10.0F;
            if (f11 >= 0.5F) return;
        }

        // Render the auto spin attack animation
        boolean flag = hand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm =
                flag ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean flag3 = humanoidarm == HumanoidArm.RIGHT;
        this.applyItemArmTransform(
                poseStack, humanoidarm, 0f); // Always render at the top so the attack speed doesn't interfere!
        int j = flag3 ? 1 : -1;
        poseStack.translate((float) j * -0.4F, 0.8F, 0.3F);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) j * 65.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) j * -85.0F));
        this.renderItem(
                player,
                itemStack,
                flag3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                poseStack,
                submitNodeCollector,
                lightCoords);
        ci.cancel();
    }
}
