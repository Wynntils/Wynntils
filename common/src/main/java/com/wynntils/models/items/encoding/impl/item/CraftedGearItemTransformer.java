/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.item;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.encoding.data.CustomGearTypeData;
import com.wynntils.models.items.encoding.data.CustomIdentificationsData;
import com.wynntils.models.items.encoding.data.DamageData;
import com.wynntils.models.items.encoding.data.DefenseData;
import com.wynntils.models.items.encoding.data.DurabilityData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.PowderData;
import com.wynntils.models.items.encoding.data.RequirementsData;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemDataMap;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CraftedGearItemTransformer extends ItemTransformer<CraftedGearItem> {
    @Override
    public ErrorOr<CraftedGearItem> decodeItem(ItemDataMap itemDataMap) {
        String name;
        GearType gearType;
        int effectStrength;
        CappedValue durability;
        GearRequirements requirements;
        GearAttackSpeed attackSpeed = null;
        int health = 0;
        List<Pair<DamageType, RangedValue>> damages = new ArrayList<>();
        List<Pair<Element, Integer>> defences = new ArrayList<>();
        List<StatPossibleValues> possibleValues = new ArrayList<>();
        List<StatActualValue> identifications = new ArrayList<>();
        int powderSlots = 0;
        List<Powder> powders = new ArrayList<>();

        // Required blocks
        CustomGearTypeData gearTypeData = itemDataMap.get(CustomGearTypeData.class);
        if (gearTypeData == null) {
            return ErrorOr.error("Crafted gear item does not have gear type data!");
        }
        gearType = gearTypeData.gearType();

        DurabilityData durabilityData = itemDataMap.get(DurabilityData.class);
        if (durabilityData == null) {
            return ErrorOr.error("Crafted gear item does not have durability data!");
        }
        effectStrength = durabilityData.effectStrength();
        durability = durabilityData.durability();

        RequirementsData requirementsData = itemDataMap.get(RequirementsData.class);
        if (requirementsData == null) {
            return ErrorOr.error("Crafted gear item does not have requirements data!");
        }
        requirements = requirementsData.requirements();

        // Optional blocks
        // Warning: The name data from Crafted items is deliberately removed from the item data map to prevent
        //           input sanitization issues.
        //           The name data present here is from plain-text string shared after the encoded item.
        NameData nameData = itemDataMap.get(NameData.class);
        if (nameData != null && nameData.name().isPresent()) {
            name = nameData.name().get();
        } else {
            name = "Crafted "
                    + StringUtils.capitalizeFirst(gearTypeData.gearType().name().toLowerCase(Locale.ROOT));
        }

        DamageData damageData = itemDataMap.get(DamageData.class);
        if (damageData != null && damageData.attackSpeed().isPresent()) {
            attackSpeed = damageData.attackSpeed().get();
            damages = damageData.damages();
        }

        DefenseData defenseData = itemDataMap.get(DefenseData.class);
        if (defenseData != null) {
            health = defenseData.health();
            defences = defenseData.defences();
        }

        CustomIdentificationsData identificationsData = itemDataMap.get(CustomIdentificationsData.class);
        if (identificationsData != null) {
            possibleValues = identificationsData.possibleValues();
            // For crafted items, the max values can be used to calculate the current values (from the overall
            // effectiveness).
            identifications = identificationsData.possibleValues().stream()
                    .map(statPossibleValues -> new StatActualValue(
                            statPossibleValues.statType(),
                            Math.round(statPossibleValues.range().high() * effectStrength / 100f),
                            0,
                            RangedValue.NONE))
                    .toList();
        }

        PowderData powderData = itemDataMap.get(PowderData.class);
        if (powderData != null) {
            powderSlots = powderData.powderSlots();
            powders = powderData.powders().stream().map(Pair::a).toList();
        }

        return ErrorOr.of(new CraftedGearItem(
                name,
                effectStrength,
                gearType,
                attackSpeed,
                health,
                damages,
                defences,
                requirements,
                possibleValues,
                identifications,
                powders,
                powderSlots,
                false,
                durability));
    }

    @Override
    protected List<ItemData> encodeItem(CraftedGearItem item, EncodingSettings encodingSettings) {
        List<ItemData> dataList = new ArrayList<>();

        // Required blocks
        dataList.add(new CustomGearTypeData(item.getGearType()));
        dataList.add(new DurabilityData(item.getEffectStrength(), item.getDurability()));
        dataList.add(new RequirementsData(item.getRequirements()));

        // Optional blocks
        if (encodingSettings.shareItemName()) {
            dataList.add(NameData.sanitized(item.getName()));
        }

        dataList.add(new DamageData(item.getAttackSpeed(), item.getDamages()));
        dataList.add(new DefenseData(item.getHealth(), item.getDefences()));
        dataList.add(new CustomIdentificationsData(item.getPossibleValues()));
        dataList.add(PowderData.from(item));

        return dataList;
    }

    @Override
    public ItemType getType() {
        return ItemType.CRAFTED_GEAR;
    }
}
