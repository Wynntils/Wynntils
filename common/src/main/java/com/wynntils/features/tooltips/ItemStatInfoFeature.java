/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.TOOLTIPS)
public class ItemStatInfoFeature extends Feature {
    private final Map<IdentificationDecoratorType, TooltipIdentificationDecorator> identificationDecorators = Map.of(
            IdentificationDecoratorType.PERCENTAGE, new PercentageIdentificationDecorator(),
            IdentificationDecoratorType.REROLL, new RerollIdentificationDecorator(),
            IdentificationDecoratorType.RANGE, new RangeIdentificationDecorator(),
            IdentificationDecoratorType.INNER_ROLL, new InnerRollIdentificationDecorator());

    private final Map<WeightDecoratorType, TooltipWeightDecorator> weightDecorators = Map.of(
            WeightDecoratorType.OVERALL, new SimpleWeightDecorator(),
            WeightDecoratorType.FULL_DISTRIBUTION, new FullWeightDecorator(true),
            WeightDecoratorType.FULL_CONTRIBUTION, new FullWeightDecorator(false));

    private final Set<WynnItem> brokenItems = new HashSet<>();

    @Persisted
    public final Config<Boolean> showStars = new Config<>(true);

    @Persisted
    public final Config<Boolean> colorLerp = new Config<>(true);

    @Persisted
    private final Config<Boolean> legacyColors = new Config<>(false);

    @Persisted
    private final Config<ColorThreshold> perfectColorThreshold = new Config<>(ColorThreshold.NINETY_FIVE);

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
    public final Config<ItemWeightSource> itemWeights = new Config<>(ItemWeightSource.NONE);

    @Persisted
    public final Config<Boolean> overallPercentageInName = new Config<>(true);

    @Persisted
    public final Config<Boolean> showBestValueLastAlways = new Config<>(true);

    @Persisted
    public final Config<Boolean> showMaxValues = new Config<>(true);

    private static final NavigableMap<Float, TextColor> LERP_MAP = new TreeMap<>(Map.of(
            0f,
            TextColor.fromLegacyFormat(ChatFormatting.RED),
            40f,
            TextColor.fromLegacyFormat(ChatFormatting.GOLD),
            70f,
            TextColor.fromLegacyFormat(ChatFormatting.YELLOW),
            90f,
            TextColor.fromLegacyFormat(ChatFormatting.GREEN),
            100f,
            TextColor.fromLegacyFormat(ChatFormatting.AQUA)));

    private NavigableMap<Float, TextColor> flatMap = createFlatMap();

    @Override
    protected void onConfigUpdate(Config<?> config) {
        if (config == legacyColors || config == perfectColorThreshold) {
            flatMap = createFlatMap();
        }
    }

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

    public NavigableMap<Float, TextColor> getColorMap() {
        return colorLerp.get() ? LERP_MAP : flatMap;
    }

    public TooltipIdentificationDecorator getIdentificationDecorator() {
        return identificationDecorators.get(IdentificationDecoratorType.getCurrentType());
    }

    public TooltipWeightDecorator getWeightDecorator() {
        return weightDecorators.get(WeightDecoratorType.getCurrentType());
    }

