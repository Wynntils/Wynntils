/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.items.items.game.GearItem;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator implements ItemAnnotator {
    private static final Pattern GEAR_PATTERN =
            Pattern.compile("^(?:§f⬡ )?(?<rarity>§[5abcdef])(?<unidentified>Unidentified )?(?:Shiny )?(?<name>.+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(GEAR_PATTERN);
        if (!matcher.matches()) return null;

        // Lookup Gear Profile
        String itemName = matcher.group("name");
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(itemName);
        if (gearInfo == null) return null;

        // Verify that rarity matches
        if (!matcher.group("rarity").equals(gearInfo.tier().getChatFormatting().toString())) return null;

        GearInstance gearInstance =
                matcher.group("unidentified") != null ? null : Models.Gear.parseInstance(gearInfo, itemStack);

        // TODO: Add SetInstance
        Optional<SetInfo> setInfo = Optional.empty();
        Optional<SetInstance> setInstance = Optional.empty();
        if (gearInfo.tier() == GearTier.SET) {
            // Parse set info (mostly from SetModel)
            String setName = Models.Set.getSetName(gearInfo.name());
            setInfo = Optional.of(Models.Set.getSetInfo(setName));

            // Parse set instance

        }
        return new GearItem(gearInfo, gearInstance, setInfo, setInstance);
    }

    // Set parsing
    Optional<SetInstance> setInstance = Optional.empty();
        if (gearInfo.tier() == GearTier.SET && gearInfo.setInfo().isPresent()) {
        // We have a set and the information required to make instances
        // Go through all of user's active gear and figure out how many of this set is active
        Pair<Integer, Integer> setCount = getActiveSetCount(gearInfo.setInfo().get().name());
        SetInfo setInfo = gearInfo.setInfo().get();

        // we are wearing some item from this set
        setInstance = Optional.of(new SetInstance(setInfo, setCount.a(), setCount.b(), activeSetsCache.get(setInfo.name())));
        if (!activeSetsCache.containsKey(setInfo.name())) {
            activeSetsCache.put(setInfo.name(), new ArrayList<>());
        }
        activeSetsCache.get(setInfo.name()).add(setInstance.get());
        activeSetsCache.get(setInfo.name()).forEach(si -> {
            if (si.getWynncraftCount() != setCount.a()) {
                si.setWynncraftCount(setCount.a());
            }
            if (si.getTrueCount() != setCount.b()) {
                si.setTrueCount(setCount.b());
            }
            if (si.getSetInstances() != activeSetsCache.get(setInfo.name())) {
                si.setSetInstances(activeSetsCache.get(setInfo.name()));
            }
            System.out.println("updated SetInstance to " + si);
        });

        // todo remove
        if (gearInfo.name().equals("Morph-Gold")) {
            System.out.println("Found SetInstance for " + gearInfo.name() + ": " + setInstance);
        } else {
//                System.out.println("Found SetInstance for " + gearInfo.name() + ": " + setInstance);
        }
    }

    /**
     * Goes through user's equipped gear (all armour, accessories, held weapon) and returns count of specified
     * @return <wynncraft count, true count>
     */
    private Pair<Integer, Integer> getActiveSetCount(String setName) {
        Pair<Integer, Integer> count = Pair.of(0, 0);

        for (ItemStack itemStack : McUtils.inventory().armor) {
            int wynncraftCount = countSet(itemStack, setName);
            if (wynncraftCount == -1) continue;

            count = Pair.of(wynncraftCount, count.b() + 1);
        }

        int[] accessorySlots = {9, 10, 11, 12};
        if (McUtils.player().hasContainerOpen()) {
            // Scale according to server chest size
            // Eg. 3 row chest size = 27 (ends on i=26 since 0-index), we would get accessory slots {27, 28, 29, 30}
            int baseSize = McUtils.player().containerMenu.getItems().size();
            accessorySlots = new int[]{baseSize, baseSize + 1, baseSize + 2, baseSize + 3};
        }
        for (int i : accessorySlots) {
            ItemStack itemStack = McUtils.inventory().getItem(i);
            int wynncraftCount = countSet(itemStack, setName);
            if (wynncraftCount == -1) continue;

            count = Pair.of(wynncraftCount, count.b() + 1);
        }

        // held item - must check if it's actually valid before counting
        ItemStack itemInHand = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (InventoryUtils.itemRequirementsMet(itemInHand)) {
            int wynncraftCount = countSet(itemInHand, setName);
            if (wynncraftCount == -1) return count;

            count = Pair.of(wynncraftCount, count.b() + 1);
        }

        return count;
    }

    /**
     * @return Wynncraft's count of the set if the set matches the specified name, or -1 if it doesn't
     */
    private int countSet(ItemStack itemStack, String setName) {
        for (StyledText line : LoreUtils.getLore(itemStack)) {
            Matcher nameMatcher = SET_PATTERN.matcher(line.getString());
            if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                return Integer.parseInt(nameMatcher.group(2));
            }
        }
        return -1;
    }
}
