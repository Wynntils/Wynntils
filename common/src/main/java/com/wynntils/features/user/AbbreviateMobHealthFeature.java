/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.mixin.accessors.ClientboundBossEventPacketAccessor;
import com.wynntils.utils.StringUtils;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.AddOperation;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Operation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AbbreviateMobHealthFeature extends UserFeature {

    private static final Pattern MOB_HEALTH_PATTERN = Pattern.compile("(.*§c)(\\d+)(§4❤.*)");

    @SubscribeEvent
    public void onHealthBarEvent(BossHealthUpdateEvent event) {
        ClientboundBossEventPacket packet = event.getPacket();
        Operation operation = ((ClientboundBossEventPacketAccessor) packet).getOperation();

        if (!(operation instanceof AddOperation addOperation)) return;

        String name = addOperation.name.getString();
        Matcher healthMatcher = MOB_HEALTH_PATTERN.matcher(name);
        if (!healthMatcher.matches()) return;

        int rawHealth = Integer.parseInt(healthMatcher.group(2));
        String formattedHealth = StringUtils.integerToShortString(rawHealth).toUpperCase(Locale.ROOT);

        String formattedName = healthMatcher.replaceAll("$1" + formattedHealth + "$3");
        addOperation.name = new TextComponent(formattedName);
    }
}
