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
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.actionbar.matchers.MountEnergySegmentMatcher;
import com.wynntils.models.mount.actionbar.segments.MountEnergySegment;
import com.wynntils.models.mount.type.ColorType;
import com.wynntils.models.mount.type.MountChoice;
import com.wynntils.models.mount.type.MountColorInfo;
import com.wynntils.models.mount.type.MountColorType;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

public final class MountModel extends Model {
    private Map<String, MountColorInfo> mountColors = new HashMap<>();

    // Parsed from the UI element so is not as accurate as the item tooltip
    private CappedValue currentMountEnergy = CappedValue.EMPTY;

    private boolean hideMountEnergy = false;

    public MountModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new MountEnergySegmentMatcher());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MOUNT_COLORS).handleReader(this::handleMountColors);
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

    public void setHideMountEnergy(boolean hide) {
        hideMountEnergy = hide;
    }

    public Optional<CappedValue> getCurrentMountEnergy() {
        if (currentMountEnergy == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(currentMountEnergy);
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
}
