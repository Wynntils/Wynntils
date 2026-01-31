/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.features.players.HadesFeature;
import com.wynntils.hades.objects.HadesConnection;
import com.wynntils.hades.protocol.builders.HadesNetworkBuilder;
import com.wynntils.hades.protocol.enums.PacketAction;
import com.wynntils.hades.protocol.enums.PacketDirection;
import com.wynntils.hades.protocol.enums.SocialType;
import com.wynntils.hades.protocol.packets.client.HCPacketPing;
import com.wynntils.hades.protocol.packets.client.HCPacketSocialUpdate;
import com.wynntils.hades.protocol.packets.client.HCPacketUpdateStatus;
import com.wynntils.hades.protocol.packets.client.HCPacketUpdateWorld;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.players.event.HadesRelationsUpdateEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.services.athena.event.AthenaLoginEvent;
import com.wynntils.services.hades.event.HadesEvent;
import com.wynntils.services.hades.type.GearShareOptions;
import com.wynntils.services.hades.type.PlayerStatus;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;

public final class HadesService extends Service {
    private static final int TICKS_PER_UPDATE = 2;
    private static final int MS_PER_PING = 1000;

    private static final EncodingSettings HADES_ENCODING_SETTINGS = new EncodingSettings(false, false);

    private final HadesUserRegistry userRegistry = new HadesUserRegistry();

    private CompletableFuture<Void> connectionFuture;
    private HadesConnection hadesConnection;
    private int tickCountUntilUpdate = 0;
    private PlayerStatus lastSentStatus;
    private ScheduledExecutorService pingScheduler;

    @Persisted
    private final Storage<GearShareOptions> gearShareOptions = new Storage<>(new GearShareOptions());

    @Persisted
    private final Storage<Map<String, Boolean>> characterGearShareEnabled = new Storage<>(new TreeMap<>());

    @Persisted
    private final Storage<Map<String, GearShareOptions>> characterGearShareOptions = new Storage<>(new TreeMap<>());

    // Original WynnItem cache to avoid unnecessary encoding
    private NavigableMap<InventoryArmor, WynnItem> armorCache = new TreeMap<>();
    private NavigableMap<InventoryAccessory, WynnItem> accessoriesCache = new TreeMap<>();
    private WynnItem heldItemCache = null;
    // Encoded items to be sent
    private final Map<InventoryArmor, String> armor = new TreeMap<>();
    private final Map<InventoryAccessory, String> accessories = new TreeMap<>();
    private String heldItem = "";

    public HadesService() {
        super(List.of());
    }

    public Stream<HadesUser> getHadesUsers() {
        return userRegistry.getHadesUserMap().values().stream();
    }

    public Optional<HadesUser> getHadesUser(UUID uuid) {
        return userRegistry.getUser(uuid);
    }

    private void login() {
        connect();
    }

    private synchronized void connect() {
        // Try to log in to Hades, if we're not already connected or trying to connect
        if (!isConnected() && (connectionFuture == null || connectionFuture.isDone())) {
            connectionFuture = CompletableFuture.runAsync(this::tryCreateConnection);
        }
    }

    private void tryCreateConnection() {
        try {
            hadesConnection = new HadesNetworkBuilder()
                    .setAddress(InetAddress.getByName("io.wynntils.com"), 9000)
                    .setDirection(PacketDirection.SERVER)
                    .setCompressionThreshold(256)
                    .setHandlerFactory(a -> new HadesClientHandler(a, userRegistry))
                    .buildClient();

            tickCountUntilUpdate = 0;
            lastSentStatus = null;
        } catch (IOException e) {
            WynntilsMod.error("Could not connect to Hades.", e);
        }
    }

