/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.hades.protocol.packets.server.HSPacketUpdateMutual;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.services.hades.type.PlayerRelation;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.ErrorOr;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;

public class HadesUser {
    private final UUID uuid;
    private final String name;

    private PlayerRelation relation;
    private float x, y, z;
    private PoiLocation poiLocation;
    private CappedValue health;
    private CappedValue mana;
    // Decoded items for use in features
    private NavigableMap<InventoryArmor, WynnItem> armor = new TreeMap<>();
    private NavigableMap<InventoryAccessory, WynnItem> accessories = new TreeMap<>();
    private WynnItem heldItem = null;
    // Encoded string cache to avoid unnecessary decoding
    private Map<InventoryArmor, String> armorCache = new TreeMap<>();
    private Map<InventoryAccessory, String> accessoriesCache = new TreeMap<>();
    private String heldItemCache = "";

    public HadesUser(HSPacketUpdateMutual packet) {
        uuid = packet.getUser();
        name = packet.getName();

        this.updateFromPacket(packet);
    }

    // Dummy constructor for previews
    public HadesUser(String name, CappedValue health, CappedValue mana) {
        this.uuid = UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"); // Steve
        this.name = name;

        this.x = 0;
        this.y = 0;
        this.z = 0;

        this.poiLocation = new PoiLocation(0, 0, 0);

        this.relation = PlayerRelation.FRIEND;

        this.health = health;
        this.mana = mana;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public PoiLocation getMapLocation() {
        return poiLocation;
    }

    public CappedValue getHealth() {
        return health;
    }

    public CappedValue getMana() {
        return mana;
    }

    public NavigableMap<InventoryArmor, WynnItem> getArmor() {
        return Collections.unmodifiableNavigableMap(armor);
    }

    public NavigableMap<InventoryAccessory, WynnItem> getAccessories() {
        return Collections.unmodifiableNavigableMap(accessories);
    }

    public WynnItem getHeldItem() {
        return heldItem;
    }

    public void updateFromPacket(HSPacketUpdateMutual packet) {
        this.x = packet.getX();
        this.y = packet.getY();
        this.z = packet.getZ();
        this.poiLocation = new PoiLocation((int) x, (int) y, (int) z);

        this.health = new CappedValue(packet.getHealth(), packet.getMaxHealth());
        this.mana = new CappedValue(packet.getMana(), packet.getMaxMana());

        handleArmorData(InventoryArmor.HELMET, GearType.HELMET, packet.getHelmet());
        handleArmorData(InventoryArmor.CHESTPLATE, GearType.CHESTPLATE, packet.getChestplate());
        handleArmorData(InventoryArmor.LEGGINGS, GearType.LEGGINGS, packet.getLeggings());
        handleArmorData(InventoryArmor.BOOTS, GearType.BOOTS, packet.getBoots());

        handleAccessoryData(InventoryAccessory.RING_1, GearType.RING, packet.getRingOne());
        handleAccessoryData(InventoryAccessory.RING_2, GearType.RING, packet.getRingTwo());
        handleAccessoryData(InventoryAccessory.BRACELET, GearType.BRACELET, packet.getBracelet());
        handleAccessoryData(InventoryAccessory.NECKLACE, GearType.NECKLACE, packet.getNecklace());

        if (packet.getHeldItem().isEmpty()) {
            this.heldItem = null;
            this.heldItemCache = "";
        } else if (!this.heldItemCache.equals(packet.getHeldItem())) {
            ErrorOr<WynnItem> errorOrDecodedItem = decodeItem(packet.getHeldItem());

            if (errorOrDecodedItem.hasError()) {
                WynntilsMod.warn("Failed to decode Hades user held item: " + errorOrDecodedItem.getError());
            } else {
                WynnItem item = errorOrDecodedItem.getValue();

                if (item instanceof GearTypeItemProperty gearItemType
                        && gearItemType.getGearType().isWeapon()) {
                    this.heldItem = item;
                    this.heldItemCache = packet.getHeldItem();
                }
            }
        }

        this.relation = packet.isPartyMember()
                ? PlayerRelation.PARTY
                : packet.isMutualFriend()
                        ? PlayerRelation.FRIEND
                        : packet.isGuildMember() ? PlayerRelation.GUILD : PlayerRelation.OTHER;
    }

    public CustomColor getRelationColor() {
        return relation != null ? relation.getRelationColor() : CommonColors.WHITE;
    }

    public PlayerRelation getRelation() {
        return relation;
    }

    private void handleArmorData(InventoryArmor armor, GearType expectedGearType, String armorData) {
        if (armorData.isEmpty()) {
            this.armor.remove(armor);
            this.armorCache.remove(armor);
        } else if (!this.armorCache.getOrDefault(armor, "").equals(armorData)) {
            ErrorOr<WynnItem> errorOrDecodedItem = decodeItem(armorData);

            if (errorOrDecodedItem.hasError()) {
                WynntilsMod.warn("Failed to decode Hades user " + armor + ": " + errorOrDecodedItem.getError());
            } else {
                WynnItem item = errorOrDecodedItem.getValue();

                if (item instanceof GearTypeItemProperty gearItemType
                        && gearItemType.getGearType() == expectedGearType) {
                    this.armor.put(armor, errorOrDecodedItem.getValue());
                    this.armorCache.put(armor, armorData);
                }
            }
        }
    }

    private void handleAccessoryData(InventoryAccessory accessory, GearType expectedGearType, String accessoryData) {
        if (accessoryData.isEmpty()) {
            this.accessories.remove(accessory);
            this.accessoriesCache.remove(accessory);
        } else if (!this.accessoriesCache.getOrDefault(accessory, "").equals(accessoryData)) {
            ErrorOr<WynnItem> errorOrDecodedItem = decodeItem(accessoryData);

            if (errorOrDecodedItem.hasError()) {
                WynntilsMod.warn("Failed to decode Hades user " + accessory + ": " + errorOrDecodedItem.getError());
            } else {
                WynnItem item = errorOrDecodedItem.getValue();

                if (item instanceof GearTypeItemProperty gearItemType
                        && gearItemType.getGearType() == expectedGearType) {
                    this.accessories.put(accessory, errorOrDecodedItem.getValue());
                    this.accessoriesCache.put(accessory, accessoryData);
                }
            }
        }
    }

    private ErrorOr<WynnItem> decodeItem(String encodedData) {
        Matcher encodedMatcher = Models.ItemEncoding.getEncodedDataPattern().matcher(encodedData);
        if (!encodedMatcher.find()) {
            return ErrorOr.error("Failed to match encoded pattern for Hades user data: " + encodedData);
        }

        EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(encodedMatcher.group("data"));
        return Models.ItemEncoding.decodeItem(encodedByteBuffer, encodedMatcher.group("name"));
    }
}
