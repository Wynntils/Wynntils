/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quickfilters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public abstract class TerritoryQuickFilterWidget extends AbstractWidget {
    private final TerritoryManagementScreen screen;

    protected TerritoryQuickFilterWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean isHovered = this.isMouseOver(mouseX, mouseY);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(
                                getFilterName().withStyle(isHovered ? ChatFormatting.BOLD : ChatFormatting.RESET)),
                        this.getX() + 5,
                        this.getY() + 5,
                        getFilterColor(),
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
        Map<ItemStatProvider<?>, List<StatProviderAndFilterPair>> filterMap = new HashMap<>();

        for (StatProviderAndFilterPair filter : getFilters()) {
            filterMap
                    .computeIfAbsent(filter.statProvider(), k -> new ArrayList<>())
                    .add(filter);
        }

        return Services.ItemFilter.getItemFilterString(filterMap, List.of(), List.of());
    }

    protected abstract void forwardClick();

    protected abstract void backwardClick();

    protected abstract void resetClick();

    protected abstract MutableComponent getFilterName();

    protected abstract CustomColor getFilterColor();

    protected abstract List<StatProviderAndFilterPair> getFilters();
}
