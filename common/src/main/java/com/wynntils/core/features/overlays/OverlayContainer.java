/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;

public class OverlayContainer extends Overlay {
    public static final int DEFAULT_SPACING = 3;

    @RegisterConfig("overlay.wynntils.overlay.growDirection")
    protected final Config<GrowDirection> growDirection = new Config<>(GrowDirection.DOWN);

    @RegisterConfig("overlay.wynntils.overlay.spacing")
    protected final Config<Integer> spacing = new Config<>(DEFAULT_SPACING);

    private final List<Overlay> children = new ArrayList<>();
    private final Map<Overlay, OverlaySize> inherentSize = new HashMap<>();

    public OverlayContainer(OverlayPosition position, OverlaySize size, GrowDirection growDirection, int spacing) {
        super(position, size);
        this.growDirection.updateConfig(growDirection);
        this.spacing.updateConfig(spacing);
    }

    public OverlayContainer(OverlayPosition position, OverlaySize size, GrowDirection growDirection) {
        this(position, size, growDirection, DEFAULT_SPACING);
    }

    public void addChild(Overlay overlay) {
        inherentSize.put(overlay, overlay.getSize().copy());
        int currentHeight = children.stream()
                .mapToInt(o -> (int) o.size.get().getHeight() + spacing.get())
                .sum();
        int currentWidth = children.stream()
                .mapToInt(o -> (int) o.size.get().getWidth() + spacing.get())
                .sum();

        GrowDirection direction = growDirection.get();
        overlay.setPosition(direction.getChildPosition(
                getRenderX(), getRenderY(), getSize(), overlay.getSize(), currentHeight, currentWidth));
        direction.updateSize(overlay, this.getSize(), inherentSize.get(overlay));

        children.add(overlay);
    }

    public void clearChildren() {
        children.clear();
    }

    @Override
    public void render(
            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
        children.forEach(o -> o.render(poseStack, bufferSource, partialTicks, window));
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        updateChildrenLayout();

        children.forEach(o -> o.onConfigUpdate(configHolder));
    }

    public int size() {
        return children.size();
    }

    @Override
    public void setPosition(OverlayPosition position) {
        super.setPosition(position);

        updateChildrenLayout();
    }

    private void updateChildrenLayout() {
        // Update position for all children
        int currentHeight = 0;
        int currentWidth = 0;
        GrowDirection direction = growDirection.get();

        for (Overlay overlay : children) {
            overlay.setPosition(direction.getChildPosition(
                    getRenderX(), getRenderY(), getSize(), overlay.getSize(), currentHeight, currentWidth));
            direction.updateSize(overlay, this.getSize(), inherentSize.get(overlay));

            currentHeight += overlay.getSize().getHeight() + spacing.get();
            currentWidth += overlay.getSize().getWidth() + spacing.get();
        }
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);

        updateChildrenLayout();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);

        updateChildrenLayout();
    }

    public enum GrowDirection {
        UP(-1, 0),
        DOWN(1, 0),
        LEFT(0, -1),
        RIGHT(0, 1);

        private final int verticalMultiplier;
        private final int horizontalMultiplier;

        GrowDirection(int verticalMultiplier, int horizontalMultiplier) {
            this.verticalMultiplier = verticalMultiplier;
            this.horizontalMultiplier = horizontalMultiplier;
        }

        public void updateSize(Overlay overlay, OverlaySize containerSize, OverlaySize inherentSize) {
            var size = overlay.getSize();
            if (verticalMultiplier != 0) {
                size.setWidth(containerSize.getWidth());
                size.setHeight(inherentSize.getHeight());
            }
            if (horizontalMultiplier != 0) {
                size.setWidth(inherentSize.getWidth());
                size.setHeight(containerSize.getHeight());
            }
        }

        public OverlayPosition getChildPosition(
                float containerX,
                float containerY,
                OverlaySize containerSize,
                OverlaySize childSize,
                int currentHeight,
                int currentWidth) {

            // If the multiplier is negative, we must add the corresponding size to the base
            // position. If it is zero or positive, use the original position
            float heightOffset = verticalMultiplier < 0 ? containerSize.getHeight() - childSize.getHeight() : 0;
            float widthOffset = horizontalMultiplier < 0 ? containerSize.getWidth() - childSize.getWidth() : 0;

            return new OverlayPosition(
                    containerY + heightOffset + currentHeight * verticalMultiplier,
                    containerX + widthOffset + currentWidth * horizontalMultiplier,
                    VerticalAlignment.Top,
                    HorizontalAlignment.Left,
                    OverlayPosition.AnchorSection.TopLeft);
        }
    }
}
