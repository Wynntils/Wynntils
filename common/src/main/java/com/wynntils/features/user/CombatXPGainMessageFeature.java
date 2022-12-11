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
    private static float startTickXP = 0;
    private static float endTickXP = 0;

    @SubscribeEvent
    public void onTickStart (ClientTickEvent.Start event) {
        if(!getCombatXPGainMessages) { return; }
        CharacterManager.CharacterInfo data = WynnUtils.getCharacterInfo();

        startTickXP = data.getCurrentXp();

        WynntilsMod.info("START TICK XP IS " + startTickXP); //FIXME DEBUG
    }

    @SubscribeEvent
    public void onTickEnd (ClientTickEvent.End event) {
        if(!getCombatXPGainMessages) { return; }
        CharacterManager.CharacterInfo data = WynnUtils.getCharacterInfo();
    
        endTickXP = data.getCurrentXp();
        WynntilsMod.info("END TICK XP IS " + endTickXP); //FIXME DEBUG

        if (endTickXP == startTickXP) { return; }

        int gainedXP = Math.round(endTickXP) - Math.round(startTickXP);
        int neededXP = data.getXpPointsNeededToLevelUp();
        float percentGained = (float) (gainedXP / neededXP) * 100;

        percentFormat.format(percentGained);

        String message = String.format("§a+%d XP (§b%s§a)", gainedXP, percentGained);

        NotificationManager.queueMessage(message);
    }
}
