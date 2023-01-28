/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.GearUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.item.ItemStack;

public class GearJsonLoreParser {
    public GearItem fromJsonLore(ItemStack itemStack, GearInfo gearInfo) {
        // attempt to parse item itemData
        JsonObject itemData;
        String rawLore = StringUtils.substringBeforeLast(LoreUtils.getStringLore(itemStack), "}")
                + "}"; // remove extra unnecessary info
        try {
            itemData = JsonParser.parseString(rawLore).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            itemData = new JsonObject(); // invalid or empty itemData on item
        }

        List<StatActualValue> identifications = new ArrayList<>();

        // Lore lines is: type: "LORETYPE", percent: <number>, where 100 is baseline, so can be > 100 and < 100.
        if (itemData.has("identifications")) {
            JsonArray ids = itemData.getAsJsonArray("identifications");
            for (int i = 0; i < ids.size(); i++) {
                JsonObject idInfo = ids.get(i).getAsJsonObject();
                String id = idInfo.get("type").getAsString();
                int intPercent = idInfo.get("percent").getAsInt();

                // Convert e.g. DAMAGEBONUS to our StatTypes
                StatType statType = Models.Stat.fromLoreId(id);
                if (statType == null) continue;

                StatPossibleValues possibleValue = gearInfo.getPossibleValues(statType);
                if (possibleValue == null) {
                    WynntilsMod.warn("Remote player's " + gearInfo.name() + " claims to have " + statType);
                    continue;
                }
                int value = Math.round(possibleValue.baseValue() * (intPercent / 100f));

                // account for mistaken rounding
                if (value == 0) {
                    value = 1;
                }

                int stars = GearUtils.getStarsFromPercent(intPercent);
                // FIXME: Negative values should never show stars!

                identifications.add(new StatActualValue(statType, value, stars));
            }
        }

        List<Powder> powders = new ArrayList<>();

        if (itemData.has("powders")) {
            JsonArray powderData = itemData.getAsJsonArray("powders");
            for (int i = 0; i < powderData.size(); i++) {
                String type = powderData.get(i).getAsJsonObject().get("type").getAsString();
                Powder powder = Powder.valueOf(type.toUpperCase(Locale.ROOT));

                powders.add(powder);
            }
        }

        int rerolls = 0;
        if (itemData.has("identification_rolls")) {
            rerolls = itemData.get("identification_rolls").getAsInt();
        }

        GearInstance gearInstance = new GearInstance(identifications, powders, rerolls, List.of());
        return new GearItem(gearInfo, gearInstance);
    }
}
