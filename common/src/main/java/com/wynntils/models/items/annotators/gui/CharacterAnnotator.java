/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.items.gui.CharacterItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class CharacterAnnotator implements GuiItemAnnotator {
    private static final Pattern CHARACTER_MENU_NAME_PATTERN =
            Pattern.compile("[\uDB00\uDC0B-\uDB00\uDC46]§6(§o)?(?<name>[A-Za-z0-9_ ]{1,20})");

    // Test in CharacterAnnotator_CHARACTER_MENU_CLASS_PATTERN
    private static final Pattern CHARACTER_MENU_CLASS_PATTERN =
            Pattern.compile("§6- §7Class:(?: (?<gamemodes>§.[\uE027\uE083\uE026\uE029\uE028])+§7)? §f(?<class>.+)");

    // Test in CharacterAnnotator_CHARACTER_MENU_LEVEL_PATTERN
    private static final Pattern CHARACTER_MENU_LEVEL_PATTERN =
            Pattern.compile("§6- §7Level: §f(?<level>\\d+)§7 §8\\(\\d+(?:\\.\\d+)?%\\)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = StyledText.fromComponent(itemStack.getHoverName()).getMatcher(CHARACTER_MENU_NAME_PATTERN);
        if (!matcher.matches()) return null;

        String className = matcher.group("name");
        int level = 0;
        ClassType classType = null;
        boolean reskinned = false;

        for (StyledText lore : LoreUtils.getLore(itemStack)) {
            Matcher classMatcher = lore.getMatcher(CHARACTER_MENU_CLASS_PATTERN);
            if (classMatcher.matches()) {
                classType = ClassType.fromName(classMatcher.group("class"));
                reskinned = ClassType.isReskinned(classMatcher.group("class"));
            }

            Matcher levelMatcher = lore.getMatcher(CHARACTER_MENU_LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
            }
        }

        if (classType == null || classType == ClassType.NONE) return null;

        return new CharacterItem(className, level, classType, reskinned);
    }
}
