/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.items.items.game.SkillPotionItem;
import com.wynntils.utils.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillPotionAnnotator implements ItemAnnotator {
    private static final Pattern SKILL_POTION_PATTERN =
            Pattern.compile("^§aPotion of §[2ebcf][✤✦❉✹❋] (.*)§a \\[(\\d+)/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, String name) {
        Matcher matcher = SKILL_POTION_PATTERN.matcher(name);
        if (!matcher.matches()) return null;

        String skillName = matcher.group(1);
        int uses = Integer.parseInt(matcher.group(2));
        int maxUses = Integer.parseInt(matcher.group(3));
        Skill skill = Skill.fromString(skillName);

        return new SkillPotionItem(skill, new CappedValue(uses, maxUses));
    }
}
