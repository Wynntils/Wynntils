package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.utils.Delay;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.SMALL)
public class MountHorseHotkeyFeature extends Feature {

    private static final int searchRadius = 8;
    private static final int summonAttempts = 3;
    private static final int summonDelayTicks = 5;
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

        Entity horse = searchForHorseNearby();
        Player player = McUtils.player();
        if (horse == null) { // Horse has not spawned, we should do that
            int horseInventorySlot = findHorseInInventory();
            if (horseInventorySlot > 8 || horseInventorySlot == -1) {
                postHorseErrorMessage(MountHorseStatus.NO_HORSE);
                return;
            }
            System.out.println(horseInventorySlot);
            trySummonHorse(horseInventorySlot, summonAttempts);
        } else { // Horse already exists, mount it
            mountHorse(horse);
        }
    });

    private static void mountHorse(Entity horse) {
        // Horse should be nearby when this is called
        Player player = McUtils.player();

        // swap to soul points to avoid right click problems
        int prevItem = player.getInventory().selected;
        player.getInventory().selected = 8;
        McUtils.player().connection.send(ServerboundInteractPacket.createInteractionPacket(horse, false, InteractionHand.MAIN_HAND));
        player.getInventory().selected = prevItem;
    }

    private static void trySummonHorse(int horseInventorySlot, int attempts) {
        if (attempts <= 0) {
            postHorseErrorMessage(MountHorseStatus.NO_HORSE);
            return;
        }

        Player player = McUtils.player();
        int prevItem = McUtils.player().getInventory().selected;

        new Delay(() -> {
            player.getInventory().selected = horseInventorySlot;
            System.out.println("sending rclick to slot " + player.getInventory().selected + " with attempts " + attempts);
            McUtils.player().connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND));

            if (searchForHorseNearby() != null) {
                System.out.println("success on " + attempts);
                player.getInventory().selected = prevItem;
                mountNextHorseSpawn = true;
                return;
            }
            trySummonHorse(horseInventorySlot, attempts - 1);
        }, summonDelayTicks);
    }

    private static boolean isPlayersHorse(Entity horse, String playerName) {
        return (horse instanceof AbstractHorse) && isPlayersHorse(horse.getCustomName(), playerName);
    }

    private static boolean isPlayersHorse(Component horseName, String playerName) {
        String defaultName = "§f" + playerName + "§7" + "'s horse";
        String customSuffix = "§7" + " [" + playerName + "]";

        if (horseName == null) return false;

        return defaultName.equals(horseName.getString()) || horseName.getString().endsWith(customSuffix);
    }

    private static Entity searchForHorseNearby() {
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

    @SubscribeEvent
    public void onHorseDataReceived(PacketReceivedEvent<ClientboundSetEntityDataPacket> e) {
        System.out.println("evt f");
        mountNextHorseSpawn = true;
        if (!WynnUtils.onWorld() || !mountNextHorseSpawn) return;
        System.out.println("passed first return");
        int id = e.getPacket().getId();
        Entity entity = McUtils.mc().level.getEntity(id);
        System.out.println(entity.getDisplayName().getString());
        if (!(entity instanceof AbstractHorse) || e.getPacket().getUnpackedData().isEmpty()) return;

        Player player = McUtils.player();
        System.out.println(e.getPacket().getUnpackedData());
    }
}
