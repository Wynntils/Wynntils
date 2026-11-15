/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.annotators.ItemPageAnnotator;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            "^\uDAFC\uDC00(?<unidentified>§f\uE008\uDB00\uDC02)?(?<rarity>§[5bcdef])(?<isShiny>Shiny )?(?<name>.+)\uDAFC\uDC00$");

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
    public GearItem parseInstance(GearInfo gearInfo, ItemStack itemStack, boolean isUnidentified) {
        GearItem item = new GearItem(gearInfo, isUnidentified);
        ItemPageAnnotator.update(item, itemStack);
        return item;
    }

    public CraftedGearItem parseCraftedGearItem(String name, ItemStack itemStack) {
        GearType gearType = GearType.fromFrameSprite(WynnItemParser.extractFrameSpriteCode(itemStack));
        if (gearType == null) {
            gearType = GearType.fromItemStack(itemStack);

            if (gearType == null) {
                gearType = GearType.WEAPON;
            }
        }

        CraftedGearItem item = new CraftedGearItem(name, gearType);
        ItemPageAnnotator.update(item, itemStack);
        return item;
    }

    public UnknownGearItem parseUnknownGearItem(
            String name, GearType gearType, GearTier gearTier, boolean isUnidentified, ItemStack itemStack) {
        UnknownGearItem item = new UnknownGearItem(name, gearTier, gearType, isUnidentified);
        ItemPageAnnotator.update(item, itemStack);
        return item;
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
