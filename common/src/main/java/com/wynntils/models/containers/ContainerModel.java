/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.features.statemanaged.DataStorageFeature;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.WynnItemMatchers;
import com.wynntils.utils.wynn.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ContainerModel extends Model {
    private static final Pattern ABILITY_TREE_PATTERN =
            Pattern.compile("(?:Warrior|Shaman|Mage|Assassin|Archer) Abilities");

    private static final Pattern LOOT_CHEST_PATTERN = Pattern.compile("Loot Chest (.+)");

    private static final int LOOT_CHEST_ITEM_COUNT = 27;
    private int nextExpectedLootContainerId = -2;

    public boolean isAbilityTreeScreen(Screen screen) {
        return ABILITY_TREE_PATTERN.matcher(screen.getTitle().getString()).matches();
    }

    public boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen && lootChestMatcher(screen).matches();
    }

    public boolean isLootChest(String title) {
        return title.startsWith("Loot Chest");
    }

    public boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?>)) return false;

        String title = screen.getTitle().getString();
        return isLootChest(title) || title.startsWith("Daily Rewards") || title.contains("Objective Rewards");
    }

    public Matcher lootChestMatcher(Screen screen) {
        return LOOT_CHEST_PATTERN.matcher(
                WynnUtils.normalizeBadString(ComponentUtils.getUnformatted(screen.getTitle())));
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isLootChest(ComponentUtils.getUnformatted(event.getTitle()))) {
            nextExpectedLootContainerId = event.getContainerId();

            DataStorageFeature.INSTANCE.dryCount++;
            Managers.Config.saveConfig();
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        if (!WynnItemMatchers.isGearBox(itemStack)) return;

        GearTier gearTier = GearTier.fromComponent(itemStack.getHoverName());

        if (gearTier == GearTier.MYTHIC) {
            DataStorageFeature.INSTANCE.dryBoxes = 0;
            DataStorageFeature.INSTANCE.dryCount = 0;
        } else {
            DataStorageFeature.INSTANCE.dryBoxes += 1;
        }

        Managers.Config.saveConfig();
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }
}
