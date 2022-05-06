/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.Reference;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.utils.reference.EmeraldSymbols;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class EmeraldPouchHotkeyFeature extends Feature {

    private static final Pattern POUCH_USAGE_PATTERN =
            Pattern.compile("§6§l(\\d* ?\\d* ?\\d*)" + EmeraldSymbols.E_STRING);
    private static final Pattern POUCH_CAPACITY_PATTERN =
            Pattern.compile("\\((\\d+)(" + EmeraldSymbols.BLOCKS + "|" + EmeraldSymbols.LE + "|stx) Total\\)");

    private static class EmeraldPouch {
        int slotNumber;
        ItemStack stack;

        private EmeraldPouch(int slotNumber, ItemStack stack) {
            this.slotNumber = slotNumber;
            this.stack = stack;
        }

        public int getSlotNumber() {
            return slotNumber;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int getUsage() {
            return getPouchUsage(stack);
        }

        public int getCapacity() {
            return getPouchCapacity(stack);
        }
    }

    // TODO: change sendMessageToClient to GameUpdateOverlay messages once that's available
    private final KeyHolder emeraldPouchKeybind =
            new KeyHolder("Open Emerald Pouch", GLFW.GLFW_KEY_UNKNOWN, "Wynntils", true, () -> {
                if (!WynnUtils.onWorld()) return;

                Player player = McUtils.player();
                List<EmeraldPouch> emeraldPouches = new ArrayList<>();

                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && WynnItemMatchers.isEmeraldPouch(stack)) {
                        emeraldPouches.add(new EmeraldPouch(i, stack));
                    }
                }

                pouchSwitch:
                switch (emeraldPouches.size()) {
                    case 0 -> McUtils.sendMessageToClient(
                            new TranslatableComponent("feature.wynntils.emeraldPouchKeybind.noPouch")
                                    .withStyle(ChatFormatting.DARK_RED));
                    case 1 -> {
                        // Just get the first value in the HashMap since we only have one pouch
                        int slotNumber = emeraldPouches.get(0).getSlotNumber();
                        if (slotNumber < 9) {
                            slotNumber += 36; // Raw slot numbers, remap if in hotbar
                        }
                        dispatchRightClick(slotNumber, player.getInventory().getItem(slotNumber));
                    }
                    default -> { // More than one emerald pouch
                        Integer slotNumber = -1;
                        ItemStack slotStack = null;
                        boolean hasFilled = false;

                        for (EmeraldPouch ep : emeraldPouches) {
                            if (ep.getUsage() > 0 && !hasFilled) {
                                // Found one pouch with a nonzero balance, remember this
                                hasFilled = true;
                                slotNumber = ep.getSlotNumber(); // Save our pouch slot ID
                                slotStack = ep.getStack();
                            } else if (ep.getUsage() > 0) {
                                // Another pouch has a non-zero balance; notify user
                                McUtils.sendMessageToClient(
                                        new TranslatableComponent("feature.wynntils.emeraldPouchKeybind.multipleFilled")
                                                .withStyle(ChatFormatting.DARK_RED));
                                break pouchSwitch;
                            }
                        }

                        // At this point, we have either multiple pouches with no emeralds, or multiple pouches but
                        // only one with a non-zero balance
                        // Check to make sure we don't have a bunch of zero balances - if we do, open largest capacity
                        if (!hasFilled) {
                            EmeraldPouch largest = null;
                            for (EmeraldPouch ep : emeraldPouches) {
                                if (largest == null || ep.getCapacity() > largest.getCapacity()) {
                                    largest = ep;
                                }
                            }
                            slotNumber = largest.getSlotNumber();
                            slotStack = largest.getStack();
                        }

                        // Now, we know we have 1 used and 1+ empty pouches - open the used one we saved from before
                        if (slotNumber < 9) {
                            slotNumber += 36; // Raw slot numbers, remap if in hotbar
                        }

                        dispatchRightClick(slotNumber, slotStack);
                    }
                }
            });

    private static void dispatchRightClick(int slotNumber, ItemStack stack) {
        Int2ObjectMap<ItemStack> changedSlots = new Int2ObjectOpenHashMap<>();
        changedSlots.putIfAbsent(slotNumber, stack);
        McUtils.player()
                .connection
                .send(new ServerboundContainerClickPacket(
                        McUtils.player().inventoryMenu.containerId,
                        McUtils.player().inventoryMenu.getStateId(),
                        slotNumber,
                        1,
                        ClickType.PICKUP,
                        ItemStack.EMPTY,
                        changedSlots));
    }

    private static int getPouchUsage(ItemStack stack) { // TODO: move to EmeraldPouchManager or equivalent when created
        String lore = ItemUtils.getStringLore(stack);
        Matcher usageMatcher = POUCH_USAGE_PATTERN.matcher(lore);
        if (!usageMatcher.find()) {
            if (lore.contains("§7Empty")) {
                return 0;
            }

            Reference.LOGGER.error(
                    "EmeraldPouchHotkeyFeature#getPouchUsage was called on an ItemStack that wasn't an emerald pouch");
            return -1;
        }
        return Integer.parseInt(usageMatcher.group(1).replaceAll("\\s", ""));
    }

    private static int getPouchCapacity(ItemStack stack) { // TODO: move to EmeraldPouchManager or equiv when created
        String lore = ItemUtils.getStringLore(stack);
        Matcher capacityMatcher = POUCH_CAPACITY_PATTERN.matcher(lore);
        if (!capacityMatcher.find()) {
            Reference.LOGGER.error(
                    "EmeraldPouchHotkeyFeature#getPouchCapacity was called on an ItemStack that wasn't an emerald pouch");
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
