/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.handlers.actionbar.ActionBarHandler;
import com.wynntils.handlers.bossbar.BossBarHandler;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.handlers.command.CommandHandler;
import com.wynntils.handlers.container.ContainerQueryHandler;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.handlers.labels.LabelHandler;
import com.wynntils.handlers.particle.ParticleHandler;
import com.wynntils.handlers.scoreboard.ScoreboardHandler;
import com.wynntils.handlers.tooltip.TooltipHandler;
import com.wynntils.handlers.wrappedscreen.WrappedScreenHandler;

public final class Handlers {
    public static final ActionBarHandler ActionBar = new ActionBarHandler();
    public static final BossBarHandler BossBar = new BossBarHandler();
    public static final ChatHandler Chat = new ChatHandler();
    public static final CommandHandler Command = new CommandHandler();
    public static final ContainerQueryHandler ContainerQuery = new ContainerQueryHandler();
    public static final ItemHandler Item = new ItemHandler();
    public static final LabelHandler Label = new LabelHandler();
    public static final ParticleHandler Particle = new ParticleHandler();
    public static final ScoreboardHandler Scoreboard = new ScoreboardHandler();
    public static final TooltipHandler Tooltip = new TooltipHandler();
    public static final WrappedScreenHandler WrappedScreen = new WrappedScreenHandler();
}
