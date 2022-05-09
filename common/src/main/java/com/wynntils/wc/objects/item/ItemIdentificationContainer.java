/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.item;

import com.wynntils.core.webapi.profiles.item.IdentificationContainer;
import com.wynntils.core.webapi.profiles.item.IdentificationModifier;
import com.wynntils.core.webapi.profiles.item.ItemProfile;
import com.wynntils.features.ItemStatInfoFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.wc.objects.SpellType;
import com.wynntils.wc.utils.IdentificationOrderer;
import com.wynntils.wc.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

public class ItemIdentificationContainer {
    private static final Pattern ITEM_IDENTIFICATION_PATTERN =
            Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                    + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    private ItemProfile item;
    private IdentificationContainer identification;
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
            IdentificationContainer identification,
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

    public IdentificationContainer getIdentificationContainer() {
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
            shortIdName = IdentificationContainer.getAsShortName(idName, isRaw);
        }

        boolean isInverted = IdentificationOrderer.INSTANCE.isInverted(shortIdName);
        IdentificationContainer container = item.getStatuses().get(shortIdName);
        IdentificationModifier type =
                container != null ? container.getType() : IdentificationContainer.getTypeFromName(shortIdName);
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

        boolean isNew = container == null || container.isInvalidValue(value);

        if (isNew) percentLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));

        rangeLine = percentLine.copy();
        rerollLine = percentLine.copy();

        float percentage = -1;
        if (!isNew && !container.hasConstantValue()) {
            // calculate percent/range/reroll chances, append to lines
            int min = container.getMin();
            int max = container.getMax();

            if (isInverted) {
                percentage = MathUtils.inverseLerp(max, min, value) * 100;
            } else {
                percentage = MathUtils.inverseLerp(min, max, value) * 100;
            }

            IdentificationContainer.ReidentificationChances chances =
                    container.getChances(value, isInverted, starCount);

            percentLine.append(ItemStatInfoFeature.getPercentageTextComponent(percentage));

            rangeLine.append(ItemStatInfoFeature.getRangeTextComponent(min, max));

            rerollLine.append(ItemStatInfoFeature.getRerollChancesComponent(
                    container.getPerfectChance(), chances.increase(), chances.decrease()));
        }

        // create container
        return new ItemIdentificationContainer(
                item,
                container,
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
}
