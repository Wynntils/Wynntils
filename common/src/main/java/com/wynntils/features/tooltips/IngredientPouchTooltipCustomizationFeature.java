/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.wynnfonts.BannerSymbolFont;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.TOOLTIPS)
public class IngredientPouchTooltipCustomizationFeature extends Feature {
    private static final Integer DEFAULT_INGREDIENT_COLOR = 0x20aa20;

    @Persisted
    private final Config<Boolean> mergeItems = new Config<>(true);

    @Persisted
    private final Config<IngredientStyle> ingredientStyle = new Config<>(IngredientStyle.COLORED_WITH_STARS);

    @Persisted
    private final Config<SortOption> primarySort = new Config<>(SortOption.RARITY);

    @Persisted
    private final Config<SortOption> secondarySort = new Config<>(SortOption.LEVEL);

    @Persisted
    private final Config<Boolean> invertSort = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideNonIngredients = new Config<>(false);

    public IngredientPouchTooltipCustomizationFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<IngredientPouchItem> ingredientPouchItemOpt =
                Models.Item.asWynnItem(event.getItemStack(), IngredientPouchItem.class);

        if (ingredientPouchItemOpt.isEmpty()) return;

        IngredientPouchItem ingredientPouchItem = ingredientPouchItemOpt.get();

        if (ingredientPouchItem.getCount() == 0) return;

        event.setTooltips(getCustomizedTooltip(ingredientPouchItem));
    }

    private List<Component> getCustomizedTooltip(IngredientPouchItem ingredientPouchItem) {
        List<Component> newTooltip = new ArrayList<>();

        newTooltip.add(Component.literal("Ingredient Pouch").withStyle(ChatFormatting.GOLD));
        newTooltip.add(Component.empty());

        if (ingredientPouchItem.isUltIronman()) {
            newTooltip.add(Component.literal("As an ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Ultimate Ironman")
                            .withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(", you").withStyle(ChatFormatting.GRAY))));
            newTooltip.add(Component.literal("may place any item here.").withStyle(ChatFormatting.GRAY));
            newTooltip.add(Component.empty());
        }

        newTooltip.add(Component.literal("Left Click")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" to view contents").withStyle(ChatFormatting.GRAY)));
        MutableComponent sellLine = Component.literal("Shift Right-Click")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" to sell (").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(
                                ingredientPouchItem.getSellRange().low()))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("-").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(
                                ingredientPouchItem.getSellRange().high()))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(EmeraldUnits.EMERALD.getSymbol()).withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY));
        newTooltip.add(sellLine);
        newTooltip.add(Component.empty());

        List<Pair<IngredientInfo, Integer>> ingredients = ingredientPouchItem.getIngredients();
        List<Pair<StyledText, Integer>> otherItems = ingredientPouchItem.getOtherItems();

        if (mergeItems.get()) {
            Map<IngredientInfo, Integer> mergedIngredients = new LinkedHashMap<>();
            for (Pair<IngredientInfo, Integer> pair : ingredients) {
                mergedIngredients.merge(pair.a(), pair.b(), Integer::sum);
            }

            ingredients = mergedIngredients.entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toCollection(ArrayList::new));

            Map<StyledText, Integer> mergedOtherItems = new LinkedHashMap<>();
            for (Pair<StyledText, Integer> pair : otherItems) {
                mergedOtherItems.merge(pair.a(), pair.b(), Integer::sum);
            }

            otherItems = mergedOtherItems.entrySet().stream()
                    .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        sortIngredients(ingredients);

        for (Pair<IngredientInfo, Integer> item : ingredients) {
            newTooltip.add(buildIngredientLine(item));
        }

        if (!hideNonIngredients.get()) {
            for (Pair<StyledText, Integer> item : otherItems) {
                newTooltip.add(Component.literal(item.b() + " x ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(item.a().getComponent()));
            }
        }

        return newTooltip;
    }

    private void sortIngredients(List<Pair<IngredientInfo, Integer>> ingredients) {
        SortOption tertiary = getRemainingSortOption(primarySort.get(), secondarySort.get());

        Comparator<Pair<IngredientInfo, Integer>> comparator = comparatorFor(primarySort.get())
                .thenComparing(comparatorFor(secondarySort.get()))
                .thenComparing(comparatorFor(tertiary));

        if (!mergeItems.get()) {
            comparator = comparator.thenComparing(
                    Comparator.comparingInt(Pair<IngredientInfo, Integer>::b).reversed());
        }

        ingredients.sort(comparator);
    }

    private Comparator<Pair<IngredientInfo, Integer>> comparatorFor(SortOption option) {
        Comparator<Pair<IngredientInfo, Integer>> comparator =
                switch (option) {
                    case RARITY ->
                        Comparator.comparingInt((Pair<IngredientInfo, Integer> pair) ->
                                        pair.a().tier())
                                .reversed();
                    case LEVEL ->
                        Comparator.comparingInt((Pair<IngredientInfo, Integer> pair) ->
                                        pair.a().level())
                                .reversed();
                    case NAME -> Comparator.comparing(pair -> pair.a().name());
                };

        return invertSort.get() ? comparator.reversed() : comparator;
    }

    private SortOption getRemainingSortOption(SortOption first, SortOption second) {
        for (SortOption option : SortOption.values()) {
            if (option != first && option != second) {
                return option;
            }
        }

        WynntilsMod.error("Remaining sort option not found, defaulting to name");
        return SortOption.NAME;
    }

    private Component buildIngredientLine(Pair<IngredientInfo, Integer> item) {
        MutableComponent component = Component.literal(item.b() + " x ").withStyle(ChatFormatting.GRAY);

        switch (ingredientStyle.get()) {
            case VANILLA -> {
                component.append(getColoredIngredientName(true, item.a()));
            }
            case COLORED -> {
                component.append(getColoredIngredientName(false, item.a()));
            }
            case WITH_STARS -> {
                component.append(getColoredIngredientName(true, item.a()));
                component.append(getStarsComponent(item.a()));
            }
            case COLORED_WITH_STARS -> {
                component.append(getColoredIngredientName(false, item.a()));
                component.append(getStarsComponent(item.a()));
            }
        }

        return component;
    }

    private Component getColoredIngredientName(boolean defaultColor, IngredientInfo ingredient) {
        if (defaultColor) {
            return Component.literal(ingredient.name()).withColor(DEFAULT_INGREDIENT_COLOR);
        }

        return Component.literal(ingredient.name()).withStyle(getColorForTier(ingredient.tier()));
    }

    private Component getStarsComponent(IngredientInfo ingredient) {
        return Component.literal(" ")
                .append(BannerSymbolFont.buildMessage(
                        3,
                        ingredient.tier(),
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(getColorForTier(ingredient.tier())),
                        CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                        ""));
    }

    private ChatFormatting getColorForTier(Integer tier) {
        return switch (tier) {
            case 3 -> ChatFormatting.AQUA;
            case 2 -> ChatFormatting.LIGHT_PURPLE;
            case 1 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.DARK_GRAY;
        };
    }

    private enum IngredientStyle {
        VANILLA,
        COLORED,
        WITH_STARS,
        COLORED_WITH_STARS
    }

    private enum SortOption {
        RARITY,
        LEVEL,
        NAME
    }
}
