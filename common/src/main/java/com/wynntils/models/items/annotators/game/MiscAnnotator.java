/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.annotators.game;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemAnnotator;
import com.wynntils.models.items.items.game.MiscItem;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.type.StyledText;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class MiscAnnotator implements ItemAnnotator {
    private static final StyledText UNTRADABLE = StyledText.of("§cUntradable Item");
    private static final StyledText QUEST_ITEM = StyledText.of("§cQuest Item");

    @Override
    public ItemAnnotation getAnnotation(ItemStack itemStack, StyledText name) {
        ListTag loreTag = LoreUtils.getLoreTag(itemStack);
        if (loreTag == null) return null;

        boolean untradable = false;
        boolean questItem = false;

        for (Tag line : loreTag) {
            StyledText coded = ComponentUtils.getCoded(line.getAsString());
            if (coded.equals(UNTRADABLE)) {
                untradable = true;
            }
            if (coded.equals(QUEST_ITEM)) {
                questItem = true;
            }
        }

        // If it is neither Untradable nor a Quest Item, we can't be sure that it is not
        // e.g. a GUI element
        if (!untradable && !questItem) return null;

        return new MiscItem(name, untradable, questItem);
    }
}
