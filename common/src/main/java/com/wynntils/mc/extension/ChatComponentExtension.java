/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import net.minecraft.network.chat.Component;

public interface ChatComponentExtension {
    void deleteMessage(Component component);
}
