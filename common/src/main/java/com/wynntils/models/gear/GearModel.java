/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.models.elements.ElementModel;
import com.wynntils.models.gear.parsing.GearParseResult;
import com.wynntils.models.gear.parsing.GearParser;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.StatModel;
import com.wynntils.utils.type.CappedValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
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
    private final GearInfoRegistry gearInfoRegistry = new GearInfoRegistry();

    private final GearChatEncoding gearChatEncoding = new GearChatEncoding();
    private Map<GearBoxItem, List<GearInfo>> possibilitiesCache = new HashMap<>();

    public GearModel(ElementModel elementModel, StatModel statModel) {
        super(List.of(statModel));
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

    public boolean canBeGearBox(GearInfo gear) {
        // If all the ways we can obtain this is by merchants, it cannot be in a gear box
        return gear.metaInfo().obtainInfo().stream()
                .anyMatch(o -> !o.sourceType().isMerchant());
    }

    public void reloadData() {
        gearInfoRegistry.reloadData();
    }

    public GearInstance parseInstance(GearInfo gearInfo, ItemStack itemStack) {
        GearParseResult result = GearParser.parseItemStack(itemStack, gearInfo);
        if (result.tier() != gearInfo.tier()) {
            WynntilsMod.warn("Tier for " + gearInfo.name() + " is reported as " + result.tier());
        }

        return GearInstance.create(gearInfo, result.identifications(), result.powders(), result.rerolls());
    }

    public GearInstance parseInstance(GearInfo gearInfo, JsonObject itemData) {
        GearParseResult result = GearParser.parseInternalRolls(gearInfo, itemData);

        return GearInstance.create(gearInfo, result.identifications(), result.powders(), result.rerolls());
    }

    public CraftedGearItem parseCraftedGearItem(ItemStack itemStack) {
        GearParseResult result = GearParser.parseItemStack(itemStack, null);
        CappedValue durability = new CappedValue(result.durabilityCurrent(), result.durabilityMax());
        // FIXME: Damages and requirements are not yet parsed
        return new CraftedGearItem(
                result.gearType(), List.of(), List.of(), result.identifications(), result.powders(), durability);
    }

    public GearItem fromEncodedString(String encoded) {
        return gearChatEncoding.fromEncodedString(encoded);
    }

    public String toEncodedString(GearItem gearItem) {
        return gearChatEncoding.toEncodedString(gearItem);
    }

    public Matcher gearChatEncodingMatcher(String str) {
        return gearChatEncoding.gearChatEncodingMatcher(str);
    }

    public GearInfo getGearInfoFromDisplayName(String gearName) {
        return gearInfoRegistry.getFromDisplayName(gearName);
    }

    public GearInfo getGearInfoFromApiName(String apiName) {
        return gearInfoRegistry.getFromApiName(apiName);
    }

    public Stream<GearInfo> getAllGearInfos() {
        return gearInfoRegistry.getGearInfoStream();
    }
}
