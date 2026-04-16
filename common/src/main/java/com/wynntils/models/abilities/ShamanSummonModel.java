/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.abilities.label.ShamanPuppetInfo;
import com.wynntils.models.abilities.label.ShamanPuppetParser;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public class ShamanSummonModel extends Model {
    private static final Pattern HUMMINGBIRD_SENT_PATTERN =
            Pattern.compile("§e((\uE008\uE002)|\uE001) You sent your hummingbirds to attack!$");

    private static final Pattern HUMMINGBIRD_RETURN_PATTERN =
            Pattern.compile("§e((\uE008\uE002)|\uE001) Your hummingbirds have returned to you!$");

    private final Map<Integer, ShamanPuppetInfo> ActivePuppetsMap = new HashMap<>();

    public boolean hummingBirdsState = false;

    public ShamanSummonModel() {
        super(List.of());

        Handlers.Label.registerParser(new ShamanPuppetParser());
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        StyledText message = StyledTextUtils.unwrap(event.getMessage().stripAlignment());
        if (message.matches(HUMMINGBIRD_RETURN_PATTERN)) {
            hummingBirdsState = false;
        } else if (message.matches(HUMMINGBIRD_SENT_PATTERN)) {
            hummingBirdsState = true;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        hummingBirdsState = false;
    }

    @SubscribeEvent
    public void onPuppetIdentified(LabelIdentifiedEvent event) {
        if (!(event.getLabelInfo() instanceof ShamanPuppetInfo labelInfo)) return;
        if (!labelInfo.getPlayerName().equals(McUtils.playerName())) {
            return;
        }

        ActivePuppetsMap.put(labelInfo.getEntity().getId(), labelInfo);
    }

    @SubscribeEvent
    public void onPuppetRemoved(RemoveEntitiesEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.SHAMAN) return;
        event.getEntityIds().forEach(ActivePuppetsMap::remove);
    }

    public List<ShamanPuppetInfo> getActivePuppetsLabels() {
        return ActivePuppetsMap.values().stream().toList();
    }
}
