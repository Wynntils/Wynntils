/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.ui.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.wynntils.utils.ColorUtils;
import java.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Rewrite of {@link net.minecraft.client.gui.components.AbstractSelectionList} to support variable
 * length and to be more general
 */
@Environment(EnvType.CLIENT)
public class VariableListWidget extends AbstractContainerEventHandler
        implements Widget, NarratableEntry {
    private static final int backgroundColor = ColorUtils.generateColor(134, 194, 50, 100);

    private final TreeMap<Integer, ListEntry> entriesMap = new TreeMap<>();
    private final List<ListEntry> entries = new ArrayList<>();
    public int width;
    public int height;
    public int y0;
    public int y1;
    public int x1;
    public int x0;
    private double scrollAmount;
    private boolean scrolling;

    private int focusedIndex;

    private ListEntry hovered;

    private int scrollHeight;

    private int maxPosition;

    public VariableListWidget(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        this.x0 = x;
        this.x1 = x + width;
        this.y0 = y;
        this.y1 = y + height;
    }

    public void refresh() {
        maxPosition = 0;

        entriesMap.clear();
        for (ListEntry entry : entries) {
            entriesMap.put(maxPosition, entry);
            maxPosition += entry.getHeight();
        }

        maxPosition = Math.max(0, maxPosition - (this.y1 - this.y0 - 4));

        if (maxPosition != 0) {
            scrollHeight =
                    Mth.clamp(
                            (int) ((float) ((this.height) * (this.height)) / (float) maxPosition),
                            32,
                            height - 8);
        } else {
            scrollHeight = -1;
        }

        // recalculate heights
    }

    public final void clearEntries() {
        this.entriesMap.clear();
        refresh();
    }

    public void replaceEntries(Collection<ListEntry> entries) {
        this.entriesMap.clear();
        this.entries.addAll(entries);
        refresh();
    }

    public List<ListEntry> getEntries() {
        return entries;
    }

    public ListEntry getEntry(int index) {
        return this.entries.get(index);
    }

    public int addEntry(ListEntry entry) {
        this.entriesMap.put(Integer.MAX_VALUE, entry);
        refresh();

        return this.entriesMap.size() - 1;
    }

    public int getItemCount() {
        return this.children().size();
    }

    public void updateSize(int width, int height, int y0, int y1, int x0) {
        this.width = width;
        this.height = height;
        this.y0 = y0;
        this.y1 = y1;
        this.x0 = x0;
        this.x1 = x0 + width;
    }

    public int getMaxPosition() {
        Map.Entry<Integer, ListEntry> last = entriesMap.lastEntry();

        return last.getKey() + last.getValue().getHeight();
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        if (isMouseOver(mouseX, mouseY)) {
            Map.Entry<Integer, ListEntry> hoveredEntry =
                    entriesMap.ceilingEntry((int) (mouseY + scrollAmount));
            hovered = hoveredEntry != null ? hoveredEntry.getValue() : null;
        }

        fill(poseStack, x0, y0, x1, y1, backgroundColor);

        renderList(poseStack, mouseX, mouseY, partialTick);

        if (maxPosition > 0) {
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int n = (int) (scrollAmount * (y1 - y0 - scrollHeight) / maxPosition + y0);
            if (n < y0) {
                n = y0;
            }

            bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(x1, y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(x1 + 6, y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(x1 + 6, y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(x1, y0, 0.0D).color(0, 0, 0, 255).endVertex();

            bufferBuilder
                    .vertex(x1, (n + scrollHeight), 0.0D)
                    .color(128, 128, 128, 255)
                    .endVertex();
            bufferBuilder
                    .vertex(x1 + 6, (n + scrollHeight), 0.0D)
                    .color(128, 128, 128, 255)
                    .endVertex();
            bufferBuilder.vertex(x1 + 6, n, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferBuilder.vertex(x1, n, 0.0D).color(128, 128, 128, 255).endVertex();

            bufferBuilder
                    .vertex(x1, (n + scrollHeight - 1), 0.0D)
                    .color(192, 192, 192, 255)
                    .endVertex();
            bufferBuilder
                    .vertex((x1 + 6 - 1), (n + scrollHeight - 1), 0.0D)
                    .color(192, 192, 192, 255)
                    .endVertex();
            bufferBuilder.vertex((x1 + 6 - 1), n, 0.0D).color(192, 192, 192, 255).endVertex();
            bufferBuilder.vertex(x1, n, 0.0D).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void scroll(int scroll) {
        setScrollAmount(scrollAmount + (double) scroll);
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(double scroll) {
        scrollAmount = Mth.clamp(scroll, 0.0D, maxPosition);
    }

    public int getMaxScroll() {
        return maxPosition;
    }

    public void updateScrollingState(double mouseX, double mouseY, int button) {
        scrolling = button == 0 && mouseX >= (double) x1 && mouseX < (double) (x1 + 6);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return entries;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateScrollingState(mouseX, mouseY, button);
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            Map.Entry<Integer, ListEntry> entry =
                    entriesMap.ceilingEntry((int) (mouseY + scrollAmount));
            if (entry != null) {
                if (entry.getValue().mouseClicked(mouseX, mouseY, button)) {
                    setFocused(entry.getValue());
                    focusedIndex = entry.getKey();
                    setDragging(true);
                    return true;
                }
            }

            return scrolling;
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (getFocused() != null) {
            getFocused().mouseReleased(mouseX, mouseY - focusedIndex, button);
        }

        setFocused(null);
        setDragging(false);

        return false;
    }

    public boolean mouseDragged(
            double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (button == 0 && scrolling) {
            if (mouseY < y0) {
                setScrollAmount(0.0D);
            } else if (mouseY > y1) {
                setScrollAmount(maxPosition);
            } else {
                double d = Math.max(1, maxPosition);
                double e = Math.max(1, d / (double) (height - scrollHeight));
                setScrollAmount(scrollAmount + dragY * e);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.setScrollAmount(scrollAmount - delta * 10D);
        return true;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= y0 && mouseY <= y1 && mouseX >= x0 && mouseX <= x1;
    }

    public void renderList(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        matrixStack.translate(x0, y0 - scrollAmount, 0);

        // Unboxing throws an exception so this is required
        int lower = Objects.requireNonNullElse(entriesMap.floorKey((int) (y0 + scrollAmount)), 0);
        int higher =
                Objects.requireNonNullElse(
                        entriesMap.floorKey((int) (y1 + scrollAmount)), entriesMap.size() - 1);

        int i = 0;

        for (Map.Entry<Integer, ListEntry> entry : entriesMap.entrySet()) {
            if (i < lower) {
                continue;
            } else if (i > higher) {
                break;
            }

            matrixStack.pushPose();
            matrixStack.translate(0, entry.getKey(), 0);
            entry.getValue()
                    .render(
                            matrixStack,
                            mouseX,
                            mouseY,
                            Objects.equals(hovered, entry.getValue()),
                            partialTicks);
            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    public boolean isFocused() {
        return false;
    }

    public NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarrationPriority.FOCUSED;
        } else {
            return this.hovered != null ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }

    @Nullable
    public ListEntry remove(int index) {
        ListEntry entry = entriesMap.get(index);
        return this.removeEntry(entry) ? entry : null;
    }

    public boolean removeEntry(ListEntry entry) {
        boolean bl = this.entries.remove(entry);
        if (bl) {
            refresh();
        }

        return bl;
    }

    @Nullable
    public ListEntry getHovered() {
        return this.hovered;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public interface ListEntry extends GuiEventListener, NarratableEntry {
        void render(
                PoseStack poseStack,
                int mouseX,
                int mouseY,
                boolean isMouseOver,
                float partialTick);

        int getHeight();
    }

    public enum SelectionDirection {
        UP,
        DOWN;

        SelectionDirection() {}
    }
}
