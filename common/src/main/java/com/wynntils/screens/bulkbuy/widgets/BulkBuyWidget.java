/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.bulkbuy.widgets;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.AnimationPercentage;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.time.Duration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class BulkBuyWidget extends AbstractWidget {
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE =
            MultiBufferSource.immediate(new ByteBufferBuilder(256));

    private static final int BULK_BUY_WIDGET_CENTER = 89;

    private final int originalX;
    private final AnimationPercentage animationPercentage;
    private BulkBuyFeature.BulkBoughtItem bulkBoughtItem = null;

    public BulkBuyWidget(int x, int y, int width, int height, int animationDuration) {
        super(x, y, width, height, Component.literal("Bulk Buy Widget"));
        this.originalX = x;
        animationPercentage = new AnimationPercentage(
                () -> bulkBoughtItem != null, Duration.of(animationDuration, java.time.temporal.ChronoUnit.MILLIS));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.setX(originalX - (int) (getWidth() * animationPercentage.getAnimation()));
        // Prevent widget from rendering behind highlights
        RenderUtils.createRectMask(guiGraphics.pose(), originalX - getWidth(), getY(), getWidth(), getHeight());
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.BULK_BUY_PANEL, getX(), getY());

        // bulkBoughtItemStack is null when there is no item being bulk bought
        if (bulkBoughtItem == null) {
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
        } else {
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
            GuiGraphics itemRenderGuiGraphics = new GuiGraphics(McUtils.mc(), BUFFER_SOURCE);
            itemRenderGuiGraphics.renderItem(
                    bulkBoughtItem.itemStack(), getX() + BULK_BUY_WIDGET_CENTER - 8, getY() + 34);

            FontRenderer.getInstance()
                    .renderScrollingText(
                            guiGraphics.pose(),
                            StyledText.fromString(
                                    bulkBoughtItem.itemStack().getHoverName().getString()),
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
                            StyledText.fromString(
                                    I18n.get("feature.wynntils.bulkBuy.widget.amount", bulkBoughtItem.amount())),
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
                                    "feature.wynntils.bulkBuy.widget.totalPrice",
                                    (bulkBoughtItem.amount() * bulkBoughtItem.price()))),
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

        RenderUtils.clearMask();
    }

    public void setBulkBoughtItem(BulkBuyFeature.BulkBoughtItem bulkBoughtItem) {
        this.bulkBoughtItem = bulkBoughtItem;
    }

    public AnimationPercentage getAnimationPercentage() {
        return animationPercentage;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
