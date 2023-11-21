package com.noxcrew.noxesium.mixin.render;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentExt {

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getTrimmedMessages();

    @Accessor("chatScrollbarPos")
    int getChatScrollbarPos();

    @Accessor("newMessageSinceScroll")
    boolean getNewMessageSinceScroll();
}
