package com.noxcrew.noxesium.feature.ui.wrapper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.config.MapLocation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

/**
 * Renders heldheld maps as UI elements.
 */
public class MapUiWrapper extends ElementWrapper {

    private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(
            ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png")
    );

    /**
     * Returns whether a map is being held in the given arm.
     */
    private static boolean hasMapItem(Minecraft minecraft, HumanoidArm arm) {
        var mainArm = minecraft.player.getMainArm();
        var hand = mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var item = minecraft.player.getItemInHand(hand);
        var otherHand = minecraft.player.getItemInHand(
                hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND
        );
        return item.is(Items.FILLED_MAP) && (hand == InteractionHand.OFF_HAND || !otherHand.isEmpty());
    }

    public MapUiWrapper() {
        // Update every tick for the map contents
        registerVariable("tick", (minecraft, partialTicks) -> RenderSystem.getShaderGameTime());

        // Update as the setting changes
        registerVariable("main_hand", (minecraft, partialTicks) -> minecraft.options.mainHand());
        registerVariable("size", (minecraft, partialTicks) -> NoxesiumMod.getInstance().getConfig().mapUiSize);
        registerVariable("location", (minecraft, partialTicks) -> NoxesiumMod.getInstance().getConfig().mapUiLocation);
    }

    @Override
    protected void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight, Font font, DeltaTracker deltaTracker) {
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? font.lineHeight : 0;
        var pose = graphics.pose();
        var mainArm = minecraft.player.getMainArm();
        for (var arm : HumanoidArm.values()) {
            if (hasMapItem(minecraft, arm)) {
                renderMap(minecraft, graphics, deltaTracker, pose, arm, minecraft.player.getItemInHand(
                        mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
                ), offset);
            }
        }
    }

    private void renderMap(Minecraft minecraft, GuiGraphics graphics, DeltaTracker deltaTracker, PoseStack pose, HumanoidArm arm, ItemStack item, int offset) {
        pose.pushPose();
        var scale = 1f / ((float) minecraft.getWindow().getGuiScale()) * 4f * ((float) NoxesiumMod.getInstance().getConfig().mapUiSize);
        var bottom = NoxesiumMod.getInstance().getConfig().mapUiLocation == MapLocation.BOTTOM;
        if (arm == HumanoidArm.RIGHT) {
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
        MapId mapid = item.get(DataComponents.MAP_ID);
        MapItemSavedData mapitemsaveddata = MapItem.getSavedData(mapid, minecraft.level);
        var bufferSource = graphics.bufferSource();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = pose.last().pose();
        var light = minecraft.getEntityRenderDispatcher().getPackedLightCoords(minecraft.player, deltaTracker.getGameTimeDeltaPartialTick(true));
        vertexconsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(light);
        vertexconsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(light);
        vertexconsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(light);
        vertexconsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(light);
        if (mapitemsaveddata != null) {
            minecraft.gameRenderer.getMapRenderer().render(pose, bufferSource, mapid, mapitemsaveddata, false, light);
        }
        bufferSource.endBatch();
        pose.popPose();
    }
}
