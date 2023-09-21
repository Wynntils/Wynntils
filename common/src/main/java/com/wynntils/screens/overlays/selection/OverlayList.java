/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.selection;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;

public class OverlayList extends ContainerObjectSelectionList<OverlayEntry> {
    private static final int ITEM_HEIGHT = 25;
    private static final int ROW_WIDTH = 161;

    private static final List<Component> HELP_TOOLTIP_LINES = List.of(
            Component.literal("Left click on the overlay to edit it."),
            Component.literal("Right click on the overlay to disable/enable it."));

    private static final List<Component> DISABLED_PARENT_TOOLTIP_LINES = List.of(
            Component.literal("This overlay's parent feature is disabled.").withStyle(ChatFormatting.RED),
            Component.literal("Enable the feature to edit this overlay.").withStyle(ChatFormatting.RED));

    public OverlayList(OverlaySelectionScreen screen) {
        super(
                McUtils.mc(),
                screen.width,
                screen.height,
                screen.height / 10 + 15,
                screen.height / 10 + Texture.OVERLAY_SELECTION_GUI.height() - 15,
                ITEM_HEIGHT);

        List<Overlay> overlays = Managers.Overlay.getOverlays().stream()
                .sorted(Overlay::compareTo)
                .toList();

        for (Overlay overlay : overlays) {
            this.addEntry(new OverlayEntry(overlay));
        }

        this.setRenderBackground(false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        OverlayEntry hovered = this.getHovered();

        if (hovered != null) {
            if (!hovered.getOverlay().isParentEnabled()) {
                List<Component> helpModified = new ArrayList<>(DISABLED_PARENT_TOOLTIP_LINES);
                helpModified.add(Component.literal(""));
                helpModified.add(Component.literal("Feature: "
                        + Managers.Overlay.getOverlayParent(hovered.getOverlay())
                                .getTranslatedName()));

                McUtils.mc()
                        .screen
                        .setTooltipForNextRenderPass(Lists.transform(helpModified, Component::getVisualOrderText));
            } else {
                McUtils.mc()
                        .screen
                        .setTooltipForNextRenderPass(
                                Lists.transform(HELP_TOOLTIP_LINES, Component::getVisualOrderText));
            }
        }
    }

    @Override
    protected void renderList(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        int itemCount = this.getItemCount();

        int renderedCount = 0;

        this.hovered = null;

        for (int i = 0; i < itemCount; i++) {
            int top = this.y0 + 1 + renderedCount * this.itemHeight + this.headerHeight;
            int bottom = top + this.itemHeight;
            if (getRowTop(i) < this.y0 || bottom > this.y1) continue;

            OverlayEntry entry = this.getEntry(i);

            if (top + 1 <= mouseY
                    && top + 1 + this.itemHeight >= mouseY
                    && this.getRowLeft() <= mouseX
                    && this.getRowLeft() + this.getRowWidth() >= mouseX) {
                this.hovered = entry;
            }

            entry.render(
                    guiGraphics,
                    i,
                    top + 1,
                    this.getRowLeft(),
                    this.getRowWidth(),
                    this.itemHeight,
                    mouseX,
                    mouseY,
                    Objects.equals(this.getHovered(), entry),
                    partialTick);

            renderedCount++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);

        return this.hovered != null && this.hovered.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() - 43;
    }

    @Override
    protected int getRowTop(int index) {
        return this.y0 - (int) this.getScrollAmount() + index * this.itemHeight + this.headerHeight + 1;
    }

    @Override
    public int getRowWidth() {
        return ROW_WIDTH;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + this.width / 2 - this.getRowWidth() / 2 - 10;
    }

    public static int getItemHeight() {
        return ITEM_HEIGHT;
    }
}
