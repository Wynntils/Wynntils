/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class NewLoadoutButton extends AbstractButton implements TooltipProvider {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public NewLoadoutButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 133 - 10, 20, Component.literal("New Loadout Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GREEN, x, y, this.width, this.height);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_PLUS_ICON,
                this.x + 15,
                (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_PLUS_ICON.height() / 2f);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.buildLoadouts.loadoutSelectionWidget.newLoadout")),
                        (this.x + this.width / 2f),
                        (this.y + this.height / 2f),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (parent.getCurrentCategory() == MenuCategory.NEW_LOADOUT) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB,
                    x + this.width - Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB.width() / 2f,
                    (this.y + this.height / 2f) - Texture.BUILD_LOADOUTS_WIDGET_SELECT_TAB.height() / 2f);
        }
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        parent.setCurrentCategory(MenuCategory.NEW_LOADOUT);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return List.of();
    }
}
