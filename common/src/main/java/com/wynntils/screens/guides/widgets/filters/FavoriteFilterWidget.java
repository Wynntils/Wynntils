/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.filters.BooleanStatFilter;
import com.wynntils.services.itemfilter.statproviders.FavoriteStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ConfirmedBoolean;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class FavoriteFilterWidget extends GuideFilterWidget {
    private ConfirmedBoolean state;
    private FavoriteStatProvider provider;

    public FavoriteFilterWidget(int x, int y, WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(x, y, 16, 16, guideScreen);

        updateFromQuery(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics.pose(),
                CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f),
                getX(),
                getY(),
                0,
                getWidth(),
                getHeight());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(state == ConfirmedBoolean.UNCONFIRMED ? "☆" : "★"),
                        getX() + getWidth() / 2f,
                        getY() + getHeight() / 2f + 1,
                        getStarColor(),
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        if (isHovered) {
            McUtils.screen()
                    .setTooltipForNextRenderPass(Lists.transform(
                            ComponentUtils.wrapTooltips(
                                    List.of(Component.translatable(
                                            "screens.wynntils.wynntilsGuides.filterWidget.tooltip",
                                            I18n.get("service.wynntils.itemFilter.stat.favorite.name"))),
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
    protected List<StatProviderAndFilterPair> getFilters() {
        if (state == ConfirmedBoolean.UNCONFIRMED) return List.of();

        return List.of(new StatProviderAndFilterPair(
                provider,
                new BooleanStatFilter.BooleanStatFilterFactory().fromBoolean(state == ConfirmedBoolean.TRUE)));
    }

    @Override
    public void getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof FavoriteStatProvider)
                .map(statProvider -> (FavoriteStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get favorite stat provider"));
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        Optional<StatProviderAndFilterPair> filterPairOpt = searchQuery.filters().values().stream()
                .filter(filterPair -> filterPair.statProvider() instanceof FavoriteStatProvider)
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

    private CustomColor getStarColor() {
        return switch (state) {
            case UNCONFIRMED -> CommonColors.WHITE;
            case TRUE -> CommonColors.YELLOW;
            case FALSE -> CommonColors.RED;
        };
    }
}
