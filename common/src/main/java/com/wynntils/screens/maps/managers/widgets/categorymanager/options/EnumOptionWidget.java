/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.categorymanager.options;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class EnumOptionWidget<E extends Enum<E>> extends AbstractOptionWidget<E> {
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 20;

    public EnumOptionWidget(
            Component label,
            Component description,
            OptionCategory category,
            Function<MapAttributes, Optional<E>> valueGetter) {
        super(label, description, 20, category, valueGetter);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(getMessage().getString()),
                        getX(),
                        getY() + this.height / 2f,
                        !this.inherited || isOverridden() ? CommonColors.WHITE : CommonColors.GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        int elementY = getY() + (this.height - BUTTON_HEIGHT) / 2;
        int buttonX = getX() + getWidth() - BUTTON_WIDTH;

        if (isMouseOverButton(mouseX, mouseY)) {
            handleCursor(guiGraphics);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, buttonX, elementY, BUTTON_WIDTH, BUTTON_HEIGHT);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(EnumUtils.toNiceString(value)),
                        buttonX + BUTTON_WIDTH / 2f,
                        elementY + BUTTON_HEIGHT / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (super.mouseClicked(event, isDoubleClick)) return true;

        if (!isMouseOver(event.x(), event.y())) return false;

        int addToIndex =
                switch (event.button()) {
                    case InputConstants.MOUSE_BUTTON_LEFT -> 1;
                    case InputConstants.MOUSE_BUTTON_RIGHT -> -1;
                    default -> 0;
                };
        if (addToIndex == 0) return false;

        List<E> enumConstants =
                EnumSet.allOf((Class<E>) value.getClass()).stream().toList();
        int nextIndex = (enumConstants.indexOf(value) + addToIndex + enumConstants.size()) % enumConstants.size();
        E nextValue = enumConstants.get(nextIndex);

        this.playDownSound(McUtils.mc().getSoundManager());
        setValue(nextValue);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private boolean isMouseOverButton(double mouseX, double mouseY) {
        int buttonY = getY() + (this.height - BUTTON_HEIGHT) / 2;
        int buttonX = getX() + getWidth() - BUTTON_WIDTH;

        return MathUtils.isInside(
                (int) mouseX, (int) mouseY, buttonX, buttonX + BUTTON_WIDTH - 1, buttonY, buttonY + BUTTON_HEIGHT - 1);
    }
}
