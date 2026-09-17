/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.GearTypeStatProvider;
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

public class GearTypeFilterWidget extends GuideFilterWidget {
    private final List<GearTypeButton> gearTypeButtons = new ArrayList<>();
    private GearTypeStatProvider provider;

    public GearTypeFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(130, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Gear Type")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        gearTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (GearTypeButton gearTypeButton : gearTypeButtons) {
            if (gearTypeButton.isMouseOver(event.x(), event.y())) {
                clicked = gearTypeButton.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        gearTypeButtons.clear();

        gearTypeButtons.add(new GearTypeButton(GearType.HELMET, Texture.HELMET_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.CHESTPLATE, Texture.CHESTPLATE_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.LEGGINGS, Texture.LEGGINGS_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.BOOTS, Texture.BOOTS_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.RING, Texture.RING_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.BRACELET, Texture.BRACELET_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.NECKLACE, Texture.NECKLACE_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.SPEAR, Texture.SPEAR_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.WAND, Texture.WAND_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.DAGGER, Texture.DAGGER_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.BOW, Texture.BOW_FILTER_ICON, searchQuery));
        gearTypeButtons.add(new GearTypeButton(GearType.RELIK, Texture.RELIK_FILTER_ICON, searchQuery));

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (gearTypeButtons == null) return;

        int renderX = getX();
        int renderY = getY() + 10;
        for (int i = 0; i < gearTypeButtons.size(); i++) {
            gearTypeButtons.get(i).setPosition(renderX, renderY);

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

        for (GearTypeButton gearTypeButton : gearTypeButtons) {
            StatProviderAndFilterPair filterPair = gearTypeButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof GearTypeStatProvider)
                .map(statProvider -> (GearTypeStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get gear type stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        gearTypeButtons.forEach(gearTypeButton -> gearTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class GearTypeButton extends GuideFilterButton<GearTypeStatProvider> {
        private final GearType gearType;

        protected GearTypeButton(GearType gearType, Texture texture, ItemSearchQuery searchQuery) {
            super(0, 0, 64, 16, texture);

            this.gearType = gearType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics, texture, getX(), getY());

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(EnumUtils.toNiceString(gearType))
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
                    .filter(filterPair -> filterPair.statProvider() instanceof GearTypeStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(EnumUtils.toNiceString(gearType)));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(GearTypeStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt =
                    new StringStatFilter.StringStatFilterFactory().create(EnumUtils.toNiceString(gearType));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
