/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FilterButton extends WynntilsButton implements TooltipProvider {
    private static final Component DEFAULT_ENABLED_TOOLTIP =
            Component.translatable("screens.wynntils.content.clickToHide").withStyle(ChatFormatting.GRAY);

    private static final Component DEFAULT_DISABLED_TOOLTIP =
            Component.translatable("screens.wynntils.content.clickToShow").withStyle(ChatFormatting.GRAY);

    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);
    private static final CustomColor BUTTON_COLOR_ENABLED = new CustomColor(164, 212, 142);

    private final Texture texture;
    private final boolean dynamicTexture;
    private final List<Component> tooltipList;
    private final List<Component> enabledActionTooltip;
    private final List<Component> disabledActionTooltip;
    private final Runnable onPress;
    private final Supplier<Boolean> isEnabled;

    public FilterButton(
            int x,
            int y,
            int width,
            int height,
            Texture texture,
            boolean dynamicTexture,
            List<Component> tooltipList,
            Runnable onPress,
            Supplier<Boolean> isEnabled) {
        super(x, y, width, height, Component.literal("Filter Button"));

        this.texture = texture;
        this.dynamicTexture = dynamicTexture;
        this.tooltipList = tooltipList;
        this.enabledActionTooltip = List.of(DEFAULT_ENABLED_TOOLTIP);
        this.disabledActionTooltip = List.of(DEFAULT_DISABLED_TOOLTIP);
        this.onPress = onPress;
        this.isEnabled = isEnabled;
    }

    public FilterButton(
            int x,
            int y,
            int width,
            int height,
            Texture texture,
            boolean dynamicTexture,
            List<Component> tooltipList,
            List<Component> enabledActionTooltip,
            List<Component> disabledActionTooltip,
            Runnable onPress,
            Supplier<Boolean> isEnabled) {
        super(x, y, width, height, Component.literal("Filter Button"));
        this.texture = texture;
        this.dynamicTexture = dynamicTexture;
        this.tooltipList = tooltipList;
        this.enabledActionTooltip = enabledActionTooltip;
        this.disabledActionTooltip = disabledActionTooltip;
        this.onPress = onPress;
        this.isEnabled = isEnabled;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(poseStack, getButtonColor(), getX(), getY(), 0, width, height);

        if (!this.dynamicTexture) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    texture.resource(),
                    getX() + (width - texture.width()) / 2f,
                    getY() + (height - texture.height()) / 2f,
                    1,
                    texture.width(),
                    texture.height(),
                    0,
                    0,
                    texture.width(),
                    texture.height(),
                    texture.width(),
                    texture.height());
        } else {
            if (this.isHovered) {
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        getX() + (width - texture.width()) / 2f,
                        getY() + (height - texture.height() / 2f) / 2f,
                        1,
                        texture.width(),
                        texture.height() / 2f,
                        0,
                        texture.height() / 2,
                        texture.width(),
                        texture.height() / 2,
                        texture.width(),
                        texture.height());
            } else {
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        getX() + (width - texture.width()) / 2f,
                        getY() + (height - texture.height() / 2f) / 2f,
                        1,
                        texture.width(),
                        texture.height() / 2f,
                        0,
                        0,
                        texture.width(),
                        texture.height() / 2,
                        texture.width(),
                        texture.height());
            }
        }
    }

    private CustomColor getButtonColor() {
        if (isEnabled.get()) return BUTTON_COLOR_ENABLED;

        return isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;
    }

    @Override
    public List<Component> getTooltipLines() {
        List<Component> renderedTooltip = new ArrayList<>(tooltipList);

        if (isEnabled.get()) {
            renderedTooltip.addAll(enabledActionTooltip);
        } else {
            renderedTooltip.addAll(disabledActionTooltip);
        }

        return renderedTooltip;
    }

    @Override
    public void onPress() {
        onPress.run();
    }
}
