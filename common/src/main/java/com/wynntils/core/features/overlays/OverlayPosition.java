/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.VerticalAlignment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OverlayPosition {

    protected static final Pattern POSITION_PATTERN = Pattern.compile(
            "OverlayPosition\\{verticalOffset=(.+),horizontalOffset=(.+),verticalAlignment=(.+),horizontalAlignment=(.+),anchorSection=(.+)}");

    private int verticalOffset;
    private int horizontalOffset;

    private VerticalAlignment verticalAlignment;
    private HorizontalAlignment horizontalAlignment;

    private AnchorSection anchorSection;

    public OverlayPosition(
            int verticalOffset,
            int horizontalOffset,
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
            this.verticalOffset = Integer.parseInt(matcher.group(1));
            this.horizontalOffset = Integer.parseInt(matcher.group(2));
            this.verticalAlignment = VerticalAlignment.valueOf(matcher.group(3));
            this.horizontalAlignment = HorizontalAlignment.valueOf(matcher.group(4));
            this.anchorSection = AnchorSection.valueOf(matcher.group(5));
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("Failed to parse OverlayPosition");
        }
    }

    public AnchorSection getAnchorSection() {
        return anchorSection;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public int getHorizontalOffset() {
        return horizontalOffset;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public int getVerticalOffset() {
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
