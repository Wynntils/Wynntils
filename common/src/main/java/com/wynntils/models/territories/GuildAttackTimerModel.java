/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.TimedSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class GuildAttackTimerModel extends Model {
    private static final Pattern GUILD_ATTACK_PATTERN = Pattern.compile("§b- (.+):(.+) §3(.+)");
    private static final Pattern GUILD_DEFENSE_CHAT_PATTERN = Pattern.compile("§3.+§b (.+) defense is (.+)");
    private static final ScoreboardPart GUILD_ATTACK_SCOREBOARD_PART = new GuildAttackScoreboardPart();

    private final TimedSet<Pair<String, String>> territoryDefenseSet = new TimedSet<>(5, TimeUnit.SECONDS, true);

    private List<TerritoryAttackTimer> attackTimers = List.of();

    public GuildAttackTimerModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(GUILD_ATTACK_SCOREBOARD_PART);
    }

    @SubscribeEvent
    public void onMessage(ChatMessageReceivedEvent event) {
        if (event.getRecipientType() != RecipientType.GUILD) return;

        Matcher matcher = event.getOriginalStyledText().getMatcher(GUILD_DEFENSE_CHAT_PATTERN);
        if (!matcher.matches()) return;

        Optional<TerritoryAttackTimer> territory = attackTimers.stream()
                .filter(territoryAttackTimer -> territoryAttackTimer.territory().equals(matcher.group(1))
                        && !territoryAttackTimer.isDefenseKnown())
                .findFirst();

        if (territory.isPresent()) {
            territory.get().setDefense(matcher.group(2));
        } else {
            for (Pair<String, String> defensePair : territoryDefenseSet) {
                if (defensePair.a().equals(matcher.group(1))) {
                    return; // do not put it in the set twice
                }
            }

            territoryDefenseSet.put(new Pair<>(matcher.group(1), matcher.group(2)));
        }
    }

    public List<TerritoryAttackTimer> getAttackTimers() {
        return attackTimers;
    }

    public Optional<TerritoryAttackTimer> getAttackTimerForTerritory(String territory) {
        return attackTimers.stream()
                .filter(t -> t.territory().equals(territory))
                .findFirst();
    }

    void processChanges(ScoreboardSegment segment) {
        List<TerritoryAttackTimer> newList = new ArrayList<>();

        for (StyledText line : segment.getContent()) {
            Matcher matcher = line.getMatcher(GUILD_ATTACK_PATTERN);

            if (matcher.matches()) {
                TerritoryAttackTimer timer = new TerritoryAttackTimer(
                        matcher.group(3), Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                newList.add(timer);

                boolean foundDefense = false;
                Optional<TerritoryAttackTimer> oldTimer = attackTimers.stream()
                        .filter(territoryAttackTimer ->
                                territoryAttackTimer.territory().equals(timer.territory()))
                        .findFirst();

                if (oldTimer.isPresent()) {
                    if (oldTimer.get().isDefenseKnown()) {
                        timer.setDefense(oldTimer.get().defense());
                        foundDefense = true;
                    }
                }

                if (!foundDefense) {
                    for (Pair<String, String> defensePair : territoryDefenseSet) {
                        if (defensePair.a().equals(timer.territory())) {
                            timer.setDefense(defensePair.b());
                            break;
                        }
                    }
                }
            }
        }

        attackTimers = newList;
    }

    void resetTimers() {
        attackTimers = List.of();
    }
}
