/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TOOLTIPS)
public class ItemStatInfoFeature extends Feature {
    private final Set<WynnItem> brokenItems = new HashSet<>();

    @Persisted
    public final Config<Boolean> showStars = new Config<>(true);

    @Persisted
    public final Config<Boolean> colorLerp = new Config<>(true);

    @Persisted
    public final Config<Integer> decimalPlaces = new Config<>(1);

    @Persisted
    public final Config<Boolean> perfect = new Config<>(true);

    @Persisted
    public final Config<Boolean> defective = new Config<>(true);

    @Persisted
    public final Config<Float> obfuscationChanceStart = new Config<>(0.08f);

    @Persisted
    public final Config<Float> obfuscationChanceEnd = new Config<>(0.04f);

    @Persisted
    public final Config<StatListOrdering> identificationsOrdering = new Config<>(StatListOrdering.DEFAULT);

    @Persisted
    public final Config<Boolean> groupIdentifications = new Config<>(true);

    @Persisted
    public final Config<Boolean> identificationDecorations = new Config<>(true);

    @Persisted
    public final Config<Boolean> overallPercentageInName = new Config<>(true);

    @Persisted
    public final Config<Boolean> showBestValueLastAlways = new Config<>(true);

    @Persisted
    public final Config<Boolean> showMaxValues = new Config<>(true);

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) return;

        ItemStack itemStack = event.getItemStack();
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;

        WynnItem wynnItem = wynnItemOpt.get();
        if (brokenItems.contains(wynnItem)) return;

        try {
            List<Component> tooltips = TooltipUtils.getWynnItemTooltip(itemStack, wynnItem);

            if (tooltips.isEmpty()) return;
            event.setTooltips(tooltips);
        } catch (Exception e) {
            brokenItems.add(wynnItem);

            String itemName = wynnItem.getClass().getSimpleName();
            Optional<NamedItemProperty> namedItemPropertyOpt =
                    Models.Item.asWynnItemProperty(event.getItemStack(), NamedItemProperty.class);
            if (namedItemPropertyOpt.isPresent()) {
                itemName = namedItemPropertyOpt.get().getName();
            }

            WynntilsMod.error("Exception when creating tooltips for item " + itemName, e);
            WynntilsMod.warn("This item has been disabled from ItemStatInfoFeature: " + wynnItem);
            McUtils.sendErrorToClient("Wynntils error: Problem showing tooltip for item " + itemName);

            if (brokenItems.size() > 10) {
                // Give up and disable feature
                throw new RuntimeException(e);
            }
        }
    }

    public class IdentificationDecorator implements TooltipIdentificationDecorator {
        @Override
        public MutableComponent getSuffix(
                StatActualValue statActualValue, StatPossibleValues possibleValues, TooltipStyle style) {
            if (!possibleValues.range().inRange(statActualValue.value())) {
                // Our actual value lies outside the range of possible values
                // This can happen if the API data is outdated. In this case, just mark
                // the stat as "NEW".
                return Component.literal(" [NEW]").withStyle(ChatFormatting.GOLD);
            }

            if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
                    && KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                return getInnerRollSuffix(style, statActualValue, possibleValues);
            } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                return getRangeSuffix(style, statActualValue, possibleValues);
            } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                return getRerollSuffix(style, statActualValue, possibleValues);
            } else {
                return getPercentSuffix(style, statActualValue, possibleValues);
            }
        }

        private MutableComponent getInnerRollSuffix(
                TooltipStyle style, StatActualValue statActualValue, StatPossibleValues possibleValues) {
            MutableComponent rangeTextComponent = Component.literal(" <")
                    .append(Component.literal(statActualValue.internalRoll().low() + "% to "
                                    + statActualValue.internalRoll().high() + "%")
                            .withStyle(ChatFormatting.GREEN))
                    .append(">")
                    .withStyle(ChatFormatting.DARK_GREEN);

            return rangeTextComponent;
        }

        private MutableComponent getRangeSuffix(
                TooltipStyle style, StatActualValue actualValue, StatPossibleValues possibleValues) {
            Pair<Integer, Integer> displayRange =
                    StatCalculator.getDisplayRange(possibleValues, style.showBestValueLastAlways());

            MutableComponent rangeTextComponent = Component.literal(" [")
                    .append(Component.literal(displayRange.a() + ", " + displayRange.b())
                            .withStyle(ChatFormatting.GREEN))
                    .append("]")
                    .withStyle(ChatFormatting.DARK_GREEN);

            return rangeTextComponent;
        }

        private MutableComponent getRerollSuffix(
                TooltipStyle style, StatActualValue actualValue, StatPossibleValues possibleValues) {
            MutableComponent rerollChancesComponent = Component.literal(String.format(
                            Locale.ROOT, " \u2605%.2f%%", StatCalculator.getPerfectChance(possibleValues)))
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.format(
                                    Locale.ROOT,
                                    " \u21E7%.1f%%",
                                    StatCalculator.getIncreaseChance(actualValue, possibleValues)))
                            .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(String.format(
                                    Locale.ROOT,
                                    " \u21E9%.1f%%",
                                    StatCalculator.getDecreaseChance(actualValue, possibleValues)))
                            .withStyle(ChatFormatting.RED));

            return rerollChancesComponent;
        }

        private MutableComponent getPercentSuffix(
                TooltipStyle style, StatActualValue actualValue, StatPossibleValues possibleValues) {
            float percentage = StatCalculator.getPercentage(actualValue, possibleValues);
            MutableComponent percentageTextComponent =
                    ColorScaleUtils.getPercentageTextComponent(percentage, colorLerp.get(), decimalPlaces.get());

            return percentageTextComponent;
        }
    }
}
