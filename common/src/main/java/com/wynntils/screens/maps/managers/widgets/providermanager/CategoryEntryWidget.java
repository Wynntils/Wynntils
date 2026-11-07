/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CategoryEntryWidget extends AbstractWidget implements TooltipProvider {
    private static final int PADDING = 4;
    private static final int ICON_BORDER = 2;
    private static final int TEXT_GAP = 6;

    private final MapCategory mapCategory;
    private final String categoryId;
    private final Optional<String> categoryName;
    private final MapIcon icon;
    private final String iconId;
    private final CustomColor iconColor;

    private List<Component> generatedTooltip = new ArrayList<>();

    public CategoryEntryWidget(int x, int y, int width, int height, MapCategory mapCategory) {
        super(x, y, width, height, Component.literal(mapCategory.getCategoryId()));
        this.mapCategory = mapCategory;
        this.categoryId = mapCategory.getCategoryId();
        this.categoryName = mapCategory.getName();

        this.iconId = resolveAttribute(MapAttributes::getIconId).orElse(MapIcon.NO_ICON_ID);
        this.iconColor = resolveAttribute(MapAttributes::getIconColor).orElse(CommonColors.WHITE);

        this.icon = this.iconId.equals(MapIcon.NO_ICON_ID)
                ? null
                : Services.MapData.getIcon(this.iconId).orElse(null);

        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, getX(), getY(), this.width, this.height);

        int textX;

        if (icon != null) {
            int iconBoxSize = this.height - PADDING * 2;
            int iconBoxX = getX() + PADDING;
            int iconBoxY = getY() + PADDING;

            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxSize, iconBoxSize);

            int iconWidth = icon.getWidth();
            int iconHeight = icon.getHeight();

            float scale = Math.min(1f, (float) (iconBoxSize - ICON_BORDER * 2) / Math.max(iconWidth, iconHeight));
            int drawWidth = Math.max(1, Math.round(iconWidth * scale));
            int drawHeight = Math.max(1, Math.round(iconHeight * scale));

            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    icon.getIdentifier(),
                    iconColor,
                    iconBoxX + (iconBoxSize - drawWidth) / 2f,
                    iconBoxY + (iconBoxSize - drawHeight) / 2f,
                    drawWidth,
                    drawHeight,
                    iconWidth,
                    iconHeight);

            textX = iconBoxX + iconBoxSize + TEXT_GAP;
        } else {
            textX = getX() + TEXT_GAP;
        }

        int maxTextWidth = getX() + this.width - textX - PADDING;

        Component title = categoryName.isPresent()
                ? Component.literal(categoryName.get())
                : Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.noCategoryName");

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(title),
                        textX,
                        getY() + 9,
                        maxTextWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(categoryId).withStyle(ChatFormatting.GRAY)),
                        textX,
                        getY() + this.height - 9,
                        maxTextWidth,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            this.playDownSound(McUtils.mc().getSoundManager());

            McUtils.setScreen(CategoryManagementScreen.create(
                    McUtils.mc().screen, categoryId, OverrideType.MAP_LOCATION_OVERRIDE));

            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    private <T> Optional<T> resolveAttribute(Function<MapAttributes, Optional<T>> getter) {
        return Services.MapData.getOwnAttributesForCategory(categoryId)
                .flatMap(getter)
                .or(() -> Services.MapData.getInheritedAttributesForCategory(categoryId)
                        .flatMap(getter));
    }

    private void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.literal(categoryId).withStyle(ChatFormatting.GOLD));

        categoryName.ifPresent(name -> addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.name"), name));

        resolveAttribute(MapAttributes::getLabel)
                .ifPresent(label -> addLine(
                        Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.label"),
                        label));

        if (!iconId.equals(MapIcon.NO_ICON_ID)) {
            MutableComponent iconValue = Component.literal(iconId).withStyle(ChatFormatting.WHITE);

            if (icon == null) {
                iconValue.append(
                        Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.notFound")
                                .withStyle(ChatFormatting.RED));
            }

            addLine(
                    Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.iconId"),
                    iconValue);
        }

        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(Component.empty()
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable(
                                "screens.wynntils.map.managers.providerManager.entryWidgets.rightClickToEditCategory")
                        .withStyle(ChatFormatting.GREEN)));
    }

    private void addLine(Component label, String value) {
        addLine(label, Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    private void addLine(Component label, Component value) {
        this.generatedTooltip.add(Component.literal("- ")
                .withStyle(ChatFormatting.GOLD)
                .append(label.copy().withStyle(ChatFormatting.GRAY))
                .append(value));
    }
}
