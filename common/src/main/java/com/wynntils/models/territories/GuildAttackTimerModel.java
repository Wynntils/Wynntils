/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.territories.event.GuildWarQueuedEvent;
import com.wynntils.models.territories.markers.GuildAttackMarkerProvider;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * A note about territory attack timers:
 * - Chat messages are sent on attack, and on certain time intervals
 * - Scoreboard is always up to date, but can cut off vertically and horizontally
 * (this means it can cut off long territory names and full lines, if there are too many attacks)
 * <p>
 * We use a hybrid approach:
 * - Use chat messages when we can, as they are more reliable
 * - Use scoreboard messages when we don't have a chat message
 * - If a chat message is received for a territory, we stop using the scoreboard for that territory
 * - We also take territory defenses from chat messages
 */
public final class GuildAttackTimerModel extends Model {
    private static final Pattern GUILD_ATTACK_PATTERN = Pattern.compile("§b- (.+):(.+) §3(.+)");
    private static final Pattern GUILD_DEFENSE_CHAT_PATTERN = Pattern.compile("§3.+§b (.+) defense is (.+)");
    private static final Pattern WAR_MESSAGE_PATTERN = Pattern.compile(
            "§3\\[WAR\\]§c The war for (?<territory>.+) will start in (?<remaining>.+) (?<type>minutes|seconds)\\.");
    private static final Pattern CAPTURED_PATTERN =
            Pattern.compile("§3\\[WAR\\]§c \\[(?<guild>.+)\\] (?:has )?captured the territory (?<territory>.+)\\.");
    private static final ScoreboardPart GUILD_ATTACK_SCOREBOARD_PART = new GuildAttackScoreboardPart();

    private static final GuildAttackMarkerProvider GUILD_ATTACK_MARKER_PROVIDER = new GuildAttackMarkerProvider();

    private final Map<TerritoryProfile, GuildResourceValues> territoryDefenses = new HashMap<>();
    private final Map<TerritoryProfile, TerritoryAttackTimer> chatAttackTimers = new HashMap<>();
    private final Map<TerritoryProfile, TerritoryAttackTimer> scoreboardAttackTimers = new HashMap<>();

    public GuildAttackTimerModel(MarkerModel marker) {
        super(List.of(marker));

        Handlers.Scoreboard.addPart(GUILD_ATTACK_SCOREBOARD_PART);
        Models.Marker.registerMarkerProvider(GUILD_ATTACK_MARKER_PROVIDER);
    }

