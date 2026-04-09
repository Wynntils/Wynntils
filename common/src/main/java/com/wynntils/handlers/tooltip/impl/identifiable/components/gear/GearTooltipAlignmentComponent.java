/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.core.components.Managers;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.TooltipMarkers;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class GearTooltipAlignmentComponent {
    private static final FontDescription SPACE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("space"));
    private static final FontDescription PAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/page"));
    private static final FontDescription CHAT_TILE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("chat/tile"));

    private GearTooltipAlignmentComponent() {}

    public static void realignMarkedTooltipLines(List<Component> tooltips) {
        if (tooltips.isEmpty()) {
            return;
        }

        int targetWidth = tooltips.stream()
                .mapToInt(GearTooltipAlignmentComponent::getEffectiveLineWidth)
                .max()
                .orElse(0);

        for (int i = 0; i < tooltips.size(); i++) {
            Component line = tooltips.get(i);
            TooltipMarkers marker = TooltipMarkers.fromToken(line.getStyle().getInsertion());
            if (marker == null) {
                continue;
            }

            switch (marker) {
                case ALIGN_CENTER, SECTION_DIVIDER, IDENTIFICATION_DIVIDER -> {
                    Component unaligned = stripLeadingOffset(clearMarker(line));
                    MutableComponent aligned = containsFont(unaligned, PAGE_FONT)
                            ? rebuildCenteredPaginationLine(unaligned, targetWidth)
                            : alignCenterLine(unaligned, targetWidth);
                    tooltips.set(i, markLine(aligned, marker));
                }
                case ALIGN_RIGHT -> {
                    Component unaligned = stripRightAlignmentOffset(clearMarker(line));
                    tooltips.set(i, markLine(alignRightSegment(unaligned, targetWidth), marker));
                }
                case REROLL_BANNER -> {
                    Component unaligned = stripLeadingOffset(clearMarker(line));
                    tooltips.set(i, markLine(alignRightWhole(unaligned, targetWidth), marker));
                }
            }
        }
    }

    private static int getEffectiveLineWidth(Component line) {
        TooltipMarkers marker = TooltipMarkers.fromToken(line.getStyle().getInsertion());
        if (marker == null) {
            return McUtils.mc().font.width(line);
        }

        Component unaligned =
                switch (marker) {
                    case ALIGN_CENTER, SECTION_DIVIDER, IDENTIFICATION_DIVIDER, REROLL_BANNER ->
                        stripLeadingOffset(clearMarker(line));
                    case ALIGN_RIGHT -> stripRightAlignmentOffset(clearMarker(line));
                };

        return McUtils.mc().font.width(unaligned);
    }

    private static Component stripLeadingOffset(Component line) {
        List<Component> siblings = line.getSiblings();
        if (siblings.isEmpty()) {
            return line.copy();
        }

        int firstContentIndex = 0;
        while (firstContentIndex < siblings.size() && isLeadingOffsetSegment(siblings.get(firstContentIndex))) {
            firstContentIndex++;
        }

        MutableComponent stripped = Component.empty();
        for (int i = firstContentIndex; i < siblings.size(); i++) {
            stripped.append(siblings.get(i).copy());
        }

        return stripped;
    }

    private static Component stripRightAlignmentOffset(Component line) {
        Component noLeadingOffset = stripLeadingOffset(line);
        List<Component> siblings = noLeadingOffset.getSiblings();
        if (siblings.size() < 3) {
            return noLeadingOffset;
        }

        int offsetIndex = siblings.size() - 2;
        if (!isLeadingOffsetSegment(siblings.get(offsetIndex))) {
            return noLeadingOffset;
        }

        MutableComponent stripped = Component.empty();
        for (int i = 0; i < siblings.size(); i++) {
            if (i == offsetIndex) {
                continue;
            }
            stripped.append(siblings.get(i).copy());
        }

        return stripped;
    }

    private static boolean isLeadingOffsetSegment(Component component) {
        return SPACE_FONT.equals(component.getStyle().getFont())
                && component.getSiblings().isEmpty();
    }

    private static boolean containsFont(Component component, FontDescription targetFont) {
        if (targetFont.equals(component.getStyle().getFont())) {
            return true;
        }

        for (Component sibling : component.getSiblings()) {
            if (containsFont(sibling, targetFont)) {
                return true;
            }
        }

        return false;
    }

    private static MutableComponent clearMarker(Component line) {
        MutableComponent cleared = line.copy();
        cleared.setStyle(cleared.getStyle().withInsertion(null));
        return cleared;
    }

    private static MutableComponent markLine(MutableComponent line, TooltipMarkers marker) {
        line.setStyle(line.getStyle().withInsertion(marker.token()));
        return line;
    }

    private static MutableComponent alignCenterLine(Component line, int targetWidth) {
        int lineWidth = McUtils.mc().font.width(line);
        int leftPadding = Math.max(0, (targetWidth - lineWidth) / 2);

        MutableComponent aligned = Component.empty();
        appendOffset(aligned, leftPadding);
        aligned.append(line.copy());
        return aligned;
    }

    private static MutableComponent rebuildCenteredPaginationLine(Component line, int targetWidth) {
        List<Component> leaves = flattenLeafComponents(line);
        int firstPageIndex = -1;
        for (int i = 0; i < leaves.size(); i++) {
            if (PAGE_FONT.equals(leaves.get(i).getStyle().getFont())) {
                firstPageIndex = i;
                break;
            }
        }

        if (firstPageIndex < 0) {
            return alignCenterLine(line, targetWidth);
        }

        MutableComponent pageDots = Component.empty();
        for (int i = firstPageIndex; i < leaves.size(); i++) {
            Component leaf = leaves.get(i);
            if (PAGE_FONT.equals(leaf.getStyle().getFont())) {
                pageDots.append(leaf.copy());
                continue;
            }

            if (!leaf.getString().isEmpty()) {
                pageDots.append(
                        Component.literal(leaf.getString()).withStyle(IdentifiableTooltipComponent.SPACING_STYLE));
            }
        }

        Component keybind = null;
        for (int i = firstPageIndex - 1; i >= 0; i--) {
            if (CHAT_TILE_FONT.equals(leaves.get(i).getStyle().getFont())) {
                keybind = leaves.get(i).copy();
                break;
            }
        }

        MutableComponent aligned = Component.empty();
        int pageWidth = McUtils.mc().font.width(pageDots);
        appendOffset(aligned, Math.max(0, (targetWidth - pageWidth) / 2));
        aligned.append(pageDots);

        if (keybind != null) {
            appendOffset(aligned, McUtils.mc().font.width(" "));
            aligned.append(keybind.copy());
        }

        return aligned;
    }

    private static MutableComponent alignRightSegment(Component line, int targetWidth) {
        List<Component> siblings = line.getSiblings();
        if (siblings.size() < 2) {
            return alignRightWhole(line, targetWidth);
        }

        MutableComponent left = Component.empty();
        for (int i = 0; i < siblings.size() - 1; i++) {
            left.append(siblings.get(i).copy());
        }

        Component right = siblings.get(siblings.size() - 1);
        MutableComponent aligned = Component.empty().append(left);
        int currentWidth = McUtils.mc().font.width(left) + McUtils.mc().font.width(right);
        appendOffset(aligned, Math.max(0, targetWidth - currentWidth));
        aligned.append(right.copy());
        return aligned;
    }

    private static MutableComponent alignRightWhole(Component line, int targetWidth) {
        int lineWidth = McUtils.mc().font.width(line);
        int leftPadding = Math.max(0, targetWidth - lineWidth);

        MutableComponent aligned = Component.empty();
        appendOffset(aligned, leftPadding);
        aligned.append(line.copy());
        return aligned;
    }

    private static void appendOffset(MutableComponent line, int pixels) {
        if (pixels <= 0) {
            return;
        }

        String offset = Managers.Font.calculateOffset(0, pixels);
        if (!offset.isEmpty()) {
            line.append(Component.literal(offset).withStyle(IdentifiableTooltipComponent.SPACING_STYLE));
        }
    }

    private static List<Component> flattenLeafComponents(Component component) {
        java.util.ArrayList<Component> leaves = new java.util.ArrayList<>();
        collectLeafComponents(component, leaves);
        return leaves;
    }

    private static void collectLeafComponents(Component component, List<Component> leaves) {
        MutableComponent leaf = component.copy();
        leaf.getSiblings().clear();
        if (!leaf.getString().isEmpty()) {
            leaves.add(leaf);
        }

        for (Component sibling : component.getSiblings()) {
            collectLeafComponents(sibling, leaves);
        }
    }
}
