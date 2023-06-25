package com.wynntils.core.discord;


import com.wynntils.antiope.core.DiscordGameSDKCore;
import com.wynntils.antiope.core.type.CreateParams;
import com.wynntils.antiope.manager.activity.type.Activity;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.mc.event.TickAlwaysEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class DiscordManager extends Manager {

    private DiscordGameSDKCore core;
    private Activity activity;

    public DiscordManager() {
        super(List.of());

        DiscordGameSDKCore.loadLibrary();
        CreateParams params = new CreateParams();
        try {
            params.setClientID(387266678607577088L);
            params.setFlags(CreateParams.getDefaultFlags());
            core = new DiscordGameSDKCore(params);
            activity = new Activity();
            activity.timestamps().setStart(Instant.now());
        } catch (RuntimeException e) {
            WynntilsMod.error("Could not initialize Discord Game SDK", e);
        }
    }

    public void setLocation(String location) {
        activity.setDetails(location);
        core.activityManager().updateActivity(activity);
    }

    public void setCharacterInfo(String name, int level, ClassType classType) {
        activity.assets().setLargeImage(classType.getActualName(false).toLowerCase(Locale.ROOT));
        activity.assets().setLargeText(name + " - Level " + level + " " + classType.getName());
        core.activityManager().updateActivity(activity);
    }

    public void setWynncraftLogo() {
        activity.assets().setLargeImage("wynn");
        activity.assets().setLargeText("play.wynncraft.com");
        core.activityManager().updateActivity(activity);
    }

    public void setWorld(String world) {
        activity.setState(world);
        core.activityManager().updateActivity(activity);
    }

    public void clearAll() {
        core.activityManager().clearActivity();
    }

    @SubscribeEvent
    public void onTick(TickAlwaysEvent event) {
        // TickAlwaysEvent is used otherwise we can't clear the activity when the player disconnects
        core.runCallbacks();
    }
}
