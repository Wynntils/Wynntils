/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class MythicFoundFeature extends Feature {
    private static final ResourceLocation MYTHIC_FOUND_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found");
    private static final SoundEvent MYTHIC_FOUND_SOUND = SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_ID);

    @Persisted
    public final Config<Boolean> playSound = new Config<>(true);

    @Persisted
    public final Config<Boolean> showDryStreakMessage = new Config<>(true);

    @SubscribeEvent
    public void onMythicFound(MythicFoundEvent event) {
        if (playSound.get()) {
            McUtils.playSoundAmbient(MYTHIC_FOUND_SOUND);
        }

        if (!showDryStreakMessage.get()) return;

        ItemStack itemStack = event.getMythicBoxItem();

        // Normal loot chest reward
        Optional<GearBoxItem> gearBoxItemOptional = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (gearBoxItemOptional.isPresent()) {
            GearBoxItem gearBoxItem = gearBoxItemOptional.get();

            if (gearBoxItem.getGearType() != GearType.MASTERY_TOME) {
                sendNormalDryStreakMessage(itemStack.getHoverName());
            }
            return;
        }

        // Lootrun rewards
        boolean validLootrunMythic = false;
        Optional<GearItem> gearItem = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItem.isPresent()) {
            validLootrunMythic = true;
        }

        Optional<InsulatorItem> insulatorItem = Models.Item.asWynnItem(itemStack, InsulatorItem.class);
        if (insulatorItem.isPresent()) {
            validLootrunMythic = true;
        }

        Optional<SimulatorItem> simulatorItem = Models.Item.asWynnItem(itemStack, SimulatorItem.class);
        if (simulatorItem.isPresent()) {
            validLootrunMythic = true;
        }

        if (validLootrunMythic) {
            sendLootrunDryStreakMessage(ComponentUtils.createItemChatComponent(gearItem.get()));
        }
    }

    private void sendLootrunDryStreakMessage(Component itemName) {
        McUtils.sendMessageToClient(Component.empty()
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.translatable(
                                "feature.wynntils.mythicFound.lootrunDryStreakBroken",
                                itemName,
                                Component.translatable(
                                                "feature.wynntils.mythicFound.lootrunDryPulls",
                                                String.format(Locale.ROOT, "%,d", Models.Lootrun.dryPulls.get()))
                                        .withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    private static void sendNormalDryStreakMessage(Component itemName) {
        McUtils.sendMessageToClient(Component.translatable(
                        "feature.wynntils.mythicFound.lootChestDryStreakBroken",
                        itemName,
                        Component.translatable(
                                        "feature.wynntils.mythicFound.lootChestMythicChestNumber",
                                        String.format(Locale.ROOT, "%,d", Models.LootChest.getOpenedChestCount()))
                                .withStyle(ChatFormatting.GOLD),
                        Component.translatable(
                                        "feature.wynntils.mythicFound.lootChestDryChests",
                                        String.format(Locale.ROOT, "%,d", Models.LootChest.getDryCount()))
                                .withStyle(ChatFormatting.DARK_PURPLE),
                        Component.translatable(
                                        "feature.wynntils.mythicFound.lootChestDryBoxes",
                                        String.format(Locale.ROOT, "%,d", Models.LootChest.getDryBoxes()))
                                .withStyle(ChatFormatting.DARK_PURPLE))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
