/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.parsing.CraftedItemParseResults;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

/**
 * Gear and stats are complex, have lots of corner cases and suffer from a general
 * lack of comprehensible, exhaustive, correct and authoritive documentation. :-(
 *
 * Here is a collection of generally helpful links:
 *
 * 2016 Guide: https://forums.wynncraft.com/threads/how-identifications-are-calculated.128923/
 * 2019 Guide: https://forums.wynncraft.com/threads/stats-and-identifications-guide.246308/
 * The Damage Bible: https://docs.google.com/document/d/1BXdLrMWj-BakPcAWnuqvSFbwiz7oGTOMcEEdC5vCWs4
 * WynnBuilder "Wynnfo": https://hppeng-wynn.github.io/wynnfo/, especially
 * Damage Calculations: https://hppeng-wynn.github.io/wynnfo/pdfs/Damage_calculation.pdf
 *
 * A note on percent vs raw numbers and how they combine, from HeyZeer0:
 * base = base + (base * percentage1) + (base * percentage2) + rawValue
 */
public final class GearModel extends Model {
    // Test in GearModel_GEAR_PATTERN
    public static final Pattern GEAR_PATTERN = Pattern.compile(
            "^(?:(?<unidrarity>§[5abcdef])(?<unidentified>Unidentified ))?(?:§f⬡ )?(?<idrarity>§[5abcdef])?(?:Shiny )?(?<name>.+)$");

    private final GearInfoRegistry gearInfoRegistry = new GearInfoRegistry();

    private final Map<GearBoxItem, List<GearInfo>> possibilitiesCache = new HashMap<>();

    public GearModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        gearInfoRegistry.registerDownloads(registry);
    }

    public List<GearInfo> getPossibleGears(GearBoxItem gearBoxItem) {
        List<GearInfo> possibilities = possibilitiesCache.get(gearBoxItem);
        if (possibilities != null) return possibilities;

        List<GearInfo> possibleGear = getAllGearInfos()
                .filter(gear -> gear.type() == gearBoxItem.getGearType()
                        && gear.tier() == gearBoxItem.getGearTier()
                        && canBeGearBox(gear)
                        && gearBoxItem
                                .getLevelRange()
                                .inRange(gear.requirements().level()))
                .toList();
        possibilitiesCache.put(gearBoxItem, possibleGear);

        return possibleGear;
    }

    private boolean canBeGearBox(GearInfo gear) {
        // If an item is pre-identified, it cannot be in a gear box
        // Also check that the item has a source that can drop boxed items
        return !gear.metaInfo().preIdentified()
                && getObtainInfo(gear).stream().anyMatch(x -> ItemObtainType.BOXED_ITEMS.contains(x.sourceType()));
    }

    // For "real" gear items eg. from the inventory
    public GearInstance parseInstance(GearInfo gearInfo, ItemStack itemStack) {
        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, gearInfo.getVariableStatsMap());
        if (result.tier() != gearInfo.tier()) {
            WynntilsMod.warn("Tier for " + gearInfo.name() + " is reported as " + result.tier());
        }

        return GearInstance.create(
                gearInfo,
                result.identifications(),
                result.powders(),
                result.rerolls(),
                result.shinyStat(),
                result.allRequirementsMet(),
                result.setInstance());
    }

    // For parsing gear from the gear viewer
    public GearInstance parseInstance(GearInfo gearInfo, JsonObject itemData) {
        WynnItemParseResult result = WynnItemParser.parseInternalRolls(gearInfo, itemData);

        return GearInstance.create(
                gearInfo,
                result.identifications(),
                result.powders(),
                result.rerolls(),
                Optional.empty(),
                false,
                Optional.empty());
    }

    public CraftedGearItem parseCraftedGearItem(ItemStack itemStack) {
        // We pass this down to the parser, so it can populate it
        // (gears don't have to parse possible values on the fly, since the api provides them)
        Map<StatType, StatPossibleValues> possibleValuesMap = new HashMap<>();
        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, possibleValuesMap);

        CraftedItemParseResults craftedResults = WynnItemParser.parseCraftedItem(itemStack);

        if (craftedResults == null) return null;

        // If the item doesn't have an effect strength, it's not a crafted gear item
        if (craftedResults.effectStrength() == -1) return null;

        CappedValue durability = new CappedValue(result.durabilityCurrent(), result.durabilityMax());
        GearType gearType;
        // If it is crafted, and has a skin, then we cannot determine weapon type from item stack
        // Maybe it is possible to find in the string type, e.g. "Crafted Wand"
        gearType = GearType.fromString(result.itemType());
        if (gearType == null && result.requirements().classType().isPresent()) {
            // If the item is signed, we can find the class type from the requirements
            gearType = GearType.fromClassType(result.requirements().classType().get());
        }

        // If we still failed to find the gear type, try to find it from the item stack
        if (gearType == null) {
            gearType = GearType.fromItemStack(itemStack, true);

            if (gearType == null) {
                // If we failed to find the gear type, assume it is a weapon
                gearType = GearType.WEAPON;
            }
        }

        return new CraftedGearItem(
                craftedResults.name(),
                craftedResults.effectStrength(),
                gearType,
                result.attackSpeed(),
                result.health(),
                result.damages(),
                result.defences(),
                result.requirements(),
                possibleValuesMap.values().stream().toList(),
                result.identifications(),
                result.powders(),
                result.powderSlots(),
                result.allRequirementsMet(),
                durability);
    }

    public UnknownGearItem parseUnknownGearItem(
            String name, GearType gearType, GearTier gearTier, boolean isUnidentified, ItemStack itemStack) {
        WynnItemParseResult result = WynnItemParser.parseItemStack(itemStack, null);

        if (gearType == GearType.WEAPON) {
            // If the gear type is weapon, we can try to find the weapon type from the requirements
            gearType = result.requirements()
                    .classType()
                    .map(GearType::fromClassType)
                    .orElse(gearType);
        }

        return new UnknownGearItem(
                name,
                gearType,
                gearTier,
                isUnidentified,
                result.level(),
                result.attackSpeed(),
                result.health(),
                result.damages(),
                result.defences(),
                result.requirements(),
                result.allRequirementsMet(),
                result.identifications(),
                result.powders(),
                result.powderSlots(),
                result.rerolls(),
                result.setInstance().orElse(null),
                result.shinyStat().orElse(null));
    }

    public GearInfo getGearInfoFromDisplayName(String gearName) {
        return gearInfoRegistry.getFromDisplayName(gearName);
    }

    public GearInfo getGearInfoFromApiName(String apiName) {
        return gearInfoRegistry.getFromApiName(apiName);
    }

    public List<ItemObtainInfo> getObtainInfo(GearInfo gearInfo) {
        List<ItemObtainInfo> obtainInfo = new ArrayList<>(gearInfo.metaInfo().obtainInfo());

        // If the API gave no info, then use the crowd sourced info
        if (obtainInfo.size() == 1 && obtainInfo.getFirst().equals(ItemObtainInfo.UNKNOWN)) {
            obtainInfo.clear();
        }

        obtainInfo.addAll(Models.WynnItem.getObtainInfo(gearInfo.name()));
        return obtainInfo;
    }

    public Stream<GearInfo> getAllGearInfos() {
        return gearInfoRegistry.getGearInfoStream();
    }
}
