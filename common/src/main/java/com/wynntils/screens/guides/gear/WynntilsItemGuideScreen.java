/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.filters.GearTypeFilterWidget;
import com.wynntils.screens.guides.widgets.filters.RarityFilterWidget;
import com.wynntils.screens.guides.widgets.sorts.GuideSortButton;
import com.wynntils.services.itemfilter.statproviders.RarityStatProvider;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsItemGuideScreen extends WynntilsGuideScreen<GuideGearItemStack, GuideGearItemStackButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<GuideGearItemStack> allGearItems = List.of();

    private WynntilsItemGuideScreen() {
        super(
                Component.translatable("screens.wynntils.wynntilsGuides.itemGuide.name"),
                List.of(ItemProviderType.GENERIC, ItemProviderType.GEAR));
    }

    public static Screen create() {
        return new WynntilsItemGuideScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        if (searchWidget instanceof ItemSearchWidget itemSearchWidget) {
            guideFilterWidgets.add(this.addRenderableWidget(
                    new GearTypeFilterWidget(19 + offsetX, 81 + offsetY, this, itemSearchWidget.getSearchQuery())));
            guideFilterWidgets.add(this.addRenderableWidget(
                    new RarityFilterWidget(19 + offsetX, 121 + offsetY, this, itemSearchWidget.getSearchQuery())));

            guideSortWidget.setSecondarySortButton(
                    new GuideSortButton(itemSearchWidget.getSearchQuery(), this, RarityStatProvider.class));
        } else {
            WynntilsMod.error("WynntilsItemGuideScreen's SearchWidget is not an ItemSearchWidget");
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.itemGuide.name"));

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof GuideGearItemStackButton guideGearItemStackButton) {
            guiGraphics.renderTooltip(
                    FontRenderer.getInstance().getFont(), guideGearItemStackButton.getItemStack(), mouseX, mouseY);
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
    protected GuideGearItemStackButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuideGearItemStackButton(
                (int) (xOffset + Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 13 + offsetX),
                yOffset + 43 + offsetY,
                18,
                18,
                elements.get(i),
                this);
    }

    protected void reloadElementsList(ItemSearchQuery searchQuery) {
        elements.addAll(Services.ItemFilter.filterAndSort(searchQuery, getAllGearItems()));

        guideFilterWidgets.forEach(filter -> filter.updateFromQuery(searchQuery));
        if (guideSortWidget == null) return;
        guideSortWidget.updateFromQuery(searchQuery);
    }

    private List<GuideGearItemStack> getAllGearItems() {
        if (allGearItems.isEmpty()) {
            // Populate list
            allGearItems =
                    Models.Gear.getAllGearInfos().map(GuideGearItemStack::new).toList();
        }

        return allGearItems;
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}
