/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.event.CommandsPacketEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

    @Shadow
    protected abstract void send(ServerboundResourcePackPacket.Action action);

    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private LayeredRegistryAccess<ClientRegistryLayer> registryAccess;

    @Shadow
    private MessageSignatureCache messageSignatureCache;

    private static boolean isRenderThread() {
        return McUtils.mc().isSameThread();
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onChatPre(String string, CallbackInfo ci) {
        ChatSentEvent result = EventFactory.onChatSent(string);
        if (result.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSignedCommandPre(String string, CallbackInfo ci) {
        if (EventFactory.onCommandSent(string, true).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void onUnsignedCommandPre(String command, CallbackInfoReturnable<Boolean> cir) {
        if (EventFactory.onCommandSent(command, false).isCanceled()) {
            // Return true here, to signal to MC that we handled the command.
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
            method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V",
            at = @At("RETURN"))
    private void handleCommandsPost(ClientboundCommandsPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        // We need to read the root from the CommandDispatcher, not the packet,
        // due to interop with other mods
        RootCommandNode<SharedSuggestionProvider> root = this.commands.getRoot();
        CommandsPacketEvent event = EventFactory.onCommandsPacket(root);

        if (event.getRoot() != root) {
            // If we changed the root, replace the CommandDispatcher
            this.commands = new CommandDispatcher<>(event.getRoot());
        }
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("RETURN"))
    private void handlePlayerInfoUpdatePost(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerInfoUpdatePacket(packet);
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("RETURN"))
    private void handlePlayerInfoRemovePost(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerInfoRemovePacket(packet);
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

        if (!packet.getItems().equals(event.getItems())) {
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

    @Inject(method = "handleSetExperience", at = @At("RETURN"))
    private void handleSetExperiencePost(ClientboundSetExperiencePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onSetXp(packet);
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

            // Signal loading complete to the loading screen,
            // or else we are stuck in an "infinite" loading state
            if (McUtils.mc().screen instanceof ReceivingLevelScreen receivingLevelScreen) {
                receivingLevelScreen.loadingPacketsReceived();
            }
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
                                    "Lnet/minecraft/client/multiplayer/chat/ChatListener;handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V"),
            cancellable = true)
    private void handlePlayerChat(
            ClientboundPlayerChatPacket packet,
            CallbackInfo ci,
            @Local PlayerChatMessage playerChatMessage,
            @Local PlayerInfo playerInfo) {
        if (!isRenderThread()) return;
        ChatPacketReceivedEvent result = EventFactory.onPlayerChatReceived(packet.unsignedContent());
        if (result.isCanceled()) {
            ci.cancel();
            return;
        }

        if (!Objects.equals(result.getMessage(), packet.unsignedContent())) {
            // We know this is present because of the injection point
            ChatType.Bound bound = packet.chatType()
                    .resolve(this.registryAccess.compositeAccess())
                    .get();

            this.minecraft.getChatListener().handlePlayerChatMessage(playerChatMessage, playerInfo.getProfile(), bound);
            this.messageSignatureCache.push(playerChatMessage);

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
        ChatPacketReceivedEvent result = EventFactory.onSystemChatReceived(packet.content(), packet.overlay());
        if (result.isCanceled()) {
            ci.cancel();
            return;
        }

        if (!Objects.equals(result.getMessage(), packet.content())) {
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

    @Inject(
            method = "handleUpdateMobEffect(Lnet/minecraft/network/protocol/game/ClientboundUpdateMobEffectPacket;)V",
            at = @At("RETURN"))
    private void handleUpdateMobEffectPost(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onUpdateMobEffect(packet);
    }

    @Inject(
            method = "handleRemoveMobEffect(Lnet/minecraft/network/protocol/game/ClientboundRemoveMobEffectPacket;)V",
            at = @At("RETURN"))
    private void handleRemoveMobEffectPost(ClientboundRemoveMobEffectPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onRemoveMobEffect(packet);
    }

    @Inject(
            method = "handleAddEntity(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V",
            at = @At("RETURN"))
    private void handleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onAddEntity(packet);
    }

    @Inject(
            method = "handleSetEntityData(Lnet/minecraft/network/protocol/game/ClientboundSetEntityDataPacket;)V",
            at = @At("RETURN"))
    private void handleSetEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onSetEntityData(packet);
    }

    @Inject(
            method = "handleRemoveEntities(Lnet/minecraft/network/protocol/game/ClientboundRemoveEntitiesPacket;)V",
            at = @At("RETURN"))
    private void handleRemoveEntities(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onRemoveEntities(packet);
    }
}
