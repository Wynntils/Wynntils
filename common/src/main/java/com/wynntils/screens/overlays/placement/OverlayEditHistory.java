/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayDeque;
import java.util.Deque;

final class OverlayEditHistory {
    private static final int MAX_EDITS = 100;

    private final Deque<Edit> undo = new ArrayDeque<>();
    private final Deque<Edit> redo = new ArrayDeque<>();
    private Overlay editingOverlay;
    private State before;

    public void begin(Overlay overlay) {
        finish();
        editingOverlay = overlay;
        before = State.capture(overlay);
    }

    public void finish() {
        if (editingOverlay == null) return;

        State after = State.capture(editingOverlay);
        if (!before.equals(after)) {
            undo.addLast(new Edit(editingOverlay, before, after));
            if (undo.size() > MAX_EDITS) {
                undo.removeFirst();
            }
            redo.clear();
        }
        editingOverlay = null;
        before = null;
    }

    public Overlay undo() {
        finish();
        if (undo.isEmpty()) return null;

        Edit edit = undo.removeLast();
        edit.before().apply(edit.overlay());
        redo.addLast(edit);
        return edit.overlay();
    }

    public Overlay redo() {
        finish();
        if (redo.isEmpty()) return null;

        Edit edit = redo.removeLast();
        edit.after().apply(edit.overlay());
        undo.addLast(edit);
        return edit.overlay();
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public void clear() {
        undo.clear();
        redo.clear();
        editingOverlay = null;
        before = null;
    }

    private record Edit(Overlay overlay, State before, State after) {}

    private record State(
            float xOffset,
            float yOffset,
            HorizontalAlignment horizontalAlignment,
            VerticalAlignment verticalAlignment,
            OverlayPosition.AnchorSection anchor,
            float width,
            float height,
            HorizontalAlignment horizontalOverride,
            VerticalAlignment verticalOverride) {
        static State capture(Overlay overlay) {
            OverlayPosition position = overlay.getPosition();
            return new State(
                    position.getHorizontalOffset(),
                    position.getVerticalOffset(),
                    position.getHorizontalAlignment(),
                    position.getVerticalAlignment(),
                    position.getAnchorSection(),
                    overlay.getWidth(),
                    overlay.getHeight(),
                    (HorizontalAlignment) overlay.getConfigOptionFromString("horizontalAlignmentOverride")
                            .map(Config::get)
                            .orElse(null),
                    (VerticalAlignment) overlay.getConfigOptionFromString("verticalAlignmentOverride")
                            .map(Config::get)
                            .orElse(null));
        }

        void apply(Overlay overlay) {
            overlay.setPosition(new OverlayPosition(yOffset, xOffset, verticalAlignment, horizontalAlignment, anchor));
            overlay.getConfigOptionFromString("size")
                    .ifPresent(config -> ((Config<OverlaySize>) config).setValue(new OverlaySize(width, height)));
            overlay.getConfigOptionFromString("horizontalAlignmentOverride")
                    .ifPresent(config -> ((Config<HorizontalAlignment>) config).setValue(horizontalOverride));
            overlay.getConfigOptionFromString("verticalAlignmentOverride")
                    .ifPresent(config -> ((Config<VerticalAlignment>) config).setValue(verticalOverride));
        }
    }
}
