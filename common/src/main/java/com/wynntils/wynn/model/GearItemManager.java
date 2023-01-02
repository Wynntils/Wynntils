/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.mc.mixin.accessors.ItemStackInfoAccessor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.handleditems.FakeItemStack;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.Powder;
import com.wynntils.wynn.objects.profiles.item.GearIdentification;
import com.wynntils.wynn.objects.profiles.item.IdentificationProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.utils.WynnItemUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.ArrayUtils;

public final class GearItemManager extends Manager {
    // private-use unicode chars
    private static final String START = new String(Character.toChars(0xF5FF0));
    private static final String END = new String(Character.toChars(0xF5FF1));
    private static final String SEPARATOR = new String(Character.toChars(0xF5FF2));
    private static final String RANGE =
            "[" + new String(Character.toChars(0xF5000)) + "-" + new String(Character.toChars(0xF5F00)) + "]";
    private static final int OFFSET = 0xF5000;

    private static final Pattern ENCODED_PATTERN = Pattern.compile(START + "(?<Name>.+?)" + SEPARATOR + "(?<Ids>"
            + RANGE + "*)(?:" + SEPARATOR + "(?<Powders>" + RANGE + "+))?(?<Rerolls>" + RANGE + ")" + END);
    private static final Pattern ITEM_TIER =
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?");
    private static final boolean ENCODE_NAME = false;

    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    public GearItemManager() {
        super(List.of());
    }

    public GearItem fromItemStack(ItemStack itemStack) {
        ItemProfile itemProfile;
        List<GearIdentification> identifications = new ArrayList<>();
        List<ItemIdentificationContainer> idContainers = new ArrayList<>();
        List<Powder> powders = List.of();
        int rerolls = 0;
        List<Component> setBonus = new ArrayList<>();

        // Lookup Gear Profile
        String name = itemStack.getHoverName().getString();
        String strippedName = WynnUtils.normalizeBadString(ComponentUtils.stripFormatting(name));
        itemProfile = Managers.ItemProfiles.getItemsProfile(strippedName);
        if (itemProfile == null) return null;

        // Verify that rarity matches
        if (!name.startsWith(itemProfile.getTier().getChatFormatting().toString())) return null;

        // Parse lore for identifications, powders and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        boolean collectingSetBonus = false;
        for (Component loreLine : lore) {
            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            // Look for Set Bonus
            if (unformattedLoreLine.equals("Set Bonus:")) {
                collectingSetBonus = true;
                continue;
            }
            if (collectingSetBonus) {
                setBonus.add(loreLine);

                if (unformattedLoreLine.isBlank()) {
                    collectingSetBonus = false;
                }
                continue;
            }

            // Look for Powder
            if (unformattedLoreLine.contains("] Powder Slots")) {
                powders = Powder.findPowders(unformattedLoreLine);
                continue;
            }

            // Look for Rerolls
            Matcher rerollMatcher = ITEM_TIER.matcher(unformattedLoreLine);
            if (rerollMatcher.find()) {
                if (rerollMatcher.group("Rolls") == null) continue;
                rerolls = Integer.parseInt(rerollMatcher.group("Rolls"));
                continue;
            }

            // Look for identifications
            Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
            if (identificationMatcher.find()) {
                String idName = WynnItemMatchers.getShortIdentificationName(
                        identificationMatcher.group("ID"), identificationMatcher.group("Suffix") == null);
                int value = Integer.parseInt(identificationMatcher.group("Value"));
                int stars = identificationMatcher.group("Stars").length();
                identifications.add(new GearIdentification(idName, value, stars));

                // This is partially overlapping with GearIdentification, sort this out later
                ItemIdentificationContainer idContainer =
                        Managers.ItemProfiles.identificationFromLore(loreLine, itemProfile);
                if (idContainer == null) continue;
                idContainers.add(idContainer);
            }
        }

        return new GearItem(itemProfile, identifications, idContainers, powders, rerolls, setBonus);
    }

    public GearItem fromJsonLore(ItemStack itemStack) {
        String itemName = WynnItemUtils.getTranslatedName(itemStack);

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
        List<GearIdentification> identifications = new ArrayList<>();

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
                // FIXME: Get proper short name!
                identifications.add(new GearIdentification(translatedId, value, 0));
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

        return new GearItem(itemProfile, identifications, idContainers, powders, rerolls, List.of());
    }