    @SubscribeEvent
    public void onMessage(ChatMessageReceivedEvent event) {
        if (event.getRecipientType() != RecipientType.GUILD) return;

        Matcher matcher = event.getOriginalStyledText().getMatcher(WAR_MESSAGE_PATTERN);
        if (matcher.matches()) {
            long remaining = Long.parseLong(matcher.group("remaining"));
            long timerEnd = (matcher.group("type").equals("minutes") ? remaining * 60 : remaining) * 1000
                    + System.currentTimeMillis();
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfile(matcher.group("territory"));

            if (territoryProfile == null) {
                WynntilsMod.warn("Received war message for unknown territory: " + matcher.group("guild"));
                return;
            }

            TerritoryAttackTimer scoreboardTimer = scoreboardAttackTimers.remove(territoryProfile);

            TerritoryAttackTimer attackTimer = new TerritoryAttackTimer(territoryProfile, timerEnd);
            TerritoryAttackTimer oldTimer = chatAttackTimers.put(territoryProfile, attackTimer);

            // If we didn't have a timer before, post an event
            if (oldTimer == null && scoreboardTimer == null) {
                WynntilsMod.postEvent(new GuildWarQueuedEvent(attackTimer));
            }
        }

        matcher = event.getOriginalStyledText().getMatcher(CAPTURED_PATTERN);
        if (matcher.matches()) {
            // Remove the attack timer for the territory, if it exists
            // (the captured message appears for both owned and attacked territories)
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfile(matcher.group("territory"));

            if (territoryProfile == null) {
                WynntilsMod.warn("Received captured message for unknown territory: " + matcher.group("territory"));
                return;
            }

            chatAttackTimers.remove(territoryProfile);
            scoreboardAttackTimers.remove(territoryProfile);
        }

        matcher = event.getOriginalStyledText().getMatcher(GUILD_DEFENSE_CHAT_PATTERN);
        if (matcher.matches()) {
            TerritoryProfile territoryProfile = Models.Territory.getTerritoryProfile(matcher.group(1));
            territoryDefenses.put(territoryProfile, GuildResourceValues.fromString(matcher.group(2)));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        // Every 20 ticks, clean up old timers
        if (McUtils.player().tickCount % 20 != 0) return;

        long currentTime = System.currentTimeMillis();

        List<TerritoryProfile> removedTimers = new ArrayList<>();
        for (Map.Entry<TerritoryProfile, TerritoryAttackTimer> entry : chatAttackTimers.entrySet()) {
            if (entry.getValue().timerEnd() < currentTime) {
                removedTimers.add(entry.getKey());
            }
        }

        removedTimers.forEach(chatAttackTimers::remove);

        removedTimers.clear();

        for (Map.Entry<TerritoryProfile, TerritoryAttackTimer> entry : scoreboardAttackTimers.entrySet()) {
            if (entry.getValue().timerEnd() < currentTime) {
                removedTimers.add(entry.getKey());
            }
        }

        removedTimers.forEach(scoreboardAttackTimers::remove);
    }

    public List<TerritoryAttackTimer> getAttackTimers() {
        return getUpcomingTimers().toList();
    }

    public Optional<TerritoryAttackTimer> getAttackTimerForTerritory(String territory) {
        return getUpcomingTimers()
                .filter(t -> t.territoryProfile().getFriendlyName().equals(territory))
                .findFirst();
    }

    public Stream<TerritoryAttackTimer> getUpcomingTimers() {
        return Stream.concat(chatAttackTimers.values().stream(), scoreboardAttackTimers.values().stream())
                .filter(t -> t.timerEnd() > System.currentTimeMillis());
    }

    public Optional<GuildResourceValues> getDefenseForTerritory(TerritoryProfile territory) {
        return Optional.ofNullable(territoryDefenses.get(territory));
    }

    void processScoreboardChanges(ScoreboardSegment segment) {
        Set<TerritoryProfile> usedProfiles = new HashSet<>();

        for (StyledText line : segment.getContent()) {
            Matcher matcher = line.getMatcher(GUILD_ATTACK_PATTERN);

            if (matcher.matches()) {
                String territory = matcher.group(3);

                // Scoreboard cuts off long territory names, so we need to "guess" the full name
                // There could be multiple matches, so we need to sort by the timer difference, the closest one is the
                // correct one
                Optional<TerritoryAttackTimer> chatTimerOpt = chatAttackTimers.values().stream()
                        .filter(timer ->
                                timer.territoryProfile().getFriendlyName().startsWith(territory))
                        .min((a, b) -> (int) (a.timerEnd() - b.timerEnd()));

                // If we found a chat timer, use it
                if (chatTimerOpt.isPresent()) {
                    // Don't put a new timer in for scoreboard if we already have one from chat
                    usedProfiles.add(chatTimerOpt.get().territoryProfile());
                    continue;
                }

                // Only use the scoreboard timer if we didn't find a chat timer
                // (eg. we joined the server after the war was announced)
                TerritoryProfile territoryProfile =
                        Models.Territory.getTerritoryProfileFromShortName(territory, usedProfiles);

                if (territoryProfile == null) {
                    WynntilsMod.warn("Received scoreboard attack timer for unknown territory: " + territory);
                    continue;
                }

                usedProfiles.add(territoryProfile);

                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));

                long timerEnd = (minutes * 60L + seconds) * 1000 + System.currentTimeMillis();

                TerritoryAttackTimer timer = new TerritoryAttackTimer(territoryProfile, timerEnd);
                TerritoryAttackTimer oldTimer = scoreboardAttackTimers.put(territoryProfile, timer);

                // If we didn't have a timer before, post an event
                if (oldTimer == null) {
                    WynntilsMod.postEvent(new GuildWarQueuedEvent(timer));
                }
            }
        }
    }
}
