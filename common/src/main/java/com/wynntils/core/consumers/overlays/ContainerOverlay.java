/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public abstract class ContainerOverlay<T extends Overlay> extends Overlay {
    private static final int DEFAULT_SPACING = 3;

    @Persisted(i18nKey = "overlay.wynntils.overlay.growDirection")
    private final Config<GrowDirection> growDirection = new Config<>(GrowDirection.DOWN);

    @Persisted(i18nKey = "overlay.wynntils.overlay.spacing")
    private final Config<Integer> spacing = new Config<>(DEFAULT_SPACING);

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
        this.growDirection.store(growDirection);
        this.spacing.store(spacing);
        this.horizontalAlignmentOverride.store(horizontalAlignment);
        this.verticalAlignmentOverride.store(verticalAlignment);
        WynntilsMod.registerListener(this::onResizeEvent);
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
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        children.forEach(o -> o.render(guiGraphics, deltaTracker, window));
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        List<T> previewChildren = getPreviewChildren();
        Map<T, OverlaySize> previewSize =
                previewChildren.stream().collect(Collectors.toMap(Function.identity(), Overlay::getSize));

        updateLayout(previewChildren, previewSize);
        previewChildren.forEach(o -> o.renderPreview(guiGraphics, deltaTracker, window));
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
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

    @Override
    public void tick() {
        super.tick();
        children.forEach(Overlay::tick);
    }

    // As this is an abstract class, this event was subscribed to manually in ctor
    private void onResizeEvent(DisplayResizeEvent event) {
        updateAllChildren();
    }

    private void updateAllChildren() {
        updateLayout(children, inherentSize);
    }

    private void updateLayout(List<T> children, Map<T, OverlaySize> inherentSize) {
        // Update position for all children
        GrowDirection direction = growDirection.get();

        // Special cases
        if (direction == GrowDirection.CENTER_VERTICAL || direction == GrowDirection.CENTER_HORIZONTAL) {
            updateLayoutCentered(children, inherentSize, direction);
            return;
        }

        int currentHeight = 0;
        int currentWidth = 0;

        for (T overlay : children) {
            overlay.setPosition(direction.getChildPosition(
                    getRenderX(), getRenderY(), getSize(), overlay.getSize(), currentWidth, currentHeight));
            direction.updateSize(overlay, this.getSize(), inherentSize.get(overlay));
            overlay.horizontalAlignmentOverride.store(horizontalAlignmentOverride.get());
            overlay.verticalAlignmentOverride.store(verticalAlignmentOverride.get());

            currentHeight += overlay.getSize().getHeight() + spacing.get();
            currentWidth += overlay.getSize().getWidth() + spacing.get();
        }
    }

    private void updateLayoutCentered(List<T> children, Map<T, OverlaySize> inherentSize, GrowDirection direction) {
        int count = children.size();
        if (count == 0) return;

        boolean vertical = direction == GrowDirection.CENTER_VERTICAL;
        int space = spacing.get();

        // Total span of all children including spacing
        int totalSize = 0;
        for (T child : children) {
            OverlaySize size = inherentSize.get(child);
            totalSize += vertical ? size.getHeight() : size.getWidth();
        }
        totalSize += space * (count - 1);

        float containerCenterX = getRenderX() + getSize().getWidth() / 2f;
        float containerCenterY = getRenderY() + getSize().getHeight() / 2f;

        // Start so that the whole group is centered
        float cursor = (vertical ? containerCenterY : containerCenterX) - totalSize / 2f;

        for (T overlay : children) {
            OverlaySize overlaySize = inherentSize.get(overlay);

            direction.updateSize(overlay, this.getSize(), overlaySize);

            float x;
            float y;

            if (vertical) {
                y = cursor;
                x = containerCenterX - overlay.getSize().getWidth() / 2f;
                cursor += overlaySize.getHeight() + space;
            } else {
                x = cursor;
                y = containerCenterY - overlay.getSize().getHeight() / 2f;
                cursor += overlaySize.getWidth() + space;
            }

            overlay.setPosition(new OverlayPosition(
                    y, x, VerticalAlignment.TOP, HorizontalAlignment.LEFT, OverlayPosition.AnchorSection.TOP_LEFT));

            overlay.horizontalAlignmentOverride.store(horizontalAlignmentOverride.get());
            overlay.verticalAlignmentOverride.store(verticalAlignmentOverride.get());
        }
    }

    public enum GrowDirection {
        UP(-1, 0),
        DOWN(1, 0),
        LEFT(0, -1),
        RIGHT(0, 1),
        CENTER_VERTICAL(1, 0),
        CENTER_HORIZONTAL(0, 1);

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
