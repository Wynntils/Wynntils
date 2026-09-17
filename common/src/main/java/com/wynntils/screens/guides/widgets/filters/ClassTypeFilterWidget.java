/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.statproviders.ClassStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
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

public class ClassTypeFilterWidget extends GuideFilterWidget {
    private final List<ClassTypeButton> classTypeButtons = new ArrayList<>();
    private ClassStatProvider provider;

    public ClassTypeFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(70, containerWidget);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Class")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        classTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (ClassTypeButton classTypeButton : classTypeButtons) {
            if (classTypeButton.isMouseOver(event.x(), event.y())) {
                clicked = classTypeButton.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        classTypeButtons.clear();

        classTypeButtons.add(new ClassTypeButton(ClassType.WARRIOR, Texture.SPEAR_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(ClassType.MAGE, Texture.WAND_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(ClassType.ASSASSIN, Texture.DAGGER_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(ClassType.ARCHER, Texture.BOW_FILTER_ICON, searchQuery));
        classTypeButtons.add(new ClassTypeButton(ClassType.SHAMAN, Texture.RELIK_FILTER_ICON, searchQuery));

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (classTypeButtons == null) return;

        int renderX = getX();
        int renderY = getY() + 10;
        for (int i = 0; i < classTypeButtons.size(); i++) {
            classTypeButtons.get(i).setPosition(renderX, renderY);

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

        for (ClassTypeButton classTypeButton : classTypeButtons) {
            StatProviderAndFilterPair filterPair = classTypeButton.getFilterPair(provider);

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof ClassStatProvider)
                .map(statProvider -> (ClassStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get class stat provider"));
        return provider;
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        classTypeButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class ClassTypeButton extends GuideFilterButton<ClassStatProvider> {
        private final ClassType classType;

        protected ClassTypeButton(ClassType classType, Texture texture, ItemSearchQuery searchQuery) {
            super(0, 0, 64, 16, texture);

            this.classType = classType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics, texture, getX(), getY());

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(classType.getName())
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
                    .filter(filterPair -> filterPair.statProvider() instanceof ClassStatProvider)
                    .anyMatch(filterPair -> filterPair.statFilter().matches(classType.getName()));
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(ClassStatProvider provider) {
            if (!state) return null;

            Optional<StringStatFilter> statFilterOpt =
                    new StringStatFilter.StringStatFilterFactory().create(classType.getName());

            return statFilterOpt
                    .map(stringStatFilter -> new StatProviderAndFilterPair(provider, stringStatFilter))
                    .orElse(null);
        }
    }
}
