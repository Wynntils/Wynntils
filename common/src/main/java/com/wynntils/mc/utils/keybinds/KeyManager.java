/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils.keybinds;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.mixin.accessors.OptionsAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Registers and handles keybinds */
public class KeyManager {
    private static final List<KeyHolder> keyHolders = new ArrayList<>();

    public static void init() {
        WynntilsMod.getEventBus().register(KeyManager.class);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent e) {
        if (e.getTickPhase() == ClientTickEvent.Phase.END) {
            triggerKeybinds();
        }
    }

    public static void registerKeybind(KeyHolder toAdd) {
        if (hasName(toAdd.getName())) {
            throw new IllegalStateException("Can not add " + toAdd + " since the name already exists");
        }

        keyHolders.add(toAdd);

        Options options = Minecraft.getInstance().options;

        if (options == null) { // fabric's modinitalizer runs before options init, instead this is
            // loaded later by a mixin
            return;
        }

        synchronized (options) {
            KeyMapping[] keyMappings = options.keyMappings;

            List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);
            newKeyMappings.add(toAdd.getKeybind());

            ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
        }
    }

    public static void loadKeybinds(Options options) {
        synchronized (options) {
            KeyMapping[] keyMappings = options.keyMappings;

            List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);

            for (KeyHolder holder : keyHolders) {
                if (!newKeyMappings.contains(holder.getKeybind())) {
                    newKeyMappings.add(holder.getKeybind());
                }
            }

            ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
        }
    }

    public static void unregisterKeybind(KeyHolder toAdd) {
        if (keyHolders.remove(toAdd)) {
            Options options = Minecraft.getInstance().options;
            synchronized (options) {
                KeyMapping[] keyMappings = options.keyMappings;

                List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);
                newKeyMappings.remove(toAdd.getKeybind());

                ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
            }
        }
    }

    public static void triggerKeybinds() {
        keyHolders.forEach(k -> {
            if (k.isFirstPress()) {
                if (k.getKeybind().consumeClick()) {
                    k.onPress();
                }

                while (k.getKeybind().consumeClick()) {}

                return;
            }

            if (k.getKeybind().isDown()) {
                k.onPress();
            }
        });
    }

    private static boolean hasName(String name) {
        return keyHolders.stream().anyMatch(k -> k.getName().equals(name));
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