    public void tryDisconnect() {
        if (hadesConnection != null && hadesConnection.isOpen()) {
            hadesConnection.disconnect();
            connectionFuture = null;
        }
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        if (Models.WorldState.onWorld()) {
            // Send initial world data if Hades login only happened after joining the player's class
            tryResendWorldData();
        }

        WynntilsMod.info("Starting Hades Ping Scheduler Task");

        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(this::sendPing, 0, MS_PER_PING, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onDisconnect(HadesEvent.Disconnected event) {
        if (pingScheduler == null) return;
        pingScheduler.shutdown();
        pingScheduler = null;
        connectionFuture = null;
    }

    private void sendPing() {
        if (!isConnected()) return;

        hadesConnection.sendPacketAndFlush(new HCPacketPing(System.currentTimeMillis()));
    }

    @SubscribeEvent
    public void onFriendListUpdate(HadesRelationsUpdateEvent.FriendList event) {
        if (!isConnected()) return;
        if (!Managers.Feature.getFeatureInstance(HadesFeature.class)
                .shareWithFriends
                .get()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.FRIEND));
    }

    @SubscribeEvent
    public void onPartyListUpdate(HadesRelationsUpdateEvent.PartyList event) {
        if (!isConnected()) return;
        if (!Managers.Feature.getFeatureInstance(HadesFeature.class)
                .shareWithParty
                .get()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.PARTY));
    }

