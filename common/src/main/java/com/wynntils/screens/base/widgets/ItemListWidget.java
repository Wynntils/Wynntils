package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemListWidget extends AbstractWidget {
    private List<ItemStack> items;
    private int page = 0;
    public ItemListWidget(int x, int y, int width, int height, List<ItemStack> items) {
        super(x, y, width, height, Component.literal("Page: 0"));
        this.items = items;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    private ItemStack getHovered(int mouseX, int mouseY) {
        return null;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setPage(int page) {
        setMessage(Component.literal("Page: " + page));
        this.page = page;
    }

    public int getPage() {
        return page;
    }
}
