/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.NpcDialogEvent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NpcDialogAutoProgressFeature extends UserFeature {
    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ChatModel.class);
        ChatModel.addNpcDialogExtractionDependent(this);
    }

    @SubscribeEvent
    public void onNpcDialog(NpcDialogEvent event) {
        System.out.println("event = " + event);
        McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                McUtils.player(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        // McUtils.sendPacket(new ServerboundPlayerCommandPacket(McUtils.player(),
        // ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }
}