    @SubscribeEvent
    public void onGuildMemberListUpdate(HadesRelationsUpdateEvent.GuildMemberList event) {
        if (!isConnected()) return;
        if (!Managers.Feature.getFeatureInstance(HadesFeature.class)
                .shareWithGuild
                .get()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.GUILD));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.NOT_CONNECTED && Services.WynntilsAccount.isLoggedIn()) {
            connect();
        }

        userRegistry.reset();

        if (event.isFirstJoinWorld()) {
            if (!isConnected()) {
                MutableComponent failed = Component.translatable("service.wynntils.hades.failedToConnect")
                        .withStyle(ChatFormatting.GREEN);
                failed.append(Component.translatable("service.wynntils.hades.clickToConnect1")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
                failed.append(Component.translatable("service.wynntils.hades.clickToConnect2")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.AQUA)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reauth"))));
                failed.append(Component.translatable("service.wynntils.hades.clickToConnect3")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));

                McUtils.sendMessageToClient(failed);

                return;
            }
        }

        tryResendWorldData();

        refreshGear();
    }

    @SubscribeEvent
    public void onAthenaLogin(AthenaLoginEvent event) {
        if (Models.WorldState.getCurrentState() != WorldState.NOT_CONNECTED && !isConnected()) {
            if (Services.WynntilsAccount.isLoggedIn()) {
                login();
            }
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent event) {
        String id = Models.Character.getId();

        characterGearShareOptions.get().putIfAbsent(id, new GearShareOptions());
        characterGearShareOptions.touched();

        tryResendWorldData();
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Post event) {
        if (!event.getContainer().equals(McUtils.inventory())) return;
        if (getGearShareOptions().shouldShare()) {
            for (InventoryAccessory accessory : InventoryAccessory.values()) {
                if (event.getSlot() == accessory.getSlot()) {
                    updateAccessoryCache(accessory);
                    return;
                }
            }

            for (InventoryArmor armorSlot : InventoryArmor.values()) {
                if (event.getSlot() == armorSlot.getInventorySlot()) {
                    updateArmorCache(armorSlot);
                    return;
                }
            }

            if (event.getSlot() == McUtils.player().getInventory().selected) {
                updateHeldItemCache();
            }
        }
    }

    @SubscribeEvent
    public void onSwappedItem(ChangeCarriedItemEvent event) {
        if (getGearShareOptions().shouldShare()) {
            updateHeldItemCache();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!isConnected()) return;
        if (!Models.WorldState.onWorld() || McUtils.player().hasEffect(MobEffects.NIGHT_VISION)) return;
        if (!Managers.Feature.getFeatureInstance(HadesFeature.class)
                        .shareWithParty
                        .get()
                && !Managers.Feature.getFeatureInstance(HadesFeature.class)
                        .shareWithGuild
                        .get()
                && !Managers.Feature.getFeatureInstance(HadesFeature.class)
                        .shareWithFriends
                        .get()) return;

        tickCountUntilUpdate--;

        if (tickCountUntilUpdate <= 0) {
            LocalPlayer player = McUtils.player();

            float pX = (float) player.getX();
            float pY = (float) player.getY();
            float pZ = (float) player.getZ();

            PlayerStatus newStatus;

            if (getGearShareOptions().shouldShare()) {
                newStatus = new PlayerStatus(
                        pX,
                        pY,
                        pZ,
                        Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY),
                        Models.CharacterStats.getMana().orElse(CappedValue.EMPTY),
                        armor.getOrDefault(InventoryArmor.HELMET, ""),
                        armor.getOrDefault(InventoryArmor.CHESTPLATE, ""),
                        armor.getOrDefault(InventoryArmor.LEGGINGS, ""),
                        armor.getOrDefault(InventoryArmor.BOOTS, ""),
                        accessories.getOrDefault(InventoryAccessory.RING_1, ""),
                        accessories.getOrDefault(InventoryAccessory.RING_2, ""),
                        accessories.getOrDefault(InventoryAccessory.BRACELET, ""),
                        accessories.getOrDefault(InventoryAccessory.NECKLACE, ""),
                        heldItem);
            } else {
                newStatus = new PlayerStatus(
                        pX,
                        pY,
                        pZ,
                        Models.CharacterStats.getHealth().orElse(CappedValue.EMPTY),
                        Models.CharacterStats.getMana().orElse(CappedValue.EMPTY));
            }

            if (newStatus.equals(lastSentStatus)) {
                tickCountUntilUpdate = 1;
                return;
            }

            tickCountUntilUpdate = TICKS_PER_UPDATE;

            lastSentStatus = newStatus;

            hadesConnection.sendPacketAndFlush(new HCPacketUpdateStatus(
                    lastSentStatus.x(),
                    lastSentStatus.y(),
                    lastSentStatus.z(),
                    lastSentStatus.health().current(),
                    lastSentStatus.health().max(),
                    lastSentStatus.mana().current(),
                    lastSentStatus.mana().max(),
                    lastSentStatus.helmet(),
                    lastSentStatus.chestplate(),
                    lastSentStatus.leggings(),
                    lastSentStatus.boots(),
                    lastSentStatus.ringOne(),
                    lastSentStatus.ringTwo(),
                    lastSentStatus.bracelet(),
                    lastSentStatus.necklace(),
                    lastSentStatus.heldItem()));
        }
    }

    public void tryResendWorldData() {
        if (!isConnected()) return;

        hadesConnection.sendPacket(new HCPacketUpdateWorld(
                Models.WorldState.getCurrentWorldName(),
                Models.Character.getId().hashCode()));
    }

    public void resetSocialType(SocialType socialType) {
        if (!isConnected()) return;

        hadesConnection.sendPacketAndFlush(new HCPacketSocialUpdate(List.of(), PacketAction.RESET, socialType));
    }

    public void resetHadesUsers() {
        userRegistry.getHadesUserMap().clear();
    }

    public GearShareOptions getGearShareOptions() {
        if (isCharacterGearShareEnabled()) {
            return characterGearShareOptions.get().getOrDefault(Models.Character.getId(), new GearShareOptions());
        }

        return gearShareOptions.get();
    }

    public void toggleCharacterGearShareEnabled() {
        characterGearShareEnabled.get().put(Models.Character.getId(), !isCharacterGearShareEnabled());
        characterGearShareEnabled.touched();
    }

    public boolean isCharacterGearShareEnabled() {
        return characterGearShareEnabled.get().getOrDefault(Models.Character.getId(), false);
    }

    public void saveGearShareOptions() {
        gearShareOptions.touched();
        characterGearShareOptions.touched();
        refreshGear();
    }

    private void refreshGear() {
        if (McUtils.player() == null) return;

        if (getGearShareOptions().shouldShare()) {
            for (InventoryArmor inventoryArmor : InventoryArmor.values()) {
                updateArmorCache(inventoryArmor);
            }

            for (InventoryAccessory inventoryAccessory : InventoryAccessory.values()) {
                updateAccessoryCache(inventoryAccessory);
            }

            updateHeldItemCache();
        } else {
            armor.clear();
            armorCache.clear();
            accessories.clear();
            accessoriesCache.clear();
            heldItem = "";
            heldItemCache = null;
        }
    }

    private boolean isConnected() {
        return hadesConnection != null && hadesConnection.isOpen();
    }

    private void updateArmorCache(InventoryArmor inventoryArmor) {
        Optional<WynnItem> armorItemOpt =
                Models.Item.getWynnItem(McUtils.inventory().armor.get(inventoryArmor.getArmorSlot()));

        if (armorItemOpt.isEmpty()
                || (armorItemOpt.get() instanceof CraftedGearItem
                        && !getGearShareOptions().shareCraftedItems())
                || !getGearShareOptions().shouldShareArmor(inventoryArmor)) {
            armor.remove(inventoryArmor);
            armorCache.remove(inventoryArmor);
        } else if (!armorCache.containsKey(inventoryArmor)
                || !armorCache.get(inventoryArmor).equals(armorItemOpt.get())) {
            this.armor.put(inventoryArmor, encodeItem(armorItemOpt));
            this.armorCache.put(inventoryArmor, armorItemOpt.get());
        }
    }

    private void updateAccessoryCache(InventoryAccessory inventoryAccessory) {
        Optional<WynnItem> accessoryItemOpt =
                Models.Item.getWynnItem(McUtils.inventory().getItem(inventoryAccessory.getSlot()));

        if (accessoryItemOpt.isEmpty()
                || (accessoryItemOpt.get() instanceof CraftedGearItem
                        && !getGearShareOptions().shareCraftedItems())
                || !getGearShareOptions().shouldShareAccessory(inventoryAccessory)) {
            accessories.remove(inventoryAccessory);
            accessoriesCache.remove(inventoryAccessory);
        } else if (!accessoriesCache.containsKey(inventoryAccessory)
                || !accessoriesCache.get(inventoryAccessory).equals(accessoryItemOpt.get())) {
            this.accessories.put(inventoryAccessory, encodeItem(accessoryItemOpt));
            this.accessoriesCache.put(inventoryAccessory, accessoryItemOpt.get());
        }
    }

    private void updateHeldItemCache() {
        Optional<WynnItem> heldItemOpt =
                Models.Item.getWynnItem(McUtils.player().getMainHandItem());

        if (heldItemOpt.isEmpty()
                || (heldItemOpt.get() instanceof CraftedGearItem
                        && !getGearShareOptions().shareCraftedItems())
                || !getGearShareOptions().shouldShareHeldItem()) {
            heldItem = "";
            heldItemCache = null;
        } else if (heldItemCache == null || !heldItemCache.equals(heldItemOpt.get())) {
            heldItem = encodeItem(heldItemOpt);
            heldItemCache = heldItemOpt.get();
        }
    }

    private String encodeItem(Optional<WynnItem> item) {
        if (item.isPresent() && Models.ItemEncoding.canEncodeItem(item.get())) {
            ErrorOr<EncodedByteBuffer> errorOrEncodedByteBuffer =
                    Models.ItemEncoding.encodeItem(item.get(), HADES_ENCODING_SETTINGS);

            if (!errorOrEncodedByteBuffer.hasError()) {
                String itemName = "";

                if (getGearShareOptions().shareCraftedNames()
                        && item.get() instanceof CraftedGearItem craftedGearItem) {
                    itemName = " \"" + craftedGearItem.getName() + "\"";
                }

                return errorOrEncodedByteBuffer.getValue().toUtf16String() + itemName;
            }
        }

        return "";
    }
}
