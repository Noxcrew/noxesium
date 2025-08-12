package com.noxcrew.noxesium.fabric.feature.misc;

import com.noxcrew.noxesium.api.fabric.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.fabric.network.serverbound.ServerboundClientSettingsPacket;
import net.minecraft.client.Minecraft;

/**
 * Sends the server the GUI scale of the client and other relevant
 * client information.
 */
public class SyncGuiScale implements NoxesiumFeature {

    @Override
    public void onRegister() {
        syncGuiScale();
    }

    /**
     * Sends a packet to the server containing the GUI scale of the client which
     * allows servers to more accurately adapt their UI to clients.
     */
    public void syncGuiScale() {
        // Don't send if there is no established connection
        if (Minecraft.getInstance().getConnection() == null) return;

        var window = Minecraft.getInstance().getWindow();
        var options = Minecraft.getInstance().options;

        new ServerboundClientSettingsPacket(new ClientSettings(
                        options.guiScale().get(),
                        window.getGuiScale(),
                        window.getGuiScaledWidth(),
                        window.getGuiScaledHeight(),
                        Minecraft.getInstance().isEnforceUnicode(),
                        options.touchscreen().get(),
                        options.notificationDisplayTime().get()))
                .send();
    }
}
