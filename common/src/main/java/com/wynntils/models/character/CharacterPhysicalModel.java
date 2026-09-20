/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.bossbar.DeathScreenBar;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.models.character.event.CharacterMovedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.Position;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Tracks physical, transient aspects of the player's in-world character, such as
 * movement, death state, and position. Unlike {@link CharacterModel}, this model
 * only concerns moment-to-moment presence in the game world.
 */
public final class CharacterPhysicalModel extends Model {
    private static final DeathScreenBar deathScreenBar = new DeathScreenBar();

    private static final int MOVE_CHECK_FREQUENCY = 10;
    private int moveCheckTicks;
    private Position currentPosition;
    private boolean deathViaKillCommand = false;

    public CharacterPhysicalModel() {
        super(List.of());

        Handlers.BossBar.registerBar(deathScreenBar);
    }

    @SubscribeEvent
    public void onCommandSent(CommandSentEvent e) {
        if (Objects.equals(e.getCommand(), "kill")) {
            deathViaKillCommand = true;
        }
    }

    // This event does not fire for raid or lootrun deaths
    @SubscribeEvent
    public void onBossBarAdd(BossBarAddedEvent event) {
        if (event.getTrackedBar() == deathScreenBar) {
            if (deathViaKillCommand) {
                WynntilsMod.postEvent(new CharacterDeathEvent(new Location(McUtils.player().blockPosition()), false));
                deathViaKillCommand = false;
            } else {
                WynntilsMod.postEvent(new CharacterDeathEvent(new Location(McUtils.player().blockPosition()), true));
            }
        }
    }

    @SubscribeEvent
    public void checkPlayerMove(TickEvent e) {
        if (McUtils.player() == null) return;
        if (moveCheckTicks++ % MOVE_CHECK_FREQUENCY != 0) return;

        Position newPosition = McUtils.player().position();
        if (newPosition != currentPosition) {
            currentPosition = newPosition;
            WynntilsMod.postEvent(new CharacterMovedEvent(currentPosition));
        }
    }
}
