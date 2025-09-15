/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.bonustotems.type.BonusTotemType;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MobTotemTimerOverlay extends TextOverlay {
    private static final String TEMPLATE =
            "{MOB_TOTEM_OWNER(%d)}'s Mob Totem [{MOB_TOTEM_DISTANCE(%d):0} m] ({MOB_TOTEM_TIME_LEFT(%d)})";
    private static final Pattern DIGIT_PATTERN = Pattern.compile("%d");

    public MobTotemTimerOverlay() {
        super(
                new OverlayPosition(
                        330,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(120, 35));
    }

    @Override
    public String getTemplate() {
        return IntStream.rangeClosed(
                        1,
                        Models.BonusTotem.getBonusTotemsByType(BonusTotemType.MOB)
                                .size())
                .mapToObj(i -> DIGIT_PATTERN.matcher(TEMPLATE).replaceAll(String.valueOf(i)))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getPreviewTemplate() {
        return "Player's Mob Totem [16 m] (4:11)";
    }

    @Override
    protected StyledText[] calculateTemplateValue(String template) {
        return Arrays.stream(super.calculateTemplateValue(template))
                .map(s -> RenderedStringUtils.trySplitOptimally(s, this.getWidth()))
                .map(s -> s.split("\n"))
                .flatMap(Arrays::stream)
                .toArray(StyledText[]::new);
    }
}
