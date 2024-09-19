/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.features.combat.AbbreviateMobHealthFeature;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.damage.DamageModel;
import com.wynntils.models.damage.type.FocusedDamageEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.ChatFormatting;
import net.neoforged.bus.api.SubscribeEvent;

public class FocusedMobHealthBarOverlay extends BaseBarOverlay {
    private static final String FMT_STR_WITHOUT_ELEMS =
            ChatFormatting.WHITE + "%s" + ChatFormatting.RED + " ❤ " + ChatFormatting.WHITE + "%s";
    private static final String FMT_STR_WITH_ELEMS =
            FMT_STR_WITHOUT_ELEMS + " " + ChatFormatting.GRAY + "[%s" + ChatFormatting.GRAY + "]";

    public FocusedMobHealthBarOverlay() {
        super(
                new OverlayPosition(
                        36,
                        0,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.TOP_MIDDLE),
                new OverlaySize(162, 32),
                CommonColors.RED);
        this.userEnabled.store(false);
    }

    @Override
    public BossBarProgress progress() {
        CappedValue health = Models.Damage.getFocusedMobHealthPercent();
        return new BossBarProgress(health, (float) health.getProgress());
    }

    @SubscribeEvent
    public void onFocusChange(FocusedDamageEvent.MobFocused event) {
        this.currentProgress = progress().progress();
    }

    @Override
    protected String text() {
        String healthString = Managers.Feature.getFeatureInstance(AbbreviateMobHealthFeature.class)
                        .isEnabled()
                ? StringUtils.integerToShortString(Models.Damage.getFocusedMobHealth())
                : Long.toString(Models.Damage.getFocusedMobHealth());
        String elementals = Models.Damage.getFocusedMobElementals();
        if (elementals.isBlank()) {
            return String.format(FMT_STR_WITHOUT_ELEMS, Models.Damage.getFocusedMobName(), healthString);
        } else {
            return String.format(FMT_STR_WITH_ELEMS, Models.Damage.getFocusedMobName(), healthString, elementals);
        }
    }

    @Override
    public boolean isActive() {
        return Models.Damage.getFocusedMobHealth() > 0
                && System.currentTimeMillis() - Models.Damage.getLastDamageDealtTimestamp() < 5000;
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return DamageModel.DamageBar.class;
    }
}
