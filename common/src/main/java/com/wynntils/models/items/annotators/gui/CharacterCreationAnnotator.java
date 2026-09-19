/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GuiItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.character.type.CharacterGamemode;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.items.gui.CharacterCreationItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class CharacterCreationAnnotator implements GuiItemAnnotator {
    private static final String GAMEMODE_ICONS = "\uE027\uE083\uE026\uE029\uE028";

    private static final Pattern CHARACTER_CREATION_NAME_PATTERN = Pattern.compile("§a§lConfirm and Create");

    // Test in CharacterAnnotator_CHARACTER_MENU_CLASS_PATTERN
    private static final Pattern CHARACTER_MENU_CLASS_PATTERN =
            Pattern.compile("§6- §7Class:(?: (?<gamemodes>(?:(?:§.)?[" + GAMEMODE_ICONS + "])+)§7)? §f(?<class>.+)");

    private static final Pattern GAMEMODE_PATTERN = Pattern.compile("(?<color>§.)?(?<icon>[" + GAMEMODE_ICONS + "])");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher =
                StyledText.fromComponent(itemStack.getHoverName()).getMatcher(CHARACTER_CREATION_NAME_PATTERN);
        if (!matcher.matches()) return null;

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
        }

        if (classType == null || classType == ClassType.NONE) return null;

        return new CharacterCreationItem(classType, reskinned, gamemodes);
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
