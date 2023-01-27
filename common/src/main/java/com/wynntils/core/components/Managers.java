/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.chat.ChatTabManager;
import com.wynntils.core.commands.CommandManager;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.config.upfixers.ConfigUpfixerManager;
import com.wynntils.core.features.FeatureManager;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.ConnectionManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.UpdateManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.core.net.translation.TranslationManager;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.core.splashes.SplashManager;

public final class Managers {
    // Start with UrlManager to give it chance to update URLs in background
    public static final UrlManager Url = new UrlManager();

    public static final ChatTabManager ChatTab = new ChatTabManager();
    public static final CommandManager Command = new CommandManager();
    public static final ConfigUpfixerManager ConfigUpfixer = new ConfigUpfixerManager();
    public static final ConnectionManager Connection = new ConnectionManager();
    public static final CrashReportManager CrashReport = new CrashReportManager();
    public static final FunctionManager Function = new FunctionManager();
    public static final KeyBindManager KeyBind = new KeyBindManager();
    public static final NotificationManager Notification = new NotificationManager();
    public static final TickSchedulerManager TickScheduler = new TickSchedulerManager();
    public static final TranslationManager Translation = new TranslationManager();

    // Managers with dependencies, ordered alphabetically as far as possible
    public static final ConfigManager Config = new ConfigManager(ConfigUpfixer);
    public static final FeatureManager Feature = new FeatureManager(Config, CrashReport);
    public static final NetManager Net = new NetManager(Url);
    public static final OverlayManager Overlay = new OverlayManager(CrashReport);
    public static final SplashManager Splash = new SplashManager(Net);
    public static final UpdateManager Update = new UpdateManager(Net);
    public static final WynntilsAccountManager WynntilsAccount = new WynntilsAccountManager(Net);
}
