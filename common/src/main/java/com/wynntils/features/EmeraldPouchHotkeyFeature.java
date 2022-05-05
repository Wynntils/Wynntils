/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import static com.wynntils.utils.reference.EmeraldSymbols.E_STRING;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class EmeraldPouchHotkeyFeature extends Feature {

    private static final Pattern POUCH_USAGE_PATTERN = Pattern.compile("§6§l(\\d* ?\\d* ?\\d*)" + E_STRING);

    private final KeyHolder emeraldPouchKeybind =
            new KeyHolder("Open Emerald Pouch", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", true, () -> {
                if (!WynnUtils.onWorld()) return;

                Player player = McUtils.player();
                Inventory inventory = player.getInventory();
                HashMap<Integer, Integer> emeraldPouches = new HashMap<>() {};

                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty() && WynnItemMatchers.isEmeraldPouch(stack)) {
                        emeraldPouches.put(i, getPouchUsage(stack));
                    }
                }

                pouchSwitch:
                switch (emeraldPouches.size()) {
                    default:
                        boolean alreadyHasNonEmpty = false;
                        Integer usedPouch = -1;
                        for (Integer key : emeraldPouches.keySet()) {
                            if (emeraldPouches.get(key) > 0
                                    && !alreadyHasNonEmpty) { // We've discovered one pouch with a non-zero balance,
                                // remember this
                                alreadyHasNonEmpty = true;
                                usedPouch = key; // Save our pouch slot ID
                            } else if (emeraldPouches.get(key)
                                    > 0) { // Another pouch has a non-zero balance; notify user
                                // GameUpdateOverlay.queueMessage(TextFormatting.DARK_RED + "You have more than one
                                // filled emerald pouch in your inventory.");
                                break pouchSwitch;
                            }
                        }

                        // At this point, we have either multiple pouches with zero emeralds, or multiple pouches but only one with a non-zero balance
                        // Check to make sure we don't have a bunch of zero balances - if we do, notify user
                        if (!alreadyHasNonEmpty) {
                            // GameUpdateOverlay.queueMessage(TextFormatting.DARK_RED + "You have more than one empty and no filled emerald pouches in your inventory.");
                            break;
                        }

                        // Now, we know we have 1 used pouch and 1+ empty pouches - just open the used one we saved from before
                        McUtils.player()
                                .connection
                                .send(new ServerboundContainerClickPacket(
                                        player.inventoryMenu.containerId,
                                        player.inventoryMenu.getStateId(),
                                        usedPouch,
                                        1,
                                        ClickType.PICKUP,
                                        ItemStack.EMPTY, ));
                        break;

                    case 0:
                        // GameUpdateOverlay.queueMessage(TextFormatting.DARK_RED + "You do not have an emerald pouch in your inventory.");
                        break;
                    case 1:
                        int slotNumber = emeraldPouches
                                .entrySet()
                                .iterator()
                                .next()
                                .getKey(); // We can just get the first value in the HashMap since we only have one value
                        if (slotNumber < 9) {
                            // sendPacket uses raw slot numbers, we need to remap the hotbar
                            slotNumber += 36;
                        }
                        player.connection.sendPacket(new CPacketClickWindow(
                                player.inventoryContainer.windowId,
                                slotNumber,
                                1,
                                ClickType.PICKUP,
                                player.inventory.getStackInSlot(slotNumber),
                                player.inventoryContainer.getNextTransactionID(player.inventory)));
                        break;
                }
            });

    private int getPouchUsage(ItemStack stack) { // TODO: move to EmeraldPouchManager or equivalent when created
        String lore = ItemUtils.getStringLore(stack);
        Matcher usageMatcher = POUCH_USAGE_PATTERN.matcher(lore);
        if (!usageMatcher.find()) {
            if (lore.contains("§7Empty")) {
                return 0;
            }

            return -1;
        }
        return Integer.parseInt(usageMatcher.group(1).replaceAll("\\s", ""));
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.emeraldPouchKeybind.name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        KeyManager.registerKeybind(emeraldPouchKeybind);
        return true;
    }

    @Override
    protected void onDisable() {
        KeyManager.unregisterKeybind(emeraldPouchKeybind);
    }
}
