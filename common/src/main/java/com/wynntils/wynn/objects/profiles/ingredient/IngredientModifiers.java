/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class IngredientModifiers {
    int left = 0;
    int right = 0;
    int above = 0;
    int under = 0;
    int touching = 0;
    int notTouching = 0;

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getAbove() {
        return above;
    }

    public int getUnder() {
        return under;
    }

    public int getTouching() {
        return touching;
    }

    public int getNotTouching() {
        return notTouching;
    }

    public boolean anyExists() {
        return left != 0 || right != 0 || under != 0 || above != 0 || touching != 0 || notTouching != 0;
    }

    private static String[] getLoreLines(String modifierName, int modifierValue) {
        return new String[] {
            (modifierValue > 0 ? ChatFormatting.GREEN + "+" : ChatFormatting.RED.toString()) + modifierValue + "%"
                    + ChatFormatting.GRAY + " Ingredient Effectiveness",
            ChatFormatting.GRAY + "(To ingredients " + modifierName + " this one)"
        };
    }

    public List<MutableComponent> getModifierLoreLines() {
        List<String> itemLore = new ArrayList<>();

        if (this.left != 0) {
            itemLore.addAll(Arrays.asList(IngredientModifiers.getLoreLines("to the left of", left)));
        }
        if (this.right != 0) {
            itemLore.addAll(Arrays.asList(IngredientModifiers.getLoreLines("to the right of", right)));
        }
        if (this.above != 0) {
            itemLore.addAll(Arrays.asList(IngredientModifiers.getLoreLines("above", above)));
        }
        if (this.under != 0) {
            itemLore.addAll(Arrays.asList(IngredientModifiers.getLoreLines("under", under)));
        }
        if (this.touching != 0) {
            itemLore.addAll(Arrays.asList(IngredientModifiers.getLoreLines("touching", touching)));
        }
        if (this.notTouching != 0) {
            itemLore.addAll(Arrays.asList(IngredientModifiers.getLoreLines("not touching", notTouching)));
        }

        return itemLore.stream().map(Component::literal).toList();
    }

    @Override
    public String toString() {
        return "IngredientModifiers{" + "left="
                + left + ", right="
                + right + ", above="
                + above + ", under="
                + under + ", touching="
                + touching + ", notTouching="
                + notTouching + '}';
    }
}
