/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemListWidget extends AbstractWidget {
    public static final int ITEM_SIZE = 16;
    public static final int ITEM_SPACING = ITEM_SIZE + 2;
    private List<? extends ListItem> items;
    private ListItem hovered = null;
    private int page = 0;
    private boolean isCtrl = false;

    public ItemListWidget(int x, int y, int width, int height, List<? extends ListItem> items) {
        super(x, y, width, height, Component.empty());
        this.items = items;
        setPage(0);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(getMessage()),
                        getX() + getWidth() / 2,
                        getY(),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);

        hovered = null;
        for (int i = 0; i < getRows() * getColumns() && i < items.size(); i++) {
            ListItem listItem = items.get(i);
            int x = i % getColumns() * ITEM_SPACING + getX();
            int y = i / getColumns() * ITEM_SPACING + getListStartY();

            if (hovered == null && isHovered && isHovering(x, y, ITEM_SIZE, ITEM_SIZE, mouseX, mouseY)) {
                hovered = listItem;
                GuiComponent.fill(poseStack, x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0, 0x80ffffff);
            }

            McUtils.mc().getItemRenderer().renderAndDecorateItem(poseStack, listItem.getItemStack(), x, y);
            McUtils.mc()
                    .getItemRenderer()
                    .renderGuiItemDecorations(poseStack, McUtils.mc().font, listItem.getItemStack(), x, y);
        }
        if (hovered != null) {
            McUtils.mc()
                    .screen
                    .renderTooltip(
                            poseStack, hovered.getTooltip(), hovered.itemStack.getTooltipImage(), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public void playDownSound(SoundManager handler) {}

    private boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void setItems(List<? extends ListItem> items) {
        this.items = items;
        hovered = null;
        setPage(getPage());
    }

    public List<? extends ListItem> getItems() {
        return items;
    }

    public int getColumns() {
        return getWidth() / ITEM_SPACING;
    }

    public int getRows() {
        return (getHeight() - McUtils.mc().font.lineHeight + 2) / ITEM_SPACING;
    }

    public int getListStartY() {
        return getY() + McUtils.mc().font.lineHeight + 2;
    }

    public void setPage(int page) {
        page %= getPages() + 1;
        setMessage(Component.literal("Page: " + (page + 1) + "/" + (getPages() + 1) + ", Items: " + items.size()));
        this.page = page;
    }

    private int getPages() {
        return items.size() / (getRows() * getColumns());
    }

    public int getPage() {
        return page;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setPage(getPages() + delta > 0 ? 1 : -1);
        return true;
    }

    public abstract static class ListItem {
        private final ItemStack itemStack;

        protected ListItem(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public List<Component> getTooltip() {
            return McUtils.mc().screen.getTooltipFromItem(itemStack);
        }

        public abstract void onClick();
    }
}
