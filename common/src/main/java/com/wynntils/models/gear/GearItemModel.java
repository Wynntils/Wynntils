/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.mixin.accessors.ItemStackInfoAccessor;
import com.wynntils.models.concepts.Powder;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.type.CharmProfile;
import com.wynntils.models.gear.type.TomeProfile;
import com.wynntils.models.gearinfo.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.wynn.GearUtils;
import com.wynntils.utils.wynn.WynnUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

public final class GearItemModel extends Model {
    public static final String UNIDENTIFIED_PREFIX = "Unidentified ";

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
            Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) "
                    + "(Raid Reward|Item)(?: \\[(?<Rolls>\\d+)])?");
    private static final boolean ENCODE_NAME = false;

    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    public static final Pattern ID_NEW_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)(%|/3s|/5s| tier)?(?:§r§2(\\*{1,3}))? ?§r§7 ?(.*)$");

    private static final Pattern RANGE_PATTERN =
            Pattern.compile("^§([ac])([-+]\\d+)§r§2 to §r§a(\\d+)(%|/3s|/5s| tier)?§r§7 ?(.*)$");

    public TomeItem fromTomeItemStack(ItemStack itemStack, TomeProfile tomeProfile) {
        List<StatActualValue> identifications = new ArrayList<>();
        int rerolls = 0;

        // Parse lore for identifications and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // Look for rerolls
            Optional<Integer> rerollOpt = rerollsFromLore(loreLine);
            if (rerollOpt.isPresent()) {
                rerolls = rerollOpt.get();
                continue;
            }

            // Look for identifications
            Optional<StatActualValue> gearIdOpt = gearIdentificationFromLore(loreLine);
            if (gearIdOpt.isEmpty()) continue;
            identifications.add(gearIdOpt.get());
        }

        return new TomeItem(tomeProfile, identifications, rerolls);
    }

    public CharmItem fromCharmItemStack(ItemStack itemStack, CharmProfile charmProfile) {
        List<StatActualValue> identifications = new ArrayList<>();
        int rerolls = 0;

        // Parse lore for identifications and rerolls
        List<Component> lore = ComponentUtils.stripDuplicateBlank(itemStack.getTooltipLines(null, TooltipFlag.NORMAL));
        lore.remove(0); // remove item name

        for (Component loreLine : lore) {
            // Look for rerolls
            Optional<Integer> rerollOpt = rerollsFromLore(loreLine);
            if (rerollOpt.isPresent()) {
                rerolls = rerollOpt.get();
                continue;
            }

            // Look for identifications
            Optional<StatActualValue> gearIdOpt = gearIdentificationFromLore(loreLine);
            if (gearIdOpt.isEmpty()) continue;
            identifications.add(gearIdOpt.get());
        }

        return new CharmItem(charmProfile, identifications, rerolls);
    }

    private Optional<Integer> rerollsFromLore(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        Matcher rerollMatcher = ITEM_TIER.matcher(unformattedLoreLine);
        if (!rerollMatcher.find()) return Optional.empty();

        int rerolls = 0;
        if (rerollMatcher.group("Rolls") != null) rerolls = Integer.parseInt(rerollMatcher.group("Rolls"));
        return Optional.of(rerolls);
    }

    // FIXME: this is a remnant, used by tome/charms...
    private Optional<StatActualValue> gearIdentificationFromLore(Component lore) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());

        // Look for identifications
        Matcher statMatcher = ID_NEW_PATTERN.matcher(unformattedLoreLine);
        if (!statMatcher.matches()) return Optional.empty();

        int value = Integer.parseInt(statMatcher.group(2));
        String unit = statMatcher.group(3);
        String statDisplayName = statMatcher.group(5);

        StatType type = Models.Stat.fromDisplayName(statDisplayName, unit);
        if (type == null && Skill.isSkill(statDisplayName)) {
            // Skill point buff looks like stats when parsing
            return Optional.empty();
        }

        // FIXME: stars are not fixed here. align with normal parsing
        return Optional.of(new StatActualValue(type, value, -1));
    }

    public boolean isUnidentified(String itemName) {
        return itemName.startsWith(UNIDENTIFIED_PREFIX);
    }

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

    private GearItem fromEncodedString(String encoded) {
        Matcher m = ENCODED_PATTERN.matcher(encoded);
        if (!m.matches()) return null;

        String name = ENCODE_NAME ? decodeString(m.group("Name")) : m.group("Name");
        int[] ids = decodeNumbers(m.group("Ids"));
        int[] powders = m.group("Powders") != null ? decodeNumbers(m.group("Powders")) : new int[0];
        int rerolls = decodeNumbers(m.group("Rerolls"))[0];

        GearInfo gearInfo = Models.GearInfo.getGearInfo(name);
        if (gearInfo == null) return null;

        // ids
        List<StatActualValue> identifications = new ArrayList<>();

        List<StatType> sortedStats = Models.Stat.getSortedStats(gearInfo, StatListOrdering.LEGACY);

        int counter = 0; // for id value array
        for (StatType statType : sortedStats) {
            StatPossibleValues status = gearInfo.getPossibleValues(statType);

            int value;
            int stars = 0;
            if (status.isPreIdentified()) {
                value = status.baseValue();
            } else {
                if (counter >= ids.length) return null; // some kind of mismatch, abort

                // id value
                int encodedValue = ids[counter] / 4;
                // FIXME: I have probably broken the protocol...
                if (Math.abs(status.baseValue()) > 100) {
                    // using bigdecimal here for precision when rounding
                    value = new BigDecimal(encodedValue + 30)
                            .movePointLeft(2)
                            .multiply(new BigDecimal(status.baseValue()))
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                } else {
                    // min/max must be flipped for inverted IDs due to encoding
                    value = statType.showAsInverted()
                            ? encodedValue + status.range().high()
                            : encodedValue + status.range().low();
                }

                // stars
                stars = ids[counter] % 4;

                counter++;
            }

            // create ID and append to list
            identifications.add(new StatActualValue(statType, value, stars));
        }

        // powders
        List<Powder> powderList = new ArrayList<>();
        if (gearInfo.powderSlots() > 0 && powders.length > 0) {
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
        GearInstance gearInstance = new GearInstance(identifications, powderList, rerolls, List.of());
        return new GearItem(gearInfo, gearInstance);
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
        String itemName = gearItem.getGearInfo().name();
        GearInstance gearInstance = gearItem.getGearInstance();

        // We must use Legacy ordering for compatibility reasons
        List<StatType> sortedStats = Models.Stat.getSortedStats(gearItem.getGearInfo(), StatListOrdering.LEGACY);

        // name
        StringBuilder encoded = new StringBuilder(START);
        encoded.append(ENCODE_NAME ? encodeString(itemName) : itemName);
        encoded.append(SEPARATOR);

        // ids
        for (StatType statType : sortedStats) {
            StatActualValue actualValue = gearInstance.getActualValue(statType);
            StatPossibleValues possibleValues = gearItem.getGearInfo().getPossibleValues(statType);

            int shiftedValue;

            // FIXME: I have probably broken the protocol...
            // min/max must be flipped for inverted IDs to avoid negative values
            shiftedValue = statType.showAsInverted()
                    ? actualValue.value() - possibleValues.range().high()
                    : actualValue.value() - possibleValues.range().low();

            // stars
            int stars = actualValue.stars();

            // encode value + stars in one character
            encoded.append(encodeNumber(shiftedValue * 4 + stars));
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
                        gearItem.getGearInfo().name())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(gearItem.getGearInfo().tier().getChatFormatting());

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
