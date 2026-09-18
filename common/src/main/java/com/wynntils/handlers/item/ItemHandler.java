/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.extension.ItemStackExtension;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ItemHandler extends Handler {
    private static final List<Item> WILDCARD_ITEMS = List.of(Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE);

    private static final Pattern TOOLTIP_PAGE_PATTERN = Pattern.compile("(§#ffea80ff)?\uE000");

    private final List<ItemAnnotator> annotators = new ArrayList<>();
    private final Map<Class<?>, Integer> profilingTimes = new HashMap<>();
    private final Map<Class<?>, Integer> profilingCounts = new HashMap<>();
    // Keep this as a field just of performance reasons to skip a new allocation in annotate()
    private final List<ItemAnnotator> crashedAnnotators = new ArrayList<>();
    private final List<Pattern> simplifiablePatterns = new ArrayList<>();

    public void addSimplifiablePatterns(Pattern... patterns) {
        Collections.addAll(simplifiablePatterns, patterns);
    }

    public static Optional<ItemAnnotation> getItemStackAnnotation(ItemStack itemStack) {
        if (itemStack == null) return Optional.empty();

        ItemAnnotation annotation = ((ItemStackExtension) itemStack).getAnnotation();
        return Optional.ofNullable(annotation);
    }

    public void registerAnnotator(ItemAnnotator annotator) {
        annotators.add(annotator);
    }

    public void updateItem(ItemStack itemStack, ItemAnnotation annotation, StyledText name) {
        ItemStackExtension itemStackExtension = (ItemStackExtension) itemStack;
        itemStackExtension.setAnnotation(annotation);
        itemStackExtension.setOriginalName(name);
        annotation.onUpdate(itemStack);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetSlot(SetSlotEvent.Pre event) {
        onItemStackUpdate(event.getContainer().getItem(event.getSlot()), event.getItemStack());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        NonNullList<ItemStack> existingItems;

        if (event.getContainerId() == 0) {
            // Set all for inventory
            existingItems = McUtils.inventoryMenu().getItems();
        } else if (event.getContainerId() == McUtils.containerMenu().containerId) {
            // Set all for the currently open container. Vanilla has copied inventory in the last
            // slots
            existingItems = McUtils.containerMenu().getItems();
        } else {
            // No matching container found. Annotate anyways.
            annotate(event.getItemStack());
            return;
        }

        onItemStackUpdate(existingItems.get(event.getSlot()), event.getItemStack());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerSetContent(ContainerSetContentEvent.Pre event) {
        NonNullList<ItemStack> existingItems;
        if (event.getContainerId() == 0) {
            // Set all for inventory
            existingItems = McUtils.inventoryMenu().getItems();
        } else if (event.getContainerId() == McUtils.containerMenu().containerId) {
            // Set all for the currently open container. Vanilla has copied inventory in the last
            // slots
            existingItems = McUtils.containerMenu().getItems();
        } else {
            // No matching container found. This can be due to a ContainerQuery, so
            // annotate all items
            List<ItemStack> newItems = event.getItems();

            for (ItemStack newItem : newItems) {
                annotate(newItem);
            }

            return;
        }

        List<ItemStack> newItems = event.getItems();

        for (int i = 0; i < newItems.size(); i++) {
            onItemStackUpdate(existingItems.get(i), newItems.get(i));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());

        int itemId = -1;
        if (entity instanceof ItemEntity) {
            itemId = ItemEntity.DATA_ITEM.id();
        } else if (entity instanceof Display.ItemDisplay) {
            itemId = Display.ItemDisplay.DATA_ITEM_STACK_ID.id();
        }

        // No item on this entity
        if (itemId == -1) return;

        // Item entities can have an item that needs to be annotated
        for (SynchedEntityData.DataValue<?> packedItem : event.getPackedItems()) {
            if (packedItem.id() == itemId) {
                if (!(packedItem.value() instanceof ItemStack itemStack)) return;

                annotate(itemStack);
                return;
            }
        }
    }

    private void onItemStackUpdate(ItemStack existingItem, ItemStack newItem) {
        // For e.g. FakeItemStacks we will already have an annotation
        if (((ItemStackExtension) newItem).getAnnotation() != null) return;

        ItemAnnotation annotation = ((ItemStackExtension) existingItem).getAnnotation();
        if (annotation == null) {
            annotate(newItem);
            return;
        }

        // Check if item type, damage and count matches; if not, it's definitely a new item
        // Wildcard items are exempt from this check due to the possibility of gear skins
        if (!similarStack(existingItem, newItem) && !isWildcardItem(existingItem) && !isWildcardItem(newItem)) {
            annotate(newItem);
            return;
        }

        // We need to check if the name has changed, and/or the lore has changed
        StyledText originalName = ((ItemStackExtension) existingItem).getOriginalName();
        StyledText existingName =
                StyledText.fromComponent(existingItem.getHoverName()).getNormalized();
        StyledText newName = StyledText.fromComponent(newItem.getHoverName()).getNormalized();

        if (newName.equals(existingName)) {
            // The name is identical to the existing stack; now check the lore
            if (isLoreSoftMatching(existingItem, newItem, annotation)) {
                // This is exactly the same item, so copy existing annotation
                updateItem(newItem, annotation, originalName);
            } else {
                // This could be essentially the same item, but with slight changes
                // e.g. in shiny stats or durability.
                // We need to reparse the lore since it has changed
                annotate(newItem);
            }
        } else {
            // The name is different, and it is not a know special name. This means it could be a
            // completely new item, or it could be the same item slightly changed (e.g. durability
            // has decreased). In any case, we need to reparse the complete item.
            annotate(newItem);
        }
    }

    private boolean similarStack(ItemStack firstItem, ItemStack secondItem) {
        if (!firstItem.getItem().equals(secondItem.getItem())) return false;

        // We have to use the count field here to bypass the getCount method empty flag
        // If the count is not 1, we need to reannotate, since we can't tell the old item count
        // This is because we set the item count locally, then the server sends a packet to confirm,
        // which passes this check, but the annotation is not updated
        if (firstItem.count != secondItem.count || firstItem.count != 1) {
            return false;
        }

        return firstItem.getDamageValue() == secondItem.getDamageValue();
    }

    private boolean isWildcardItem(ItemStack itemStack) {
        // This checks for gear skin items, which are a special exception for item comparisons
        return WILDCARD_ITEMS.contains(itemStack.getItem());
    }

    /**
     * This checks if the lore of the second item contains the entirety of the first item's lore, or vice versa.
     * It might have additional lines added, but these are not checked.
     */
    private boolean isLoreSoftMatching(ItemStack firstItem, ItemStack secondItem, ItemAnnotation firstItemAnnotation) {
        List<StyledText> firstLoreLines = LoreUtils.getLore(firstItem);
        List<StyledText> secondLoreLines = LoreUtils.getLore(secondItem);

        if (firstItemAnnotation instanceof PagedItemProperty pagedItemProperty && secondLoreLines.size() > 2) {
            int secondLinesLen = secondLoreLines.size();
            int secondItemPage = parseTooltipPage(secondLoreLines.get(secondLinesLen - 2));

            if (pagedItemProperty.currentPage() != secondItemPage) {
                pagedItemProperty.updatePage(secondItemPage);
                return true;
            }
        }

        // Tags implement equals, so we can use this to check if the lore is identical
        // This is the most common short-circuit case
        if (Objects.equals(firstLoreLines, secondLoreLines)) return true;

        // Continue, as we allow 3 lines to differ
        int firstLinesLen = firstLoreLines.size();
        int secondLinesLen = secondLoreLines.size();

        // Only allow a maximum number of additional lines in the longer tooltip
        if (Math.abs(firstLinesLen - secondLinesLen) > 3) return false;

        int linesToCheck = Math.min(firstLinesLen, secondLinesLen);
        // Prevent soft matching on tooltips that are very small
        if (linesToCheck < 3 && firstLinesLen != secondLinesLen) return false;

        for (int i = 0; i < linesToCheck; i++) {
            StyledText firstLine = firstLoreLines.get(i);
            StyledText secondLine = secondLoreLines.get(i);

            if (!firstLine.equals(secondLine)) return false;
        }

        // Every lore line matches from the first to the second (or second to the first), so we have a match
        return true;
    }

    private ItemAnnotation calculateAnnotation(ItemStack itemStack, StyledText name) {
        long startTime = System.currentTimeMillis();

        StyledText simplified = simplifyName(name);

        ItemAnnotation annotation = null;

        for (ItemAnnotator annotator : annotators) {
            try {
                annotation = annotator.getAnnotation(itemStack, simplified);
                if (annotation != null) {
                    break;
                }
            } catch (Throwable t) {
                // We can't disable it right away since that will cause ConcurrentModificationException
                crashedAnnotators.add(annotator);

                String annotatorName = annotator.getClass().getSimpleName();
                WynntilsMod.reportCrash(
                        CrashType.ANNOTATOR, annotatorName, annotator.getClass().getName(), "handling", t);

                WynntilsMod.warn("Problematic item:" + itemStack);
                WynntilsMod.warn("Problematic item name:" + StyledText.fromComponent(itemStack.getHoverName()));
                WynntilsMod.warn("Problematic item tags:" + itemStack.tags());

                McUtils.sendErrorToClient("Not all items will be properly parsed.");
            }
        }

        // Hopefully we have none :)
        for (ItemAnnotator annotator : crashedAnnotators) {
            annotators.remove(annotator);
        }
        crashedAnnotators.clear();

        if (annotation == null) return null;

        // Store the itemstack in the data for later use
        if (annotation instanceof WynnItem wynnItem) {
            wynnItem.getData().store(WynnItemData.ITEMSTACK_KEY, itemStack);
        }

        // Measure performance
        logProfilingData(startTime, annotation);

        return annotation;
    }

    private StyledText simplifyName(StyledText name) {
        for (Pattern pattern : simplifiablePatterns) {
            Matcher matcher = name.getMatcher(pattern);

            if (matcher.matches()) {
                int start = matcher.start(1);
                int end = matcher.end(1);

                StyledText[] separate = name.partition(StyleType.DEFAULT, start, end);

                return separate[1];
            }
        }

        return name;
    }

    private static int parseTooltipPage(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(TOOLTIP_PAGE_PATTERN);
        int index = 0;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                return index;
            }

            index++;
        }

        return 0;
    }

    private void annotate(ItemStack itemStack) {
        StyledText name = StyledText.fromComponent(itemStack.getHoverName()).getNormalized();
        ItemAnnotation annotation = calculateAnnotation(itemStack, name);
        if (annotation == null) return;

        updateItem(itemStack, annotation, name);
    }

    private void logProfilingData(long startTime, ItemAnnotation annotation) {
        long endTime = System.currentTimeMillis();
        int timeSpent = (int) (endTime - startTime);
        int allTime = profilingTimes.getOrDefault(annotation.getClass(), 0);
        profilingTimes.put(annotation.getClass(), allTime + timeSpent);

        int allCount = profilingCounts.getOrDefault(annotation.getClass(), 0);
        profilingCounts.put(annotation.getClass(), allCount + 1);
    }

    public Map<Class<?>, Integer> getProfilingTimes() {
        return profilingTimes;
    }

    public Map<Class<?>, Integer> getProfilingCounts() {
        return profilingCounts;
    }

    public void resetProfiling() {
        profilingTimes.clear();
        profilingCounts.clear();
    }

    public List<ItemAnnotator> getAnnotators() {
        return Collections.unmodifiableList(annotators);
    }
}
