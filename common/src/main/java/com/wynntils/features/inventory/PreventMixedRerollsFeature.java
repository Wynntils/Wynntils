/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.google.common.primitives.Ints;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GameItem;
import com.wynntils.models.items.items.gui.IngredientPouchItem;
import com.wynntils.models.items.properties.UnidentifiedItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class PreventMixedRerollsFeature extends Feature {
    private static final String ITEM_IDENTIFIER_NAME = "\udaff\udff8\ue018";
    private static final int[] ITEM_IDENTIFIER_SLOTS = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
    private static final String EMPTY_ITEM_SLOT_NAME = "§8§lEmpty Item Slot";

    private State state = State.NOT_OPEN;

    @SubscribeEvent
    public void onMenuOpen(MenuEvent.MenuOpenedEvent.Post event) {
        if (event.getTitle().getString().equals(ITEM_IDENTIFIER_NAME)) {
            state = State.NO_FILTER;
        } else {
            state = State.NOT_OPEN;
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        state = State.NOT_OPEN;
    }

    @SubscribeEvent
    public void onUpdateContents(ContainerSetContentEvent.Post event) {
        if (state == State.NOT_OPEN) return;

        AbstractContainerMenu menu = McUtils.containerMenu();
        if (menu.containerId != event.getContainerId()) return;

        updateState(menu);
    }

    @SubscribeEvent
    public void onUpdateSlot(ContainerSetSlotEvent.Post event) {
        if (state == State.NOT_OPEN) return;

        AbstractContainerMenu menu = McUtils.containerMenu();
        if (menu.containerId != event.getContainerId() || Ints.indexOf(ITEM_IDENTIFIER_SLOTS, event.getSlot()) == -1)
            return;

        updateState(menu);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSlotClick(ContainerClickEvent event) {
        if (state == State.NOT_OPEN || state == State.NO_FILTER) return;

        AbstractContainerMenu menu = event.getContainerMenu();
        int slotNum = event.getSlotNum();
        if (slotNum < 0
                || slotNum >= menu.slots.size()
                || !isItemFiltered(menu.getSlot(slotNum).getItem())) return;
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSlotRender(SlotRenderEvent.CountPre event) {
        if (state == State.NOT_OPEN || state == State.NO_FILTER) return;

        Slot slot = event.getSlot();
        if (!isItemFiltered(slot.getItem())) return;

        BufferedRenderUtils.drawRect(
                event.getPoseStack(),
                event.getGuiGraphics().bufferSource(),
                CommonColors.BLACK.withAlpha(0.5f),
                slot.x - 1,
                slot.y - 1,
                500,
                18f,
                18f);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderItemTooltip(ItemTooltipRenderEvent.Pre event) {
        if (state == State.NOT_OPEN || state == State.NO_FILTER || !isItemFiltered(event.getItemStack())) return;

        List<Component> newTooltips = new ArrayList<>(event.getTooltips());
        newTooltips.add(Component.empty());
        newTooltips.add(Component.translatable("feature.wynntils.preventMixedRerolls.blocked")
                .withStyle(ChatFormatting.RED));
        event.setTooltips(newTooltips);
    }

    private void updateState(AbstractContainerMenu menu) {
        for (int slotNum : ITEM_IDENTIFIER_SLOTS) {
            Optional<UnidentifiedItemProperty> unidItemOpt =
                    Models.Item.asWynnItemProperty(menu.getSlot(slotNum).getItem(), UnidentifiedItemProperty.class);
            if (unidItemOpt.isPresent()) {
                state = unidItemOpt.get().isUnidentified() ? State.UNIDENTIFIED_ONLY : State.IDENTIFIED_ONLY;
                return;
            }
        }
        state = State.NO_FILTER;
    }

    private boolean isItemFiltered(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;

        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return false;

        WynnItem wynnItem = wynnItemOpt.get();
        if (wynnItem instanceof UnidentifiedItemProperty unidItem) {
            return switch (state) {
                case UNIDENTIFIED_ONLY -> !unidItem.isUnidentified();
                case IDENTIFIED_ONLY -> unidItem.isUnidentified();
                default -> false;
            };
        } else {
            return wynnItem instanceof GameItem || wynnItem instanceof IngredientPouchItem;
        }
    }

    private enum State {
        NOT_OPEN,
        NO_FILTER,
        UNIDENTIFIED_ONLY,
        IDENTIFIED_ONLY
    }
}
