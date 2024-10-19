/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.consumers.commands.ClientCommandManager;
import com.wynntils.core.consumers.features.FeatureManager;
import com.wynntils.core.consumers.functions.FunctionManager;
import com.wynntils.core.consumers.overlays.OverlayManager;
import com.wynntils.core.crowdsource.CrowdSourcedDataManager;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.ConnectionManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.net.DownloadManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.core.persisted.PersistedManager;
import com.wynntils.core.persisted.config.ConfigManager;
import com.wynntils.core.persisted.storage.StorageManager;
import com.wynntils.core.persisted.upfixers.UpfixerManager;
import com.wynntils.core.properties.SystemPropertiesManager;

public final class Managers {
    // Start with SystemPropertiesManager so it can bootstrap before other Managers access properties
    public static final SystemPropertiesManager SystemProperties = new SystemPropertiesManager();

    // Then, load UrlManager to give it chance to update URLs in background
    public static final NetManager Net = new NetManager();
    public static final UrlManager Url = new UrlManager(Net);
    public static final DownloadManager Download = new DownloadManager();

    public static final ClientCommandManager Command = new ClientCommandManager();
    public static final ConfigManager Config = new ConfigManager();
    public static final ConnectionManager Connection = new ConnectionManager();
    public static final CrashReportManager CrashReport = new CrashReportManager();
    public static final CrowdSourcedDataManager CrowdSourcedData = new CrowdSourcedDataManager();
    public static final FeatureManager Feature = new FeatureManager();
    public static final FunctionManager Function = new FunctionManager();
    public static final JsonManager Json = new JsonManager();
    public static final KeyBindManager KeyBind = new KeyBindManager();
    public static final NotificationManager Notification = new NotificationManager();
    public static final PersistedManager Persisted = new PersistedManager();
    public static final StorageManager Storage = new StorageManager();
    public static final TickSchedulerManager TickScheduler = new TickSchedulerManager();
    public static final UpfixerManager Upfixer = new UpfixerManager();

    // Managers with constructor dependencies, ordered alphabetically as far as possible
    public static final OverlayManager Overlay = new OverlayManager(CrashReport);
}
