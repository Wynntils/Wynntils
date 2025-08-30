/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class MythicFoundFeature extends Feature {
    private static final ResourceLocation MYTHIC_FOUND_CLASSIC_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found-classic");
    private static final ResourceLocation MYTHIC_FOUND_MODERN_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found-modern");

    @Persisted
    private final Config<MythicSound> chestSound = new Config<>(MythicSound.MODERN);

    @Persisted
    private final Config<MythicSound> lootrunSound = new Config<>(MythicSound.MODERN);

    @Persisted
    private final Config<MythicSound> aspectFoundSound = new Config<>(MythicSound.MODERN);

    @Persisted
    private final Config<MythicSound> tomeFoundSound = new Config<>(MythicSound.NONE);

    @Persisted
    private final Config<Boolean> showDryStreakMessage = new Config<>(true);

    @Persisted
    private final Config<Boolean> showAspectDryStreakMessage = new Config<>(true);

    @Persisted
    private final Config<Boolean> showTomeDryStreakMessage = new Config<>(false);

    @SubscribeEvent
    public void onMythicFound(MythicFoundEvent event) {
        ItemStack itemStack = event.getMythicBoxItem();

        // Normal loot chest reward
        if (event.getMythicSource() == MythicFoundEvent.MythicSource.LOOT_CHEST
                && (showDryStreakMessage.get() || chestSound.get() != MythicSound.NONE)) {
            Optional<GearBoxItem> gearBoxItem = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
            if (gearBoxItem.isPresent()) {
                if (gearBoxItem.get().getGearType() != GearType.MASTERY_TOME) {
                    if (chestSound.get() != MythicSound.NONE) {
                        McUtils.playSoundAmbient(chestSound.get().getSoundEvent());
                    }

                    if (!showDryStreakMessage.get()) return;
                    sendNormalDryStreakMessage(
                            StyledText.fromComponent(event.getMythicBoxItem().getHoverName()));
                }
                return;
            }
        }

        // Lootrun rewards
        if (event.getMythicSource() == MythicFoundEvent.MythicSource.LOOTRUN_REWARD_CHEST
                && (showDryStreakMessage.get() || lootrunSound.get() != MythicSound.NONE)) {
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
                if (lootrunSound.get() != MythicSound.NONE) {
                    McUtils.playSoundAmbient(lootrunSound.get().getSoundEvent());
                }

                if (!showDryStreakMessage.get()) return;
                sendLootrunDryStreakMessage(
                        StyledText.fromComponent(event.getMythicBoxItem().getHoverName()));
            }
        }

        // Raid rewards
        if (event.getMythicSource() == MythicFoundEvent.MythicSource.RAID_REWARD_CHEST) {
            if (showAspectDryStreakMessage.get() || aspectFoundSound.get() != MythicSound.NONE) {
                Optional<AspectItem> aspectItem = Models.Item.asWynnItem(itemStack, AspectItem.class);
                if (aspectItem.isPresent()) {
                    if (aspectFoundSound.get() != MythicSound.NONE) {
                        McUtils.playSoundAmbient(aspectFoundSound.get().getSoundEvent());
                    }
                    if (showAspectDryStreakMessage.get()) {
                        sendAspectDryStreakMessage(StyledText.fromComponent(
                                event.getMythicBoxItem().getHoverName()));
                    }
                    return;
                }
            }

            if (showTomeDryStreakMessage.get() || tomeFoundSound.get() != MythicSound.NONE) {
                Optional<TomeItem> tomeItem = Models.Item.asWynnItem(itemStack, TomeItem.class);
                if (tomeItem.isPresent()) {
                    if (tomeFoundSound.get() != MythicSound.NONE) {
                        McUtils.playSoundAmbient(tomeFoundSound.get().getSoundEvent());
                    }
                    if (showTomeDryStreakMessage.get()) {
                        sendTomeDryStreakMessage(StyledText.fromComponent(
                                event.getMythicBoxItem().getHoverName()));
                    }
                    return;
                }
            }
        }
    }

    private void sendLootrunDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found an ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(Models.Lootrun.dryPulls.get() + " pulls")
                                .withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" without a mythic.").withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    private static void sendNormalDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found an ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" in chest ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal("#" + Models.LootChest.getOpenedChestCount())
                                .withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(String.valueOf(Models.LootChest.getDryCount()))
                                .withStyle(ChatFormatting.DARK_PURPLE)))
                .append(Component.literal(" dry chests and "))
                .append(Component.literal(String.valueOf(Models.LootChest.getDryBoxes()))
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(" dry boxes.")));
    }

    private static void sendAspectDryStreakMessage(StyledText itemName) {
        sendRaidDryStreakMessage(
                itemName,
                Models.Raid.getRaidsWithoutMythicAspect(),
                Models.Raid.getAspectPullsWithoutMythicAspect(),
                "aspect");
    }

    private static void sendTomeDryStreakMessage(StyledText itemName) {
        sendRaidDryStreakMessage(
                itemName,
                Models.Raid.getRaidsWithoutMythicTome(),
                Models.Raid.getRewardPullsWithoutMythicTome(),
                "reward");
    }

    private static void sendRaidDryStreakMessage(StyledText itemName, int numRaids, int numPulls, String pullType) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found ")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(itemName.getComponent())
                .append(Component.literal(" after ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(numRaids + " raids").withStyle(ChatFormatting.GOLD)))
                .append(Component.literal(" and ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(numPulls + " " + pullType + " pulls").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" without a mythic."))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private enum MythicSound {
        MODERN(SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_MODERN_ID)),
        CLASSIC(SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_CLASSIC_ID)),
        NONE(null);

        private final SoundEvent soundEvent;

        MythicSound(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
        }

        public SoundEvent getSoundEvent() {
            return soundEvent;
        }
    }
}
