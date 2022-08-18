/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.google.common.collect.Lists;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.mixin.accessors.OptionsAccessor;
import com.wynntils.mc.utils.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Registers and handles keybinds */
public final class KeyBindManager extends CoreManager {
    private static final List<KeyBind> KEY_BINDS = new ArrayList<>();

    /** Needed for all Models */
    public static void init() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent e) {
        if (e.getTickPhase() == ClientTickEvent.Phase.END) {
            triggerKeybinds();
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InventoryKeyPressEvent e) {
        KEY_BINDS.forEach(keyBind -> {
            if (keyBind.getKeyMapping().matches(e.getKeyCode(), e.getScanCode())) {
                keyBind.onInventoryPress(e.getHoveredSlot());
            }
        });
    }

    public static void registerKeybind(KeyBind toAdd) {
        if (hasName(toAdd.getName())) {
            throw new IllegalStateException("Can not add " + toAdd + " since the name already exists");
        }

        KEY_BINDS.add(toAdd);

        Options options = McUtils.options();

        if (options == null) { // fabric's modinitalizer runs before options init, instead this is
            // loaded later by a mixin
            return;
        }

        synchronized (options) {
            KeyMapping[] keyMappings = options.keyMappings;

            List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);
            newKeyMappings.add(toAdd.getKeyMapping());

            ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
        }
    }

    public static void loadKeybinds(Options options) {
        synchronized (options) {
            KeyMapping[] keyMappings = options.keyMappings;

            List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);

            for (KeyBind keyBind : KEY_BINDS) {
                if (!newKeyMappings.contains(keyBind.getKeyMapping())) {
                    newKeyMappings.add(keyBind.getKeyMapping());
                }
            }

            ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
        }
    }

    public static void unregisterKeybind(KeyBind toAdd) {
        if (KEY_BINDS.remove(toAdd)) {
            Options options = McUtils.options();
            synchronized (options) {
                KeyMapping[] keyMappings = options.keyMappings;

                List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);
                newKeyMappings.remove(toAdd.getKeyMapping());

                ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
            }
        }
    }

    private static void triggerKeybinds() {
        KEY_BINDS.forEach(keyBind -> {
            if (keyBind.isFirstPress()) {
                if (keyBind.getKeyMapping().consumeClick()) {
                    keyBind.onPress();
                }

                while (keyBind.getKeyMapping().consumeClick()) {
                    // do nothing
                }

                return;
            }

            if (keyBind.getKeyMapping().isDown()) {
                keyBind.onPress();
            }
        });
    }

    private static boolean hasName(String name) {
        return KEY_BINDS.stream().anyMatch(k -> k.getName().equals(name));
    }

    public static void initKeyMapping(String category, Map<String, Integer> categorySortOrder) {
        if (categorySortOrder.containsKey(category)) return;

        int max = 0;

        for (int val : categorySortOrder.values()) {
            if (val > max) {
                max = val;
            }
        }

        categorySortOrder.put(category, max + 1);
    }
}
