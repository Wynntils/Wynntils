/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing.widgets;

import com.google.common.collect.Lists;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.RenderDirection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SavedItemsButton extends WynntilsButton {
    private final Consumer<Integer> onClick;
    private final List<Component> tooltip;
    private final Texture buttonTexture;

    public SavedItemsButton(int x, int y, Consumer<Integer> onClick, List<Component> tooltip, Texture buttonTexture) {
        super(x, y, 9, 9, Component.literal("Saved Items Button"));
        this.onClick = onClick;
        this.tooltip = ComponentUtils.wrapTooltips(tooltip, 200);
        this.buttonTexture = buttonTexture;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, buttonTexture, this.getX(), this.getY(), this.isHovered, RenderDirection.VERTICAL);

        if (this.isHovered) {
            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        if (onClick != null) {
            onClick.accept(event.button());
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    // Unused
    @Override
    public void onPress(InputWithModifiers input) {}
}
