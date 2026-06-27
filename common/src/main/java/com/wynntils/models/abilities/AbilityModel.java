/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.DistortionBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.models.abilities.bossbars.MirrorImageBar;
import com.wynntils.models.abilities.bossbars.MomentumBar;
import com.wynntils.models.abilities.bossbars.NightcloakKnivesBar;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import com.wynntils.models.abilities.event.AbilityCooldownRefreshedEvent;
import com.wynntils.models.abilities.event.AbilityCooldownsUpdatedEvent;
import com.wynntils.models.abilities.type.AbilityCooldown;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.models.statuseffects.type.StatusEffect;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class AbilityModel extends Model {
    public static final String COOLDOWN_PREFIX = "§8⬤";

    private static final Pattern REFRESH_PATTERN = Pattern.compile("\\[⬤\\] (.+) has been refreshed!");
    private static final float COOLDOWN_EPSILON_SECONDS = 0.001f;

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final CommanderBar commanderBar = new CommanderBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final DistortionBar distortionBar = new DistortionBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar holyPowerBar = new HolyPowerBar();

    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final MirrorImageBar mirrorImageBar = new MirrorImageBar();

    public static final MomentumBar momentumBar = new MomentumBar();

    public static final NightcloakKnivesBar nightcloakKnivesBar = new NightcloakKnivesBar();

    public static final OphanimBar ophanimBar = new OphanimBar();

    private static final List<TrackedBar> ALL_BARS = Arrays.asList(
            awakenedBar,
            bloodPoolBar,
            commanderBar,
            corruptedBar,
            distortionBar,
            focusBar,
            holyPowerBar,
            manaBankBar,
            mirrorImageBar,
            momentumBar,
            nightcloakKnivesBar,
            ophanimBar);

    private final Set<AbilityCooldown> activeCooldowns = new HashSet<>();
    private final Map<AbilityCooldown, Float> interpolatedCooldowns = new HashMap<>();
    private final Map<AbilityCooldown, Long> lastTickNanosMap = new HashMap<>();

    public AbilityModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onStatusEffectUpdate(StatusEffectsChangedEvent event) {
        Set<AbilityCooldown> presentCooldowns = new HashSet<>();

        for (StatusEffect statusEffect : event.getOriginalStatusEffects()) {
            if (statusEffect.getPrefix().getString().equals(COOLDOWN_PREFIX)) {
                AbilityCooldown cooldown = AbilityCooldown.fromStatusEffect(statusEffect);

                if (cooldown != null) {
                    // +1 because the cooldowns display as 00:00 even when still on cooldown for the final second
                    float serverSeconds = (float) (statusEffect.getDuration() + 1);

                    cooldown.setServerRemainingSeconds(serverSeconds);
                    presentCooldowns.add(cooldown);
                }
            }
        }

        // Remove any cooldowns no longer present and clear their state
        activeCooldowns.removeIf(cooldown -> {
            if (!presentCooldowns.contains(cooldown)) {
                cooldown.resetCooldownState();
                interpolatedCooldowns.remove(cooldown);
                lastTickNanosMap.remove(cooldown);
                return true;
            }
            return false;
        });

        // Initialise interpolation state for newly added cooldowns
        for (AbilityCooldown cooldown : presentCooldowns) {
            if (!activeCooldowns.contains(cooldown)) {
                interpolatedCooldowns.putIfAbsent(cooldown, cooldown.getServerRemainingSeconds());
                lastTickNanosMap.putIfAbsent(cooldown, System.nanoTime());
            }
        }

        activeCooldowns.addAll(presentCooldowns);

        WynntilsMod.postEvent(new AbilityCooldownsUpdatedEvent(activeCooldowns));
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        for (AbilityCooldown cooldown : activeCooldowns) {
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChat(ChatMessageEvent.Match event) {
        Matcher matcher = event.getMessage().getMatcher(REFRESH_PATTERN, StyleType.NONE);
        if (!matcher.matches()) return;

        AbilityCooldownRefreshedEvent cooldownRefreshedEvent = new AbilityCooldownRefreshedEvent(event.getMessage());
        WynntilsMod.postEvent(cooldownRefreshedEvent);

        if (cooldownRefreshedEvent.shouldCancelMessage()) {
            event.cancelChat();
        }
    }

    public float getInterpolatedCooldown(AbilityCooldown cooldown) {
        return interpolatedCooldowns.getOrDefault(cooldown, cooldown.getServerRemainingSeconds());
    }

    public Set<AbilityCooldown> getActiveCooldowns() {
        return Collections.unmodifiableSet(activeCooldowns);
    }
}
