/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.gear.SetModel;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class GearAnnotator implements ItemAnnotator {
    private static final Pattern GEAR_PATTERN =
            Pattern.compile("^(?:§f⬡ )?(?<rarity>§[5abcdef])(?<unidentified>Unidentified )?(?:Shiny )?(?<name>.+)$");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
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

        return new GearItem(gearInfo, gearInstance);
    }

    /**
     * @return Wynncraft's count of specified set
     */
    private int getWynncraftCount(String setName) {
        for (ItemStack itemStack : McUtils.inventory().armor) {
            for (StyledText line : LoreUtils.getLore(itemStack)) {
                Matcher nameMatcher = SetModel.SET_PATTERN.matcher(line.getString());
                if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                    return Integer.parseInt(nameMatcher.group(2));
                }
            }
        }

        int[] accessorySlots = {9, 10, 11, 12};
        if (McUtils.player().hasContainerOpen()) {
            // Scale according to server chest size
            // Eg. 3 row chest size = 27 (ends on i=26 since 0-index), we would get accessory slots {27, 28, 29, 30}
            int baseSize = McUtils.player().containerMenu.getItems().size();
            accessorySlots = new int[] {baseSize, baseSize + 1, baseSize + 2, baseSize + 3};
        }
        for (int i : accessorySlots) {
            for (StyledText line : LoreUtils.getLore(McUtils.inventory().getItem(i))) {
                Matcher nameMatcher = SetModel.SET_PATTERN.matcher(line.getString());
                if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                    return Integer.parseInt(nameMatcher.group(2));
                }
            }
        }

        Optional<WynnItem> wynnItem =
                Models.Item.getWynnItem(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND));
        if (wynnItem.isPresent() && wynnItem.get() instanceof GearItem gearItem && gearItem.meetsActualRequirements()) {
            for (StyledText line : LoreUtils.getLore(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND))) {
                Matcher nameMatcher = SetModel.SET_PATTERN.matcher(line.getString());
                if (nameMatcher.matches() && nameMatcher.group(1).equals(setName)) {
                    return Integer.parseInt(nameMatcher.group(2));
                }
            }
        }

        return 0;
    }
}
