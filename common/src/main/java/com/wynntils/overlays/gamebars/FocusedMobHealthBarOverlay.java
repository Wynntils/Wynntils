/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.combat.bossbar.DamageBar;
import com.wynntils.models.combat.type.FocusedDamageEvent;
import com.wynntils.models.combat.type.MobElementals;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.SubscribeEvent;

public class FocusedMobHealthBarOverlay extends BaseBarOverlay {
    @Persisted
    private final Config<Boolean> abbreviateHealth = new Config<>(true);

    private StyledText barText = StyledText.EMPTY;

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
    protected BossBarProgress progress() {
        CappedValue health = Models.Combat.getFocusedMobHealthPercent();
        return new BossBarProgress(health, (float) health.getProgress());
    }

    @SubscribeEvent
    public void onFocusChange(FocusedDamageEvent.MobFocused event) {
        this.currentProgress = progress().progress();
    }

    @Override
    public boolean isVisible() {
        return Models.Combat.getFocusedMobHealth() > 0
                && System.currentTimeMillis() - Models.Combat.getLastDamageDealtTimestamp() < 5000;
    }

    @Override
    protected void renderText(PoseStack poseStack, MultiBufferSource bufferSource, float renderY, String text) {
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        bufferSource,
                        barText,
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        renderY,
                        0,
                        this.textColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }

    @Override
    public void tick() {
        super.tick();

        String healthString = abbreviateHealth.get()
                ? StringUtils.integerToShortString(Models.Combat.getFocusedMobHealth())
                : Long.toString(Models.Combat.getFocusedMobHealth());

        MutableComponent text = Component.empty()
                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                .append(Component.literal(Models.Combat.getFocusedMobName()))
                .append(Component.literal(" ❤ ").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)))
                .append(Component.literal(healthString));

        if (!Models.Combat.getFocusedMobElementals().equals(MobElementals.EMPTY)) {
            text.append(Component.literal(" [").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
                    .append(Models.Combat.getFocusedMobElementals().getFormatted())
                    .append(Component.literal("]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }

        barText = StyledText.fromComponent(text);
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return DamageBar.class;
    }
}
