package com.noxcrew.noxesium.core.fabric.feature;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.core.feature.ChatVisibility;
import com.noxcrew.noxesium.core.feature.ClientSettings;
import com.noxcrew.noxesium.core.network.serverbound.ServerboundClientSettingsPacketV2;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * Sends the server the GUI scale of the client and other relevant
 * client information.
 */
public class SyncGuiScale extends NoxesiumFeature {

    private boolean requested = false;
    private int requestedCooldown = 0;

    public SyncGuiScale() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            if (!isRegistered()) return;

            // Ignore if not requested
            if (!requested || requestedCooldown-- > 0) return;
            requested = false;
            requestedCooldown = 20;

            // Don't send if there is no established connection
            if (Minecraft.getInstance().getConnection() == null) return;

            var window = Minecraft.getInstance().getWindow();
            var options = Minecraft.getInstance().options;

            NoxesiumServerboundNetworking.send(new ServerboundClientSettingsPacketV2(new ClientSettings(
                    options.guiScale().get(),
                    window.getGuiScale(),
                    window.getGuiScaledWidth(),
                    window.getGuiScaledHeight(),
                    Minecraft.getInstance().isEnforceUnicode(),
                    options.touchscreen().get(),
                    options.notificationDisplayTime().get(),
                    switch (options.chatVisibility().get()) {
                        case FULL -> ChatVisibility.FULL;
                        case HIDDEN -> ChatVisibility.HIDDEN;
                        case SYSTEM -> ChatVisibility.SYSTEM;
                    },
                    options.chatWidth().get(),
                    options.chatHeightUnfocused().get(),
                    options.fov().get(),
                    options.fovEffectScale().get())));
        });
    }

    @Override
    public void onRegister() {
        syncGuiScale();
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public void syncGuiScale() {
        requested = true;
    }
}
