package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.mixin.accessors.ClientboundBossEventPacketAccessor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Operation;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.AddOperation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbbreviateMobHealthFeature extends UserFeature {

    private static final Pattern MOB_HEALTH_PATTERN = Pattern.compile("(.*§c)(\\d+)(§4❤.*)");

    @Config
    public static int precision = 1;

    @SubscribeEvent
    public void onHealthBarEvent(BossHealthUpdateEvent event) {
        ClientboundBossEventPacket packet = event.getPacket();
        Operation operation = ((ClientboundBossEventPacketAccessor) packet).getOperation();

        if (!(operation instanceof AddOperation addOperation)) return;

        String name = addOperation.name.getString();
        Matcher healthMatcher = MOB_HEALTH_PATTERN.matcher(name);
        if (!healthMatcher.matches()) return;

        int rawHealth = Integer.parseInt(healthMatcher.group(2));
        String formattedHealth = Abbreviation.formatNumber(rawHealth);

        String formattedName = healthMatcher.replaceAll("$1" + formattedHealth + "$3");
        addOperation.name = new TextComponent(formattedName);
    }

    private enum Abbreviation {
        B(1_000_000_000),
        M(1_000_000),
        K(1_000);

        final float value;

        Abbreviation(float value) {
            this.value = value;
        }

        static String formatNumber(int num) {
            for (Abbreviation abbr : Abbreviation.values()) {
                if (num >= abbr.value) {
                    BigDecimal shortened = new BigDecimal(num/abbr.value).setScale(precision, RoundingMode.HALF_UP);
                    return shortened.toPlainString() + abbr.name();
                }
            }

            // no abbreviation necessary
            return Integer.toString(num);
        }
    }
}
