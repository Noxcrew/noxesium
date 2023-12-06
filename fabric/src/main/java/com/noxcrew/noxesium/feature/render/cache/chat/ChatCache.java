package com.noxcrew.noxesium.feature.render.cache.chat;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import com.noxcrew.noxesium.mixin.performance.render.ext.ChatComponentExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the current cache of the chat.
 */
public class ChatCache extends ElementCache<ChatInformation> {

    private static ChatCache instance;

    public static int lastTick = 0;
    public static int mouseX = 0;
    public static int mouseY = 0;

    /**
     * Returns the current instance of this chat cache.
     */
    public static ChatCache getInstance() {
        if (instance == null) {
            instance = new ChatCache();
        }
        return instance;
    }

    public ChatCache() {
        // Re-evaluate which lines are fading out every tick
        registerVariable("fading", (minecraft, partialTicks) -> {
            var chatOverlay = minecraft.gui.getChat();
            var chatExt = (ChatComponentExt) chatOverlay;
            var focused = chatExt.isChatFocused();
            var messages = new ArrayList<>(chatExt.getTrimmedMessages());
            if (messages.isEmpty()) return List.of();

            var fading = new ArrayList<>();

            var index = 0;
            for (var line : messages) {
                index++;

                var ticksSinceMessageSend = lastTick - line.addedTime();
                var timeFactor = focused ? 1.0 : ChatComponentExt.getTimeFactor(ticksSinceMessageSend);
                if (timeFactor < 1.0) {
                    fading.add(index - 1);
                }
            }
            return fading;
        });
    }

    /**
     * Creates newly cached chat information.
     * <p>
     * Depends on the following information:
     * - Current resource pack configuration
     * - The current screen width
     * - The current chat overlay values
     * - The current chat settings (scale, width, height, line spacing)
     * - The current screen type
     * - The delayed message queue
     * - Lines that are fading out
     */
    @Override
    protected ChatInformation createCache(Minecraft minecraft, Font font) {
        var chatOverlay = minecraft.gui.getChat();
        var chatExt = (ChatComponentExt) chatOverlay;
        if (chatExt.isChatHidden() || chatExt.getTrimmedMessages().isEmpty()) {
            return ChatInformation.EMPTY;
        }

        var queueSize = minecraft.getChatListener().queueSize();
        var lines = new ArrayList<BakedComponent>();
        var focused = chatExt.isChatFocused();
        var messages = new ArrayList<>(chatExt.getTrimmedMessages());
        List<Integer> fading = getVariable("fading");

        for (var line : messages) {
            var baked = new BakedComponent(line.content(), font);
            lines.add(baked);
        }
        return new ChatInformation(
                messages,
                chatExt.getChatScrollbarPos(),
                chatExt.getNewMessageSinceScroll(),
                focused,
                new BakedComponent(Component.translatable("chat.queue", queueSize), font),
                lines,
                fading
        );
    }

