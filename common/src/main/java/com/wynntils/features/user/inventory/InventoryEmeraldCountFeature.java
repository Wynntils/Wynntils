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
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.objects.EmeraldSymbols;
import com.wynntils.wynn.objects.EmeraldUnits;
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

    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.PlayerInventory);
    }

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        boolean isInventory = (event.getScreen().getMenu().containerId == 0);
        int emeralds = isInventory
                ? Managers.Emerald.getAmountInInventory()
                : Managers.Emerald.getAmountInContainer() + Managers.Emerald.getAmountInInventory();

        if (emeralds == 0) return;

        switch (emeraldCountType) {
            case Text -> renderTextCount(event.getPoseStack(), emeralds);
            case Texture -> renderTexturedCount(event.getPoseStack(), emeralds);
        }
    }

    private void renderTextCount(PoseStack poseStack, int emeralds) {
        Screen screen = McUtils.mc().screen;

        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

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
                        containerScreen.leftPos + 1,
                        containerScreen.topPos - 10,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NORMAL);

        poseStack.popPose();
    }

    private void renderTexturedCount(PoseStack poseStack, int emeralds) {
        Screen screen = McUtils.mc().screen;

        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        poseStack.pushPose();
        poseStack.translate(containerScreen.leftPos, containerScreen.topPos, 0);

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
                    .renderGuiItem(
                            EmeraldUnits.values()[i].getItemStack(),
                            containerScreen.leftPos + renderX + 6,
                            containerScreen.topPos + renderY + 6);

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
                            FontRenderer.TextShadow.OUTLINE);
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
