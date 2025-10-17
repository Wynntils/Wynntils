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
import com.wynntils.models.containers.event.ValuableFoundEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.AspectItem;
import com.wynntils.models.items.items.game.CorruptedCacheItem;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public class ValuableFoundFeature extends Feature {
    private static final ResourceLocation MYTHIC_FOUND_CLASSIC_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found-classic");
    private static final ResourceLocation MYTHIC_FOUND_MODERN_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.mythic-found-modern");
    private static final ResourceLocation CACHE_FOUND_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "misc.cache-found");

    @Persisted
    private final Config<ValuableFoundSound> chestSound = new Config<>(ValuableFoundSound.MODERN);

    @Persisted
    private final Config<ValuableFoundSound> lootrunSound = new Config<>(ValuableFoundSound.MODERN);

    @Persisted
    private final Config<ValuableFoundSound> aspectFoundSound = new Config<>(ValuableFoundSound.MODERN);

    @Persisted
    private final Config<ValuableFoundSound> tomeFoundSound = new Config<>(ValuableFoundSound.NONE);

    @Persisted
    private final Config<ValuableFoundSound> cacheFoundSound = new Config<>(ValuableFoundSound.CACHE);

    @Persisted
    private final Config<ValuableFoundSound> emeraldPouchSound = new Config<>(ValuableFoundSound.NONE);

    @Persisted
    private final Config<Boolean> showDryStreakMessage = new Config<>(true);

    @Persisted
    private final Config<Boolean> showAspectDryStreakMessage = new Config<>(true);

    @Persisted
    private final Config<Boolean> showTomeDryStreakMessage = new Config<>(false);

    @Persisted
    private final Config<Boolean> showCacheDryStreakMessage = new Config<>(true);

    @Persisted
    private final Config<Boolean> showEmeraldPouchDryStreakMessage = new Config<>(true);

    @Persisted
    private final Config<EmeraldPouchTier> emeraldPouchTier = new Config<>(EmeraldPouchTier.EIGHT);

    @Persisted
    private final Config<Float> soundVolume = new Config<>(1.0f);

    @Persisted
    private final Config<Float> soundPitch = new Config<>(1.0f);

    @SubscribeEvent
    public void onValuableFound(ValuableFoundEvent event) {
        ItemStack itemStack = event.getItem();

        // Normal loot chest reward
        if (event.getItemSource() == ValuableFoundEvent.ItemSource.LOOT_CHEST) {
            if (showDryStreakMessage.get() || chestSound.get() != ValuableFoundSound.NONE) {
                Optional<GearBoxItem> gearBoxItem = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
                if (gearBoxItem.isPresent()) {
                    if (gearBoxItem.get().getGearType() != GearType.MASTERY_TOME) {
                        if (chestSound.get() != ValuableFoundSound.NONE) {
                            McUtils.playSoundAmbient(
                                    chestSound.get().getSoundEvent(), soundVolume.get(), soundPitch.get());
                        }

                        if (!showDryStreakMessage.get()) return;
                        sendNormalDryStreakMessage(
                                StyledText.fromComponent(event.getItem().getHoverName()));
                    }
                    return;
                }
            }

            if (emeraldPouchTier.get() != EmeraldPouchTier.NONE
                    && (showEmeraldPouchDryStreakMessage.get() || emeraldPouchSound.get() != ValuableFoundSound.NONE)) {
                Optional<EmeraldPouchItem> emeraldPouchItem = Models.Item.asWynnItem(itemStack, EmeraldPouchItem.class);
                if (emeraldPouchItem.isPresent()
                        && emeraldPouchItem.get().getTier()
                                >= emeraldPouchTier.get().getTier()) {
                    if (emeraldPouchSound.get() != ValuableFoundSound.NONE) {
                        McUtils.playSoundAmbient(
                                emeraldPouchSound.get().getSoundEvent(), soundVolume.get(), soundPitch.get());
                    }

                    if (!showEmeraldPouchDryStreakMessage.get()) return;
                    sendEmeraldPouchDryStreakMessage(
                            StyledText.fromComponent(event.getItem().getHoverName()),
                            emeraldPouchTier.get().getTier());
                }

                return;
            }
        }

        // Lootrun rewards
        if (event.getItemSource() == ValuableFoundEvent.ItemSource.LOOTRUN_REWARD_CHEST
                && (showDryStreakMessage.get() || lootrunSound.get() != ValuableFoundSound.NONE)) {
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
                if (lootrunSound.get() != ValuableFoundSound.NONE) {
                    McUtils.playSoundAmbient(lootrunSound.get().getSoundEvent(), soundVolume.get(), soundPitch.get());
                }

                if (!showDryStreakMessage.get()) return;
                sendLootrunDryStreakMessage(
                        StyledText.fromComponent(event.getItem().getHoverName()));
            }
        }

        // Raid rewards
        if (event.getItemSource() == ValuableFoundEvent.ItemSource.RAID_REWARD_CHEST) {
            if (showAspectDryStreakMessage.get() || aspectFoundSound.get() != ValuableFoundSound.NONE) {
                Optional<AspectItem> aspectItem = Models.Item.asWynnItem(itemStack, AspectItem.class);
                if (aspectItem.isPresent()) {
                    if (aspectFoundSound.get() != ValuableFoundSound.NONE) {
                        McUtils.playSoundAmbient(
                                aspectFoundSound.get().getSoundEvent(), soundVolume.get(), soundPitch.get());
                    }
                    if (showAspectDryStreakMessage.get()) {
                        sendAspectDryStreakMessage(
                                StyledText.fromComponent(event.getItem().getHoverName()));
                    }
                    return;
                }
            }

            if (showTomeDryStreakMessage.get() || tomeFoundSound.get() != ValuableFoundSound.NONE) {
                Optional<TomeItem> tomeItem = Models.Item.asWynnItem(itemStack, TomeItem.class);
                if (tomeItem.isPresent()) {
                    if (tomeFoundSound.get() != ValuableFoundSound.NONE) {
                        McUtils.playSoundAmbient(
                                tomeFoundSound.get().getSoundEvent(), soundVolume.get(), soundPitch.get());
                    }
                    if (showTomeDryStreakMessage.get()) {
                        sendTomeDryStreakMessage(
                                StyledText.fromComponent(event.getItem().getHoverName()));
                    }
                    return;
                }
            }
        }

        // World Event Cache
        if (event.getItemSource() == ValuableFoundEvent.ItemSource.WORLD_EVENT) {
            if (showCacheDryStreakMessage.get() || cacheFoundSound.get() != ValuableFoundSound.NONE) {
                Optional<CorruptedCacheItem> cacheItem = Models.Item.asWynnItem(itemStack, CorruptedCacheItem.class);
                if (cacheItem.isPresent()) {
                    if (cacheFoundSound.get() != ValuableFoundSound.NONE) {
                        McUtils.playSoundAmbient(
                                cacheFoundSound.get().getSoundEvent(), soundVolume.get(), soundPitch.get());
                    }
                    if (showCacheDryStreakMessage.get()) {
                        sendCacheDryStreakMessage(
                                StyledText.fromComponent(event.getItem().getHoverName()));
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

    private static void sendCacheDryStreakMessage(StyledText itemName) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found a ")
                .withColor(CustomColor.fromHexString("#800080").asInt())
                .append(itemName.getComponent())
                .append(Component.literal(" after ")
                        .withColor(CustomColor.fromHexString("#800080").asInt())
                        .append(Component.literal(String.valueOf(Models.WorldEvent.dryAnnihilations.get()))
                                .withColor(CustomColor.fromHexString("#FFD700").asInt())))
                .append(Component.literal(" dry Annihilations.")
                        .withColor(CustomColor.fromHexString("#800080").asInt())));
    }

    private static void sendEmeraldPouchDryStreakMessage(StyledText itemName, int tier) {
        McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found an ")
                .withColor(CustomColor.fromHexString("#7CFC00").asInt())
                .append(itemName.getComponent())
                .append(Component.literal(" in chest ")
                        .withColor(CustomColor.fromHexString("#7CFC00").asInt())
                        .append(Component.literal("#" + Models.LootChest.getOpenedChestCount())
                                .withStyle(ChatFormatting.DARK_AQUA)))
                .append(Component.literal(" after ")
                        .withColor(CustomColor.fromHexString("#7CFC00").asInt())
                        .append(Component.literal(String.valueOf(Models.LootChest.getDryPouchCount(tier)))
                                .withColor(CustomColor.fromHexString("#228B22").asInt())))
                .append(Component.literal(" dry chests.")));
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

    private enum ValuableFoundSound {
        MODERN(SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_MODERN_ID)),
        CLASSIC(SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_CLASSIC_ID)),
        CACHE(SoundEvent.createVariableRangeEvent(CACHE_FOUND_ID)),
        NONE(null);

        private final SoundEvent soundEvent;

        ValuableFoundSound(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
        }

        public SoundEvent getSoundEvent() {
            return soundEvent;
        }
    }

    private enum EmeraldPouchTier {
        NONE(-1),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10);

        private final int tier;

        EmeraldPouchTier(int tier) {
            this.tier = tier;
        }

        public int getTier() {
            return tier;
        }
    }
}
