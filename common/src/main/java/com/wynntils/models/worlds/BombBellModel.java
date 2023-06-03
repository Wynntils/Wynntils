/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BombBellModel extends Model {
    private static final Pattern BOMB_BELL_PATTERN =
            Pattern.compile("^\\[Bomb Bell\\] (?<user>.+) has thrown an? (?<bomb>.+) Bomb on (?<server>.+)$");

    private static final Set<BombInfo> BOMB_BELLS = ConcurrentHashMap.newKeySet();

    public BombBellModel() {
        super(List.of());
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

            BOMB_BELLS.add(new BombInfo(user, BombType.fromString(bomb), server, System.currentTimeMillis()));
        }
    }

    private void removeOldTimers() {
        BOMB_BELLS.removeIf(bombInfo ->
                bombInfo.startTime() + (bombInfo.bomb().getActiveMinutes() * 60000L) < System.currentTimeMillis());
    }

    public Set<BombInfo> getBombBells() {
        removeOldTimers();
        return BOMB_BELLS;
    }
}
