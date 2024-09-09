/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.wynntils.utils.type.Pair;
import java.time.LocalDateTime;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public interface GuiMessageLineExtension {
    LocalDateTime getCreated();

    void setCreated(LocalDateTime date);

    Optional<Pair<Component, Integer>> getTimestamp();

    void setTimestamp(Component component);
}
