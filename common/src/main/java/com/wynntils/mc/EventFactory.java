/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.AddEntityLookupEvent;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ChatScreenKeyTypedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.CommandsPacketEvent;
import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent;
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerRenderEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.DisplayResizeEvent;
import com.wynntils.mc.event.DrawPotionGlintEvent;
import com.wynntils.mc.event.DropHeldItemEvent;
import com.wynntils.mc.event.GroundItemEntityTransformEvent;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.ItemTooltipHoveredNameEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.LivingEntityRenderTranslucentCheckEvent;
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent;
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent;
import com.wynntils.mc.event.MobEffectEvent;
import com.wynntils.mc.event.MouseScrollEvent;
import com.wynntils.mc.event.NametagRenderEvent;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import com.wynntils.mc.event.PauseMenuInitEvent;
import com.wynntils.mc.event.PlayerArmorRenderEvent;
import com.wynntils.mc.event.PlayerAttackEvent;
import com.wynntils.mc.event.PlayerInfoEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerDisplayNameChangeEvent;
import com.wynntils.mc.event.PlayerInfoEvent.PlayerLogInEvent;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.PlayerJoinedWorldEvent;
import com.wynntils.mc.event.PlayerTeleportEvent;
import com.wynntils.mc.event.RemovePlayerFromTeamEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.mc.event.ResourcePackClearEvent;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.ScreenRenderEvent;
import com.wynntils.mc.event.SetEntityPassengersEvent;
import com.wynntils.mc.event.SetPlayerTeamEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.mc.event.SetXpEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.mc.mixin.accessors.ClientboundSetPlayerTeamPacketAccessor;
import com.wynntils.mc.objects.ChatType;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.Event;
import org.joml.Matrix4f;

/** Creates events from mixins and platform dependent hooks */
public final class EventFactory {
    private static <T extends Event> T post(T event) {
        if (WynnUtils.onServer()) {
            WynntilsMod.postEvent(event);
        }
        return event;
    }

    /**
     * Post event without checking if we are connected to a Wynncraft server
     */
    private static <T extends Event> T postAlways(T event) {
        WynntilsMod.postEvent(event);
        return event;
    }

    // region Render Events
    public static PlayerArmorRenderEvent onPlayerArmorRender(Player player, EquipmentSlot slot) {
        return post(new PlayerArmorRenderEvent(player, slot));
    }

    public static GroundItemEntityTransformEvent onGroundItemRender(PoseStack poseStack, ItemStack stack) {
        return post(new GroundItemEntityTransformEvent(poseStack, stack));
    }

    public static NametagRenderEvent onNameTagRender(
            AbstractClientPlayer entity,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        return post(new NametagRenderEvent(entity, displayName, poseStack, buffer, packedLight));
    }

    public static void onRenderLevelPost(
            LevelRenderer context,
            PoseStack poseStack,
            float partialTick,
            Matrix4f projectionMatrix,
            long finishTimeNano,
            Camera camera) {
        post(new RenderLevelEvent.Post(context, poseStack, partialTick, projectionMatrix, finishTimeNano, camera));
    }

    public static void onRenderLevelPre(
            LevelRenderer context,
            PoseStack poseStack,
            float partialTick,
            Matrix4f projectionMatrix,
            long finishTimeNano,
            Camera camera) {
        post(new RenderLevelEvent.Pre(context, poseStack, partialTick, projectionMatrix, finishTimeNano, camera));
    }

    public static void onRenderTileLast(
            LevelRenderer context,
            PoseStack poseStack,
            float partialTick,
            Matrix4f projectionMatrix,
            long finishTimeNano,
            Camera camera) {
        post(new RenderTileLevelLastEvent(context, poseStack, partialTick, projectionMatrix, finishTimeNano, camera));
    }

    public static void onRenderGuiPre(PoseStack poseStack, float partialTicks, Window window) {
        post(new RenderEvent.Pre(poseStack, partialTicks, window, RenderEvent.ElementType.GUI));
    }

    public static void onRenderGuiPost(PoseStack poseStack, float partialTicks, Window window) {
        post(new RenderEvent.Post(poseStack, partialTicks, window, RenderEvent.ElementType.GUI));
    }

    public static RenderEvent.Pre onRenderCrosshairPre(PoseStack poseStack, Window window) {
        return post(new RenderEvent.Pre(poseStack, 0, window, RenderEvent.ElementType.Crosshair));
    }

    public static RenderEvent.Pre onRenderHearthsPre(PoseStack poseStack, Window window) {
        return post(new RenderEvent.Pre(poseStack, 0, window, RenderEvent.ElementType.HealthBar));
    }

