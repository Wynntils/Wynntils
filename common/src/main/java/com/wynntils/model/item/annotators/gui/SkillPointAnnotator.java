/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.annotators.gui;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.model.item.gui.SkillPointItem;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class SkillPointAnnotator implements ItemAnnotator {
    private static final Pattern POINT_PATTERN = Pattern.compile("^§7[ À]+(\\d+) points[ À]+§r§6\\d+ points$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack) {
        Matcher matcher = WynnItemMatchers.skillPointNameMatcher(itemStack.getHoverName());
        if (!matcher.matches()) return null;

        int skillPoints = -1;
        for (String lore : ItemUtils.getLore(itemStack)) {
            Matcher m = POINT_PATTERN.matcher(lore);
            if (m.find()) {
                String points = m.group(1);
                skillPoints = Integer.parseInt(points);
                break;
            }
        }
        if (skillPoints == -1) return null;

        return new SkillPointItem(skillPoints);
    }
}
