/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.maps.widgets.IconFilterWidget;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class IconFilterScreen extends WynntilsScreen {
    private static final float GRID_DIVISIONS = 64.0f;
    private static final int HEADER_HEIGHT = 12;
    private static final int ICONS_PER_ROW = 9;
    private static final int MAX_ICONS_TO_DISPLAY = 45;

    private final List<AbstractWidget> iconFilterWidgets = new ArrayList<>();
    private final Map<Texture, Boolean> icons;
    private final PoiManagementScreen previousScreen;

    private boolean draggingScroll = false;
    private Button includeAllButton;
    private Button excludeAllButton;
    private float backgroundHeight;
    private float backgroundWidth;
    private float backgroundX;
    private float backgroundY;
    private float dividedHeight;
    private float dividedWidth;
    private float scrollButtonHeight;
    private float scrollButtonRenderX;
    private float scrollButtonRenderY;
    private int iconButtonSize;
    private int scrollAreaHeight;
    private int scrollOffset = 0;
    private List<Texture> usedIcons;

    private IconFilterScreen(PoiManagementScreen previousScreen, Map<Texture, Boolean> icons) {
        super(Component.literal("Icon Filter Screen"));

        this.previousScreen = previousScreen;
        this.icons = icons;
    }

    public static Screen create(PoiManagementScreen previousScreen, Map<Texture, Boolean> icons) {
        return new IconFilterScreen(previousScreen, icons);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;
        iconButtonSize = (int) (dividedWidth * 4);
        backgroundX = dividedWidth * 10;
        backgroundWidth = dividedWidth * 44;
        backgroundY = dividedHeight * 7;
        backgroundHeight = dividedHeight * 50;

        // Height of the scroll button relative to the scaled width
        scrollButtonHeight = ((dividedWidth / 2) / Texture.SCROLL_BUTTON.width()) * Texture.SCROLL_BUTTON.height();

        // How far the scrollbar should be able to go
        scrollAreaHeight = (int) (backgroundHeight - scrollButtonHeight) - (int) (dividedHeight * 4);

        // X position of the scroll button
        scrollButtonRenderX = (int) (dividedWidth * 52) + (dividedWidth / 4);

        int filterButtonWidth = (int) (dividedWidth * 10);

        // region include/exclude all buttons
        includeAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.iconFilter.includeAll"), (button) -> {
                            icons.replaceAll((key, value) -> true);
                            button.active = false;
                            excludeAllButton.active = true;
                            populateIcons();
                        })
                .pos((int) backgroundX, (int) (dividedHeight * 3))
                .size(filterButtonWidth, 20)
                .build();

        includeAllButton.active = icons.containsValue(false);

        this.addRenderableWidget(includeAllButton);

        excludeAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.iconFilter.excludeAll"), (button) -> {
                            icons.replaceAll((key, value) -> false);
                            button.active = false;
                            includeAllButton.active = true;
                            populateIcons();
                        })
                .pos((int) backgroundX + filterButtonWidth + 5, (int) (dividedHeight * 3))
                .size(filterButtonWidth, 20)
                .build();

        excludeAllButton.active = icons.containsValue(true);

        this.addRenderableWidget(excludeAllButton);
        // endregion

        // region done button
        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.iconFilter.done"), (button) -> McUtils.mc()
                                .setScreen(previousScreen))
                        .pos((int) (dividedWidth * 44), (int) (dividedHeight * 3))
                        .size(filterButtonWidth, 20)
                        .build());
        // endregion

        // The icons that will be displayed
        usedIcons = new ArrayList<>(icons.keySet());

        populateIcons();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        renderScrollButton(guiGraphics.pose());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics.pose(),
                Texture.WAYPOINT_MANAGER_BACKGROUND.resource(),
                backgroundX,
                backgroundY,
                0,
                backgroundWidth,
                backgroundHeight,
                Texture.WAYPOINT_MANAGER_BACKGROUND.width(),
                Texture.WAYPOINT_MANAGER_BACKGROUND.height());
    }

    public void toggleIcon(Texture icon) {
        // Toggle the icon
        icons.put(icon, !icons.get(icon));

        previousScreen.setFilteredIcons(icons);

        // Only have the buttons active if they will do anything.
        // Eg. Include all will set all to true, so deactive it if no falses
        includeAllButton.active = icons.containsValue(false);
        excludeAllButton.active = icons.containsValue(true);

        populateIcons();
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return child.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (!draggingScroll && (usedIcons.size() > MAX_ICONS_TO_DISPLAY)) {
            if (mouseX >= scrollButtonRenderX
                    && mouseX <= scrollButtonRenderX + (dividedWidth / 2)
                    && mouseY >= scrollButtonRenderY
                    && mouseY <= scrollButtonRenderY + scrollButtonHeight) {
                draggingScroll = true;
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double scrollValue = -Math.signum(deltaY);
        scroll((int) scrollValue);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int renderY = (int) ((this.height - backgroundHeight) / 2 + (int) (dividedHeight * 3));
        int scrollAreaStartY = renderY + 12;

        int newValue = (int) MathUtils.map(
                (float) mouseY,
                scrollAreaStartY,
                scrollAreaStartY + (int) (dividedHeight * 40),
                0,
                Math.max(0, usedIcons.size() - MAX_ICONS_TO_DISPLAY));

        scroll(newValue - scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderScrollButton(PoseStack poseStack) {
        // Don't render the scroll button if it will not be useable
        if (usedIcons.size() <= MAX_ICONS_TO_DISPLAY) return;

        // Calculate where the scroll button should be on the Y axis
        scrollButtonRenderY = (this.height - backgroundHeight) / 2
                + (int) (dividedHeight * 3)
                + MathUtils.map(scrollOffset, 0, usedIcons.size() - MAX_ICONS_TO_DISPLAY, 0, scrollAreaHeight);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                scrollButtonRenderX,
                scrollButtonRenderY,
                1,
                (dividedWidth / 2),
                scrollButtonHeight,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void scroll(int delta) {
        // Calculate how many rows should be scrolled past
        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, usedIcons.size() - MAX_ICONS_TO_DISPLAY));

        populateIcons();
    }

    private void populateIcons() {
        for (AbstractWidget widget : iconFilterWidgets) {
            this.removeWidget(widget);
        }

        this.iconFilterWidgets.clear();

        // Starting Y position for the icons
        int row = (int) ((int) (dividedHeight * (HEADER_HEIGHT + 2)) + (dividedHeight / 2f));
        // Starting X position for the row
        int xPos = (int) (dividedWidth * 13);

        int currentIcon;

        // Render icon widgets
        for (int i = 0; i < MAX_ICONS_TO_DISPLAY; i++) {
            // Get the icon to render
            currentIcon = i + (scrollOffset * ICONS_PER_ROW);

            // If there are less icons than MAX_ICONS_TO_DISPLAY, make sure we don't try and get a icon out of range
            if (currentIcon > usedIcons.size() - 1) {
                break;
            }

            Texture icon = usedIcons.get(currentIcon);

            IconFilterWidget filterWidget = new IconFilterWidget(
                    xPos, row, iconButtonSize, iconButtonSize, icon, this, icons.getOrDefault(icon, false));

            iconFilterWidgets.add(filterWidget);

            this.addRenderableWidget(filterWidget);

            // Calculate if we can place another icon button on the same row or not
            if ((i + 1) % ICONS_PER_ROW == 0) {
                row += iconButtonSize;
                xPos = (int) (dividedWidth * 13);
            } else {
                xPos += iconButtonSize;
            }
        }
    }
}
