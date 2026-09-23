/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.statproviders.QualityTierStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class QualityTierFilterWidget extends GuideFilterWidget {
    private final List<QualityTierButton> qualityTierButtons = new ArrayList<>();
    private QualityTierStatProvider provider;

    public QualityTierFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(50, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Quality Tier")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        qualityTierButtons.forEach(widget -> widget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (QualityTierButton qualityTierButton : qualityTierButtons) {
            if (qualityTierButton.isMouseOver(event.x(), event.y())) {
                clicked = qualityTierButton.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        qualityTierButtons.clear();

        qualityTierButtons.add(new QualityTierButton(0, Texture.TIER_0_FILTER_ICON, searchQuery));
        qualityTierButtons.add(new QualityTierButton(1, Texture.TIER_1_FILTER_ICON, searchQuery));
        qualityTierButtons.add(new QualityTierButton(2, Texture.TIER_2_FILTER_ICON, searchQuery));
        qualityTierButtons.add(new QualityTierButton(3, Texture.TIER_3_FILTER_ICON, searchQuery));

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (qualityTierButtons == null) return;

        int renderX = getX();
        int renderY = getY() + 10;
        for (int i = 0; i < qualityTierButtons.size(); i++) {
            qualityTierButtons.get(i).setPosition(renderX, renderY);

            if (i % 2 == 0) {
                renderX = getX() + 65;
            } else {
                renderX = getX();
                renderY += 20;
            }
        }
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (QualityTierButton qualityTierButton : qualityTierButtons) {
            StatProviderAndFilterPair filterPair = qualityTierButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof QualityTierStatProvider)
                .map(statProvider -> (QualityTierStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get quality tier stat provider"));
        return provider;
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        qualityTierButtons.forEach(qualityTierButton -> qualityTierButton.updateStateFromQuery(searchQuery));
    }

    private static class QualityTierButton extends GuideFilterButton<QualityTierStatProvider> {
        private final int tier;

        protected QualityTierButton(int tier, Texture texture, ItemSearchQuery searchQuery) {
            super(0, 0, 64, 16, texture);

            this.tier = tier;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics, texture, getX(), getY());

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal("Tier " + tier)
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                            getX() + 18,
                            getY() + 8,
                            getWidth() - 20,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (!isHovered && !state) return;

            RenderUtils.drawRect(
                    guiGraphics,
                    (state ? CommonColors.LIGHT_GREEN : CommonColors.WHITE).withAlpha(isHovered ? 0.7f : 0.5f),
                    getX(),
                    getY(),
                    getWidth(),
                    16);

            handleCursor(guiGraphics);

            if (isHovered) {
                MutableComponent toggleComponent = Component.empty()
                        .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                        .append(" ")
                        .append(Component.literal("/")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                        .append(" ")
                        .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                        .append(" ")
                        .append(Component.translatable(
                                "screens.wynntils.wynntilsGuides.filterWidget.tooltipToggle",
                                I18n.get("service.wynntils.itemFilter.stat.qualityTier.name") + " " + tier));
                MutableComponent removeComponent = Component.empty()
                        .append(WynnFont.asFont("middle_click", WynncraftKeybindsFont.class))
                        .append(" ")
                        .append(Component.translatable("screens.wynntils.wynntilsGuides.filterWidget.tooltipRemove"));

                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(
                                ComponentUtils.wrapTooltips(List.of(toggleComponent, removeComponent), 250),
                                Component::getVisualOrderText),
                        mouseX,
                        mouseY);
            }
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof QualityTierStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(tier));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(QualityTierStatProvider provider) {
            if (!state) return null;

            Optional<RangedStatFilters.RangedIntegerStatFilter> statFilterOpt =
                    new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                            .create(String.valueOf(tier));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
