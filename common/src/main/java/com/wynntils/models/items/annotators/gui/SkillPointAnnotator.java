/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.items.gui.SkillPointItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillPointAnnotator implements ItemAnnotator {
    // Test suite: https://regexr.com/7h0tl
    private static final Pattern SKILL_POINT_PATTERN = Pattern.compile("^§dUpgrade your §[2ebcf][✤✦❉✹❋] (.*)§d skill$");
    // Test suite: https://regexr.com/7h0to
    private static final Pattern LORE_PATTERN = Pattern.compile("^[ À]+§7(-?\\d+) points?§r[ À]+§6-?\\d+ points?$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(SKILL_POINT_PATTERN);
        if (!matcher.matches()) return null;

        String skillName = matcher.group(1);
        Skill skill = Skill.fromString(skillName);

        Matcher m = LoreUtils.matchLoreLine(itemStack, 3, LORE_PATTERN);
        if (!m.matches()) return null;

        int skillPoints = Integer.parseInt(m.group(1));

        return new SkillPointItem(skill, skillPoints);
    }
}
