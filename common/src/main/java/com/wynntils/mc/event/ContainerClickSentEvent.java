/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.bus.api.Event;

@EventThread(EventThread.Type.ANY)
public class ContainerClickSentEvent extends Event {
    private final int containerId;
    private final int slotNum;
    private final int mouseButton;
    private final ClickType clickType;

    public ContainerClickSentEvent(int containerId, int slotNum, int mouseButton, ClickType clickType) {
        this.containerId = containerId;
        this.slotNum = slotNum;
        this.mouseButton = mouseButton;
        this.clickType = clickType;
    }

    public int getContainerId() {
        return containerId;
    }

    public int getSlotNum() {
        return slotNum;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public ClickType getClickType() {
        return clickType;
    }
}
