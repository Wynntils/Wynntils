/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.container.ContainerQueryException;
import com.wynntils.handlers.container.scriptedquery.QueryBuilder;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.containers.AbilityTreeContainer;
import com.wynntils.models.containers.containers.AspectsContainer;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AspectContainerQueries {
    private static final int ABILITY_TREE_SLOT = 9;
    private static final int ASPECTS_BUTTON_SLOT = 86;
    private static final int PREVIOUS_PAGE_SLOT = 57;
    private static final int NEXT_PAGE_SLOT = 59;
    private static final StyledText NEXT_PAGE_ITEM_NAME = StyledText.fromString("§7Next Page");
    private static final StyledText PREVIOUS_PAGE_ITEM_NAME = StyledText.fromString("§7Previous Page");
    private int currentPage;
    private int checkAspectSlotIdx;
    private boolean hasAlreadyEquippedAspects;

    // Ordered from first equippable to last equippable slot
    private static final List<Integer> EQUIPPED_SLOTS = List.of(18, 11, 4, 15, 26);
    // Inventory slots where owned/aspect items appear (35-53)
    private static final List<Integer> ASPECT_INVENTORY_SLOTS =
            IntStream.rangeClosed(35, 53).boxed().toList();

    public void dumpAspectContainer(
            Consumer<SavableAspectSet> supplier,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        queryAspectContainer(new AspectContainerQueries.AspectContainerDumper(supplier), onStatus, onError, onComplete);
    }

    private void queryAspectContainer(
            AspectContainerProcessor processor,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        QueryBuilder builder = ScriptedContainerQuery.builder("Aspect Container Dumper")
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

                // Open aspects menu
                .then(QueryStep.clickOnSlot(ASPECTS_BUTTON_SLOT)
                        .expectContainer(AbilityTreeContainer.class, AspectsContainer.class)
                        .verifyContentChange((container, changes, changeType) ->
                                Models.Container.getCurrentContainer() instanceof AspectsContainer)
                        .processIncomingContainer(processor::processContainer))
                .execute(() -> onStatus.accept("Aspects menu"));

        builder.execute(() -> onComplete.accept("Finished dumping aspects"));
        builder.build().executeQuery();
    }

    public void applyAspectLoadout(
            List<String> aspectsToEquip,
            Consumer<String> onStatus,
            Consumer<String> onError,
            Consumer<String> onComplete) {
        Map<Integer, Deque<Pair<String, Integer>>> aspectLocations = new HashMap<>();
        Deque<Integer> slotsToUnequip = new ArrayDeque<>();

        QueryBuilder builder = ScriptedContainerQuery.builder("Aspect Unlock")
                .onError(msg -> {
                    onError.accept(msg);
                    WynntilsMod.error(msg);
                })

                // Open compass menu
                .then(QueryStep.useItemInHotbar(InventoryUtils.COMPASS_SLOT_NUM)
                        .expectContainer(CharacterInfoContainer.class))
                .execute(() -> onStatus.accept("Compass menu"))

                // Open ability menu
                .then(QueryStep.clickOnSlot(ABILITY_TREE_SLOT).expectContainer(AbilityTreeContainer.class))
                .execute(() -> onStatus.accept("Ability tree menu"))

                // Open aspects menu
                .then(QueryStep.clickOnSlot(ASPECTS_BUTTON_SLOT)
                        .expectContainer(AbilityTreeContainer.class, AspectsContainer.class)
                        .verifyContentChange((container, changes, changeType) ->
                                Models.Container.getCurrentContainer() instanceof AspectsContainer))
                .execute(() -> onStatus.accept("Aspects menu"))

                // skip everything if the desired aspects are already equipped
                .reprocess(container -> {
                    List<String> equipped = getEquippedAspectNames(container);
                    if (areAspectsMatching(equipped, aspectsToEquip)) {
                        hasAlreadyEquippedAspects = true;
                        onStatus.accept("Aspects already equipped");
                    }
                })

                // Unequip all currently equipped aspects
                .repeat(
                        c -> {
                            if (hasAlreadyEquippedAspects) return false;
                            slotsToUnequip.clear();
                            for (int slot : EQUIPPED_SLOTS) {
                                ItemStack itemStack = c.items().get(slot);
                                Optional<AspectItem> item = Models.Item.asWynnItem(itemStack, AspectItem.class);
                                if (item.isPresent()) slotsToUnequip.addFirst(slot);
                            }
                            return !slotsToUnequip.isEmpty();
                        },
                        QueryStep.clickOnSlot(() -> slotsToUnequip.peekFirst())
                                .expectContainer(AspectsContainer.class)
                                .accumulateSetSlotChanges(2)
                                .processIncomingContainer(container -> {
                                    onStatus.accept("Unequipped aspect from slot: " + slotsToUnequip.peekFirst());
                                    slotsToUnequip.removeFirst();
                                }))

                // Rewind to page 1
                .repeat(
                        c -> {
                            if (hasAlreadyEquippedAspects) return false;
                            return ScriptedContainerQuery.containerHasSlot(
                                    c, PREVIOUS_PAGE_SLOT, Items.POTION, PREVIOUS_PAGE_ITEM_NAME);
                        },
                        QueryStep.clickOnSlot(PREVIOUS_PAGE_SLOT)
                                .expectContainer(AspectsContainer.class)
                                .accumulateSetSlotChanges(2)
                                .processIncomingContainer(c -> onStatus.accept("Moving to first page")))
                .execute(() -> {
                    if (!hasAlreadyEquippedAspects) currentPage = 1;
                })

                // Scan page 1 for desired aspects
                .reprocess(container -> {
                    if (hasAlreadyEquippedAspects) return;
                    scanPageForAspects(container, aspectsToEquip, aspectLocations, currentPage);
                })

                // Equip aspects across all pages
                .repeat(
                        c -> {
                            if (hasAlreadyEquippedAspects) return false;
                            // Stop early if all 5 aspect slots are already filled
                            if (checkAspectSlotIdx >= EQUIPPED_SLOTS.size()) return false;

                            // Continue if there are still aspects to equip on this page
                            Deque<Pair<String, Integer>> pageAspects = aspectLocations.get(currentPage);
                            if (pageAspects != null && !pageAspects.isEmpty()) return true;

                            // Otherwise continue only if there is another page
                            return ScriptedContainerQuery.containerHasSlot(
                                    c, NEXT_PAGE_SLOT, Items.POTION, NEXT_PAGE_ITEM_NAME);
                        },
                        QueryStep.clickOnSlot(() -> {
                                    Deque<Pair<String, Integer>> pageAspects = aspectLocations.get(currentPage);
                                    if (pageAspects != null && !pageAspects.isEmpty()) {
                                        // Equip the next aspect on this page
                                        return pageAspects.peekFirst().value();
                                    } else {
                                        // Turn to the next page
                                        return NEXT_PAGE_SLOT;
                                    }
                                })
                                .expectContainer(AspectsContainer.class)
                                .accumulateSetSlotChanges(2)
                                .processIncomingContainer(container -> {
                                    Deque<Pair<String, Integer>> pageAspects = aspectLocations.get(currentPage);

                                    if (pageAspects != null && !pageAspects.isEmpty()) {
                                        // We just clicked an aspect slot
                                        String aspectName =
                                                pageAspects.peekFirst().key();

                                        // Remove it before re-scanning
                                        pageAspects.removeFirst();

                                        // Verify it actually landed in the next free equipped slot
                                        int checkSlot = EQUIPPED_SLOTS.get(checkAspectSlotIdx);
                                        ItemStack stack = container.items().get(checkSlot);

                                        if (stack.isEmpty()) {
                                            throw new ContainerQueryException("Failed to place aspect.");
                                        }

                                        AspectItem aspect = Models.Item.asWynnItem(stack, AspectItem.class)
                                                .orElseThrow(
                                                        () -> new ContainerQueryException("Failed to place aspect."));

                                        if (!aspect.getName().equals(aspectName)) {
                                            throw new ContainerQueryException("Failed to place aspect.");
                                        }

                                        checkAspectSlotIdx = checkAspectSlotIdx + 1;
                                        onStatus.accept("aspect placed: " + aspect.getName());

                                        // Clear and re-scan the current page for any remaining desired aspects
                                        if (pageAspects != null) {
                                            pageAspects.clear();
                                        }
                                        scanPageForAspects(container, aspectsToEquip, aspectLocations, currentPage);
                                    } else {
                                        // We just turned the page
                                        currentPage++;
                                        onStatus.accept("Scanning page " + currentPage);

                                        // Scan the new page for desired aspects
                                        scanPageForAspects(container, aspectsToEquip, aspectLocations, currentPage);
                                    }
                                }))

                // verify every aspect is actually equipped
                .reprocess(container -> {
                    if (hasAlreadyEquippedAspects) return;
                    List<String> equipped = getEquippedAspectNames(container);
                    if (!areAspectsMatching(equipped, aspectsToEquip)) {
                        throw new ContainerQueryException(
                                "Failed to equip all aspects. Probably because an aspect name got changed");
                    }
                });

        builder.execute(() -> onComplete.accept("Finished loading aspects"));
        builder.build().executeQuery();
    }

    private static void scanPageForAspects(
            ContainerContent container,
            List<String> aspectsToEquip,
            Map<Integer, Deque<Pair<String, Integer>>> aspectLocations,
            int currentPage) {
        for (int slot : ASPECT_INVENTORY_SLOTS) {
            if (slot >= container.items().size()) continue;

            ItemStack stack = container.items().get(slot);
            if (stack.isEmpty()) continue;

            Optional<AspectItem> aspectOpt = Models.Item.asWynnItem(stack, AspectItem.class);
            if (aspectOpt.isEmpty()) continue;

            String name = aspectOpt.get().getName();
            if (aspectsToEquip.contains(name)) {
                aspectLocations
                        .computeIfAbsent(currentPage, p -> new ArrayDeque<>())
                        .addLast(Pair.of(name, slot));
            }
        }
    }

    private static List<String> getEquippedAspectNames(ContainerContent container) {
        List<String> names = new ArrayList<>();
        for (int slot : EQUIPPED_SLOTS) {
            if (slot >= container.items().size()) continue;
            ItemStack stack = container.items().get(slot);
            if (stack.isEmpty()) continue;
            Models.Item.asWynnItem(stack, AspectItem.class).ifPresent(aspect -> names.add(aspect.getName()));
        }
        return names;
    }

    private static boolean areAspectsMatching(List<String> equipped, List<String> target) {
        if (equipped.size() != target.size()) return false;
        return new java.util.HashSet<>(equipped).equals(new java.util.HashSet<>(target));
    }

    private abstract static class AspectContainerProcessor {
        protected abstract void processContainer(ContainerContent content);
    }

    /**
     * Reads the equipped aspect slots from the container and emits a SavableAspectLoadout.
     */
    private static class AspectContainerDumper extends AspectContainerProcessor {
        private final Consumer<SavableAspectSet> supplier;

        protected AspectContainerDumper(Consumer<SavableAspectSet> supplier) {
            this.supplier = supplier;
        }

        @Override
        protected void processContainer(ContainerContent content) {
            List<String> currentAspects = new ArrayList<>();
            ClassType classType = ClassType.NONE;

            if (Models.Container.getCurrentContainer() instanceof AspectsContainer aspectsContainer) {
                for (int slot : aspectsContainer.getEquippedSlots()) {
                    if (slot >= content.items().size()) continue;

                    ItemStack stack = content.items().get(slot);
                    if (stack.isEmpty()) continue;

                    Optional<AspectItem> aspectOpt = Models.Item.asWynnItem(stack, AspectItem.class);
                    if (aspectOpt.isEmpty()) continue;

                    currentAspects.add(aspectOpt.get().getName());

                    // Derive class type from the first valid aspect if not yet known
                    if (classType == ClassType.NONE) {
                        AspectInfo info = aspectOpt.get().getAspectInfo();
                        if (info != null && info.classType() != null) {
                            classType = info.classType();
                        }
                    }
                }
            }

            // Fallback to the active character class if we couldn't determine it from aspects
            if (classType == ClassType.NONE) {
                classType = Models.Character.getClassType();
            }

            supplier.accept(new SavableAspectSet(currentAspects, classType));
        }
    }
}
