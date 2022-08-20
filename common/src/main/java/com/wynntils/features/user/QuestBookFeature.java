/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ScriptedContainerQuery;
import com.wynntils.wynn.utils.InventoryUtils;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class QuestBookFeature extends UserFeature {
    private static final String QUESTS_TITLE = "§0[Pg. 1] §8mag_icus'§0 Quests";
    private static final String QUESTS_TITLE_2 = "§0[Pg. 2] §8mag_icus'§0 Quests";
    private static final String QUESTS_TITLE_3 = "§0[Pg. 3] §8mag_icus'§0 Quests";
    private static final int NEXT_PAGE_SLOT = 8;

    @RegisterKeyBind
    private final KeyBind questBookKeyBind =
            new KeyBind("Read Quest Book", GLFW.GLFW_KEY_F, true, this::onScanQuestBook2);

    private void onScanQuestBook2() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder()
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .expectTitle(QUESTS_TITLE)
                .processContainer(c -> {
                    System.out.println("GOT PAGE 1:" + c.title().getString() + ": " + c.items());
                    if (c.title().getString().equals(QUESTS_TITLE)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                })
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, "[§f§lPage 2§a >§2>§a>§2>§a>]")
                .expectTitle(QUESTS_TITLE_2)
                .processContainer(c -> {
                    System.out.println("GOT PAGE 2:" + c.title().getString() + ": " + c.items());
                    if (c.title().getString().equals(QUESTS_TITLE_2)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                })
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, "[§f§lPage 3§a >§2>§a>§2>§a>]")
                .expectTitle(QUESTS_TITLE_3)
                .processContainer(c -> {
                    System.out.println("GOT PAGE 3:" + c.title().getString() + ": " + c.items());
                    if (c.title().getString().equals(QUESTS_TITLE_3)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                })
                .onError(msg -> System.out.println("error:" + msg))
                .build();

        query.executeQuery();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            // trigger reading at login
            // FIXME
        }
    }
}
