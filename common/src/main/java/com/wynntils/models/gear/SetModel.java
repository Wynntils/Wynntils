/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearSlot;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SetModel extends Model {
    public static final Pattern SET_PATTERN = Pattern.compile("§a(.+) Set §7\\((\\d)/\\d\\)");

    // Stored as a map for quick lookup <name, SetInfo>
    private final Map<String, SetInfo> setData = new HashMap<>();
    private final Map<GearSlot, SetInstance> setInstances = new EnumMap<>(GearSlot.class);

    public SetModel() {
        super(List.of());
        loadSetData();
    }

    public SetInfo getSetInfo(String setId) {
        return setData.getOrDefault(setId, null);
    }

    public SetInfo getSetInfoForItem(String itemName) {
        return getSetInfo(getSetName(itemName));
    }

    /**
     * @param itemName The name of the item to check
     * @return The set name if the item is part of a set, null otherwise
     */
    public String getSetName(String itemName) {
        for (Map.Entry<String, SetInfo> entry : setData.entrySet()) {
            if (entry.getValue().items().contains(itemName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void updateSetInstance(GearSlot slot, SetInstance instance) {
        setInstances.put(slot, instance);
    }

    /**
     * @return A Set of all equipped set names
     */
    public Set<String> getUniqueSetNames() {
        return setInstances.values().stream().map(x -> x.getSetInfo().name()).collect(Collectors.toSet());
    }

    public int getTrueCount(String setName) {
        int trueCount = 0;

        for (ItemStack itemStack : McUtils.inventory().armor) {
            for (StyledText line : LoreUtils.getLore(itemStack)) {
                Matcher nameMatcher = SetModel.SET_PATTERN.matcher(line.getString());
                if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                    trueCount++;
                }
            }
        }

        int[] accessorySlots = {9, 10, 11, 12};
        if (McUtils.player().hasContainerOpen()) {
            // Scale according to server chest size
            // Eg. 3 row chest size = 27 (ends on i=26 since 0-index), we would get accessory slots {27, 28, 29, 30}
            int baseSize = McUtils.player().containerMenu.getItems().size();
            accessorySlots = new int[] {baseSize, baseSize + 1, baseSize + 2, baseSize + 3};
        }
        for (int i : accessorySlots) {
            for (StyledText line : LoreUtils.getLore(McUtils.inventory().getItem(i))) {
                Matcher nameMatcher = SetModel.SET_PATTERN.matcher(line.getString());
                if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                    trueCount++;
                }
            }
        }

        Optional<WynnItem> wynnItem =
                Models.Item.getWynnItem(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND));
        if (wynnItem.isPresent() && wynnItem.get() instanceof GearItem gearItem && gearItem.meetsActualRequirements()) {
            for (StyledText line : LoreUtils.getLore(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND))) {
                Matcher nameMatcher = SetModel.SET_PATTERN.matcher(line.getString());
                if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                    trueCount++;
                }
            }
        }

        return trueCount;
    }

    public boolean hasSetData() {
        return !setData.isEmpty();
    }

    private void loadSetData() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_SETS);
        dl.handleReader(reader -> {
            TypeToken<Map<String, RawSetInfo>> type = new TypeToken<>() {};
            Map<String, RawSetInfo> rawSets = Managers.Json.GSON.fromJson(reader, type.getType());
            rawSets.forEach((setName, rawSetInfo) -> {
                List<Map<StatType, Integer>> bonuses = rawSetInfo.bonuses.stream()
                        .map(bonusPair -> {
                            Map<StatType, Integer> bonusMap = new HashMap<>();
                            bonusPair.forEach((statName, statValue) -> {
                                StatType statType = Models.Stat.fromApiName(statName);
                                if (statType == null) {
                                    WynntilsMod.warn("Unknown stat type: " + statName);
                                }
                                bonusMap.put(statType, statValue);
                            });
                            return bonusMap;
                        })
                        .toList();

                setData.put(setName, new SetInfo(setName, bonuses, rawSetInfo.items));
            });
        });
    }

    private static class RawSetInfo {
        public List<Map<String, Integer>> bonuses;
        public List<String> items;
    }
}
