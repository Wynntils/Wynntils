/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.SortDirection;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.services.itemfilter.type.StatValue;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ProviderButton extends WynntilsButton {
    private static final CustomColor ENABLED_COLOR = new CustomColor(0, 220, 0, 255);
    private static final CustomColor ENABLED_COLOR_BORDER = new CustomColor(0, 116, 0, 255);
    private static final CustomColor ENABLED_COLOR_BORDER_HOVERED = new CustomColor(0, 66, 0, 255);

    private static final CustomColor DISABLED_COLOR = new CustomColor(255, 0, 0, 255);
    private static final CustomColor DISABLED_COLOR_BORDER = new CustomColor(120, 0, 0, 255);
    private static final CustomColor DISABLED_COLOR_BORDER_HOVERED = new CustomColor(70, 0, 0, 255);

    private static final Map<Class<?>, AnyStatFilters.AbstractAnyStatFilter> ANY_MAP = Map.of(
            String.class,
            new AnyStatFilters.AnyStringStatFilter(),
            Integer.class,
            new AnyStatFilters.AnyIntegerStatFilter(),
            CappedValue.class,
            new AnyStatFilters.AnyCappedValueStatFilter(),
            StatValue.class,
            new AnyStatFilters.AnyStatValueStatFilter());

    private final ItemFilterScreen filterScreen;
    private final ItemStatProvider<?> provider;
    private final List<Component> tooltip;

    public ProviderButton(
            int x, int y, int width, int height, ItemFilterScreen filterScreen, ItemStatProvider<?> provider) {
        super(x, y, width, height, Component.literal(provider.getTranslatedName()));
        this.filterScreen = filterScreen;
        this.provider = provider;

        // Boolean is currently the only stat type to not support "any" so don't
        // add the tooltip mentioning it
        if (provider.getType().equals(Boolean.class)) {
            this.tooltip = List.of(
                    Component.literal(provider.getDescription()),
                    Component.translatable("screens.wynntils.itemFilter.providerHelp2"));
        } else {
            this.tooltip = List.of(
                    Component.literal(provider.getDescription()),
                    Component.translatable("screens.wynntils.itemFilter.providerHelp1"),
                    Component.translatable("screens.wynntils.itemFilter.providerHelp2"));
        }
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(guiGraphics, getRectColor().withAlpha(100), getX(), getY(), width, height);

        RenderUtils.drawRectBorders(guiGraphics, getBorderColor(), getX(), getY(), getX() + width, getY() + height, 2);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(provider.getDisplayName()),
                        getX() + 2,
                        getY() + (height / 2f),
                        width - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.0f);

        // Don't want to display tooltip when the tile is outside the mask from the screen
        if (isHovered
                && (mouseY <= filterScreen.getProviderMaskTopY() || mouseY >= filterScreen.getProviderMaskBottomY())) {
            isHovered = false;
        }

        if (this.isHovered) {
            guiGraphics.setTooltipForNextFrame(Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Prevent interaction when the button is outside of the mask from the screen
        if ((event.y() <= filterScreen.getProviderMaskTopY() || event.y() >= filterScreen.getProviderMaskBottomY())) {
            return false;
        }

        if (filterScreen.inSortMode()) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                filterScreen.addSort(new SortInfo(SortDirection.ASCENDING, provider));
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                filterScreen.removeSort(provider);
            }
        } else {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                filterScreen.setSelectedProvider(provider);
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                filterScreen.setFiltersForProvider(provider, null);
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                AnyStatFilters.AbstractAnyStatFilter anyFilter = ANY_MAP.getOrDefault(provider.getType(), null);

                if (anyFilter != null) {
                    filterScreen.setFiltersForProvider(
                            provider, List.of(new StatProviderAndFilterPair(provider, anyFilter)));
                }
            }

            filterScreen.updateFilterWidget();
        }

        return true;
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    private CustomColor getRectColor() {
        if (McUtils.screen() instanceof ItemFilterScreen itemFilterScreen) {
            if (itemFilterScreen.getSelectedProvider() == provider) {
                return CommonColors.GRAY;
            }
        }

        return filterScreen.isProviderInUse(provider) ? ENABLED_COLOR : DISABLED_COLOR;
    }

    private CustomColor getBorderColor() {
        if (McUtils.screen() instanceof ItemFilterScreen itemFilterScreen) {
            if (itemFilterScreen.getSelectedProvider() == provider) {
                return isHovered ? CommonColors.LIGHT_GRAY : CommonColors.WHITE;
            }
        }

        return filterScreen.isProviderInUse(provider)
                ? (isHovered ? ENABLED_COLOR_BORDER_HOVERED : ENABLED_COLOR_BORDER)
                : (isHovered ? DISABLED_COLOR_BORDER_HOVERED : DISABLED_COLOR_BORDER);
    }
}
