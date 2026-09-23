/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.statproviders.LevelStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class LevelFilterWidget extends GuideFilterWidget {
    private static final int MIN_LEVEL = 1;
    private static int maxLevel;

    private LevelSlider minLevelSlider;
    private LevelSlider maxLevelSlider;
    private LevelStatProvider provider;

    public LevelFilterWidget(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, boolean combatLevel) {
        super(58, containerWidget);

        maxLevel = combatLevel ? Models.CombatXp.MAX_LEVEL : Models.Profession.MAX_LEVEL;

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Level")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        minLevelSlider.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        maxLevelSlider.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        if (minLevelSlider.isMouseOver(event.x(), event.y())) {
            clicked = minLevelSlider.mouseClicked(event, isDoubleClick);
        } else if (maxLevelSlider.isMouseOver(event.x(), event.y())) {
            clicked = maxLevelSlider.mouseClicked(event, isDoubleClick);
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (minLevelSlider.dragging) {
            minLevelSlider.mouseDragged(event, dragX, dragY);
        } else if (maxLevelSlider.dragging) {
            maxLevelSlider.mouseDragged(event, dragX, dragY);
        }

        containerWidget.updateSearchFromQuickFilters();

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (minLevelSlider.isMouseOver(event.x(), event.y())) {
            minLevelSlider.mouseReleased(event);
        } else if (maxLevelSlider.isMouseOver(event.x(), event.y())) {
            maxLevelSlider.mouseReleased(event);
        } else {
            minLevelSlider.dragging = false;
            maxLevelSlider.dragging = false;
        }

        return false;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        minLevelSlider = new LevelSlider(MIN_LEVEL, true, searchQuery);
        maxLevelSlider = new LevelSlider(maxLevel, false, searchQuery);

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (minLevelSlider == null || maxLevelSlider == null) return;

        minLevelSlider.setPosition(getX(), getY() + 10);
        maxLevelSlider.setPosition(getX(), getY() + 32);
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        int min = minLevelSlider.getLevel();
        int max = maxLevelSlider.getLevel();
        StatProviderAndFilterPair levelFilter =
                new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                        .create(min + "-" + max)
                        .map(statFilter -> new StatProviderAndFilterPair<>(provider, statFilter))
                        .orElse(null);
        if (levelFilter != null) {
            filterPairs.add(levelFilter);
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof LevelStatProvider)
                .map(statProvider -> (LevelStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get level stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        maxLevelSlider.updateStateFromQuery(searchQuery);
        minLevelSlider.updateStateFromQuery(searchQuery);
    }

    private static class LevelSlider extends GuideFilterSlider<LevelStatProvider> {
        private final boolean minSlider;
        private int level;

        protected LevelSlider(int initalLevel, boolean minSlider, ItemSearchQuery searchQuery) {
            super(
                    0,
                    0,
                    128,
                    20,
                    Component.literal((minSlider ? "Min: " : "Max: ") + initalLevel)
                            .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)),
                    (double) (initalLevel - MIN_LEVEL) / (maxLevel - MIN_LEVEL));

            level = initalLevel;
            this.minSlider = minSlider;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            List<StatProviderAndFilterPair> levelProviders = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof LevelStatProvider)
                    .toList();

            if (levelProviders.isEmpty()) {
                if (minSlider) {
                    level = MIN_LEVEL;
                } else {
                    level = maxLevel;
                }
            } else if (levelProviders.size() != 1) {
                WynntilsMod.error("Unexpected count of level providers: " + levelProviders.size());
            } else if (levelProviders.getFirst().statFilter() instanceof RangedStatFilters.RangedIntegerStatFilter) {
                if (minSlider) {
                    level = ((RangedStatFilters.RangedIntegerStatFilter)
                                    levelProviders.getFirst().statFilter())
                            .getMin();
                } else {
                    level = ((RangedStatFilters.RangedIntegerStatFilter)
                                    levelProviders.getLast().statFilter())
                            .getMax();
                }
            } else {
                WynntilsMod.error("Level filter is not a ranged integer filter");
            }
        }

        public int getLevel() {
            return level;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal((minSlider ? "Min: " : "Max: ") + level)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
        }

        @Override
        protected void applyValue() {
            level = (int) Math.round(MIN_LEVEL + this.value * (maxLevel - 1));
        }
    }
}
