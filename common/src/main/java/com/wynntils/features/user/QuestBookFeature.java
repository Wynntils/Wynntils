/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.event.QuestBookReloadedEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.questbook.QuestBookModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class QuestBookFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyBind questBookKeyBind =
            new KeyBind("Rescan Quest Book", GLFW.GLFW_KEY_K, true, this::rescanQuestBook);

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(QuestBookModel.class);
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            rescanQuestBook();
        }
    }

    @SubscribeEvent
    public void onQuestBookReloadedEvent(QuestBookReloadedEvent e) {
        // FIXME: placeholder for doing something serious with the quests
    }

    private void rescanQuestBook() {
        WynntilsMod.info("Requesting rescan of Quest Book");
        QuestBookModel.queryQuestBook();
    }
}
