/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.aspects;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.aspects.type.AspectInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.AspectsContainer;
import com.wynntils.models.containers.containers.RaidRewardChestContainer;
import com.wynntils.models.containers.containers.RaidRewardPreviewContainer;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class AspectModel extends Model {
    private static final Pattern NO_ASPECT_PATTERN = Pattern.compile("§8§l(?:Empty|Locked) Aspect Slot");
    private final AspectInfoRegistry aspectInfoRegistry = new AspectInfoRegistry();

    @Persisted
    private final Storage<Map<String, List<String>>> equippedAspects = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, Integer>> ownedAspects = new Storage<>(new TreeMap<>());

    public AspectModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        aspectInfoRegistry.registerDownloads(registry);
    }

    @SubscribeEvent
    public void onContentSet(ContainerSetContentEvent.Pre event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        List<Integer> equippedSlots = new ArrayList<>();
        if (currentContainer instanceof AspectsContainer aspectsContainer) {
            equippedSlots = new ArrayList<>(aspectsContainer.getEquippedSlots());
        }

        List<Integer> ownedSlots = getOwnedSlotsFromContainer(currentContainer);

        if (ownedSlots.isEmpty()) return;

        List<String> characterEquippedAspects = new ArrayList<>();
        Map<String, Integer> newOwnedAspects = ownedAspects.get();

        for (int i = 0; i < event.getItems().size(); i++) {
            boolean owned = ownedSlots.contains(i);
            boolean equipped = equippedSlots.contains(i);

            if (!equipped && !owned) continue;

            ItemStack itemStack = event.getItems().get(i);
            if (itemStack.isEmpty()) continue;

            if (equipped) {
                StyledText itemName = StyledText.fromComponent(itemStack.getHoverName());
                if (itemName.matches(NO_ASPECT_PATTERN)) continue;
            }

            Optional<AspectItem> aspectItemOpt = Models.Item.asWynnItem(itemStack, AspectItem.class);

            if (aspectItemOpt.isEmpty()) break;

            newOwnedAspects.put(
                    aspectItemOpt.get().getName(), aspectItemOpt.get().getTier());

            if (equipped) {
                characterEquippedAspects.add(aspectItemOpt.get().getName());
            }
        }

        if (!equippedSlots.isEmpty()) {
            Map<String, List<String>> currentEquippedAspects = equippedAspects.get();
            currentEquippedAspects.put(Models.Character.getId(), characterEquippedAspects);
            equippedAspects.store(currentEquippedAspects);
            equippedAspects.touched();
        }

        ownedAspects.store(newOwnedAspects);
        ownedAspects.touched();
    }

    // Handles right clicking adding/removing aspects
    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Pre event) {
        if (!(Models.Container.getCurrentContainer() instanceof AspectsContainer aspectsContainer)) return;

        List<String> characterAspects = new ArrayList<>();

        McUtils.mc().player.containerMenu.slots.forEach(slot -> {
            if (aspectsContainer.getEquippedSlots().contains(slot.index)) {
                ItemStack itemStack = slot.getItem();
                if (itemStack.isEmpty()) return;

                StyledText itemName = StyledText.fromComponent(itemStack.getHoverName());
                if (itemName.matches(NO_ASPECT_PATTERN)) return;

                Optional<AspectItem> aspectItemOpt = Models.Item.asWynnItem(itemStack, AspectItem.class);

                if (aspectItemOpt.isEmpty()) return;

                characterAspects.add(aspectItemOpt.get().getName());
            }
        });

        Map<String, List<String>> currentEquippedAspects = equippedAspects.get();
        currentEquippedAspects.put(Models.Character.getId(), characterAspects);
        equippedAspects.store(currentEquippedAspects);
        equippedAspects.touched();
    }

    public Stream<AspectInfo> getAllAspectInfos() {
        return aspectInfoRegistry.getAllAspectInfos();
    }

    public Optional<String> getEquippedAspect(int index) {
        List<String> characterAspects = equippedAspects.get().getOrDefault(Models.Character.getId(), new ArrayList<>());

        if (index < 0 || index >= characterAspects.size()) return Optional.empty();

        return Optional.of(characterAspects.get(index));
    }

    public Optional<String> getEquippedAspectByName(String aspectName) {
        List<String> characterAspects = equippedAspects.get().getOrDefault(Models.Character.getId(), new ArrayList<>());

        return characterAspects.stream()
                .filter(aspect -> aspect.toLowerCase(Locale.ROOT).endsWith(aspectName.toLowerCase(Locale.ROOT)))
                .findFirst();
    }

    public Optional<Integer> getAspectTierByName(String aspectName) {
        return ownedAspects.get().entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).endsWith(aspectName.toLowerCase(Locale.ROOT)))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public ItemAnnotation fromNameAndClass(StyledText name, ClassType classType, int tier) {
        AspectInfo aspectInfo =
                aspectInfoRegistry.getFromClassAndDisplayName(classType, name.getStringWithoutFormatting());

        if (aspectInfo == null) {
            WynntilsMod.warn("Could not find aspect info for " + name.getStringWithoutFormatting());
            return null;
        }

        return new AspectItem(aspectInfo, tier);
    }

    private List<Integer> getOwnedSlotsFromContainer(Container container) {
        if (container instanceof AspectsContainer aspectsContainer) {
            return aspectsContainer.getAspectBounds().getSlots();
        } else if (container instanceof RaidRewardChestContainer rewardChestContainer) {
            return rewardChestContainer.getAspectBounds().getSlots();
        } else if (container instanceof RaidRewardPreviewContainer rewardPreviewContainer) {
            return rewardPreviewContainer.getRewardBounds().getSlots();
        }

        return List.of();
    }
}
