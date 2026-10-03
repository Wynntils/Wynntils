/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.GearRestrictionStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
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

public class GearRestrictionFilterWidget extends GuideFilterWidget {
    private final List<GearRestrictionCheckbox> restrictionCheckboxes = new ArrayList<>();
    private GearRestrictionStatProvider provider;

    public GearRestrictionFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(70, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Restriction")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        restrictionCheckboxes.forEach(widget -> widget.extractRenderState(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (GearRestrictionCheckbox restrictionCheckbox : restrictionCheckboxes) {
            if (restrictionCheckbox.isMouseOver(event.x(), event.y())) {
                clicked = restrictionCheckbox.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        restrictionCheckboxes.clear();

        restrictionCheckboxes.add(new GearRestrictionCheckbox(GearRestrictions.NONE, searchQuery));
        restrictionCheckboxes.add(new GearRestrictionCheckbox(GearRestrictions.UNTRADABLE, searchQuery));
        restrictionCheckboxes.add(new GearRestrictionCheckbox(GearRestrictions.QUEST_ITEM, searchQuery));
        // Soulbound is not currently used so don't add it, but we keep it for parsing in case it ever returns

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (restrictionCheckboxes == null) return;

        int renderY = getY() + 10;
        for (GearRestrictionCheckbox restrictionButon : restrictionCheckboxes) {
            restrictionButon.setPosition(getX(), renderY);

            renderY += 20;
        }
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (GearRestrictionCheckbox restrictionCheckbox : restrictionCheckboxes) {
            StatProviderAndFilterPair filterPair = restrictionCheckbox.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof GearRestrictionStatProvider)
                .map(statProvider -> (GearRestrictionStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get gear restriction stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        restrictionCheckboxes.forEach(restrictionCheckbox -> restrictionCheckbox.updateStateFromQuery(searchQuery));
    }

    private static class GearRestrictionCheckbox extends GuideFilterCheckbox<GearRestrictionStatProvider> {
        private final GearRestrictions gearRestriction;

        protected GearRestrictionCheckbox(GearRestrictions gearRestriction, ItemSearchQuery searchQuery) {
            super(EnumUtils.toNiceString(gearRestriction));

            this.gearRestriction = gearRestriction;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            selected = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof GearRestrictionStatProvider)
                    .anyMatch(filterPair -> filterPair
                            .statFilter()
                            .matches(EnumUtils.toNiceString(gearRestriction).replace(" ", "_")));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(GearRestrictionStatProvider provider) {
            if (!selected) return null;

            Optional<StringStatFilter> statFilterOpt = new StringStatFilter.StringStatFilterFactory()
                    .create(EnumUtils.toNiceString(gearRestriction).replace(" ", "_"));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
