package com.noxcrew.noxesium.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noxcrew.noxesium.NoxesiumMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.joml.Matrix4f;

/**
 * Renders heldheld maps as UI elements.
 */
public class CustomMapUiWidget implements LayeredDraw.Layer {

    private static final RenderType MAP_BACKGROUND =
            RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD =
            RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
    private final MapRenderState mapRenderState = new MapRenderState();

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();

        // Check that the main GUI is not hidden
        if (minecraft.options.hideGui) return;

        // Check that the player exists
        if (minecraft.player == null) return;

        // Check that this layer is enabled
        if (!NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi()) return;

        var font = minecraft.font;
        var offset = NoxesiumMod.getPlatform().isModLoaded("toggle-sprint-display") ? font.lineHeight : 0;
        var pose = guiGraphics.pose();
        var mainArm = minecraft.player.getMainArm();
        for (var arm : HumanoidArm.values()) {
            if (hasMapItem(minecraft.player, arm)) {
                renderMap(
                        minecraft,
                        guiGraphics,
                        deltaTracker,
                        pose,
                        arm,
                        minecraft.player.getItemInHand(
                                mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND),
                        offset);
            }
        }
    }

    /**
     * Returns whether a map is being held in the given arm.
     */
    private static boolean hasMapItem(LocalPlayer player, HumanoidArm arm) {
        var mainArm = player.getMainArm();
        var hand = mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var item = player.getItemInHand(hand);
        var otherHand = player.getItemInHand(
                hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return item.is(Items.FILLED_MAP) && (hand == InteractionHand.OFF_HAND || !otherHand.isEmpty());
    }

    /**
     * Renders a map to the UI.
     */
    private void renderMap(
            Minecraft minecraft,
            GuiGraphics graphics,
            DeltaTracker deltaTracker,
            PoseStack pose,
            HumanoidArm arm,
            ItemStack item,
            int offset) {
        pose.pushPose();
        var scale = 1f
                / ((float) minecraft.getWindow().getGuiScale())
                * 4f
                * ((float) NoxesiumMod.getInstance().getConfig().mapUiSize);
        var setting = NoxesiumMod.getInstance().getConfig().mapUiLocation;
        var bottom = setting.isBottom();
        var flipped = setting.isFlipped();

        if ((arm == HumanoidArm.RIGHT) != flipped) {
            if (bottom) {
                // Translate it to be at the bottom right of the GUI
                pose.translate(graphics.guiWidth() - (148f * scale), graphics.guiHeight() - (148f * scale), 0f);
            } else {
                // Only translate to the right of the GUI
                pose.translate(graphics.guiWidth() - (148f * scale), 0f, 0f);
            }
        } else {
            if (bottom) {
                // Translate it to be at the bottom of the GUI
                pose.translate(0f, graphics.guiHeight() - (148f * scale), 0f);
            } else {
                // Only add the offset on the left top side!
                pose.translate(0f, offset, 0f);
            }
        }

        pose.scale(1f * scale, 1f * scale, -1f);
        pose.translate(10f, 10f, 0f);
        var mapId = item.get(DataComponents.MAP_ID);
        var mapitemsaveddata = MapItem.getSavedData(mapId, minecraft.level);
        graphics.drawSpecial(bufferSource -> {
            VertexConsumer vertexconsumer =
                    bufferSource.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
            Matrix4f matrix4f = pose.last().pose();
            var light = minecraft
                    .getEntityRenderDispatcher()
                    .getPackedLightCoords(minecraft.player, deltaTracker.getGameTimeDeltaPartialTick(true));
            vertexconsumer
                    .addVertex(matrix4f, -7.0F, 135.0F, 0.0F)
                    .setColor(-1)
                    .setUv(0.0F, 1.0F)
                    .setLight(light);
            vertexconsumer
                    .addVertex(matrix4f, 135.0F, 135.0F, 0.0F)
                    .setColor(-1)
                    .setUv(1.0F, 1.0F)
                    .setLight(light);
            vertexconsumer
                    .addVertex(matrix4f, 135.0F, -7.0F, 0.0F)
                    .setColor(-1)
                    .setUv(1.0F, 0.0F)
                    .setLight(light);
            vertexconsumer
                    .addVertex(matrix4f, -7.0F, -7.0F, 0.0F)
                    .setColor(-1)
                    .setUv(0.0F, 0.0F)
                    .setLight(light);
            if (mapitemsaveddata != null) {
                var mapRenderer = minecraft.getMapRenderer();
                mapRenderer.extractRenderState(mapId, mapitemsaveddata, mapRenderState);
                mapRenderer.render(mapRenderState, pose, bufferSource, false, light);
            }
        });
        pose.popPose();
    }
}
