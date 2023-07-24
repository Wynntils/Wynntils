/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.screens.gearviewer.GearViewerScreen;
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
import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.INVENTORY)
public class InventoryEmeraldCountFeature extends Feature {
    private static final int TEXTURE_SIZE = 28;

    @RegisterConfig
    public final Config<EmeraldCountType> emeraldCountType = new Config<>(EmeraldCountType.TEXTURE);

    @RegisterConfig
    public final Config<TextDisplaySide> textDisplaySide = new Config<>(TextDisplaySide.LEFT);

    @RegisterConfig
    public final Config<Boolean> showInventoryEmeraldCount = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> showContainerEmeraldCount = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> showZerosInEmeraldCount = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> combineInventoryAndContainer = new Config<>(false);

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        Screen screen = McUtils.mc().screen;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
        if (screen instanceof GearViewerScreen) return;

        if (StyledText.fromComponent(screen.getTitle())
                .getStringWithoutFormatting()
                .equals("Character Info")) return;

        // Always draw top part, which is all there is if it is inventory,
        // and all there is if we combine them, otherwise it is just the
        // container
        boolean isInventory = (event.getScreen().getMenu().containerId == 0);
        int topEmeralds;
        if (isInventory) {
            if (!showInventoryEmeraldCount.get()) return;
            topEmeralds = Models.Emerald.getAmountInInventory();
        } else {
            topEmeralds = 0;
            if (showContainerEmeraldCount.get()) topEmeralds += Models.Emerald.getAmountInContainer();
            if (combineInventoryAndContainer.get() && showInventoryEmeraldCount.get()) {
                topEmeralds += Models.Emerald.getAmountInInventory();
            }
        }

        int textureX = containerScreen.leftPos;
        int textX = (textDisplaySide.get() == TextDisplaySide.LEFT)
                ? containerScreen.leftPos + 2
                : screen.width - containerScreen.leftPos - 2;

        if (topEmeralds != 0) {
            int y = containerScreen.topPos;
            switch (emeraldCountType.get()) {
                case TEXT -> renderTextCount(event.getPoseStack(), textX, y, topEmeralds);
                case TEXTURE -> renderTexturedCount(
                        event.getPoseStack(), textureX, y, topEmeralds, showZerosInEmeraldCount.get());
            }
        }

        if (!isInventory && !combineInventoryAndContainer.get() && showInventoryEmeraldCount.get()) {
            int bottomEmeralds = Models.Emerald.getAmountInInventory();
            if (bottomEmeralds != 0) {
                int y = containerScreen.topPos + containerScreen.imageHeight;
                switch (emeraldCountType.get()) {
                    case TEXT -> renderTextCount(event.getPoseStack(), textX, y + 11, bottomEmeralds);
                    case TEXTURE -> renderTexturedCount(
                            event.getPoseStack(),
                            textureX,
                            y - 28 * 3 - 2,
                            bottomEmeralds,
                            showZerosInEmeraldCount.get());
                }
            }
        }
    }

    private void renderTextCount(PoseStack poseStack, int x, int y, int emeralds) {
        final HorizontalAlignment emeraldTextAlignment =
                textDisplaySide.get() == TextDisplaySide.LEFT ? HorizontalAlignment.LEFT : HorizontalAlignment.RIGHT;
        final int emeraldTextOffsetX = textDisplaySide.get() == TextDisplaySide.LEFT ? 1 : -1;

        poseStack.pushPose();
        poseStack.translate(0, 0, 200);

        String emeraldText;
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            emeraldText = emeralds + EmeraldUnits.EMERALD.getSymbol();
        } else {
            emeraldText = Models.Emerald.getFormattedString(emeralds, showZerosInEmeraldCount.get());
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(emeraldText),
                        x + emeraldTextOffsetX,
                        y - 10,
                        0,
                        CommonColors.WHITE,
                        emeraldTextAlignment,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        poseStack.popPose();
    }

    private void renderTexturedCount(PoseStack poseStack, int x, int y, int emeralds, boolean appendZeros) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);

        String[] emeraldAmounts = new String[4];
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            emeraldAmounts[0] = StringUtils.formatAmount(emeralds);
            emeraldAmounts[1] = StringUtils.formatAmount(emeralds / 64d);
            emeraldAmounts[2] = StringUtils.formatAmount(emeralds / 4096d);
            emeraldAmounts[3] = StringUtils.formatAmount(emeralds / 262144d);
        } else {
            emeraldAmounts = Arrays.stream(Models.Emerald.emeraldsPerUnit(emeralds))
                    .mapToObj(String::valueOf)
                    .toArray(String[]::new);
        }

        int renderedCount = 0;

        for (int i = emeraldAmounts.length - 1; i >= 0; i--) {
            String emeraldAmount = emeraldAmounts[i];
            if (Objects.equals(emeraldAmount, "0") && !appendZeros) continue;

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
                    .renderGuiItem(poseStack, EmeraldUnits.values()[i].getItemStack(), renderX + 6, renderY + 6);

            if (EmeraldUnits.values()[i].getSymbol().equals("stx")) { // Make stx not look like normal LE
                McUtils.mc()
                        .getItemRenderer()
                        .renderGuiItem(poseStack, EmeraldUnits.values()[i].getItemStack(), renderX + 3, renderY + 4);
                McUtils.mc()
                        .getItemRenderer()
                        .renderGuiItem(poseStack, EmeraldUnits.values()[i].getItemStack(), renderX + 6, renderY + 6);
                McUtils.mc()
                        .getItemRenderer()
                        .renderGuiItem(poseStack, EmeraldUnits.values()[i].getItemStack(), renderX + 9, renderY + 8);
            } else {
                // This needs to be separate since Z levels are determined by order here
                McUtils.mc()
                        .getItemRenderer()
                        .renderGuiItem(poseStack, EmeraldUnits.values()[i].getItemStack(), renderX + 6, renderY + 6);
            }

            poseStack.pushPose();
            poseStack.translate(0, 0, 200);
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            StyledText.fromString(emeraldAmount),
                            renderX,
                            renderX + TEXTURE_SIZE - 2,
                            renderY,
                            renderY + TEXTURE_SIZE - 2,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.RIGHT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.OUTLINE);
            poseStack.popPose();

            renderedCount++;
        }

        poseStack.popPose();
    }

    public enum EmeraldCountType {
        TEXT,
        TEXTURE
    }

    private enum TextDisplaySide {
        RIGHT,
        LEFT
    }
}
