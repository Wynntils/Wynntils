/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.models.emeralds.type.EmeraldSymbols;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class InventoryEmeraldCountFeature extends UserFeature {
    private static final int TEXTURE_SIZE = 28;

    @Config
    public EmeraldCountType emeraldCountType = EmeraldCountType.Texture;

    @Config
    public boolean showInventoryEmeraldCount = true;

    @Config
    public boolean showContainerEmeraldCount = true;

    @Config
    public boolean combineInventoryAndContainer = false;

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.PlayerInventory);
    }

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        Screen screen = McUtils.mc().screen;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        // Always draw top part, which is all there is if it is inventory,
        // and all there is if we combine them, otherwise it is just the
        // container
        boolean isInventory = (event.getScreen().getMenu().containerId == 0);
        int topEmeralds;
        if (isInventory) {
            if (!showInventoryEmeraldCount) return;
            topEmeralds = Managers.Emerald.getAmountInInventory();
        } else {
            topEmeralds = 0;
            if (showContainerEmeraldCount) topEmeralds += Managers.Emerald.getAmountInContainer();
            if (combineInventoryAndContainer && showInventoryEmeraldCount) {
                topEmeralds += Managers.Emerald.getAmountInInventory();
            }
        }

        int x = containerScreen.leftPos;
        if (topEmeralds != 0) {
            int y = containerScreen.topPos;
            switch (emeraldCountType) {
                case Text -> renderTextCount(event.getPoseStack(), x + 2, y, topEmeralds);
                case Texture -> renderTexturedCount(event.getPoseStack(), x, y, topEmeralds);
            }
        }

        if (!isInventory && !combineInventoryAndContainer && showInventoryEmeraldCount) {
            int bottomEmeralds = Managers.Emerald.getAmountInInventory();
            if (bottomEmeralds != 0) {
                int y = containerScreen.topPos + containerScreen.imageHeight;
                switch (emeraldCountType) {
                    case Text -> renderTextCount(event.getPoseStack(), x + 2, y + 11, bottomEmeralds);
                    case Texture -> renderTexturedCount(event.getPoseStack(), x, y - 28 * 3 - 2, bottomEmeralds);
                }
            }
        }
    }

    private void renderTextCount(PoseStack poseStack, int x, int y, int emeralds) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 200);

        String emeraldText;
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            emeraldText = String.valueOf(emeralds) + EmeraldSymbols.E;
        } else {
            int[] emeraldAmounts = calculateEmeraldAmounts(emeralds);
            StringBuilder builder = new StringBuilder();

            for (int i = emeraldAmounts.length - 1; i >= 0; i--) {
                if (emeraldAmounts[i] == 0) continue;

                builder.append(emeraldAmounts[i])
                        .append(EmeraldUnits.values()[i].getSymbol())
                        .append(" ");
            }

            emeraldText = builder.toString().trim();
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        emeraldText,
                        x + 1,
                        y - 10,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NORMAL);

        poseStack.popPose();
    }

    private void renderTexturedCount(PoseStack poseStack, int x, int y, int emeralds) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);

        String[] emeraldAmounts = new String[3];
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            emeraldAmounts[0] = StringUtils.formatAmount(emeralds);
            emeraldAmounts[1] = StringUtils.formatAmount(emeralds / 64d);
            emeraldAmounts[2] = StringUtils.formatAmount(emeralds / 4096d);
        } else {
            emeraldAmounts = Arrays.stream(calculateEmeraldAmounts(emeralds))
                    .mapToObj(String::valueOf)
                    .toArray(String[]::new);
        }

        int renderedCount = 0;

        for (int i = emeraldAmounts.length - 1; i >= 0; i--) {
            String emeraldAmount = emeraldAmounts[i];
            if (Objects.equals(emeraldAmount, "0")) continue;

            final int renderX = -TEXTURE_SIZE;
            final int renderY = renderedCount * TEXTURE_SIZE;
            RenderUtils.drawTexturedRect(
                    poseStack,
                    Texture.EMERALD_COUNT_BACKGROUND.resource(),
                    renderX,
                    renderY,
                    0,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE,
                    0,
                    0,
                    Texture.EMERALD_COUNT_BACKGROUND.width(),
                    Texture.EMERALD_COUNT_BACKGROUND.height(),
                    Texture.EMERALD_COUNT_BACKGROUND.width(),
                    Texture.EMERALD_COUNT_BACKGROUND.height());

            McUtils.mc()
                    .getItemRenderer()
                    .renderGuiItem(EmeraldUnits.values()[i].getItemStack(), x + renderX + 6, y + renderY + 6);

            poseStack.pushPose();
            poseStack.translate(0, 0, 200);
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            emeraldAmount,
                            renderX,
                            renderX + TEXTURE_SIZE - 2,
                            renderY,
                            renderY + TEXTURE_SIZE - 2,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.Right,
                            VerticalAlignment.Bottom,
                            TextShadow.OUTLINE);
            poseStack.popPose();

            renderedCount++;
        }

        poseStack.popPose();
    }

    private static int[] calculateEmeraldAmounts(int emeralds) {
        return new int[] {emeralds % 64, (emeralds / 64) % 64, emeralds / 4096};
    }

    public enum EmeraldCountType {
        Text,
        Texture
    }
}
