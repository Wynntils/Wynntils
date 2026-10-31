/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class IconEntryWidget extends AbstractWidget implements TooltipProvider {
    private static final int PADDING = 4;
    private static final int ICON_BORDER = 2;
    private static final int TEXT_GAP = 6;

    private final MapIcon mapIcon;
    private final String iconId;

    private List<Component> generatedTooltip = new ArrayList<>();

    public IconEntryWidget(int x, int y, int width, int height, MapIcon mapIcon) {
        super(x, y, width, height, Component.literal(mapIcon.getIconId()));
        this.mapIcon = mapIcon;
        this.iconId = mapIcon.getIconId();

        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND, getX(), getY(), this.width, this.height);

        int iconBoxSize = this.height - PADDING * 2;
        int iconBoxX = getX() + PADDING;
        int iconBoxY = getY() + PADDING;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_TEXT_BOX_BACKGROUND, iconBoxX, iconBoxY, iconBoxSize, iconBoxSize);

        int iconWidth = mapIcon.getWidth();
        int iconHeight = mapIcon.getHeight();

        float scale = Math.min(1f, (float) (iconBoxSize - ICON_BORDER * 2) / Math.max(iconWidth, iconHeight));
        int drawWidth = Math.max(1, Math.round(iconWidth * scale));
        int drawHeight = Math.max(1, Math.round(iconHeight * scale));

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                mapIcon.getIdentifier(),
                CommonColors.WHITE,
                iconBoxX + (iconBoxSize - drawWidth) / 2f,
                iconBoxY + (iconBoxSize - drawHeight) / 2f,
                drawWidth,
                drawHeight,
                iconWidth,
                iconHeight);

        int textX = iconBoxX + iconBoxSize + TEXT_GAP;

        int maxTextWidth = getX() + this.width - textX - PADDING;

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(iconId),
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
                        StyledText.fromComponent(Component.literal("%dx%d".formatted(iconWidth, iconHeight))
                                .withStyle(ChatFormatting.GRAY)),
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
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    private void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.literal(iconId).withStyle(ChatFormatting.GOLD));

        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.type"),
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.mapIcon")
                        .withStyle(ChatFormatting.WHITE));
        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.size"),
                "%d x %d".formatted(mapIcon.getWidth(), mapIcon.getHeight()));
        addLine(
                Component.translatable("screens.wynntils.map.managers.providerManager.entryWidgets.texture"),
                mapIcon.getIdentifier().toString());
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
