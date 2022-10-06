/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.wynn.item.ItemStackTransformModel;
import com.wynntils.wynn.item.UnidentifiedItemStack;
import com.wynntils.wynn.utils.ContainerUtils;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LootrunFunctions {
    public static class DryStreakFunction extends ActiveFunction<Integer> {
        private int dryCount = 0;

        @Override
        public Integer getValue(String argument) {
            return dryCount;
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }

        @Override
        public List<String> getAliases() {
            return List.of("dry_s");
        }

        @SubscribeEvent
        public void onOpen(ScreenOpenedEvent event) {
            if (ContainerUtils.isLootChest(event.getScreen())) {
                ContainerScreen containerScreen = (ContainerScreen) event.getScreen();

                for (ItemStack itemStack : containerScreen.getMenu().getItems()) {
                    if (itemStack instanceof UnidentifiedItemStack unidentifiedItemStack) {
                        if (unidentifiedItemStack.getItemTier().isEmpty()) continue;

                        if (unidentifiedItemStack.getItemTier().get() == ItemTier.MYTHIC) {
                            dryCount = 0;
                            return;
                        }
                    }
                }

                dryCount++;
            }
        }
    }

    public static class DryBoxesFunction extends ActiveFunction<Integer> {
        private int dryBoxes = 0;

        @Override
        public Integer getValue(String argument) {
            return dryBoxes;
        }

        @Override
        public List<Class<? extends Model>> getModelDependencies() {
            return List.of(ItemStackTransformModel.class);
        }

        @Override
        public List<String> getAliases() {
            return List.of("dry_b");
        }

        @SubscribeEvent
        public void onOpen(ScreenOpenedEvent event) {
            if (ContainerUtils.isLootChest(event.getScreen())) {
                ContainerScreen containerScreen = (ContainerScreen) event.getScreen();

                int dryBoxesIfNoMythic = 0;

                for (ItemStack itemStack : containerScreen.getMenu().getItems()) {
                    if (itemStack instanceof UnidentifiedItemStack unidentifiedItemStack) {
                        if (unidentifiedItemStack.getItemTier().isEmpty()) continue;

                        if (unidentifiedItemStack.getItemTier().get() == ItemTier.MYTHIC) {
                            dryBoxes = 0;
                            return;
                        } else {
                            dryBoxesIfNoMythic++;
                        }
                    }
                }

                dryBoxes = dryBoxesIfNoMythic;
            }
        }
    }
}
