/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Bounded undo/redo storage; unavailable entries are retained until their target exists again. */
final class HistoryStack<T> {
    private final int limit;
    private final List<T> undo = new ArrayList<>();
    private final List<T> redo = new ArrayList<>();
    private List<T> savedUndo = List.of();
    private List<T> savedRedo = List.of();
    private boolean dirty;

    HistoryStack(int limit) {
        this.limit = limit;
    }

    void record(T edit) {
        dirty = true;
        undo.add(edit);
        if (undo.size() > limit) undo.removeFirst();
        redo.clear();
    }

    T next(boolean forward, Predicate<T> available) {
        List<T> source = forward ? redo : undo;
        for (int i = source.size() - 1; i >= 0; i--) {
            if (available.test(source.get(i))) return source.get(i);
        }
        return null;
    }

    List<T> upcoming(boolean forward, Predicate<T> available, int count) {
        List<T> source = forward ? redo : undo;
        List<T> result = new ArrayList<>(count);
        for (int i = source.size() - 1; i >= 0 && result.size() < count; i--) {
            T edit = source.get(i);
            if (available.test(edit)) result.add(edit);
        }
        return result;
    }

    T restore(boolean forward, Predicate<T> available, Consumer<T> apply) {
        T edit = next(forward, available);
        if (edit == null) return null;
        List<T> source = forward ? redo : undo;
        int index = source.size() - 1;
        while (source.get(index) != edit) index--;
        apply.accept(edit);
        source.remove(index);
        (forward ? undo : redo).add(edit);
        dirty = true;
        return edit;
    }

    void saved(UnaryOperator<T> copy) {
        if (!dirty) return;
        savedUndo = undo.stream().map(copy).toList();
        savedRedo = redo.stream().map(copy).toList();
        dirty = false;
    }

    void discardUnsaved(UnaryOperator<T> copy) {
        undo.clear();
        savedUndo.forEach(edit -> undo.add(copy.apply(edit)));
        redo.clear();
        savedRedo.forEach(edit -> redo.add(copy.apply(edit)));
        dirty = false;
    }

    void clear() {
        undo.clear();
        redo.clear();
        savedUndo = List.of();
        savedRedo = List.of();
        dirty = false;
    }
}
