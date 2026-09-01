/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.handlers.tooltip.type.TooltipOptions;
import com.wynntils.handlers.tooltip.type.TooltipOptions.IdentificationDisplay;
import com.wynntils.handlers.tooltip.type.TooltipOptions.WeightDisplay;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class ItemStatInfoFeature extends Feature {
    private final Set<WynnItem> brokenItems = new HashSet<>();

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
    public final Config<Boolean> craftedPercentages = new Config<>(true);

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

    @RegisterKeyBind
    private final KeyBind identificationRangeKeyBind = KeyBindDefinition.SHOW_IDENTIFICATION_RANGE.create(null, null);

    @RegisterKeyBind
    private final KeyBind identificationRerollKeyBind = KeyBindDefinition.SHOW_IDENTIFICATION_REROLL.create(null, null);

    @RegisterKeyBind
    private final KeyBind weightDistributionKeyBind = KeyBindDefinition.SHOW_WEIGHT_DISTRIBUTION.create(null, null);

    @RegisterKeyBind
    private final KeyBind weightContributionKeyBind = KeyBindDefinition.SHOW_WEIGHT_CONTRIBUTION.create(null, null);

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

        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;

        event.setTooltips(getUpdatedTooltip(event.getItemStack(), wynnItemOpt.get(), event.getTooltips()));
    }

    public List<Component> getUpdatedTooltip(ItemStack itemStack, WynnItem wynnItem, List<Component> tooltips) {
        if (brokenItems.contains(wynnItem)) return tooltips;

        try {
            return Handlers.Tooltip.updateWynnItemTooltip(tooltips, wynnItem, getTooltipOptions());
        } catch (Exception e) {
            brokenItems.add(wynnItem);

            String itemName = wynnItem.getClass().getSimpleName();
            Optional<NamedItemProperty> namedItemPropertyOpt =
                    Models.Item.asWynnItemProperty(itemStack, NamedItemProperty.class);
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

            return tooltips;
        }
    }

    public TooltipOptions getTooltipOptions() {
        return new TooltipOptions(
                new TooltipStyle(
                        identificationsOrdering.get(),
                        groupIdentifications.get(),
                        showBestValueLastAlways.get(),
                        rainbowInternalRoll.get(),
                        craftedPercentages.get(),
                        showRollWheel.get()),
                perfect.get(),
                defective.get(),
                identificationDecorations.get(),
                getIdentificationDisplay(),
                itemWeights.get(),
                getWeightDisplay(),
                overallPercentageInName.get(),
                overallPercentageInPerfectDefectiveName.get(),
                getColorMap(),
                colorLerp.get(),
                decimalPlaces.get());
    }

    private IdentificationDisplay getIdentificationDisplay() {
        if (!identificationDecorations.get()) return IdentificationDisplay.PERCENTAGE;

        boolean rangeDown = isKeyDown(identificationRangeKeyBind);
        boolean rerollDown = isKeyDown(identificationRerollKeyBind);

        if (rangeDown && rerollDown) return IdentificationDisplay.INTERNAL_ROLL;
        if (rerollDown) return IdentificationDisplay.REROLL;
        if (rangeDown) return IdentificationDisplay.RANGE;
        return IdentificationDisplay.PERCENTAGE;
    }

    private WeightDisplay getWeightDisplay() {
        if (!isKeyDown(weightDistributionKeyBind)) return WeightDisplay.OVERALL;

        return isKeyDown(weightContributionKeyBind) ? WeightDisplay.CONTRIBUTION : WeightDisplay.DISTRIBUTION;
    }

    private static boolean isKeyDown(KeyBind keyBind) {
        return !keyBind.getKeyMapping().isUnbound()
                && KeyboardUtils.isKeyDown(keyBind.getKeyMapping().key.getValue());
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
