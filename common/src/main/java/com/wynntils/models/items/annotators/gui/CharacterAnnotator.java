/*
 * Copyright © Wynntils 2024.
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
    // (?:§l)? is trying to account for the fact that seemingly Wynn is trying to bold the text,
    // but Wynn adds a color after the bold, resetting the bold effect.
    private static final Pattern CLASS_MENU_NAME_PATTERN =
            Pattern.compile("(?:§l)?§6(?:§l)?\\[>\\] Select ((.+)|This Character)");

    // Test in CharacterAnnotator_CLASS_MENU_CLASS_PATTERN
    private static final Pattern CLASS_MENU_CLASS_PATTERN =
            Pattern.compile("§e- §7Class:(?: (?<gamemodes>§.[\uE027\uE083\uE026\uE029\uE028])+§r)? §f(?<class>.+)");
    private static final Pattern CLASS_MENU_LEVEL_PATTERN = Pattern.compile("§e- §7Level: §f(\\d+)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = StyledText.fromComponent(itemStack.getHoverName()).getMatcher(CLASS_MENU_NAME_PATTERN);
        if (!matcher.matches()) return null;

        String className = matcher.group(1);
        int level = 0;
        ClassType classType = null;
        boolean reskinned = false;

        for (StyledText lore : LoreUtils.getLore(itemStack)) {
            Matcher classMatcher = lore.getMatcher(CLASS_MENU_CLASS_PATTERN);
            if (classMatcher.matches()) {
                classType = ClassType.fromName(classMatcher.group("class"));
                reskinned = ClassType.isReskinned(classMatcher.group("class"));
            }

            Matcher levelMatcher = lore.getMatcher(CLASS_MENU_LEVEL_PATTERN);
            if (levelMatcher.matches()) {
                level = Integer.parseInt(levelMatcher.group(1));
            }
        }

        if (classType == null || classType == ClassType.NONE) return null;

        return new CharacterItem(className, level, classType, reskinned);
    }
}
