/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

public enum RenderElementType {
    GUI_PRE(true),
    CAMERA_OVERLAYS(true),
    CROSSHAIR(true),
    HOTBAR(true),
    SELECTED_ITEM(false),
    BOSS_BARS(true),
    SCOREBOARD(true),
    ACTION_BAR(true),
    TITLE(true),
    CHAT(true),
    PLAYER_TAB_LIST(true),
    GUI_POST(true);

    // Whether the element type is not called in Gui#render or not
    private final boolean rootRender;

    RenderElementType(boolean rootRender) {
        this.rootRender = rootRender;
    }

    public boolean isRootRender() {
        return rootRender;
    }

    public static RenderElementType[] overlayValues() {
        return new RenderElementType[] {
            CAMERA_OVERLAYS,
            CROSSHAIR,
            HOTBAR,
            BOSS_BARS,
            SCOREBOARD,
            ACTION_BAR,
            TITLE,
            CHAT,
            PLAYER_TAB_LIST,
            GUI_POST
        };
    }
}
