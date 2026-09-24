/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import com.google.gson.JsonElement;
import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.OverlayGroupHolder;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Client-lifetime editor history. Capture happens at interaction boundaries, never while rendering. */
public final class OverlayHistory {
    private static final int MAX_EDITS = 100;
    private static final int TOOLTIP_EDITS = 5;
    private static final Set<String> PLACEMENT = Set.of(
            "position",
            "size",
            "horizontalAlignmentOverride",
            "verticalAlignmentOverride",
            "placementLocked",
            "renderOrder",
            "renderElement");

    private final boolean placement;
    private final HistoryStack<Edit> history = new HistoryStack<>(MAX_EDITS);
    private final Map<String, Map<String, Value>> pending = new LinkedHashMap<>();

    public OverlayHistory(boolean placement) {
        this.placement = placement;
    }

    public static boolean isPlacement(Config<?> config) {
        return PLACEMENT.contains(config.getFieldName());
    }

    public void begin(Overlay overlay) {
        finish();
        include(overlay);
    }

    public void include(Overlay overlay) {
        if (overlay == null) return;
        String key = Managers.Overlay.getOverlayKey(overlay);
        pending.computeIfAbsent(key, ignored -> capture(overlay, placement));
    }

    public boolean isPending() {
        return !pending.isEmpty();
    }

    public void finish() {
        if (pending.isEmpty()) return;
        List<Change> changes = new ArrayList<>();
        pending.forEach((key, before) -> {
            Overlay overlay = Managers.Overlay.findOverlay(key);
            if (overlay == null) return;
            boolean enabledChanged = before.containsKey("userEnabled")
                    && overlay.getConfigOptionFromString("userEnabled")
                            .map(config -> !before.get("userEnabled").equals(Value.capture(config)))
                            .orElse(false);
            for (Config<?> config : overlay.getConfigOptions()) {
                if (!placement && config.getFieldName().equals("renderOrder") && !enabledChanged) continue;
                Value value = before.get(config.getFieldName());
                if (value == null) continue;
                Value after = Value.capture(config);
                if (!value.equals(after)) changes.add(new Change(key, config.getFieldName(), value, after));
            }
        });
        pending.clear();
        if (!changes.isEmpty()) record(new SettingsEdit(List.copyOf(changes), placement));
    }

    public void created(OverlayGroupHolder group, Overlay overlay) {
        finish();
        record(new ExistenceEdit(
                group.getConfigKey(), ((DynamicOverlay) overlay).getId(), true, capture(overlay, null)));
    }

    public void delete(OverlayGroupHolder group, Overlay overlay) {
        finish();
        Edit edit = new ExistenceEdit(
                group.getConfigKey(), ((DynamicOverlay) overlay).getId(), false, capture(overlay, null));
        Managers.Overlay.removeSingleOverlay(group, ((DynamicOverlay) overlay).getId());
        record(edit);
    }

    private void record(Edit edit) {
        history.record(edit);
    }

    public Overlay undo() {
        return restore(false);
    }

    public Overlay redo() {
        return restore(true);
    }

    private Overlay restore(boolean forward) {
        finish();
        Overlay[] result = new Overlay[1];
        history.restore(
                forward,
                Edit::available,
                edit -> Managers.Overlay.batchOverlayUpdates(() -> result[0] = edit.apply(forward)));
        return result[0];
    }

    public boolean canUndo() {
        return history.next(false, Edit::available) != null;
    }

    public boolean canRedo() {
        return history.next(true, Edit::available) != null;
    }

    public Component tooltip(boolean forward) {
        return Component.empty()
                .append(Component.translatable(
                                "screens.wynntils.overlaySettings.history." + (forward ? "redo" : "undo"))
                        .withStyle(ChatFormatting.GOLD))
                .append("\n\n")
                .append(actionList(forward));
    }

    private Component actionList(boolean forward) {
        List<Component> descriptions = new ArrayList<>(TOOLTIP_EDITS);
        if (!forward && !pending.isEmpty()) {
            Overlay overlay =
                    Managers.Overlay.findOverlay(pending.keySet().iterator().next());
            if (overlay != null)
                descriptions.add(Component.translatable(
                        "screens.wynntils.overlaySettings.history.edit", overlay.getTranslatedName()));
        }
        for (Edit edit : history.upcoming(forward, Edit::available, TOOLTIP_EDITS - descriptions.size())) {
            descriptions.add(edit.description());
        }
        MutableComponent tooltip = Component.empty();
        if (descriptions.isEmpty()) {
            tooltip.append(Component.translatable(
                            "screens.wynntils.overlaySettings.history." + (forward ? "emptyRedo" : "emptyUndo"))
                    .withStyle(ChatFormatting.GRAY));
        }
        for (int i = 0; i < descriptions.size(); i++) {
            if (i > 0) tooltip.append("\n");
            tooltip.append(descriptions.get(i).copy().withStyle(i == 0 ? ChatFormatting.WHITE : ChatFormatting.GRAY));
        }
        return tooltip;
    }

