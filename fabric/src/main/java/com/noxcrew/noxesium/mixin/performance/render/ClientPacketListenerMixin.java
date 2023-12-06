package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "applyPlayerInfoUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private void applyPlayerInfoUpdateAdd(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private void applyPlayerInfoUpdateRemove(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void handlePlayerInfoRemove(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }
}
