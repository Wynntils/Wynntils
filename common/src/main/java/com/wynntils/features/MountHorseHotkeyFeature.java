/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.utils.Delay;
import com.wynntils.wc.utils.WynnItemMatchers;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class MountHorseHotkeyFeature extends FeatureBase {

    private static final int SEARCH_RADIUS = 6; // Furthest blocks away from which we can interact with a horse
    private static final int SUMMON_ATTEMPTS = 8;
    private static final int SUMMON_DELAY_TICKS = 5;

    private static int prevItem = -1;
    private static boolean alreadySetPrevItem = false;

    private final KeyHolder mountHorseKeybind = new KeyHolder(
            "Mount Horse", GLFW.GLFW_KEY_R, "Wynntils", true, MountHorseHotkeyFeature::onMountHorseKeyPress);

    public MountHorseHotkeyFeature() {
        setupKeyHolder(mountHorseKeybind);
    }

    private static void onMountHorseKeyPress() {
        if (McUtils.player().getVehicle() != null) {
            postHorseErrorMessage(MountHorseStatus.ALREADY_RIDING);
            return;
        }

        Entity horse = searchForHorseNearby();
        if (horse == null) { // Horse has not spawned, we should do that
            int horseInventorySlot = findHorseInInventory();
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
    private static void mountHorse(Entity horse) {
        Player player = McUtils.player();

        // swap to soul points to avoid right click problems
        int prevItem = player.getInventory().selected;
        McUtils.player().connection.send(new ServerboundSetCarriedItemPacket(8));
        McUtils.player()
                .connection
                .send(ServerboundInteractPacket.createInteractionPacket(horse, false, InteractionHand.MAIN_HAND));
        McUtils.player().connection.send(new ServerboundSetCarriedItemPacket(prevItem));
    }

    private static void trySummonAndMountHorse(int horseInventorySlot, int attempts) {
        if (attempts <= 0) {
            postHorseErrorMessage(MountHorseStatus.NO_HORSE);
            return;
        }

        Player player = McUtils.player();
        if (!alreadySetPrevItem) {
            prevItem = player.getInventory().selected;
            alreadySetPrevItem = true;
        }

        new Delay(
                () -> {
                    Entity horse = searchForHorseNearby();
                    if (isPlayersHorse(horse)) { // Horse successfully summoned
                        McUtils.player().connection.send(new ServerboundSetCarriedItemPacket(prevItem));
                        alreadySetPrevItem = false;
                        mountHorse(horse);
                        return;
                    }
                    McUtils.player().connection.send(new ServerboundSetCarriedItemPacket(horseInventorySlot));
                    McUtils.player().connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));

                    trySummonAndMountHorse(horseInventorySlot, attempts - 1);
                },
                SUMMON_DELAY_TICKS);
    }

    private static boolean isPlayersHorse(Entity horse) {
        if (!(horse instanceof AbstractHorse)) return false;

        String playerName = McUtils.player().getName().getString();
        String defaultName = "§f" + playerName + "§7" + "'s horse";
        String customNameSuffix = "§7" + " [" + playerName + "]";

        Component horseName = horse.getCustomName();
        if (horseName == null) return false;

        return defaultName.equals(horseName.getString())
                || horseName.getString().endsWith(customNameSuffix);
    }

    private static Entity searchForHorseNearby() {
        Player player = McUtils.player();

        List<AbstractHorse> horses = McUtils.mc()
                .level
                .getEntitiesOfClass(
                        AbstractHorse.class,
                        new AABB(
                                player.getX() - SEARCH_RADIUS,
                                player.getY() - SEARCH_RADIUS,
                                player.getZ() - SEARCH_RADIUS,
                                player.getX() + SEARCH_RADIUS,
                                player.getY() + SEARCH_RADIUS,
                                player.getZ() + SEARCH_RADIUS));

        for (AbstractHorse h : horses) {
            if (isPlayersHorse(h)) {
                return h;
            }
        }
        return null;
    }

    private static int findHorseInInventory() {
        Player player = McUtils.player();
        for (int i = 0; i <= 44; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (WynnItemMatchers.isHorse(stack)) {
                return i;
            }
        }
        return -1;
    }

    private static void postHorseErrorMessage(MountHorseStatus status) {
        McUtils.sendMessageToClient(new TranslatableComponent(status.getTcString()).withStyle(ChatFormatting.DARK_RED));
    }

    private enum MountHorseStatus {
        NO_HORSE("feature.wynntils.mountHorseHotkey.noHorse"),
        ALREADY_RIDING("feature.wynntils.mountHorseHotkey.alreadyRiding");

        private final String tcString;

        MountHorseStatus(String tcString) {
            this.tcString = tcString;
        }

        public String getTcString() {
            return this.tcString;
        }
    }
}
