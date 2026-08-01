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
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.filters.BooleanStatFilter;
import com.wynntils.services.itemfilter.statproviders.ProfessionStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.OptionalBoolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

public class ProfessionTypeFilterWidget extends GuideFilterWidget {
    private final boolean gatheringTypes;
    private final List<ProfessionTypeButton> professionTypeButtons = new ArrayList<>();
    private Map<ProfessionType, ProfessionStatProvider> professionProviderMap;

    public ProfessionTypeFilterWidget(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, boolean gatheringTypes) {
        super(gatheringTypes ? 50 : 90, containerWidget);

        this.gatheringTypes = gatheringTypes;
        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal("Profession Type")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX(),
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        professionTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean clicked = false;

        for (ProfessionTypeButton professionTypeButton : professionTypeButtons) {
            if (professionTypeButton.isMouseOver(event.x(), event.y())) {
                clicked = professionTypeButton.mouseClicked(event, isDoubleClick);
                break;
            }
        }

        containerWidget.updateSearchFromQuickFilters();

        return clicked;
    }

    @Override
    protected void rebuildWidgets(ItemSearchQuery searchQuery) {
        professionTypeButtons.clear();

        if (gatheringTypes) {
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.FISHING, Texture.FISHING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.WOODCUTTING, Texture.WOODCUTTING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.MINING, Texture.MINING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.FARMING, Texture.FARMING_FILTER_ICON, searchQuery));
        } else {
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.ALCHEMISM, Texture.ALCHEMISM_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.ARMOURING, Texture.ARMOURING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.COOKING, Texture.COOKING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.JEWELING, Texture.JEWELING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.SCRIBING, Texture.SCRIBING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.TAILORING, Texture.TAILORING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(new ProfessionTypeButton(
                    ProfessionType.WEAPONSMITHING, Texture.WEAPONSMITHING_FILTER_ICON, searchQuery));
            professionTypeButtons.add(
                    new ProfessionTypeButton(ProfessionType.WOODWORKING, Texture.WOODWORKING_FILTER_ICON, searchQuery));
        }

        updateWidgetPositions();
    }

    @Override
    protected void updateWidgetPositions() {
        if (professionTypeButtons == null) return;

        int renderX = getX();
        int renderY = getY() + 10;
        for (int i = 0; i < professionTypeButtons.size(); i++) {
            professionTypeButtons.get(i).setPosition(renderX, renderY);

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

        for (ProfessionTypeButton professionTypeButton : professionTypeButtons) {
            StatProviderAndFilterPair filterPair =
                    professionTypeButton.getFilterPair(professionProviderMap.get(professionTypeButton.professionType));

            if (filterPair != null) {
                filterPairs.add(filterPair);
            }
        }

        return filterPairs;
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        professionProviderMap = new HashMap<>();

        Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof ProfessionStatProvider)
                .map(statProvider -> (ProfessionStatProvider) statProvider)
                .forEach(professionStatProvider -> {
                    ProfessionType type = ProfessionType.fromString(professionStatProvider.getDisplayName());
                    professionProviderMap.put(type, professionStatProvider);
                });

        return professionProviderMap.values().stream().findFirst().orElseThrow();
    }

    @Override
    public java.util.List<ItemStatProvider<?>> getProviders() {
        return new java.util.ArrayList<>(professionProviderMap.values());
    }

    @Override
    public void updateFromQuery(ItemSearchQuery searchQuery) {
        professionTypeButtons.forEach(professionTypeButton -> professionTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class ProfessionTypeButton extends GuideFilterButton<ProfessionStatProvider> {
        private final ProfessionType professionType;
        private OptionalBoolean state;

        protected ProfessionTypeButton(ProfessionType professionType, Texture texture, ItemSearchQuery searchQuery) {
            super(0, 0, 64, 16, texture);

            this.professionType = professionType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics, texture, getX(), getY());

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(EnumUtils.toNiceString(professionType))
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                            getX() + 18,
                            getY() + 8,
                            getWidth() - 20,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (!isHovered && state == OptionalBoolean.NULL) return;

            CustomColor color = CommonColors.WHITE;

            if (state == OptionalBoolean.TRUE) {
                color = CommonColors.LIGHT_GREEN;
            } else if (state == OptionalBoolean.FALSE) {
                color = CommonColors.RED;
            }

            RenderUtils.drawRect(
                    guiGraphics, color.withAlpha(isHovered ? 0.7f : 0.5f), getX(), getY(), getWidth(), getHeight());

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
                                professionType.getDisplayName()));
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
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT || event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (state != OptionalBoolean.TRUE) {
                    state = OptionalBoolean.TRUE;
                } else if (state != OptionalBoolean.FALSE) {
                    state = OptionalBoolean.FALSE;
                }
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                state = OptionalBoolean.NULL;
            }

            return super.mouseClicked(event, isDoubleClick);
        }

        @Override
        protected void updateStateFromQuery(ItemSearchQuery searchQuery) {
            Optional<StatProviderAndFilterPair> filterPairOpt = searchQuery.filters().values().stream()
                    .filter(filterPair -> {
                        if (filterPair.statProvider() instanceof ProfessionStatProvider professionStatProvider) {
                            return professionStatProvider.getDisplayName().equals(professionType.getDisplayName());
                        }

                        return false;
                    })
                    .findFirst();

            if (filterPairOpt.isPresent()) {
                if (filterPairOpt.get().statFilter().matches(true)) {
                    state = OptionalBoolean.TRUE;
                } else if (filterPairOpt.get().statFilter().matches(false)) {
                    state = OptionalBoolean.FALSE;
                }
            } else {
                state = OptionalBoolean.NULL;
            }
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(ProfessionStatProvider provider) {
            if (state == OptionalBoolean.NULL) return null;

            return new StatProviderAndFilterPair(
                    provider,
                    new BooleanStatFilter.BooleanStatFilterFactory().fromBoolean(state == OptionalBoolean.TRUE));
        }
    }
}
