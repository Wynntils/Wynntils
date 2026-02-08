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
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class AbilityCooldownsOverlay extends ContainerOverlay<AbilityCooldownsOverlay.AbilityCooldownOverlay> {
    private static final Pattern REFRESH_PATTERN = Pattern.compile("\\[⬤\\] (.+) has been refreshed!");

    // Stores interpolated cooldown times
    private final Map<AbilityCooldown, Float> interpolatedCooldowns = new HashMap<>();
    private final Map<AbilityCooldown, Long> lastTickNanosMap = new HashMap<>();

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
                new AbilityCooldownOverlay(AbilityCooldown.COUNTER),
                new AbilityCooldownOverlay(AbilityCooldown.VANISH));
    }

    @SubscribeEvent
    public void onAbilityCooldownsUpdate(AbilityCooldownsUpdatedEvent event) {
        this.clearChildren();
        Set<AbilityCooldown> newCooldowns = event.getCooldowns();
        interpolatedCooldowns.keySet().removeIf(cooldown -> !newCooldowns.contains(cooldown));
        lastTickNanosMap.keySet().removeIf(cooldown -> !newCooldowns.contains(cooldown));

        for (AbilityCooldown cooldown : newCooldowns) {
            this.addChild(new AbilityCooldownOverlay(cooldown));
            interpolatedCooldowns.putIfAbsent(cooldown, cooldown.getServerRemainingSeconds());
            lastTickNanosMap.putIfAbsent(cooldown, System.nanoTime());
        }
    }

    @SubscribeEvent
    public void onStatusEffectUpdate(StatusEffectsChangedEvent event) {
        if (!removeStatusEffect.get()) return;

        for (StatusEffect statusEffect : event.getOriginalStatusEffects()) {
            if (statusEffect.getPrefix().getString().equals(Models.Ability.COOLDOWN_PREFIX)) {
                event.removeStatusEffect(statusEffect);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ChatMessageEvent.Match event) {
        if (redirectRefreshedMessages.get() == RedirectAction.KEEP) return;

        if (event.getMessage().matches(REFRESH_PATTERN, StyleType.NONE)) {
            event.cancelChat();

            if (redirectRefreshedMessages.get() == RedirectAction.REDIRECT) {
                Managers.Notification.queueMessage(event.getMessage());
            }
        }
    }

    public final class AbilityCooldownOverlay extends Overlay {
        private static final float COOLDOWN_EPSILON_SECONDS = 0.001f;
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
        public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
            float renderX = Math.round(getRenderX());
            float renderY = Math.round(getRenderY());

            RenderUtils.drawTexturedRect(guiGraphics, Texture.COOLDOWN_BACKGROUND, renderX, renderY);

            float remainingSeconds = interpolateTime.get()
                    ? interpolatedCooldowns.getOrDefault(cooldown, cooldown.getServerRemainingSeconds())
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

        @Override
        public void tick() {
            if (!interpolateTime.get()) return;

            long now = System.nanoTime();
            long lastTickNanos = lastTickNanosMap.getOrDefault(cooldown, 0L);
            if (lastTickNanos == 0L) {
                interpolatedCooldowns.put(cooldown, cooldown.getServerRemainingSeconds());
                lastTickNanosMap.put(cooldown, now);
                return;
            }

            float dtSeconds = (now - lastTickNanos) / 1_000_000_000.0f;
            lastTickNanosMap.put(cooldown, now);

            dtSeconds = Mth.clamp(dtSeconds, 0.0f, 0.5f);

            float server = cooldown.getServerRemainingSeconds();
            float interpolated = interpolatedCooldowns.getOrDefault(cooldown, server);

            // interpolate toward server value
            interpolated -= dtSeconds;
            interpolated = Math.max(0.0f, interpolated);

            if (server > 0.0f) {
                float floor = Math.max(0.0f, server - 1.0f + COOLDOWN_EPSILON_SECONDS);
                if (interpolated < floor) {
                    interpolated = floor;
                }
                if (interpolated > server) {
                    interpolated = server;
                }
            }

            interpolatedCooldowns.put(cooldown, interpolated);
        }
    }
}
