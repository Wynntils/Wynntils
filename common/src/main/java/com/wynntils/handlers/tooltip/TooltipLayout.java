/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.components.Managers;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TooltipLayout {
    private static final Component DEFAULT_SPACING = Component.literal(" ");

    public static List<Component> align(List<TooltipLine> lines) {
        return align(lines, 0);
    }

    public static List<Component> align(List<TooltipLine> lines, int minimumWidth) {
        int widestLine = Math.max(
                minimumWidth,
                lines.stream()
                        .map(TooltipLine::unaligned)
                        .mapToInt(line -> McUtils.mc().font.width(line))
                        .max()
                        .orElse(0));

        while (true) {
            LayoutResult result = alignOnce(lines, widestLine);
            if (result.requiredWidth() <= widestLine) return List.copyOf(result.lines());

            widestLine = result.requiredWidth();
        }
    }

    private static LayoutResult alignOnce(List<TooltipLine> lines, int widestLine) {
        List<Component> aligned = new ArrayList<>(lines.size());
        int requiredWidth = widestLine;

        for (TooltipLine line : lines) {
            if (line instanceof TooltipLine.Fixed fixed) {
                aligned.add(fixed.component());
            } else if (line instanceof TooltipLine.Centered centered) {
                aligned.add(center(centered.component(), widestLine));
            } else if (line instanceof TooltipLine.Aligned pair) {
                Component unaligned = pair.unaligned();
                int currentWidth = McUtils.mc().font.width(unaligned);
                String spacing = Managers.Font.calculateOffset(currentWidth, widestLine);
                if (spacing.isEmpty()) {
                    MutableComponent withDefaultSpacing = Component.empty()
                            .append(pair.left().copy())
                            .append(DEFAULT_SPACING.copy())
                            .append(pair.right().copy());
                    requiredWidth = Math.max(requiredWidth, McUtils.mc().font.width(withDefaultSpacing));
                    aligned.add(withDefaultSpacing);
                    continue;
                }

                aligned.add(Component.empty()
                        .append(pair.left().copy())
                        .append(Component.literal(spacing).withStyle(CommonStyles.SPACE))
                        .append(pair.right().copy()));
            }
        }

        return new LayoutResult(aligned, requiredWidth);
    }

    private static Component center(Component line, int widestLine) {
        int currentWidth = McUtils.mc().font.width(line);
        int target = currentWidth + ((widestLine - currentWidth) / 2);
        String spacing = Managers.Font.calculateOffset(currentWidth, target);
        return Component.empty()
                .append(Component.literal(spacing).withStyle(CommonStyles.SPACE))
                .append(line.copy());
    }

    private record LayoutResult(List<Component> lines, int requiredWidth) {}
}
