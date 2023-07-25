/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public abstract class ContainerOverlay<T extends Overlay> extends Overlay {
    private static final int DEFAULT_SPACING = 3;

    @RegisterConfig("overlay.wynntils.overlay.growDirection")
    protected final Config<GrowDirection> growDirection = new Config<>(GrowDirection.DOWN);

    @RegisterConfig("overlay.wynntils.overlay.spacing")
    protected final Config<Integer> spacing = new Config<>(DEFAULT_SPACING);

    private final List<T> children = new ArrayList<>();
    private final Map<T, OverlaySize> inherentSize = new HashMap<>();

    private ContainerOverlay(
            OverlayPosition position,
            OverlaySize size,
            GrowDirection growDirection,
            int spacing,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        super(position, size);
        this.growDirection.updateConfig(growDirection);
        this.spacing.updateConfig(spacing);
        this.horizontalAlignmentOverride.updateConfig(horizontalAlignment);
        this.verticalAlignmentOverride.updateConfig(verticalAlignment);
    }

    protected ContainerOverlay(
            OverlayPosition position,
            OverlaySize size,
            GrowDirection growDirection,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment) {
        this(position, size, growDirection, DEFAULT_SPACING, horizontalAlignment, verticalAlignment);
    }

    public void addChild(T overlay) {
        inherentSize.put(overlay, overlay.getSize().copy());
        children.add(overlay);
        WynntilsMod.registerEventListener(overlay);

        updateAllChildren();
    }

    public void clearChildren() {
        children.forEach(WynntilsMod::unregisterEventListener);
        children.clear();
        inherentSize.clear();
    }

    public int size() {
        return children.size();
    }

    protected abstract List<T> getPreviewChildren();

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        children.forEach(o -> o.render(poseStack, bufferSource, partialTicks, window));
    }

    @Override
    public void renderPreview(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        List<T> previewChildren = getPreviewChildren();
        Map<T, OverlaySize> previewSize =
                previewChildren.stream().collect(Collectors.toMap(Function.identity(), Overlay::getSize));

        updateLayout(previewChildren, previewSize);
        previewChildren.forEach(o -> o.renderPreview(poseStack, bufferSource, partialTicks, window));
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        updateAllChildren();
    }

    @Override
    public void setPosition(OverlayPosition position) {
        super.setPosition(position);

        updateAllChildren();
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);

        updateAllChildren();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);

        updateAllChildren();
    }

    @SubscribeEvent
    public void onResizeEvent(DisplayResizeEvent event) {
        updateAllChildren();
    }

    private void updateAllChildren() {
        updateLayout(children, inherentSize);
    }

    private void updateLayout(List<T> children, Map<T, OverlaySize> inherentSize) {
        // Update position for all children
        int currentHeight = 0;
        int currentWidth = 0;
        GrowDirection direction = growDirection.get();

        for (T overlay : children) {
            overlay.setPosition(direction.getChildPosition(
                    getRenderX(), getRenderY(), getSize(), overlay.getSize(), currentWidth, currentHeight));
            direction.updateSize(overlay, this.getSize(), inherentSize.get(overlay));
            overlay.horizontalAlignmentOverride.updateConfig(horizontalAlignmentOverride.get());
            overlay.verticalAlignmentOverride.updateConfig(verticalAlignmentOverride.get());

            currentHeight += overlay.getSize().getHeight() + spacing.get();
            currentWidth += overlay.getSize().getWidth() + spacing.get();
        }
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

        private void updateSize(Overlay overlay, OverlaySize containerSize, OverlaySize inherentSize) {
            OverlaySize size = overlay.getSize();

            if (verticalMultiplier != 0) {
                size.setWidth(containerSize.getWidth());
                size.setHeight(inherentSize.getHeight());
            }
            if (horizontalMultiplier != 0) {
                size.setWidth(inherentSize.getWidth());
                size.setHeight(containerSize.getHeight());
            }
        }

        private OverlayPosition getChildPosition(
                float containerX,
                float containerY,
                OverlaySize containerSize,
                OverlaySize childSize,
                int accumulatedWidth,
                int accumulatedHeight) {
            // If the multiplier is negative, we must add the corresponding size to the base
            // position. If it is zero or positive, use the original position
            float heightOffset = verticalMultiplier < 0 ? containerSize.getHeight() - childSize.getHeight() : 0;
            float widthOffset = horizontalMultiplier < 0 ? containerSize.getWidth() - childSize.getWidth() : 0;

            return new OverlayPosition(
                    containerY + heightOffset + accumulatedHeight * verticalMultiplier,
                    containerX + widthOffset + accumulatedWidth * horizontalMultiplier,
                    VerticalAlignment.TOP,
                    HorizontalAlignment.LEFT,
                    OverlayPosition.AnchorSection.TOP_LEFT);
        }
    }
}
