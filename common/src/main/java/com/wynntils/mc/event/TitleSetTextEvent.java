/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import net.minecraft.network.chat.Component;

public class TitleSetTextEvent extends BaseEvent implements OperationCancelable {
    private final Component component;

    public TitleSetTextEvent(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }
}
