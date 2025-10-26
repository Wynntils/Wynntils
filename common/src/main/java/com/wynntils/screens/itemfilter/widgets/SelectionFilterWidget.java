/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.services.itemfilter.filters.StringStatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class SelectionFilterWidget extends GeneralFilterWidget {
    private static final CustomColor UNUSED_COLOR = new CustomColor(116, 0, 0, 255);
    private static final CustomColor UNUSED_COLOR_BORDER = new CustomColor(220, 0, 0, 255);
    private static final CustomColor USED_COLOR = new CustomColor(0, 116, 0, 255);
    private static final CustomColor USED_COLOR_BORDER = new CustomColor(0, 220, 0, 255);

    private final String valueName;
    private final WynntilsCheckbox usedCheckbox;

    private boolean used;

    public SelectionFilterWidget(
            int x,
            int y,
            int width,
            int height,
            String valueName,
            Optional<StatProviderAndFilterPair> filterPair,
            ProviderFilterListWidget parent) {
        super(x, y, width, height, Component.literal("Selection Filter Widget"), parent);

        this.valueName = valueName;

        used = filterPair.isPresent();

        this.usedCheckbox =
                new WynntilsCheckbox(x + width - 16, y + 2, 16, Component.literal(""), used, 0, (c, b) -> toggleUsed());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(guiGraphics, getRectColor().withAlpha(100), getX(), getY(), width - 18, height);

        RenderUtils.drawRectBorders(
                guiGraphics, getBorderColor(), getX(), getY(), getX() + width - 18, getY() + height, 2);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(valueName),
                        getX() + 2,
                        getY() + (height / 2f),
                        width - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.0f);

        usedCheckbox.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (usedCheckbox.isMouseOver(event.x(), event.y())) {
            return usedCheckbox.mouseClicked(event, isDoubleClick);
        }

        return false;
    }

    @Override
    public void updateY(int y) {
        setY(y);

        usedCheckbox.setY(y + 2);
    }

    @Override
    protected StatProviderAndFilterPair getFilterPair() {
        if (!used) return null;

        Optional<StringStatFilter> statFilterOpt = new StringStatFilter.StringStatFilterFactory().create(valueName);

        return statFilterOpt
                .map(stringStatFilter -> new StatProviderAndFilterPair(parent.getProvider(), stringStatFilter))
                .orElse(null);
    }

    private void toggleUsed() {
        used = !used;

        parent.updateQuery();
    }

    private CustomColor getRectColor() {
        return used ? USED_COLOR : UNUSED_COLOR;
    }

    private CustomColor getBorderColor() {
        return used ? USED_COLOR_BORDER : UNUSED_COLOR_BORDER;
    }
}
