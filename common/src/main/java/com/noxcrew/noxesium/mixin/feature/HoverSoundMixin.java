package com.noxcrew.noxesium.mixin.feature;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.feature.HoverSoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.noxcrew.noxesium.api.NoxesiumReferences.BUKKIT_COMPOUND_ID;

@Mixin(AbstractContainerScreen.class)
public class HoverSoundMixin {

    @Unique
    private static ItemStack noxesium$lastHoveredStack = ItemStack.EMPTY;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(method = "onClose", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        hoveredSlot = null;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void noxesium$render(GuiGraphics guiGraphics, int i, int j, float k, CallbackInfo ci) {
        ItemStack currentStack = this.hoveredSlot != null ? this.hoveredSlot.getItem() : ItemStack.EMPTY;

        // If the hovered item has not changed, dont need to do anything
        if (ItemStack.matches(currentStack, noxesium$lastHoveredStack)) {
            return;
        }

        // play hover off sound
        if (!noxesium$lastHoveredStack.isEmpty()) {
            var hoverSoundTag = noxesium$getHoverSoundTag(noxesium$lastHoveredStack);
            if (hoverSoundTag != null && hoverSoundTag.hoverOff().isPresent()) {
                noxesium$playSound(hoverSoundTag.hoverOff().get());
            }
        }

        // play hover on sound
        if (!currentStack.isEmpty()) {
            var hoverSoundTag = noxesium$getHoverSoundTag(currentStack);
            if (hoverSoundTag != null && hoverSoundTag.hoverOn().isPresent()) {
                noxesium$playSound(hoverSoundTag.hoverOn().get());
            }
        }

        noxesium$lastHoveredStack = currentStack.copy();
    }

    @Unique
    private @Nullable HoverSoundTag noxesium$getHoverSoundTag(ItemStack stack) {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        final CompoundTag rootTag = data.getUnsafe();

        @Nullable CompoundTag hoverTag;
        if (rootTag.contains(NoxesiumReferences.HOVER_SOUND_TAG)) {
            hoverTag = rootTag.getCompound(NoxesiumReferences.HOVER_SOUND_TAG).orElse(null);
        } else {
            hoverTag = rootTag.getCompound(BUKKIT_COMPOUND_ID).orElse(null);
        }

        if (hoverTag == null) return null;

        return HoverSoundTag.CODEC.decode(NbtOps.INSTANCE, hoverTag).getOrThrow().getFirst();
    }

    @Unique
    private void noxesium$playSound(ResourceLocation resourceLocation) {
        var soundEvent = BuiltInRegistries.SOUND_EVENT.get(resourceLocation);
        if (soundEvent.isEmpty()) return;

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent.get().value(), 1.0F));
    }
}
