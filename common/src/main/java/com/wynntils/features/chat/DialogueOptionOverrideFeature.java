/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class DialogueOptionOverrideFeature extends Feature {
    @SubscribeEvent
    public void onDialogueKeyPress(KeyInputEvent e) {
        if (!Models.WorldState.onWorld() || e.getAction() != 1) return; // Only send packet on presses, not releases

        if (e.getKey() - 49 == McUtils.inventory().selected) { // keys 1-9 are +49 offset from hotbar
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(e.getKey() - 49));
        }
    }
}
