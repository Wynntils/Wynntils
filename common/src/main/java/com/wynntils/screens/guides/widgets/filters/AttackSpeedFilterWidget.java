/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.AttackSpeedStatProvider;
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

public class AttackSpeedFilterWidget extends GuideFilterWidget {
    private final List<AttackSpeedCheckbox> attackSpeedCheckboxes = new ArrayList<>();
    private AttackSpeedStatProvider provider;

    public AttackSpeedFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(150, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Attack Speed")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        attackSpeedCheckboxes.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (AttackSpeedCheckbox attackSpeedCheckbox : attackSpeedCheckboxes) {
            if (attackSpeedCheckbox.isMouseOver(event.x(), event.y())) {
                clicked = attackSpeedCheckbox.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        attackSpeedCheckboxes.clear();

        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.SUPER_FAST, searchQuery));
        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.VERY_FAST, searchQuery));
        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.FAST, searchQuery));
        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.NORMAL, searchQuery));
        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.SLOW, searchQuery));
        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.VERY_SLOW, searchQuery));
        attackSpeedCheckboxes.add(new AttackSpeedCheckbox(GearAttackSpeed.SUPER_SLOW, searchQuery));

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (attackSpeedCheckboxes == null) return;

        int renderY = getY() + 10;
        for (AttackSpeedCheckbox attackSpeedCheckbox : attackSpeedCheckboxes) {
            attackSpeedCheckbox.setPosition(getX(), renderY);

            renderY += 20;
        }
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        List<StatProviderAndFilterPair> filterPairs = new ArrayList<>();

        for (AttackSpeedCheckbox attackSpeedCheckbox : attackSpeedCheckboxes) {
            StatProviderAndFilterPair filterPair = attackSpeedCheckbox.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof AttackSpeedStatProvider)
                .map(statProvider -> (AttackSpeedStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get attack speed stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        attackSpeedCheckboxes.forEach(attackSpeedCheckbox -> attackSpeedCheckbox.updateStateFromQuery(searchQuery));
    }

    private static class AttackSpeedCheckbox extends GuideFilterCheckbox<AttackSpeedStatProvider> {
        private final GearAttackSpeed gearAttackSpeed;

        protected AttackSpeedCheckbox(GearAttackSpeed gearAttackSpeed, ItemSearchQuery searchQuery) {
            super(EnumUtils.toNiceString(gearAttackSpeed));

            this.gearAttackSpeed = gearAttackSpeed;
            updateStateFromQuery(searchQuery);
        }

        @Override
        public void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            selected = searchQuery.filters().values().stream()
                    .filter(filterPair -> filterPair.statProvider() instanceof AttackSpeedStatProvider)
                    .anyMatch(filterPair -> filterPair
                            .statFilter()
                            .matches(EnumUtils.toNiceString(gearAttackSpeed).replace(" ", "_")));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(AttackSpeedStatProvider provider) {
            if (!selected) return null;

            Optional<StringStatFilter> statFilterOpt = new StringStatFilter.StringStatFilterFactory()
                    .create(EnumUtils.toNiceString(gearAttackSpeed).replace(" ", "_"));

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
