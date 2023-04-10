/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.abilities.type.AbilityTreeDump;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AbilityTreeContainerQueries {
    private static final int ABILITY_TREE_PAGES = 7;
    private static final int ABILITY_TREE_SLOT = 9;
    private static final int NEXT_PAGE_SLOT = 59;
    private static final int DUMMY_SLOT = 89;
    private static final StyledText NEXT_PAGE_ITEM_NAME = StyledText.fromString("§7Next Page");

    public void dumpAbilityTreeData() {
        AbilityTreeDump dump = new AbilityTreeDump();

        ScriptedContainerQuery.QueryBuilder queryBuilder = ScriptedContainerQuery.builder("Ability Tree Dump Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Ability Tree: " + msg);
                    McUtils.sendMessageToClient(
                            Component.literal("Error dumping ability tree.").withStyle(ChatFormatting.RED));
                })
                .useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                .matchTitle("Character Info")
                .processContainer(container -> {})
                .setWaitForMenuReopen(false)
                .clickOnSlot(ABILITY_TREE_SLOT)
                .matchTitle(Models.Container.ABILITY_TREE_PATTERN.pattern())
                .processContainer(c -> dumpAbilityTreePage(c, 1, dump));

        // region Hack for setWaitForMenuReopen to work with page 2
        // Going from first page to second adds a new button, "previous". This triggers processContainer prematurely.

        queryBuilder
                .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.STONE_AXE, NEXT_PAGE_ITEM_NAME)
                .matchTitle(Models.Container.ABILITY_TREE_PATTERN.pattern())
                .processContainer(c -> {});
        queryBuilder
                .clickOnSlot(DUMMY_SLOT)
                .matchTitle(Models.Container.ABILITY_TREE_PATTERN.pattern())
                .processContainer(c -> dumpAbilityTreePage(c, 2, dump));

        // endregion

        for (int i = 3; i <= ABILITY_TREE_PAGES; i++) {
            final int page = i; // Lambdas need final variables
            queryBuilder
                    .clickOnSlotWithName(NEXT_PAGE_SLOT, Items.STONE_AXE, NEXT_PAGE_ITEM_NAME)
                    .matchTitle(Models.Container.ABILITY_TREE_PATTERN.pattern())
                    .processContainer(c -> dumpAbilityTreePage(c, page, dump));
        }

        queryBuilder.build().executeQuery();
    }

    private void dumpAbilityTreePage(ContainerContent content, int page, AbilityTreeDump dump) {
        List<ItemStack> items = content.items();

        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack item = items.get(slot);
            if (Models.AbilityTree.isNodeItem(item, slot)) {
                dump.addNodeFromItem(item, page, slot);
            } else if (Models.AbilityTree.isConnectionItem(item)) {
                dump.addConnectionFromItem(page, slot);
            }
        }

        dump.processConnections(page, page == ABILITY_TREE_PAGES);

        if (page == ABILITY_TREE_PAGES) {
            dump.finalizeDump();
        }
    }
}
