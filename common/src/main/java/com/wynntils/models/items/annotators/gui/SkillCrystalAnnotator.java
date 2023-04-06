/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText2;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.SkillCrystalItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillCrystalAnnotator implements ItemAnnotator {
    private static final StyledText2 SKILL_CRYSTAL_NAME = StyledText2.of("§2§lSkill Crystal");
    private static final Pattern SKILL_POINTS_PATTERN = Pattern.compile("^§7You have §r§a(\\d+)§r§7 skill points$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText2 name) {
        if (!name.equals(SKILL_CRYSTAL_NAME)) return null;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 1, SKILL_POINTS_PATTERN);
        if (!matcher.matches()) return null;

        int count = Integer.parseInt(matcher.group(1));
        return new SkillCrystalItem(count);
    }
}
