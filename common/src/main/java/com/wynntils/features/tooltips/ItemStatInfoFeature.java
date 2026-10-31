/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.WynntilsMod;
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
import com.wynntils.handlers.tooltip.type.TooltipOptions.RollStyle;
import com.wynntils.handlers.tooltip.type.TooltipOptions.WeightDisplay;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.TooltipUtils;
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
    private final Config<Boolean> perfect = new Config<>(true);

    @Persisted
    private final Config<Boolean> defective = new Config<>(true);

    @Persisted
    private final Config<StatListOrdering> identificationsOrdering = new Config<>(StatListOrdering.DEFAULT);

    @Persisted
    private final Config<Boolean> groupIdentifications = new Config<>(true);

    @Persisted
    private final Config<ItemWeightSource> itemWeights = new Config<>(ItemWeightSource.ALL);

    @Persisted
    private final Config<RollStyle> identifiedOverallRollStyle = new Config<>(RollStyle.PERCENTAGE);

    @Persisted
    private final Config<Boolean> overallRollInSpecialName = new Config<>(true);

    @Persisted
    private final Config<RollStyle> craftedOverallRollStyle = new Config<>(RollStyle.PERCENTAGE);

    @Persisted
    private final Config<Boolean> showBestValueLastAlways = new Config<>(true);

    @Persisted
    private final Config<Boolean> rainbowInternalRoll = new Config<>(true);

    @Persisted
    private final Config<RollStyle> identifiedRollStyle = new Config<>(RollStyle.PERCENTAGE);

    @Persisted
    private final Config<RollStyle> craftedRollStyle = new Config<>(RollStyle.PERCENTAGE);

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

    public static final NavigableMap<Float, TextColor> LERP_MAP = new TreeMap<>(Map.of(
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

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        if (KeyboardUtils.isKeyDown(InputConstants.KEY_RSHIFT)) return;

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

    public TooltipOptions getTooltipOptions() {
        return new TooltipOptions(
                identifiedOverallRollStyle.get(),
                craftedOverallRollStyle.get(),
                perfect.get(),
                defective.get(),
                overallRollInSpecialName.get(),
                itemWeights.get(),
                getWeightDisplay(),
                identificationsOrdering.get(),
                groupIdentifications.get(),
                getIdentificationDisplay(),
                showBestValueLastAlways.get(),
                rainbowInternalRoll.get(),
                identifiedRollStyle.get(),
                craftedRollStyle.get(),
                decimalPlaces.get(),
                getColorMap(),
                colorLerp.get());
    }

    private IdentificationDisplay getIdentificationDisplay() {
        boolean rangeDown = KeyboardUtils.isKeybindDown(identificationRangeKeyBind);
        boolean rerollDown = KeyboardUtils.isKeybindDown(identificationRerollKeyBind);

        if (rangeDown && rerollDown) {
            return IdentificationDisplay.INTERNAL_ROLL;
        } else if (rerollDown) {
            return IdentificationDisplay.REROLL_CHANCE;
        } else if (rangeDown) {
            return IdentificationDisplay.STAT_RANGES;
        }

        return IdentificationDisplay.PERCENTAGE;
    }

    private WeightDisplay getWeightDisplay() {
        if (!KeyboardUtils.isKeybindDown(weightDistributionKeyBind)) {
            return WeightDisplay.OVERALL;
        }

        return KeyboardUtils.isKeybindDown(weightContributionKeyBind)
                ? WeightDisplay.CONTRIBUTION
                : WeightDisplay.DISTRIBUTION;
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
