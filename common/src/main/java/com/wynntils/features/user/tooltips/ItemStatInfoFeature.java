/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.ColorUtils;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.handleditems.WynnItemCache;
import com.wynntils.wynn.handleditems.items.game.GearItem;
import com.wynntils.wynn.utils.GearTooltipBuilder;
import com.wynntils.wynn.utils.WynnItemUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class ItemStatInfoFeature extends UserFeature {
    public static ItemStatInfoFeature INSTANCE;

    private final Set<GearItem> brokenItems = new HashSet<>();

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
        if (brokenItems.contains(gearItem)) return;

        try {
            GearTooltipBuilder builder = gearItem.getCache()
                    .getOrCalculate(
                            WynnItemCache.TOOLTIP_KEY,
                            () -> GearTooltipBuilder.fromItemStack(
                                    event.getItemStack(), gearItem.getGearProfile(), gearItem));
            if (builder == null) return;

            LinkedList<Component> tooltips =
                    new LinkedList<>(builder.getTooltipLines(WynnItemUtils.getCurrentIdentificationStyle()));

            if (gearItem.hasVariableIds()) {
                if (perfect && gearItem.isPerfect()) {
                    tooltips.removeFirst();
                    tooltips.addFirst(getPerfectName(gearItem.getGearProfile().getDisplayName()));
                } else if (defective && gearItem.isDefective()) {
                    tooltips.removeFirst();
                    tooltips.addFirst(getDefectiveName(gearItem.getGearProfile().getDisplayName()));
                } else if (overallPercentageInName) {
                    MutableComponent name = Component.literal(
                                    tooltips.getFirst().getString())
                            .withStyle(tooltips.getFirst().getStyle());
                    name.append(getPercentageTextComponent(gearItem.getOverallPercentage()));
                    tooltips.removeFirst();
                    tooltips.addFirst(name);
                }
            }

            event.setTooltips(tooltips);
        } catch (Exception e) {
            brokenItems.add(gearItem);
            WynntilsMod.error(
                    "Exception when creating tooltips for item "
                            + gearItem.getGearProfile().getDisplayName(),
                    e);
            WynntilsMod.warn("This item has been disabled from ItemStatInfoFeature: " + gearItem);
            McUtils.sendMessageToClient(Component.literal("Wynntils error: Problem showing tooltip for item "
                            + gearItem.getGearProfile().getDisplayName())
                    .withStyle(ChatFormatting.RED));

            if (brokenItems.size() > 10) {
                // Give up and disable feature
                throw new RuntimeException(e);
            }
        }
    }

    private MutableComponent getPercentageTextComponent(float percentage) {
        return ColorUtils.getPercentageTextComponent(percentage, colorLerp, decimalPlaces);
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
