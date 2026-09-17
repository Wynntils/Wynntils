/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.SortDirection;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class GuideSortWidget extends AbstractWidget {
    private final GuideContainerWidget<?> containerWidget;
    private final ItemStatProvider<?> provider;
    private SortDirection sortDirection;

    public GuideSortWidget(
            GuideContainerWidget<?> containerWidget, ItemStatProvider<?> provider, ItemSearchQuery query) {
        super(0, 0, 128, 20, Component.empty());
        this.containerWidget = containerWidget;
        this.provider = provider;
        updateFromQuery(query);
    }

    @Override
    protected void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics,
                (isHovered ? CommonColors.LIGHT_GRAY : CommonColors.GRAY).withAlpha(0.5f),
                getX(),
                getY(),
                getWidth(),
                getHeight());

        String state = sortDirection == null ? "-" : sortDirection == SortDirection.DESCENDING ? "▼" : "▲";
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromComponent(Component.literal(provider.getDisplayName())
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))),
                        getX() + 4,
                        getY() + height / 2f,
                        width - 20,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1f);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(state),
                        getX() + width - 6,
                        getY() + height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (isHovered) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (sortDirection == null) {
            sortDirection = SortDirection.DESCENDING;
        } else if (sortDirection == SortDirection.DESCENDING) {
            sortDirection = SortDirection.ASCENDING;
        } else {
            sortDirection = null;
        }
        containerWidget.updateSearchFromQuickFilters();
        return true;
    }

    public Optional<SortInfo> getSortInfo() {
        return sortDirection == null ? Optional.empty() : Optional.of(new SortInfo(sortDirection, provider));
    }

    public ItemStatProvider<?> getProvider() {
        return provider;
    }

    public void updateFromQuery(ItemSearchQuery query) {
        sortDirection = query.sorts().stream()
                .filter(sort -> sort.provider().getName().equals(provider.getName()))
                .findFirst()
                .map(SortInfo::direction)
                .orElse(null);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
