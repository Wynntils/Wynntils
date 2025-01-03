/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.OuterVoidItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class OuterVoidItemAnnotator implements GameItemAnnotator {
    private static final Pattern OUTER_VOID_TAG = Pattern.compile(
            "§#cc66bbff\uE060\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE037\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE044\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE045\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE033\uDAFF\uDFFF\uE062\uDAFF\uDFB0§f\uE013\uE007\uE004 \uE00E\uE014\uE013\uE004\uE011 \uE015\uE00E\uE008\uE003\uDB00\uDC02");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        if (lore.isEmpty()) return null;

        if (lore.getFirst().matches(OUTER_VOID_TAG)) {
            GearTier gearTier = GearTier.fromStyledText(name);

            return new OuterVoidItem(gearTier);
        }

        return null;
    }
}
