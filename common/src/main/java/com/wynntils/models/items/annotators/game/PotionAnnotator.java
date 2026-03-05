/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.elements.type.PotionType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.items.game.PotionItem;
import com.wynntils.models.wynnitem.type.ConsumableEffect;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
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
    private static final Pattern MIN_LEVEL_PATTERN =
            Pattern.compile("^§(c✖|a✔) ?§7 ?Combat Lv. Min: (?:§f)?(?<level>\\d+)(?:§r)?$");
    private static final Pattern EFFECT_LINE_PATTERN = Pattern.compile("^§(.)- §7(.*): §f([+-]?\\d+)(?:§.§.)? ?(.*)$");
    private static final Pattern EFFECT_HEADER_PATTERN = Pattern.compile("^§(.)Effect:$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(POTION_PATTERN);
        if (!matcher.matches()) return null;

        String potionType = matcher.group(1);

        int level = 0;
        List<NamedItemEffect> namedEffects = new ArrayList<>();
        List<ItemEffect> effects = new ArrayList<>();

        boolean parsingEffects = false;
        String effectsColorCode = "";

        List<StyledText> lore = LoreUtils.getLore(itemStack);

        for (StyledText line : lore) {
            Matcher minLevelMatcher = line.getMatcher(MIN_LEVEL_PATTERN);
            if (minLevelMatcher.matches()) {
                level = Integer.parseInt(minLevelMatcher.group("level"));
                continue;
            }

            Matcher effectHeaderMatcher = line.getMatcher(EFFECT_HEADER_PATTERN);
            if (effectHeaderMatcher.matches()) {
                effectsColorCode = effectHeaderMatcher.group(1);
                parsingEffects = true;
                continue;
            }
            if (parsingEffects) {
                Matcher effectMatcher = line.getMatcher(EFFECT_LINE_PATTERN);
                if (effectMatcher.matches()) {
                    String colorCode = effectMatcher.group(1);
                    String type = effectMatcher.group(2);
                    int value = Integer.parseInt(effectMatcher.group(3));
                    String suffix = effectMatcher.group(4);

                    // A sanity check; otherwise fall through
                    if (colorCode.equals(effectsColorCode)) {
                        // If type is "Heal", "Mana" or "Duration", keep it, otherwise
                        // replace it with the actual effect type
                        if (type.equals("Effect")) {
                            type = suffix;
                        }
                        ConsumableEffect consumableEffect = ConsumableEffect.fromString(type);
                        if (consumableEffect != null) {
                            namedEffects.add(new NamedItemEffect(consumableEffect, value));
                        } else {
                            effects.add(new ItemEffect(type, value));
                        }
                        continue;
                    }
                }

                parsingEffects = false;
            }
        }

        Matcher healingMatcher = HEALING_PATTERN.matcher(potionType);
        if (healingMatcher.matches()) {
            int uses = Integer.parseInt(healingMatcher.group(1));
            int maxUses = Integer.parseInt(healingMatcher.group(2));

            return new PotionItem(PotionType.HEALING, level, namedEffects, effects, new CappedValue(uses, maxUses));
        }

        Matcher manaMatcher = MANA_PATTERN.matcher(potionType);
        if (manaMatcher.matches()) {
            int uses = Integer.parseInt(manaMatcher.group(1));
            int maxUses = Integer.parseInt(manaMatcher.group(2));

            return new PotionItem(PotionType.MANA, level, namedEffects, effects, new CappedValue(uses, maxUses));
        }

        Matcher xpMatcher = XP_PATTERN.matcher(potionType);
        if (xpMatcher.matches()) {
            return new PotionItem(PotionType.XP, level, namedEffects, effects, new CappedValue(1, 1));
        }

        Matcher skillMatcher = SKILL_PATTERN.matcher(potionType);
        if (skillMatcher.matches()) {
            String skillName = skillMatcher.group(1);
            int uses = Integer.parseInt(skillMatcher.group(2));
            int maxUses = Integer.parseInt(skillMatcher.group(3));
            Skill skill = Skill.fromString(skillName);

            return new PotionItem(
                    PotionType.fromSkill(skill), level, namedEffects, effects, new CappedValue(uses, maxUses));
        }

        return null;
    }
}
