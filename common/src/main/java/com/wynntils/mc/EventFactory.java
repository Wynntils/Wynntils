/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.*;
import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent;
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogInEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogOutEvent;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.PlayerUpdate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

/** Creates events from mixins and platform dependent hooks */
public class EventFactory {
    private static void post(Event event) {
        WynntilsMod.getEventBus().post(event);
    }

    public static void onScreenCreated(Screen screen, Consumer<AbstractWidget> addButton) {
        if (screen instanceof TitleScreen titleScreen) {
            post(new TitleScreenInitEvent(titleScreen, addButton));
        } else if (screen instanceof PauseScreen gameMenuScreen) {
            post(new GameMenuInitEvent(gameMenuScreen, addButton));
        }
    }

    public static void onScreenOpened(Screen screen) {
        post(new ScreenOpenedEvent(screen));
    }

    public static void onWebSetup() {
        post(new WebSetupEvent());
    }

    public static void onInventoryRender(
            Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks, Slot hoveredSlot) {
        post(new InventoryRenderEvent(screen, poseStack, mouseX, mouseY, partialTicks, hoveredSlot));
    }

    public static void onPlayerInfoPacket(ClientboundPlayerInfoPacket packet) {
        Action action = packet.getAction();
        List<PlayerUpdate> entries = packet.getEntries();

        if (action == Action.UPDATE_DISPLAY_NAME) {
            for (PlayerUpdate entry : entries) {
                GameProfile profile = entry.getProfile();
                if (entry.getDisplayName() == null) continue;
                post(new PlayerDisplayNameChangeEvent(profile.getId(), entry.getDisplayName()));
            }
        } else if (action == Action.ADD_PLAYER) {
            for (PlayerUpdate entry : entries) {
                GameProfile profile = entry.getProfile();
                post(new PlayerLogInEvent(profile.getId(), profile.getName()));
            }
        } else if (action == Action.REMOVE_PLAYER) {
            for (PlayerUpdate entry : entries) {
                GameProfile profile = entry.getProfile();
                post(new PlayerLogOutEvent(profile.getId()));
            }
        }
    }

    public static void onTooltipRender(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        // TODO: Not implemented yet
    }

    public static void onItemTooltipRender(PoseStack poseStack, ItemStack stack, int mouseX, int mouseY) {
        post(new ItemTooltipRenderEvent(poseStack, stack, mouseX, mouseY));
    }

    public static void onSlotRenderPre(Screen screen, Slot slot) {
        post(new SlotRenderEvent.Pre(screen, slot));
    }

    public static void onSlotRenderPost(Screen screen, Slot slot) {
        post(new SlotRenderEvent.Post(screen, slot));
    }

    public static void onHotbarSlotRenderPre(ItemStack stack, int x, int y) {
        post(new HotbarSlotRenderEvent.Pre(stack, x, y));
    }

    public static void onHotbarSlotRenderPost(ItemStack stack, int x, int y) {
        post(new HotbarSlotRenderEvent.Post(stack, x, y));
    }

    public static boolean onInventoryKeyPress(int keyCode, int scanCode, int modifiers, Slot hoveredSlot) {
        InventoryKeyPressEvent event = new InventoryKeyPressEvent(keyCode, scanCode, modifiers, hoveredSlot);
        post(event);
        return event.isCanceled();
    }

    public static void onTabListCustomisation(ClientboundTabListPacket packet) {
        String footer = packet.getFooter().getString();
        post(new PlayerInfoFooterChangedEvent(footer));
    }

    public static void onDisconnect() {
        post(new DisconnectedEvent());
    }

    public static void onConnect(String host, int port) {
        post(new ConnectedEvent(host, port));
    }

    public static void onResourcePack(ClientboundResourcePackPacket packet) {
        post(new ResourcePackEvent());
    }

    public static <T extends Packet<?>> boolean onPacketSent(T packet) {
        PacketSentEvent<T> event = new PacketSentEvent<>(packet);
        post(event);
        return event.isCanceled();
    }

    public static <T extends Packet<?>> boolean onPacketReceived(T packet) {
        PacketReceivedEvent<T> event = new PacketReceivedEvent<>(packet);
        post(event);
        return event.isCanceled();
    }

    public static void onPlayerMove(ClientboundPlayerPositionPacket packet) {
        if (!packet.getRelativeArguments().isEmpty()) return;

        Position newPosition = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        post(new PlayerTeleportEvent(newPosition));
    }

    public static void onOpenScreen(ClientboundOpenScreenPacket packet) {
        post(new MenuOpenedEvent(packet.getType(), packet.getTitle()));
    }

    public static void onContainerClose(ClientboundContainerClosePacket packet) {
        post(new MenuClosedEvent());
    }

    public static void onItemsReceived(List<ItemStack> items, AbstractContainerMenu container) {
        post(new ItemsReceivedEvent(container, items));
    }

    public static void onKeyInput(int key, int scanCode, int action, int modifiers) {
        post(new KeyInputEvent(key, scanCode, action, modifiers));
    }

    public static void onRenderLast(
            LevelRenderer context,
            PoseStack poseStack,
            float partialTick,
            Matrix4f projectionMatrix,
            long finishTimeNano) {
        post(new RenderLevelLastEvent(context, poseStack, partialTick, projectionMatrix, finishTimeNano));
    }

    public static void onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, hitVec);
        post(event);
    }
}
