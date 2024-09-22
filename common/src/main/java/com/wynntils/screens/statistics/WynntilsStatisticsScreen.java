/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.statistics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.FilterButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.statistics.widgets.StatisticButton;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.services.statistics.type.StatisticEntry;
import com.wynntils.services.statistics.type.StatisticKind;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsStatisticsScreen extends WynntilsListScreen<StatisticKind, StatisticButton> {
    private StatisticKind highlightedStatisticKind;

    private WynntilsStatisticsScreen() {
        super(Component.translatable("screens.wynntils.statistics.name"));
    }

    public static WynntilsStatisticsScreen create() {
        return new WynntilsStatisticsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW_OFFSET.width() / 2,
                Texture.BACK_ARROW_OFFSET.height(),
                WynntilsMenuScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 50,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));

        this.addRenderableWidget(new FilterButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 15) / 2f),
                157,
                30,
                30,
                Texture.FAVORITE_ICON,
                false,
                List.of(Component.literal("[>] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.translatable("screens.wynntils.statistics.modeButton.name")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN))),
                List.of(
                        Component.translatable("screens.wynntils.statistics.modeButton.descriptionOverall")
                                .withStyle(ChatFormatting.GRAY),
                        Component.empty(),
                        Component.translatable("screens.wynntils.statistics.modeButton.enabled")
                                .withStyle(ChatFormatting.GRAY)),
                List.of(
                        Component.translatable("screens.wynntils.statistics.modeButton.descriptionSingle")
                                .withStyle(ChatFormatting.GRAY),
                        Component.empty(),
                        Component.translatable("screens.wynntils.statistics.modeButton.disabled")
                                .withStyle(ChatFormatting.GRAY)),
                () -> {
                    Storage<Boolean> screenOverallMode = Services.Statistics.screenOverallMode;
                    screenOverallMode.store(!screenOverallMode.get());
                    reloadElements();
                },
                Services.Statistics.screenOverallMode::get));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.statistics.name"));

        renderVersion(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        if (elements.isEmpty()) {
            renderNoElementsHelper(poseStack, I18n.get("screens.wynntils.statistics.noStatistics"));
        }

        renderDescription(poseStack);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderDescription(PoseStack poseStack) {
        if (highlightedStatisticKind == null) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString(I18n.get("screens.wynntils.statistics.noItemSelected")),
                            20,
                            100,
                            Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                            CommonColors.BLACK,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NONE);
            return;
        }

        StatisticEntry entry = Services.Statistics.screenOverallMode.get()
                ? Services.Statistics.getOverallStatistic(highlightedStatisticKind)
                : Services.Statistics.getStatistic(highlightedStatisticKind);

        poseStack.pushPose();
        poseStack.translate(20, 75, 0);

        // Name
        poseStack.pushPose();
        poseStack.scale(1.2f, 1.2f, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(highlightedStatisticKind.getName()),
                        0,
                        0,
                        (Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20) / 1.2f,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();

        // Statistics
        switch (highlightedStatisticKind.getType()) {
            case COUNT -> renderCountStatistics(poseStack, highlightedStatisticKind, entry);
            case ADVANCED -> renderAdvancedStatistics(poseStack, highlightedStatisticKind, entry);
        }

        poseStack.popPose();
    }

    private static void renderCountStatistics(PoseStack poseStack, StatisticKind statisticKind, StatisticEntry entry) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.statistics.count", statisticKind.getFormattedValue(entry.count()))),
                        0,
                        30,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    private static void renderAdvancedStatistics(
            PoseStack poseStack, StatisticKind statisticKind, StatisticEntry entry) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.statistics.total", statisticKind.getFormattedValue(entry.total()))),
                        0,
                        30,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        // Note: Count is not formatted according to the formatter
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.statistics.count", entry.count())),
                        0,
                        40,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.statistics.min", statisticKind.getFormattedValue(entry.min()))),
                        0,
                        50,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.statistics.max", statisticKind.getFormattedValue(entry.max()))),
                        0,
                        60,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get(
                                "screens.wynntils.statistics.average",
                                statisticKind.getFormattedValue(entry.average()))),
                        0,
                        70,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 20,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    public void setHighlightedStatisticKind(StatisticKind kind) {
        this.highlightedStatisticKind = kind;
    }

    public StatisticKind getHighlightedStatisticKind() {
        return highlightedStatisticKind;
    }

    @Override
    protected StatisticButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new StatisticButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Arrays.stream(StatisticKind.values())
                .filter(statisticEntry -> StringUtils.partialMatch(statisticEntry.getName(), searchTerm))
                .toList());
    }
}
