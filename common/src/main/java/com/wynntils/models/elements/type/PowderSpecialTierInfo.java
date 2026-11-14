/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import java.math.BigDecimal;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public record PowderSpecialTierInfo(
        Powder element,
        int tier,
        int encodingId,
        String name,
        boolean weaponSpecial,
        double valueOne,
        Double valueTwo,
        Component description) {
    public Component getDescription() {
        String firstValue = BigDecimal.valueOf(valueOne).stripTrailingZeros().toPlainString();
        String secondValue = valueTwo == null
                ? ""
                : BigDecimal.valueOf(valueTwo).stripTrailingZeros().toPlainString();
        MutableComponent formattedDescription = Component.empty();
        description.visit(
                (style, text) -> {
                    formattedDescription.append(
                            Component.literal(text.replace("%1$s", firstValue).replace("%2$s", secondValue))
                                    .withStyle(style));
                    return Optional.empty();
                },
                Style.EMPTY);
        return formattedDescription;
    }
}
