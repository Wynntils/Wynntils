/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat.type;

public enum NpcDialogueType {
    NONE, // This is a "clear screen" with no real message in it
    NORMAL, // User needs to press sneak to continue
    SELECTION, // User needs to select an option to continue
    CONFIRMATIONLESS // Message needs no confirmation
}
