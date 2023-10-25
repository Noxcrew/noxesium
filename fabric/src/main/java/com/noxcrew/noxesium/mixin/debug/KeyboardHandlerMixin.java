package com.noxcrew.noxesium.mixin.debug;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Adds the F3 + Z debug hotkey to dump a file with commands to create the current UI.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Shadow
    private long debugCrashKeyTime;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void debugFeedbackTranslated(String string, Object... objects);

    @Redirect(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    public void redirect(ChatComponent instance, Component component) {
        if (component.getContents() instanceof TranslatableContents translatableContents) {
            if (translatableContents.getKey().equals("debug.pause.help")) {
                instance.addMessage(Component.translatable("debug.dump_ui.help"));
            }
        }
        instance.addMessage(component);
    }

    @Inject(method = "handleDebugKeys", at = @At("TAIL"), cancellable = true)
    public void injected(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return;
        }

        if (keyCode == InputConstants.KEY_Z) {
            cir.setReturnValue(true);

            try {
                var name = "ui_" + System.currentTimeMillis();
                var debugFolder = this.minecraft.gameDirectory.toPath().resolve("debug");
                Files.createDirectories(debugFolder);
                var path = debugFolder.resolve(name + ".mcfunction").toAbsolutePath();

                var scoreboard = this.minecraft.player.getScoreboard();
                var objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);

                var text = new StringBuilder();
                if (objective != null) {
                    text.append("scoreboard objectives add " + name + " dummy " + Component.Serializer.toJson(objective.getDisplayName())).append("\n");
                    text.append("scoreboard objectives setdisplay sidebar " + name).append("\n");

                    var scores = scoreboard.getPlayerScores(objective).stream()
                            .sorted(Comparator.comparing(f -> -f.getScore()))
                            .limit(15)
                            .collect(Collectors.toList());
                    for (var score : scores) {
                        var team = System.currentTimeMillis() + "_" + score.getScore();
                        text.append("scoreboard players set " + score.getOwner() + " " + name + " " + score.getScore()).append("\n");

                        var ownerTeam = scoreboard.getPlayersTeam(score.getOwner());
                        if (ownerTeam != null) {
                            text.append("team add " + team).append("\n");
                            text.append("team join " + team + " " + score.getOwner()).append("\n");
                            text.append("team modify " + team + " prefix " + Component.Serializer.toJson(ownerTeam.getPlayerPrefix())).append("\n");
                            text.append("team modify " + team + " suffix " + Component.Serializer.toJson(ownerTeam.getPlayerSuffix())).append("\n");
                        }
                    }
                }

                Files.writeString(path, text.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                Component component = Component.literal(path.toString()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toFile().toString())));
                this.debugFeedbackTranslated("debug.dump_ui.success", component);
            } catch (Exception x) {
                x.printStackTrace();
                this.debugFeedbackTranslated("debug.dump_ui.error");
            }
        }
    }
}
