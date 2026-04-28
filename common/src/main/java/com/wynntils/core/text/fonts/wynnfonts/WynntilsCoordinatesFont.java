/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts.wynnfonts;

import com.wynntils.core.text.fonts.RegisteredFont;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.overlays.minimap.CoordinatesOverlay;
import com.wynntils.utils.type.CardinalDirection;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class WynntilsCoordinatesFont extends RegisteredFont {
    private static final FontDescription WYNNTILS_COORDINATES_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("wynntils", "coordinates"));
    private static final Component NEGATIVE_SPACE_CHARACTER = Component.literal("\uDAFF\uDFFF")
            .withStyle(Style.EMPTY.withFont(
                    new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "space"))));

    public WynntilsCoordinatesFont() {
        super("wynntils_coordinates");
    }

    public static Component buildCoordinates(
            CoordinatesOverlay.CompassStyle compassStyle,
            CoordinatesOverlay.CompassDirectionYPos compassDirectionYPos,
            CoordinatesOverlay.CompassEnd compassEnd,
            CardinalDirection lastCardinalDirection,
            BlockPos lastBlockPos) {
        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(WYNNTILS_COORDINATES_FONT).withColor(ChatFormatting.WHITE));
        char[] lastCardinalDirectionChars = lastCardinalDirection.getShortName().toCharArray();
        if (compassEnd == CoordinatesOverlay.CompassEnd.HEAD) {
            if (compassStyle == CoordinatesOverlay.CompassStyle.STATIC) {
                component.append(fromGlyphKey("compass_head_n"));
            } else if (compassStyle == CoordinatesOverlay.CompassStyle.NONE) {
                component.append(fromGlyphKey("head"));
            } else if (compassStyle == CoordinatesOverlay.CompassStyle.ANIMATED) {
                component.append(fromGlyphKey(
                        "compass_head_" + lastCardinalDirection.getShortName().toLowerCase(Locale.ROOT)));
            }
            component.append(NEGATIVE_SPACE_CHARACTER);
        } else {
            component.append(fromGlyphKey("head"));
            component.append(NEGATIVE_SPACE_CHARACTER);
            if (compassDirectionYPos == CoordinatesOverlay.CompassDirectionYPos.BOTH) {
                for (char c : lastCardinalDirectionChars) {
                    component.append(fromGlyphKey(String.valueOf(c)));
                    component.append(NEGATIVE_SPACE_CHARACTER);
                }
            }
        }

        component.append(fromGlyphKey("large_space"));
        component.append(NEGATIVE_SPACE_CHARACTER);

        for (char c : String.valueOf(lastBlockPos.getX()).toCharArray()) {
            component.append(fromGlyphKey(String.valueOf(c)));
            component.append(NEGATIVE_SPACE_CHARACTER);
        }

        component.append(fromGlyphKey("large_space"));
        component.append(NEGATIVE_SPACE_CHARACTER);

        if (compassDirectionYPos == CoordinatesOverlay.CompassDirectionYPos.DIRECTION) {
            for (char c : lastCardinalDirectionChars) {
                component.append(fromGlyphKey(String.valueOf(c)));
                component.append(NEGATIVE_SPACE_CHARACTER);
            }
        } else {
            for (char c : String.valueOf(lastBlockPos.getY()).toCharArray()) {
                component.append(fromGlyphKey(String.valueOf(c)));
                component.append(NEGATIVE_SPACE_CHARACTER);
            }
        }

        component.append(fromGlyphKey("large_space"));
        component.append(NEGATIVE_SPACE_CHARACTER);

        for (char c : String.valueOf(lastBlockPos.getZ()).toCharArray()) {
            component.append(fromGlyphKey(String.valueOf(c)));
            component.append(NEGATIVE_SPACE_CHARACTER);
        }

        component.append(fromGlyphKey("large_space"));
        component.append(NEGATIVE_SPACE_CHARACTER);

        if (compassEnd == CoordinatesOverlay.CompassEnd.TAIL) {
            if (compassStyle == CoordinatesOverlay.CompassStyle.STATIC) {
                component.append(fromGlyphKey("compass_tail_n"));
            } else if (compassStyle == CoordinatesOverlay.CompassStyle.NONE) {
                component.append(fromGlyphKey("tail"));
            } else if (compassStyle == CoordinatesOverlay.CompassStyle.ANIMATED) {
                component.append(fromGlyphKey(
                        "compass_tail_" + lastCardinalDirection.getShortName().toLowerCase(Locale.ROOT)));
            }
        } else {
            if (compassDirectionYPos == CoordinatesOverlay.CompassDirectionYPos.BOTH) {
                for (char c : lastCardinalDirectionChars) {
                    component.append(fromGlyphKey(String.valueOf(c)));
                    component.append(NEGATIVE_SPACE_CHARACTER);
                }
            }
            component.append(fromGlyphKey("tail"));
        }

        return component;
    }

    private static Component fromGlyphKey(String glyphKey) {
        return Component.literal(WynnFont.toGlyph(glyphKey, WynntilsCoordinatesFont.class));
    }
}
