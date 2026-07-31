/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.utils.render.Texture;
import net.minecraft.ChatFormatting;

public enum MaterialType {
    ORE(ProfessionType.MINING, ChatFormatting.WHITE, Texture.MINING),
    LOG(ProfessionType.WOODCUTTING, ChatFormatting.GOLD, Texture.WOODCUTTING),
    CROP(ProfessionType.FARMING, ChatFormatting.YELLOW, Texture.FARMING),
    FISH(ProfessionType.FISHING, ChatFormatting.AQUA, Texture.FISHING);

    private final ProfessionType professionType;
    private final ChatFormatting labelColor;
    private final Texture materialTexture;

    MaterialType(ProfessionType professionType, ChatFormatting labelColor, Texture materialTexture) {
        this.professionType = professionType;
        this.labelColor = labelColor;
        this.materialTexture = materialTexture;
    }

    public ProfessionType getProfessionType() {
        return professionType;
    }

    public ChatFormatting getLabelColor() {
        return labelColor;
    }

    public Texture getMaterialTexture() {
        return materialTexture;
    }
}
