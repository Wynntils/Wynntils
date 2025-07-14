/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.models.containers.containers.LootrunChestContainer;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class LootrunChestTextFeature extends Feature {
    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        if (!(Models.Container.getCurrentContainer() instanceof LootrunChestContainer container)) return;

        String title = event.getScreen().getTitle().getString();
        LootrunLocation location = LootrunLocation.fromContainerTitle(title);

        int sacrificedPulls = Models.Lootrun.getSacrificedPulls(location);

        AbstractContainerScreen<?> screen = event.getScreen();
        PoseStack poseStack = event.getPoseStack();

        // Position the text to the right side of the container
        String sacrificeText = "Sacrificed: " + sacrificedPulls;
        int textWidth = FontRenderer.getInstance().getFont().width(sacrificeText);
        int textHeight = FontRenderer.getInstance().getFont().lineHeight;

        int x = screen.leftPos + screen.imageWidth + 12; // To the right of container
        int y = screen.topPos + 20;

        // Background padding
        int padding = 4;
        int bgX = x - padding;
        int bgY = y - padding;
        int bgWidth = textWidth + (padding * 2);
        int bgHeight = textHeight + (padding * 2);

        poseStack.pushPose();
        poseStack.translate(0, 0, 200); // Z-level 200 for overlay

        // Draw semi-transparent background rectangle
        RenderUtils.drawRect(poseStack, CommonColors.BLACK.withAlpha(180), bgX, bgY, 0, bgWidth, bgHeight);

        // Draw border around background
        RenderUtils.drawRect(poseStack, CommonColors.ORANGE.withAlpha(200), bgX, bgY, 1, bgWidth, 1); // Top
        RenderUtils.drawRect(
                poseStack, CommonColors.ORANGE.withAlpha(200), bgX, bgY + bgHeight - 1, 1, bgWidth, 1); // Bottom
        RenderUtils.drawRect(poseStack, CommonColors.ORANGE.withAlpha(200), bgX, bgY, 1, 1, bgHeight); // Left
        RenderUtils.drawRect(
                poseStack, CommonColors.ORANGE.withAlpha(200), bgX + bgWidth - 1, bgY, 1, 1, bgHeight); // Right

        // Render the text
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(sacrificeText),
                        x,
                        y,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        poseStack.popPose();
    }
}
