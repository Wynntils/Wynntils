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
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.handlers.container.type.ContainerContentChangeType;
import com.wynntils.models.abilitytree.parser.UnprocessedAbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeInfo;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeState;
import com.wynntils.models.abilitytree.type.AbilityTreeNodeType;
import com.wynntils.models.abilitytree.type.AbilityTreeSkillNode;
import com.wynntils.models.abilitytree.type.ParsedAbilityTree;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AbilityTreeResetContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.items.items.game.AbilityShardItem;
import com.wynntils.models.items.items.gui.AbilityTreeItem;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AbilityTreeContainerQueries {
    private static final int ABILITY_TREE_SLOT = 9;
    private static final int PREVIOUS_PAGE_SLOT = 57;
    private static final int NEXT_PAGE_SLOT = 59;
    private static final int ABILITY_TREE_SHIFT_CLICK_RESET_SLOT = 58;
    private static final int RESET_ABILITY_TREE_SLOT = 85;
    private static final int ABILITY_SHARD_ONE_SLOT = 11;
    private static final int ABILITY_SHARD_TWO_SLOT = 15;
    private static final int ABILITY_SHARD_THREE_SLOT = 40;
    private static final int ABILITY_TREE_RESET_CONFIRM_SLOT = 22;
    private static final int LAST_ABILITY_TREE_ANIMATION_SLOT = 53;
    private static final int REQUIRED_ABILITY_SHARDS = 3;
    private static final StyledText NEXT_PAGE_ITEM_NAME = StyledText.fromString("§7Next Page");
    private static final StyledText PREVIOUS_PAGE_ITEM_NAME = StyledText.fromString("§7Previous Page");
    private int pageCount;
    private boolean needsAbilityTreeReset;
    private boolean hasAbilityShards;

    public void dumpAbilityTree(
            Consumer<AbilityTreeInfo> supplier,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        queryAbilityTree(new AbilityTreeContainerQueries.AbilityPageDumper(supplier), onStatus, onError, onComplete);
    }

    public void getUnlockedAbilityTree(
            Consumer<AbilityTreeInfo> supplier,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        queryAbilityTree(
                new AbilityTreeContainerQueries.AbilityPageUnlockedProcessor(supplier), onStatus, onError, onComplete);
    }

    public void updateParsedAbilityTree() {
        McUtils.player().closeContainer();

        // Wait for the container to close
        Managers.TickScheduler.scheduleNextTick(() -> queryAbilityTree(
                new AbilityTreeContainerQueries.AbilityPageSoftProcessor(Models.AbilityTree::setCurrentAbilityTree),
                status -> {},
                error -> {},
                completed -> {}));
    }

    private void queryAbilityTree(
            AbilityTreeProcessor processor,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        QueryBuilder builder = ScriptedContainerQuery.builder("Ability Tree Dumper")
                .onError(msg -> {
                    onError.accept(msg);
                    WynntilsMod.error(msg);
                })

                // Open character/compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))
                .execute(() -> onStatus.accept("Compass menu"))

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT).expectContainer(AbilityTreeContainer.class))
                .execute(() -> onStatus.accept("Ability tree menu"))
                .execute(() -> this.pageCount = 0)
                .repeat(
                        c -> ScriptedContainerQuery.containerHasSlot(
                                c, PREVIOUS_PAGE_SLOT, Items.POTION, PREVIOUS_PAGE_ITEM_NAME),
                        QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT)
                                .expectContainer(AbilityTreeContainer.class)
                                .accumulateSetSlotChanges(2)
                                .processIncomingContainer(c -> {
                                    onStatus.accept("Moving to first page");
                                }))
                .reprocess(processor::processPage)
                .execute(() -> this.pageCount++)
                .execute(() ->
                        onStatus.accept("Read page: " + this.pageCount + "/" + Models.AbilityTree.ABILITY_TREE_PAGES));

        for (int page = 2; page <= Models.AbilityTree.ABILITY_TREE_PAGES; page++) {
            builder.then(QueryStep.clickOnSlot(NEXT_PAGE_SLOT)
                            .expectContainer(AbilityTreeContainer.class)
                            .accumulateSetSlotChanges(2))
                    .reprocess(processor::processPage)
                    .execute(() -> this.pageCount++)
                    .execute(() -> onStatus.accept(
                            "Read page: " + this.pageCount + "/" + Models.AbilityTree.ABILITY_TREE_PAGES));
        }

        builder.execute(() -> onComplete.accept("Finished dumping ability tree"));
        builder.build().executeQuery();
    }

    public void applyAbilityTreeLoadout(
            List<AbilityTreeSkillNode> nodesToUnlock,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        needsAbilityTreeReset = false;
        hasAbilityShards = false;
        StatusEffect statusEffect = Models.StatusEffect.searchStatusEffectByName("Tree Manipulation");

        QueryBuilder builder = ScriptedContainerQuery.builder("Ability Tree Unlock")
                .onError(msg -> {
                    onError.accept(msg);
                    WynntilsMod.error(msg);
                })

                // Open character/compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))
                .execute(() -> onStatus.accept("Compass menu"))

                // Check ability points and abiliy shards in compass menu
                .reprocess(container -> {
                    Optional<AbilityTreeItem> abilityTreeItem =
                            Models.Item.asWynnItem(container.items().get(ABILITY_TREE_SLOT), AbilityTreeItem.class);
                    if (abilityTreeItem.isEmpty()) {
                        needsAbilityTreeReset = false;
                    }
                    abilityTreeItem.ifPresent(
                            treeItem -> needsAbilityTreeReset = treeItem.getCount() <= treeItem.getTotalPoints());

                    int amount = 0;

                    for (ItemStack itemStack : container.items()) {
                        Optional<AbilityShardItem> abilityShardItem =
                                Models.Item.asWynnItem(itemStack, AbilityShardItem.class);
                        if (abilityShardItem.isEmpty()) continue;

                        if (!abilityShardItem.get().getQuestRequirement()) {
                            throw new ContainerQueryException("You do not meet the quest requirement.");
                        }

                        amount += itemStack.getCount();
                    }

                    if (amount >= REQUIRED_ABILITY_SHARDS) {
                        hasAbilityShards = true;
                    }

                    if (needsAbilityTreeReset && !hasAbilityShards && statusEffect == null) {
                        throw new ContainerQueryException("insufficient ability shards (need 3)");
                    }
                })

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT).expectContainer(AbilityTreeContainer.class))
                .execute(() -> onStatus.accept("Ability tree menu"));

        if (statusEffect != null) {
            AtomicBoolean sawAnimationEnd = new AtomicBoolean(false);

            builder.execute(() -> onStatus.accept("Has Tree Manipulation resetting tree via shift click"));

            // Conditional: Only shift-click reset if we need to
            builder.conditionalThen(
                    container -> needsAbilityTreeReset,
                    QueryStep.shiftClickOnSlot(ABILITY_TREE_SHIFT_CLICK_RESET_SLOT)
                            .expectContainer(AbilityTreeContainer.class)
                            .verifyContentChange((container, changes, changeType) -> {
                                if (!sawAnimationEnd.get()) {
                                    if (changeType == ContainerContentChangeType.SET_SLOT
                                            && changes.containsKey(LAST_ABILITY_TREE_ANIMATION_SLOT)
                                            && changes.get(LAST_ABILITY_TREE_ANIMATION_SLOT)
                                                            .getItem()
                                                    == Items.AIR) {
                                        sawAnimationEnd.set(true);
                                    }
                                    return false;
                                }
                                return changeType == ContainerContentChangeType.SET_CONTENT;
                            })
                            .processIncomingContainer(container -> onStatus.accept("Ability tree has been reset")));
        }

        if (statusEffect == null) {
            // Conditional reset steps: each only executes if needsReset is true
            // If needsReset is false, each conditionalThen skips its wrapped step and continues
            builder.execute(() -> onStatus.accept("Opening ability tree reset menu"));

            builder.conditionalThen(
                    container -> needsAbilityTreeReset,
                    QueryStep.clickOnSlot(RESET_ABILITY_TREE_SLOT)
                            .expectContainer(AbilityTreeContainer.class, AbilityTreeResetContainer.class)
                            .verifyContentChange((container, changes, changeType) ->
                                    Models.Container.getCurrentContainer() instanceof AbilityTreeResetContainer)
                            .processIncomingContainer(c -> onStatus.accept("Ability tree reset menu opened")));

            builder.conditionalThen(
                    container -> needsAbilityTreeReset,
                    QueryStep.clickOnSlot(ABILITY_SHARD_ONE_SLOT).accumulateSetSlotChanges(2));

            builder.conditionalThen(
                    container -> needsAbilityTreeReset,
                    QueryStep.clickOnSlot(ABILITY_SHARD_TWO_SLOT).accumulateSetSlotChanges(2));

            builder.conditionalThen(
                    container -> needsAbilityTreeReset,
                    QueryStep.clickOnSlot(ABILITY_SHARD_THREE_SLOT).accumulateSetSlotChanges(2));

            builder.conditionalThen(
                    container -> needsAbilityTreeReset,
                    QueryStep.clickOnSlot(ABILITY_TREE_RESET_CONFIRM_SLOT)
                            .expectContainer(AbilityTreeResetContainer.class, AbilityTreeContainer.class)
                            .verifyContentChange((container, changes, changeType) ->
                                    Models.Container.getCurrentContainer() instanceof AbilityTreeContainer)
                            .processIncomingContainer(c -> onStatus.accept("Ability tree has been reset")));
        }

        // Rewind to page 1
        builder.repeat(
                c -> ScriptedContainerQuery.containerHasSlot(
                        c, PREVIOUS_PAGE_SLOT, Items.POTION, PREVIOUS_PAGE_ITEM_NAME),
                QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT)
                        .expectContainer(AbilityTreeContainer.class)
                        .accumulateSetSlotChanges(2)
                        .processIncomingContainer(c -> {
                            onStatus.accept("Moving to first page");
                        }));

        int currentPage = 1;
        int totalNodes = nodesToUnlock.size();
        int nodeIndex = 0;

        for (AbilityTreeSkillNode node : nodesToUnlock) {
            nodeIndex++;
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
            final int progress = nodeIndex;
            final String nodeName = node.name();

            // Click the node
            // ABILITY_TREE_SHIFT_CLICK_RESET_SLOT is not the last slot if the ability has an archetype attached to it.
            // then it will be the archetype slot, but this does not matter for confirming if it's unlocked.
            builder.execute(() -> onStatus.accept("Unlocking " + nodeName + " (" + progress + "/" + totalNodes + ")"));
            builder.then(QueryStep.clickOnSlot(targetSlot)
                    .expectContainer(AbilityTreeContainer.class)
                    .verifyContentChange((container, changes, changeType) -> changeType
                                    == ContainerContentChangeType.SET_SLOT
                            && changes.containsKey(ABILITY_TREE_SHIFT_CLICK_RESET_SLOT)
                            && changes.get(ABILITY_TREE_SHIFT_CLICK_RESET_SLOT).getItem() == Items.POTION));

            // Verify it actually became unlocked
            builder.reprocess(container -> {
                ItemStack item = container.items().get(verifySlot);
                AbilityTreeNodeType type = AbilityTreeNodeType.fromItemStack(item);
                if (type == null || type.getState() != AbilityTreeNodeState.UNLOCKED) {
                    throw new ContainerQueryException(
                            "Node unlock failed at slot " + verifySlot + " (expected UNLOCKED, found " + type
                                    + "). Probably because the ability tree got updated.");
                }

                StyledText nameStyledText = StyledText.fromComponent(item.getHoverName());
                StyledText actualName;
                if (nameStyledText.getPartCount() == 1) {
                    actualName = nameStyledText;
                } else {
                    actualName = nameStyledText.iterate((part, changes) -> {
                        if (!part.getPartStyle().isBold()) {
                            changes.clear();
                        }
                        return IterationDecision.CONTINUE;
                    });
                }

                String foundName = actualName.getString(StyleType.NONE);
                if (!foundName.equals(node.name())) {
                    throw new ContainerQueryException(
                            "Node unlock failed at slot " + verifySlot + " (expected name '" + node.name()
                                    + "', found '" + foundName + "'). Probably because the ability tree got updated.");
                }
            });
        }

        builder.execute(() -> onComplete.accept("Ability tree loadout applied successfully"));

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
            this.unprocessedTree.setNormalizeToDefaultType(true);
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
}
