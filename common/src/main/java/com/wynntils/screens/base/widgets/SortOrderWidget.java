/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.models.content.type.ContentSortOrder;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

public class SortOrderWidget extends WynntilsButton {
    private final SortableContentScreen sortableContentScreen;

    public SortOrderWidget(int x, int y, int width, int height, SortableContentScreen sortableContentScreen) {
        super(x, y, width, height, Component.literal("Sort Order Button"));
        this.sortableContentScreen = sortableContentScreen;
    }

    @Override
    public void onPress() {
        ContentSortOrder contentSortOrder = sortableContentScreen.getContentSortOrder();

        ContentSortOrder[] contentSortOrders = ContentSortOrder.values();

        ContentSortOrder newSort = contentSortOrders[(contentSortOrder.ordinal() + 1) % contentSortOrders.length];

        sortableContentScreen.setContentSortOrder(newSort);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Texture sortTexture =
                switch (sortableContentScreen.getContentSortOrder()) {
                    case LEVEL -> Texture.SORT_LEVEL;
                    case DISTANCE -> Texture.SORT_DISTANCE;
                    case ALPHABETIC -> Texture.SORT_ALPHABETICALLY;
                };

        float renderX = this.getX();
        float renderY = this.getY();

        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    sortTexture.resource(),
                    renderX,
                    renderY,
                    0,
                    this.width,
                    this.height,
                    0,
                    sortTexture.height() / 2,
                    sortTexture.width(),
                    sortTexture.height() / 2,
                    sortTexture.width(),
                    sortTexture.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    sortTexture.resource(),
                    renderX,
                    renderY,
                    0,
                    this.width,
                    this.height,
                    0,
                    0,
                    sortTexture.width(),
                    sortTexture.height() / 2,
                    sortTexture.width(),
                    sortTexture.height());
        }
    }
}
