/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.text.StyledText;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

// Fired when a message is sent to the local chat.
@Cancelable
public class ClientsideMessageEvent extends Event {
    private final StyledText originalStyledText;

    private StyledText styledText;

    public ClientsideMessageEvent(Component component) {
        this.originalStyledText = StyledText.fromComponent(component);
        this.styledText = originalStyledText;
    }

    public void setMessage(StyledText styledText) {
        this.styledText = styledText;
    }

    public StyledText getOriginalStyledText() {
        return originalStyledText;
    }

    public StyledText getStyledText() {
        return styledText;
    }
}
