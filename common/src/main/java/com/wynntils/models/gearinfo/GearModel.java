/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.google.gson.JsonObject;
import com.wynntils.core.components.Model;
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

* GearChatEncoding -- did I break the protocol wrt inverse/negative values?
* -- Also, large values should have been encoded as percent, I *did* break this!

* RerollCalculator: flip() is probably broken. Make this into a method instead.

* tooltip Post: major IDs are incorrectly formatted
* tooltip Pre: did the old code correctly set our requirements?
* tooltip variable needs cleaning in how we build identified/unidentified lines
* All other Guide stacks should also use the vanilla tooltip rendering!

* SPELL COST STATS: It is a mess. Create aliases instead of multiple stats...
*  -- then remove fixme in tooltip variable.

* GearParser needs cleaning
* Crafted gear needs some thinking
* MODELLING: A GearInstance should have powder specials as well!!!!

stats should have Range internalRoll


NEW IDEAS:
* Custom ordering
* Show range of actual internal roll on ctrl+shift
* Calculate possible gear on the fly (and then cache it)
* Rename the item tooltip feature...
* Rename WynnItemMatcher to WynnItemUtil
* Also fix Ingredients
* Correctly show requirements as missing or fulfilled in custom tooltip
* Just copy to chat should be separate hotkey
* Option to turn "Spell Cost" into "Spell Cost Reduction" (invert value)

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
public final class GearModel extends Model {
    private GearInfoRegistry gearInfoRegistry = new GearInfoRegistry();

    private GearParser gearParser = new GearParser();
    private GearChatEncoding gearChatEncoding = new GearChatEncoding();

    public GearModel(StatModel statModel) {
        super(List.of(statModel));

        // FIXME
        ItemGuessProfile.init();
    }

    public void reloadData() {
        gearInfoRegistry.reloadData();
    }

    public GearInstance parseInstance(GearInfo gearInfo, ItemStack itemStack) {
        return gearParser.fromItemStack(gearInfo, itemStack);
    }

    public GearInstance parseInstance(GearInfo gearInfo, JsonObject itemData) {
        return gearParser.fromIngameItemData(gearInfo, itemData);
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

    public ItemGuessProfile getItemGuess(String levelRange) {
        return ItemGuessProfile.getItemGuess(levelRange);
    }
}
