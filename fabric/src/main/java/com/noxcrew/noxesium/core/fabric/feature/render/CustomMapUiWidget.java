package com.noxcrew.noxesium.core.fabric.feature.render;

import com.noxcrew.noxesium.api.client.GuiElement;
import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import com.noxcrew.noxesium.core.fabric.config.NoxesiumConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.joml.Matrix3x2fStack;

/**
 * Renders heldheld maps as UI elements.
 */
public class CustomMapUiWidget {

    public static final ResourceLocation MAP_BACKGROUND = ResourceLocation.withDefaultNamespace("map/map_background");
    public static final ResourceLocation MAP_BACKGROUND_CHECKERBOARD =
            ResourceLocation.withDefaultNamespace("map/map_background_checkerboard");

    private static final MapRenderState mapRenderState = new MapRenderState();

    /**
     * Renders the map UI widget.
     */
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();

        // Check that the main GUI is not hidden
        if (minecraft.options.hideGui) return;

        // Check that the player exists
        if (minecraft.player == null) return;

        // Check that this layer is enabled
        var config = NoxesiumMod.getInstance().getConfig();
        if (!config.shouldRenderMapsInUi()) return;

        var mainArm = minecraft.player.getMainArm();
        for (var arm : HumanoidArm.values()) {
            if (hasMapItem(minecraft.player, arm)) {
                guiGraphics.nextStratum();
                var pose = guiGraphics.pose();
                pose.pushMatrix();
                renderMap(
                        minecraft,
                        guiGraphics,
                        pose,
                        arm,
                        minecraft.player.getItemInHand(
                                mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND),
                        config);
                pose.popMatrix();
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
    private static void renderMap(
            Minecraft minecraft,
            GuiGraphics graphics,
            Matrix3x2fStack pose,
            HumanoidArm arm,
            ItemStack item,
            NoxesiumConfig config) {
        var setting = NoxesiumMod.getInstance().getConfig().mapUiLocation;
        var flipped = setting.isFlipped();

        var padding = 10f;
        var border = 7;
        var size = 128;
        var scale = (float) config.getScale(GuiElement.MAP);
        var scaledHeight = ((float) size) * scale;
        var offset = (float) ((config.mapPosition + 1f) * (graphics.guiHeight() - (2 * padding) - scaledHeight) / 2.0);

        if ((arm == HumanoidArm.RIGHT) != flipped) {
            pose.translate(graphics.guiWidth() - (size * scale), offset);
        } else {
            pose.translate(0f, offset);
        }

        pose.translate(padding, padding);
        pose.scale(scale, scale);

        var mapId = item.get(DataComponents.MAP_ID);
        var mapitemsaveddata = MapItem.getSavedData(mapId, minecraft.level);

        // Draw the background
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD,
                -border,
                -border,
                size + 2 * border,
                size + 2 * border);

        if (mapitemsaveddata != null) {
            var mapRenderer = minecraft.getMapRenderer();
            mapRenderer.extractRenderState(mapId, mapitemsaveddata, mapRenderState);
            mapRenderState.decorations.forEach(it -> it.renderOnFrame = true);
            graphics.submitMapRenderState(mapRenderState);
        }
    }
}
