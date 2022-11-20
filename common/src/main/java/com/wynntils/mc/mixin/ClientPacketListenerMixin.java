/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.CommandsPacketEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.utils.McUtils;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

    @Shadow
    protected abstract void send(ServerboundResourcePackPacket.Action action);

    @Shadow
    private RegistryAccess.Frozen registryAccess;

    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow
    @Final
    private Minecraft minecraft;

    private static boolean isRenderThread() {
        return (McUtils.mc().isSameThread());
    }

    @Inject(
            method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V",
            at = @At("HEAD"))
    private void handleCommandsPre(ClientboundCommandsPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        RootCommandNode<SharedSuggestionProvider> root = packet.getRoot(new CommandBuildContext(this.registryAccess));
        CommandsPacketEvent event = EventFactory.onCommandsPacket(root);
        if (event.getRoot() != root) {
            // We modified command root, so inject it
            this.commands = new CommandDispatcher<>(event.getRoot());
        }
    }

    @Inject(
            method = "handlePlayerInfo(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoPacket;)V",
            at = @At("RETURN"))
    private void handlePlayerInfoPost(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerInfoPacket(packet);
    }

    @Inject(
            method = "handleTabListCustomisation(Lnet/minecraft/network/protocol/game/ClientboundTabListPacket;)V",
            at = @At("RETURN"))
    private void handleTabListCustomisationPost(ClientboundTabListPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onTabListCustomisation(packet);
    }

    @Inject(
            method = "handleResourcePack(Lnet/minecraft/network/protocol/game/ClientboundResourcePackPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleResourcePackPre(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        if (EventFactory.onResourcePack(packet).isCanceled()) {
            this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
            ci.cancel();
        }
    }

    @Inject(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("RETURN"))
    private void handleMovePlayerPost(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerMove(packet);
    }

    @Inject(
            method = "handleOpenScreen(Lnet/minecraft/network/protocol/game/ClientboundOpenScreenPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleOpenScreenPre(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onOpenScreen(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleContainerClose(Lnet/minecraft/network/protocol/game/ClientboundContainerClosePacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleContainerClosePre(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onClientboundContainerClosePacket(packet.getContainerId())
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleContainerContentPre(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        ContainerSetContentEvent event = EventFactory.onContainerSetContentPre(packet);
        if (event.isCanceled()) {
            ci.cancel();
        }

        if (packet.getItems() != event.getItems()) {
            if (packet.getContainerId() == 0) {
                McUtils.player()
                        .inventoryMenu
                        .initializeContents(packet.getStateId(), packet.getItems(), packet.getCarriedItem());
            } else if (packet.getContainerId() == McUtils.player().containerMenu.containerId) {
                McUtils.player()
                        .containerMenu
                        .initializeContents(packet.getStateId(), packet.getItems(), packet.getCarriedItem());
            }

            ci.cancel();
        }
    }

    @Inject(
            method =
                    "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At("RETURN"))
    private void handleContainerContentPost(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onContainerSetContentPost(packet);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"))
    private void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onContainerSetSlot(packet);
    }

    @Inject(
            method = "handleSetPlayerTeamPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPlayerTeamPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetPlayerTeamPacketPre(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetPlayerTeam(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetEntityPassengersPacketPre(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetEntityPassengers(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetSpawn", at = @At("HEAD"), cancellable = true)
    private void handleSetSpawnPre(ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetSpawn(packet.getPos()).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "setTitleText(Lnet/minecraft/network/protocol/game/ClientboundSetTitleTextPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void setTitleTextPre(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onTitleSetText(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "setSubtitleText(Lnet/minecraft/network/protocol/game/ClientboundSetSubtitleTextPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void setSubtitleTextPre(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSubtitleSetText(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handlePlayerChat",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/network/chat/ChatType$Bound;)V"),
            cancellable = true)
    private void handlePlayerChat(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        ChatPacketReceivedEvent result = EventFactory.onChatReceived(
                com.wynntils.mc.objects.ChatType.CHAT, packet.message().serverContent());
        if (result.isCanceled()) return;

        if (!result.getMessage().equals(packet.message().serverContent())) {
            PlayerChatMessage modified = packet.message().withUnsignedContent(result.getMessage());

            // Because of the injection point, we know this option is present
            Optional<ChatType.Bound> optional = packet.resolveChatType(this.registryAccess);
            this.minecraft.getChatListener().handleChatMessage(modified, optional.get());
            ci.cancel();
        }
    }

    @Inject(
            method = "handleSystemChat",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
            cancellable = true)
    private void handleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        ChatPacketReceivedEvent result = EventFactory.onChatReceived(
                packet.overlay() ? com.wynntils.mc.objects.ChatType.GAME_INFO : com.wynntils.mc.objects.ChatType.SYSTEM,
                packet.content());
        if (result.isCanceled()) return;

        if (!result.getMessage().equals(packet.content())) {

            this.minecraft.getChatListener().handleSystemMessage(result.getMessage(), packet.overlay());
            ci.cancel();
        }
    }

    @Inject(method = "handleSetScore", at = @At("HEAD"), cancellable = true)
    private void handleSetScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetScore(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void onDisconnectPre(Component reason, CallbackInfo ci) {
        // Unexpected disconnect
        EventFactory.onDisconnect();
    }

    @Inject(method = "handleAddPlayer", at = @At("HEAD"))
    private void handleAddPlayer(ClientboundAddPlayerPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerJoinedWorld(packet, this.getPlayerInfo(packet.getPlayerId()));
    }

    @Inject(method = "handleUpdateAdvancementsPacket", at = @At("RETURN"))
    private void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onUpdateAdvancements(packet);
    }
}
