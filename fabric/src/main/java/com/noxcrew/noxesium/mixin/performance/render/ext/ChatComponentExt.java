package com.noxcrew.noxesium.mixin.performance.render.ext;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatComponentExt {

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getTrimmedMessages();

    @Accessor("chatScrollbarPos")
    int getChatScrollbarPos();

    @Accessor("newMessageSinceScroll")
    boolean getNewMessageSinceScroll();

    @Invoker("isChatHidden")
    boolean isChatHidden();

    @Invoker("isChatFocused")
    boolean isChatFocused();

    @Invoker("getLineHeight")
    int getLineHeight();

    @Invoker("getTimeFactor")
    static double getTimeFactor(int time) {
        throw new AssertionError("Unimplemented");
    }

    @Invoker("screenToChatX")
    double screenToChatX(double screenX);

    @Invoker("screenToChatY")
    double screenToChatY(double screenY);

    @Invoker("getMessageEndIndexAt")
    int getMessageEndIndexAt(double x, double y);
}
