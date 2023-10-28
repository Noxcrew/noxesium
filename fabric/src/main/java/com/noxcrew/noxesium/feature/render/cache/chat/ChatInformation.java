package com.noxcrew.noxesium.feature.render.cache.chat;

import com.noxcrew.noxesium.feature.render.cache.ElementInformation;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.GuiMessage;

import java.util.List;

/**
 * Stores information about the state of the chat.
 */
public record ChatInformation(
        List<GuiMessage.Line> trimmedMessages,
        int chatScrollbarPos,
        boolean newMessageSinceScroll,
        boolean focused,
        BakedComponent queueSize,
        List<BakedComponent> lines,
        boolean hasObfuscation,
        List<Integer> fading
) implements ElementInformation {

    /**
     * The fallback contents if the chat is absent.
     */
    public static final ChatInformation EMPTY = new ChatInformation(List.of(), 0, false, false, BakedComponent.EMPTY, List.of(), false, List.of());
}
