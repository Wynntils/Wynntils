/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.RarityStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
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

public class GearTierFilterWidget extends GuideFilterWidget {
    private final List<GearTierButton> gearTierButtons = new ArrayList<>();
    private RarityStatProvider provider;

    public GearTierFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(70, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Rarity")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        gearTierButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (GearTierButton gearTierButton : gearTierButtons) {
            if (gearTierButton.isMouseOver(event.x(), event.y())) {
                clicked = gearTierButton.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        gearTierButtons.clear();

        gearTierButtons.add(new GearTierButton(GearTier.MYTHIC, Texture.MYTHIC_FILTER_ICON, searchQuery));
        gearTierButtons.add(new GearTierButton(GearTier.FABLED, Texture.FABLED_FILTER_ICON, searchQuery));
        gearTierButtons.add(new GearTierButton(GearTier.LEGENDARY, Texture.LEGENDARY_FILTER_ICON, searchQuery));
        gearTierButtons.add(new GearTierButton(GearTier.RARE, Texture.RARE_FILTER_ICON, searchQuery));
        gearTierButtons.add(new GearTierButton(GearTier.UNIQUE, Texture.UNIQUE_FILTER_ICON, searchQuery));
        gearTierButtons.add(new GearTierButton(GearTier.NORMAL, Texture.NORMAL_FILTER_ICON, searchQuery));

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (gearTierButtons == null) return;

        int renderX = getX();
        int renderY = getY() + 10;
        for (int i = 0; i < gearTierButtons.size(); i++) {
            gearTierButtons.get(i).setPosition(renderX, renderY);

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

        for (GearTierButton gearTierButton : gearTierButtons) {
            StatProviderAndFilterPair filterPair = gearTierButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof RarityStatProvider)
                .map(statProvider -> (RarityStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get rarity stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        gearTierButtons.forEach(gearTierButton -> gearTierButton.updateStateFromQuery(searchQuery));
    }

    private static class GearTierButton extends GuideFilterButton<RarityStatProvider> {
        private final GearTier gearTier;

        protected GearTierButton(GearTier gearTier, Texture texture, ItemSearchQuery searchQuery) {
            super(0, 0, 64, 16, texture);

            this.gearTier = gearTier;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics, texture, getX(), getY());

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(gearTier.getName())
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                            getX() + 18,
                            getY() + 8,
                            getWidth() - 20,
                            CustomColor.fromChatFormatting(gearTier.getChatFormatting()),
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
                    .filter(filterPair -> filterPair.statProvider() instanceof RarityStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(gearTier.getName()));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(RarityStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt =
                    new StringStatFilter.StringStatFilterFactory().create(gearTier.getName());

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
