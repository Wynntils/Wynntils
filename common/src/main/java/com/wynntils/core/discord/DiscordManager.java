/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.discord;

import com.wynntils.antiope.core.DiscordGameSDKCore;
import com.wynntils.antiope.core.type.CreateParams;
import com.wynntils.antiope.manager.activity.type.Activity;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.utils.mc.McUtils;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscordManager extends Manager {
    private static final long DISCORD_APPLICATION_ID = 387266678607577088L;

    private CreateParams params;
    private DiscordGameSDKCore core;
    private Activity activity;

    private int level = 0;
    private ClassType classType = null;

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
        activity.close();
        activity = null;
        core.close();
        core = null;
        params.close();
        params = null;
    }

    private void createCore() {
        params = new CreateParams();
        try {
            params.setClientID(DISCORD_APPLICATION_ID);
            params.setFlags(CreateParams.getDefaultFlags());
            core = new DiscordGameSDKCore(params);
            activity = new Activity();
            activity.timestamps().setStart(Instant.now());
        } catch (Throwable e) {
            WynntilsMod.error("Could not initialize Discord Game SDK", e);
        }
    }

    public void setLocation(String location) {
        if (!isReady()) return;
        activity.setDetails(location);
        core.activityManager().updateActivity(activity);
    }

    private void updateCharacterInfo() {
        if (!isReady()) return;
        String name = StyledText.fromComponent(McUtils.player().getName()).getString(PartStyle.StyleType.NONE);
        if (classType == null) {
            setWynncraftLogo();
        } else {
            activity.assets().setLargeImage(classType.getActualName(false).toLowerCase(Locale.ROOT));
            activity.assets().setLargeText(name + " - Level " + level + " " + classType.getName());
        }
        core.activityManager().updateActivity(activity);
    }

    public void setLevel(int level) {
        this.level = level;
        updateCharacterInfo();
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
        updateCharacterInfo();
    }

    public void setWynncraftLogo() {
        if (!isReady()) return;
        activity.assets().setLargeImage("wynn");
        activity.assets().setLargeText("play.wynncraft.com");
        core.activityManager().updateActivity(activity);
    }

    public void setWorld(String world) {
        if (!isReady()) return;
        activity.setState(world);
        core.activityManager().updateActivity(activity);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (isReady()) {
            core.runCallbacks();
        }
    }
}
