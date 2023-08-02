/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.consumers.commands.CommandManager;
import com.wynntils.core.consumers.features.FeatureManager;
import com.wynntils.core.consumers.functions.FunctionManager;
import com.wynntils.core.consumers.overlays.OverlayManager;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.ConnectionManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.core.persisted.PersistedManager;
import com.wynntils.core.persisted.config.ConfigManager;
import com.wynntils.core.persisted.storage.StorageManager;
import com.wynntils.core.persisted.upfixers.ConfigUpfixerManager;

public final class Managers {
    // Start with UrlManager to give it chance to update URLs in background
    // We need TickScheduler to send out the loaded events on the main thread
    public static final TickSchedulerManager TickScheduler = new TickSchedulerManager();
    public static final UrlManager Url = new UrlManager(TickScheduler);

    public static final CommandManager Command = new CommandManager();
    public static final ConfigUpfixerManager ConfigUpfixer = new ConfigUpfixerManager();
    public static final ConnectionManager Connection = new ConnectionManager();
    public static final CrashReportManager CrashReport = new CrashReportManager();
    public static final FunctionManager Function = new FunctionManager();
    public static final JsonManager Json = new JsonManager();
    public static final KeyBindManager KeyBind = new KeyBindManager();
    public static final NotificationManager Notification = new NotificationManager();
    public static final PersistedManager Persisted = new PersistedManager();

    // Managers with dependencies, ordered alphabetically as far as possible
    public static final OverlayManager Overlay = new OverlayManager(CrashReport);
    public static final FeatureManager Feature = new FeatureManager(Command, CrashReport, KeyBind, Overlay);
    public static final ConfigManager Config = new ConfigManager(ConfigUpfixer, Json, Feature, Overlay);
    public static final NetManager Net = new NetManager(Url);
    public static final StorageManager Storage = new StorageManager(Json, Feature);
}
