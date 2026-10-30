/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.parsing.WynnItemLoreParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemMiscStatsParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.models.wynnitem.parsing.WynnItemStatsParseResult;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

public final class ItemPageAnnotator {
    public static void update(PagedItemProperty item, ItemStack stack) {
        int page = WynnItemParser.parseTooltipPage(stack);

        if (page == 0) {
            Map<StatType, StatPossibleValues> ranges =
                    switch (item) {
                        case GearItem gear -> gear.getItemInfo().getVariableStatsMap();
                        case CharmItem charm -> charm.getItemInfo().getVariableStatsMap();
                        case CraftedGearItem crafted -> new HashMap<>();
                        default -> null;
                    };

            WynnItemStatsParseResult stats = WynnItemParser.parseItemStackStatsPage(stack, ranges);
            if (item instanceof GearItem gear) {
                GearInfo info = gear.getItemInfo();
                if (stats.level() != info.requirements().level() && info.tier() == GearTier.MYTHIC) {
                    WynntilsMod.warn("Gear level for " + info.name() + " is reported as "
                            + info.requirements().level());
                    GearInfo ascended = Models.Gear.getGearInfoFromDisplayName("Ascended " + info.name());
                    if (ascended != null && ascended.requirements().level() == stats.level()) {
                        gear.setItemInfo(ascended);
                        WynntilsMod.info("Parsing " + info.name() + " as " + ascended.name());
                        stats = WynnItemParser.parseItemStackStatsPage(stack, ascended.getVariableStatsMap());
                    } else {
                        WynntilsMod.warn("No ascended gear info found for " + info.name());
                    }
                }
                // Sometimes the API lists quest requirements but the in-game item does not have it, so we trust the
                // game
                GearInfo currentInfo = gear.getItemInfo();
                gear.setItemInfo(currentInfo.withRequirements(
                        currentInfo.requirements().withParsedQuestRequirement(stats.requirements())));
            }

            switch (item) {
                case GearItem gear ->
                    gear.updateStats(
                            stats.gearInstanceRequirements(),
                            stats.shinyStat(),
                            stats.rerolls(),
                            stats.identifications());
                case CharmItem gear ->
                    gear.updateStats(stats.gearInstanceRequirements(), stats.rerolls(), stats.identifications());
                case CraftedGearItem gear ->
                    gear.updateStats(
                            stats.dps(),
                            stats.health(),
                            stats.durability(),
                            stats.attackSpeed(),
                            stats.damages(),
                            stats.defences(),
                            stats.requirements(),
                            stats.gearInstanceRequirements(),
                            stats.identifications());
                case UnknownGearItem gear -> {
                    gear.updateStats(
                            stats.restrictions(),
                            stats.health(),
                            stats.attackSpeed(),
                            stats.damages(),
                            stats.defences(),
                            stats.level(),
                            stats.requirements(),
                            stats.gearInstanceRequirements(),
                            stats.shinyStat(),
                            stats.rerolls(),
                            stats.identifications(),
                            stats.majorIdLines());
                }
                default -> throw new IllegalArgumentException("Unsupported paged item: " + item);
            }
        } else if (page == 1) {
            WynnItemMiscStatsParseResult stats = WynnItemParser.parseItemStackMiscStatsPage(stack);

            switch (item) {
                case GearItem gear -> {
                    gear.setSetInstance(stats.setInstance());
                    gear.setPowderSpecial(stats.powderSpecial());
                    gear.setPowders(stats.powders());
                }
                case CraftedGearItem gear -> {
                    gear.setDps(stats.dps());
                    gear.setHealth(stats.health());
                    gear.setAttackSpeed(stats.attackSpeed());
                    gear.setDamages(stats.damages());
                    gear.setDefences(stats.defences());
                    gear.setPowderSlots(stats.powderSlots());
                    gear.setPowderSpecial(stats.powderSpecial());
                    gear.setPowders(stats.powders());
                    gear.setDurability(stats.durability());
                }
                case UnknownGearItem gear -> {
                    gear.setHealth(stats.health());
                    gear.setAttackSpeed(stats.attackSpeed());
                    gear.setDamages(stats.damages());
                    gear.setDefences(stats.defences());
                    gear.setRestrictions(stats.restrictions());
                    gear.setSetInstance(stats.setInstance());
                    gear.setPowderSlots(stats.powderSlots());
                    gear.setPowderSpecial(stats.powderSpecial());
                    gear.setPowders(stats.powders());
                }
                // Charms have nothing to parse on page 1
                default -> {}
            }
        } else if (page == 2) {
            WynnItemLoreParseResult lore = WynnItemParser.parseItemStackLorePage(stack);
            item.setLore(lore.lore());

            switch (item) {
                case CraftedGearItem gear -> {
                    gear.setDps(lore.dps());
                    gear.setHealth(lore.health());
                    gear.setAttackSpeed(lore.attackSpeed());
                    gear.setDamages(lore.damages());
                    gear.setDefences(lore.defences());
                    gear.setDurability(lore.durability());
                    gear.setCreator(lore.creator());
                }
                case UnknownGearItem gear -> {
                    gear.setHealth(lore.health());
                    gear.setAttackSpeed(lore.attackSpeed());
                    gear.setDamages(lore.damages());
                    gear.setDefences(lore.defences());
                    gear.setRestrictions(lore.restrictions());
                }
                default -> {}
            }
        }

        item.setCurrentPage(page);
        item.markParsedPage(page);
    }
}
