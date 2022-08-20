/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.model.container.ContainerContent;
import com.wynntils.wynn.model.container.ScriptedContainerQuery;
import com.wynntils.wynn.utils.InventoryUtils;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class QuestBookFeature extends UserFeature {
    private static final int NEXT_PAGE_SLOT = 8;

    @RegisterKeyBind
    private final KeyBind questBookKeyBind =
            new KeyBind("Rescan Quest Book", GLFW.GLFW_KEY_UNKNOWN, true, this::queryQuestBook);

    private void processQuestBookPage(ContainerContent container, int page) {
        System.out.println("GOT PAGE " + page + ":" + container.title().getString() + ": " + container.items());
    }

    private String getNextPageButtonName(int nextPageNum) {
        return "[§f§lPage " + nextPageNum + "§a >§2>§a>§2>§a>]";
    }

    private String getQuestBookTitle(int pageNum) {
        return "§0[Pg. " + pageNum + "] §8mag_icus'§0 Quests";
    }

    private void queryQuestBook() {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Quest Book Query")
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .expectTitle(getQuestBookTitle(1))
                .processContainer(c -> processQuestBookPage(c, 1))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(2))
                .expectTitle(getQuestBookTitle(2))
                .processContainer(c -> processQuestBookPage(c, 2))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(3))
                .expectTitle(getQuestBookTitle(3))
                .processContainer(c -> processQuestBookPage(c, 3))
                .clickOnSlotMatching(NEXT_PAGE_SLOT, Items.GOLDEN_SHOVEL, getNextPageButtonName(4))
                .expectTitle(getQuestBookTitle(4))
                .processContainer(c -> processQuestBookPage(c, 4))
                .onError(msg -> WynntilsMod.warn("Error querying Quest Book:" + msg))
                .build();

        query.executeQuery();
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            WynntilsMod.info("Scheduling quest book query");
            queryQuestBook();
        }
    }
}
