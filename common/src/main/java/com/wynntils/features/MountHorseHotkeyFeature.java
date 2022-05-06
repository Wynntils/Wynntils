package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class MountHorseHotkeyFeature extends Feature {

    private static final int searchRadius = 18;
    public static boolean mountNextHorseSpawn = false;

    public enum MountHorseStatus {
        NO_HORSE,
        ALREADY_RIDING,
    }

    private final KeyHolder mountHorseKeybind = new KeyHolder("Mount Horse", GLFW.GLFW_KEY_R, "Wynntils", true, () -> {
        if (McUtils.player().getVehicle() != null) {
            postHorseErrorMessage(MountHorseStatus.ALREADY_RIDING);
            return;
        }

        Entity horse = searchForNearbyHorses();
        Player player = McUtils.player();
        if (horse == null) { // Horse has not spawned, we should do that
            int horseInventorySlot = findHorseInInventory();
            if (horseInventorySlot > 8 || horseInventorySlot == -1) {
                postHorseErrorMessage(MountHorseStatus.NO_HORSE);
            }
            int prevItem = McUtils.player().getInventory().selected;
            player.getInventory().selected = horseInventorySlot;
            McUtils.player().connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));
            player.getInventory().selected = prevItem;
            mountNextHorseSpawn = true;
        } else { // Horse already exists, mount it
            mountHorse(horse);
        }
    });

    public static MountHorseStatus mountHorse(Entity horse) {
        // Horse should be nearby when this is called
        Player player = McUtils.player();

        // swap to soul points to avoid right click problems
        int prevItem = player.getInventory().selected;
        player.getInventory().selected = 8;
        player.interactOn(horse, InteractionHand.MAIN_HAND);
        player.getInventory().selected = prevItem;
    }

    public static boolean isPlayersHorse(Entity horse, String playerName) {
        return (horse instanceof AbstractHorse) && isPlayersHorse(horse.getCustomName(), playerName);
    }

    public static boolean isPlayersHorse(Component horseName, String playerName) {
        String defaultName = "§f" + playerName + "§7" + "'s horse";
        String customSuffix = "§7" + " [" + playerName + "]";

        return defaultName.equals(horseName.getString()) || horseName.getString().endsWith(customSuffix);
    }

    private static Entity searchForNearbyHorses() {
        Player player = McUtils.player();

        List<AbstractHorse> horses = McUtils.mc().level.getEntitiesOfClass(AbstractHorse.class, new AABB(
                player.getX() - searchRadius, player.getY() - searchRadius, player.getZ() - searchRadius,
                player.getX() + searchRadius, player.getY() + searchRadius, player.getZ() + searchRadius
        ));

        for (AbstractHorse h : horses) {
            if (isPlayersHorse(h, player.getName().getString())) {
                return h;
            }
        }
        return null;
    }

    private static int findHorseInInventory() {
        Player player = McUtils.player();
        for (int i = 0; i <= 44; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.SADDLE && stack.getDisplayName().getString().contains("Horse")) {
                return i;
            }
        }
        return -1;
    }

    private static void postHorseErrorMessage(MountHorseStatus status) {
        switch (status) {
            case NO_HORSE:
                McUtils.sendMessageToClient(new TranslatableComponent("feature.wynntils.mountHorseHotkey.noHorse").withStyle(ChatFormatting.DARK_RED));
            case ALREADY_RIDING:
                McUtils.sendMessageToClient(new TranslatableComponent("feature.wynntils.mountHorseHotkey.alreadyRiding").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.mountHorseHotkey.name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        KeyManager.registerKeybind(mountHorseKeybind);
        return true;
    }

    @Override
    protected void onDisable() {
        KeyManager.unregisterKeybind(mountHorseKeybind);
    }
}
