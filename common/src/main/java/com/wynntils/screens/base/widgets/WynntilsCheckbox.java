/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WynntilsCheckbox extends Checkbox {
    private final int maxTextWidth;
    private final CustomColor color;

    private BiConsumer<WynntilsCheckbox, Integer> onClick;
    private List<Component> tooltip;

    public WynntilsCheckbox(
            int x, int y, int width, int height, Component message, boolean selected, int maxTextWidth) {
        super(x, y, width, height, message, selected);
        this.maxTextWidth = maxTextWidth;
        this.color = CommonColors.WHITE;
    }

    public WynntilsCheckbox(
            int x,
            int y,
            int width,
            int height,
            Component message,
            boolean selected,
            int maxTextWidth,
            Consumer<Integer> onClick) {
        super(x, y, width, height, message, selected);
        this.maxTextWidth = maxTextWidth;
        this.color = CommonColors.WHITE;
        this.onClick = (checkbox, button) -> onClick.accept(button);
    }

    public WynntilsCheckbox(
            int x,
            int y,
            int width,
            int height,
            Component message,
            boolean selected,
            int maxTextWidth,
            Consumer<Integer> onClick,
            List<Component> tooltip) {
        super(x, y, width, height, message, selected);
        this.maxTextWidth = maxTextWidth;
        this.color = CommonColors.WHITE;
        this.onClick = (checkbox, button) -> onClick.accept(button);
        this.tooltip = tooltip;
    }

    public WynntilsCheckbox(
            int x,
            int y,
            int width,
            int height,
            Component message,
            boolean selected,
            int maxTextWidth,
            BiConsumer<WynntilsCheckbox, Integer> onClick,
            List<Component> tooltip) {
        super(x, y, width, height, message, selected);
        this.maxTextWidth = maxTextWidth;
        this.color = CommonColors.WHITE;
        this.onClick = onClick;
        this.tooltip = tooltip;
    }

    public WynntilsCheckbox(
            int x,
            int y,
            int width,
            int height,
            Component message,
            boolean selected,
            int maxTextWidth,
            CustomColor color) {
        super(x, y, width, height, message, selected);
        this.maxTextWidth = maxTextWidth;
        this.color = color;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourceLocation;
        if (this.selected) {
            resourceLocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourceLocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.showLabel) {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics.pose(),
                            StyledText.fromComponent(this.getMessage()),
                            this.getX() + this.width + 2,
                            this.getY() + (this.height / 2f),
                            maxTextWidth,
                            color,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1f);
        }

        if (isHovered && tooltip != null) {
            McUtils.mc().screen.setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        // Do the click before the onClick so that the checkbox is updated before the consumer is called.
        boolean superClicked = super.mouseClicked(mouseX, mouseY, button);

        // Only trigger the onClick if the super was actually clicked
        if (onClick != null && superClicked) {
            onClick.accept(this, button);
        }

        return superClicked;
    }
}
