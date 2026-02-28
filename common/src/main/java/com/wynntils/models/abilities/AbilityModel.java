/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.models.abilities.bossbars.AwakenedBar;
import com.wynntils.models.abilities.bossbars.BloodPoolBar;
import com.wynntils.models.abilities.bossbars.CommanderBar;
import com.wynntils.models.abilities.bossbars.CorruptedBar;
import com.wynntils.models.abilities.bossbars.FocusBar;
import com.wynntils.models.abilities.bossbars.HolyPowerBar;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.models.abilities.bossbars.MomentumBar;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import com.wynntils.models.abilities.event.AbilityCooldownsUpdatedEvent;
import com.wynntils.models.abilities.type.AbilityCooldown;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class AbilityModel extends Model {
    private static final Pattern HUMMINGBIRD_SENT_PATTERN =
            Pattern.compile("§e((\uE008\uE002)|\uE001) You sent your hummingbirds to attack!$");
    private static final Pattern HUMMINGBIRD_RETURN_PATTERN =
            Pattern.compile("§e((\uE008\uE002)|\uE001) Your hummingbirds have returned to you!$");

    public static final String COOLDOWN_PREFIX = "§8⬤";

    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    public static final OphanimBar ophanimBar = new OphanimBar();

    public static final TrackedBar holyPowerBar = new HolyPowerBar();

    public static final CommanderBar commanderBar = new CommanderBar();

    public static final MomentumBar momentumBar = new MomentumBar();

    public boolean hummingBirdsState = false;

    private static final List<TrackedBar> ALL_BARS = Arrays.asList(
            awakenedBar,
            bloodPoolBar,
            commanderBar,
            corruptedBar,
            focusBar,
            holyPowerBar,
            manaBankBar,
            momentumBar,
            ophanimBar);

    private final Set<AbilityCooldown> activeCooldowns = new HashSet<>();

    public AbilityModel() {
        super(List.of());

        ALL_BARS.forEach(Handlers.BossBar::registerBar);
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        StyledText message = StyledTextUtils.unwrap(event.getMessage().stripAlignment());
        if (message.matches(HUMMINGBIRD_RETURN_PATTERN)) {
            hummingBirdsState = false;
        } else if (message.matches(HUMMINGBIRD_SENT_PATTERN)) {
            hummingBirdsState = true;
        }
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
                return true;
            }
            return false;
        });

        // Add new ones
        activeCooldowns.addAll(presentCooldowns);

        WynntilsMod.postEvent(new AbilityCooldownsUpdatedEvent(activeCooldowns));
    }
}
