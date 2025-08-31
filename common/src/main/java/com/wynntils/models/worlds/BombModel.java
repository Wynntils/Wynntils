/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.worlds.bossbars.InfoBar;
import com.wynntils.models.worlds.event.BombEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class BombModel extends Model {
    private static final TrackedBar InfoBar = new InfoBar();

    // Test in BombModel_BOMB_BELL_PATTERN
    private static final Pattern BOMB_BELL_PATTERN = Pattern.compile(
            "^§#fddd5cff(?:\uE01E\uE002|\uE001) (?<user>.+) has thrown an? §#f3e6b2ff(?<bomb>.+) Bomb§#fddd5cff on §#f3e6b2ff§n(?<server>.+)$");

    // Test in BombModel_BOMB_EXPIRED_PATTERN
    private static final Pattern BOMB_EXPIRED_PATTERN = Pattern.compile(
            "^§#a0c84bff(?:\uE014\uE002|\uE001) §#ffd750ff.+§#a0c84bff (?<bomb>.+) Bomb has expired!.*$");

    // Test in BombModel_BOMB_THROWN_PATTERN
    private static final Pattern BOMB_THROWN_PATTERN =
            Pattern.compile("^§#a0c84bff(?:\uE014\uE002|\uE001) §l(?<bomb>.+) Bomb$");

    private static final Map<BombType, BombInfo> CURRENT_SERVER_BOMBS = new EnumMap<>(BombType.class);

    private static final ActiveBombContainer BOMBS = new ActiveBombContainer();

    public BombModel() {
        super(List.of());

        Handlers.BossBar.registerBar(InfoBar);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        StyledText message = event.getOriginalStyledText();
        StyledText unwrapped =
                StyledTextUtils.unwrap(event.getOriginalStyledText()).stripAlignment();

        Matcher bellMatcher = unwrapped.getMatcher(BOMB_BELL_PATTERN);
        if (bellMatcher.matches()) {
            BombInfo bombInfo = addBombFromChat(
                    bellMatcher.group("user"),
                    bellMatcher.group("bomb"),
                    bellMatcher.group("server").trim());
            if (bombInfo == null) return;

            BombEvent.BombBell bombEvent = new BombEvent.BombBell(bombInfo, message);
            WynntilsMod.postEvent(bombEvent);
            event.setMessage(bombEvent.getMessage());

            return;
        }

        Matcher localMatcher = unwrapped.getMatcher(BOMB_THROWN_PATTERN);
        if (localMatcher.matches()) {
            // FIXME: User is sent on following chat line, we don't currently use the name anywhere but if we do in
            //  the future then this needs fixing
            BombInfo bombInfo =
                    addBombFromChat("", localMatcher.group("bomb"), Models.WorldState.getCurrentWorldName());
            if (bombInfo == null) return;

            BombEvent.Local bombEvent = new BombEvent.Local(bombInfo, message);
            WynntilsMod.postEvent(bombEvent);
            event.setMessage(bombEvent.getMessage());
            return;
        }

        Matcher expiredMatcher = unwrapped.getMatcher(BOMB_EXPIRED_PATTERN);
        if (expiredMatcher.matches()) {
            String bomb = expiredMatcher.group("bomb");

            // Better to do a bit of processing and clean up the set than leaking memory
            removeOldTimers();

            BombType bombType = BombType.fromString(bomb);
            if (bombType == null) return;

            BombInfo removed = CURRENT_SERVER_BOMBS.remove(bombType);
            if (removed == null) return;

            BOMBS.remove(removed);
        }
    }

    private BombInfo addBombFromChat(String user, String bomb, String server) {
        // Better to do a bit of processing and clean up the set than leaking memory
        removeOldTimers();

        BombType bombType = BombType.fromString(bomb);
        if (bombType == null) return null;

        return BOMBS.forceAdd(
                new BombInfo(user, bombType, server, System.currentTimeMillis(), bombType.getActiveMinutes()));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        CURRENT_SERVER_BOMBS.clear();
    }

    public void addBombInfoFromInfoBar(BombInfo bombInfo) {
        CURRENT_SERVER_BOMBS.put(bombInfo.bomb(), bombInfo);

        BOMBS.add(bombInfo);
        WynntilsMod.postEvent(new BombEvent.Local(bombInfo, null));
    }

    public boolean isBombActive(BombType bombType) {
        return CURRENT_SERVER_BOMBS.containsKey(bombType)
                && CURRENT_SERVER_BOMBS.get(bombType).isActive();
    }

    private void removeOldTimers() {
        BOMBS.removeIf(bombInfo -> !bombInfo.isActive());
    }

    public Set<BombInfo> getBombBells() {
        removeOldTimers();
        return BOMBS.asSet();
    }

    public BombInfo getLastBomb() {
        return BOMBS.getLastBomb();
    }

    private static final class ActiveBombContainer {
        private final Map<BombKey, BombInfo> bombs = new ConcurrentHashMap<>();

        public BombInfo add(BombInfo bombInfo) {
            return add(bombInfo, false);
        }

        public BombInfo forceAdd(BombInfo bombInfo) {
            return add(bombInfo, true);
        }

        private BombInfo add(BombInfo bombInfo, boolean replaceIfExists) {
            BombKey key = new BombKey(bombInfo.server(), bombInfo.bomb());

            // Ensure no duplicate bombs are added
            if (bombs.containsKey(key) && !replaceIfExists) {
                return bombs.get(key);
            }

            bombs.put(key, bombInfo);
            return bombInfo;
        }

        public Set<BombInfo> asSet() {
            return Set.copyOf(bombs.values());
        }

        public void removeIf(Predicate<BombInfo> predicate) {
            bombs.entrySet().removeIf(entry -> predicate.test(entry.getValue()));
        }

        public void remove(BombInfo removed) {
            bombs.remove(new BombKey(removed.server(), removed.bomb()));
        }

        /**
         * @return Newest bomb bell received, or null if none exist
         */
        public BombInfo getLastBomb() {
            return bombs.values().stream()
                    .max(Comparator.comparingLong(BombInfo::startTime))
                    .orElse(null);
        }
    }

    private record BombKey(String server, BombType type) {}
}
