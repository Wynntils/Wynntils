/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.filters.ProfessionTypeFilterWidget;
import com.wynntils.screens.guides.widgets.filters.QualityTierFilterWidget;
import com.wynntils.screens.guides.widgets.sorts.GuideSortButton;
import com.wynntils.services.itemfilter.statproviders.QualityTierStatProvider;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsIngredientGuideScreen
        extends WynntilsGuideScreen<GuideIngredientItemStack, GuideIngredientItemStackButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<GuideIngredientItemStack> allIngredientItems = List.of();

    private WynntilsIngredientGuideScreen() {
        super(
                Component.translatable("screens.wynntils.wynntilsGuides.ingredientGuide.name"),
                List.of(ItemProviderType.GENERIC, ItemProviderType.INGREDIENT, ItemProviderType.PROFESSION));
    }

    public static Screen create() {
        return new WynntilsIngredientGuideScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        if (searchWidget instanceof ItemSearchWidget itemSearchWidget) {
            guideFilterWidgets.add(this.addRenderableWidget(new ProfessionTypeFilterWidget(
                    47 + offsetX, 81 + offsetY, this, itemSearchWidget.getSearchQuery())));
            guideFilterWidgets.add(this.addRenderableWidget(
                    new QualityTierFilterWidget(47 + offsetX, 121 + offsetY, this, itemSearchWidget.getSearchQuery())));

            guideSortWidget.setSecondarySortButton(
                    new GuideSortButton(itemSearchWidget.getSearchQuery(), this, QualityTierStatProvider.class));
        } else {
            WynntilsMod.error("WynntilsIngredientGuideScreen's SearchWidget is not an ItemSearchWidget");
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.ingredientGuide.name"));

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTitle(PoseStack poseStack, String titleString) {
        int txWidth = Texture.CONTENT_BOOK_TITLE.width();
        int txHeight = Texture.CONTENT_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CONTENT_BOOK_TITLE.resource(),
                offsetX,
                30 + offsetY,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(titleString),
                        10 + offsetX,
                        36 + offsetY,
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        1.8f);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof GuideIngredientItemStackButton guideGearItemStack) {
            guiGraphics.renderTooltip(
                    FontRenderer.getInstance().getFont(), guideGearItemStack.getItemStack(), mouseX, mouseY);
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderItemsHeader(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available")),
                        Texture.CONTENT_BOOK_BACKGROUND.width() * 0.75f + offsetX,
                        30 + offsetY,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    @Override
    protected GuideIngredientItemStackButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuideIngredientItemStackButton(
                (int) (xOffset + Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 13 + offsetX),
                yOffset + 43 + offsetY,
                18,
                18,
                elements.get(i),
                this);
    }

    protected void reloadElementsList(ItemSearchQuery searchQuery) {
        elements.addAll(Services.ItemFilter.filterAndSort(searchQuery, getAllIngredientItems()));

        guideFilterWidgets.forEach(filter -> filter.updateFromQuery(searchQuery));
        if (guideSortWidget == null) return;
        guideSortWidget.updateFromQuery(searchQuery);
    }

    private List<GuideIngredientItemStack> getAllIngredientItems() {
        if (allIngredientItems.isEmpty()) {
            allIngredientItems = Models.Ingredient.getAllIngredientInfos()
                    .map(GuideIngredientItemStack::new)
                    .toList();
        }

        return allIngredientItems;
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}
