package com.noxcrew.noxesium.mixin.info;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.feature.render.cache.chat.ChatCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

/**
 * Syncs up with the server whenever relevant settings are updated.
 * Or whenever the chat spacing is changed we re-render chat.
 */
@Mixin(OptionInstance.class)
public class OptionInstanceMixin {

    @Redirect(method = "set", at = @At(value = "FIELD", target = "Lnet/minecraft/client/OptionInstance;onValueUpdate:Ljava/util/function/Consumer;"))
    private Consumer setOption(OptionInstance instance) {
        var options = Minecraft.getInstance().options;
        if (instance == options.touchscreen() ||
                instance == options.notificationDisplayTime()) {
            NoxesiumMod.syncGuiScale();
        }
        if (instance == options.chatLineSpacing()) {
            ChatCache.getInstance().clearCache();
        }
        return ((OptionInstanceExt) ((Object) instance)).getOnValudUpdate();
    }
}
