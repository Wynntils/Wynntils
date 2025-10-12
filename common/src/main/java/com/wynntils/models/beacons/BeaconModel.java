/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.EntityPositionSyncEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.beacons.event.BeaconEvent;
import com.wynntils.models.beacons.event.BeaconMarkerEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconKind;
import com.wynntils.models.beacons.type.BeaconMarker;
import com.wynntils.models.beacons.type.BeaconMarkerKind;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.PreciseLocation;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class BeaconModel extends Model {
    private static final Pattern MARKER_DISTANCE_PATTERN = Pattern.compile("\n(\\d+)m (§[a-z0-9])?(\uE000|\uE001)?");
    private static final Pattern MARKER_COLOR_PATTERN = Pattern.compile("§((?:#)?([a-z0-9]{1,8}))");

    private static final ResourceLocation MARKER_FONT = ResourceLocation.withDefaultNamespace("marker");
    private static final List<BeaconKind> beaconRegistry = new ArrayList<>();
    private static final List<BeaconMarkerKind> beaconMarkerRegistry = new ArrayList<>();
    // Maps base entity id to corresponding beacon
    private final Map<Integer, Beacon> beacons = new Int2ObjectArrayMap<>();
    private final Map<Integer, BeaconMarker> beaconMarkers = new Int2ObjectArrayMap<>();

    public static final String BEACON_COLOR_CUSTOM_MODEL_DATA_KEY = "beacon_color";

    public BeaconModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (entity instanceof Display.ItemDisplay) {
            SynchedEntityData.DataValue<?> dataValue = event.getPackedItems().stream()
                    .filter(data -> data.id() == Display.ItemDisplay.DATA_ITEM_STACK_ID.id())
                    .findFirst()
                    .orElse(null);
            if (dataValue == null) return;

            ItemStack itemStack = (ItemStack) dataValue.value();

            // Try to identify the beacon kind, when the display item's data is set
            BeaconKind beaconKind = beaconKindFromItemStack(itemStack);

            if (beaconKind == null) return;

            Beacon beacon = new Beacon(PreciseLocation.fromVec(entity.position()), beaconKind);
            beacons.put(event.getId(), beacon);
            WynntilsMod.postEvent(new BeaconEvent.Added(beacon, entity));
        } else if (entity instanceof Display.TextDisplay textDisplay) {
            StyledText styledText = StyledText.fromComponent(textDisplay.getText());
            if (styledText.getPartCount() == 0) return;
            if (!styledText.getFirstPart().getPartStyle().getFont().equals(MARKER_FONT)) return;

            BeaconMarkerKind beaconMarkerKind = beaconMarkerKindFromIcon(styledText);

            if (beaconMarkerKind == null) return;

            Optional<Integer> distanceOpt = getDistance(styledText);
            Optional<CustomColor> customColorOpt = getCustomColor(styledText);

            BeaconMarker beaconMarker =
                    new BeaconMarker(entity.position(), beaconMarkerKind, distanceOpt, customColorOpt);
            beaconMarkers.put(event.getId(), beaconMarker);
            WynntilsMod.postEvent(new BeaconMarkerEvent.Added(beaconMarker, entity));
        }
    }

    @SubscribeEvent
    public void onEntityPositionSync(EntityPositionSyncEvent event) {
        Beacon movedBeacon = beacons.get(event.getEntity().getId());
        BeaconMarker movedBeaconMarker = beaconMarkers.get(event.getEntity().getId());
        if (movedBeacon != null) {
            Beacon newBeacon = new Beacon(PreciseLocation.fromVec(event.getNewPosition()), movedBeacon.beaconKind());
            // Replace the old map entry
            beacons.put(event.getEntity().getId(), newBeacon);
            WynntilsMod.postEvent(new BeaconEvent.Moved(movedBeacon, newBeacon));
        } else if (movedBeaconMarker != null) {
            BeaconMarker newBeaconMarker = new BeaconMarker(
                    event.getNewPosition(),
                    movedBeaconMarker.beaconMarkerKind(),
                    movedBeaconMarker.distance(),
                    movedBeaconMarker.color());
            // Replace the old map entry
            beaconMarkers.put(event.getEntity().getId(), newBeaconMarker);
            WynntilsMod.postEvent(new BeaconMarkerEvent.Moved(movedBeaconMarker, newBeaconMarker));
        }
    }

    @SubscribeEvent
    public void onEntityRemoved(RemoveEntitiesEvent event) {
        event.getEntityIds().stream().filter(beacons::containsKey).forEach(entityId -> {
            Beacon removedBeacon = beacons.get(entityId);
            beacons.remove(entityId);
            WynntilsMod.postEvent(new BeaconEvent.Removed(removedBeacon));
        });

        event.getEntityIds().stream().filter(beaconMarkers::containsKey).forEach(entityId -> {
            BeaconMarker removedBeaconMarker = beaconMarkers.get(entityId);
            beaconMarkers.remove(entityId);
            WynntilsMod.postEvent(new BeaconMarkerEvent.Removed(removedBeaconMarker));
        });
    }

    public void registerBeacon(BeaconKind beaconKind) {
        beaconRegistry.add(beaconKind);
    }

    public void registerBeaconMarker(BeaconMarkerKind beaconMarkerKind) {
        beaconMarkerRegistry.add(beaconMarkerKind);
    }

    private static BeaconKind beaconKindFromItemStack(ItemStack itemStack) {
        for (BeaconKind beaconKind : beaconRegistry) {
            if (beaconKind.matches(itemStack)) {
                return beaconKind;
            }
        }

        if (WynntilsMod.isDevelopmentEnvironment()) {
            if (itemStack.getItem() != Items.POTION) return null;

            Optional<Float> beaconColorCustomModelData =
                    Services.CustomModel.getFloat(BEACON_COLOR_CUSTOM_MODEL_DATA_KEY);
            if (beaconColorCustomModelData.isEmpty()) return null;

            // Extract custom color from potion
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null) return null;

            // Extract custom model data from potion
            CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (customModelData == null) return null;

            List<Float> customModelValues = customModelData.floats().stream()
                    .filter(value -> beaconRegistry.stream()
                            .map(BeaconKind::getCustomModelData)
                            .anyMatch(value::equals))
                    .toList();
            if (customModelValues.isEmpty()) return null;

            // Extract custom color from potion
            // If there is no custom color, assume it's white
            int customColor = potionContents.customColor().orElse(CommonColors.WHITE.asInt());

            // Log the color if it's likely to be a new beacon kind
            if (customModelValues.stream().anyMatch(beaconColorCustomModelData.get()::equals)) {
                WynntilsMod.warn("Unknown beacon kind: " + beaconColorCustomModelData.get() + " " + customColor);
            }
        }

        return null;
    }

    private static BeaconMarkerKind beaconMarkerKindFromIcon(StyledText iconStyledText) {
        for (BeaconMarkerKind beaconMarkerKind : beaconMarkerRegistry) {
            if (beaconMarkerKind.matches(iconStyledText)) {
                return beaconMarkerKind;
            }
        }

        return null;
    }

    private static Optional<Integer> getDistance(StyledText styledText) {
        Optional<Integer> distanceOpt = Optional.empty();

        Matcher distanceMatcher = styledText.getMatcher(MARKER_DISTANCE_PATTERN);
        if (distanceMatcher.find()) {
            distanceOpt = Optional.of(Integer.parseInt(distanceMatcher.group(1)));
        }

        return distanceOpt;
    }

    private static Optional<CustomColor> getCustomColor(StyledText styledText) {
        Optional<CustomColor> colorOpt = Optional.empty();

        Matcher colorMatcher = styledText.getMatcher(MARKER_COLOR_PATTERN);
        if (colorMatcher.find()) {
            String colorStr = colorMatcher.group(1);

            if (colorStr.startsWith("#")) {
                colorOpt = Optional.of(CustomColor.fromHexString(colorMatcher.group(1)));
            } else {
                colorOpt = Optional.of(CustomColor.fromChatFormatting(ChatFormatting.getByCode(colorStr.charAt(0))));
            }
        }

        return colorOpt;
    }
}
