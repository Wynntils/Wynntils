/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wynntils.core.components.Managers;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.IdentificationModifier;
import com.wynntils.wynn.objects.profiles.item.IdentificationProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WynnItemUtils {
    /**
     * Create a list of ItemIdentificationContainer corresponding to the given ItemProfile, formatted for item guide items
     *
     * @param item the profile of the item
     * @return a list of appropriately formatted ItemIdentificationContainer
     */
    public static List<ItemIdentificationContainer> identificationsFromProfile(ItemProfile item) {
        List<ItemIdentificationContainer> ids = new ArrayList<>();

        for (Map.Entry<String, IdentificationProfile> entry : item.getStatuses().entrySet()) {
            IdentificationProfile idProfile = entry.getValue();
            IdentificationModifier type = idProfile.getType();
            String idName = entry.getKey();
            MutableComponent line;

            boolean inverted = idProfile.isInverted();
            if (idProfile.hasConstantValue()) {
                int value = idProfile.getBaseValue();
                line = Component.literal((value > 0 ? "+" : "") + value + type.getInGame(idName));
                line.setStyle(
                        Style.EMPTY.withColor(inverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));
            } else {
                int min = idProfile.getMin();
                int max = idProfile.getMax();
                ChatFormatting mainColor = inverted ^ (min > 0) ? ChatFormatting.GREEN : ChatFormatting.RED;
                ChatFormatting textColor = inverted ^ (min > 0) ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;
                line = Component.literal((min > 0 ? "+" : "") + min).withStyle(mainColor);
                line.append(Component.literal(" to ").withStyle(textColor));
                line.append(Component.literal((max > 0 ? "+" : "") + max + type.getInGame(idName))
                        .withStyle(mainColor));
            }

            line.append(Component.literal(" " + IdentificationProfile.getAsLongName(idName))
                    .withStyle(ChatFormatting.GRAY));

            ItemIdentificationContainer id =
                    new ItemIdentificationContainer(item, idProfile, type, idName, 0, 0, -1, line, line, line, line);
            ids.add(id);
        }

        return ids;
    }

    public static void removeLoreTooltipLines(List<Component> tooltip) {
        List<Component> toRemove = new ArrayList<>();
        boolean lore = false;
        for (Component c : tooltip) {
            // only remove text after the item type indicator
            if (!lore && WynnItemMatchers.rarityLineMatcher(c).find()) {
                lore = true;
                continue;
            }

            if (lore) toRemove.add(c);
        }
        tooltip.removeAll(toRemove);
    }

    public static String getTranslatedName(ItemStack itemStack) {
        String unformattedItemName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        return Managers.ItemProfiles.getTranslatedReference(unformattedItemName).replace("֎", "");
    }

    // Get gear item from un-parsed wynn item
    public static ItemStack getParsedItemStack(ItemStack itemStack) {
        if (itemStack.getItem() == Items.AIR) {
            return itemStack;
        }

        String itemName = WynnItemUtils.getTranslatedName(itemStack);

        // can't create lore on crafted items
        if (itemName.startsWith("Crafted")) {
            itemStack.setHoverName(Component.literal(itemName).withStyle(ChatFormatting.DARK_AQUA));
            return itemStack;
        }

        // disable viewing unidentified items
        if (itemStack.getItem() == Items.STONE_SHOVEL
                && itemStack.getDamageValue() >= 1
                && itemStack.getDamageValue() <= 6) {
            itemStack.setHoverName(Component.literal("Unidentified Item")
                    .withStyle(
                            ItemTier.fromBoxDamage(itemStack.getDamageValue()).getChatFormatting()));
            return itemStack;
        }

        ItemProfile itemProfile = Managers.ItemProfiles.getItemsProfile(itemName);

        if (itemProfile == null) {
            return null;
        }

        // attempt to parse item itemData
        JsonObject itemData;
        String rawLore =
                org.apache.commons.lang3.StringUtils.substringBeforeLast(ItemUtils.getStringLore(itemStack), "}")
                        + "}"; // remove extra unnecessary info
        try {
            itemData = JsonParser.parseString(rawLore).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            itemData = new JsonObject(); // invalid or empty itemData on item
        }

        List<ItemIdentificationContainer> idContainers = new ArrayList<>();

        if (itemData.has("identifications")) {
            JsonArray ids = itemData.getAsJsonArray("identifications");
            for (int i = 0; i < ids.size(); i++) {
                JsonObject idInfo = ids.get(i).getAsJsonObject();
                String id = idInfo.get("type").getAsString();
                float percent = idInfo.get("percent").getAsInt() / 100f;

                // get wynntils name from internal wynncraft name
                String translatedId = Managers.ItemProfiles.getInternalIdentification(id);
                if (translatedId == null || !itemProfile.getStatuses().containsKey(translatedId)) continue;

                // calculate value
                IdentificationProfile idContainer = itemProfile.getStatuses().get(translatedId);
                int value = idContainer.isFixed()
                        ? idContainer.getBaseValue()
                        : Math.round(idContainer.getBaseValue() * percent);

                // account for mistaken rounding
                if (value == 0) {
                    value = 1;
                }

                idContainers.add(Managers.ItemProfiles.identificationFromValue(
                        null, itemProfile, IdentificationProfile.getAsLongName(translatedId), translatedId, value, 0));
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

        return new GearItemStack(itemStack, itemProfile, idContainers, powders, rerolls);
    }
}
