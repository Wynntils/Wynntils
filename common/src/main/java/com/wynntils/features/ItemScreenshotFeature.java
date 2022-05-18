/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.wynntils.core.features.FeatureBase;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.core.keybinds.KeyHolder;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.INVARIABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class ItemScreenshotFeature extends FeatureBase {

    private static final Pattern ITEM_PATTERN =
            Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic) Item.*");
    private Slot screenshotSlot = null;

    private final KeyHolder itemScreenshotKeybind =
            new KeyHolder("Screenshot Item", GLFW.GLFW_KEY_F4, "Wynntils", true, () -> {});

    public ItemScreenshotFeature() {
        setupEventListener();
        setupKeyHolder(itemScreenshotKeybind);
    }

    @SubscribeEvent
    public void onKeyPress(InventoryKeyPressEvent e) {
        if (itemScreenshotKeybind.getKeybind().matches(e.getKeyCode(), e.getScanCode())) {
            screenshotSlot = e.getHoveredSlot();
        }
    }

    @SubscribeEvent
    public void render(ItemTooltipRenderEvent e) {
        if (screenshotSlot == null) return;

        // has to be called during a render period
        takeScreenshot(screenshotSlot);
        screenshotSlot = null;
    }

    private static void takeScreenshot(Slot hoveredSlot) {
        if (!WynnUtils.onWorld()) return;

        Screen screen = McUtils.mc().screen;
        if (!(screen instanceof AbstractContainerScreen<?>)) return;
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        List<Component> tooltip = stack.getTooltipLines(McUtils.player(), TooltipFlag.Default.NORMAL);
        removeItemLore(tooltip);

        Font font = McUtils.mc().font;
        int width = 0;
        int height = 16;

        // width calculation
        for (Component c : tooltip) {
            int w = font.width(c.getString());
            if (w > width) {
                width = w;
            }
        }
        width += 8;

        // height calculation
        if (tooltip.size() > 1) {
            height += 2 + (tooltip.size() - 1) * 10;
        }

        // calculate tooltip size to fit to framebuffer
        float scaleh = (float) screen.height / height;
        float scalew = (float) screen.width / width;

        // draw tooltip to framebuffer, create image
        McUtils.mc().getMainRenderTarget().unbindWrite();

        PoseStack poseStack = new PoseStack();
        RenderTarget fb = new MainTarget(width * 2, height * 2);
        fb.setClearColor(1f, 1f, 1f, 0f);
        fb.createBuffers(width * 2, height * 2, false);
        fb.bindWrite(false);
        poseStack.pushPose();
        poseStack.scale(scalew, scaleh, 1);
        drawTooltip(
                tooltip.stream()
                        .map(Component::getVisualOrderText)
                        .map(ClientTooltipComponent::create)
                        .collect(Collectors.toList()),
                poseStack,
                font);
        poseStack.popPose();
        fb.unbindWrite();
        McUtils.mc().getMainRenderTarget().bindWrite(true);

        BufferedImage bi = createScreenshot(fb);

        // copy to clipboard
        ClipboardImage ci = new ClipboardImage(bi);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ci, null);

        McUtils.sendMessageToClient(
                new TranslatableComponent("feature.wynntils.itemScreenshot.message", stack.getHoverName())
                        .withStyle(ChatFormatting.GREEN));
    }

    private static void removeItemLore(List<Component> tooltip) {
        List<Component> toRemove = new ArrayList<>();
        boolean lore = false;
        for (Component c : tooltip) {
            // only remove text after the item type indicator
            Matcher m = ITEM_PATTERN.matcher(c.getString());
            if (!lore && m.find()) {
                lore = true;
                continue;
            }

            if (lore) toRemove.add(c);
        }
        tooltip.removeAll(toRemove);
    }

    private static void drawTooltip(List<ClientTooltipComponent> lines, PoseStack poseStack, Font font) {
        int tooltipWidth = 0;
        int tooltipHeight = lines.size() == 1 ? -2 : 0;

        for (ClientTooltipComponent clientTooltipComponent : lines) {
            int lineWidth = clientTooltipComponent.getWidth(font);
            if (lineWidth > tooltipWidth) {
                tooltipWidth = lineWidth;
            }
            tooltipHeight += clientTooltipComponent.getHeight();
        }

        // background box
        poseStack.pushPose();
        int tooltipX = 4;
        int tooltipY = 4;
        // somewhat hacky solution to get around transparency issues - these colors were chosen to best match
        // how tooltips are displayed in-game
        int backgroundColor = 0xFF100010;
        int borderColorStart = 0xFF25005B;
        int borderColorEnd = 0xFF180033;
        int zLevel = 400;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 3,
                tooltipY - 4,
                tooltipX + tooltipWidth + 3,
                tooltipY - 3,
                zLevel,
                backgroundColor,
                backgroundColor);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 3,
                tooltipY + tooltipHeight + 3,
                tooltipX + tooltipWidth + 3,
                tooltipY + tooltipHeight + 4,
                zLevel,
                backgroundColor,
                backgroundColor);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + tooltipWidth + 3,
                tooltipY + tooltipHeight + 3,
                zLevel,
                backgroundColor,
                backgroundColor);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 4,
                tooltipY - 3,
                tooltipX - 3,
                tooltipY + tooltipHeight + 3,
                zLevel,
                backgroundColor,
                backgroundColor);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX + tooltipWidth + 3,
                tooltipY - 3,
                tooltipX + tooltipWidth + 4,
                tooltipY + tooltipHeight + 3,
                zLevel,
                backgroundColor,
                backgroundColor);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 3,
                tooltipY - 3 + 1,
                tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1,
                zLevel,
                borderColorStart,
                borderColorEnd);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX + tooltipWidth + 2,
                tooltipY - 3 + 1,
                tooltipX + tooltipWidth + 3,
                tooltipY + tooltipHeight + 3 - 1,
                zLevel,
                borderColorStart,
                borderColorEnd);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + tooltipWidth + 3,
                tooltipY - 3 + 1,
                zLevel,
                borderColorStart,
                borderColorStart);
        fillGradient(
                matrix4f,
                bufferBuilder,
                tooltipX - 3,
                tooltipY + tooltipHeight + 2,
                tooltipX + tooltipWidth + 3,
                tooltipY + tooltipHeight + 3,
                zLevel,
                borderColorEnd,
                borderColorEnd);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        // text
        MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.translate(0.0, 0.0, 400.0);
        int s = tooltipY;
        boolean first = true;
        for (ClientTooltipComponent line : lines) {
            line.renderText(font, tooltipX, s, matrix4f, bufferSource);
            s += line.getHeight() + (first ? 2 : 0);
            first = false;
        }
        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static void fillGradient(
            Matrix4f matrix,
            BufferBuilder builder,
            int x1,
            int y1,
            int x2,
            int y2,
            int blitOffset,
            int colorA,
            int colorB) {
        float f = (float) (colorA >> 24 & 0xFF) / 255.0f;
        float g = (float) (colorA >> 16 & 0xFF) / 255.0f;
        float h = (float) (colorA >> 8 & 0xFF) / 255.0f;
        float i = (float) (colorA & 0xFF) / 255.0f;
        float j = (float) (colorB >> 24 & 0xFF) / 255.0f;
        float k = (float) (colorB >> 16 & 0xFF) / 255.0f;
        float l = (float) (colorB >> 8 & 0xFF) / 255.0f;
        float m = (float) (colorB & 0xFF) / 255.0f;
        builder.vertex(matrix, x2, y1, blitOffset).color(g, h, i, f).endVertex();
        builder.vertex(matrix, x1, y1, blitOffset).color(g, h, i, f).endVertex();
        builder.vertex(matrix, x1, y2, blitOffset).color(k, l, m, j).endVertex();
        builder.vertex(matrix, x2, y2, blitOffset).color(k, l, m, j).endVertex();
    }

    private static BufferedImage createScreenshot(RenderTarget fb) {
        NativeImage image = new NativeImage(fb.width, fb.height, false);
        RenderSystem.bindTexture(fb.getColorTextureId());
        image.downloadTexture(0, false);
        image.flipY();

        int[] pixelValues = image.makePixelArray();
        BufferedImage bufferedimage = new BufferedImage(fb.width, fb.height, BufferedImage.TYPE_INT_ARGB);
        bufferedimage.setRGB(0, 0, fb.width, fb.height, pixelValues, 0, fb.width);
        return bufferedimage;
    }

    private static class ClipboardImage implements Transferable {
        Image image;

        public ClipboardImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) throw new UnsupportedFlavorException(flavor);
            return this.image;
        }
    }
}
