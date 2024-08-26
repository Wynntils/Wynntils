/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.bulkbuy.widgets;

import com.wynntils.core.text.StyledText;
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
import net.minecraft.world.item.ItemStack;

public class BulkBuyWidget extends AbstractWidget {
    private ItemStack bulkBoughtItemStack = null;
    private int bulkBoughtAmount = 0;
    private int bulkBoughtPrice = 0;

    public BulkBuyWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Bulk Buy Widget"));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics.pose(), CustomColor.fromHexString("#9c784b"), getX(), getY(), 0, getWidth(), getHeight());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("Currently Buying: "),
                        getX() + 75,
                        getY() + 20,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        // bulkBoughtItemStack is null when there is no item being bulk bought
        if (bulkBoughtItemStack == null) return;
        guiGraphics.renderItem(bulkBoughtItemStack, getX() + 67, getY() + 24);
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromString(bulkBoughtItemStack.getHoverName().getString()),
                        getX() + 75,
                        getY() + 50,
                        getWidth() - 20,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("Amount: " + bulkBoughtAmount),
                        getX() + 75,
                        getY() + 70,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("Total Price: " + (bulkBoughtAmount * bulkBoughtPrice) + "²"),
                        getX() + 75,
                        getY() + 80,
                        CommonColors.LIGHT_GREEN,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString("Close shop to cancel"),
                        getX() + 75,
                        getY() + 90,
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
