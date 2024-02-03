package com.noxcrew.noxesium.mixin.performance.render;

import com.noxcrew.noxesium.feature.render.cache.tablist.TabListCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "applyPlayerInfoUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private void refreshTabListOnApplyPlayerInfoUpdateAdd(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private void refreshTabListOnApplyPlayerInfoUpdateRemove(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void refreshTabListOnHandlePlayerInfoRemove(CallbackInfo ci) {
        TabListCache.getInstance().clearCache();
    }
}
