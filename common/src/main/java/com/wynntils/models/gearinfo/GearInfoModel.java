/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.StatModel;
import com.wynntils.utils.mc.ComponentUtils;
import java.util.List;
import java.util.regex.Matcher;
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
 */
public final class GearInfoModel extends Model {
    private GearInfoRegistry gearInfoRegistry = new GearInfoRegistry();

    private GearParser gearParser = new GearParser();
    private GearChatEncoding gearChatEncoding = new GearChatEncoding();

    public GearInfoModel(StatModel statModel) {
        super(List.of(statModel));
    }

    public GearInstance fromItemStack(GearInfo gearInfo, ItemStack itemStack) {
        return gearParser.fromItemStack(gearInfo, itemStack);
    }

    public GearItem fromJsonLore(ItemStack itemStack, GearInfo gearInfo) {
        return gearParser.fromJsonLore(itemStack, gearInfo);
    }

    public GearItem fromEncodedString(String encoded) {
        return gearChatEncoding.fromEncodedString(encoded);
    }

    public String toEncodedString(GearItem gearItem) {
        return gearChatEncoding.toEncodedString(gearItem);
    }

    public Matcher gearChatEncodingMatcher(String text) {
        return gearChatEncoding.gearChatEncodingMatcher(text);
    }

    public List<GearInfo> getGearInfoRegistry() {
        return gearInfoRegistry.gearInfoRegistry;
    }

    public GearInfo getGearInfo(String gearName) {
        return gearInfoRegistry.gearInfoLookup.get(gearName);
    }

    public String getTranslatedName(ItemStack itemStack) {
        // FIXME!!!
        String unformattedItemName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        return Models.GearProfiles.getTranslatedReference(unformattedItemName).replace("֎", "");
    }
}
