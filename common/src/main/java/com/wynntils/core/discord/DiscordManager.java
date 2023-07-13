/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.discord;

import com.wynntils.antiope.core.DiscordGameSDKCore;
import com.wynntils.antiope.core.type.CreateParams;
import com.wynntils.antiope.core.type.GameSDKException;
import com.wynntils.antiope.core.type.Result;
import com.wynntils.antiope.manager.activity.type.Activity;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.mc.event.TickEvent;
import java.time.Instant;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordManager extends Manager {
    private static final long DISCORD_APPLICATION_ID = 387266678607577088L;

    private CreateParams params;
    private DiscordGameSDKCore core;
    private Activity activity;

    public DiscordManager() {
        super(List.of());
    }

    public void load() {
        DiscordGameSDKCore.loadLibrary();
        if (!isReady()) {
            createCore();
        }
    }

    public boolean isReady() {
        return core != null && core.isOpen() && activity != null;
    }

    public void unload() {
        if (!isReady()) return;
        try {
            activity.close();
            activity = null;
            core.close();
            core = null;
            params.close();
            params = null;
        } catch (GameSDKException e) {
            if (e.getResult() == Result.TRANSACTION_ABORTED) {
                // This occurs when player closes game and JVM exits before we can close the core
                return;
            }
            WynntilsMod.error("Could not unload Discord Game SDK", e);
        }
    }

    private void createCore() {
        params = new CreateParams();
        try {
            params.setClientID(DISCORD_APPLICATION_ID);
            params.setFlags(CreateParams.getNoRequireDiscordFlags());
            core = new DiscordGameSDKCore(params);
            activity = new Activity();
            activity.timestamps().setStart(Instant.now());
        } catch (GameSDKException e) {
            if (e.getResult() == Result.INTERNAL_ERROR) {
                // Occurs when player launches game without Discord open
                return;
            }
            WynntilsMod.error("Could not initialize Discord Game SDK", e);
        } catch (Throwable e) {
            WynntilsMod.error("Could not initialize Discord Game SDK", e);
        }
    }

    public void setDetails(String details) {
        if (!isReady()) return;
        activity.setDetails(details);
        core.activityManager().updateActivity(activity);
    }

    public void setImage(String imageId) {
        if (!isReady()) return;
        activity.assets().setLargeImage(imageId);
        core.activityManager().updateActivity(activity);
    }

    public void setImageText(String text) {
        if (!isReady()) return;
        activity.assets().setLargeText(text);
        core.activityManager().updateActivity(activity);
    }

    public void setWynncraftLogo() {
        if (!isReady()) return;
        activity.assets().setLargeImage("wynn");
        activity.assets().setLargeText("play.wynncraft.com");
        core.activityManager().updateActivity(activity);
    }

    public void setState(String state) {
        if (!isReady()) return;
        activity.setState(state);
        core.activityManager().updateActivity(activity);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (isReady()) {
            core.runCallbacks();
        }
    }
}
