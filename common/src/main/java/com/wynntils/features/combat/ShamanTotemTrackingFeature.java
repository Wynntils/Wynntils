/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.labels.event.TextDisplayChangedEvent;
import com.wynntils.models.abilities.label.ShamanTotemLabelInfo;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class ShamanTotemTrackingFeature extends Feature {
    @Persisted
    private final Config<Boolean> highlightShamanTotems = new Config<>(true);

    @Persisted
    private final Config<CustomColor> firstTotemColor = new Config<>(CommonColors.WHITE);

    @Persisted
    private final Config<CustomColor> secondTotemColor = new Config<>(CommonColors.BLUE);

    @Persisted
    private final Config<CustomColor> thirdTotemColor = new Config<>(CommonColors.RED);

    @Persisted
    private final Config<CustomColor> fourthTotemColor = new Config<>(CommonColors.GREEN);

    public ShamanTotemTrackingFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onLabelChanged(TextDisplayChangedEvent.Text event) {
        if (!highlightShamanTotems.get()) return;
        if (event.getLabelInfo().isEmpty()) return;
        if (!(event.getLabelInfo().get() instanceof ShamanTotemLabelInfo)) return;

        ShamanTotem totem = Models.ShamanTotem.getTotemByTimerEntityId(
                event.getTextDisplay().getId());
        if (totem == null) return;

        int totemNumber = totem.getTotemNumber();
        CustomColor color =
                switch (totemNumber) {
                    case 1 -> firstTotemColor.get();
                    case 2 -> secondTotemColor.get();
                    case 3 -> thirdTotemColor.get();
                    case 4 -> fourthTotemColor.get();
                    default ->
                        throw new IllegalArgumentException(
                                "totemNumber should be 1, 2, 3 or 4! (color switch in #onTotemSummoned in ShamanTotemTrackingFeature");
                };

        float[] hsb = color.asHSB();
        float brightnessShift = hsb[2] < 0.5f ? 0.9f : -0.9f;

        Component totemBanner = BannerBoxFont.buildMessage(
                "Totem " + totemNumber, color.saturationShift(-0.2f), color.brightnessShift(brightnessShift), "");

        event.setText(StyledText.fromComponent(totemBanner).append("\n").append(event.getText()));
    }
}
