package com.noxcrew.noxesium.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noxcrew.noxesium.feature.rule.ServerRuleModule;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = OptionImpl.class, remap = false)
public class SodiumOptionImplMixin {

    @WrapMethod(method = "reset")
    public void reset(Operation<Void> original) {
        ServerRuleModule.noxesium$disableSettingOverrides = true;
        original.call();
        ServerRuleModule.noxesium$disableSettingOverrides = false;
    }
}
