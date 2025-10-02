/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.notifications.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.notifications.MessageContainer;

public class NotificationEvent extends BaseEvent {
    private final MessageContainer messageContainer;

    private NotificationEvent(MessageContainer messageContainer) {
        this.messageContainer = messageContainer;
    }

    public MessageContainer getMessageContainer() {
        return messageContainer;
    }

    public static class Queue extends NotificationEvent {
        public Queue(MessageContainer messageContainer) {
            super(messageContainer);
        }
    }

    public static class Edit extends NotificationEvent {
        public Edit(MessageContainer messageContainer) {
            super(messageContainer);
        }
    }

    public static class Remove extends NotificationEvent {
        public Remove(MessageContainer messageContainer) {
            super(messageContainer);
        }
    }
}
