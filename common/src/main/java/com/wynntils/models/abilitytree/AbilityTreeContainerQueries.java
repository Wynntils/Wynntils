/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree;

import com.google.common.collect.ImmutableMap;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.abilitytree.parser.UnprocessedAbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
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
    private static final int BACK_BUTTON_SLOT = 63;
    private static final StyledText NEXT_PAGE_ITEM_NAME = StyledText.fromString("§7Next Page");
    private static final StyledText PREVIOUS_PAGE_ITEM_NAME = StyledText.fromString("§7Previous Page");
    private int pageCount;

    public void dumpAbilityTree(Consumer<AbilityTreeInfo> supplier) {
        queryAbilityTree(new AbilityTreeContainerQueries.AbilityPageDumper(supplier));
    }

    public void getUnlockedAbilityTree(Consumer<AbilityTreeInfo> supplier) {
        queryAbilityTree(new AbilityTreeContainerQueries.AbilityPageUnlockedProcessor(supplier));
    }

    public void updateParsedAbilityTree() {
        McUtils.player().closeContainer();

        // Wait for the container to close
        Managers.TickScheduler.scheduleNextTick(() -> queryAbilityTree(
                new AbilityTreeContainerQueries.AbilityPageSoftProcessor(Models.AbilityTree::setCurrentAbilityTree)));
    }

    private void queryAbilityTree(AbilityTreeProcessor processor) {
        QueryBuilder builder = ScriptedContainerQuery.builder("Ability Tree Navigation Debug")
                .onError(msg -> WynntilsMod.warn("[AbilityTreeDebug] Query failed: " + msg))

                // Open character/compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT).expectContainer(AbilityTreeContainer.class))
                .execute(() -> WynntilsMod.info("[AbilityTreeDebug] start"))
                .execute(() -> this.pageCount = 0)
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, PREVIOUS_PAGE_SLOT, Items.POTION, PREVIOUS_PAGE_ITEM_NAME),
                        QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT)
                                .expectContainer(AbilityTreeContainer.class)
                                .accumulateSetSlotChanges(2)
                                .processIncomingContainer(c -> {
                                    WynntilsMod.info("[AbilityTreeDebug] going backwards.");
                                }))
                .reprocess(processor::processPage)
                .execute(() -> this.pageCount++);

        for (int page = 2; page <= Models.AbilityTree.ABILITY_TREE_PAGES; page++) {
            builder.then(QueryStep.clickOnSlot(NEXT_PAGE_SLOT)
                            .expectContainer(AbilityTreeContainer.class)
                            .accumulateSetSlotChanges(2))
                    .reprocess(processor::processPage)
                    .execute(() -> this.pageCount++);
        }

        builder.execute(() -> WynntilsMod.info(
                "[AbilityTreeDebug] Reached final page (" + this.pageCount + "), navigation complete"));
        builder.build().executeQuery();
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

    /**
     * Parses the ability tree and returns only unlocked nodes with their connections processed.
     * Description lines are stripped to save memory.
     */
    private static class AbilityPageUnlockedProcessor extends AbilityTreeProcessor {
        private final Consumer<AbilityTreeInfo> supplier;
        private final UnprocessedAbilityTreeInfo unprocessedTree = new UnprocessedAbilityTreeInfo();

        protected AbilityPageUnlockedProcessor(Consumer<AbilityTreeInfo> supplier) {
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
                AbilityTreeInfo fullTree = unprocessedTree.getProcesssed();

                List<AbilityTreeSkillNode> unlockedNodes = fullTree.nodes().stream()
                        .filter(node -> node.abilityTreeNodeType().getState() == AbilityTreeNodeState.UNLOCKED)
                        .map(AbilityTreeSkillNode::withoutDescriptions)
                        .toList();

                this.supplier.accept(new AbilityTreeInfo(unlockedNodes));
            }
        }
    }

    public void executeUnlocks(
            List<AbilityTreeSkillNode> nodesToUnlock,
            Consumer<String> onError,
            Runnable onComplete) {

        QueryBuilder builder = ScriptedContainerQuery.builder("Ability Tree Unlock")
                .onError(msg -> onError.accept("Ability tree unlock failed: " + msg))
                // Open character/compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))

                .execute(() -> WynntilsMod.info("compass"))

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT).expectContainer(AbilityTreeContainer.class))

                .execute(() -> WynntilsMod.info("starting unlock"));

        // Rewind to page 1
        builder.repeat(
                c -> ScriptedContainerQuery.containerHasSlot(
                        c, PREVIOUS_PAGE_SLOT, Items.POTION, PREVIOUS_PAGE_ITEM_NAME),
                QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT)
                        .expectContainer(AbilityTreeContainer.class)
                        .accumulateSetSlotChanges(2));

        int currentPage = 1;

        for (AbilityTreeSkillNode node : nodesToUnlock) {
            int targetPage = node.location().page();
            int targetSlot = node.location().row() * 9 + node.location().col();

            // Navigate
            while (currentPage < targetPage) {
                builder.then(QueryStep.clickOnSlot(NEXT_PAGE_SLOT)
                        .expectContainer(AbilityTreeContainer.class)
                        .accumulateSetSlotChanges(2));
                currentPage++;
            }
            while (currentPage > targetPage) {
                builder.then(QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT)
                        .expectContainer(AbilityTreeContainer.class)
                        .accumulateSetSlotChanges(2));
                currentPage--;
            }

            final int verifySlot = targetSlot;

            // Click the node
            builder.then(QueryStep.clickOnSlot(targetSlot)
                    .expectContainer(AbilityTreeContainer.class)
                    .accumulateSetSlotChanges(2));

            // Verify it actually became unlocked
            builder.reprocess(container -> {
                ItemStack item = container.items().get(verifySlot);
                AbilityTreeNodeType type = AbilityTreeNodeType.fromItemStack(item);
                if (type == null || type.getState() != AbilityTreeNodeState.UNLOCKED) {
                    throw new ContainerQueryException(
                            "Node unlock failed at slot " + verifySlot
                                    + " (expected UNLOCKED, found " + type + ")");
                }
            });
        }

        builder.execute(() -> {
            WynntilsMod.info("Ability tree loadout applied successfully");
            onComplete.run();
        });

        builder.build().executeQuery();
    }
}
