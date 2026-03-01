/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum ConsumableType {
    POTION(0, "\uE027"),
    FOOD(1, "\uE033"),
    SCROLL(2, "\uE032"),
    // This is a fallback for when the type is unknown and can't be parsed.
    CONSUMABLE(3, "");

    private final int encodingId;
    private final String frameSpriteCode;

    ConsumableType(int encodingId, String frameSpriteCode) {
        this.encodingId = encodingId;
        this.frameSpriteCode = frameSpriteCode;
    }

    public static ConsumableType fromString(String name) {
        for (ConsumableType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }

        return ConsumableType.CONSUMABLE;
    }

    public static ConsumableType fromEncodingId(int id) {
        for (ConsumableType value : values()) {
            if (value.encodingId == id) {
                return value;
            }
        }

        return null;
    }

    public static ConsumableType fromFrameSprite(String frameSpriteCode) {
        for (ConsumableType value : values()) {
            if (value.frameSpriteCode.equals(frameSpriteCode)) {
                return value;
            }
        }

        return ConsumableType.CONSUMABLE;
    }

    public int getEncodingId() {
        return encodingId;
    }

    public String getFrameSpriteCode() {
        return frameSpriteCode;
    }
}
