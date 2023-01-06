/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.handleditems.items.gui.SkillPointItem;
import com.wynntils.wynn.objects.Skill;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillPointAnnotator implements ItemAnnotator {
    private static final Pattern SKILL_POINT_PATTERN = Pattern.compile("^§dUpgrade your §[2ebcf][✤✦❉✹❋] (.*)§d skill$");
    private static final Pattern LORE_PATTERN = Pattern.compile("^§7 +(-?\\d+) points? +§r§6-?\\d+ points?$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = SKILL_POINT_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String skillName = matcher.group(1);
        Skill skill = Skill.fromString(skillName);

        String loreLine = WynnUtils.normalizeBadString(ItemUtils.getLoreLine(itemStack, 3));
        Matcher m = LORE_PATTERN.matcher(loreLine);
        if (!m.matches()) return null;

        int skillPoints = Integer.parseInt(m.group(1));

        return new SkillPointItem(skill, skillPoints);
    }
}
