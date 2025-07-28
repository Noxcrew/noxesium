package com.noxcrew.noxesium.mixin.feature;

import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.feature.HoverSoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import static com.noxcrew.noxesium.api.NoxesiumReferences.BUKKIT_COMPOUND_ID;

@Mixin(AbstractContainerScreen.class)
public abstract class HoverSoundMixin {

    @Unique
    private static ItemStack noxesium$lastHoveredStack = ItemStack.EMPTY;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Unique
    private final static Random noxesium$random = new Random();

    @Inject(method = "onClose", at = @At("HEAD"))
    private void noxesium$onClose(CallbackInfo ci) {
        hoveredSlot = null;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void noxesium$render(GuiGraphics guiGraphics, int i, int j, float k, CallbackInfo ci) {
        ItemStack currentStack = this.hoveredSlot != null ? this.hoveredSlot.getItem() : ItemStack.EMPTY;

        if (ItemStack.matches(currentStack, noxesium$lastHoveredStack)) {
            return;
        }

        // hover off sound for the previous item
        noxesium$tryPlaySound(noxesium$lastHoveredStack, HoverSoundTag::hoverOff);

        // hover on sound for current item
        noxesium$tryPlaySound(currentStack, HoverSoundTag::hoverOn);

        noxesium$lastHoveredStack = currentStack.copy();
    }

    @Unique
    private void noxesium$tryPlaySound(ItemStack stack, Function<HoverSoundTag, Optional<HoverSoundTag.Sound>> sound) {
        Optional.ofNullable(noxesium$getHoverSoundTag(stack))
                .filter(this::noxesium$shouldPlaySound)
                .flatMap(sound)
                .ifPresent(this::noxesium$playSound);
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

        var result = HoverSoundTag.CODEC.decode(NbtOps.INSTANCE, hoverTag);
        if (result.isSuccess()) {
            return result.getOrThrow().getFirst();
        } else {
            return null;
        }
    }

    @Unique
    private void noxesium$playSound(HoverSoundTag.Sound sound) {
        var soundEvent = BuiltInRegistries.SOUND_EVENT.get(sound.sound());
        if (soundEvent.isEmpty()) return;

        float pitch = sound.pitchMax() == sound.pitchMin() ? sound.pitchMax() : noxesium$random.nextFloat(sound.pitchMin(), sound.pitchMax());

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent.get().value(), pitch, sound.volume()));
    }

    @Unique
    private boolean noxesium$shouldPlaySound(HoverSoundTag tag) {
        if (tag.onlyPlayInNonPlayerInventories()) {
            var currentScreen = (AbstractContainerScreen) (Object) this;
            return !(currentScreen instanceof InventoryScreen || currentScreen instanceof CreativeModeInventoryScreen);
        }
        return true;
    }
}