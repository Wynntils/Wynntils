/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.components.Managers;
import com.wynntils.features.tooltips.ItemStatInfoFeature;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.wynn.ColorScaleUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a tooltip component generator that can be used in {@link IdentifiableTooltipBuilder}
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public abstract class IdentifiableTooltipComponent<T, U> {
    public record TooltipParts(List<Component> header, List<Component> footer) {}

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
    protected static final FontDescription TOOLTIP_DIVIDER_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"));
    protected static final FontDescription TOOLTIP_PAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/page"));
    protected static final Style WYNNCRAFT_WHITE_STYLE =
            Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.WHITE);

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

    public TooltipParts buildTooltipParts(
            ItemStack itemStack,
            IdentifiableItemProperty<T, U> itemProperty,
            boolean hideUnidentified,
            boolean showItemType) {
        return null;
    }

    public List<Component> buildWeightedHeaderTooltip(
            List<Component> originalHeader,
            IdentifiableItemProperty<T, U> itemProperty,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator,
            TooltipStyle style) {
        return originalHeader;
    }

    public List<Component> finalizeTooltipLines(List<Component> tooltip, int targetWidth, T itemInfo, U itemInstance) {
        return tooltip;
    }

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

        requirement.append(withWhiteShadow(
                fulfilled
                        ? Component.literal("\uE006\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE)
                        : Component.literal("\uE007\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE)));
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
            default -> CustomColor.NONE;
        };
    }

    protected static MutableComponent withWhiteShadow(Component component) {
        return Component.empty()
                .withStyle(style -> style.withShadowColor(0xFFFFFF))
                .append(component.copy());
    }

    protected MutableComponent appendOverallPercentageInName(
            MutableComponent line, boolean hasOverallValue, float overallPercentage) {
        return appendOverallPercentageInName(line, hasOverallValue, overallPercentage, false, false);
    }

    protected MutableComponent appendOverallPercentageInName(
            MutableComponent line,
            boolean hasOverallValue,
            float overallPercentage,
            boolean perfect,
            boolean defective) {
        if (!hasOverallValue || line.getString().contains("%]")) {
            return line;
        }

        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
        if (!shouldAppendOverallPercentageInName(feature, perfect, defective)) {
            return line;
        }

        line.append(ColorScaleUtils.getPercentageTextComponent(
                        feature.getColorMap(), overallPercentage, feature.colorLerp.get(), feature.decimalPlaces.get())
                .withStyle(style -> style.withFont(WYNNCRAFT_LANGUAGE_FONT)));
        return line;
    }

    protected MutableComponent buildRewardItemNameComponent(
            String itemName,
            ChatFormatting tierFormatting,
            boolean perfectItem,
            boolean defectiveItem,
            boolean hasOverallValue,
            float overallPercentage) {
        ItemStatInfoFeature feature = Managers.Feature.getFeatureInstance(ItemStatInfoFeature.class);
        boolean perfectTitle = feature.perfect.get() && perfectItem;
        boolean defectiveTitle = feature.defective.get() && defectiveItem;

        MutableComponent nameComponent;
        if (perfectTitle) {
            nameComponent = ComponentUtils.makeRainbowStyle("Perfect " + itemName, true);
        } else if (defectiveTitle) {
            nameComponent = ComponentUtils.makeCrimsonStyle("Defective " + itemName, true);
        } else {
            nameComponent = Component.literal(itemName)
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(tierFormatting));
        }

        if (!hasOverallValue || !shouldAppendOverallPercentageInName(feature, perfectTitle, defectiveTitle)) {
            return nameComponent;
        }

        nameComponent.append(ColorScaleUtils.getPercentageTextComponent(
                        feature.getColorMap(), overallPercentage, feature.colorLerp.get(), feature.decimalPlaces.get())
                .withStyle(style -> style.withFont(WYNNCRAFT_LANGUAGE_FONT)));
        return nameComponent;
    }

    protected MutableComponent buildRequirementValueLine(Component label, Component value, boolean fulfilled) {
        MutableComponent requirement = Component.empty();
        requirement.append(withWhiteShadow(
                fulfilled
                        ? Component.literal("\uE006\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE)
                        : Component.literal("\uE007\uDAFF\uDFFF").withStyle(REQUIREMENT_STYLE)));
        requirement.append(label.copy());

        MutableComponent paddedValue = Component.literal("  ").withStyle(value.getStyle());
        paddedValue.append(value.copy());
        requirement.append(paddedValue);
        return requirement;
    }

    protected static boolean shouldAppendOverallPercentageInName(
            ItemStatInfoFeature feature, boolean perfect, boolean defective) {
        if ((feature.perfect.get() && perfect) || (feature.defective.get() && defective)) {
            return feature.overallPercentageInPerfectDefectiveName.get();
        }

        return feature.overallPercentageInName.get();
    }
}
