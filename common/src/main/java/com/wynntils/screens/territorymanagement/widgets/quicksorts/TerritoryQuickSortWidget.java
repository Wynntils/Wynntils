/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quicksorts;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.itemfilter.type.SortDirection;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class TerritoryQuickSortWidget extends AbstractWidget {
    private final TerritoryManagementScreen screen;

    protected SortDirection sortDirection = null;

    protected TerritoryQuickSortWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean isHovered = this.isMouseOver(mouseX, mouseY);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(Component.literal(getSortName())
                                .withStyle(isHovered ? ChatFormatting.BOLD : ChatFormatting.RESET)
                                .append(" ")
                                .append(
                                        sortDirection == null
                                                ? "-"
                                                : sortDirection == SortDirection.ASCENDING ? "▲" : "▼")),
                        this.getX() + 5,
                        this.getY() + 5,
                        getSortColor(),
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;

        if (this.isValidClickButton(button)) {
            boolean clicked = this.isMouseOver(mouseX, mouseY);
            if (clicked) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());

                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    forwardClick();
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    backwardClick();
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    resetClick();
                }

                screen.updateSearchFromQuickFilters();

                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    }

    public final String getItemSearchQuery() {
        if (sortDirection == null) return "";

        return Services.ItemFilter.getItemFilterString(Map.of(), getSortInfos(), List.of());
    }

    private void forwardClick() {
        // Cycle between null and the sort directions
        if (sortDirection == null) {
            sortDirection = SortDirection.ASCENDING;
        } else {
            sortDirection = sortDirection == SortDirection.ASCENDING ? SortDirection.DESCENDING : null;
        }
    }

    private void backwardClick() {
        // Cycle between null and the sort directions
        if (sortDirection == null) {
            sortDirection = SortDirection.DESCENDING;
        } else {
            sortDirection = sortDirection == SortDirection.DESCENDING ? SortDirection.ASCENDING : null;
        }
    }

    private void resetClick() {
        sortDirection = null;
    }

    protected abstract String getSortName();

    protected abstract CustomColor getSortColor();

    protected abstract List<SortInfo> getSortInfos();
}
