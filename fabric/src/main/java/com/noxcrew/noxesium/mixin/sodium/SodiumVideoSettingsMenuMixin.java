package com.noxcrew.noxesium.mixin.sodium;

import com.noxcrew.noxesium.config.sodium.NoxesiumConfigMenu;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Injects into Sodium's video settings menu and adds a tab for Noxesium.
 * Set priority low enough to go before Iris.
 */
@Pseudo
@Mixin(value = SodiumOptionsGUI.class, remap = false, priority = 500)
public abstract class SodiumVideoSettingsMenuMixin {

    @Shadow
    @Final
    private List<OptionPage> pages;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(Screen prevScreen, CallbackInfo ci) {
        NoxesiumConfigMenu.configure(pages);
    }
}
