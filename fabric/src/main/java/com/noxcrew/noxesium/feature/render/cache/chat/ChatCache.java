package com.noxcrew.noxesium.feature.render.cache.chat;

import com.noxcrew.noxesium.feature.render.cache.ElementCache;
import com.noxcrew.noxesium.feature.render.font.BakedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;

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

    @Override
    protected boolean isEmpty(ChatInformation cache) {
        return Minecraft.getInstance().gui.getChat().isChatHidden() || cache.trimmedMessages().isEmpty();
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
        var queueSize = minecraft.getChatListener().queueSize();
        var lines = new ArrayList<BakedComponent>();
        var fading = new ArrayList<Integer>();
        var focused = chatOverlay.isChatFocused();
        var messages = new ArrayList<>(chatOverlay.trimmedMessages);

        var index = 0;
        for (var line : messages) {
            index++;
            var baked = new BakedComponent(line.content(), font);
            lines.add(baked);

            // Determine if this line is fading out!
            var ticksSinceMessageSend = lastTick - line.addedTime();
            var timeFactor = focused ? 1.0 : ChatComponent.getTimeFactor(ticksSinceMessageSend);
            if (timeFactor < 1.0) {
                fading.add(index - 1);
            }
        }

        return new ChatInformation(
                messages,
                chatOverlay.chatScrollbarPos,
                chatOverlay.newMessageSinceScroll,
                focused,
                new BakedComponent(Component.translatable("chat.queue", queueSize), font),
                lines,
                fading
        );
    }

    @Override
    protected void render(GuiGraphics graphics, ChatInformation cache, Minecraft minecraft, int screenWidth, int screenHeight, Font font, float partialTicks, boolean buffered) {
        var chatOverlay = minecraft.gui.getChat();
        var clearCache = false;
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
            var highlightedMessage = chatOverlay.getMessageEndIndexAt(chatOverlay.screenToChatX(mouseX), chatOverlay.screenToChatY(mouseY));
            var opacity = minecraft.options.chatOpacity().get() * (double) 0.9f + (double) 0.1f;
            double backgroundOpacity = minecraft.options.textBackgroundOpacity().get();
            double lineSpacing = minecraft.options.chatLineSpacing().get();
            var lineHeight = chatOverlay.getLineHeight();
            var lineSize = (int) Math.round(-8.0 * (lineSpacing + 1.0) + 4.0 * lineSpacing);
            var shownLineCount = 0;

            // Draw all the lines in the chat
            for (int currentLine = 0; currentLine + cache.chatScrollbarPos() < messageCount && currentLine < linesPerPage; ++currentLine) {
                var messageIndex = currentLine + cache.chatScrollbarPos();

                var line = cache.trimmedMessages().get(messageIndex);
                if (line == null || (ticksSinceMessageSend = lastTick - line.addedTime()) >= 200 && !focused) continue;

                var timeFactor = focused ? 1.0 : ChatComponent.getTimeFactor(ticksSinceMessageSend);
                alpha = (int) (255.0 * timeFactor * opacity);
                backgroundAlpha = (int) (255.0 * timeFactor * backgroundOpacity);
                ++shownLineCount;
                if (alpha <= 3) continue;

                // Clear the cache next tick if we need to start a fade.
                if (!buffered && timeFactor < 1.0 && !cache.fading().contains(messageIndex)) {
                    clearCache = true;
                }

                if (buffered || cache.lines().get(messageIndex).needsCustomReRendering || cache.fading().contains(messageIndex)) {
                    lineBottom = scaledHeight - currentLine * lineHeight;
                    var lineDrawTop = lineBottom + lineSize;

                    pose.pushPose();
                    pose.translate(0.0f, 0.0f, 50.0f);
                    var guiMessageTag = line.tag();
                    if (cache.fading().contains(messageIndex) != buffered) {
                        graphics.fill(-4, lineBottom - lineHeight, scaledWidth + 4 + 4, lineBottom, backgroundAlpha << 24);
                        if (guiMessageTag != null) {
                            var tagColor = guiMessageTag.indicatorColor() | alpha << 24;
                            graphics.fill(-4, lineBottom - lineHeight, -2, lineBottom, tagColor);
                        }
                    }
                    if (!buffered) {
                        if (guiMessageTag != null && messageIndex == highlightedMessage && guiMessageTag.icon() != null) {
                            var tagIconLeft = cache.lines().get(messageIndex).width + 4;
                            var tagIconTop = lineDrawTop + font.lineHeight;
                            var drawTop = tagIconTop - guiMessageTag.icon().height - 1;
                            guiMessageTag.icon().draw(graphics, tagIconLeft, drawTop);
                        }
                    }
                    if (!buffered || (!cache.fading().contains(messageIndex) && !cache.lines().get(messageIndex).needsCustomReRendering)) {
                        pose.translate(0.0f, 0.0f, 50.0f);
                        cache.lines().get(messageIndex).draw(graphics, font, 0, lineDrawTop, 0xFFFFFF + (alpha << 24));
                    }
                    pose.popPose();
                }
            }

            if (buffered) {
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
            if (clearCache) {
                clearCache();
            }
        }
    }
}
