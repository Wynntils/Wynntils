package com.wynntils.features.user;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.utils.WynnUtils;
import java.text.DecimalFormat;


@FeatureInfo
public class CombatXPGainMessageFeature extends UserFeature {

    @Config
    public boolean getCombatXPGainMessages = true;

    private static final DecimalFormat percentFormat = new DecimalFormat("##.##'%'");
    private static float newTickXP = 0;
    private static float lastTickXP = 0;

    @SubscribeEvent
    public void onTick (ClientTickEvent.End event) {
        if(!WynnUtils.onWorld() || !getCombatXPGainMessages) { return; }
        CharacterManager.CharacterInfo data = WynnUtils.getCharacterInfo();
    
        newTickXP = data.getCurrentXp();
        WynntilsMod.info("NEW TICK XP IS " + newTickXP); //FIXME DEBUG

        if (newTickXP == lastTickXP) { return; }

        int gainedXP = Math.round(newTickXP) - Math.round(lastTickXP);
        int neededXP = data.getXpPointsNeededToLevelUp();
        float percentGained = (float) (gainedXP / neededXP) * 100;

        percentFormat.format(percentGained);

        String message = String.format("§a+%d XP (§b%s§a)", gainedXP, percentGained);

        NotificationManager.queueMessage(message);

        lastTickXP = newTickXP;
        WynntilsMod.info("LAST TICK XP IS " + lastTickXP); //FIXME DEBUG
    }
}
