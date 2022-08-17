/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.ContainerQueryManager;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.WorldStateManager;
import com.wynntils.wynn.utils.InventoryUtils;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class QuestBookFeature extends UserFeature {
    private static final String QUESTS_TITLE = "§0[Pg. 1] §8mag_icus'§0 Quests";
    private static final String QUESTS_TITLE_2 = "§0[Pg. 2] §8mag_icus'§0 Quests";
    private static final String QUESTS_TITLE_3 = "§0[Pg. 3] §8mag_icus'§0 Quests";

    @RegisterKeyBind
    private final KeyBind questBookKeyBind =
            new KeyBind("Read Quest Book", GLFW.GLFW_KEY_F, true, this::onScanQuestBook);

    private void onScanQuestBook() {
        ContainerQueryManager.start((ignored, title, menuType) -> {
            ContainerQueryManager.openInventory(InventoryUtils.QUEST_BOOK_SLOT_NUM);
            ContainerQueryManager.nextAction = (itemsPg1, titlePg1, menuTypePg1) -> {
                System.out.println("GOT PAGE 1:" + titlePg1.getString() + ": " + itemsPg1);
                if (titlePg1.getString().equals(QUESTS_TITLE)) {
                    System.out.println("title ok");
                } else {
                    System.out.println("title not ok");
                }
                if (isPage2Button(itemsPg1)) {
                    ContainerQueryManager.clickOnSlot(itemsPg1, 8);
                }
                ContainerQueryManager.nextAction = (itemsPg2, titlePg2, menuTypePg2) -> {
                    System.out.println("GOT PAGE 2:" + titlePg2.getString() + ": " + itemsPg2);
                    if (titlePg2.getString().equals(QUESTS_TITLE_2)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                    if (isPage3Button(itemsPg2)) {
                        ContainerQueryManager.clickOnSlot(itemsPg2, 8);
                    }
                    ContainerQueryManager.nextAction = (itemsPg3, titlePg3, menuTypePg3) -> {
                        System.out.println("GOT PAGE 3:" + titlePg3.getString() + ": " + itemsPg3);
                        if (titlePg3.getString().equals(QUESTS_TITLE_3)) {
                            System.out.println("title ok");
                        } else {
                            System.out.println("title not ok");
                        }
                        return null;
                    };

                    return QUESTS_TITLE_3;
                };
                return QUESTS_TITLE_2;
            };
            return QUESTS_TITLE;
        });
    }

    private void onScanQuestBook2() {
        ContainerQueryManager.ContainerQuery query = ContainerQueryManager.ContainerQueryBuilder.start()
                .expectTitle(QUESTS_TITLE)
                .useItemInHotbar(InventoryUtils.QUEST_BOOK_SLOT_NUM)
                .processContainer((itemsPg1, titlePg1, menuTypePg1) -> {
                    System.out.println("GOT PAGE 1:" + titlePg1.getString() + ": " + itemsPg1);
                    if (titlePg1.getString().equals(QUESTS_TITLE)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                })
                .expectTitle(QUESTS_TITLE_2)
                .clickOnSlot(8)
                .processContainer((itemsPg2, titlePg2, menuTypePg2) -> {
                    System.out.println("GOT PAGE 2:" + titlePg2.getString() + ": " + itemsPg2);
                    if (titlePg2.getString().equals(QUESTS_TITLE_2)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                })
                .expectTitle(QUESTS_TITLE_3)
                .clickOnSlot(8)
                .processContainer((itemsPg3, titlePg3, menuTypePg3) -> {
                    System.out.println("GOT PAGE 3:" + titlePg3.getString() + ": " + itemsPg3);
                    if (titlePg3.getString().equals(QUESTS_TITLE_3)) {
                        System.out.println("title ok");
                    } else {
                        System.out.println("title not ok");
                    }
                })
                .build();

        query.executeQuery();
    }

    private boolean isPage2Button(List<ItemStack> items) {
        ItemStack nextPage = items.get(8);
        if (nextPage.is(Items.GOLDEN_SHOVEL)) {
            String dispName = nextPage.getDisplayName().getString();
            if (dispName.equals("[§f§lPage 2§a >§2>§a>§2>§a>]")) {
                return true;
            }
        }
        return false;
    }

    private boolean isPage3Button(List<ItemStack> items) {
        ItemStack nextPage = items.get(8);
        if (nextPage.is(Items.GOLDEN_SHOVEL)) {
            String dispName = nextPage.getDisplayName().getString();
            if (dispName.equals("[§f§lPage 3§a >§2>§a>§2>§a>]")) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            // trigger reading at login
            // FIXME
        }
    }
}