    private NavigableMap<Float, TextColor> createFlatMap() {
        boolean useLegacyColors = legacyColors.get();

        float redThreshold = useLegacyColors ? 30f : 20f;
        float aquaThreshold = perfectColorThreshold.get().getThreshold();

        NavigableMap<Float, TextColor> map = new TreeMap<>();

        map.put(redThreshold, TextColor.fromLegacyFormat(ChatFormatting.RED));

        if (!useLegacyColors) {
            map.put(50f, TextColor.fromLegacyFormat(ChatFormatting.GOLD));
        }

        map.put(80f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
        map.put(aquaThreshold, TextColor.fromLegacyFormat(ChatFormatting.GREEN));
        map.put(Float.MAX_VALUE, TextColor.fromLegacyFormat(ChatFormatting.AQUA));

        return map;
    }

    private abstract static class IdentificationDecorator implements TooltipIdentificationDecorator {
        @Override
        public MutableComponent getSuffix(
                StatActualValue statActualValue, StatPossibleValues possibleValues, TooltipStyle style) {
            if (!possibleValues.range().inRange(statActualValue.value())) {
                // Our actual value lies outside the range of possible values
                // This can happen if the API data is outdated. In this case, just mark
                // the stat as "NEW".
                return Component.literal(" [NEW]").withStyle(ChatFormatting.GOLD);
            }

            return getRollSuffix(style, statActualValue, possibleValues);
        }

        protected abstract MutableComponent getRollSuffix(
                TooltipStyle style, StatActualValue actualValue, StatPossibleValues possibleValues);
    }

    private class PercentageIdentificationDecorator extends IdentificationDecorator {
        @Override
        protected MutableComponent getRollSuffix(
                TooltipStyle style, StatActualValue actualValue, StatPossibleValues possibleValues) {
            float percentage = StatCalculator.getPercentage(actualValue, possibleValues);
            MutableComponent percentageTextComponent = ColorScaleUtils.getPercentageTextComponent(
                    getColorMap(), percentage, colorLerp.get(), decimalPlaces.get());

            return percentageTextComponent;
        }
    }

    private static class RerollIdentificationDecorator extends IdentificationDecorator {
        @Override
        protected MutableComponent getRollSuffix(
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
    }

    private static class RangeIdentificationDecorator extends IdentificationDecorator {
        @Override
        protected MutableComponent getRollSuffix(
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
    }

    private static class InnerRollIdentificationDecorator extends IdentificationDecorator {
        @Override
        protected MutableComponent getRollSuffix(
                TooltipStyle style, StatActualValue actualValue, StatPossibleValues possibleValues) {
            MutableComponent rangeTextComponent = Component.literal(" <")
                    .append(Component.literal(actualValue.internalRoll().low() + "% to "
                                    + actualValue.internalRoll().high() + "%")
                            .withStyle(ChatFormatting.GREEN))
                    .append(">")
                    .withStyle(ChatFormatting.DARK_GREEN);

            return rangeTextComponent;
        }
    }

    private abstract static class WeightingDecorator implements TooltipWeightDecorator {
        @Override
        public List<MutableComponent> getLines(ItemWeighting weighting, IdentifiableItemProperty<?, ?> itemInfo) {
            return getWeightLines(weighting, itemInfo);
        }

        protected abstract List<MutableComponent> getWeightLines(
                ItemWeighting weighting, IdentifiableItemProperty<?, ?> itemInfo);
    }

    private class SimpleWeightDecorator extends WeightingDecorator {
        @Override
        protected List<MutableComponent> getWeightLines(
                ItemWeighting weighting, IdentifiableItemProperty<?, ?> itemInfo) {
            MutableComponent weightingComponent = Component.literal(" - ")
                    .append(Component.literal(weighting.weightName() + " Scale"))
                    .withStyle(ChatFormatting.GRAY);

            float percentage = Services.ItemWeight.calculateWeighting(weighting, itemInfo);
            weightingComponent.append(ColorScaleUtils.getPercentageTextComponent(
                    getColorMap(), percentage, colorLerp.get(), decimalPlaces.get()));

            return List.of(weightingComponent);
        }
    }

    private final class FullWeightDecorator extends WeightingDecorator {
        private final boolean distribution;

        private FullWeightDecorator(boolean distribution) {
            this.distribution = distribution;
        }

        @Override
        protected List<MutableComponent> getWeightLines(
                ItemWeighting weighting, IdentifiableItemProperty<?, ?> itemInfo) {
            List<MutableComponent> lines = new ArrayList<>();
            MutableComponent weightingComponent = Component.literal(" - ")
                    .append(Component.literal(weighting.weightName() + " Scale"))
                    .withStyle(ChatFormatting.GRAY);

            float weightPercentage = Services.ItemWeight.calculateWeighting(weighting, itemInfo);
            weightingComponent.append(ColorScaleUtils.getPercentageTextComponent(
                    getColorMap(), weightPercentage, colorLerp.get(), decimalPlaces.get()));

            lines.add(weightingComponent);

            Map<StatType, Pair<Float, Float>> statWeights = Services.ItemWeight.getStatWeights(weighting, itemInfo);

            statWeights.forEach((statType, weight) -> {
                String displayName = statType.getDisplayName() + " ";

                if (statType.getUnit() == StatUnit.RAW) {
                    displayName += "Raw ";
                }

                String weightStr = String.format(Locale.ROOT, "(%.1f%%)", weight.a());
                float percentage = distribution ? weight.b() : ((weight.a() / 100f) * weight.b());

                lines.add(Component.literal("   - ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(displayName))
                        .append(Component.literal(weightStr).withStyle(ChatFormatting.WHITE))
                        .append(ColorScaleUtils.getPercentageTextComponent(
                                getColorMap(), percentage, colorLerp.get(), decimalPlaces.get())));
            });
            lines.add(Component.empty());

            return lines;
        }
    }

    private enum IdentificationDecoratorType {
        INNER_ROLL(Set.of(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL)),
        REROLL(Set.of(GLFW.GLFW_KEY_LEFT_CONTROL)),
        RANGE(Set.of(GLFW.GLFW_KEY_LEFT_SHIFT)),
        PERCENTAGE(Set.of());

        private final Set<Integer> keyCodes;

        IdentificationDecoratorType(Set<Integer> keyCodes) {
            this.keyCodes = keyCodes;
        }

        public static IdentificationDecoratorType getCurrentType() {
            for (IdentificationDecoratorType type : values()) {
                if (type.keyCodes.stream().allMatch(KeyboardUtils::isKeyDown)) {
                    return type;
                }
            }

            return PERCENTAGE;
        }
    }

    private enum WeightDecoratorType {
        FULL_CONTRIBUTION(Set.of(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL)),
        FULL_DISTRIBUTION(Set.of(GLFW.GLFW_KEY_LEFT_SHIFT)),
        OVERALL(Set.of());

        private final Set<Integer> keyCodes;

        WeightDecoratorType(Set<Integer> keyCodes) {
            this.keyCodes = keyCodes;
        }

        public static WeightDecoratorType getCurrentType() {
            for (WeightDecoratorType type : values()) {
                if (type.keyCodes.stream().allMatch(KeyboardUtils::isKeyDown)) {
                    return type;
                }
            }

            return OVERALL;
        }
    }

    public enum ColorThreshold {
        NINETY_FIVE(95f),
        NINETY_SIX(96f);

        private final float threshold;

        ColorThreshold(float threshold) {
            this.threshold = threshold;
        }

        public float getThreshold() {
            return threshold;
        }
    }
}
