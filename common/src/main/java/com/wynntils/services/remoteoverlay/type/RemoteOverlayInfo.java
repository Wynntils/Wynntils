/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.remoteoverlay.type;

public class RemoteOverlayInfo {
    private final String name;
    private final String function;
    private final OverlayType type = OverlayType.INFO_BOX;

    public RemoteOverlayInfo(String name, String function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public String getFunction() {
        return function;
    }

    public OverlayType getType() {
        return type;
    }

    public enum OverlayType {
        INFO_BOX,
        CUSTOM_BAR
    }

    @Override
    public String toString() {
        return "RemoteOverlayInfo{name=\"" + name + "\",function=\"" + function + "\"}";
    }
}
