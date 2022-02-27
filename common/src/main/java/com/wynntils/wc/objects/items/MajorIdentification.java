/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import net.minecraft.ChatFormatting;

public class MajorIdentification {

    String name;
    String description;

    public MajorIdentification(String name, String description) {}

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