    public Component tooltip() {
        return Component.empty()
                .append(Component.translatable("screens.wynntils.overlaySettings.history.title")
                        .withStyle(ChatFormatting.GOLD))
                .append("\n\n")
                .append(actionList(false))
                .append("\n\n")
                .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable("screens.wynntils.overlaySettings.history.leftClick")
                        .withStyle(ChatFormatting.GREEN))
                .append("\n")
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.translatable("screens.wynntils.overlaySettings.history.rightClick")
                        .withStyle(ChatFormatting.GREEN));
    }

    public boolean restoreConfig(Config<?> config, boolean forward) {
        finish();
        Edit edit = history.next(forward, Edit::available);
        if (!(Managers.Persisted.getMetadata(config).owner() instanceof Overlay overlay)
                || !(edit instanceof SettingsEdit settings)
                || settings.changes().stream()
                        .noneMatch(change -> change.overlay().equals(Managers.Overlay.getOverlayKey(overlay))
                                && change.field().equals(config.getFieldName()))) return false;
        restore(forward);
        return true;
    }

    public void saved() {
        finish();
        history.saved(Edit::copy);
    }

    public void discardUnsaved() {
        pending.clear();
        history.discardUnsaved(Edit::copy);
    }

    public void clear() {
        pending.clear();
        history.clear();
    }

    private static Map<String, Value> capture(Overlay overlay, Boolean placement) {
        Map<String, Value> values = new LinkedHashMap<>();
        for (Config<?> config : overlay.getConfigOptions()) {
            if (placement == null
                    || isPlacement(config) == placement
                    || (!placement && config.getFieldName().equals("renderOrder"))) {
                values.put(config.getFieldName(), Value.capture(config));
            }
        }
        return values;
    }

    private record Value(JsonElement json, boolean userEdited) {
        static Value capture(Config<?> config) {
            return new Value(Managers.Json.GSON.toJsonTree(config.get(), config.getType()), config.userEdited());
        }

        void apply(Config<?> config) {
            config.restoreEditorValue(Managers.Json.GSON.fromJson(json, config.getType()), userEdited);
        }
    }

    private record Change(String overlay, String field, Value before, Value after) {}

    private interface Edit {
        boolean available();

        Overlay apply(boolean forward);

        Component description();

        default Edit copy() {
            return this;
        }
    }

    private record SettingsEdit(List<Change> changes, boolean placement) implements Edit {
        @Override
        public Component description() {
            Change change = changes.getFirst();
            Overlay overlay = Managers.Overlay.findOverlay(change.overlay());
            String setting = overlay.getConfigOptionFromString(change.field())
                    .map(Config::getDisplayName)
                    .orElse(change.field());
            return Component.translatable(
                    "screens.wynntils.overlaySettings.history.change", overlay.getTranslatedName(), setting);
        }

        @Override
        public boolean available() {
            return changes.stream().allMatch(change -> Managers.Overlay.findOverlay(change.overlay()) != null);
        }

        @Override
        public Overlay apply(boolean forward) {
            boolean renderOrder = false;
            for (Change change : changes) {
                if (change.field().equals("renderOrder")) continue;
                Overlay overlay = Managers.Overlay.findOverlay(change.overlay());
                overlay.getConfigOptionFromString(change.field())
                        .ifPresent(config -> (forward ? change.after() : change.before()).apply(config));
                renderOrder |=
                        change.field().equals("renderOrder") || change.field().equals("renderElement");
            }
            for (Change change : changes) {
                if (!change.field().equals("renderOrder")) continue;
                Managers.Overlay.findOverlay(change.overlay())
                        .getConfigOptionFromString(change.field())
                        .ifPresent(config -> (forward ? change.after() : change.before()).apply(config));
                if (!placement) {
                    Overlay overlay = Managers.Overlay.findOverlay(change.overlay());
                    Managers.Overlay.restoreOverlayOrder(overlay, overlay.getRenderOrder());
                }
                renderOrder = true;
            }
            if (renderOrder) Managers.Overlay.rebuildAndNormalizeRenderOrder();
            return Managers.Overlay.findOverlay(changes.getFirst().overlay());
        }
    }

    private static final class ExistenceEdit implements Edit {
        private final String groupKey;
        private final int id;
        private final boolean created;
        private Map<String, Value> values;

        private ExistenceEdit(String groupKey, int id, boolean created, Map<String, Value> values) {
            this.groupKey = groupKey;
            this.id = id;
            this.created = created;
            this.values = values;
        }

        @Override
        public Edit copy() {
            return new ExistenceEdit(groupKey, id, created, values);
        }

        private OverlayGroupHolder group() {
            return Managers.Overlay.getOverlayGroups().stream()
                    .filter(group -> group.getConfigKey().equals(groupKey))
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public Component description() {
            return Component.translatable(
                    "screens.wynntils.overlaySettings.history." + (created ? "create" : "delete"), id);
        }

        @Override
        public Overlay apply(boolean forward) {
            OverlayGroupHolder group = group();
            if (forward == created) {
                Overlay overlay = Managers.Overlay.addSingleOverlay(group, id);
                values.forEach((field, value) -> {
                    if (!field.equals("renderOrder"))
                        overlay.getConfigOptionFromString(field).ifPresent(value::apply);
                });
                // Enabling can assign a default order, so restore the saved order last.
                overlay.getConfigOptionFromString("renderOrder")
                        .ifPresent(config -> values.get("renderOrder").apply(config));
                Managers.Overlay.restoreOverlayOrder(overlay, overlay.getRenderOrder());
                return overlay;
            }
            Overlay overlay = group.getOverlays().stream()
                    .filter(candidate -> ((DynamicOverlay) candidate).getId() == id)
                    .findFirst()
                    .orElseThrow();
            // Preserve intervening placement edits when undoing creation or redoing deletion.
            values = capture(overlay, null);
            Managers.Overlay.removeSingleOverlay(group, id);
            return null;
        }
    }
}
