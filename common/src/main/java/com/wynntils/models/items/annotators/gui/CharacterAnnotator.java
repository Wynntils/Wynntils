/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.character.type.CharacterGamemode;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.items.gui.CharacterItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class CharacterAnnotator implements GuiItemAnnotator {
    private static final String GAMEMODE_ICONS = "\uE027\uE083\uE026\uE029\uE028";

    // \uDB00\uDCC8 = space font 200. Biggest location I have found is Aldorei Cliffside Waterfalls which is
    // \uDB00\uDC54 = space font 84. Hopefully the range is big enough.
    private static final Pattern CHARACTER_MENU_NAME_PATTERN =
            Pattern.compile("[\uDB00\uDC0B-\uDB00\uDCC8]§6(§o)?(?<name>[A-Za-z0-9_ ]{1,20})");

    private static final Pattern CHARACTER_CREATION_NAME_PATTERN = Pattern.compile("§a§lConfirm and Create");
    private static final Pattern CHARACTER_CREATION_NICKNAME_PATTERN = Pattern.compile("§6- §7Nickname: §f(?<name>.+)");

    // Test in CharacterAnnotator_CHARACTER_MENU_CLASS_PATTERN
    private static final Pattern CHARACTER_MENU_CLASS_PATTERN =
            Pattern.compile("§6- §7Class:(?: (?<gamemodes>(?:(?:§.)?[" + GAMEMODE_ICONS + "])+)§7)? §f(?<class>.+)");

    private static final Pattern GAMEMODE_PATTERN = Pattern.compile("(?<color>§.)?(?<icon>[" + GAMEMODE_ICONS + "])");

    // Test in CharacterAnnotator_CHARACTER_MENU_LEVEL_PATTERN
    private static final Pattern CHARACTER_MENU_LEVEL_PATTERN =
            Pattern.compile("§6- §7Level: §f(?<level>\\d+)§7 §8\\(\\d+(?:\\.\\d+)?%\\)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        StyledText hoverName = StyledText.fromComponent(itemStack.getHoverName());

        Matcher menuNameMatcher = hoverName.getMatcher(CHARACTER_MENU_NAME_PATTERN);
        Matcher creationNameMatcher = hoverName.getMatcher(CHARACTER_CREATION_NAME_PATTERN);

        boolean fromCreation;
        String className = null;
        int level = 0;

        if (menuNameMatcher.matches()) {
            fromCreation = false;
            className = menuNameMatcher.group("name");
        } else if (creationNameMatcher.matches()) {
            fromCreation = true;
            level = 1;
        } else {
            return null;
        }

        ClassType classType = null;
        boolean reskinned = false;
        Set<CharacterGamemode> gamemodes = EnumSet.noneOf(CharacterGamemode.class);

        for (StyledText lore : LoreUtils.getLore(itemStack)) {
            Matcher classMatcher = lore.getMatcher(CHARACTER_MENU_CLASS_PATTERN);
            if (classMatcher.matches()) {
                classType = ClassType.fromName(classMatcher.group("class"));
                reskinned = ClassType.isReskinned(classMatcher.group("class"));
                gamemodes = parseGamemodes(classMatcher.group("gamemodes"));
            }

            if (fromCreation) {
                Matcher nicknameMatcher = lore.getMatcher(CHARACTER_CREATION_NICKNAME_PATTERN);
                if (nicknameMatcher.matches()) {
                    String nickName = nicknameMatcher.group("name");

                    if (Objects.equals(nickName, "Not Defined")) {
                        className = classType.getActualName(reskinned);
                    } else {
                        className = nickName;
                    }
                }
            } else {
                Matcher levelMatcher = lore.getMatcher(CHARACTER_MENU_LEVEL_PATTERN);
                if (levelMatcher.matches()) {
                    level = Integer.parseInt(levelMatcher.group("level"));
                }
            }
        }

        if (classType == null || classType == ClassType.NONE) return null;

        return new CharacterItem(className, level, classType, reskinned, gamemodes, fromCreation);
    }

    private static Set<CharacterGamemode> parseGamemodes(String gamemodeText) {
        if (gamemodeText == null || gamemodeText.isEmpty()) {
            return EnumSet.noneOf(CharacterGamemode.class);
        }

        Set<CharacterGamemode> gamemodes = EnumSet.noneOf(CharacterGamemode.class);
        Matcher matcher = GAMEMODE_PATTERN.matcher(gamemodeText);

        while (matcher.find()) {
            CharacterGamemode gamemode = CharacterGamemode.fromIcon(matcher.group("icon"));
            String color = matcher.group("color");

            if (gamemode == null) continue;
            if (gamemode == CharacterGamemode.HARDCORE && color == null) continue;

            gamemodes.add(gamemode);
        }

        return gamemodes;
    }
}