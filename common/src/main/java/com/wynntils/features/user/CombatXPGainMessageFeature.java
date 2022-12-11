/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CombatXPGainMessageFeature extends UserFeature {

    @Config
    public int tickDelay = 20;

    private static long tickCounter = 0;

    private static float newTickXP = 0;
    private static float lastTickXP = 0;
    private static int trackedPercentage = 0;

    @SubscribeEvent
    public void onTick(ClientTickEvent.End event) {
        if (!WynnUtils.onWorld()) return;

        tickCounter++;

        if (tickCounter % tickDelay != 0) return;

        tickCounter = 0;

        CharacterManager.CharacterInfo data = WynnUtils.getCharacterInfo();

        newTickXP = data.getCurrentXp();

        if (newTickXP == lastTickXP) return;

        int neededXP = data.getXpPointsNeededToLevelUp();
        if (lastTickXP != 0) {
            trackedPercentage = (int) lastTickXP / neededXP;
        } else {
            trackedPercentage = (int) newTickXP / neededXP;
        }

        int gainedXP = Math.round(newTickXP) - Math.round(lastTickXP);

        float percentGained = (float) gainedXP / neededXP;
        // to keep the math as striaght as possible, we only multiply by 100
        // at the very end of all percentage calculations.
        float percentChange = (percentGained - trackedPercentage) * 100;

        String message = String.format("§2+%d XP (§6%.2f%%§2)", gainedXP, percentChange);

        lastTickXP = newTickXP;

        NotificationManager.queueMessage(message);
    }
}
