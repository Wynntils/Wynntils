/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.wynntils.core.features.InternalFeature;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import com.wynntils.wc.model.Character;
import com.wynntils.wc.model.Character.CharacterInfo;
import com.wynntils.wc.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ActionBarParserFeature extends InternalFeature {

    private static final Pattern ACTIONBAR_PATTERN = Pattern.compile("§c❤ (\\d+)/(\\d+)§0\\s+(.+)\\s+§b✺ \\d+/\\d+");

    private String previousActionbar = null;

    // The server doesn't send SetActionBar packets, instead it sends ChatPackets with the ChatType GAME_INFO
    @SubscribeEvent
    public void onActionbarUpdate(PacketReceivedEvent<ClientboundChatPacket> e) {
        if (!WynnUtils.onWorld()) return;

        ClientboundChatPacket packet = e.getPacket();
        if (packet.getType() != ChatType.GAME_INFO) return;

        String actionbar = packet.getMessage().getString();
        if (actionbar.equals(previousActionbar)) return;
        previousActionbar = actionbar;

        Character character = WynnUtils.getCharacter();
        if (character == null) return;

        CharacterInfo info = character.getCharacterInfo();
        if (info == null) return;

        Matcher matcher = ACTIONBAR_PATTERN.matcher(actionbar);
        if (!matcher.matches()) return;

        int currentHealth = Integer.parseInt(matcher.group(1));
        int maxHealth = Integer.parseInt(matcher.group(2));
        String centerText = matcher.group(3);
        // TODO: parse partial spell progress from centerText

        info.setHealth(currentHealth);
        info.setMaxHealth(maxHealth);
    }
}
