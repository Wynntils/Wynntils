/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.mc.event.ItemTooltipLinesEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.TooltipUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class ItemStatInfoFeature extends Feature {
    private static final FontDescription IDENTIFICATION_METER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/identification/meter"));
    private static final FontDescription WYNNCRAFT_LANGUAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("language/wynncraft"));

    @Persisted
    public final Config<Boolean> perfect = new Config<>(true);

    @Persisted
    public final Config<Boolean> defective = new Config<>(true);

    @Persisted
    public final Config<StatListOrdering> identificationsOrdering = new Config<>(StatListOrdering.DEFAULT);

    @Persisted
    public final Config<Boolean> groupIdentifications = new Config<>(true);

    @Persisted
    public final Config<Boolean> identificationDecorations = new Config<>(true);

    @Persisted
    public final Config<ItemWeightSource> itemWeights = new Config<>(ItemWeightSource.ALL);

    @Persisted
    public final Config<Boolean> overallPercentageInName = new Config<>(true);

    @Persisted
    public final Config<Boolean> overallPercentageInPerfectDefectiveName = new Config<>(true);

    @Persisted
    public final Config<Boolean> showBestValueLastAlways = new Config<>(true);

    @Persisted
    public final Config<Boolean> rainbowInternalRoll = new Config<>(true);

    @Persisted
    public final Config<Boolean> showRollWheel = new Config<>(true);

    @Persisted
    public final Config<Boolean> colorLerp = new Config<>(true);

    @Persisted
    private final Config<Boolean> legacyColors = new Config<>(false);

    @Persisted
    private final Config<ColorThreshold> perfectColorThreshold = new Config<>(ColorThreshold.NINETY_FIVE);

    @Persisted
    public final Config<Integer> decimalPlaces = new Config<>(1);

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

    public ItemStatInfoFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE, ConfigProfile.MINIMAL)
                .build());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        if (config == legacyColors || config == perfectColorThreshold) {
            flatMap = createFlatMap();
        }
    }

    public NavigableMap<Float, TextColor> getColorMap() {
        return colorLerp.get() ? LERP_MAP : flatMap;
    }

    @SubscribeEvent
    public void onTooltipLines(ItemTooltipLinesEvent event) {
        if (event.getTooltipLines().isEmpty()) return;

        ItemHandler.getItemStackAnnotation(event.getItemStack()).ifPresent(annotation -> {
            if (!(annotation instanceof IdentifiableItemProperty<?, ?> identifiableItem)
                    || !identifiableItem.hasOverallValue()) {
                return;
            }

            boolean perfectItem = perfect.get() && identifiableItem.isPerfect();
            boolean defectiveItem = defective.get() && identifiableItem.isDefective();
            boolean showPercentage = overallPercentageInName.get()
                    && (!perfectItem && !defectiveItem || overallPercentageInPerfectDefectiveName.get());
            boolean decorateTitle = perfectItem || defectiveItem || showPercentage;

            List<Component> tooltips = new ArrayList<>(event.getTooltipLines());
            if (decorateTitle) {
                for (int i = 1; i < tooltips.size(); i++) {
                    MutableComponent line = tooltips.get(i).copy();
                    if (!decorateItemName(line, identifiableItem, perfectItem, defectiveItem, showPercentage)) continue;

                    tooltips.set(i, line);
                    break;
                }
            }

            if (!identificationDecorations.get()) {
                event.setTooltipLines(tooltips);
                return;
            }

            List<StatActualValue> remainingIdentifications = identifiableItem.getIdentifications().stream()
                    .filter(identification -> findPossibleValues(identification, identifiableItem)
                            .filter(possibleValues -> !possibleValues.range().isFixed())
                            .isPresent())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            for (int i = 0; i < tooltips.size(); i++) {
                Component line = tooltips.get(i);
                if (!TooltipUtils.containsFont(line, IDENTIFICATION_METER_FONT)) continue;

                Optional<StatActualValue> identification = findIdentification(line, remainingIdentifications);
                if (identification.isEmpty()) continue;

                MutableComponent decoratedLine = line.copy();
                if (replaceIdentificationMeter(decoratedLine, identification.get(), identifiableItem)) {
                    tooltips.set(i, decoratedLine);
                    remainingIdentifications.remove(identification.get());
                }
            }

            event.setTooltipLines(tooltips);
        });
    }

    private boolean decorateItemName(
            MutableComponent line,
            IdentifiableItemProperty<?, ?> identifiableItem,
            boolean perfectItem,
            boolean defectiveItem,
            boolean showPercentage) {
        List<Component> siblings = line.getSiblings();
        for (int i = siblings.size() - 1; i >= 0; i--) {
            Component sibling = siblings.get(i);
            String text = sibling.getString().trim();
            if (!text.equals(identifiableItem.getName()) && !text.endsWith(identifiableItem.getName())) continue;

            MutableComponent name = createDecoratedName(sibling, perfectItem, defectiveItem);
            if (showPercentage) {
                name.append(ColorScaleUtils.getPercentageTextComponent(
                        getColorMap(),
                        identifiableItem.getOverallPercentage(),
                        colorLerp.get(),
                        decimalPlaces.get()));
            }
            siblings.set(i, name);
            return true;
        }

        return false;
    }

    private MutableComponent createDecoratedName(Component name, boolean perfectItem, boolean defectiveItem) {
        if (perfectItem) {
            return ComponentUtils.makeRainbowStyle("Perfect " + name.getString(), true);
        }

        if (defectiveItem) {
            return ComponentUtils.makeCrimsonStyle("Defective " + name.getString(), true);
        }

        return name.copy();
    }

    private Optional<StatActualValue> findIdentification(
            Component line, List<StatActualValue> remainingIdentifications) {
        String lineText = line.getString();
        return remainingIdentifications.stream()
                .filter(identification -> lineText.contains(getDisplayedValue(identification)))
                .findFirst();
    }

    private String getDisplayedValue(StatActualValue identification) {
        int value = identification.statType().calculateAsInverted() ? -identification.value() : identification.value();
        return StringUtils.toSignedCommaString(value) + identification.statType().getUnit().getDisplayName();
    }

    private boolean replaceIdentificationMeter(
            MutableComponent line,
            StatActualValue identification,
            IdentifiableItemProperty<?, ?> identifiableItem) {
        StatPossibleValues possibleValues = findPossibleValues(identification, identifiableItem).orElseThrow();
        MutableComponent percentage = ColorScaleUtils.getPercentageTextComponent(
                        getColorMap(),
                        StatCalculator.getPercentage(identification, possibleValues),
                        colorLerp.get(),
                        decimalPlaces.get())
                .withStyle(style -> style.withFont(WYNNCRAFT_LANGUAGE_FONT));
        if (rainbowInternalRoll.get() && identification.stars() == 3) {
            percentage.withColor(WynncraftShaderColor.RAINBOW.color.asInt());
        }

        return replaceMeterComponent(line, percentage);
    }

    private Optional<StatPossibleValues> findPossibleValues(
            StatActualValue identification, IdentifiableItemProperty<?, ?> identifiableItem) {
        return identifiableItem.getPossibleValues().stream()
                .filter(possibleValues -> possibleValues.statType().equals(identification.statType()))
                .findFirst();
    }

    private boolean replaceMeterComponent(MutableComponent component, Component replacement) {
        List<Component> siblings = component.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            Component sibling = siblings.get(i);
            if (TooltipUtils.containsFont(sibling, IDENTIFICATION_METER_FONT)) {
                if (i > 0 && siblings.get(i - 1).getString().equals(" ")) {
                    siblings.set(i - 1, replacement);
                    siblings.remove(i);
                    return true;
                }

                siblings.set(i, replacement);
                return true;
            }

            MutableComponent copiedSibling = sibling.copy();
            if (!replaceMeterComponent(copiedSibling, replacement)) continue;

            siblings.set(i, copiedSibling);
            return true;
        }

        return false;
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
