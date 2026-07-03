package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.items.game.AbilityShardItem;
import com.wynntils.utils.mc.LoreUtils;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbilityShardAnnotator implements GameItemAnnotator {
    private static final Pattern ABILITY_SHARD_PATTERN = Pattern.compile(
            "^§#82eff4ffAbility Shard");
    private static final Pattern QUEST_REQUIREMENT_PATTERN = Pattern.compile(
            "§.([✖✔]) §7Quest Req: §f([^§]+)§7 \\[Lv\\. (\\d+)\\]");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(ABILITY_SHARD_PATTERN);
        if (!matcher.matches()) return null;

        Matcher questRequirementMatcher = LoreUtils.matchLoreLine(itemStack, 3, QUEST_REQUIREMENT_PATTERN);
        if (!questRequirementMatcher.matches()) return null;

        boolean isCompleted = questRequirementMatcher.group(1).equals("✔");

        return new AbilityShardItem(name, isCompleted);
    }

}
