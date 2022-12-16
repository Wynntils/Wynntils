/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Manager;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.mixin.accessors.OptionsAccessor;
import com.wynntils.mc.utils.McUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Registers and handles keybinds */
public final class KeyBindManager extends Manager {
    private final Set<KeyBind> keyBinds = ConcurrentHashMap.newKeySet();

    public KeyBindManager() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.End e) {
        triggerKeybinds();
    }

    @SubscribeEvent
    public void onKeyPress(InventoryKeyPressEvent e) {
        checkAllKeyBinds(keyBind -> {
            if (keyBind.getKeyMapping().matches(e.getKeyCode(), e.getScanCode())) {
                keyBind.onInventoryPress(e.getHoveredSlot());
            }
        });
    }

    @SubscribeEvent
    public void onMousePress(InventoryMouseClickedEvent e) {
        checkAllKeyBinds(keyBind -> {
            if (keyBind.getKeyMapping().matchesMouse(e.getButton())) {
                keyBind.onInventoryPress(e.getHoveredSlot());
            }
        });
    }

    public void registerKeybind(KeyBind toAdd) {
        if (hasName(toAdd.getName())) {
            throw new IllegalStateException(
                    "Can not add keybind " + toAdd.getName() + " since the name already exists");
        }

        KeyMapping keyMapping = toAdd.getKeyMapping();

        synchronized (McUtils.options()) {
            keyBinds.add(toAdd);

            Options options = McUtils.options();
            KeyMapping[] keyMappings = options.keyMappings;

            List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);
            newKeyMappings.add(keyMapping);

            ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
        }

        // Bind keybind to its default key, however, this might get overwritten by options loading later
        keyMapping.setKey(keyMapping.getDefaultKey());
        KeyMapping.resetMapping();
    }

    public void unregisterKeybind(KeyBind toRemove) {
        if (!keyBinds.remove(toRemove)) return;

        KeyMapping keyMapping = toRemove.getKeyMapping();

        synchronized (McUtils.options()) {
            Options options = McUtils.options();
            KeyMapping[] keyMappings = options.keyMappings;

            List<KeyMapping> newKeyMappings = Lists.newArrayList(keyMappings);
            newKeyMappings.remove(toRemove.getKeyMapping());

            ((OptionsAccessor) options).setKeyBindMixins(newKeyMappings.toArray(new KeyMapping[0]));
        }

        // Unbind keybind
        keyMapping.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();
    }

    private void triggerKeybinds() {
        checkAllKeyBinds(keyBind -> {
            if (keyBind.onlyFirstPress()) {
                while (keyBind.getKeyMapping().consumeClick()) {
                    keyBind.onPress();
                }
            } else if (keyBind.getKeyMapping().isDown()) {
                keyBind.onPress();
            }
        });
    }

    private void checkAllKeyBinds(Consumer<KeyBind> checkKeybind) {
        List<KeyBind> crashedKeyBinds = new LinkedList<>();

        for (KeyBind keyBind : keyBinds) {
            try {
                checkKeybind.accept(keyBind);
            } catch (Throwable t) {
                WynntilsMod.error("Exception when handling key bind " + keyBind, t);
                WynntilsMod.warn("This key bind will be disabled");
                McUtils.sendMessageToClient(
                        new TextComponent("Wynntils error: Key bind " + keyBind + " has crashed and will be disabled")
                                .withStyle(ChatFormatting.RED));
                // We can't disable it right away since that will cause ConcurrentModificationException
                crashedKeyBinds.add(keyBind);
            }
        }

        // Hopefully we have none :)
        for (KeyBind keyBind : crashedKeyBinds) {
            unregisterKeybind(keyBind);
        }
    }

    private boolean hasName(String name) {
        return keyBinds.stream().anyMatch(k -> k.getName().equals(name));
    }

    /**
     * Note: this is called directly from a mixin!
     */
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
