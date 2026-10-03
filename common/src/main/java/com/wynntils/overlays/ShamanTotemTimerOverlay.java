/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.network.chat.TextColor;

public class ShamanTotemTimerOverlay extends TextOverlay {
    @Persisted
    private final Config<TotemTrackingDetail> totemTrackingDetail = new Config<>(TotemTrackingDetail.COORDS);

    @Persisted
    private final Config<CustomColor> firstTotemTextColor = new Config<>(CustomColor.fromTextColor(TextColor.WHITE));

    @Persisted
    private final Config<CustomColor> secondTotemTextColor = new Config<>(CustomColor.fromTextColor(TextColor.BLUE));

    @Persisted
    private final Config<CustomColor> thirdTotemTextColor = new Config<>(CustomColor.fromTextColor(TextColor.RED));

    @Persisted
    private final Config<CustomColor> fourthTotemTextColor = new Config<>(CustomColor.fromTextColor(TextColor.GREEN));

    private final CustomColor[] totemColorsArray = {
        firstTotemTextColor.get(), secondTotemTextColor.get(), thirdTotemTextColor.get(), fourthTotemTextColor.get()
    };

    public ShamanTotemTimerOverlay() {
        super(
                new OverlayPosition(
                        275,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(120, 35));
    }

    @Override
    public String getTemplate() {
        return Models.ShamanTotem.getActiveTotems().stream()
                .filter(Objects::nonNull)
                .map(totem -> "§" + totemColorsArray[totem.getTotemNumber() - 1].toHexString()
                        + totemTrackingDetail
                                .get()
                                .getTemplate()
                                .replaceAll("%d", String.valueOf(totem.getTotemNumber())))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getPreviewTemplate() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < TotemTrackingDetail.values().length; i++) {
            builder.append("§")
                    .append(totemColorsArray[i].toHexString())
                    .append(TotemTrackingDetail.values()[i].getPreviewTemplate())
                    .append("\n");
        }

        return builder.toString();
    }

    @Override
    protected StyledText[] calculateTemplateValue(String template) {
        return Arrays.stream(super.calculateTemplateValue(template))
                .map(s -> RenderedStringUtils.trySplitOptimally(s, this.getWidth()))
                .map(s -> s.split("\n"))
                .flatMap(Arrays::stream)
                .toArray(StyledText[]::new);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        totemColorsArray[0] = firstTotemTextColor.get();
        totemColorsArray[1] = secondTotemTextColor.get();
        totemColorsArray[2] = thirdTotemTextColor.get();
        totemColorsArray[3] = fourthTotemTextColor.get();
    }

    private enum TotemTrackingDetail {
        NONE(
                "{IF_STR(EQ_STR(SHAMAN_TOTEM_STATE(%d); \"ACTIVE\"); CONCAT(\"Totem %d (\"; STRING(SHAMAN_TOTEM_TIME_LEFT(%d));\" s)\"); IF_STR(EQ_STR(SHAMAN_TOTEM_STATE(%d); \"SUMMONED\"); \"Totem %d summoned\"; \"\"))}",
                "Totem 1 (10 s)"),
        COORDS(
                "{IF_STR(EQ_STR(SHAMAN_TOTEM_STATE(%d); \"ACTIVE\"); CONCAT(\"Totem %d (\"; STRING(SHAMAN_TOTEM_TIME_LEFT(%d));\" s)\"; \" \"; SHAMAN_TOTEM_LOCATION(%d)); IF_STR(EQ_STR(SHAMAN_TOTEM_STATE(%d); \"SUMMONED\"); \"Totem %d summoned\"; \"\"))}",
                "Totem 2 (15 s) [1425, 12, 512]"),
        DISTANCE(
                "{IF_STR(EQ_STR(SHAMAN_TOTEM_STATE(%d); \"ACTIVE\"); CONCAT(\"Totem %d (\"; STRING(SHAMAN_TOTEM_TIME_LEFT(%d));\" s, \"; STRING(INT(SHAMAN_TOTEM_DISTANCE(%d))); \" m)\"); IF_STR(EQ_STR(SHAMAN_TOTEM_STATE(%d); \"SUMMONED\"); \"Totem %d summoned\"; \"\"))}",
                "Totem 3 (7s, 10 m)");

        private final String template;
        private final String previewTemplate;

        TotemTrackingDetail(String template, String previewTemplate) {
            this.template = template;
            this.previewTemplate = previewTemplate;
        }

        private String getTemplate() {
            return template;
        }

        private String getPreviewTemplate() {
            return previewTemplate;
        }
    }
}
