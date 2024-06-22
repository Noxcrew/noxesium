package com.noxcrew.noxesium.mixin.ui.render.ext;

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
    boolean invokeIsChatHidden();

    @Invoker("isChatFocused")
    boolean invokeIsChatFocused();

    @Invoker("getLineHeight")
    int invokeGetLineHeight();

    @Invoker("getTimeFactor")
    static double invokeGetTimeFactor(int time) {
        throw new AssertionError("Unimplemented");
    }

    @Invoker("screenToChatX")
    double invokeScreenToChatX(double screenX);

    @Invoker("screenToChatY")
    double invokeScreenToChatY(double screenY);

    @Invoker("getMessageEndIndexAt")
    int invokeGetMessageEndIndexAt(double x, double y);
}
