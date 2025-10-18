/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WynntilsCheckbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox_selected_highlighted.png");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox_selected.png");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox_highlighted.png");
    private static final ResourceLocation CHECKBOX_SPRITE =
            ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/checkbox.png");

    public boolean selected;
    private final int maxTextWidth;
    private final CustomColor color;
    private final BiConsumer<WynntilsCheckbox, Boolean> onClick;
    private final List<Component> tooltip;

    public WynntilsCheckbox(
            int x,
            int y,
            int size,
            Component message,
            boolean selected,
            int maxTextWidth,
            CustomColor color,
            BiConsumer<WynntilsCheckbox, Boolean> onClick,
            List<Component> tooltip) {
        super(x, y, size, size, message);
        this.selected = selected;
        this.maxTextWidth = maxTextWidth;
        this.color = color;
        this.onClick = onClick;
        this.tooltip = tooltip;
    }

    public WynntilsCheckbox(
            int x,
            int y,
            int size,
            Component message,
            boolean selected,
            int maxTextWidth,
            BiConsumer<WynntilsCheckbox, Boolean> onClick,
            List<Component> tooltip) {
        this(x, y, size, message, selected, maxTextWidth, CommonColors.WHITE, onClick, tooltip);
    }

    public WynntilsCheckbox(int x, int y, int size, Component message, boolean selected, int maxTextWidth) {
        this(x, y, size, message, selected, maxTextWidth, CommonColors.WHITE, (checkbox, bl) -> {}, List.of());
    }

    public WynntilsCheckbox(
            int x,
            int y,
            int size,
            Component message,
            boolean selected,
            int maxTextWidth,
            BiConsumer<WynntilsCheckbox, Boolean> onClick) {
        this(x, y, size, message, selected, maxTextWidth, CommonColors.WHITE, onClick, null);
    }

    public WynntilsCheckbox(
            int x, int y, int size, Component message, boolean selected, int maxTextWidth, CustomColor color) {
        this(x, y, size, message, selected, maxTextWidth, color, (checkbox, bl) -> {}, List.of());
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onClick.accept(this, this.selected);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation resourceLocation;
        if (this.selected) {
            resourceLocation = this.isFocused() || this.isHovered()
                    ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE
                    : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourceLocation = this.isFocused() || this.isHovered() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        RenderUtils.drawScalingTexturedRect(
                guiGraphics.pose(), resourceLocation, this.getX(), this.getY(), 0, this.width, this.height, 20, 20);

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

        if (isHovered && tooltip != null) {
            McUtils.screen().setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public boolean isSelected() {
        return selected;
    }
}
