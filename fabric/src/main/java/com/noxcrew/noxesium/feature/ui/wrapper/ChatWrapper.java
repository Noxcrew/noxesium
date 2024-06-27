package com.noxcrew.noxesium.feature.ui.wrapper;

import com.noxcrew.noxesium.mixin.ui.render.ext.ChatComponentExt;
import com.noxcrew.noxesium.mixin.ui.render.ext.GuiExt;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps around the chat and updates it whenever the fade effect updates.
 */
public class ChatWrapper extends ElementWrapper {

    public ChatWrapper() {
        // Re-evaluate which lines are fading out every tick
        registerVariable("fading", (minecraft, partialTicks) -> {
            var chatOverlay = minecraft.gui.getChat();
            var chatExt = (ChatComponentExt) chatOverlay;
            var guiExt = (GuiExt) minecraft.gui;
            var focused = chatExt.invokeIsChatFocused();
            var messages = new ArrayList<>(chatExt.getTrimmedMessages());
            if (messages.isEmpty()) return List.of();

            var fading = new ArrayList<>();

            var index = 0;
            for (var line : messages) {
                index++;

                var ticksSinceMessageSend = guiExt.getTickCount() - line.addedTime();
                var timeFactor = focused ? 1.0 : ChatComponentExt.invokeGetTimeFactor(ticksSinceMessageSend);
                if (timeFactor < 1.0) {
                    fading.add(index - 1);
                }
            }
            return fading;
        });
    }
}
