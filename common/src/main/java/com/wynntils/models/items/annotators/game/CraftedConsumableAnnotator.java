/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.wynnitem.parsing.WynnItemParseResult;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class CraftedConsumableAnnotator implements GameItemAnnotator {
    private static final Pattern CRAFTED_CONSUMABLE_NAME_PATTERN =
            Pattern.compile("^\uDAFC\uDC00§3(?<name>.+?) §8\\[(?<currentUses>\\d+)\\/(?<maxUses>\\d+)\\]\uDAFC\uDC00$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        Matcher matcher = name.getMatcher(CRAFTED_CONSUMABLE_NAME_PATTERN);
        if (!matcher.matches()) return null;

        WynnItemParseResult parseResult = WynnItemParser.parseItemStack(itemStack);

        if (parseResult == null) return null;

        return new CraftedConsumableItem(
                matcher.group("name"),
                ConsumableType.fromFrameSprite(parseResult.itemType()),
                parseResult.level(),
                parseResult.identifications(),
                parseResult.namedEffects(),
                parseResult.effects(),
                new CappedValue(
                        Integer.parseInt(matcher.group("currentUses")), Integer.parseInt(matcher.group("maxUses"))));
    }
}
