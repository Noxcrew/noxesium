package com.noxcrew.noxesium.mixin.ui.render;

import com.noxcrew.noxesium.feature.ui.cache.ElementManager;
import com.noxcrew.noxesium.feature.ui.cache.TabListWrapper;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "applyPlayerInfoUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private void refreshTabListOnApplyPlayerInfoUpdateAdd(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private void refreshTabListOnApplyPlayerInfoUpdateRemove(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void refreshTabListOnHandlePlayerInfoRemove(CallbackInfo ci) {
        ElementManager.getInstance(TabListWrapper.class).requestRedraw();
    }
}
