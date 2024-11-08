/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.models.containers.containers.CharacterSelectionContainer;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class CharacterSelectionUtilitiesFeature extends Feature {
    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent e) {
        if (!(Models.Container.getCurrentContainer() instanceof CharacterSelectionContainer)) return;

        KeyMapping[] keyHotbarSlots = McUtils.options().keyHotbarSlots;
        List<Integer> validSlots = Models.CharacterSelection.getValidCharacterSlots();

        for (int i = 0; i < Math.min(keyHotbarSlots.length, validSlots.size()); i++) {
            if (!keyHotbarSlots[i].matches(e.getKeyCode(), e.getScanCode())) continue;

            int slot = validSlots.get(i);
            Models.CharacterSelection.playWithCharacter(slot);
            break;
        }
    }
}
