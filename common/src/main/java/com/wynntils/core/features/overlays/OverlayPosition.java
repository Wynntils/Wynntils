/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

public class OverlayPosition {

    private final int verticalOffset;
    private final int horizontalOffset;

    private final VerticalAlignment verticalAlignment;
    private final HorizontalAlignment horizontalAlignment;

    private final AnchorNinth anchorNinth;

    public OverlayPosition(
            int verticalOffset,
            int horizontalOffset,
            VerticalAlignment verticalAlignment,
            HorizontalAlignment horizontalAlignment,
            AnchorNinth anchorNinth) {
        this.verticalAlignment = verticalAlignment;
        this.horizontalAlignment = horizontalAlignment;
        this.anchorNinth = anchorNinth;
        this.verticalOffset = verticalOffset;
        this.horizontalOffset = horizontalOffset;
    }

    public AnchorNinth getAnchorNinth() {
        return anchorNinth;
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

    public enum AnchorNinth {
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

        AnchorNinth(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public enum HorizontalAlignment {
        Left,
        Center,
        Right
    }

    public enum VerticalAlignment {
        Top,
        Middle,
        Bottom
    }
}
