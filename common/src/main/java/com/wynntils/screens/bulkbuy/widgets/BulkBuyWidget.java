/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.bulkbuy.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BulkBuyWidget extends AbstractWidget {
    private static final int BULK_BUY_WIDGET_CENTER = 89;
    private ItemStack bulkBoughtItemStack = null;
    private int bulkBoughtAmount = 0;
    private int bulkBoughtPrice = 0;

    public BulkBuyWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Bulk Buy Widget"));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.BULK_BUY_PANEL, getX(), getY());

        // bulkBoughtItemStack is null when there is no item being bulk bought
        if (bulkBoughtItemStack == null) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            StyledText.fromString(I18n.get("feature.wynntils.bulkBuy.widget.bulkBuy")),
                            getX() + BULK_BUY_WIDGET_CENTER,
                            getY() + 54,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            StyledText.fromString(I18n.get("feature.wynntils.bulkBuy.widget.idle")),
                            getX() + BULK_BUY_WIDGET_CENTER,
                            getY() + 65,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
            return;
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(I18n.get("feature.wynntils.bulkBuy.widget.currentlyBuying")),
                        getX() + BULK_BUY_WIDGET_CENTER,
                        getY() + 29,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        // X coordinate is center of widget (BULK_BUY_WIDGET_CENTER) minus half of the item icon width (8)
        guiGraphics.renderItem(bulkBoughtItemStack, getX() + BULK_BUY_WIDGET_CENTER - 8, getY() + 34);
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(bulkBoughtItemStack.getHoverName().getString()),
                        getX() + BULK_BUY_WIDGET_CENTER,
                        getY() + 63,
                        getWidth() - 20,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(I18n.get("feature.wynntils.bulkBuy.widget.amount", bulkBoughtAmount)),
                        getX() + BULK_BUY_WIDGET_CENTER,
                        getY() + 79,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(I18n.get(
                                "feature.wynntils.bulkBuy.widget.totalPrice", (bulkBoughtAmount * bulkBoughtPrice))),
                        getX() + BULK_BUY_WIDGET_CENTER,
                        getY() + 89,
                        CommonColors.LIGHT_GREEN,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(I18n.get("feature.wynntils.bulkBuy.widget.closeCancel")),
                        getX() + BULK_BUY_WIDGET_CENTER,
                        getY() + 99,
                        CommonColors.GRAY,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
    }

    public void setBulkBoughtItemStack(ItemStack bulkBoughtItemStack) {
        this.bulkBoughtItemStack = bulkBoughtItemStack;
    }

    public void setBulkBoughtAmount(int bulkBoughtAmount) {
        this.bulkBoughtAmount = bulkBoughtAmount;
    }

    public void setBulkBoughtPrice(int bulkBoughtPrice) {
        this.bulkBoughtPrice = bulkBoughtPrice;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
