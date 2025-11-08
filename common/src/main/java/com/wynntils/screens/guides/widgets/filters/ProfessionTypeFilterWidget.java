/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.BooleanStatFilter;
import com.wynntils.services.itemfilter.statproviders.ProfessionStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.ConfirmedBoolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProfessionTypeFilterWidget extends GuideFilterWidget {
    private final List<ProfessionTypeButton> professionTypeButtons = new ArrayList<>();
    private Map<ProfessionType, ProfessionStatProvider> professionProviderMap;

    public ProfessionTypeFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 76, 36, guideScreen);

        professionTypeButtons.add(
                new ProfessionTypeButton(x, y, ProfessionType.ALCHEMISM, Texture.ALCHEMISM_FILTER_ICON, searchQuery));
        professionTypeButtons.add(new ProfessionTypeButton(
                x + 20, y, ProfessionType.ARMOURING, Texture.ARMOURING_FILTER_ICON, searchQuery));
        professionTypeButtons.add(
                new ProfessionTypeButton(x + 40, y, ProfessionType.COOKING, Texture.COOKING_FILTER_ICON, searchQuery));
        professionTypeButtons.add(new ProfessionTypeButton(
                x + 60, y, ProfessionType.JEWELING, Texture.JEWELING_FILTER_ICON, searchQuery));
        professionTypeButtons.add(new ProfessionTypeButton(
                x, y + 20, ProfessionType.SCRIBING, Texture.SCRIBING_FILTER_ICON, searchQuery));
        professionTypeButtons.add(new ProfessionTypeButton(
                x + 20, y + 20, ProfessionType.TAILORING, Texture.TAILORING_FILTER_ICON, searchQuery));
        professionTypeButtons.add(new ProfessionTypeButton(
                x + 40, y + 20, ProfessionType.WEAPONSMITHING, Texture.WEAPONSMITHING_FILTER_ICON, searchQuery));
        professionTypeButtons.add(new ProfessionTypeButton(
                x + 60, y + 20, ProfessionType.WOODWORKING, Texture.WOODWORKING_FILTER_ICON, searchQuery));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        professionTypeButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = false;

        for (ProfessionTypeButton professionTypeButton : professionTypeButtons) {
            if (professionTypeButton.isMouseOver(mouseX, mouseY)) {
                clicked = professionTypeButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }

        guideScreen.updateSearchFromQuickFilters();

        return clicked;
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
    public void getProvider() {
        professionProviderMap = new HashMap<>();

        Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof ProfessionStatProvider)
                .map(statProvider -> (ProfessionStatProvider) statProvider)
                .forEach(professionStatProvider -> {
                    ProfessionType type = ProfessionType.fromString(professionStatProvider.getDisplayName());
                    professionProviderMap.put(type, professionStatProvider);
                });
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        professionTypeButtons.forEach(classTypeButton -> classTypeButton.updateStateFromQuery(searchQuery));
    }

    private static class ProfessionTypeButton extends GuideFilterButton<ProfessionStatProvider> {
        private final ProfessionType professionType;
        private ConfirmedBoolean state;

        protected ProfessionTypeButton(
                int x, int y, ProfessionType professionType, Texture texture, ItemSearchQuery searchQuery) {
            super(x, y, texture);

            this.professionType = professionType;
            updateStateFromQuery(searchQuery);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawTexturedRect(guiGraphics.pose(), texture, getX(), getY());

            if (!isHovered && state == ConfirmedBoolean.UNCONFIRMED) return;

            CustomColor color = CommonColors.WHITE;

            if (state == ConfirmedBoolean.TRUE) {
                color = CommonColors.LIGHT_GREEN;
            } else if (state == ConfirmedBoolean.FALSE) {
                color = CommonColors.RED;
            }

            RenderUtils.drawRect(
                    guiGraphics.pose(),
                    color.withAlpha(isHovered ? 0.7f : 0.5f),
                    getX(),
                    getY(),
                    0,
                    getWidth(),
                    getHeight());

            if (isHovered) {
                McUtils.screen()
                        .setTooltipForNextRenderPass(Lists.transform(
                                ComponentUtils.wrapTooltips(
                                        List.of(Component.translatable(
                                                "screens.wynntils.wynntilsGuides.filterWidget.tooltip",
                                                getFilterName())),
                                        200),
                                Component::getVisualOrderText));
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (state != ConfirmedBoolean.TRUE) {
                    state = ConfirmedBoolean.TRUE;
                } else if (state != ConfirmedBoolean.FALSE) {
                    state = ConfirmedBoolean.FALSE;
                }
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                state = ConfirmedBoolean.UNCONFIRMED;
            }

            return super.mouseClicked(mouseX, mouseY, button);
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
                    state = ConfirmedBoolean.TRUE;
                } else if (filterPairOpt.get().statFilter().matches(false)) {
                    state = ConfirmedBoolean.FALSE;
                }
            } else {
                state = ConfirmedBoolean.UNCONFIRMED;
            }
        }

        @Override
        protected StatProviderAndFilterPair getFilterPair(ProfessionStatProvider provider) {
            if (state == ConfirmedBoolean.UNCONFIRMED) return null;

            return new StatProviderAndFilterPair(
                    provider,
                    new BooleanStatFilter.BooleanStatFilterFactory().fromBoolean(state == ConfirmedBoolean.TRUE));
        }

        @Override
        protected String getFilterName() {
            return professionType.getDisplayName();
        }
    }
}
