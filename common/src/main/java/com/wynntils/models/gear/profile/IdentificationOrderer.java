/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.profile;

import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.models.gear.GearIdentificationContainer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;

public class IdentificationOrderer {
    private final Map<String, Integer> order = new HashMap<>();
    private final List<String> groups = new ArrayList<>();
    private final List<String> inverted = new ArrayList<>();

    private transient Map<Integer, Integer> organizedGroups = null;

    public IdentificationOrderer(
            Map<String, Integer> idOrders, ArrayList<String> groupRanges, ArrayList<String> inverted) {}

    /**
     * @param id the identification "short" name. Ex: rawMainAttackNeutralDamage
     * @return the priority level, if not present returns -1
     */
    public int getOrder(String id) {
        return order.getOrDefault(id, -1);
    }

    /**
     * @param id the identification "short" name. Ex: rawMainAttackNeutralDamage
     * @return the group id, if not present returns -1
     */
    private int getGroup(String id) {
        if (organizedGroups == null) organizeGroups();

        return organizedGroups.getOrDefault(getOrder(id), -1);
    }

    /**
     * @param id the identification "short" name. Ex: rawMainAttackNeutralDamage
     * @return if the provided identification status is inverted (negative values are positive)
     */
    public boolean isInverted(String id) {
        return inverted.contains(id);
    }

    /**
     * Order and returns a list of string based on the provided ids
     *
     * @param holder a map containing as key the "short" id name and as value the id lore
     * @param groups if ids should be grouped
     * @return a list with the ordered lore
     */
    public List<StringTag> order(Map<String, StringTag> holder, boolean groups) {
        List<StringTag> result = new ArrayList<>();
        if (holder.isEmpty()) return result;

        // order based on the priority first
        List<Map.Entry<String, StringTag>> ordered = holder.entrySet().stream()
                .sorted(Comparator.comparingInt(c -> getOrder(c.getKey())))
                .toList();

        if (groups) {
            int lastGroup = getGroup(ordered.get(0).getKey()); // first key group to avoid wrong spaces
            for (Map.Entry<String, StringTag> keys : ordered) {
                int currentGroup = getGroup(keys.getKey()); // next key group

                if (currentGroup != lastGroup)
                    result.add(ItemUtils.toLoreStringTag(
                            Component.literal(" "))); // adds a space before if the group is different

                result.add(keys.getValue());
                lastGroup = currentGroup;
            }

            return result;
        }

        ordered.forEach(c -> result.add(c.getValue()));
        return result;
    }

    /**
     * Order and returns a list of string based on the provided ids
     *
     * @param holder a map containing as key the "short" id name and as value the id lore
     * @param groups if ids should be grouped
     * @return a list with the ordered lore
     */
    public List<Component> orderComponents(Map<String, Component> holder, boolean groups) {
        List<Component> result = new ArrayList<>();
        if (holder.isEmpty()) return result;

        // order based on the priority first
        List<Map.Entry<String, Component>> ordered = holder.entrySet().stream()
                .sorted(Comparator.comparingInt(c -> getOrder(c.getKey())))
                .toList();

        if (groups) {
            int lastGroup = getGroup(ordered.get(0).getKey()); // first key group to avoid wrong spaces
            for (Map.Entry<String, Component> keys : ordered) {
                int currentGroup = getGroup(keys.getKey()); // next key group

                if (currentGroup != lastGroup)
                    result.add(Component.literal(" ")); // adds a space before if the group is different

                result.add(keys.getValue());
                lastGroup = currentGroup;
            }

            return result;
        }

        ordered.forEach(c -> result.add(c.getValue()));
        return result;
    }

    public List<GearIdentificationContainer> orderIdentifications(List<GearIdentificationContainer> ids) {
        return ids.stream()
                .sorted(Comparator.comparingInt(id -> getOrder(id.shortIdName())))
                .toList();
    }

    private void organizeGroups() {
        organizedGroups = new HashMap<>();
        for (int id = 0; id < groups.size(); id++) {
            String groupRange = groups.get(id);

            String[] split = groupRange.split("-");

            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);

            // register each range into a reference
            for (int i = min; i <= max; i++) {
                organizedGroups.put(i, id);
            }
        }
    }
}
