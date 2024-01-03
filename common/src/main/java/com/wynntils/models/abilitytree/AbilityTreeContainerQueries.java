/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.google.common.collect.ImmutableMap;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.abilitytree.parser.UnprocessedAbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AbilityTreeContainerQueries {
    private static final int ABILITY_TREE_SLOT = 9;
    private static final int PREVIOUS_PAGE_SLOT = 57;
    private static final int NEXT_PAGE_SLOT = 59;
    private static final StyledText NEXT_PAGE_ITEM_NAME = StyledText.fromString("§7Next Page");
    private static final StyledText PREVIOUS_PAGE_ITEM_NAME = StyledText.fromString("§7Previous Page");
    private int pageCount;

    public void dumpAbilityTree(Consumer<AbilityTreeInfo> supplier) {
        queryAbilityTree(new AbilityTreeContainerQueries.AbilityPageDumper(supplier));
    }

    public void updateParsedAbilityTree() {
        McUtils.player().closeContainer();

        // Wait for the container to close
        Managers.TickScheduler.scheduleNextTick(() -> queryAbilityTree(
                new AbilityTreeContainerQueries.AbilityPageSoftProcessor(Models.AbilityTree::setCurrentAbilityTree)));
    }

    private void queryAbilityTree(AbilityTreeProcessor processor) {
        ScriptedContainerQuery query = ScriptedContainerQuery.builder("Ability Tree Query")
                .onError(msg -> {
                    WynntilsMod.warn("Problem querying Ability Tree: " + msg);
                    McUtils.sendErrorToClient("Dumping Ability Tree failed");
                })

                // Open character/compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainerTitle(ContainerModel.CHARACTER_INFO_NAME))

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT)
                        .expectContainerTitle(Models.Container.ABILITY_TREE_PATTERN.pattern()))

                // Go to first page, and save current page number
                .execute(() -> this.pageCount = 0)
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, PREVIOUS_PAGE_SLOT, Items.STONE_AXE, PREVIOUS_PAGE_ITEM_NAME),
                        QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT).processIncomingContainer(c -> {
                            // Count how many times this is done, and save this value.
                            // If we did not even enter here, we were already on first page.
                            this.pageCount++;
                        }))

                // Process first page
                .reprocess(processor::processPage)

                // Repeatedly go to next page, if any, and process it
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, NEXT_PAGE_SLOT, Items.STONE_AXE, NEXT_PAGE_ITEM_NAME),
                        QueryStep.clickOnSlot(NEXT_PAGE_SLOT).processIncomingContainer(processor::processPage))

                // Go back to initial page
                .repeat(
                        c -> {
                            // Go back as many pages as the original count was from the end
                            this.pageCount++;
                            return this.pageCount != Models.AbilityTree.ABILITY_TREE_PAGES;
                        },
                        QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT))
                .build();

        query.executeQuery();
    }

    private abstract static class AbilityTreeProcessor {
        private int page = 1;

        protected void processPage(ContainerContent content) {
            processPage(content, page);
            page++;
        }

        protected abstract void processPage(ContainerContent content, int page);
    }

    /**
     * Parses the whole ability tree and saves it to disk.
     */
    private static class AbilityPageDumper extends AbilityTreeProcessor {
        private final Consumer<AbilityTreeInfo> supplier;
        private final UnprocessedAbilityTreeInfo unprocessedTree = new UnprocessedAbilityTreeInfo();

        protected AbilityPageDumper(Consumer<AbilityTreeInfo> supplier) {
            this.supplier = supplier;
        }

        @Override
        protected void processPage(ContainerContent content, int page) {
            List<ItemStack> items = content.items();

            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack itemStack = items.get(slot);

                unprocessedTree.processItem(itemStack, page, slot, true);
            }

            if (page == Models.AbilityTree.ABILITY_TREE_PAGES) {
                this.supplier.accept(unprocessedTree.getProcesssed());
            }
        }
    }

    /**
     * Only parses nodes of an ability tree, and stores it.
     */
    private static class AbilityPageSoftProcessor extends AbilityTreeProcessor {
        private final Map<AbilityTreeSkillNode, AbilityTreeNodeState> collectedInfo = new LinkedHashMap<>();
        private final Consumer<ParsedAbilityTree> callback;

        protected AbilityPageSoftProcessor(Consumer<ParsedAbilityTree> callback) {
            this.callback = callback;
        }

        @Override
        protected void processPage(ContainerContent content, int page) {
            List<ItemStack> items = content.items();

            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack itemStack = items.get(slot);
                if (!Models.AbilityTree.ABILITY_TREE_PARSER.isNodeItem(itemStack, slot)) continue;

                Pair<AbilityTreeSkillNode, AbilityTreeNodeState> parsedNode =
                        Models.AbilityTree.ABILITY_TREE_PARSER.parseNodeFromItem(
                                itemStack, page, slot, collectedInfo.size() + 1);

                collectedInfo.put(parsedNode.key(), parsedNode.value());
            }

            boolean lastPage = page == Models.AbilityTree.ABILITY_TREE_PAGES;

            if (lastPage) {
                callback.accept(new ParsedAbilityTree(ImmutableMap.copyOf(collectedInfo)));
            }
        }
    }
}
