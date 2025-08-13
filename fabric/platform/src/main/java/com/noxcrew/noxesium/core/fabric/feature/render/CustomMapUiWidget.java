package com.noxcrew.noxesium.core.fabric.feature.render;

import com.noxcrew.noxesium.core.fabric.NoxesiumMod;
import net.fabricmc.loader.api.FabricLoader;
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
        if (!NoxesiumMod.getInstance().getConfig().shouldRenderMapsInUi()) return;

        var font = minecraft.font;
        var offset = FabricLoader.getInstance().isModLoaded("toggle-sprint-display") ? font.lineHeight : 0;
        var mainArm = minecraft.player.getMainArm();
        for (var arm : HumanoidArm.values()) {
            if (hasMapItem(minecraft.player, arm)) {
                guiGraphics.nextStratum();
                var pose = guiGraphics.pose();
                pose.pushMatrix();
                renderMap(
                        minecraft,
                        guiGraphics,
                        deltaTracker,
                        pose,
                        arm,
                        minecraft.player.getItemInHand(
                                mainArm == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND),
                        offset);
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
            DeltaTracker deltaTracker,
            Matrix3x2fStack pose,
            HumanoidArm arm,
            ItemStack item,
            int offset) {
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
                pose.translate(graphics.guiWidth() - (148f * scale), graphics.guiHeight() - (148f * scale));
            } else {
                // Only translate to the right of the GUI
                pose.translate(graphics.guiWidth() - (148f * scale), 0f);
            }
        } else {
            if (bottom) {
                // Translate it to be at the bottom of the GUI
                pose.translate(0f, graphics.guiHeight() - (148f * scale));
            } else {
                // Only add the offset on the left top side!
                pose.translate(0f, offset);
            }
        }

        pose.scale(1f * scale, 1f * scale);
        pose.translate(10f, 10f);
        var mapId = item.get(DataComponents.MAP_ID);
        var mapitemsaveddata = MapItem.getSavedData(mapId, minecraft.level);

        // Draw the background
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD,
                -7,
                -7,
                142,
                142);

        if (mapitemsaveddata != null) {
            // Draw the contents of the map itself, including all decorations!
            var mapRenderer = minecraft.getMapRenderer();
            mapRenderer.extractRenderState(mapId, mapitemsaveddata, mapRenderState);
            mapRenderState.decorations.forEach(it -> it.renderOnFrame = true);
            graphics.submitMapRenderState(mapRenderState);
        }
    }
}
