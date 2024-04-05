/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProviderButton extends WynntilsButton {
    private static final CustomColor DISABLED_COLOR = new CustomColor(120, 0, 0, 255);
    private static final CustomColor DISABLED_COLOR_BORDER = new CustomColor(255, 0, 0, 255);
    private static final CustomColor ENABLED_COLOR = new CustomColor(0, 116, 0, 255);
    private static final CustomColor ENABLED_COLOR_BORDER = new CustomColor(0, 220, 0, 255);

    private final boolean inUse;
    private final float translationX;
    private final float translationY;
    private final ItemStatProvider<?> provider;

    public ProviderButton(
            int x,
            int y,
            int width,
            int height,
            ItemStatProvider<?> provider,
            boolean inUse,
            float translationX,
            float translationY) {
        super(x, y, width, height, Component.literal(provider.getTranslatedName()));

        this.provider = provider;
        this.inUse = inUse;
        this.translationX = translationX;
        this.translationY = translationY;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(poseStack, getRectColor().withAlpha(100), getX(), getY(), 0, width, height);

        RenderUtils.drawRectBorders(poseStack, getBorderColor(), getX(), getY(), getX() + width, getY() + height, 1, 2);

        FontRenderer.getInstance()
                .renderScrollingString(
                        poseStack,
                        StyledText.fromString(provider.getDisplayName()),
                        getX() + 2,
                        getY() + (height / 2f),
                        width - 4,
                        translationX,
                        translationY,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.0f);

        if (this.isHovered) {
            McUtils.mc()
                    .screen
                    .setTooltipForNextRenderPass(Lists.transform(
                            List.of(
                                    Component.literal(provider.getDescription()),
                                    Component.translatable("screens.wynntils.itemFilter.providerHelp")),
                            Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (McUtils.mc().screen instanceof ItemFilterScreen itemFilterScreen) {
            if (itemFilterScreen.inSortMode()) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    itemFilterScreen.addSort(new Pair<>(provider, provider.getName()));
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    itemFilterScreen.removeSort(provider);
                }
            } else {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    itemFilterScreen.setSelectedProvider(provider);
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    itemFilterScreen.removeFilter(provider);
                }
            }
        }

        return true;
    }

    @Override
    public void onPress() {}

    private CustomColor getRectColor() {
        if (McUtils.mc().screen instanceof ItemFilterScreen itemFilterScreen) {
            if (itemFilterScreen.getSelectedProvider() == provider) {
                return CommonColors.GRAY;
            }
        }

        return inUse ? ENABLED_COLOR_BORDER : DISABLED_COLOR_BORDER;
    }

    private CustomColor getBorderColor() {
        if (McUtils.mc().screen instanceof ItemFilterScreen itemFilterScreen) {
            if (itemFilterScreen.getSelectedProvider() == provider) {
                return CommonColors.WHITE;
            }
        }

        return inUse ? ENABLED_COLOR : DISABLED_COLOR;
    }
}
