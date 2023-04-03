/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat.transcoder;

import com.google.gson.Gson;
import com.wynntils.core.components.Manager;
import java.util.List;
import net.minecraft.network.chat.Component;

public class ChatTranscoderManager extends Manager {
    public static final Gson GSON = new Gson();

    public ChatTranscoderManager() {
        super(List.of());
    }

    public StyleString fromComponent(Component component) {
        return new StyleString(component);
    }
}
