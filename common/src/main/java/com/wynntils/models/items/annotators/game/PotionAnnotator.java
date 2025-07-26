/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.elements.type.PotionType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.items.game.PotionItem;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class PotionAnnotator implements GameItemAnnotator {
    private static final Pattern POTION_PATTERN = Pattern.compile("^§.Potion of (.*)$");
    private static final Pattern HEALING_PATTERN = Pattern.compile("^Healing§4 \\[(\\d+)/(\\d+)\\]$");
    private static final Pattern MANA_PATTERN = Pattern.compile("^Mana§3 \\[(\\d+)/(\\d+)\\]$");
    private static final Pattern XP_PATTERN = Pattern.compile("^Wisdom$");
    private static final Pattern SKILL_PATTERN =
            Pattern.compile("^§[2ebcf][\uE001\uE003\uE004\uE002\uE000] ([A-Za-z]*)(?:§2 | )\\[(\\d+)\\/(\\d+)\\]$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(POTION_PATTERN);
        if (!matcher.matches()) return null;

        String potionType = matcher.group(1);
        WynnItemParseResult parseResult = WynnItemParser.parseItemStack(itemStack, null);

        Matcher healingMatcher = HEALING_PATTERN.matcher(potionType);
        if (healingMatcher.matches()) {
            int uses = Integer.parseInt(healingMatcher.group(1));
            int maxUses = Integer.parseInt(healingMatcher.group(2));

            return new PotionItem(
                    PotionType.HEALING,
                    parseResult.level(),
                    parseResult.namedEffects(),
                    parseResult.effects(),
                    new CappedValue(uses, maxUses));
        }

        Matcher manaMatcher = MANA_PATTERN.matcher(potionType);
        if (manaMatcher.matches()) {
            int uses = Integer.parseInt(manaMatcher.group(1));
            int maxUses = Integer.parseInt(manaMatcher.group(2));

            return new PotionItem(
                    PotionType.MANA,
                    parseResult.level(),
                    parseResult.namedEffects(),
                    parseResult.effects(),
                    new CappedValue(uses, maxUses));
        }

        Matcher xpMatcher = XP_PATTERN.matcher(potionType);
        if (xpMatcher.matches()) {
            return new PotionItem(
                    PotionType.XP,
                    parseResult.level(),
                    parseResult.namedEffects(),
                    parseResult.effects(),
                    new CappedValue(1, 1));
        }

        Matcher skillMatcher = SKILL_PATTERN.matcher(potionType);
        if (skillMatcher.matches()) {
            String skillName = skillMatcher.group(1);
            int uses = Integer.parseInt(skillMatcher.group(2));
            int maxUses = Integer.parseInt(skillMatcher.group(3));
            Skill skill = Skill.fromString(skillName);

            return new PotionItem(
                    PotionType.fromSkill(skill),
                    parseResult.level(),
                    parseResult.namedEffects(),
                    parseResult.effects(),
                    new CappedValue(uses, maxUses));
        }

        return null;
    }
}
