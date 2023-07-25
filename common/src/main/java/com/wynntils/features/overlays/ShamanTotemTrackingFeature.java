/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.TextOverlay;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class ShamanTotemTrackingFeature extends Feature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final ShamanTotemTimerOverlay shamanTotemTimerOverlay = new ShamanTotemTimerOverlay();

    @RegisterConfig
    public final Config<Boolean> highlightShamanTotems = new Config<>(true);

    @RegisterConfig
    public final Config<CustomColor> firstTotemColor = new Config<>(CommonColors.WHITE);

    @RegisterConfig
    public final Config<CustomColor> secondTotemColor = new Config<>(CommonColors.BLUE);

    @RegisterConfig
    public final Config<CustomColor> thirdTotemColor = new Config<>(CommonColors.RED);

    private static final int ENTITY_GLOWING_FLAG = 6;

    @SubscribeEvent
    public void onTotemSummoned(TotemEvent.Summoned e) {
        if (!highlightShamanTotems.get()) return;

        int totemNumber = e.getTotemNumber();
        ArmorStand totemAS = e.getTotemEntity();

        CustomColor color =
                switch (totemNumber) {
                    case 1 -> firstTotemColor.get();
                    case 2 -> secondTotemColor.get();
                    case 3 -> thirdTotemColor.get();
                    default -> throw new IllegalArgumentException(
                            "totemNumber should be 1, 2, or 3! (color switch in #onTotemSummoned in ShamanTotemTrackingFeature");
                };

        ((EntityExtension) totemAS).setGlowColor(color);

        totemAS.setGlowingTag(true);
        totemAS.setSharedFlag(ENTITY_GLOWING_FLAG, true);
    }

    public static class ShamanTotemTimerOverlay extends TextOverlay {
        @RegisterConfig
        public final Config<TotemTrackingDetail> totemTrackingDetail = new Config<>(TotemTrackingDetail.COORDS);

        @RegisterConfig
        public final Config<ColorChatFormatting> firstTotemTextColor = new Config<>(ColorChatFormatting.WHITE);

        @RegisterConfig
        public final Config<ColorChatFormatting> secondTotemTextColor = new Config<>(ColorChatFormatting.BLUE);

        @RegisterConfig
        public final Config<ColorChatFormatting> thirdTotemTextColor = new Config<>(ColorChatFormatting.RED);

        private final ChatFormatting[] totemColorsArray = {
            firstTotemTextColor.get().getChatFormatting(),
            secondTotemTextColor.get().getChatFormatting(),
            thirdTotemTextColor.get().getChatFormatting()
        };

        protected ShamanTotemTimerOverlay() {
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
                    .map(totem -> totemColorsArray[totem.getTotemNumber() - 1]
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
                builder.append(totemColorsArray[i])
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
    }

    public enum TotemTrackingDetail {
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
