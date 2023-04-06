/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.text.CodedString;
import com.wynntils.utils.mc.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

// Fired when a message is sent to the local chat.
@Cancelable
public class ClientsideMessageEvent extends Event {
    private final Component originalComponent;
    private final CodedString originalCodedMessage;

    private Component component;
    private CodedString codedMessage;

    public ClientsideMessageEvent(Component component) {
        this.originalComponent = component;
        this.originalCodedMessage = ComponentUtils.getCoded(component);

        this.component = originalComponent;
        this.codedMessage = originalCodedMessage;
    }

    public void setMessage(Component component) {
        this.component = component;
        this.codedMessage = ComponentUtils.getCoded(component);
    }

    public Component getOriginalComponent() {
        return originalComponent;
    }

    public CodedString getOriginalCodedMessage() {
        return originalCodedMessage;
    }

    public Component getComponent() {
        return component;
    }

    public CodedString getCodedMessage() {
        return codedMessage;
    }
}
