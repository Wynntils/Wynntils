/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item;

import com.wynntils.core.webapi.profiles.item.IdentificationModifier;
import com.wynntils.core.webapi.profiles.item.IdentificationProfile;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.features.ItemStatInfoFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.wc.objects.SpellType;
import com.wynntils.wc.utils.IdentificationOrderer;
import com.wynntils.wc.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class ItemIdentificationContainer {
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    private ItemProfile item;
    private IdentificationProfile identification;
    private IdentificationModifier modifier;

    private String shortIdName;
    private int value;
    private boolean isRaw;
    private int stars;
    private float percent;

    private Component rawLoreLine;
    private Component percentLoreLine;
    private Component rangeLoreLine;
    private Component rerollLoreLine;

    public ItemIdentificationContainer(
            ItemProfile item,
            IdentificationProfile identification,
            IdentificationModifier modifier,
            String shortIdName,
            int value,
            boolean isRaw,
            int stars,
            float percent,
            Component rawLoreLine,
            Component percentLoreLine,
            Component rangeLoreLine,
            Component rerollLoreLine) {
        this.item = item;
        this.identification = identification;
        this.modifier = modifier;
        this.shortIdName = shortIdName;
        this.value = value;
        this.isRaw = isRaw;
        this.stars = stars;
        this.percent = percent;
        this.rawLoreLine = rawLoreLine;
        this.percentLoreLine = percentLoreLine;
        this.rangeLoreLine = rangeLoreLine;
        this.rerollLoreLine = rerollLoreLine;
    }

    public ItemProfile getItem() {
        return item;
    }

    public IdentificationProfile getIdentificationContainer() {
        return identification;
    }

    public IdentificationModifier getIdentificationModifier() {
        return modifier;
    }

    public boolean isNew() {
        return (identification == null || identification.isInvalidValue(value));
    }

    public boolean isFixed() {
        return (isNew() ? false : identification.hasConstantValue());
    }

    public String getShortIdName() {
        return shortIdName;
    }

    public int getValue() {
        return value;
    }

    public boolean isRaw() {
        return isRaw;
    }

    public int getStarCount() {
        return stars;
    }

    public float getPercent() {
        return percent;
    }

    public Component getRawLoreLine() {
        return rawLoreLine;
    }

    public Component getPercentLoreLine() {
        return percentLoreLine;
    }

    public Component getRangeLoreLine() {
        return rangeLoreLine;
    }

    public Component getRerollLoreLine() {
        return rerollLoreLine;
    }

    public static ItemIdentificationContainer fromLore(Component lore, ItemProfile item) {
        String unformattedLoreLine = WynnUtils.normalizeBadString(lore.getString());
        Matcher identificationMatcher = ITEM_IDENTIFICATION_PATTERN.matcher(unformattedLoreLine);
        if (!identificationMatcher.find()) return null; // not a valid id line

        String idName = identificationMatcher.group("ID");
        boolean isRaw = identificationMatcher.group("Suffix") == null;
        int starCount = identificationMatcher.group("Stars").length();
        int value = Integer.parseInt(identificationMatcher.group("Value"));

        String shortIdName;
        SpellType spell = SpellType.fromName(idName);
        if (spell != null) {
            shortIdName = spell.getShortIdName(isRaw);
        } else {
            shortIdName = IdentificationProfile.getAsShortName(idName, isRaw);
        }

        boolean isInverted = IdentificationOrderer.INSTANCE.isInverted(shortIdName);
        IdentificationProfile idProfile = item.getStatuses().get(shortIdName);
        IdentificationModifier type =
                idProfile != null ? idProfile.getType() : IdentificationProfile.getTypeFromName(shortIdName);
        if (type == null) return null; // not a valid id

        MutableComponent percentLine = new TextComponent("");
        MutableComponent rangeLine;
        MutableComponent rerollLine;

        MutableComponent statInfo = new TextComponent((value > 0 ? "+" : "") + value + type.getInGame(shortIdName));
        statInfo.setStyle(Style.EMPTY.withColor(isInverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));

        percentLine.append(statInfo);

        if (ItemStatInfoFeature.showStars)
            percentLine.append(new TextComponent("***".substring(3 - starCount)).withStyle(ChatFormatting.DARK_GREEN));

        percentLine.append(new TextComponent(" " + idName).withStyle(ChatFormatting.GRAY));

        boolean isNew = idProfile == null || idProfile.isInvalidValue(value);

        if (isNew) percentLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));

        rangeLine = percentLine.copy();
        rerollLine = percentLine.copy();

        float percentage = -1;
        if (!isNew && !idProfile.hasConstantValue()) {
            // calculate percent/range/reroll chances, append to lines
            int min = idProfile.getMin();
            int max = idProfile.getMax();

            if (isInverted) {
                percentage = MathUtils.inverseLerp(max, min, value) * 100;
            } else {
                percentage = MathUtils.inverseLerp(min, max, value) * 100;
            }

            IdentificationProfile.ReidentificationChances chances = idProfile.getChances(value, isInverted, starCount);

            percentLine.append(ItemStatInfoFeature.getPercentageTextComponent(percentage));

            rangeLine.append(ItemStatInfoFeature.getRangeTextComponent(min, max));

            rerollLine.append(ItemStatInfoFeature.getRerollChancesComponent(
                    idProfile.getPerfectChance(), chances.increase(), chances.decrease()));
        }

        // create container
        return new ItemIdentificationContainer(
                item,
                idProfile,
                type,
                shortIdName,
                value,
                isRaw,
                starCount,
                percentage,
                lore,
                percentLine,
                rangeLine,
                rerollLine);
    }

    public static List<ItemIdentificationContainer> fromProfile(ItemProfile item) {
        List<ItemIdentificationContainer> ids = new ArrayList<>();

        for (Map.Entry<String, IdentificationProfile> entry : item.getStatuses().entrySet()) {
            IdentificationProfile idProfile = entry.getValue();
            IdentificationModifier type = idProfile.getType();
            String idName = entry.getKey();
            MutableComponent line;

            boolean inverted = IdentificationOrderer.INSTANCE.isInverted(idName);
            if (idProfile.hasConstantValue()) {
                int value = idProfile.getBaseValue();
                line = new TextComponent((value > 0 ? "+" : "") + value + type.getInGame(idName));
                line.setStyle(
                        Style.EMPTY.withColor(inverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));
            } else {
                int min = idProfile.getMin();
                int max = idProfile.getMax();
                ChatFormatting mainColor = inverted ^ (min > 0) ? ChatFormatting.GREEN : ChatFormatting.RED;
                ChatFormatting textColor = inverted ^ (min > 0) ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;
                line = new TextComponent((min > 0 ? "+" : "") + min).withStyle(mainColor);
                line.append(new TextComponent(" to ").withStyle(textColor));
                line.append(
                        new TextComponent((max > 0 ? "+" : "") + max + type.getInGame(idName)).withStyle(mainColor));
            }

            line.append(new TextComponent(" " + IdentificationProfile.getAsLongName(idName))
                    .withStyle(ChatFormatting.GRAY));

            ItemIdentificationContainer id = new ItemIdentificationContainer(
                    item,
                    idProfile,
                    type,
                    idName,
                    0,
                    type == IdentificationModifier.Integer,
                    0,
                    -1,
                    line,
                    line,
                    line,
                    line);
            ids.add(id);
        }

        return ids;
    }
}