    public static RenderEvent.Pre onRenderFoodPre(PoseStack poseStack, Window window) {
        return post(new RenderEvent.Pre(poseStack, 0, window, RenderEvent.ElementType.FoodBar));
    }

    public static void onContainerRender(
            AbstractContainerScreen<?> screen,
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float partialTicks,
            Slot hoveredSlot) {
        post(new ContainerRenderEvent(screen, poseStack, mouseX, mouseY, partialTicks, hoveredSlot));
    }

    public static ItemTooltipRenderEvent.Pre onItemTooltipRenderPre(
            PoseStack poseStack, ItemStack stack, List<Component> tooltips, int mouseX, int mouseY) {
        return post(new ItemTooltipRenderEvent.Pre(poseStack, stack, tooltips, mouseX, mouseY));
    }

    public static void onItemTooltipRenderPost(PoseStack poseStack, ItemStack stack, int mouseX, int mouseY) {
        post(new ItemTooltipRenderEvent.Post(poseStack, stack, mouseX, mouseY));
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

    public static DrawPotionGlintEvent onPotionIsFoil(PotionItem item) {
        return post(new DrawPotionGlintEvent(item));
    }

    public static LivingEntityRenderTranslucentCheckEvent onTranslucentCheck(boolean translucent, LivingEntity entity) {
        return post(new LivingEntityRenderTranslucentCheckEvent(translucent, entity, translucent ? 0.15f : 1f));
    }

    // endregion

    // region Screen Events
    public static void onScreenCreatedPost(Screen screen, Consumer<AbstractWidget> addButton) {
        if (screen instanceof TitleScreen titleScreen) {
            postAlways(new TitleScreenInitEvent.Post(titleScreen, addButton));
        } else if (screen instanceof PauseScreen pauseMenuScreen) {
            post(new PauseMenuInitEvent(pauseMenuScreen, addButton));
        }
    }

    public static void onScreenCreatedPre(Screen screen, Consumer<AbstractWidget> addButton) {
        if (screen instanceof TitleScreen titleScreen) {
            postAlways(new TitleScreenInitEvent.Pre(titleScreen, addButton));
        }
    }

    public static void onScreenOpened(Screen screen) {
        post(new ScreenOpenedEvent(screen));
    }

    public static void onScreenClose() {
        post(new ScreenClosedEvent());
    }

    public static MenuOpenedEvent onOpenScreen(ClientboundOpenScreenPacket packet) {
        return post(new MenuOpenedEvent(packet.getType(), packet.getTitle(), packet.getContainerId()));
    }

    public static ContainerSetContentEvent onContainerSetContentPre(ClientboundContainerSetContentPacket packet) {
        return post(new ContainerSetContentEvent.Pre(
                packet.getItems(), packet.getCarriedItem(), packet.getContainerId(), packet.getStateId()));
    }

    public static void onContainerSetContentPost(ClientboundContainerSetContentPacket packet) {
        post(new ContainerSetContentEvent.Post(
                packet.getItems(), packet.getCarriedItem(), packet.getContainerId(), packet.getStateId()));
    }

    public static void onContainerSetSlot(ClientboundContainerSetSlotPacket packet) {
        post(new ContainerSetSlotEvent(
                packet.getContainerId(), packet.getStateId(), packet.getSlot(), packet.getItem()));
    }

    public static void onScreenInit(Screen screen) {
        post(new ScreenInitEvent(screen));
    }

    public static void onScreenRenderPost(
            Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        post(new ScreenRenderEvent(screen, poseStack, mouseX, mouseY, partialTick));
    }

    // endregion

    // region Container Events
    public static void onChestMenuQuickMove(int containerId) {
        post(new ChestMenuQuickMoveEvent(containerId));
    }

    public static Event onClientboundContainerClosePacket(int containerId) {
        return post(new MenuClosedEvent(containerId));
    }

    public static ContainerCloseEvent.Pre onCloseContainerPre() {
        return post(new ContainerCloseEvent.Pre());
    }

    public static void onCloseContainerPost() {
        post(new ContainerCloseEvent.Post());
    }

    public static SetSlotEvent onSetSlotPre(Container container, int slot, ItemStack item) {
        return post(new SetSlotEvent.Pre(container, slot, item));
    }

    public static void onSetSlotPost(Container container, int slot, ItemStack item) {
        post(new SetSlotEvent.Post(container, slot, item));
    }

    public static InventoryKeyPressEvent onInventoryKeyPress(
            int keyCode, int scanCode, int modifiers, Slot hoveredSlot) {
        return post(new InventoryKeyPressEvent(keyCode, scanCode, modifiers, hoveredSlot));
    }

    public static InventoryMouseClickedEvent onInventoryMouseClick(
            double mouseX, double mouseY, int button, Slot hoveredSlot) {
        return post(new InventoryMouseClickedEvent(mouseX, mouseY, button, hoveredSlot));
    }

    public static ContainerClickEvent onContainerClickEvent(
            int containerId, int slotNum, ItemStack itemStack, ClickType clickType, int buttonNum) {
        return post(new ContainerClickEvent(containerId, slotNum, itemStack, clickType, buttonNum));
    }

    public static ItemTooltipHoveredNameEvent onGetHoverName(Component hoveredName, ItemStack stack) {
        return post(new ItemTooltipHoveredNameEvent(hoveredName, stack));
    }

    // endregion

    // region Player Input Events
    public static void onPlayerMove(ClientboundPlayerPositionPacket packet) {
        if (!packet.getRelativeArguments().isEmpty()) return;

        Position newPosition = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        post(new PlayerTeleportEvent(newPosition));
    }

    public static KeyInputEvent onKeyInput(int key, int scanCode, int action, int modifiers) {
        return post(new KeyInputEvent(key, scanCode, action, modifiers));
    }

    public static ChatScreenKeyTypedEvent onChatScreenKeyInput(int keyCode, int scanCode, int modifiers) {
        return post(new ChatScreenKeyTypedEvent(keyCode, scanCode, modifiers));
    }

    public static Event onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, hitVec);
        return post(event);
    }

    public static Event onInteract(Player player, InteractionHand hand, Entity target) {
        PlayerInteractEvent.Interact event = new PlayerInteractEvent.Interact(player, hand, target);
        return post(event);
    }

    public static Event onInteractAt(
            Player player, InteractionHand hand, Entity target, EntityHitResult entityHitResult) {
        PlayerInteractEvent.InteractAt event =
                new PlayerInteractEvent.InteractAt(player, hand, target, entityHitResult);
        return post(event);
    }

    public static Event onAttack(Player player, Entity target) {
        return post(new PlayerAttackEvent(player, target));
    }

    public static Event onUseItem(Player player, Level level, InteractionHand hand) {
        return post(new UseItemEvent(player, level, hand));
    }

    public static DropHeldItemEvent onDropPre(boolean fullStack) {
        return post(new DropHeldItemEvent(fullStack));
    }

    public static ArmSwingEvent onArmSwing(ArmSwingEvent.ArmSwingContext actionContext, InteractionHand hand) {
        return post(new ArmSwingEvent(actionContext, hand));
    }
    // endregion

    // region Chat Events
    public static ChatSentEvent onChatSent(String message) {
        return post(new ChatSentEvent(message));
    }

    public static CommandSentEvent onCommandSent(String command, boolean signed) {
        return post(new CommandSentEvent(command, signed));
    }

    public static ChatPacketReceivedEvent onChatReceived(ChatType type, Component message) {
        return post(new ChatPacketReceivedEvent(type, message));
    }

    public static ClientsideMessageEvent onClientsideMessage(Component component) {
        return post(new ClientsideMessageEvent(component));
    }

    // endregion

    // region Server Events
    public static void onPlayerJoinedWorld(ClientboundAddPlayerPacket packet, PlayerInfo playerInfo) {
        post(new PlayerJoinedWorldEvent(
                packet.getEntityId(),
                packet.getPlayerId(),
                packet.getX(),
                packet.getY(),
                packet.getZ(),
                packet.getxRot(),
                packet.getyRot(),
                playerInfo));
    }

    public static void onDisconnect() {
        post(new DisconnectedEvent());
    }

    public static void onConnect(String host, int port) {
        postAlways(new ConnectedEvent(host, port));
    }

    public static Event onResourcePack(ClientboundResourcePackPacket packet) {
        return post(new ResourcePackEvent(packet.getUrl(), packet.getHash(), packet.isRequired()));
    }

    public static Event onResourcePackClearEvent(String hash) {
        return postAlways(new ResourcePackClearEvent(hash));
    }

    public static CommandsPacketEvent onCommandsPacket(RootCommandNode<SharedSuggestionProvider> root) {
        return post(new CommandsPacketEvent(root));
    }

    public static SetPlayerTeamEvent onSetPlayerTeam(ClientboundSetPlayerTeamPacket packet) {
        return post(new SetPlayerTeamEvent(
                ((ClientboundSetPlayerTeamPacketAccessor) packet).getMethod(), packet.getName()));
    }

    public static void onSetXp(ClientboundSetExperiencePacket packet) {
        post(new SetXpEvent(packet.getExperienceProgress(), packet.getTotalExperience(), packet.getExperienceLevel()));
    }

    public static AddEntityLookupEvent onAddEntityLookup(UUID uuid, Map<UUID, EntityAccess> entityMap) {
        return post(new AddEntityLookupEvent(uuid, entityMap));
    }

    public static SetEntityPassengersEvent onSetEntityPassengers(ClientboundSetPassengersPacket packet) {
        return post(new SetEntityPassengersEvent(packet.getVehicle()));
    }

    public static RemovePlayerFromTeamEvent onRemovePlayerFromTeam(String username, PlayerTeam playerTeam) {
        return post(new RemovePlayerFromTeamEvent(username, playerTeam));
    }

    public static BossHealthUpdateEvent onBossHealthUpdate(
            ClientboundBossEventPacket packet, Map<UUID, LerpingBossEvent> bossEvents) {
        return post(new BossHealthUpdateEvent(packet, bossEvents));
    }

    public static void onUpdateAdvancements(ClientboundUpdateAdvancementsPacket packet) {
        post(new AdvancementUpdateEvent(
                packet.shouldReset(), packet.getAdded(), packet.getRemoved(), packet.getProgress()));
    }

    public static SetSpawnEvent onSetSpawn(BlockPos spawnPos) {
        return post(new SetSpawnEvent(spawnPos));
    }

    public static void onPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket packet) {
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.newEntries()) {
            GameProfile profile = entry.profile();
            post(new PlayerLogInEvent(profile.getId(), profile.getName()));
        }

        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            for (ClientboundPlayerInfoUpdatePacket.Action action : packet.actions()) {
                if (action == ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME) {
                    GameProfile profile = entry.profile();
                    if (entry.displayName() == null) continue;
                    post(new PlayerDisplayNameChangeEvent(profile.getId(), entry.displayName()));
                }
            }
        }
    }

    public static void onPlayerInfoRemovePacket(ClientboundPlayerInfoRemovePacket packet) {
        for (UUID uuid : packet.profileIds()) {
            post(new PlayerInfoEvent.PlayerLogOutEvent(uuid));
        }
    }

    public static void onTabListCustomisation(ClientboundTabListPacket packet) {
        String footer = packet.getFooter().getString();
        post(new PlayerInfoFooterChangedEvent(footer));
    }

    public static ScoreboardSetScoreEvent onSetScore(ClientboundSetScorePacket packet) {
        return post(new ScoreboardSetScoreEvent(
                packet.getOwner(), packet.getObjectiveName(), packet.getScore(), packet.getMethod()));
    }

    public static void onUpdateMobEffect(ClientboundUpdateMobEffectPacket packet) {
        post(new MobEffectEvent.Update(
                McUtils.mc().level.getEntity(packet.getEntityId()),
                packet.getEffect(),
                packet.getEffectAmplifier(),
                packet.getEffectDurationTicks()));
    }

    public static void onRemoveMobEffect(ClientboundRemoveMobEffectPacket packet) {
        post(new MobEffectEvent.Remove(packet.getEntity(McUtils.mc().level), packet.getEffect()));
    }

    // endregion

    // region Packet Events
    public static <T extends Packet<?>> PacketSentEvent<T> onPacketSent(T packet) {
        return postAlways(new PacketSentEvent<>(packet));
    }

    public static <T extends Packet<?>> PacketReceivedEvent<T> onPacketReceived(T packet) {
        return postAlways(new PacketReceivedEvent<>(packet));
    }
    // endregion

    // region Game Events
    public static void onTickStart() {
        post(new ClientTickEvent.Start());
    }

    public static void onTickEnd() {
        post(new ClientTickEvent.End());
    }

    public static void onResizeDisplayPost() {
        postAlways(new DisplayResizeEvent());
    }

    // endregion

    // region Title Events
    public static Event onTitleSetText(ClientboundSetTitleTextPacket packet) {
        return post(new TitleSetTextEvent(packet.getText()));
    }

    public static Event onSubtitleSetText(ClientboundSetSubtitleTextPacket packet) {
        return post(new SubtitleSetTextEvent(packet.getText()));
    }

    public static Event onMouseScroll(double windowPointer, double xOffset, double yOffset) {
        return post(new MouseScrollEvent(windowPointer, xOffset, yOffset));
    }

    // endregion
}
