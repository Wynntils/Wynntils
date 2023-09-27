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
    private Button filterAllButton;
    private float backgroundHeight;
    private float backgroundWidth;
    private float backgroundX;
    private float backgroundY;
    private float dividedHeight;
    private float dividedWidth;
    private float scrollButtonHeight;
    private float scrollButtonRenderY;
    private int bottomDisplayedIndex;
    private int iconButtonSize;
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

        scrollButtonHeight = (dividedWidth / Texture.SCROLL_BUTTON.width()) * Texture.SCROLL_BUTTON.height();

        int filterButtonWidth = (int) (dividedWidth * 10);

        // region filter all button
        filterAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.iconFilter.filterAll"), (button) -> {
                            icons.replaceAll((key, value) -> true);
                            populateIcons();
                        })
                .pos((int) backgroundX, (int) (dividedHeight * 3))
                .size(filterButtonWidth, 20)
                .build();

        filterAllButton.active = icons.containsValue(false);

        this.addRenderableWidget(filterAllButton);
        // endregion

        // region done button
        this.addRenderableWidget(
                new Button.Builder(Component.translatable("screens.wynntils.iconFilter.done"), (button) -> McUtils.mc()
                                .setScreen(previousScreen))
                        .pos((int) (dividedWidth * 44), (int) (dividedHeight * 3))
                        .size(filterButtonWidth, 20)
                        .build());
        // endregion

        usedIcons = new ArrayList<>(icons.keySet());

        bottomDisplayedIndex = Math.min(MAX_ICONS_TO_DISPLAY, usedIcons.size() - 1);

        populateIcons();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        renderScrollButton(poseStack);
        super.doRender(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
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
        icons.put(icon, !icons.get(icon));

        previousScreen.setFilteredIcons(icons);

        filterAllButton.active = icons.containsValue(false);

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
            float scrollButtonRenderX = (int) (dividedWidth * 52);

            if (mouseX >= scrollButtonRenderX
                    && mouseX <= scrollButtonRenderX + dividedWidth
                    && mouseY >= scrollButtonRenderY
                    && mouseY <= scrollButtonRenderY + scrollButtonHeight) {
                draggingScroll = true;
            }
        }

        // Returning super.doMouseClicked(mouseX, mouseY, button) causes ConcurrentModificationException
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setScrollOffset((int) delta);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int newValue = (int) MathUtils.map(
                (float) mouseY,
                (int) (dividedHeight * 10),
                (int) (dividedHeight * 52),
                0,
                Math.max(0, usedIcons.size() - MAX_ICONS_TO_DISPLAY));

        setScrollOffset(-newValue + scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderScrollButton(PoseStack poseStack) {
        if (usedIcons.size() <= MAX_ICONS_TO_DISPLAY) return;

        scrollButtonRenderY = MathUtils.map(
                scrollOffset, 0, usedIcons.size() - MAX_ICONS_TO_DISPLAY, (int) (dividedHeight * 10), (int)
                        (dividedHeight * 51));

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                (int) (dividedWidth * 52),
                scrollButtonRenderY,
                0,
                dividedWidth,
                scrollButtonHeight,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void setScrollOffset(int delta) {
        scrollOffset = MathUtils.clamp(scrollOffset - delta, 0, Math.max(0, usedIcons.size() - MAX_ICONS_TO_DISPLAY));

        populateIcons();
    }

    private void populateIcons() {
        for (AbstractWidget widget : iconFilterWidgets) {
            this.removeWidget(widget);
        }

        this.iconFilterWidgets.clear();

        int row = (int) ((int) (dividedHeight * (HEADER_HEIGHT + 2)) + (dividedHeight / 2f));
        int xPos = (int) (dividedWidth * 13);

        for (int i = 0; i < MAX_ICONS_TO_DISPLAY; i++) {
            bottomDisplayedIndex = i + (scrollOffset * ICONS_PER_ROW);

            if (bottomDisplayedIndex > usedIcons.size() - 1) {
                break;
            }

            Texture icon = usedIcons.get(bottomDisplayedIndex);

            IconFilterWidget filterWidget = new IconFilterWidget(
                    xPos, row, iconButtonSize, iconButtonSize, icon, this, icons.getOrDefault(icon, false));

            iconFilterWidgets.add(filterWidget);

            this.addRenderableWidget(filterWidget);

            if (xPos + (iconButtonSize * 2) > (int) (dividedWidth * 50)) {
                row += iconButtonSize;
                xPos = (int) (dividedWidth * 13);
            } else {
                xPos += iconButtonSize;
            }
        }
    }
}
