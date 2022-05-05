/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

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
import com.wynntils.utils.objects.Pair;
import com.wynntils.utils.reference.EmeraldSymbols;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final Pattern POUCH_USAGE_PATTERN = Pattern.compile("§6§l(\\d* ?\\d* ?\\d*)" + EmeraldSymbols.E_STRING);
    private static final Pattern POUCH_CAPACITY_PATTERN = Pattern.compile("\\((\\d+)(" + EmeraldSymbols.BLOCKS + "|" + EmeraldSymbols.LE + "|stx) Total\\)");


    private final KeyHolder emeraldPouchKeybind = // TODO: implement GameUpdateOverlay messages once that's available
            new KeyHolder("Open Emerald Pouch", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", true, () -> {
                if (!WynnUtils.onWorld()) return;

                Player player = McUtils.player();
                Inventory inventory = player.getInventory();
                HashMap<Integer, Pair<ItemStack, Pair<Integer, Integer>>> emeraldPouches = new HashMap<>() {};

                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (!stack.isEmpty() && WynnItemMatchers.isEmeraldPouch(stack)) {
                        emeraldPouches.put(i, new Pair<>(stack, new Pair<>(getPouchUsage(stack), getPouchCapacity(stack))));
                    }
                }
                Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();

                pouchSwitch:
                switch (emeraldPouches.size()) {
                    default -> {
                        Integer slotNumber = -1;
                        ItemStack slotStack = null;

                        boolean alreadyHasNonEmpty = false;
                        Integer usedPouch = -1;
                        ItemStack pouchStack = null;
                        for (Integer key : emeraldPouches.keySet()) {
                            if (emeraldPouches.get(key).b.a > 0
                                    && !alreadyHasNonEmpty) { // Found one pouch with a nonzero balance, remember this
                                alreadyHasNonEmpty = true;
                                usedPouch = key; // Save our pouch slot ID
                                pouchStack = emeraldPouches.get(key).a;
                            } else if (emeraldPouches.get(key).b.a
                                    > 0) { // Another pouch has a non-zero balance; notify user
                                // GameUpdateOverlay.queueMessage(TextFormatting.DARK_RED + "You have more than one
                                // filled emerald pouch in your inventory.");
                                break pouchSwitch;
                            }
                        }

                        // At this point, we have either multiple pouches with no emeralds, or multiple pouches but
                        // only one with a non-zero balance
                        // Check to make sure we don't have a bunch of zero balances - if we do, open largest capacity
                        if (!alreadyHasNonEmpty) {
                            Map.Entry<Integer, Pair<ItemStack, Pair<Integer, Integer>>> largest = null;
                            for (Map.Entry<Integer, Pair<ItemStack, Pair<Integer, Integer>>> entry : emeraldPouches.entrySet()) {
                                if (largest == null || entry.getValue().b.b > largest.getValue().b.b) {
                                    largest = entry;
                                }
                            }
                            slotNumber = largest.getKey();
                            slotStack = largest.getValue().a;
                        }

                        // Now, we know we have 1 used pouch and 1+ empty pouches - just open the used one we saved from
                        // before
                        if (slotNumber == -1) {
                            slotNumber = usedPouch;
                        }
                        if (slotStack == null) {
                            slotStack = pouchStack;
                        }
                        changedSlots.putIfAbsent(slotNumber, slotStack);
                        System.out.println("Attempting to send click to " + slotNumber + " " + player.getInventory().getItem(slotNumber).getDisplayName().getString());
                        McUtils.player()
                                .connection
                                .send(new ServerboundContainerClickPacket(
                                        player.inventoryMenu.containerId,
                                        player.inventoryMenu.getStateId(),
                                        slotNumber,
                                        1,
                                        ClickType.PICKUP,
                                        ItemStack.EMPTY,
                                        changedSlots));
                    }
                    case 0 -> System.out.println("No EP found");
                        // GameUpdateOverlay.queueMessage(TextFormatting.DARK_RED + "You do not have an emerald pouch in
                        // your inventory.");

                    case 1 -> {
                        int slotNumber = emeraldPouches
                                .entrySet()
                                .iterator()
                                .next()
                                .getKey(); // We can just get the first value in the HashMap since we only have one value

                        if (slotNumber < 9) {
                            // sendPacket uses raw slot numbers, we need to remap the hotbar
                            slotNumber += 36;
                        }
                        changedSlots.putIfAbsent(
                                slotNumber, player.getInventory().getItem(slotNumber));
                        McUtils.player()
                                .connection
                                .send(new ServerboundContainerClickPacket(
                                        player.inventoryMenu.containerId,
                                        player.inventoryMenu.getStateId(),
                                        slotNumber,
                                        1,
                                        ClickType.PICKUP,
                                        ItemStack.EMPTY,
                                        changedSlots));
                    }
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

    private int getPouchCapacity(ItemStack stack) { // TODO: move to EmeraldPouchManager or equiv when created
        String lore = ItemUtils.getStringLore(stack);
        Matcher capacityMatcher = POUCH_CAPACITY_PATTERN.matcher(lore);
        if (!capacityMatcher.find()) {
            return -1;
        }
        int capacity = Integer.parseInt(capacityMatcher.group(1)) * 64;
        if (capacityMatcher.group(2).equals(EmeraldSymbols.LE)) capacity *= 64;
        if (capacityMatcher.group(2).equals("stx")) capacity *= 4096;
        return capacity;
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