    private GearItem fromEncodedString(String encoded) {
        Matcher m = ENCODED_PATTERN.matcher(encoded);
        if (!m.matches()) return null;

        String name = ENCODE_NAME ? decodeString(m.group("Name")) : m.group("Name");
        int[] ids = decodeNumbers(m.group("Ids"));
        int[] powders = m.group("Powders") != null ? decodeNumbers(m.group("Powders")) : new int[0];
        int rerolls = decodeNumbers(m.group("Rerolls"))[0];

        ItemProfile item = Managers.ItemProfiles.getItemsProfile(name);
        if (item == null) return null;

        // ids
        List<ItemIdentificationContainer> idContainers = new ArrayList<>();
        List<GearIdentification> identifications = new ArrayList<>();

        List<String> sortedIds = new ArrayList<>(item.getStatuses().keySet());
        sortedIds.sort(Comparator.comparingInt(Managers.ItemProfiles::getOrder));

        int counter = 0; // for id value array
        for (String shortIdName : sortedIds) {
            IdentificationProfile status = item.getStatuses().get(shortIdName);

            int value;
            int stars = 0;
            if (status.isFixed()) {
                value = status.getBaseValue();
            } else {
                if (counter >= ids.length) return null; // some kind of mismatch, abort

                // id value
                int encodedValue = ids[counter] / 4;
                if (Math.abs(status.getBaseValue()) > 100) {
                    // using bigdecimal here for precision when rounding
                    value = new BigDecimal(encodedValue + 30)
                            .movePointLeft(2)
                            .multiply(new BigDecimal(status.getBaseValue()))
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                } else {
                    // min/max must be flipped for inverted IDs due to encoding
                    value = status.isInverted() ? encodedValue + status.getMax() : encodedValue + status.getMin();
                }

                // stars
                stars = ids[counter] % 4;

                counter++;
            }

            // name
            String longIdName = IdentificationProfile.getAsLongName(shortIdName);

            // create ID and append to list
            ItemIdentificationContainer idContainer =
                    Managers.ItemProfiles.identificationFromValue(null, item, longIdName, shortIdName, value, stars);
            if (idContainer != null) idContainers.add(idContainer);
            identifications.add(new GearIdentification(shortIdName, value, stars));
        }

        // powders
        List<Powder> powderList = new ArrayList<>();
        if (item.getPowderAmount() > 0 && powders.length > 0) {
            ArrayUtils.reverse(powders); // must reverse powders so they are read in reverse order
            for (int powderNum : powders) {
                // once powderNum is 0, all the powders have been read
                while (powderNum > 0) {
                    Powder p = Powder.values()[powderNum % 6 - 1];
                    powderList.add(0, p); // prepend powders because they are decoded in reverse

                    powderNum /= 6;
                }
            }
        }

        // create chat gear stack
        return new GearItem(item, identifications, idContainers, powderList, rerolls, List.of());
    }

    /**
     * Encodes the given item, as long as it is a standard gear item, into the following format
     *
     * START character (U+F5FF0)
     * Item name (optionally encoded)
     * SEPARATOR character (U+F5FF2)
     * Identifications/stars (encoded)
     * SEPARATOR (only if powdered)
     * Powders (encoded) (only if powdered)
     * Rerolls (encoded)
     * END character (U+F5FF1)
     *
     * Any encoded "value" is added to the OFFSET character value U+F5000 and then converted into the corresponding Unicode character:
     *
     * The name is encoded based on the ASCII value of each character minus 32
     *
     * Identifications are encoded either as the raw value minus the minimum value of that ID, or if the range is larger than 100,
     * the percent value 0 to 100 of the given roll.
     * Regardless of either case, this number is multiplied by 4, and the number of stars present on that ID is added.
     * This ensures that the value and star count can be encoded into a single character and be decoded later.
     *
     * Powders are encoded as numerical values 1-5. Up to 4 powders are encoded into a single character - for each new powder,
     * the running total is multiplied by 6 before the new powder value is added. Thus, each individual powder can be decoded.
     *
     * Rerolls are simply encoded as a raw number.
     *
     * This format is identical to that used in Wynntils 1.12, for compatibility across versions. It should not be
     * modified without also changing the encoding in legacy.
     *
     */
    public String toEncodedString(GearItem gearItem) {
        String itemName = gearItem.getItemProfile().getDisplayName();

        // get identification data - ordered for consistency
        List<ItemIdentificationContainer> sortedIds =
                Managers.ItemProfiles.orderIdentifications(gearItem.getIdContainers());

        // name
        StringBuilder encoded = new StringBuilder(START);
        encoded.append(ENCODE_NAME ? encodeString(itemName) : itemName);
        encoded.append(SEPARATOR);

        // ids
        for (ItemIdentificationContainer id : sortedIds) {
            if (id.identification().isFixed()) continue; // don't care about these

            int idValue = id.value();
            IdentificationProfile idProfile = id.identification();

            int translatedValue;
            if (Math.abs(idProfile.getBaseValue()) > 100) { // calculate percent
                translatedValue = (int) Math.round((idValue * 100.0 / idProfile.getBaseValue()) - 30);
            } else { // raw value
                // min/max must be flipped for inverted IDs to avoid negative values
                translatedValue = idProfile.isInverted() ? idValue - idProfile.getMax() : idValue - idProfile.getMin();
            }

            // stars
            int stars = id.stars();

            // encode value + stars in one character
            encoded.append(encodeNumber(translatedValue * 4 + stars));
        }

        // powders
        List<Powder> powders = gearItem.getPowders();
        if (powders != null && !powders.isEmpty()) {
            encoded.append(SEPARATOR);

            int counter = 0;
            int encodedPowders = 0;
            for (Powder p : powders) {
                encodedPowders *= 6; // shift left
                encodedPowders += p.ordinal() + 1; // 0 represents no more powders
                counter++;

                if (counter == 4) { // max # of powders encoded in a single char
                    encoded.append(encodeNumber(encodedPowders));
                    encodedPowders = 0;
                    counter = 0;
                }
            }
            if (encodedPowders != 0) encoded.append(encodeNumber(encodedPowders)); // catch any leftover powders
        }

        // rerolls
        encoded.append(encodeNumber(gearItem.getRerolls()));

        encoded.append(END);
        return encoded.toString();
    }

