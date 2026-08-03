/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.item;

import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.items.encoding.data.CustomConsumableTypeData;
import com.wynntils.models.items.encoding.data.CustomIdentificationsData;
import com.wynntils.models.items.encoding.data.EffectsData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.RequirementsData;
import com.wynntils.models.items.encoding.data.UsesData;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemDataMap;
import com.wynntils.models.items.encoding.type.ItemTransformer;
import com.wynntils.models.items.encoding.type.ItemType;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CraftedConsumableItemTransformer extends ItemTransformer<CraftedConsumableItem> {
    @Override
    public ErrorOr<CraftedConsumableItem> decodeItem(ItemDataMap itemDataMap) {
        ConsumableType consumableType;
        int level;
        CappedValue uses;
        List<StatActualValue> identifications = List.of();
        List<NamedItemEffect> namedEffects = List.of();

        // Required blocks
        CustomConsumableTypeData customConsumableTypeData = itemDataMap.get(CustomConsumableTypeData.class);
        if (customConsumableTypeData == null) {
            return ErrorOr.error("Crafted consumable item does not have consumable type data!");
        }
        consumableType = customConsumableTypeData.consumableType();

        UsesData usesData = itemDataMap.get(UsesData.class);
        if (usesData == null) {
            return ErrorOr.error("Crafted consumable item does not have uses data!");
        }
        uses = usesData.uses();

        RequirementsData requirementsData = itemDataMap.get(RequirementsData.class);
        if (requirementsData == null) {
            return ErrorOr.error("Crafted consumable item does not have requirements data!");
        }

        level = requirementsData.requirements().level();

        // Crafted names are shared as ordinary chat text and converted to NameData during decoding.
        NameData nameData = itemDataMap.get(NameData.class);
        String name = nameData != null && nameData.name().isPresent()
                ? nameData.name().get()
                : "Crafted " + StringUtils.capitalizeFirst(consumableType.name().toLowerCase(Locale.ROOT));

        EffectsData effectsData = itemDataMap.get(EffectsData.class);
        if (effectsData != null) {
            namedEffects = effectsData.namedEffects();
        }

        CustomIdentificationsData customIdentificationsData = itemDataMap.get(CustomIdentificationsData.class);
        if (customIdentificationsData != null) {
            identifications = !customIdentificationsData.identifications().isEmpty()
                    ? customIdentificationsData.identifications()
                    : customIdentificationsData.possibleValues().stream()
                            .map(statPossibleValues -> new StatActualValue(
                                    statPossibleValues.statType(),
                                    statPossibleValues.range().high(),
                                    false,
                                    RangedValue.NONE))
                            .toList();
        }

        return ErrorOr.of(
                new CraftedConsumableItem(name, consumableType, level, identifications, namedEffects, List.of(), uses));
    }

    @Override
    protected List<ItemData> encodeItem(CraftedConsumableItem item, EncodingSettings encodingSettings) {
        List<ItemData> dataList = new ArrayList<>();

        dataList.add(new CustomConsumableTypeData(item.getConsumableType()));
        dataList.add(new UsesData(item.getUses()));
        dataList.add(new RequirementsData(
                new GearRequirements(item.getLevel(), Optional.empty(), List.of(), Optional.empty())));

        // Crafted names deliberately remain outside the encoded payload. They are appended as normal
        // chat text by ItemEncodingModel so Wynncraft's chat moderation can inspect them.
        dataList.add(new EffectsData(item.getNamedEffects()));
        dataList.add(new CustomIdentificationsData(item.getPossibleValues(), item.getIdentifications()));

        return dataList;
    }

    @Override
    public ItemType getType() {
        return ItemType.CRAFTED_CONSUMABLE;
    }
}
