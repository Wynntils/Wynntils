/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.SetLocalPlayerVehicleEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.actionbar.matchers.MountEnergySegmentMatcher;
import com.wynntils.models.mount.actionbar.segments.MountEnergySegment;
import com.wynntils.models.mount.event.MountEvent;
import com.wynntils.models.mount.type.ColorType;
import com.wynntils.models.mount.type.MountChoice;
import com.wynntils.models.mount.type.MountColorInfo;
import com.wynntils.models.mount.type.MountColorType;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import com.wynntils.utils.type.CappedValue;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class MountModel extends Model {
    // How long we wait before assuming mount failure
    private static final int MOUNT_TIME_TICKS = 10;

    private Map<String, MountColorInfo> mountColors = new HashMap<>();

    // Parsed from the UI element so is not as accurate as the item tooltip
    private CappedValue currentMountEnergy = CappedValue.EMPTY;

    private boolean hideMountEnergy = false;
    private int summonTick = -1;
    private MountType expectedMountType = null;
    private Optional<MountType> currentMountType = Optional.empty();

    public MountModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new MountEnergySegmentMatcher());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MOUNT_COLORS).handleReader(this::handleMountColors);
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.InteractAt event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onUseItemOn(PlayerInteractEvent.RightClickBlock event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (summonTick == -1) return;

        int currentTick = McUtils.player().tickCount;
        if (currentTick - summonTick > MOUNT_TIME_TICKS) {
            // Assume failed to mount
            summonTick = -1;
            expectedMountType = null;
        }
    }

    @SubscribeEvent
    public void onVehicleChange(SetLocalPlayerVehicleEvent event) {
        if (!Models.WorldState.onWorld()) return;

        if (event.getVehicle() == null && currentMountType.isPresent()) {
            currentMountType = Optional.empty();
            WynntilsMod.postEvent(new MountEvent.Dismount());
        } else if (summonTick != -1) {
            currentMountType = Optional.of(expectedMountType);
            expectedMountType = null;
            WynntilsMod.postEvent(new MountEvent.Mount(currentMountType.get()));
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        summonTick = -1;
        expectedMountType = null;
        currentMountType = Optional.empty();
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        if (!hideMountEnergy) return;

        event.setSegmentEnabled(MountEnergySegment.class, false);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(MountEnergySegment.class, this::updateMountEnergy, this::clearMountEnergy);
    }

    public void tryRideMount(MountChoice mountChoice) {
        if (!Models.WorldState.onWorld()) return;

        LocalPlayer player = McUtils.player();
        if (player.getVehicle() != null) {
            postMountErrorMessage(RideMountStatus.ALREADY_RIDING);
            return;
        }

        int mountInventorySlot = findMountSlotNum(mountChoice);
        if (mountInventorySlot == -1) {
            postMountErrorMessage(RideMountStatus.NO_MOUNT);
            return;
        }
        if (mountInventorySlot > 8) {
            postMountErrorMessage(RideMountStatus.CONFLICTING_SLOTS);
            return;
        }

        Optional<MountItem> mountItem = getMount(mountChoice);
        if (mountItem.isEmpty()) {
            postMountErrorMessage(RideMountStatus.NO_MOUNT);
            return;
        } else {
            expectedMountType = mountItem.get().getMountType();
        }

        WynntilsMod.postEvent(new MountEvent.Summon(expectedMountType));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(mountInventorySlot));
        Managers.TickScheduler.scheduleNextTick(() -> {
            MouseUtils.sendRightClickInput();
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(McUtils.inventory().selected));
            summonTick = McUtils.player().tickCount;
        });
    }

    public Optional<MountItem> getMount(MountChoice mountChoice) {
        int mountSlot = findMountSlotNum(mountChoice);
        if (mountSlot == -1) return Optional.empty();

        return Models.Item.asWynnItem(McUtils.inventory().getItem(mountSlot), MountItem.class);
    }

    public int findMountSlotNum(MountChoice mountChoice) {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);
            Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(itemStack, MountItem.class);

            if (mountItemOpt.isPresent()) {
                if (mountChoice == MountChoice.FIRST
                        || mountChoice.getMountType() == mountItemOpt.get().getMountType()) {
                    return slotNum;
                }
            }
        }

        return -1;
    }

    public MountColorInfo getMountColor(int id) {
        return mountColors.values().stream()
                .filter(mountColorInfo -> mountColorInfo.id() == id)
                .findFirst()
                .orElse(MountColorInfo.UNKNOWN);
    }

    public MountColorInfo getMountColor(String displayName) {
        MountColorInfo mountColorInfo = mountColors.get(displayName);

        if (mountColorInfo == null) {
            WynntilsMod.warn("Unknown mount color info: " + displayName);
            return MountColorInfo.UNKNOWN;
        }

        return mountColorInfo;
    }

    public boolean isValidColor(MountColorInfo mountColorInfo, MountType mountType, ColorType colorType) {
        boolean validColor = false;
        for (MountColorType mountColorType : mountColorInfo.mounts()) {
            if (mountColorType.mount() == mountType && mountColorType.type() == colorType) {
                validColor = true;
                break;
            }
        }

        return validColor;
    }

    public Optional<MountType> getCurrentMountType() {
        return currentMountType;
    }

    public void setHideMountEnergy(boolean hide) {
        hideMountEnergy = hide;
    }

    public Optional<CappedValue> getCurrentMountEnergy() {
        if (currentMountEnergy == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(currentMountEnergy);
    }

    private void handleMountItemUse() {
        if (!Models.WorldState.onWorld()) return;

        ItemStack itemStack = McUtils.inventory().getSelectedItem();
        Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(itemStack, MountItem.class);
        if (mountItemOpt.isEmpty()) return;
        if (!mountItemOpt.get().isSummonItem()) return;

        expectedMountType = mountItemOpt.get().getMountType();

        summonTick = McUtils.player().tickCount;
        WynntilsMod.postEvent(new MountEvent.Summon(expectedMountType));
    }

    private void handleMountColors(Reader reader) {
        Type type = new TypeToken<List<MountColorInfo>>() {}.getType();
        List<MountColorInfo> mountColorsList = Managers.Json.GSON.fromJson(reader, type);

        mountColors = mountColorsList.stream()
                .collect(Collectors.toMap(MountColorInfo::displayName, mountColorInfo -> mountColorInfo));
    }

    private void updateMountEnergy(MountEnergySegment segment) {
        currentMountEnergy = segment.getCappedEnergy();
    }

    private void clearMountEnergy() {
        currentMountEnergy = CappedValue.EMPTY;
    }

    private void postMountErrorMessage(RideMountStatus status) {
        Managers.Notification.queueMessage(
                Component.translatable(status.getTranslationKey()).withStyle(ChatFormatting.DARK_RED));
    }

    private enum RideMountStatus {
        NO_MOUNT("model.wynntils.mount.noMount"),
        ALREADY_RIDING("model.wynntils.mount.alreadyRiding"),
        CONFLICTING_SLOTS("model.wynntils.mount.conflictingSlots");

        private final String translationKey;

        RideMountStatus(String tcString) {
            this.translationKey = tcString;
        }

        private String getTranslationKey() {
            return this.translationKey;
        }
    }
}
