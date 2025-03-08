/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsAspectGuideScreen
        extends WynntilsGuideScreen<GuideAspectItemStack, GuideAspectItemStackButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<GuideAspectItemStack> allAspectItems = List.of();

    private WynntilsAspectGuideScreen() {
        super(
                Component.translatable("screens.wynntils.wynntilsGuides.aspectGuide.name"),
                List.of(ItemProviderType.GENERIC, ItemProviderType.GEAR, ItemProviderType.TIERED));
    }

    public static Screen create() {
        return new WynntilsAspectGuideScreen();
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

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.aspectGuide.name"));

        renderDescription(
                poseStack,
                I18n.get("screens.wynntils.wynntilsGuides.guideDescription"),
                I18n.get("screens.wynntils.wynntilsGuides.filterHelper"));

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof GuideAspectItemStackButton guideAspectItemStackButton) {
            guiGraphics.renderTooltip(
                    FontRenderer.getInstance().getFont(), guideAspectItemStackButton.getItemStack(), mouseX, mouseY);
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderItemsHeader(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available")),
                        Texture.CONTENT_BOOK_BACKGROUND.width() * 0.75f,
                        30,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    @Override
    protected GuideAspectItemStackButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuideAspectItemStackButton(
                xOffset + Texture.CONTENT_BOOK_BACKGROUND.width() / 2 + 13,
                yOffset + 43,
                18,
                18,
                elements.get(i),
                this);
    }

    protected void reloadElementsList(ItemSearchQuery searchQuery) {
        elements.addAll(Services.ItemFilter.filterAndSort(searchQuery, getAllAspectItems()));
    }

    private List<GuideAspectItemStack> getAllAspectItems() {
        if (allAspectItems.isEmpty()) {
            // Populate list, create an item for each tier
            allAspectItems = Models.Aspect.getAllAspectInfos()
                    .flatMap(aspectInfo -> IntStream.range(
                                    0, aspectInfo.effects().size())
                            .mapToObj(tier -> new GuideAspectItemStack(aspectInfo, tier + 1)))
                    .collect(Collectors.toList());
        }

        return allAspectItems;
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}
