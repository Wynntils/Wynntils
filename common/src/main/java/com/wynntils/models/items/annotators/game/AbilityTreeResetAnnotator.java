package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.items.items.game.AbilityTreeNodeItem;
import com.wynntils.models.items.items.game.AbilityTreeResetItem;
import com.wynntils.utils.type.IterationDecision;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbilityTreeResetAnnotator implements GameItemAnnotator {
    private static final Pattern ABILITY_TREE_RESET_PATTERN = Pattern.compile("^(§8§lWaiting for Shards|§a§lConfirm Sacrifice)");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(ABILITY_TREE_RESET_PATTERN);

        if (matcher.find()) {
            String result = matcher.group(1);

            if (result.equals("§8§lWaiting for Shards")) {
                return new AbilityTreeResetItem(false);
            } else if (result.equals("§a§lConfirm Sacrifice")) {
                return new AbilityTreeResetItem(true);
            }
        }

        return null;
    }
}
