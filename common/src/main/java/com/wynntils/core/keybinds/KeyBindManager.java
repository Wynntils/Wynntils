/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.mixin.accessors.OptionsAccessor;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class KeyBindManager extends Manager {
    public static final KeyMapping.Category CHAT_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "chat"));
    public static final KeyMapping.Category COMBAT_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "combat"));
    public static final KeyMapping.Category COMMANDS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "commands"));
    public static final KeyMapping.Category INVENTORY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "inventory"));
    public static final KeyMapping.Category MAP_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "map"));
    public static final KeyMapping.Category OVERLAYS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "overlays"));
    public static final KeyMapping.Category PLAYERS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "players"));
    public static final KeyMapping.Category TOOLTIPS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "tooltips"));
    public static final KeyMapping.Category TRADEMARKET_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "trademarket"));
    public static final KeyMapping.Category UI_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "ui"));
    public static final KeyMapping.Category UTILITIES_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "utilities"));
    public static final KeyMapping.Category DEBUG_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WynntilsMod.MOD_ID, "debug"));

    private final Set<KeyBind> enabledKeyBinds = ConcurrentHashMap.newKeySet();
    private final Map<Feature, List<KeyBind>> keyBinds = new ConcurrentHashMap<>();
    private final Map<String, KeyMapping> mappingsById = new ConcurrentHashMap<>();

    private boolean registeredKeybinds = false;

    public KeyBindManager() {
        super(List.of());
    }

    public void registerKeybinds(Options options) {
        if (registeredKeybinds) return;
        registeredKeybinds = true;

        List<KeyMapping> list = new ArrayList<>(Arrays.asList(options.keyMappings));

        for (KeyBindDefinition def : KeyBindDefinition.definitions()) {
            KeyMapping mapping = new KeyMapping(def.name(), def.type(), def.defaultKey(), def.category());

            list.add(mapping);
            mappingsById.put(def.id(), mapping);
        }

        ((OptionsAccessor) options).setKeyBindMixins(list.toArray(KeyMapping[]::new));
        KeyMapping.resetMapping();
    }

    public KeyBind createKeyBind(KeyBindDefinition definition, Runnable onPress, Consumer<Slot> onInventoryPress) {
        KeyMapping mapping = mappingsById.get(definition.id());
        if (mapping == null) {
            throw new IllegalStateException("KeyMapping for " + definition.id() + " not registered!");
        }

        return new KeyBind(definition, mapping, onPress, onInventoryPress);
    }

    public void discoverKeyBinds(Feature feature) {
        for (Field f : FieldUtils.getFieldsWithAnnotation(feature.getClass(), RegisterKeyBind.class)) {
            if (!f.getType().equals(KeyBind.class)) continue;

            try {
                KeyBind keyBind = (KeyBind) FieldUtils.readField(f, feature, true);
                keyBinds.putIfAbsent(feature, new LinkedList<>());
                keyBinds.get(feature).add(keyBind);
            } catch (Exception e) {
                WynntilsMod.error(
                        "Failed to register KeyBind " + f.getName() + " in "
                                + feature.getClass().getName(),
                        e);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        triggerKeybinds();
    }

    @SubscribeEvent
    public void onKeyPress(InventoryKeyPressEvent e) {
        checkAllKeyBinds(keyBind -> {
            if (keyBind.getKeyMapping().matches(e.getKeyEvent())) {
                keyBind.onInventoryPress(e.getHoveredSlot());
            }
        });
    }

    @SubscribeEvent
    public void onMousePress(InventoryMouseClickedEvent e) {
        checkAllKeyBinds(keyBind -> {
            if (keyBind.getKeyMapping().matchesMouse(e.getMouseButtonEvent())) {
                keyBind.onInventoryPress(e.getHoveredSlot());
            }
        });
    }

    public void enableFeatureKeyBinds(Feature feature) {
        List<KeyBind> list = keyBinds.getOrDefault(feature, new ArrayList<>());
        enabledKeyBinds.addAll(list);
    }

    public void disableFeatureKeyBinds(Feature feature) {
        List<KeyBind> list = keyBinds.getOrDefault(feature, new ArrayList<>());
        list.forEach(enabledKeyBinds::remove);
    }

    private void triggerKeybinds() {
        checkAllKeyBinds(keyBind -> {
            if (keyBind.onlyFirstPress()) {
                if (keyBind.getKeyMapping().isDown() && !keyBind.isPressed()) {
                    keyBind.onPress();
                }

                keyBind.setIsPressed(keyBind.getKeyMapping().isDown());
            } else if (keyBind.getKeyMapping().isDown()) {
                keyBind.onPress();
            }
        });
    }

    private void checkAllKeyBinds(Consumer<KeyBind> checkKeybind) {
        if (!Managers.Connection.onServer()) return;

        List<Pair<Feature, KeyBind>> crashedKeyBinds = new LinkedList<>();

        for (Feature parent : keyBinds.keySet()) {
            for (KeyBind keyBind : keyBinds.get(parent)) {
                try {
                    checkKeybind.accept(keyBind);
                } catch (Throwable t) {
                    // We can't disable it right away since that will cause ConcurrentModificationException
                    crashedKeyBinds.add(Pair.of(parent, keyBind));

                    WynntilsMod.reportCrash(
                            CrashType.KEYBIND,
                            keyBind.getName(),
                            parent.getClass().getName() + "." + keyBind.getName(),
                            "handling",
                            t);
                }
            }
        }

        // Hopefully we have none :)
        for (Pair<Feature, KeyBind> keyBindPair : crashedKeyBinds) {
            enabledKeyBinds.remove(keyBindPair.value());
        }
    }
}
