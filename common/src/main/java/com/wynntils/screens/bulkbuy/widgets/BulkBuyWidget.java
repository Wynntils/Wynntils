/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.bulkbuy.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class BulkBuyWidget extends AbstractWidget {
    private final BulkBuyFeature.BulkBoughtItem bulkBoughtItem;

    public BulkBuyWidget(int x, int y, int width, int height, BulkBuyFeature.BulkBoughtItem bulkBoughtItem) {
        super(x, y, width, height, Component.literal("Bulk Buy Widget"));
        this.bulkBoughtItem = bulkBoughtItem;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics.pose(), CustomColor.fromHexString("#9c784b"), getX(), getY(), 0, getWidth(), getHeight());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("Currently Buying: "),
                        getX() + 102,
                        getY() + 20,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        if (bulkBoughtItem == null) return;
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(
                                bulkBoughtItem.getItemStack().getHoverName().getString()),
                        getX() + 102,
                        getY() + 30,
                        getWidth() - 20,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("Amount: " + bulkBoughtItem.getAmount()),
                        getX() + 102,
                        getY() + 40,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        guiGraphics.renderItem(bulkBoughtItem.getItemStack(), getX() + 50, getY() + 30);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
