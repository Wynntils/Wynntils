/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.models.containers.containers.reward.RewardContainer;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class MythicBlockerFeature extends Feature {
    @SubscribeEvent
    public void onChestCloseAttempt(ContainerCloseEvent.Pre e) {
        if (!Models.WorldState.onWorld()) return;
        if (!(Models.Container.getCurrentContainer() instanceof RewardContainer)) return;

        NonNullList<ItemStack> items = ContainerUtils.getItems(McUtils.mc().screen);
        for (int i = 0; i < 27; i++) {
            ItemStack itemStack = items.get(i);
            Optional<GearTierItemProperty> tieredItem =
                    Models.Item.asWynnItemProperty(itemStack, GearTierItemProperty.class);
            if (tieredItem.isPresent() && tieredItem.get().getGearTier() == GearTier.MYTHIC) {
                McUtils.sendMessageToClient(Component.translatable("feature.wynntils.mythicBlocker.closingBlocked")
                        .withStyle(ChatFormatting.RED));
                e.setCanceled(true);
                return;
            }
        }
    }
}
