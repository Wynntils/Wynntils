/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.worlds.bossbars.InfoBar;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BombModel extends Model {
    public static final TrackedBar InfoBar = new InfoBar();

    private static final Pattern BOMB_BELL_PATTERN =
            Pattern.compile("^\\[Bomb Bell\\] (?<user>.+) has thrown an? (?<bomb>.+) Bomb on (?<server>.+)$");

    // §buser's §3bomb has expired. You can buy Profession XP bombs at our website, §bwynncraft.com/store
    private static final Pattern BOMB_EXPIRED_PATTERN = Pattern.compile(
            "§b(?<user>.+)'s? §3bomb has expired. You can buy (?<bomb>.+) bombs at our website, §bwynncraft.com/store");

    private static final Map<BombType, BombInfo> CURRENT_SERVER_BOMBS = new EnumMap<>(BombType.class);

    private static final ActiveBombContainer BOMBS = new ActiveBombContainer();

    public BombModel(WorldStateModel worldState) {
        super(List.of(worldState));
        Handlers.BossBar.registerBar(InfoBar);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        Matcher matcher = event.getOriginalStyledText().getMatcher(BOMB_BELL_PATTERN, PartStyle.StyleType.NONE);
        if (matcher.matches()) {
            String user = matcher.group("user");
            String bomb = matcher.group("bomb");
            String server = matcher.group("server");

            // Better to do a bit of processing and clean up the set than leaking memory
            removeOldTimers();

            BombType bombType = BombType.fromString(bomb);
            if (bombType == null) return;

            BOMBS.add(new BombInfo(user, bombType, server, System.currentTimeMillis(), bombType.getActiveMinutes()));

            return;
        }

        matcher = event.getOriginalStyledText().getMatcher(BOMB_EXPIRED_PATTERN);
        if (matcher.matches()) {
            String user = matcher.group("user");
            String bomb = matcher.group("bomb");

            // Better to do a bit of processing and clean up the set than leaking memory
            removeOldTimers();

            BombType bombType = BombType.fromString(bomb);
            if (bombType == null) return;

            BombInfo removed = CURRENT_SERVER_BOMBS.remove(bombType);
            if (removed == null) return;

            BOMBS.remove(removed);
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        CURRENT_SERVER_BOMBS.clear();
    }

    public void addBombInfo(BombType bombType, BombInfo bombInfo) {
        BombInfo old = CURRENT_SERVER_BOMBS.put(bombType, bombInfo);

        BOMBS.add(bombInfo);
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

    private static final class ActiveBombContainer {
        private Map<BombKey, BombInfo> bombs = new ConcurrentHashMap<>();

        public void add(BombInfo bombInfo) {
            BombKey key = new BombKey(bombInfo.server(), bombInfo.bomb());

            // Ensure no duplicate bombs are added
            if (bombs.containsKey(key) && bombs.get(key).isActive()) return;

            bombs.put(key, bombInfo);
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

        private record BombKey(String server, BombType type) {}
    }
}
