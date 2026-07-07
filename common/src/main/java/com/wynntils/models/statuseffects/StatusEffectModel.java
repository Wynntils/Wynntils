/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.mc.event.PlayerInfoFooterChangedEvent;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class StatusEffectModel extends Model {
    /**
     * Parses a status effect entry into its individual components.
     *
     * <p>Supported formats include:
     *
     * <pre>
     * Ⓔ +22 󏿿󐀂Intelligence (03:28)
     * Ⓔ +40% Spell Damage (03:28)
     * Ⓔ +12/3s Mana Steal (06:08)
     * ⚔ Vengeful Spirit (00:00)
     * ❤ 10599 Extra Health (**:**)
     * </pre>
     *
     * <p>Captured groups:
     * <ul>
     *   <li><b>prefix</b> - Optional leading effect symbol or icon (Ⓔ, ⚔, ❤, ❁, etc.)</li>
     *   <li><b>modifier</b> - Optional numeric modifier value (+22, -10, 10599, 31)</li>
     *   <li><b>modifierSuffix</b> - Optional modifier suffix (% , /3s , /5s)</li>
     *   <li><b>icon</b> - Optional custom-font glyphs shown before the effect name
     *       (e.g. 󏿿󐀂 for Intelligence)</li>
     *   <li><b>name</b> - The effect name (Intelligence, Spell Damage, Mana Regen, etc.)</li>
     *   <li><b>timer</b> - The displayed duration string ((03:28), (01:09), (**:**))</li>
     *   <li><b>hours</b>, <b>minutes</b>, <b>seconds</b> - Individual timer components</li>
     * </ul>
     *
     * <p>The icon is defined as any non-alphanumeric characters immediately preceding
     * the name. The name itself must begin with an ASCII alphanumeric character,
     * allowing custom-font glyphs to be cleanly separated from effect names.
     *
     * <p>Test cases can be found in {@code TestStatusEffectParsing}.
     */
    private static final Pattern STATUS_EFFECT_PATTERN = Pattern.compile(
            "^(?:(?<prefix>\\S+)\\s+)?(?:(?<modifier>[+-]?[\\d.,]+)(?<modifierSuffix>(?:/\\d+s)|%)?\\s+)?(?<icon>[^A-Za-z0-9+']*)(?<name>[A-Za-z0-9][A-Za-z0-9 ]*?)\\s*(?<timer>\\((?:(?<hours>\\d{2}):)?(?<minutes>\\d{2}|\\*{2}):(?<seconds>\\d{2}|\\*{2})\\))$");

    private static final StyledText STATUS_EFFECTS_TITLE = StyledText.fromString("§d§lStatus Effects");

    private List<StatusEffect> statusEffects = List.of();

    public StatusEffectModel() {
        super(List.of());
    }

    public List<StatusEffect> getStatusEffects() {
        return statusEffects;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldStateChanged(WorldStateEvent e) {
        statusEffects = List.of();
        WynntilsMod.postEvent(new StatusEffectsChangedEvent(statusEffects));
    }

    @SubscribeEvent
    public void onTabListCustomization(PlayerInfoFooterChangedEvent event) {
        StyledText footer = event.getFooter();

        if (footer.isEmpty()) {
            if (!statusEffects.isEmpty()) {
                statusEffects = List.of(); // No timers, get rid of them
                WynntilsMod.postEvent(new StatusEffectsChangedEvent(statusEffects));
            }
            return;
        }

        if (!footer.startsWith(STATUS_EFFECTS_TITLE)) return;

        statusEffects = parseStatusEffects(footer);
        WynntilsMod.postEvent(new StatusEffectsChangedEvent(statusEffects));
    }

    public static List<StatusEffect> parseStatusEffects(StyledText footer) {
        List<StatusEffect> newStatusEffects = new ArrayList<>();

        StyledText[] effects = footer.split("\\n|\\s{2,}"); // Effects are split up by 2 spaces or new lines
        for (StyledText effect : effects) {
            StyledText trimmedEffect = effect.trim();
            if (trimmedEffect.isEmpty()) continue;

            StatusEffect statusEffect = parseStatusEffect(trimmedEffect);
            if (statusEffect != null) {
                newStatusEffects.add(statusEffect);
            }
        }

        return newStatusEffects;
    }

    private static StatusEffect parseStatusEffect(StyledText effect) {
        String effectString = effect.getStringWithoutFormatting();

        Matcher matcher = STATUS_EFFECT_PATTERN.matcher(effectString);
        if (!matcher.matches()) return null;

        StyledText displayedTime = effect.substring(matcher.start("timer"), matcher.end("timer"), StyleType.NONE);

        int duration = parseDuration(matcher.group("hours"), matcher.group("minutes"), matcher.group("seconds"));

        StyledText prefix = matcher.group("prefix") == null
                ? StyledText.EMPTY
                : effect.substring(matcher.start("prefix"), matcher.end("prefix"), StyleType.NONE);

        StyledText modifier = matcher.group("modifier") == null
                ? StyledText.EMPTY
                : effect.substring(matcher.start("modifier"), matcher.end("modifier"), StyleType.NONE);

        StyledText modifierSuffix = matcher.group("modifierSuffix") == null
                ? StyledText.EMPTY
                : effect.substring(matcher.start("modifierSuffix"), matcher.end("modifierSuffix"), StyleType.NONE);

        StyledText icon = matcher.group("icon") == null
                ? StyledText.EMPTY
                : effect.substring(matcher.start("icon"), matcher.end("icon"), StyleType.NONE)
                        .trim();

        StyledText name = effect.substring(matcher.start("name"), matcher.end("name"), StyleType.NONE)
                .trim();

        if (name.isEmpty()) return null;

        return new StatusEffect(name, modifier, modifierSuffix, icon, displayedTime, prefix, duration);
    }

    private static int parseDuration(String hoursString, String minutesString, String secondsString) {
        try {
            int duration = Integer.parseInt(minutesString) * 60 + Integer.parseInt(secondsString);
            if (hoursString != null) {
                duration += Integer.parseInt(hoursString) * 3600;
            }
            return duration;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public StatusEffect searchStatusEffectByName(String query) {
        for (StatusEffect effect : statusEffects) {
            if (effect.getName().getStringWithoutFormatting().startsWith(query)) {
                return effect;
            }
        }
        return null;
    }
}
