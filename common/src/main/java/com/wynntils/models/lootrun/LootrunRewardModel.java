/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.containers.containers.LootrunRewardChestContainer;
import com.wynntils.models.containers.event.ValuableFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.lootrun.event.LootrunFinishedEvent;
import com.wynntils.models.npc.label.NpcLabelInfo;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class LootrunRewardModel extends Model {
    private static final int LOOTRUN_MASTER_REWARDS_RADIUS = 20;
    private static final String LOOTRUN_MASTER_NAME = "Lootrun Master";

    @Persisted
    public final Storage<Integer> dryPulls = new Storage<>(0);

    @Persisted
    private final Storage<Integer> expectedPulls = new Storage<>(-1);

    private final Set<UUID> checkedItemEntities = new HashSet<>();

    private Location closestLootrunMasterLocation = null;
    private boolean foundLootrunMythic = false;
    private boolean rerollingRewards = false;
    private boolean rewardChestIsOpened = false;

    public LootrunRewardModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onNpcLabelFound(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof NpcLabelInfo npcLabelInfo) {
            if (npcLabelInfo.getName().equals(LOOTRUN_MASTER_NAME)) {
                closestLootrunMasterLocation = event.getLabelInfo().getLocation();
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());
        int idToCheck;

        // Currently the items are ItemEntity's however this may change in the future so we want to check for
        // ItemDisplay's too to ensure future compatibility.
        if (entity instanceof ItemEntity) {
            idToCheck = ItemEntity.DATA_ITEM.id();
        } else if (entity instanceof Display.ItemDisplay) {
            idToCheck = Display.ItemDisplay.DATA_ITEM_STACK_ID.id();
        } else {
            return;
        }

        // We only care about items that are close to the lootrun master
        // If we don't know where the lootrun master is, we probably don't care
        if (closestLootrunMasterLocation == null) return;

        // Check if the item is close enough to the lootrun master
        if (closestLootrunMasterLocation.toBlockPos().distSqr(entity.blockPosition())
                > Math.pow(LOOTRUN_MASTER_REWARDS_RADIUS, 2)) {
            return;
        }

        // Check if we've already checked this item entity
        // Otherwise duplication can occur
        if (checkedItemEntities.contains(entity.getUUID())) return;

        checkedItemEntities.add(entity.getUUID());

        // Detect lootrun end reward items by checking the appearing item entities
        // This is much more reliable than checking the item in the chest,
        // as the chest can be rerolled, etc.
        for (SynchedEntityData.DataValue<?> packedItem : event.getPackedItems()) {
            if (packedItem.id() == idToCheck) {
                if (!(packedItem.value() instanceof ItemStack itemStack)) return;

                boolean foundMythic = false;
                Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
                if (gearItemOpt.isPresent()) {
                    GearItem gearItem = gearItemOpt.get();

                    if (gearItem.getGearTier() == GearTier.MYTHIC) {
                        foundMythic = true;
                    }
                }

                // No need to check tier for these as they are only mythic
                Optional<InsulatorItem> insulatorItemOpt = Models.Item.asWynnItem(itemStack, InsulatorItem.class);
                if (insulatorItemOpt.isPresent()) {
                    foundMythic = true;
                }

                Optional<SimulatorItem> simulatorItemOpt = Models.Item.asWynnItem(itemStack, SimulatorItem.class);
                if (simulatorItemOpt.isPresent()) {
                    foundMythic = true;
                }

                if (foundMythic) {
                    foundLootrunMythic = true;
                    WynntilsMod.postEvent(
                            new ValuableFoundEvent(itemStack, ValuableFoundEvent.ItemSource.LOOTRUN_REWARD_CHEST));
                }
            }
        }
    }

    @SubscribeEvent
    public void onLootrunCompleted(LootrunFinishedEvent.Completed event) {
        expectedPulls.store(event.getRewardPulls());
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (Models.Container.getCurrentContainer() instanceof LootrunRewardChestContainer lootrunRewardChestContainer) {
            checkedItemEntities.clear();
            rewardChestIsOpened = true;
        } else {
            rewardChestIsOpened = false;
        }
    }

    @SubscribeEvent
    public void onMenuClosed(MenuEvent.MenuClosedEvent event) {
        if (!rewardChestIsOpened) return;
        if (!rerollingRewards) return;
        // This is when the server closes the chest to reroll the chest

        if (expectedPulls.get() == -1) {
            WynntilsMod.warn(
                    "[LootrunRewardModel] Failed to update dry lootrun count after closing the reward chest. Did not detect number of expected pulls. Got expectedPulls="
                            + expectedPulls.get()
                            + ".");
            return;
        }

        if (foundLootrunMythic) {
            dryPulls.store(expectedPulls.get());
        } else {
            dryPulls.store(dryPulls.get() + expectedPulls.get());
        }

        rewardChestIsOpened = false;
        rerollingRewards = false;
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (e.getItemStack().isEmpty()) return;

        if (Models.Container.getCurrentContainer() instanceof LootrunRewardChestContainer lootrunRewardChestContainer) {
            if (lootrunRewardChestContainer.REROLL_REWARDS_SLOTS.contains(e.getSlotNum())) {
                StyledText rerollLoreConfirm =
                        LoreUtils.getLore(e.getItemStack()).getFirst();

                if (rerollLoreConfirm.matches(lootrunRewardChestContainer.REROLL_CONFIRM_PATTERN)) {
                    rerollingRewards = true;
                    checkedItemEntities.clear();
                }
            } else if (e.getSlotNum() == lootrunRewardChestContainer.CLOSE_CHEST_SLOT) {
                StyledText itemName = StyledText.fromComponent(e.getItemStack().getHoverName());

                if (!itemName.equals(lootrunRewardChestContainer.CLOSE_CHEST_ITEM_NAME)) return;

                // This is when the user closes the chest after claiming rewards

                if (expectedPulls.get() == -1) {
                    WynntilsMod.warn(
                            "[LootrunRewardModel] Failed to update dry lootrun count after closing the reward chest. Did not detect number of expected pulls. Got expectedPulls="
                                    + expectedPulls.get()
                                    + ". Probably, the player tried closing the chest before, which got cancelled and the contents of the chest got refreshed.");
                    return;
                }

                if (foundLootrunMythic) {
                    dryPulls.store(0);
                } else {
                    dryPulls.store(dryPulls.get() + expectedPulls.get());
                }

                expectedPulls.store(-1);

                rewardChestIsOpened = false;
            }
        }
    }
}
