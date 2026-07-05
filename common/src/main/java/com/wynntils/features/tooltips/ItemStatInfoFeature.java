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
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.stats.type.StatListOrdering;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

@ConfigCategory(Category.TOOLTIPS)
public class ItemStatInfoFeature extends Feature {
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
