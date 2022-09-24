/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.VerticalAlignment;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.phys.Vec2;

public class OverlayPosition {
    private static final Pattern POSITION_PATTERN = Pattern.compile(
            "OverlayPosition\\{verticalOffset=(.+),horizontalOffset=(.+),verticalAlignment=(.+),horizontalAlignment=(.+),anchorSection=(.+)}");

    private final float verticalOffset;
    private final float horizontalOffset;

    private final VerticalAlignment verticalAlignment;
    private final HorizontalAlignment horizontalAlignment;

    private final AnchorSection anchorSection;

    public OverlayPosition(
            float verticalOffset,
            float horizontalOffset,
            VerticalAlignment verticalAlignment,
            HorizontalAlignment horizontalAlignment,
            AnchorSection anchorSection) {
        this.verticalAlignment = verticalAlignment;
        this.horizontalAlignment = horizontalAlignment;
        this.anchorSection = anchorSection;
        this.verticalOffset = verticalOffset;
        this.horizontalOffset = horizontalOffset;
    }

    public OverlayPosition(String string) {
        Matcher matcher = POSITION_PATTERN.matcher(string.replaceAll(" ", ""));

        if (!matcher.matches()) {
            throw new RuntimeException("Failed to parse OverlayPosition");
        }

        try {
            this.verticalOffset = Float.parseFloat(matcher.group(1));
            this.horizontalOffset = Float.parseFloat(matcher.group(2));
            this.verticalAlignment = VerticalAlignment.valueOf(matcher.group(3));
            this.horizontalAlignment = HorizontalAlignment.valueOf(matcher.group(4));
            this.anchorSection = AnchorSection.valueOf(matcher.group(5));
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("Failed to parse OverlayPosition", exception);
        }
    }

    public AnchorSection getAnchorSection() {
        return anchorSection;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public float getHorizontalOffset() {
        return horizontalOffset;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public float getVerticalOffset() {
        return verticalOffset;
    }

    @Override
    public String toString() {
        return "OverlayPosition{" + "verticalOffset="
                + verticalOffset + ", horizontalOffset="
                + horizontalOffset + ", verticalAlignment="
                + verticalAlignment + ", horizontalAlignment="
                + horizontalAlignment + ", anchorSection="
                + anchorSection + '}';
    }

    public static OverlayPosition getBestPositionFor(
            Overlay overlay, float oldRenderX, float oldRenderY, float offsetX, float offsetY) {
        Vec2 topLeftOfOverlay = new Vec2(overlay.getRenderX(), overlay.getRenderY());

        // 1. Get the best section (section with the top left point of overlay)
        AnchorSection section = Arrays.stream(AnchorSection.values())
                .filter(anchorSection ->
                        OverlayManager.getSection(anchorSection).overlaps(topLeftOfOverlay.x, topLeftOfOverlay.y))
                .findAny()
                .orElse(AnchorSection.Middle);
        SectionCoordinates sectionCoordinates = OverlayManager.getSection(section);

        // 2. Calculate the best alignment inside the section
        HorizontalAlignment horizontalAlignment = HorizontalAlignment.Center;

        float distanceToCornerHorizontally =
                Math.abs((sectionCoordinates.x1() + sectionCoordinates.x2()) / 2f - topLeftOfOverlay.x);
        if (Math.abs(sectionCoordinates.x1() - topLeftOfOverlay.x) < distanceToCornerHorizontally) {
            horizontalAlignment = HorizontalAlignment.Left;
        } else if (Math.abs(sectionCoordinates.x2() - topLeftOfOverlay.x) < distanceToCornerHorizontally) {
            horizontalAlignment = HorizontalAlignment.Right;
        }

        VerticalAlignment verticalAlignment = VerticalAlignment.Middle;

        float distanceToCornerVertically =
                Math.abs((sectionCoordinates.y1() + sectionCoordinates.y2()) / 2f - topLeftOfOverlay.y);
        if (Math.abs(sectionCoordinates.y1() - topLeftOfOverlay.y) < distanceToCornerVertically) {
            verticalAlignment = VerticalAlignment.Top;
        } else if (Math.abs(sectionCoordinates.y2() - topLeftOfOverlay.y) < distanceToCornerVertically) {
            verticalAlignment = VerticalAlignment.Bottom;
        }

        // 3. Calculate render positions for new alignment
        OverlayPosition newOverlayPositionTemp =
                new OverlayPosition(0, 0, verticalAlignment, horizontalAlignment, section);

        float renderX = overlay.getRenderX(newOverlayPositionTemp);
        float renderY = overlay.getRenderY(newOverlayPositionTemp);

        // 4. Calculate the alignment offsets to match the current render position, but factor in argument offsets
        return new OverlayPosition(
                oldRenderY - renderY + offsetY,
                oldRenderX - renderX + offsetX,
                verticalAlignment,
                horizontalAlignment,
                section);
    }

    public enum AnchorSection {
        TopLeft(0),
        TopMiddle(1),
        TopRight(2),
        MiddleLeft(3),
        Middle(4),
        MiddleRight(5),
        BottomLeft(6),
        BottomMiddle(7),
        BottomRight(8);

        private final int index;

        AnchorSection(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
