package com.wynntils.screens.bulkbuy.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;

public class BulkBuyWidget extends AbstractWidget {

    private final LinkedHashMap<Integer, BulkBuyFeature.BulkBoughtItem> bulkBuyQueue;

    public BulkBuyWidget(int x, int y, int width, int height, LinkedHashMap<Integer, BulkBuyFeature.BulkBoughtItem> bulkBuyQueue) {
        super(x, y, width, height, Component.literal("Bulk Buy Widget"));
        this.bulkBuyQueue = bulkBuyQueue;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(guiGraphics.pose(), Texture.BANK_PANEL, getX(), getY());

        FontRenderer.getInstance().renderText(guiGraphics.pose(),
                StyledText.fromString("Currently Buying: "),
                getX() + 52,
                getY() + 20,
                CommonColors.WHITE,
                HorizontalAlignment.CENTER,
                VerticalAlignment.BOTTOM,
                TextShadow.NORMAL);
        if (bulkBuyQueue.firstEntry() == null) return;
        FontRenderer.getInstance().renderScrollingText(guiGraphics.pose(),
                StyledText.fromString(bulkBuyQueue.firstEntry().getValue().getItemStack().getDisplayName().getString()),
                getX() + 52,
                getY() + 30,
                getWidth() - 20,
                CommonColors.WHITE,
                HorizontalAlignment.CENTER,
                VerticalAlignment.BOTTOM,
                TextShadow.NORMAL);
        FontRenderer.getInstance().renderText(guiGraphics.pose(),
                StyledText.fromString("Amount: " + bulkBuyQueue.firstEntry().getValue().getAmount()),
                getX() + 52,
                getY() + 40,
                CommonColors.WHITE,
                HorizontalAlignment.CENTER,
                VerticalAlignment.BOTTOM,
                TextShadow.NORMAL);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
