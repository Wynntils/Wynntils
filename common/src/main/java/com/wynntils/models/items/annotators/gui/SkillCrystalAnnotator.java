/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.gui;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.gui.SkillCrystalItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillCrystalAnnotator implements ItemAnnotator {
    private static final StyledText SKILL_CRYSTAL_NAME = StyledText.fromString("§2§lSkill Crystal");
    private static final Pattern SKILL_POINTS_PATTERN = Pattern.compile("^§7You have §a(\\d+)§7 skill points$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        if (!name.equals(SKILL_CRYSTAL_NAME)) return null;

        Matcher matcher = LoreUtils.matchLoreLine(itemStack, 1, SKILL_POINTS_PATTERN);
        if (!matcher.matches()) return null;

        int count = Integer.parseInt(matcher.group(1));
        return new SkillCrystalItem(count);
    }
}
