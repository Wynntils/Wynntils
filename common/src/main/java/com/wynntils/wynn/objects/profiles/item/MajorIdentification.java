/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import net.minecraft.ChatFormatting;

public class MajorIdentification {
    private final String name;
    private final String description;

    public MajorIdentification(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String asLore() {
        return ChatFormatting.AQUA + "+" + name + ": " + ChatFormatting.DARK_AQUA + description;
    }
}
