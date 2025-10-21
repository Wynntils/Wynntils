/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.models.activities.type.ActivitySortOrder;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class SortOrderWidget extends WynntilsButton implements TooltipProvider {
    private final SortableActivityScreen sortableActivityScreen;

    public SortOrderWidget(int x, int y, int width, int height, SortableActivityScreen sortableActivityScreen) {
        super(x, y, width, height, Component.literal("Sort Order Button"));
        this.sortableActivityScreen = sortableActivityScreen;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        ActivitySortOrder activitySortOrder = sortableActivityScreen.getActivitySortOrder();

        ActivitySortOrder[] activitySortOrders = ActivitySortOrder.values();

        ActivitySortOrder newSort = activitySortOrders[(activitySortOrder.ordinal() + 1) % activitySortOrders.length];

        sortableActivityScreen.setActivitySortOrder(newSort);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //        PoseStack poseStack = guiGraphics.pose();

        Texture sortTexture =
                switch (sortableActivityScreen.getActivitySortOrder()) {
                    case LEVEL -> Texture.SORT_LEVEL_OFFSET;
                    case DISTANCE -> Texture.SORT_DISTANCE_OFFSET;
                    case ALPHABETIC -> Texture.SORT_ALPHABETICALLY_OFFSET;
                };

        float renderX = this.getX();
        float renderY = this.getY();

        if (this.isHovered) {
            //            RenderUtils.drawTexturedRect(
            //                    poseStack,
            //                    sortTexture.resource(),
            //                    renderX,
            //                    renderY,
            //                    0,
            //                    this.width,
            //                    this.height,
            //                    0,
            //                    sortTexture.height() / 2,
            //                    sortTexture.width(),
            //                    sortTexture.height() / 2,
            //                    sortTexture.width(),
            //                    sortTexture.height());
        } else {
            //            RenderUtils.drawTexturedRect(
            //                    poseStack,
            //                    sortTexture.resource(),
            //                    renderX,
            //                    renderY,
            //                    0,
            //                    this.width,
            //                    this.height,
            //                    0,
            //                    0,
            //                    sortTexture.width(),
            //                    sortTexture.height() / 2,
            //                    sortTexture.width(),
            //                    sortTexture.height());
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        ActivitySortOrder activitySortOrder = sortableActivityScreen.getActivitySortOrder();
        return List.of(
                Component.translatable("screens.wynntils.wynntilsContentBook.sort.%s.name"
                        .formatted(activitySortOrder.name().toLowerCase(Locale.ROOT))),
                Component.translatable("screens.wynntils.wynntilsContentBook.sort.%s.description"
                        .formatted(activitySortOrder.name().toLowerCase(Locale.ROOT))));
    }
}
