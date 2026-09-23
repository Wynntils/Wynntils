/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.ContainerOverlay;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.notifications.type.RedirectAction;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilities.event.AbilityCooldownRefreshedEvent;
import com.wynntils.models.abilities.event.AbilityCooldownsUpdatedEvent;
import com.wynntils.models.abilities.type.AbilityCooldown;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class AbilityCooldownsOverlay extends ContainerOverlay<AbilityCooldownsOverlay.AbilityCooldownOverlay> {
    @Persisted
    private final Config<Boolean> removeStatusEffect = new Config<>(true);

    @Persisted
    private final Config<RedirectAction> redirectRefreshedMessages = new Config<>(RedirectAction.HIDE);

    @Persisted
    private final Config<Boolean> interpolateTime = new Config<>(true);

    @Persisted
    private final Config<Boolean> showTimer = new Config<>(true);

    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    public AbilityCooldownsOverlay() {
        super(
                new OverlayPosition(
                        0,
                        0,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(194, 56),
                ContainerOverlay.GrowDirection.CENTER_HORIZONTAL,
                HorizontalAlignment.CENTER,
                VerticalAlignment.TOP);
    }

    @Override
    protected List<AbilityCooldownOverlay> getPreviewChildren() {
        return List.of(
                new AbilityCooldownOverlay(AbilityCooldown.MIRROR_IMAGE),
                new AbilityCooldownOverlay(AbilityCooldown.VANISH));
    }

    @SubscribeEvent
    public void onAbilityCooldownsUpdate(AbilityCooldownsUpdatedEvent event) {
        this.clearChildren();
        for (AbilityCooldown cooldown : event.getCooldowns()) {
            this.addChild(new AbilityCooldownOverlay(cooldown));
        }
    }

    @SubscribeEvent
    public void onStatusEffectUpdate(StatusEffectsChangedEvent event) {
        if (!removeStatusEffect.get()) return;

        for (StatusEffect statusEffect : event.getOriginalStatusEffects()) {
            if (statusEffect.getPrefix().getString().equals(Models.Ability.COOLDOWN_PREFIX)) {
                // Make sure the overlay will be displaying a cooldown before we remove it from the list
                if (AbilityCooldown.fromStatusEffect(statusEffect) != null) {
                    event.removeStatusEffect(statusEffect);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAbilityCooldownRefreshed(AbilityCooldownRefreshedEvent event) {
        if (redirectRefreshedMessages.get() == RedirectAction.KEEP) return;

        event.setCancelMessage(true);

        if (redirectRefreshedMessages.get() == RedirectAction.REDIRECT) {
            Managers.Notification.queueMessage(event.getMessage());
        }
    }

    public final class AbilityCooldownOverlay extends Overlay {
        private static final int COOLDOWN_TEXTURE_STAGES = 77;
        private static final int TEXTURES_PER_ROW = 11;

        private final AbilityCooldown cooldown;

        private AbilityCooldownOverlay(AbilityCooldown cooldown) {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.TOP,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.MIDDLE),
                    new OverlaySize(22, 22));
            this.cooldown = cooldown;
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Window window) {
            float renderX = Math.round(getRenderX());
            float renderY = Math.round(getRenderY());

            RenderUtils.drawTexturedRect(guiGraphics, Texture.COOLDOWN_BACKGROUND, renderX, renderY);

            float remainingSeconds = interpolateTime.get()
                    ? Models.Ability.getInterpolatedCooldown(cooldown)
                    : cooldown.getServerRemainingSeconds();

            float maxSeconds = cooldown.getMaxSeconds();
            float ratio = maxSeconds > 0 ? remainingSeconds / maxSeconds : 0;

            int stage = Mth.clamp((int) ((1f - ratio) * (COOLDOWN_TEXTURE_STAGES - 1)), 0, COOLDOWN_TEXTURE_STAGES - 1);

            int texX = stage % TEXTURES_PER_ROW * 22;
            int texY = stage / TEXTURES_PER_ROW * 22;

            float rate = remainingSeconds / maxSeconds;
            float pct = Math.clamp(rate, 0.0f, 1.0f);
            CustomColor color = CustomColor.fromHSV(pct / 3.0f, 1.0f, 1.0f, 1.0f);

            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.COOLDOWN_TIMERS,
                    color,
                    renderX,
                    renderY,
                    22,
                    22,
                    texX,
                    texY,
                    22,
                    22,
                    Texture.COOLDOWN_TIMERS.width(),
                    Texture.COOLDOWN_TIMERS.height());
            RenderUtils.drawTexturedRect(guiGraphics, cooldown.getTexture(), renderX + 2f, renderY + 2f);

            if (!showTimer.get()) return;

            String time = interpolateTime.get()
                    ? String.format("%.1f", remainingSeconds)
                    : String.valueOf((int) remainingSeconds);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(time + "s"),
                            renderX,
                            renderX + 22,
                            renderY,
                            renderY + 32,
                            32,
                            color,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.BOTTOM,
                            textShadow.get(),
                            0.75f);
        }
    }
}
