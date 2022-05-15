/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.Utils;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.LARGE, performance = PerformanceImpact.SMALL)
public class ItemStatInfoFeature extends Feature {

    // TODO: Replace these with configs
    public static final boolean showStars = true;
    public static final boolean colorLerp = true;

    public static final boolean perfect = true;
    public static final boolean defective = true;
    public static final float obfuscationChanceStart = 0.08f;
    public static final float obfuscationChanceEnd = 0.04f;

    public static final boolean reorderIdentifications = true;
    public static final boolean groupIdentifications = true;

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        return WebManager.isItemListLoaded() || WebManager.tryLoadItemList();
    }

    @Override
    protected void onDisable() {}

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.itemStatInfo.name");
    }

    public static MutableComponent getPercentageTextComponent(float percentage) {
        Style color = Style.EMPTY
                .withColor(colorLerp ? getPercentageColor(percentage) : getFlatPercentageColor(percentage))
                .withItalic(false);
        return new TextComponent(String.format(Utils.getGameLocale(), " [%.1f%%]", percentage)).withStyle(color);
    }

    public static MutableComponent getRangeTextComponent(int min, int max) {
        return new TextComponent(" [")
                .append(new TextComponent(min + ", " + max).withStyle(ChatFormatting.GREEN))
                .append("]")
                .withStyle(ChatFormatting.DARK_GREEN);
    }

    public static MutableComponent getRerollChancesComponent(double perfect, double increase, double decrease) {
        return new TextComponent(String.format(Utils.getGameLocale(), " \u2605%.2f%%", perfect * 100))
                .withStyle(ChatFormatting.AQUA)
                .append(new TextComponent(String.format(Utils.getGameLocale(), " \u21E7%.1f%%", increase * 100))
                        .withStyle(ChatFormatting.GREEN))
                .append(new TextComponent(String.format(Utils.getGameLocale(), " \u21E9%.1f%%", decrease * 100))
                        .withStyle(ChatFormatting.RED));
    }

    private static final TreeMap<Float, TextColor> colorMap = new TreeMap<>() {
        {
            put(0f, TextColor.fromLegacyFormat(ChatFormatting.RED));
            put(30f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
            put(80f, TextColor.fromLegacyFormat(ChatFormatting.GREEN));
            put(96f, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
        }
    };

    private static TextColor getPercentageColor(float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = colorMap.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = colorMap.ceilingEntry(percentage);

        // Boundary conditions
        if (lowerEntry == null) {
            return higherEntry.getValue();
        } else if (higherEntry == null) {
            return lowerEntry.getValue();
        }

        if (Objects.equals(lowerEntry.getKey(), higherEntry.getKey())) {
            return lowerEntry.getValue();
        }

        float t = MathUtils.inverseLerp(lowerEntry.getKey(), higherEntry.getKey(), percentage);

        int lowerColor = lowerEntry.getValue().getValue();
        int higherColor = higherEntry.getValue().getValue();

        int r = (int) MathUtils.lerp((lowerColor >> 16) & 0xff, (higherColor >> 16) & 0xff, t);
        int g = (int) MathUtils.lerp((lowerColor >> 8) & 0xff, (higherColor >> 8) & 0xff, t);
        int b = (int) MathUtils.lerp(lowerColor & 0xff, higherColor & 0xff, t);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }

    private static TextColor getFlatPercentageColor(float percentage) {
        if (percentage < 30f) {
            return TextColor.fromLegacyFormat(ChatFormatting.RED);
        } else if (percentage < 80f) {
            return TextColor.fromLegacyFormat(ChatFormatting.YELLOW);
        } else if (percentage < 96f) {
            return TextColor.fromLegacyFormat(ChatFormatting.GREEN);
        } else {
            return TextColor.fromLegacyFormat(ChatFormatting.AQUA);
        }
    }
}
