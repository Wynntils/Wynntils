/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.wynn.handleditems.items.gui.SkillPointItem;
import com.wynntils.wynn.objects.Skill;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillPointAnnotator implements ItemAnnotator {
    private static final Pattern SKILL_POINT_PATTERN = Pattern.compile("^§dUpgrade your §[2ebcf][✤✦❉✹❋] (.*)§d skill$");
    private static final Pattern POINT_PATTERN = Pattern.compile("^§7[ À]+(\\d+) points?[ À]+§r§6\\d+ points$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        String name = ComponentUtils.getCoded(itemStack.getHoverName());

        Matcher matcher = SKILL_POINT_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String skillName = matcher.group(1);
        Skill skill = Skill.fromString(skillName);

        int skillPoints = -1;
        LinkedList<String> a = ItemUtils.getLore(itemStack);
        for (String lore : ItemUtils.getLore(itemStack)) {
            Matcher m = POINT_PATTERN.matcher(lore);
            if (m.find()) {
                String points = m.group(1);
                skillPoints = Integer.parseInt(points);
                break;
            }
        }

        if (skillPoints == -1) return null;

        return new SkillPointItem(skill, skillPoints);
    }
}