    public Matcher chatItemMatcher(String text) {
        return ENCODED_PATTERN.matcher(text);
    }

    public Component replaceComponentWithItemHover(Component message) {
        // no item tooltips to insert
        if (!ENCODED_PATTERN.matcher(ComponentUtils.getCoded(message)).find()) return message;

        List<MutableComponent> components =
                message.getSiblings().stream().map(Component::copy).collect(Collectors.toList());
        components.add(0, message.plainCopy().withStyle(message.getStyle()));

        MutableComponent temp = Component.literal("");

        for (Component comp : components) {
            Matcher m = ENCODED_PATTERN.matcher(ComponentUtils.getCoded(comp));
            if (!m.find()) {
                Component newComponent = comp.copy();
                temp.append(newComponent);
                continue;
            }

            do {
                String text = ComponentUtils.getCoded(comp);
                Style style = comp.getStyle();

                GearItem item = fromEncodedString(m.group());
                if (item == null) { // couldn't decode, skip
                    comp = comp.copy();
                    continue;
                }

                MutableComponent preText = Component.literal(text.substring(0, m.start()));
                preText.withStyle(style);
                temp.append(preText);

                // create hover-able text component for the item
                Component itemComponent = createItemComponent(item);
                temp.append(itemComponent);

                comp = Component.literal(ComponentUtils.getLastPartCodes(ComponentUtils.getCoded(preText))
                                + text.substring(m.end()))
                        .withStyle(style);
                m = ENCODED_PATTERN.matcher(ComponentUtils.getCoded(comp)); // recreate matcher for new substring
            } while (m.find()); // search for multiple items in the same message

            temp.append(comp); // leftover text after item(s)
        }

        return temp;
    }

    private Component createItemComponent(GearItem gearItem) {
        MutableComponent itemComponent = Component.literal(
                        gearItem.getItemProfile().getDisplayName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(gearItem.getItemProfile().getTier().getChatFormatting());

        ItemStack itemStack = new FakeItemStack(gearItem, "From chat");
        HoverEvent.ItemStackInfo itemHoverEvent = new HoverEvent.ItemStackInfo(itemStack);
        ((ItemStackInfoAccessor) itemHoverEvent).setItemStack(itemStack);
        itemComponent.withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, itemHoverEvent)));

        return itemComponent;
    }

    private String encodeString(String text) {
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            int value = c - 32; // offset by 32 to ignore ascii control characters
            encoded.append(new String(Character.toChars(value + OFFSET))); // get encoded representation
        }
        return encoded.toString();
    }

    private String encodeNumber(int value) {
        return new String(Character.toChars(value + OFFSET));
    }

    private String decodeString(String text) {
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < text.length(); i += 2) {
            int value = text.codePointAt(i) - OFFSET + 32;
            decoded.append((char) value);
        }
        return decoded.toString();
    }

    private int[] decodeNumbers(String text) {
        int[] decoded = new int[text.length() / 2];
        for (int i = 0; i < text.length(); i += 2) {
            decoded[i / 2] = text.codePointAt(i) - OFFSET;
        }
        return decoded;
    }
}
