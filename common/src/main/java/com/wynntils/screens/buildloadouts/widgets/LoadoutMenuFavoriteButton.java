/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LoadoutMenuFavoriteButton extends AbstractWidget implements TooltipProvider {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();

    public LoadoutMenuFavoriteButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 20, 20, Component.literal("Loadout Menu Favorite Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
        buildTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND, x - 5, y - 5, this.width + 10, this.height + 10);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT, x, y, this.width, this.height);

        Loadout loadout = parent.getSelectedLoadout();
        if (loadout != null) {
            if (loadout.favorited()) {
                RenderUtils.drawTexturedRect(guiGraphics, Texture.BUILD_LOADOUTS_STAR_ICON, x + 4, y + 4);
            } else {
                RenderUtils.drawTexturedRect(guiGraphics, Texture.BUILD_LOADOUTS_STAR_ICON_OUTLINE, x + 4, y + 4);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        Loadout selected = parent.getSelectedLoadout();
        Services.loadout.setFavorited(selected.name(), !selected.favorited());
        parent.setSelectedLoadout(Services.loadout.getLoadout(selected.name()));
        parent.loadoutScrollListWidget.scrollOffset = 0;
        parent.loadoutScrollListWidget.populateLoadouts();
        buildTooltip();

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public void buildTooltip() {
        this.generatedTooltip = new ArrayList<>();
        Loadout selected = parent.getSelectedLoadout();
        if (selected != null && selected.favorited()) {
            this.generatedTooltip.add(Component.translatable(
                            "screens.wynntils.buildLoadouts.loadoutMenu.favoriteButton.tooltip.unFavorite")
                    .withStyle(ChatFormatting.GOLD));
        } else {
            this.generatedTooltip.add(
                    Component.translatable("screens.wynntils.buildLoadouts.loadoutMenu.favoriteButton.tooltip.favorite")
                            .withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }
}
