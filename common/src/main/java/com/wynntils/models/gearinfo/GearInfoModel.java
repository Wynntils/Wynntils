/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.gearinfo.itemguess.ItemGuessProfile;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.StatModel;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.world.item.ItemStack;

/*
FIXME list:
Remaining issues:
* GearInfoModel.getGearInfoFromInternalName! - we should strip "֎" earlier. "ingame"
* Registry, items like "Coconut֎" have the same displayName after cleaning, fix this
* We need to at least pick out the possible gear stuff and move it to us
* TOTAL GEAR QUALITY: GearInstance calculations are removed! Should be done by Model instead.
* Tooltip -- split lore must be simplified!
* -- then, look at tooltip variable  appendSkillBonuses() if it can be moved
* RerollCalculator: flip() is probably broken. Make this into a method instead.
* Move the reroll calculations and other calculations from util class into the model, perhaps
*   a new GearCalculations utility class? instad of GearUtils
* Wynncraft order is WRONG wrt spell costs! Need to write a "swapPairwise" for the list.
* ItemScreenshotFeature error on copy
* Tome and Charm in GearItemModel...
* GearChatEncoding -- did I break the protocol wrt inverse/negative values?
* -- Also, large values should have been encoded as percent, I *did* break this!
* GearParser needs cleaning
* Crafted gear needs some thinking
* GearTooltipBuilder -- only cache middle segment if style is the same, otherwise
*   invalidate the cache. Also check for names like "top" and "middle", fix that.
* tooltip Post: major IDs are incorrectly formatted
* tooltip Pre: did the old code correctly set our requirements?
* MODELLING: A GearInstance should have powder specials as well!!!!
* SPELL COST STATS: It is a mess. Create aliases instead of multiple stats...
*  -- then remove fixme in tooltip variable.
* tooltip variable needs cleaning in how we build identified/unidentified lines
* GEAR MATERIAL!!!! needs 3 factory method, and only return one thing:a ItemStack.
*  -- should probablt have a MaterialHandler thingy, also move in method from GearUtils
* ItemModel has bad dependencies:GearItem for tome and charm, GearProfiles for gear box possibilities
* Can we get a better name for ingame-id than "lore"? "ingameId" perhaps!!!
* All other Guide stacks should also use the vanilla tooltip rendering!


NEW IDEAS:
* Custom ordering
* Show range of actual internal roll on ctrl+shift
* Calculate possible gear on the fly (and then cache it)
* Rename the item tooltip feature...
* Rename WynnItemMatcher to WynnItemUtil
* Also fix Ingredients
* Correctly show requirements as missing or fulfilled in custom tooltip

 */

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
public final class GearInfoModel extends Model {
    private GearInfoRegistry gearInfoRegistry = new GearInfoRegistry();

    private GearParser gearParser = new GearParser();
    private GearChatEncoding gearChatEncoding = new GearChatEncoding();

    public GearInfoModel(StatModel statModel) {
        super(List.of(statModel));

        // FIXME
        ItemGuessProfile.init();
    }

    public GearInstance fromItemStack(GearInfo gearInfo, ItemStack itemStack) {
        return gearParser.fromItemStack(gearInfo, itemStack);
    }

    public GearItem fromJsonLore(ItemStack itemStack, GearInfo gearInfo) {
        return gearParser.fromJsonLore(itemStack, gearInfo);
    }

    public CraftedGearItem getCraftedGearItem(ItemStack itemStack) {
        return gearParser.getCraftedGearItem(itemStack);
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

    public GearInfo getGearInfoFromInternalName(String gearName) {
        // FIXME!!! Also check alternative name...
        String itemName = Models.GearProfiles.getTranslatedReference(gearName).replace("֎", "");
        return gearInfoRegistry.gearInfoLookup.get(itemName);
    }

    public ItemGuessProfile getItemGuess(String levelRange) {
        return Models.GearProfiles.getItemGuess(levelRange);
    }
}
