/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.items.items.game.HorseItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.COMBAT)
public class HorseMountFeature extends Feature {
    private static final ResourceLocation HORSE_WHISTLE_ID = new ResourceLocation("wynntils:horse.whistle");
    private static final SoundEvent HORSE_WHISTLE_SOUND = SoundEvent.createVariableRangeEvent(HORSE_WHISTLE_ID);

    private static final int SEARCH_RADIUS = 6; // Furthest blocks away from which we can interact with a horse
    private static final int SUMMON_ATTEMPTS = 8;
    private static final int SUMMON_DELAY_TICKS = 6;

    private static final StyledText MSG_NO_SPACE = StyledText.fromString("§4There is no room for a horse.");
    private static final StyledText MSG_TOO_MANY_MOBS =
            StyledText.fromString("§dYour horse is scared to come out right now, too many mobs are nearby.");
    private static final StyledText MSG_HORSE_UNAVAILABLE =
            StyledText.fromString("§4You cannot interact with your horse at the moment.");
    private static final StyledText MSG_HORSE_NOT_ALLOWED = StyledText.fromString("§4You cannot use your horse here!");

    private int prevItem = -1;
    private boolean alreadySetPrevItem = false;
    private boolean cancelMountingHorse = false;

    @RegisterKeyBind
    private final KeyBind mountHorseKeyBind = new KeyBind("Mount Horse", GLFW.GLFW_KEY_R, true, this::mountHorse);

    @Persisted
    public final Config<Boolean> guaranteedMount = new Config<>(true);

    @Persisted
    public final Config<Boolean> playWhistle = new Config<>(true);

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (!guaranteedMount.get()) return;

        ItemStack itemStack = McUtils.player().getMainHandItem();
        Optional<HorseItem> horseItemOpt = Models.Item.asWynnItem(itemStack, HorseItem.class);
        if (horseItemOpt.isEmpty()) return;

        mountHorse();
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) // this needs to run before ChatRedirectFeature cancels the event
    public void onChatReceived(ChatMessageReceivedEvent e) {
        StyledText message = e.getOriginalStyledText();

        if (message.equals(MSG_NO_SPACE)
                || message.equals(MSG_TOO_MANY_MOBS)
                || message.equals(MSG_HORSE_UNAVAILABLE)
                || message.equals(MSG_HORSE_NOT_ALLOWED)) {
            cancelMountingHorse = true;
        }
    }

    private void mountHorse() {
        if (!Models.WorldState.onWorld()) return;

        LocalPlayer player = McUtils.player();
        if (player.getVehicle() != null) {
            postHorseErrorMessage(MountHorseStatus.ALREADY_RIDING);
            return;
        }

        AbstractHorse horse = Models.Horse.searchForHorseNearby(player, SEARCH_RADIUS);
        if (horse == null) { // Horse has not spawned, we should do that
            int horseInventorySlot = Models.Horse.findHorseSlotNum();
            if (horseInventorySlot > 8 || horseInventorySlot == -1) {
                postHorseErrorMessage(MountHorseStatus.NO_HORSE);
                return;
            }
            trySummonAndMountHorse(horseInventorySlot, SUMMON_ATTEMPTS);
        } else { // Horse already exists, mount it
            mountHorse(horse);
        }
    }

    /** Horse should be nearby when this is called */
    private void mountHorse(Entity horse) {
        if (playWhistle.get()) {
            McUtils.playSoundAmbient(HORSE_WHISTLE_SOUND);
        }

        // swap to soul points to avoid right click problems
        int prevItem = McUtils.inventory().selected;
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(InventoryUtils.SOUL_POINTS_SLOT_NUM));
        McUtils.sendPacket(ServerboundInteractPacket.createInteractionPacket(horse, false, InteractionHand.MAIN_HAND));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
    }

    private void trySummonAndMountHorse(int horseInventorySlot, int attempts) {
        if (attempts <= 0) {
            postHorseErrorMessage(MountHorseStatus.NO_HORSE);
            return;
        }

        if (cancelMountingHorse) {
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
            cancelMountingHorse = false;
            return;
        }

        if (!alreadySetPrevItem) {
            prevItem = McUtils.inventory().selected;
            alreadySetPrevItem = true;
        }

        Managers.TickScheduler.scheduleLater(
                () -> {
                    LocalPlayer player = McUtils.player();
                    if (player == null) return;

                    AbstractHorse horse = Models.Horse.searchForHorseNearby(player, SEARCH_RADIUS);
                    if (horse != null) { // Horse successfully summoned
                        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(prevItem));
                        alreadySetPrevItem = false;
                        mountHorse(horse);
                        return;
                    }
                    McUtils.sendPacket(new ServerboundSetCarriedItemPacket(horseInventorySlot));
                    McUtils.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id));

                    trySummonAndMountHorse(horseInventorySlot, attempts - 1);
                },
                SUMMON_DELAY_TICKS);
    }

    private void postHorseErrorMessage(MountHorseStatus status) {
        Managers.Notification.queueMessage(
                Component.translatable(status.getTcString()).withStyle(ChatFormatting.DARK_RED));
    }

    private enum MountHorseStatus {
        NO_HORSE("feature.wynntils.horseMount.noHorse"),
        ALREADY_RIDING("feature.wynntils.horseMount.alreadyRiding");

        private final String tcString;

        MountHorseStatus(String tcString) {
            this.tcString = tcString;
        }

        private String getTcString() {
            return this.tcString;
        }
    }
}
