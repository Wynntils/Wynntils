/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.chat.ChatTabManager;
import com.wynntils.core.commands.ClientCommandManager;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.config.upfixers.ConfigUpfixerManager;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.functions.FunctionManager;
import com.wynntils.core.keybinds.KeyBindManager;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.UpdateManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.core.net.hades.HadesManager;
import com.wynntils.core.net.translation.TranslationManager;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.core.splashes.SplashManager;
import com.wynntils.models.worlds.WorldStateManager;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class Managers {
    // Start with UrlManager to give it chance to update URLs in background
    public static final UrlManager Url = new UrlManager();

    public static final ChatTabManager ChatTab = new ChatTabManager();
    public static final ClientCommandManager ClientCommand = new ClientCommandManager();
    public static final ConfigUpfixerManager ConfigUpfixer = new ConfigUpfixerManager();
    public static final CrashReportManager CrashReport = new CrashReportManager();
    public static final FunctionManager Function = new FunctionManager();
    public static final KeyBindManager KeyBind = new KeyBindManager();
    public static final NotificationManager Notification = new NotificationManager();
    public static final TickSchedulerManager TickScheduler = new TickSchedulerManager();
    public static final TranslationManager Translation = new TranslationManager();
    public static final WorldStateManager WorldState = new WorldStateManager();

    // Managers with dependencies, ordered by dependency and then alphabetically
    public static final ConfigManager Config = new ConfigManager(ConfigUpfixer);
    public static final NetManager Net = new NetManager(Url);
    public static final OverlayManager Overlay = new OverlayManager(CrashReport);
    public static final SplashManager Splash = new SplashManager(Net);
    public static final UpdateManager Update = new UpdateManager(Net);
    public static final WynntilsAccountManager WynntilsAccount = new WynntilsAccountManager(Net);
    public static final HadesManager Hades = new HadesManager(WynntilsAccount, WorldState);

    public static void init() {
        // Register all manager singletons as event listeners

        FieldUtils.getAllFieldsList(Managers.class).stream()
                .filter(field -> Manager.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    try {
                        WynntilsMod.registerEventListener(field.get(null));
                    } catch (IllegalAccessException e) {
                        WynntilsMod.error("Internal error in Managers", e);
                        throw new RuntimeException(e);
                    }
                });
    }
}
