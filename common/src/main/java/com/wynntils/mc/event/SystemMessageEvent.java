/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import com.wynntils.core.text.StyledText;
import net.minecraft.network.chat.Component;

public abstract class SystemMessageEvent extends BaseEvent implements OperationCancelable {
    private Component message;
    private boolean messageChanged;
    private final StyledText originalStyledText;
    private StyledText styledText;

    protected SystemMessageEvent(Component message) {
        this.message = message;
        this.originalStyledText = StyledText.fromComponent(message);
        this.styledText = originalStyledText;
    }

    public StyledText getOriginalStyledText() {
        return originalStyledText;
    }

    public StyledText getStyledText() {
        return styledText;
    }

    public Component getMessage() {
        return message;
    }

    public void setMessage(Component message) {
        this.message = message;
        this.messageChanged = true;
        this.styledText = StyledText.fromComponent(message);
    }

    public boolean isMessageChanged() {
        return messageChanged;
    }

    public static final class GameInfoReceivedEvent extends SystemMessageEvent {
        public GameInfoReceivedEvent(Component message) {
            super(message);
        }
    }

    public static final class ChatReceivedEvent extends SystemMessageEvent {
        public ChatReceivedEvent(Component message) {
            super(message);
        }
    }
}
