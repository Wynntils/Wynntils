/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.extension.ItemStackExtension;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemHandler extends Handler {
    private static final List<Item> WILDCARD_ITEMS = List.of(Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE);

    private final List<ItemAnnotator> annotators = new ArrayList<>();
    private final Map<Class<?>, Integer> profilingTimes = new HashMap<>();
    private final Map<Class<?>, Integer> profilingCounts = new HashMap<>();
    // Keep this as a field just of performance reasons to skip a new allocation in annotate()
    private final List<ItemAnnotator> crashedAnnotators = new ArrayList<>();

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

    private void onItemStackUpdate(ItemStack existingItem, ItemStack newItem) {
        // For e.g. FakeItemStacks we will already have an annotation
        if (((ItemStackExtension) newItem).getAnnotation() != null) return;

        ItemAnnotation annotation = ((ItemStackExtension) existingItem).getAnnotation();
        if (annotation == null) {
            annotate(newItem);
            return;
        }

        // Check if item type, damage and count matches, if not, it's definitely a new item
        // Wildcard items are exempt from this check due to the possibility of gear skins
        if (!similarStack(existingItem, newItem) && !isWildcardItem(existingItem) && !isWildcardItem(newItem)) {
            annotate(newItem);
            return;
        }

        // This might be just a name update. Check if lore matches:
        if (!LoreUtils.loreSoftMatches(existingItem, newItem, 3)) {
            // This could be a new item, or a crafted item losing in durability
            annotate(newItem);
            return;
        }

        StyledText originalName = ((ItemStackExtension) existingItem).getOriginalName();
        StyledText existingName =
                StyledText.fromComponent(existingItem.getHoverName()).getNormalized();
        StyledText newName = StyledText.fromComponent(newItem.getHoverName()).getNormalized();

        if (newName.equals(existingName)) {
            // This is exactly the same item, so copy existing annotation
            updateItem(newItem, annotation, originalName);
            return;
        }

        // The lore is the same, but the name is different. Determine the reason for the name change
        StyledText originalBaseName = getBaseName(originalName);
        StyledText existingBaseName = getBaseName(existingName);
        StyledText newBaseName = getBaseName(newName);

        // When a crafted item loses durability (or a consumable loses a charge), we need to detect
        // this and update the item. But note that this might happen exactly after a spell!
        // So check against originalName, not existingName.
        if (!newName.equals(originalName) && newBaseName.equals(originalBaseName)) {
            // The base name is the same but the full name differs. This means we have an updated
            // title, and the existing item has changed some property.
            annotation = calculateAnnotation(newItem, newName);
        }

        // Set the new item with the old (or updated) annotation, and keep the original name
        updateItem(newItem, annotation, originalName);

        // If an item is "really" renamed, we need to send out an event. But this should not
        // trigger just for a consumable or crafted gear that changes the [...] text, so
        // check only on base name, not the full name.
        if (!newBaseName.equals(existingBaseName)) {
            // This is the same item, but it is renamed to signal e.g. a spell.
            ItemRenamedEvent event = new ItemRenamedEvent(newItem, existingName, newName);
            WynntilsMod.postEvent(event);
            if (event.isCanceled()) {
                newItem.setHoverName(existingItem.getHoverName());
            }
        }
    }

    private StyledText getBaseName(StyledText name) {
        return StyledText.fromPart(name.getFirstPart());
    }

    private boolean similarStack(ItemStack firstItem, ItemStack secondItem) {
        if (!firstItem.getItem().equals(secondItem.getItem())) return false;

        // We have to use the count field here to bypass the getCount method empty flag
        if (firstItem.count != secondItem.count) {
            return false;
        }

        return firstItem.getDamageValue() == secondItem.getDamageValue();
    }

    private boolean isWildcardItem(ItemStack itemStack) {
        // This checks for gear skin items, which are a special exception for item comparisons
        return WILDCARD_ITEMS.contains(itemStack.getItem());
    }

    private ItemAnnotation calculateAnnotation(ItemStack itemStack, StyledText name) {
        long startTime = System.currentTimeMillis();

        ItemAnnotation annotation = null;

        for (ItemAnnotator annotator : annotators) {
            try {
                annotation = annotator.getAnnotation(itemStack, name);
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
                WynntilsMod.warn("Problematic item tags:" + itemStack.getTag());

                McUtils.sendErrorToClient("Not all items will be properly parsed.");
            }
        }

        // Hopefully we have none :)
        for (ItemAnnotator annotator : crashedAnnotators) {
            annotators.remove(annotator);
        }
        crashedAnnotators.clear();

        if (annotation == null) return null;

        // Measure performance
        logProfilingData(startTime, annotation);

        return annotation;
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
}
