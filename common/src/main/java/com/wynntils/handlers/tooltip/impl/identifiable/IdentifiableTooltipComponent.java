/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Represents a tooltip component generator that can be used in {@link IdentifiableTooltipBuilder}
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public abstract class IdentifiableTooltipComponent<T, U> {
    public static final Style SPACING_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("space")))
            .withoutShadow();

    public static final Style RESTRICTION_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/restriction")))
            .withoutShadow();

    public static final Style SKILL_FRAME_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/frame")))
            .withoutShadow();

    public static final Style SKILL_SPRITE_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/sprite")))
            .withoutShadow();

    public static final FontDescription WYNNCRAFT_LANGUAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("language/wynncraft"));

    public static final Component DIVIDER = Component.literal("\uE000")
            .withStyle(Style.EMPTY.withFont(
                    new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"))));

    public static final Style REQUIREMENT_STYLE = Style.EMPTY.withFont(
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/requirement/sprite")));

    private static final char SKILL_REQ_FRAME_BASE = '\uE000';
    public static final String SKILL_REQ_FRAME_NONE = "\uE007";

    private static final char SKILL_REQ_ICON_BASE_ACTIVE = '\uE000';
    private static final char SKILL_REQ_ICON_BASE_UNUSED = '\uE010';

    public abstract List<Component> buildHeaderTooltip(T itemInfo, U itemInstance, boolean hideUnidentified);

    public abstract List<Component> buildFooterTooltip(T itemInfo, U itemInstance, boolean showItemType);

    protected MutableComponent buildRequirementLine(String requirementName, boolean fulfilled) {
        MutableComponent requirement;

        requirement = fulfilled
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }

    protected MutableComponent buildRequirementLine(Component requirementName, boolean fulfilled) {
        MutableComponent requirement = Component.empty();

        requirement.append(
                fulfilled
                        ? Component.literal("\uE006\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE.withoutShadow())
                        : Component.literal("\uE007\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE.withoutShadow()));
        requirement.append(requirementName);
        return requirement;
    }

    protected String getSkillReqFrame(GearTier gearTier) {
        GearTier[] valid = GearTier.validValues();
        for (int i = 0; i < valid.length; i++) {
            if (valid[i] == gearTier) {
                return String.valueOf((char) (SKILL_REQ_FRAME_BASE + i));
            }
        }

        return SKILL_REQ_FRAME_NONE;
    }

    protected String getSkillReqIcon(int ordinal, boolean active) {
        return String.valueOf((char) ((active ? SKILL_REQ_ICON_BASE_ACTIVE : SKILL_REQ_ICON_BASE_UNUSED) + ordinal));
    }

    protected static CustomColor getDividerColor(GearTier gearTier) {
        return switch (gearTier) {
            case NORMAL -> CustomColor.fromInt(0xe0e0e0);
            case UNIQUE -> CustomColor.fromInt(0xfff2b3);
            case RARE -> CustomColor.fromInt(0xf2c2f2);
            case LEGENDARY -> CustomColor.fromInt(0xc2f2f2);
            case FABLED -> CustomColor.fromInt(0xf2c2c2);
            case MYTHIC -> CustomColor.fromInt(0xe0b3e6);
            // Crafteds shouldn't be used here
            default -> CustomColor.NONE;
        };
    }
}
