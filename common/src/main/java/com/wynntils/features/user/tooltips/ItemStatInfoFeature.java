/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.handleditems.WynnItemCache;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.utils.GearTooltipBuilder;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class ItemStatInfoFeature extends UserFeature {
    public static ItemStatInfoFeature INSTANCE;

    private static final NavigableMap<Float, TextColor> COLOR_MAP = new TreeMap<>();

    static {
        COLOR_MAP.put(0f, TextColor.fromLegacyFormat(ChatFormatting.RED));
        COLOR_MAP.put(70f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
        COLOR_MAP.put(90f, TextColor.fromLegacyFormat(ChatFormatting.GREEN));
        COLOR_MAP.put(100f, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
    }

    @Config
    public boolean showStars = true;

    @Config
    public boolean colorLerp = true;

    @Config
    public int decimalPlaces = 1;

    @Config
    public boolean perfect = true;

    @Config
    public boolean defective = true;

    @Config
    public float obfuscationChanceStart = 0.08f;

    @Config
    public float obfuscationChanceEnd = 0.04f;

    @Config
    public boolean reorderIdentifications = true;

    @Config
    public boolean groupIdentifications = true;

    @Config
    public boolean overallPercentageInName = true;

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) return;

        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(event.getItemStack(), GearItem.class);
        if (gearItemOpt.isEmpty()) return;

        GearItem gearItem = gearItemOpt.get();

        GearTooltipBuilder builder = gearItem.getCache()
                .getOrCalculate(
                        WynnItemCache.TOOLTIP_KEY,
                        () -> GearTooltipBuilder.fromItemStack(
                                event.getItemStack(), gearItem.getItemProfile(), gearItem, false));
        if (builder == null) return;

        LinkedList<Component> tooltips = new LinkedList<>(builder.getTooltipLines());

        if (perfect && gearItem.isPerfect()) {
            tooltips.removeFirst();
            tooltips.addFirst(getPerfectName(gearItem.getItemProfile().getDisplayName()));
        } else if (defective && gearItem.isDefective()) {
            tooltips.removeFirst();
            tooltips.addFirst(getDefectiveName(gearItem.getItemProfile().getDisplayName()));
        } else if (overallPercentageInName) {
            MutableComponent name = Component.literal(tooltips.getFirst().getString())
                    .withStyle(tooltips.getFirst().getStyle());
            name.append(getPercentageTextComponent(gearItem.getOverallPercentage()));
            tooltips.removeFirst();
            tooltips.addFirst(name);
        }

        event.setTooltips(tooltips);
    }

    public MutableComponent getPercentageTextComponent(float percentage) {
        Style color = Style.EMPTY
                .withColor(colorLerp ? getPercentageColor(percentage) : getFlatPercentageColor(percentage))
                .withItalic(false);
        String percentString = new BigDecimal(percentage)
                .setScale(decimalPlaces, RoundingMode.DOWN)
                .toPlainString();
        return Component.literal(" [" + percentString + "%]").withStyle(color);
    }

    private TextColor getPercentageColor(float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = COLOR_MAP.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = COLOR_MAP.ceilingEntry(percentage);

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

    private TextColor getFlatPercentageColor(float percentage) {
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

    private MutableComponent getPerfectName(String itemName) {
        MutableComponent newName = Component.literal("").withStyle(ChatFormatting.BOLD);

        String name = "Perfect " + itemName;

        int cycle = 5000;

        /*
         * This math was originally based off Avaritia code.
         * Special thanks for Morpheus1101 and SpitefulFox
         * Avaritia Repo: https://github.com/Morpheus1101/Avaritia
         */

        int time = (int) (System.currentTimeMillis() % cycle);
        for (int i = 0; i < name.length(); i++) {
            int hue = (time + i * cycle / 7) % cycle;
            Style color = Style.EMPTY
                    .withColor(Color.HSBtoRGB((hue / (float) cycle), 0.8F, 0.8F))
                    .withItalic(false);

            newName.append(Component.literal(String.valueOf(name.charAt(i))).setStyle(color));
        }

        return newName;
    }

    private MutableComponent getDefectiveName(String itemName) {
        MutableComponent newName = Component.literal("").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
        newName.setStyle(newName.getStyle().withItalic(false));

        String name = "Defective " + itemName;

        boolean obfuscated = Math.random() < obfuscationChanceStart;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < name.length() - 1; i++) {
            current.append(name.charAt(i));

            float chance =
                    MathUtils.lerp(obfuscationChanceStart, obfuscationChanceEnd, (i + 1) / (float) (name.length() - 1));

            if (!obfuscated && Math.random() < chance) {
                newName.append(Component.literal(current.toString()).withStyle(Style.EMPTY.withItalic(false)));
                current = new StringBuilder();

                obfuscated = true;
            } else if (obfuscated && Math.random() > chance) {
                newName.append(Component.literal(current.toString())
                        .withStyle(Style.EMPTY.withObfuscated(true).withItalic(false)));
                current = new StringBuilder();

                obfuscated = false;
            }
        }

        current.append(name.charAt(name.length() - 1));

        if (obfuscated) {
            newName.append(Component.literal(current.toString())
                    .withStyle(Style.EMPTY.withItalic(false).withObfuscated(true)));
        } else {
            newName.append(Component.literal(current.toString()).withStyle(Style.EMPTY.withItalic(false)));
        }

        return newName;
    }
}
