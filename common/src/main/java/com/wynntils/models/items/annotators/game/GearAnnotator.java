/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.models.items.items.game.GearItem;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator extends GameItemAnnotator {
    private static final Pattern GEAR_PATTERN =
            Pattern.compile("^(?:§f⬡ )?(?<rarity>§[5abcdef])(?<unidentified>Unidentified )?(?:Shiny )?(?<name>.+)$");

    @Override
    public GameItem getAnnotation(ItemStack itemStack, StyledText name, int emeraldPrice) {
        Matcher matcher = name.getMatcher(GEAR_PATTERN);
        if (!matcher.matches()) return null;

        // Lookup Gear Profile
        String itemName = matcher.group("name");
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(itemName);
        if (gearInfo == null) return null;

        // Verify that rarity matches
        if (!matcher.group("rarity").equals(gearInfo.tier().getChatFormatting().toString())) return null;

        GearInstance gearInstance =
                matcher.group("unidentified") != null ? null : Models.Gear.parseInstance(gearInfo, itemStack);
        return new GearItem(emeraldPrice, gearInfo, gearInstance);
    }
}
