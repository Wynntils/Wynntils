/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.statistics.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.statistics.WynntilsStatisticsScreen;
import com.wynntils.services.statistics.type.StatisticEntry;
import com.wynntils.services.statistics.type.StatisticKind;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class StatisticButton extends WynntilsButton implements TooltipProvider {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);
    private static final CustomColor TRACKED_BUTTON_COLOR = new CustomColor(176, 197, 148);
    private static final CustomColor TRACKED_BUTTON_COLOR_HOVERED = new CustomColor(126, 211, 106);

    private final Pair<StatisticKind, StatisticEntry> statistic;
    private final WynntilsStatisticsScreen screen;

    public StatisticButton(
            int x,
            int y,
            int width,
            int height,
            Pair<StatisticKind, StatisticEntry> statistic,
            WynntilsStatisticsScreen screen) {
        super(x, y, width, height, Component.literal("Statistics Button"));
        this.statistic = statistic;
        this.screen = screen;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = getButtonBackgroundColor();
        RenderUtils.drawRect(poseStack, backgroundColor, this.getX(), this.getY(), 0, this.width, this.height);

        int maxTextWidth = this.width - 2;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(RenderedStringUtils.getMaxFittingText(
                                statistic.a().getName(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont())),
                        this.getX() + 2,
                        this.getY() + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    private CustomColor getButtonBackgroundColor() {
        if (isSelected()) {
            return isHovered ? TRACKED_BUTTON_COLOR_HOVERED : TRACKED_BUTTON_COLOR;
        } else {
            return isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isSelected()) {
                screen.setHighlightedButton(null);
            } else {
                screen.setHighlightedButton(this);
            }
            return true;
        }

        if (KeyboardUtils.isShiftDown() && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            Services.Statistics.resetStatistic(statistic.a());
            screen.reloadElements();
            return true;
        }

        return true;
    }

    public Pair<StatisticKind, StatisticEntry> getStatistic() {
        return statistic;
    }

    @Override
    public void onPress() {}

    @Override
    public List<Component> getTooltipLines() {
        List<Component> lines = new ArrayList<>();

        lines.add(Component.literal(statistic.a().getName()).withStyle(ChatFormatting.BOLD));
        lines.add(Component.translatable(
                "screens.wynntils.statistics.total",
                statistic.a().getFormattedValue(statistic.b().total())));
        lines.add(Component.empty());

        if (isSelected()) {
            lines.add(Component.translatable("screens.wynntils.statistics.leftClickToUnselect")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            lines.add(Component.translatable("screens.wynntils.statistics.leftClickToView")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.GREEN));
        }
        lines.add(Component.translatable("screens.wynntils.statistics.shiftMiddleClickToReset")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.RED));

        return lines;
    }

    private boolean isSelected() {
        return screen.getHighlightedButton() == this;
    }
}
