package com.noxcrew.noxesium.fabric.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.fabric.NoxesiumMod;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes changes to Sodium settings bypass the override on the actual value of the setting
 * being used by code.
 */
@Mixin(value = OptionImpl.class, remap = false)
public class SodiumSettingOverrideMixin {

    @WrapMethod(method = "reset")
    public void reset(Operation<Void> original) {
        NoxesiumMod.getInstance().disableSettingOverrides = true;
        original.call();
        NoxesiumMod.getInstance().disableSettingOverrides = false;
    }
}
