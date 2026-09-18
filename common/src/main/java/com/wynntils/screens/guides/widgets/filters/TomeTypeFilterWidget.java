/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.rewards.type.TomeType;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.TomeTypeStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class TomeTypeFilterWidget extends GuideFilterWidget {
    private final List<TomeTypeButton> tomeTypeButtons = new ArrayList<>();
    private TomeTypeStatProvider provider;

    public TomeTypeFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(90, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Tome Type")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        tomeTypeButtons.forEach(widget -> widget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (TomeTypeButton tomeTypeButton : tomeTypeButtons) {
            if (tomeTypeButton.isMouseOver(event.x(), event.y())) {
                clicked = tomeTypeButton.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        tomeTypeButtons.clear();

        tomeTypeButtons.add(new TomeTypeButton(TomeType.GUILD_TOME, Texture.GUILD_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(new TomeTypeButton(TomeType.WEAPON_TOME, Texture.WEAPON_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(new TomeTypeButton(TomeType.ARMOUR_TOME, Texture.ARMOR_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(
                new TomeTypeButton(TomeType.MYSTICISM_TOME, Texture.MYSTICISM_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(new TomeTypeButton(TomeType.MARATHON_TOME, Texture.MARATHON_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(new TomeTypeButton(TomeType.LOOTRUN_TOME, Texture.LOOTRUN_TOME_FILTER_ICON, searchQuery));
        tomeTypeButtons.add(
                new TomeTypeButton(TomeType.EXPERTISE_TOME, Texture.EXPERTISE_TOME_FILTER_ICON, searchQuery));

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (tomeTypeButtons == null) return;

        int renderX = getX();
        int renderY = getY() + 10;
        for (int i = 0; i < tomeTypeButtons.size(); i++) {
            tomeTypeButtons.get(i).setPosition(renderX, renderY);

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

        for (TomeTypeButton tomeTypeButton : tomeTypeButtons) {
            StatProviderAndFilterPair filterPair = tomeTypeButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof TomeTypeStatProvider)
                .map(statProvider -> (TomeTypeStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get rarity stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        tomeTypeButtons.forEach(tomeTypeButton -> tomeTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class TomeTypeButton extends GuideFilterButton<TomeTypeStatProvider> {
        private final TomeType tomeType;

        protected TomeTypeButton(TomeType tomeType, Texture texture, ItemSearchQuery searchQuery) {
            super(0, 0, 64, 16, texture);

            this.tomeType = tomeType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics, texture, getX(), getY());

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(
                                            EnumUtils.toNiceString(tomeType).replace(" Tome", ""))
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
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            state = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof TomeTypeStatProvider)
                    .anyMatch(filterPair -> filterPair
                            .statFilter()
                            .matches(EnumUtils.toNiceString(tomeType).replace(" ", "_")));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(TomeTypeStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt = new StringStatFilter.StringStatFilterFactory()
                    .create(EnumUtils.toNiceString(tomeType).replace(" ", "_"));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
