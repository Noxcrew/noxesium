package com.noxcrew.noxesium.core.fabric.config;

import static net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.TAB_HEADER_BACKGROUND;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.jetbrains.annotations.Nullable;

/**
 * The custom settings screen used by Noxesium which opens when clicking on Noxesium in Mod Menu or by pressing F3+W.
 */
public class NoxesiumSettingsScreen extends Screen {
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private final Screen lastScreen;

    @Nullable
    private TabNavigationBar tabNavigationBar;

    public NoxesiumSettingsScreen(Screen lastScreen) {
        super(Component.translatable("noxesium.options.screen.noxesium"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
                .addTabs(new GuiTab(), new DeveloperTab())
                .build();
        this.addRenderableWidget(this.tabNavigationBar);

        LinearLayout linearlayout =
                this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .build());
        this.layout.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
            var bottom = this.tabNavigationBar.getRectangle().bottom();
            var screenrectangle =
                    new ScreenRectangle(0, bottom, this.width, this.height - this.layout.getFooterHeight() - bottom);
            this.tabManager.setTabArea(screenrectangle);
            this.layout.setHeaderHeight(bottom);
            this.layout.arrangeElements();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int p_283640_, int p_281243_, float p_282743_) {
        super.render(guiGraphics, p_283640_, p_281243_, p_282743_);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Screen.FOOTER_SEPARATOR,
                0,
                this.height - this.layout.getFooterHeight() - 2,
                0.0F,
                0.0F,
                this.width,
                2,
                32,
                2);
    }

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TAB_HEADER_BACKGROUND,
                0,
                0,
                0.0F,
                0.0F,
                this.width,
                this.layout.getHeaderHeight(),
                16,
                16);
        this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    /**
     * Hook for extending the developer tab.
     */
    public void addToDeveloperTab(GridLayout.RowHelper rowHelper) {}

    /**
     * Creates a new widget for the given option.
     */
    public static AbstractWidget createWidget(OptionInstance<?> option) {
        return option.createButton(Minecraft.getInstance().options);
    }

    /**
     * Creates a new widget for the given option.
     */
    public static AbstractWidget createWidget(
            OptionInstance<?> option, Set<OptionInstance.OptionInstanceSliderButton<?>> set) {
        var widget = (OptionInstance.OptionInstanceSliderButton<?>) createWidget(option);
        set.add(widget);
        return widget;
    }

    class GuiTab extends GridLayoutTab {
        GuiTab() {
            super(Component.translatable("noxesium.options.header.gui_options"));

            var rowHelper = layout.columnSpacing(3).rowSpacing(3).createRowHelper(2);

            var positionWidgets = new HashSet<OptionInstance.OptionInstanceSliderButton<?>>();
            var zeroPositionWidgets = new HashSet<OptionInstance.OptionInstanceSliderButton<?>>();
            var widgets = new HashSet<OptionInstance.OptionInstanceSliderButton<?>>();

            rowHelper.addChild(createWidget(NoxesiumOptions.BOSS_BAR_POSITION, positionWidgets));
            rowHelper.addChild(createWidget(NoxesiumOptions.SCOREBOARD_POSITION, positionWidgets));
            rowHelper.addChild(createWidget(NoxesiumOptions.MAP_POSITION, zeroPositionWidgets));
            rowHelper.addChild(createWidget(VanillaOptions.MAP_UI_LOCATION));

            for (var scalar : NoxesiumOptions.GUI_SCALES.values()) {
                rowHelper.addChild(createWidget(scalar, widgets));
            }

            rowHelper.addChild(Button.builder(
                            Component.translatable("noxesium.options.reset_scales"),
                            button -> widgets.forEach(it -> it.setValue(0.495)))
                    .bounds(0, 0, 150, 20)
                    .build());

            rowHelper.addChild(Button.builder(Component.translatable("noxesium.options.reset_positions"), button -> {
                        positionWidgets.forEach(it -> it.setValue(0.5));
                        zeroPositionWidgets.forEach(it -> it.setValue(0.0));
                    })
                    .bounds(0, 0, 150, 20)
                    .build());
        }
    }

    class DeveloperTab extends GridLayoutTab {
        DeveloperTab() {
            super(Component.translatable("noxesium.options.header.developer_options"));

            var rowHelper = layout.columnSpacing(3).rowSpacing(3).createRowHelper(2);
            rowHelper.addChild(createWidget(NoxesiumOptions.GAME_TIME_OVERLAY));
            rowHelper.addChild(createWidget(NoxesiumOptions.DUMP_INCOMING_PACKETS));
            rowHelper.addChild(createWidget(NoxesiumOptions.DUMP_OUTGOING_PACKETS));
            rowHelper.addChild(createWidget(NoxesiumOptions.DEBUG_SCOREBOARD_TEAMS));
            rowHelper.addChild(createWidget(NoxesiumOptions.EXTENDED_PACKET_LOGGING));

            if (Minecraft.getInstance().player == null
                    || Minecraft.getInstance().player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                rowHelper.addChild(createWidget(NoxesiumOptions.QIB_SYSTEM_VISUAL_DEBUG));
                rowHelper.addChild(createWidget(NoxesiumOptions.SHOW_CULLING_HITBOXES));
            }

            addToDeveloperTab(rowHelper);
        }
    }
}
