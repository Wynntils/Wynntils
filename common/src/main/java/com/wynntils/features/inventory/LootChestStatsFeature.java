package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.CodedString;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.text.NumberFormat;
import java.util.Locale;

@ConfigCategory(Category.INVENTORY)
public class LootChestStatsFeature extends Feature {

    @SubscribeEvent
    public void onRenderLootChest(ContainerRenderEvent event) {
        if (!Models.Container.isLootChest(ComponentUtils.getUnformatted(event.getScreen().getTitle()))) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        int startX = event.getScreen().leftPos;
        int startY = event.getScreen().topPos;
        poseStack.pushPose();
        poseStack.translate(startX, startY, 0);
        int width = event.getScreen().imageWidth;
        int titleLabelX = event.getScreen().titleLabelX;
        int titleLabelY = event.getScreen().titleLabelY;
        int inventoryLabelX = event.getScreen().inventoryLabelX;
        int inventoryLabelY = event.getScreen().inventoryLabelY;
        renderTotalChestCount(event.getPoseStack(), width - titleLabelX, titleLabelY, Models.LootChest.getTotalCount());
        renderDryChestCount(event.getPoseStack(), width - inventoryLabelX, inventoryLabelY, Models.LootChest.getDryCount());
        poseStack.popPose();
    }

    private void renderTotalChestCount(PoseStack poseStack, int x, int y, int totalChests) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 200);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(
                new Locale(
                        System.getProperty("user.language"),
                        System.getProperty("user.country")));
        String totalChestCountStats = "Total: " + numberFormat.format(totalChests);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        CodedString.fromString(totalChestCountStats),
                        x,
                        y,
                        0,
                        CommonColors.DARK_GRAY,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();
    }

    private void renderDryChestCount(PoseStack poseStack, int x, int y, int dryChests) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 200);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale(System.getProperty("user.language"), System.getProperty("user.country")));
        String totalChestCountStats = "Dry: " + numberFormat.format(dryChests);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        CodedString.fromString(totalChestCountStats),
                        x,
                        y,
                        0,
                        CommonColors.DARK_GRAY,
                        HorizontalAlignment.RIGHT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();
    }
}
