/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.CommandsAddedEvent;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.MobEffectEvent;
import com.wynntils.mc.event.PlayerInfoEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.mc.event.SetEntityPassengersEvent;
import com.wynntils.mc.event.SetPlayerTeamEvent;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.mc.mixin.accessors.ClientboundSetPlayerTeamPacketAccessor;
import com.wynntils.utils.mc.McUtils;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Position;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import net.minecraft.world.phys.Vec3;
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
        ChatSentEvent result = MixinHelper.post(new ChatSentEvent(string));
        if (result.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSignedCommandPre(String string, CallbackInfo ci) {
        if (MixinHelper.post(new CommandSentEvent(string, true)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void onUnsignedCommandPre(String command, CallbackInfoReturnable<Boolean> cir) {
        if (MixinHelper.post(new CommandSentEvent(command, false)).isCanceled()) {
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
        CommandsAddedEvent event = MixinHelper.post(new CommandsAddedEvent(root));

        if (event.getRoot() != root) {
            // If we changed the root, replace the CommandDispatcher
            this.commands = new CommandDispatcher<>(event.getRoot());
        }
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("RETURN"))
    private void handlePlayerInfoUpdatePost(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.newEntries()) {
            GameProfile profile = entry.profile();
            MixinHelper.post(new PlayerInfoEvent.PlayerLogInEvent(profile.getId(), profile.getName()));
        }

        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            for (ClientboundPlayerInfoUpdatePacket.Action action : packet.actions()) {
                if (action == ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME) {
                    GameProfile profile = entry.profile();
                    if (entry.displayName() == null) continue;
                    MixinHelper.post(
                            new PlayerInfoEvent.PlayerDisplayNameChangeEvent(profile.getId(), entry.displayName()));
                }
            }
        }
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("RETURN"))
    private void handlePlayerInfoRemovePost(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        for (UUID uuid : packet.profileIds()) {
            MixinHelper.post(new PlayerInfoEvent.PlayerLogOutEvent(uuid));
        }
    }

    @Inject(
            method = "handleTabListCustomisation(Lnet/minecraft/network/protocol/game/ClientboundTabListPacket;)V",
            at = @At("RETURN"))
    private void handleTabListCustomisationPost(ClientboundTabListPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new PlayerInfoFooterChangedEvent(packet.getFooter().getString()));
    }

    @Inject(
            method = "handleResourcePack(Lnet/minecraft/network/protocol/game/ClientboundResourcePackPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleResourcePackPre(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        if (MixinHelper.post(new ResourcePackEvent(packet.getUrl(), packet.getHash(), packet.isRequired()))
                .isCanceled()) {
            this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
            ci.cancel();
        }
    }

    @Inject(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("RETURN"))
    private void handleMovePlayerPost(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (!packet.getRelativeArguments().isEmpty()) return;

        MixinHelper.post(new PlayerTeleportEvent(new Vec3(packet.getX(), packet.getY(), packet.getZ())));
    }

    @Inject(
            method = "handleOpenScreen(Lnet/minecraft/network/protocol/game/ClientboundOpenScreenPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleOpenScreenPre(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(
                        new MenuEvent.MenuOpenedEvent(packet.getType(), packet.getTitle(), packet.getContainerId()))
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleContainerClose(Lnet/minecraft/network/protocol/game/ClientboundContainerClosePacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleContainerClosePre(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(new MenuEvent.MenuClosedEvent(packet.getContainerId())).isCanceled()) {
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
        ContainerSetContentEvent event = MixinHelper.post(new ContainerSetContentEvent.Pre(
                packet.getItems(), packet.getCarriedItem(), packet.getContainerId(), packet.getStateId()));
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
        MixinHelper.post(new ContainerSetContentEvent.Post(
                packet.getItems(), packet.getCarriedItem(), packet.getContainerId(), packet.getStateId()));
    }

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"))
    private void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new ContainerSetSlotEvent(
                packet.getContainerId(), packet.getStateId(), packet.getSlot(), packet.getItem()));
    }

    @Inject(
            method = "handleSetPlayerTeamPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPlayerTeamPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetPlayerTeamPacketPre(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(new SetPlayerTeamEvent(
                        ((ClientboundSetPlayerTeamPacketAccessor) packet).getMethod(), packet.getName()))
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetExperience", at = @At("RETURN"))
    private void handleSetExperiencePost(ClientboundSetExperiencePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new SetXpEvent(
                packet.getExperienceProgress(), packet.getTotalExperience(), packet.getExperienceLevel()));
    }

    @Inject(
            method =
                    "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetEntityPassengersPacketPre(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(new SetEntityPassengersEvent(packet.getVehicle())).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetSpawn", at = @At("HEAD"), cancellable = true)
    private void handleSetSpawnPre(ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(new SetSpawnEvent(packet.getPos())).isCanceled()) {
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
        if (MixinHelper.post(new TitleSetTextEvent(packet.getText())).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "setSubtitleText(Lnet/minecraft/network/protocol/game/ClientboundSetSubtitleTextPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void setSubtitleTextPre(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(new SubtitleSetTextEvent(packet.getText())).isCanceled()) {
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

        // Currently, Wynncraft does not have any Player chat messages so this code
        // is not really used
        ChatPacketReceivedEvent result = MixinHelper.post(new ChatPacketReceivedEvent.Player(packet.unsignedContent()));
        if (result.isCanceled()) {
            ci.cancel();
            return;
        }

        if (result.isMessageChanged()) {
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
        Component message = packet.content();
        ChatPacketReceivedEvent event = packet.overlay()
                ? new ChatPacketReceivedEvent.GameInfo(message)
                : new ChatPacketReceivedEvent.System(message);
        MixinHelper.post(event);
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        if (event.isMessageChanged()) {
            this.minecraft.getChatListener().handleSystemMessage(event.getMessage(), packet.overlay());
            ci.cancel();
        }
    }

    @Inject(method = "handleSetScore", at = @At("HEAD"), cancellable = true)
    private void handleSetScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (MixinHelper.post(new ScoreboardSetScoreEvent(
                        packet.getOwner(), packet.getObjectiveName(), packet.getScore(), packet.getMethod()))
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void onDisconnectPre(Component reason, CallbackInfo ci) {
        // Unexpected disconnect
        MixinHelper.post(new ConnectionEvent.DisconnectedEvent());
    }

    @Inject(method = "handleUpdateAdvancementsPacket", at = @At("RETURN"))
    private void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new AdvancementUpdateEvent(
                packet.shouldReset(), packet.getAdded(), packet.getRemoved(), packet.getProgress()));
    }

    @Inject(
            method = "handleUpdateMobEffect(Lnet/minecraft/network/protocol/game/ClientboundUpdateMobEffectPacket;)V",
            at = @At("RETURN"))
    private void handleUpdateMobEffectPost(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new MobEffectEvent.Update(
                McUtils.mc().level.getEntity(packet.getEntityId()),
                packet.getEffect(),
                packet.getEffectAmplifier(),
                packet.getEffectDurationTicks()));
    }

    @Inject(
            method = "handleRemoveMobEffect(Lnet/minecraft/network/protocol/game/ClientboundRemoveMobEffectPacket;)V",
            at = @At("RETURN"))
    private void handleRemoveMobEffectPost(ClientboundRemoveMobEffectPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new MobEffectEvent.Remove(packet.getEntity(McUtils.mc().level), packet.getEffect()));
    }

    @Inject(
            method = "handleAddEntity(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V",
            at = @At("RETURN"))
    private void handleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new AddEntityEvent(packet));
    }

    @Inject(
            method = "handleSetEntityData(Lnet/minecraft/network/protocol/game/ClientboundSetEntityDataPacket;)V",
            at = @At("HEAD"))
    private void handleSetEntityDataPre(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new SetEntityDataEvent(packet));
    }

    @Inject(
            method = "handleRemoveEntities(Lnet/minecraft/network/protocol/game/ClientboundRemoveEntitiesPacket;)V",
            at = @At("RETURN"))
    private void handleRemoveEntities(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        MixinHelper.post(new RemoveEntitiesEvent(packet));
    }
}
