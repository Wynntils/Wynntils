/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.Map;
import java.util.NavigableMap;
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
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (event.getTooltips().isEmpty()) return;

        ItemHandler.getItemStackAnnotation(event.getItemStack()).ifPresent(annotation -> {
            if (!(annotation instanceof IdentifiableItemProperty<?, ?> identifiableItem)
                    || !identifiableItem.hasOverallValue()) {
                return;
            }

            boolean perfectItem = perfect.get() && identifiableItem.isPerfect();
            boolean defectiveItem = defective.get() && identifiableItem.isDefective();
            boolean showPercentage = overallPercentageInName.get()
                    && (!perfectItem && !defectiveItem || overallPercentageInPerfectDefectiveName.get());
            IdentifiableTooltipBuilder<?, ?> builder =
                    IdentifiableTooltipBuilder.fromTooltipLines(event.getTooltips(), identifiableItem);
            TooltipStyle style = new TooltipStyle(
                    identificationsOrdering.get(),
                    groupIdentifications.get(),
                    showBestValueLastAlways.get(),
                    rainbowInternalRoll.get(),
                    showRollWheel.get());
            TooltipIdentificationDecorator decorator =
                    new ItemStatInfoDecorator(identifiableItem, perfectItem, defectiveItem, showPercentage);

            event.setTooltips(builder.getTooltipLines(
                    Models.Character.getClassType(), style, decorator, ItemWeightSource.NONE, null));
        });
    }

    private MutableComponent createIdentificationPercentage(
            StatActualValue identification, StatPossibleValues possibleValues) {
        MutableComponent percentage = ColorScaleUtils.getPercentageTextComponent(
                        getColorMap(),
                        StatCalculator.getPercentage(identification, possibleValues),
                        colorLerp.get(),
                        decimalPlaces.get())
                .withStyle(style -> style.withFont(WYNNCRAFT_LANGUAGE_FONT));
        if (rainbowInternalRoll.get() && identification.stars() == 3) {
            percentage.withColor(WynncraftShaderColor.RAINBOW.color.asInt());
        }
        return percentage;
    }

    private final class ItemStatInfoDecorator implements TooltipIdentificationDecorator {
        private final IdentifiableItemProperty<?, ?> identifiableItem;
        private final boolean perfectItem;
        private final boolean defectiveItem;
        private final boolean showPercentage;

        private ItemStatInfoDecorator(
                IdentifiableItemProperty<?, ?> identifiableItem,
                boolean perfectItem,
                boolean defectiveItem,
                boolean showPercentage) {
            this.identifiableItem = identifiableItem;
            this.perfectItem = perfectItem;
            this.defectiveItem = defectiveItem;
            this.showPercentage = showPercentage;
        }

        @Override
        public MutableComponent getTitle(Component title) {
            return createDecoratedName(title, identifiableItem, perfectItem, defectiveItem, showPercentage);
        }

        @Override
        public MutableComponent getSuffix(
                StatActualValue actualValue, StatPossibleValues possibleValues, TooltipStyle style) {
            return identificationDecorations.get()
                    ? createIdentificationPercentage(actualValue, possibleValues)
                    : Component.empty();
        }
    }

    private MutableComponent createDecoratedName(
            Component name,
            IdentifiableItemProperty<?, ?> identifiableItem,
            boolean perfectItem,
            boolean defectiveItem,
            boolean showPercentage) {
        MutableComponent decoratedName;
        if (perfectItem) {
            decoratedName = ComponentUtils.makeRainbowStyle("Perfect " + name.getString(), true);
        } else if (defectiveItem) {
            decoratedName = ComponentUtils.makeCrimsonStyle("Defective " + name.getString(), true);
        } else {
            decoratedName = name.copy();
        }

        if (showPercentage) {
            decoratedName.append(ColorScaleUtils.getPercentageTextComponent(
                    getColorMap(), identifiableItem.getOverallPercentage(), colorLerp.get(), decimalPlaces.get()));
        }
        return decoratedName;
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