    @Override
    protected void render(GuiGraphics graphics, ChatInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean dynamic) {
        var chatOverlay = minecraft.gui.getChat();
        var chatExt = (ChatComponentExt) chatOverlay;
        var messageCount = cache.trimmedMessages().size();
        int lineBottom;
        int backgroundAlpha;
        int alpha;
        int ticksSinceMessageSend;

        var linesPerPage = chatOverlay.getLinesPerPage();
        var focused = cache.focused();
        var scale = (float) chatOverlay.getScale();
        var scaledWidth = Mth.ceil((float) chatOverlay.getWidth() / scale);
        var height = graphics.guiHeight();

        // Set up the pose
        var pose = graphics.pose();
        try {
            pose.pushPose();
            pose.scale(scale, scale, 1.0f);
            pose.translate(4.0f, 0.0f, 0.0f);

            var scaledHeight = Mth.floor((float) (height - 40) / scale);
            var highlightedMessage = chatExt.getMessageEndIndexAt(chatExt.screenToChatX(mouseX), chatExt.screenToChatY(mouseY));
            var opacity = minecraft.options.chatOpacity().get() * (double) 0.9f + (double) 0.1f;
            double backgroundOpacity = minecraft.options.textBackgroundOpacity().get();
            double lineSpacing = minecraft.options.chatLineSpacing().get();
            var lineHeight = chatExt.getLineHeight();
            var lineSize = (int) Math.round(-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing);
            var shownLineCount = 0;

            // Draw all the lines in the chat
            for (int currentLine = 0; currentLine + cache.chatScrollbarPos() < messageCount && currentLine < linesPerPage; ++currentLine) {
                var messageIndex = currentLine + cache.chatScrollbarPos();

                var line = cache.trimmedMessages().get(messageIndex);
                if (line == null || (ticksSinceMessageSend = lastTick - line.addedTime()) >= 200 && !focused) continue;

                var timeFactor = focused ? 1.0 : ChatComponentExt.getTimeFactor(ticksSinceMessageSend);
                alpha = (int) (255.0 * timeFactor * opacity);
                backgroundAlpha = (int) (255.0 * timeFactor * backgroundOpacity);
                ++shownLineCount;
                if (alpha <= 3) continue;

                lineBottom = scaledHeight - currentLine * lineHeight;
                var lineDrawTop = lineBottom + lineSize;

                pose.pushPose();
                pose.translate(0.0f, 0.0f, 50.0f);
                var guiMessageTag = line.tag();

                // If fading we draw on the dynamic layer, otherwise not
                if (cache.fading().contains(messageIndex) == dynamic) {
                    graphics.fill(-4, lineBottom - lineHeight, scaledWidth + 4 + 4, lineBottom, backgroundAlpha << 24);
                    if (guiMessageTag != null) {
                        var tagColor = guiMessageTag.indicatorColor() | alpha << 24;
                        graphics.fill(-4, lineBottom - lineHeight, -2, lineBottom, tagColor);
                    }
                }


                // Always draw the hover text on the dynamic layer
                if (dynamic) {
                    if (guiMessageTag != null && messageIndex == highlightedMessage && guiMessageTag.icon() != null) {
                        var tagIconLeft = cache.lines().get(messageIndex).width + 4;
                        var tagIconTop = lineDrawTop + font.lineHeight;
                        var drawTop = tagIconTop - guiMessageTag.icon().height - 1;
                        guiMessageTag.icon().draw(graphics, tagIconLeft, drawTop);
                    }
                }

                // If fading we draw the text dynamically, otherwise we draw whatever layer it needs to be on
                if (cache.fading().contains(messageIndex) ? dynamic : cache.lines().get(messageIndex).shouldDraw(dynamic)) {
                    pose.translate(0.0f, 0.0f, 50.0f);
                    cache.lines().get(messageIndex).draw(graphics, font, 0, lineDrawTop, 0xFFFFFF + (alpha << 24));
                }
                pose.popPose();
            }

            // Always draw the queue message and scroll bar on the background layer
            if (!dynamic) {
                // Draw the queue message
                var queueSize = minecraft.getChatListener().queueSize();
                if (queueSize > 0L) {
                    var queueColor = (int) (128.0 * opacity);
                    var queueBackgroundColor = (int) (255.0 * backgroundOpacity);
                    pose.pushPose();
                    pose.translate(0.0f, scaledHeight, 50.0f);
                    graphics.fill(-2, 0, scaledWidth + 4, 9, queueBackgroundColor << 24);
                    pose.translate(0.0f, 0.0f, 50.0f);
                    cache.queueSize().draw(graphics, font, 0, 1, 0xFFFFFF + (queueColor << 24));
                    pose.popPose();
                }

                // Draw the scroll bar
                if (focused) {
                    var drawnFocusedTop = messageCount * lineHeight;
                    var theoryFocusedTop = shownLineCount * lineHeight;
                    var scrollHeight = cache.chatScrollbarPos() * theoryFocusedTop / messageCount - scaledHeight;
                    alpha = theoryFocusedTop * theoryFocusedTop / drawnFocusedTop;

                    if (drawnFocusedTop != theoryFocusedTop) {
                        backgroundAlpha = scrollHeight > 0 ? 170 : 96;
                        int z = cache.newMessageSinceScroll() ? 0xCC3333 : 0x3333AA;
                        lineBottom = scaledWidth + 4;
                        graphics.fill(lineBottom, -scrollHeight, lineBottom + 2, -scrollHeight - alpha, 100, z + (backgroundAlpha << 24));
                        graphics.fill(lineBottom + 2, -scrollHeight, lineBottom + 1, -scrollHeight - alpha, 100, 0xCCCCCC + (backgroundAlpha << 24));
                    }
                }
            }
        } finally {
            pose.popPose();
        }
    }
}
