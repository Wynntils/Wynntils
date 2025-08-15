/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.type;

import com.wynntils.models.elements.type.Element;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public record MobElementals(List<Element> weaknesses, List<Element> damages, List<Element> defenses) {
    public static final MobElementals EMPTY = new MobElementals(List.of(), List.of(), List.of());

    public Component getFormatted() {
        MutableComponent text = Component.empty();

        appendElementGroup(text, weaknesses, "Weak", !damages.isEmpty() || !defenses.isEmpty());
        appendElementGroup(text, damages, "Dam", !defenses.isEmpty());
        appendElementGroup(text, defenses, "Def", false);

        return text;
    }

    private void appendElementGroup(MutableComponent text, List<Element> elements, String label, boolean addSpace) {
        if (elements.isEmpty()) return;

        for (Element element : elements) {
            text.append(Component.literal(element.getSymbol())
                    .withStyle(Style.EMPTY
                            .withFont(ResourceLocation.withDefaultNamespace("common"))
                            .withColor(element.getColorCode())));
        }

        // The label uses the colour of the last element in the group, can't use the same style as the symbol uses
        // a different font
        ChatFormatting lastColor = elements.getLast().getColorCode();
        text.append(Component.literal(label + (addSpace ? " " : "")).withStyle(Style.EMPTY.withColor(lastColor)));
    }
}
