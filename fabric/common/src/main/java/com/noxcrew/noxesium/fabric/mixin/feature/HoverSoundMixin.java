package com.noxcrew.noxesium.fabric.mixin.feature;

import com.noxcrew.noxesium.fabric.feature.item.HoverSound;
import com.noxcrew.noxesium.fabric.registry.CommonItemComponentTypes;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HoverSoundMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Unique
    private static final Random noxesium$random = new Random();

    @Inject(method = "onStopHovering", at = @At("HEAD"))
    private void noxesium$onStopHovering(Slot oldSlot, CallbackInfo ci) {
        // Ignore if the inventory was closed!
        if (Minecraft.getInstance().screen == null) return;

        ItemStack previousStack = oldSlot.getItem();
        ItemStack currentStack = this.hoveredSlot != null ? this.hoveredSlot.getItem() : ItemStack.EMPTY;

        // Ignore if no change was made to the hovered item type
        if (ItemStack.matches(previousStack, currentStack)) return;

        // Hover off sound for the previous item
        noxesium$tryPlaySound(previousStack, HoverSound::hoverOff);

        // Hover on sound for current item
        noxesium$tryPlaySound(currentStack, HoverSound::hoverOn);
    }

    @Unique
    private void noxesium$tryPlaySound(ItemStack stack, Function<HoverSound, Optional<HoverSound.Sound>> sound) {
        // Ignore empty stacks
        if (stack.isEmpty()) return;

        // Read the hover sound data
        var data = stack.noxesium$getComponent(CommonItemComponentTypes.HOVER_SOUND);
        if (data == null) return;

        // If this sound should not play we ignore it
        if (!noxesium$shouldPlaySound(data)) return;

        // Play the sound effect
        var optional = sound.apply(data);
        if (optional.isEmpty()) return;
        noxesium$playSound(optional.get());
    }

    @Unique
    private void noxesium$playSound(HoverSound.Sound sound) {
        var soundEvent = sound.sound();
        var pitch = sound.pitchMax() <= sound.pitchMin()
                ? Math.max(sound.pitchMin(), sound.pitchMax())
                : noxesium$random.nextFloat(sound.pitchMin(), sound.pitchMax());

        Minecraft.getInstance()
                .getSoundManager()
                .play(SimpleSoundInstance.forUI(soundEvent.value(), pitch, sound.volume()));
    }

    /**
     * Determines whether the given sound should be played in the current screen.
     */
    @Unique
    private boolean noxesium$shouldPlaySound(HoverSound tag) {
        if (tag.onlyPlayInNonPlayerInventories()) {
            var currentScreen = (AbstractContainerScreen) (Object) this;
            return !(currentScreen instanceof InventoryScreen || currentScreen instanceof CreativeModeInventoryScreen);
        }
        return true;
    }
}
