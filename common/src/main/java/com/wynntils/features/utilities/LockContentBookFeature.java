/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.activities.event.ContentBookOpenEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class LockContentBookFeature extends Feature {
    private static final StyledText CONTENT_BOOK_NAME = StyledText.fromString("§dContent Book");

    @Persisted
    private final Config<ForceUnlockAction> forceUnlockAction = new Config<>(ForceUnlockAction.NONE);

    @Persisted
    private final Config<Boolean> lockInRaid = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInDungeon = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInWorldEvent = new Config<>(true);

    @Persisted
    private final Config<Boolean> lockInWar = new Config<>(true);

    public LockContentBookFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSwing(ArmSwingEvent event) {
        if (shouldBlockHeldBook(event.getHand(), false)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUseItem(UseItemEvent event) {
        if (shouldBlockHeldBook(event.getHand(), true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (shouldBlockHeldBook(event.getHand(), true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent.Interact event) {
        if (shouldBlockHeldBook(event.getHand(), true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerClick(ContainerClickEvent event) {
        if (!isLocked() || forceUnlockAction.get().allowsInventoryClick()) return;
        if (event.getSlotNum() < 0
                || event.getSlotNum() >= event.getContainerMenu().slots.size()) return;

        Slot slot = event.getContainerMenu().getSlot(event.getSlotNum());
        if (slot.container instanceof Inventory
                && slot.getContainerSlot() == InventoryUtils.CONTENT_BOOK_SLOT_NUM
                && isContentBook(slot.getItem())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContentBookOpen(ContentBookOpenEvent event) {
        // Keybinds and menu buttons are neither inventory clicks nor shift-right-clicks.
        if (isLocked()) {
            event.setCanceled(true);
        }
    }

    private boolean shouldBlockHeldBook(InteractionHand hand, boolean rightClick) {
        if (!isLocked() || !isContentBook(McUtils.player().getItemInHand(hand))) return false;
        return !(rightClick
                && McUtils.player().isShiftKeyDown()
                && forceUnlockAction.get().allowsShiftRightClick());
    }

    private boolean isLocked() {
        if (!Models.WorldState.onWorld() || Models.WorldState.inCharacterWardrobe()) return false;

        return (lockInRaid.get() && Models.Raid.getCurrentRaid() != null)
                || (lockInDungeon.get() && Models.Dungeon.isInDungeon())
                || (lockInWorldEvent.get() && Models.WorldEvent.getCurrentWorldEvent() != null)
                || (lockInWar.get() && Models.War.isWarActive());
    }

    private static boolean isContentBook(ItemStack itemStack) {
        return StyledText.fromComponent(itemStack.getHoverName()).equals(CONTENT_BOOK_NAME);
    }

    private enum ForceUnlockAction {
        NONE,
        INVENTORY_CLICK,
        SHIFT_RIGHT_CLICK,
        INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK;

        private boolean allowsInventoryClick() {
            return this == INVENTORY_CLICK || this == INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK;
        }

        private boolean allowsShiftRightClick() {
            return this == SHIFT_RIGHT_CLICK || this == INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK;
        }

        @Override
        public String toString() {
            return switch (this) {
                case NONE -> "None";
                case INVENTORY_CLICK -> "Inventory Click";
                case SHIFT_RIGHT_CLICK -> "Shift Right Click";
                case INVENTORY_CLICK_AND_SHIFT_RIGHT_CLICK -> "Inventory Click & Shift Right Click";
            };
        }
    }
}
