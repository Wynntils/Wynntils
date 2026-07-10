/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IngredientPouchAnnotator implements GuiItemAnnotator {
    private static final Pattern INGREDIENT_POUCH_PATTERN = Pattern.compile("§6[a-zA-Z0-9]+(?:'s)? Pouch");
    public static final Pattern INGREDIENT_LORE_LINE_PATTERN = Pattern.compile("^§7(\\d+) x §#20aa20ff(.+)$");
    private static final Pattern OTHER_ITEMS_LORE_LINE_PATTERN = Pattern.compile("^§7(\\d+) x §(?:#.{8}|.)(?:.+)$");
    private static final Pattern COUNT_PATTERN = Pattern.compile("\\d+ x ");
    private static final Pattern SELL_RANGE_PATTERN =
            Pattern.compile("^§fShift Right-Click§7 to sell \\(§a(?<min>\\d+)§7-§a(?<max>\\d+)§2²§7\\)$");
    private static final StyledText ULTIMATE_IRONMAN_LINE = StyledText.fromString("§7As an §bUltimate Ironman§7, you");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (itemStack.getItem() != Items.POTION) return null;
        if (!name.matches(INGREDIENT_POUCH_PATTERN)) return null;

        List<Pair<IngredientInfo, Integer>> ingredients = new ArrayList<>();
        List<Pair<StyledText, Integer>> otherItems = new ArrayList<>();
        RangedValue sellRange = RangedValue.NONE;
        boolean ultimateIronman = false;

        List<StyledText> lore = LoreUtils.getLore(itemStack);
        for (StyledText line : lore) {
            Matcher matcher = line.getMatcher(INGREDIENT_LORE_LINE_PATTERN);
            if (!matcher.matches()) {
                matcher = line.getMatcher(OTHER_ITEMS_LORE_LINE_PATTERN);

                if (matcher.matches()) {
                    int count = Integer.parseInt(matcher.group(1));

                    StyledText itemName = line.iterate((part, changes) -> {
                        if (COUNT_PATTERN
                                .matcher(part.getString(null, StyleType.NONE))
                                .matches()) {
                            changes.remove(part);
                            return IterationDecision.BREAK;
                        }

                        return IterationDecision.CONTINUE;
                    });

                    otherItems.add(Pair.of(itemName, count));
                } else if (line.equals(ULTIMATE_IRONMAN_LINE)) {
                    ultimateIronman = true;
                } else if (sellRange.equals(RangedValue.NONE)) {
                    matcher = line.getMatcher(SELL_RANGE_PATTERN);

                    if (matcher.matches()) {
                        int min = Integer.parseInt(matcher.group("min"));
                        int max = Integer.parseInt(matcher.group("max"));

                        sellRange = new RangedValue(min, max);
                    }
                }

                continue;
            }

            int count = Integer.parseInt(matcher.group(1));
            String ingredientName = matcher.group(2);

            IngredientInfo ingredientInfo = Models.Ingredient.getIngredientInfoFromName(ingredientName);

            if (ingredientInfo == null) {
                ingredientInfo = Models.Ingredient.getIngredientInfoFromApiName(ingredientName);
                // Skip unknown ingredients; the pouch list will be wrong but better than nothing
                if (ingredientInfo == null) continue;
            }

            ingredients.add(Pair.of(ingredientInfo, count));
        }

        return new IngredientPouchItem(ingredients, otherItems, sellRange, ultimateIronman);
    }
}
