/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.text.StyledText;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.colors.CustomColor;

import java.util.UUID;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;

// Functions are accessed via reflection
@SuppressWarnings("unused")
public class StyledTextFunctions {

    @TemplateFunction(name = "concat_styled_text", aliases = "concat_st")
    public static StyledText concatStyledTextFunction(StyledText a, StyledText b) {
        return StyledText.join("", a, b);
    }

    @TemplateFunction(name = "concat_styled_text", aliases = "concat_st")
    public static StyledText concatStyledTextFunction(StyledText... values) {
        return StyledText.join("", values);
    }

    @TemplateFunction(name = "styled_text", aliases = "st", isPure = true)
    public static StyledText styledTextFunction(String value) {
        return StyledText.fromString(value);
    }

    @TemplateFunction(name = "with_color")
    public static StyledText withColorFunction(StyledText styledText, CustomColor customColor) {
        return styledText.map(part -> {
            if (part.getPartStyle().getColor() != CustomColor.NONE) {
                return part;
            }
            return part.withStyle(style -> style.withColor(customColor));
        });
    }

    @TemplateFunction(name = "with_bold")
    public static StyledText withBoldFunction(StyledText styledText, boolean isBold) {
        return styledText.map(part -> part.withStyle(style -> style.withBold(isBold)));
    }

    @TemplateFunction(name = "with_bold")
    public static StyledText withBoldFunction(StyledText styledText) {
        return withBoldFunction(styledText, true);
    }

    @TemplateFunction(name = "with_italic")
    public static StyledText withItalicFunction(StyledText styledText, boolean isItalic) {
        return styledText.map(part -> part.withStyle(style -> style.withItalic(isItalic)));
    }

    @TemplateFunction(name = "with_italic")
    public static StyledText withItalicFunction(StyledText styledText) {
        return withItalicFunction(styledText, true);
    }

    @TemplateFunction(name = "with_strikethrough")
    public static StyledText withStrikeThroughFunction(StyledText styledText, boolean isStrikeThrough) {
        return styledText.map(part -> part.withStyle(style -> style.withStrikethrough(isStrikeThrough)));
    }

    @TemplateFunction(name = "with_strikethrough")
    public static StyledText withStrikeThroughFunction(StyledText styledText) {
        return withStrikeThroughFunction(styledText, true);
    }

    @TemplateFunction(name = "with_obfuscated")
    public static StyledText withObfuscatedFunction(StyledText styledText, boolean isObfuscated) {
        return styledText.map(part -> part.withStyle(style -> style.withObfuscated(isObfuscated)));
    }

    @TemplateFunction(name = "with_obfuscated")
    public static StyledText withObfuscatedFunction(StyledText styledText) {
        return withObfuscatedFunction(styledText, true);
    }

    @TemplateFunction(name = "with_atlas_sprite_font")
    public static StyledText withAtlasSpriteFontFunction(StyledText styledText, String atlas, String sprite) {
        Identifier atlasLocation = Identifier.tryParse(atlas);
        Identifier spriteLocation = Identifier.tryParse(sprite);
        if (atlasLocation == null || spriteLocation == null)
            return styledText;
        FontDescription fontDescription = new FontDescription.AtlasSprite(atlasLocation, spriteLocation);
        return styledText.map(part -> {
            if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                return part;
            }
            return part.withStyle(style -> style.withFont(fontDescription));
        });
    }

    @TemplateFunction(name = "with_player_sprite_font")
    public static StyledText withPlayerSpriteFontFunction(StyledText styledText, String uuid, boolean hat) {
        UUID uuidObject;
        try {
            uuidObject = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return styledText;
        }
        FontDescription fontDescription = new FontDescription.PlayerSprite(ResolvableProfile.createUnresolved(uuidObject), hat);
        return styledText.map(part -> {
            if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                return part;
            }
            return part.withStyle(style -> style.withFont(fontDescription));
        });
    }

    @TemplateFunction(name = "with_resource_font", aliases = "with_font")
    public static StyledText withResourceFontFunction(StyledText styledText, String font) {
        Identifier fontLocation = Identifier.tryParse(font);
        if (fontLocation == null)
            return styledText;
        FontDescription fontDescription = new FontDescription.Resource(fontLocation);
        return styledText.map(part -> {
            if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                return part;
            }
            return part.withStyle(style -> style.withFont(fontDescription));
        });
    }

    @TemplateFunction(name = "with_shadow_color")
    public static StyledText withShadowColorFunction(StyledText styledText, CustomColor customColor) {
        return styledText.map(part -> {
            if (part.getPartStyle().getShadowColor() != CustomColor.NONE) {
                return part;
            }
            return part.withStyle(style -> style.withShadowColor(customColor));
        });
    }

    @TemplateFunction(name = "with_underlined")
    public static StyledText withUnderlinedFunction(StyledText styledText, boolean isUnderlined) {
        return styledText.map(part -> part.withStyle(style -> style.withUnderlined(isUnderlined)));
    }

    @TemplateFunction(name = "with_underlined")
    public static StyledText withUnderlinedFunction(StyledText styledText) {
        return withUnderlinedFunction(styledText, true);
    }

    @TemplateFunction(name = "repeat_styled_text", aliases = {"repeat_st"})
    public static StyledText repeatStyledTextFunction(StyledText value, int count) {
        StyledText styledText = value;
        Integer times = count;
        return styledText.repeat(times);
    }
}
