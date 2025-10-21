/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.utils.render.Texture;

public enum QuickJumpButtonIcon {
    NONE(null),
    ALL_CONFIG(Texture.ALL_CONFIG_ICON),
    CHAT_CONFIG(Texture.CHAT_CONFIG_ICON),
    COMBAT_CONFIG(Texture.COMBAT_CONFIG_ICON),
    COMMANDS_CONFIG(Texture.COMMANDS_CONFIG_ICON),
    DEBUG_CONFIG(Texture.DEBUG_CONFIG_ICON),
    EMBELLISHMENTS_CONFIG(Texture.EMBELLISHMENTS_CONFIG_ICON),
    INVENTORY_CONFIG(Texture.INVENTORY_CONFIG_ICON),
    MAP_CONFIG(Texture.MAP_CONFIG_ICON),
    OVERLAYS_CONFIG(Texture.OVERLAYS_CONFIG_ICON),
    PLAYERS_CONFIG(Texture.PLAYERS_CONFIG_ICON),
    REDIRECTS_CONFIG(Texture.REDIRECTS_CONFIG_ICON),
    TOOLTIPS_CONFIG(Texture.TOOLTIPS_CONFIG_ICON),
    TRADE_MARKET_CONFIG(Texture.TRADE_MARKET_CONFIG_ICON),
    UNCATEGORIZED_CONFIG(Texture.UNCATEGORIZED_CONFIG_ICON),
    UTILITIES_CONFIG(Texture.UTILITIES_CONFIG_ICON),
    WYNNTILS_CONFIG(Texture.WYNNTILS_CONFIG_ICON),
    APPLY_SETTINGS(Texture.APPLY_SETTINGS_ICON),
    DISCARD_SETTINGS(Texture.DISCARD_SETTINGS_ICON),
    IMPORT_SETTINGS(Texture.IMPORT_SETTINGS_ICON),
    EXPORT_SETTINGS(Texture.EXPORT_SETTINGS_ICON),
    UI_CONFIG(Texture.UI_CONFIG_ICON);

    private final Texture texture;

    QuickJumpButtonIcon(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public QuickJumpButtonIcon next() {
        QuickJumpButtonIcon[] values = values();
        int nextOrdinal = (this.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }

    public QuickJumpButtonIcon prev() {
        QuickJumpButtonIcon[] values = values();
        int prevOrdinal = (this.ordinal() - 1 + values.length) % values.length;
        return values[prevOrdinal];
    }
}

