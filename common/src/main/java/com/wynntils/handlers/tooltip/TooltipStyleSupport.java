/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public abstract class TooltipStyleSupport {
    public static final Style SPACING_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("space")))
            .withoutShadow();

    public static final Style RESTRICTION_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/banner")))
            .withoutShadow();

    public static final Style SKILL_FRAME_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/frame")))
            .withoutShadow();

    public static final Style SKILL_SPRITE_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/sprite")))
            .withoutShadow();

    public static final FontDescription WYNNCRAFT_LANGUAGE_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("wynntils", "language"));
    public static final FontDescription TOOLTIP_DIVIDER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"));
    public static final FontDescription TOOLTIP_PAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/page"));
    public static final Style WYNNCRAFT_WHITE_STYLE =
            Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE);

    public static final Component DIVIDER = Component.literal("\uE000")
            .withStyle(Style.EMPTY.withFont(
                    new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"))));

    public static final Style REQUIREMENT_STYLE = Style.EMPTY.withFont(
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/sprite")));

    public static MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(component.copy());
    }
}
