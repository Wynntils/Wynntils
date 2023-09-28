/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.containers.type.SearchableContainerType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class HightlightDuplicateCosmeticsFeature extends Feature {
    @Persisted
    public final Config<HighlightCondition> highlightCondition = new Config<>(HighlightCondition.BOTH);

    @Persisted
    public final Config<CustomColor> hoveredHighlightColor = new Config<>(CommonColors.BLUE);

    @Persisted
    public final Config<CustomColor> selectedHighlightColor = new Config<>(CommonColors.ORANGE);

    private static final Component ADD_REWARD_TEXT = Component.literal("§7Click on a reward to add it");
    private static final Component RETURN_TEXT = Component.literal("§7Return to Scrap Menu");
    private static final int RETURN_SLOT = 18;
    private static final List<Integer> SELECTED_COSMETIC_SLOTS = List.of(1, 2, 3, 4, 5);

    private final List<Component> selectedCosmetics = new ArrayList<>();

    private boolean onScrapMenu = false;
    private Component hoveredCosmetic;

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        if (!(screen.getMenu() instanceof ChestMenu)) return;

        StyledText title = StyledText.fromComponent(screen.getTitle());

        SearchableContainerType searchableContainerType = SearchableContainerType.getContainerType(title);
        if (searchableContainerType != SearchableContainerType.SCRAP_MENU) return;

        onScrapMenu = true;
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent e) {
        selectedCosmetics.clear();
        onScrapMenu = false;
    }

    @SubscribeEvent
    public void onContainerRender(ContainerRenderEvent event) {
        if (!onScrapMenu) return;
        // No need to get hovered cosmetic if only displaying selected cosmetics
        if (highlightCondition.get() == HighlightCondition.SELECTED) return;

        Slot hoveredSlot = event.getHoveredSlot();

        // If no item or on new reward screen don't get a hovered cosmetic, hide §f too as the glass panes on new
        // reward screen are named that and the return text item isn't added immediately
        if (hoveredSlot == null
                || hoveredSlot.getItem().getItem() == Items.AIR
                || event.getScreen()
                        .getMenu()
                        .slots
                        .get(RETURN_SLOT)
                        .getItem()
                        .getHoverName()
                        .equals(RETURN_TEXT)
                || hoveredSlot.getItem().getHoverName().equals(Component.literal("§f"))) {
            hoveredCosmetic = null;
            return;
        }

        hoveredCosmetic = hoveredSlot.getItem().getHoverName();
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!onScrapMenu) return;

        // Don't highlight the cosmetics in the selected slots
        if (SearchableContainerType.SCRAP_MENU.getBounds().getSlots().contains(e.getSlot().index)) {
            if (selectedCosmetics.contains(e.getSlot().getItem().getHoverName())
                    && highlightCondition.get() != HighlightCondition.HOVER) {
                RenderUtils.drawArc(
                        e.getPoseStack(), selectedHighlightColor.get(), e.getSlot().x, e.getSlot().y, 200, 1f, 6, 8);
            } else if (e.getSlot().getItem().getHoverName().equals(hoveredCosmetic)
                    && highlightCondition.get() != HighlightCondition.SELECTED) {
                RenderUtils.drawArc(
                        e.getPoseStack(), hoveredHighlightColor.get(), e.getSlot().x, e.getSlot().y, 200, 1f, 6, 8);
            }
        }
    }

    @SubscribeEvent
    public void onContainerSetContent(ContainerSetContentEvent.Post event) {
        if (!onScrapMenu) return;

        // Cosmetics have been scrapped and new cosmetic is being displayed, clear selected
        if (event.getItems().get(RETURN_SLOT).getHoverName().equals(RETURN_TEXT)) {
            selectedCosmetics.clear();
            return;
        }

        // Update the selected cosmetics
        for (int slot : SELECTED_COSMETIC_SLOTS) {
            Component selectedCosmetic = event.getItems().get(slot).getHoverName();

            if (selectedCosmetic.equals(ADD_REWARD_TEXT)) {
                // If the first slot does not have a cosmetic then none are selected
                if (slot == 1) {
                    selectedCosmetics.clear();
                }

                break;
            }

            if (!selectedCosmetics.contains(selectedCosmetic)) {
                selectedCosmetics.add(selectedCosmetic);
            }
        }
    }

    private enum HighlightCondition {
        HOVER,
        SELECTED,
        BOTH
    }
}
