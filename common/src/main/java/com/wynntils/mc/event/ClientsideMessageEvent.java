/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.text.StyledText;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

// Fired when a message is sent to the local chat.
@Cancelable
public class ClientsideMessageEvent extends Event {
    private final Component originalComponent;
    private final StyledText originalStyledText;

    private Component component;
    private StyledText styledText;

    public ClientsideMessageEvent(Component component) {
        this.originalComponent = component;
        this.originalStyledText = StyledText.fromComponent(component);

        this.component = originalComponent;
        this.styledText = originalStyledText;
    }

    public void setMessage(Component component) {
        this.component = component;
        this.styledText = StyledText.fromComponent(component);
    }

    public Component getOriginalComponent() {
        return originalComponent;
    }

    public StyledText getOriginalStyledText() {
        return originalStyledText;
    }

    public Component getComponent() {
        return component;
    }

    public StyledText getStyledText() {
        return styledText;
    }
}
