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
import com.wynntils.models.gearinfo.tooltip.GearTooltipBuilder;
import com.wynntils.models.gearinfo.tooltip.GearTooltipStyle;
import com.wynntils.models.gearinfo.tooltip.GearTooltipSuffixType;
import com.wynntils.models.gearinfo.type.GearInfo;
import com.wynntils.models.gearinfo.type.GearInstance;
import com.wynntils.models.items.WynnItemCache;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class ItemStatInfoFeature extends UserFeature {
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
    public StatListOrdering identificationsOrdering = StatListOrdering.DEFAULT;

    @Config
    public boolean groupIdentifications = true;

    @Config
    public boolean overallPercentageInName = true;

    @Config
    public boolean showBestValueLastAlways = true;

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) return;

        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(event.getItemStack(), GearItem.class);
        if (gearItemOpt.isEmpty()) return;

        GearItem gearItem = gearItemOpt.get();
        if (brokenItems.contains(gearItem)) return;

        GearInfo gearInfo = gearItem.getGearInfo();

        try {
            GearTooltipBuilder builder = gearItem.getCache()
                    .getOrCalculate(
                            WynnItemCache.TOOLTIP_KEY,
                            () -> GearTooltipBuilder.fromParsedItemStack(event.getItemStack(), gearItem));
            if (builder == null) return;

            LinkedList<Component> tooltips = new LinkedList<>(builder.getTooltipLines(getCurrentIdentificationStyle()));

            Optional<GearInstance> optionalGearInstance = gearItem.getGearInstance();
            if (optionalGearInstance.isPresent()) {
                GearInstance gearInstance = optionalGearInstance.get();

                // Update name depending on overall percentage; this needs to be done every rendering
                // for rainbow/defective effects
                if (overallPercentageInName && gearInstance.hasOverallValue()) {
                    updateItemName(gearInfo, gearInstance, tooltips);
                }
            }

            event.setTooltips(tooltips);
        } catch (Exception e) {
            brokenItems.add(gearItem);
            WynntilsMod.error("Exception when creating tooltips for item " + gearInfo.name(), e);
            WynntilsMod.warn("This item has been disabled from ItemStatInfoFeature: " + gearItem);
            McUtils.sendMessageToClient(
                    Component.literal("Wynntils error: Problem showing tooltip for item " + gearInfo.name())
                            .withStyle(ChatFormatting.RED));

            if (brokenItems.size() > 10) {
                // Give up and disable feature
                throw new RuntimeException(e);
            }
        }
    }

    private void updateItemName(GearInfo gearInfo, GearInstance gearInstance, LinkedList<Component> tooltips) {
        MutableComponent name;
        if (perfect && gearInstance.isPerfect()) {
            name = ComponentUtils.makeRainbowStyle("Perfect " + gearInfo.name());
        } else if (defective && gearInstance.isDefective()) {
            name = ComponentUtils.makeObfuscated(
                    "Defective " + gearInfo.name(), obfuscationChanceStart, obfuscationChanceEnd);
        } else {
            name = tooltips.getFirst().copy();
            name.append(ColorScaleUtils.getPercentageTextComponent(
                    gearInstance.getOverallPercentage(), colorLerp, decimalPlaces));
        }
        tooltips.removeFirst();
        tooltips.addFirst(name);
    }

    private GearTooltipStyle getCurrentIdentificationStyle() {
        GearTooltipSuffixType decorations;
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            decorations = GearTooltipSuffixType.RANGE;
        } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            decorations = GearTooltipSuffixType.REROLL_CHANCE;
        } else {
            decorations = GearTooltipSuffixType.PERCENT;
        }

        return new GearTooltipStyle(
                decorations,
                identificationsOrdering,
                groupIdentifications,
                showBestValueLastAlways,
                showStars,
                colorLerp,
                decimalPlaces);
    }
}
