/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UNCATEGORIZED)
public class TerritoryDefenseMessageFeature extends Feature {
    private static final Pattern ATTACK_SCREEN_TITLE = Pattern.compile("Attacking: (.+)");
    private static final Pattern TERRITORY_DEFENSE_PATTERN = Pattern.compile("Territory Defences: (.+)");
    private static final String DEFENSE_MESSAGE = "g %s defense is %s";
    // 3 seconds for the server to respond to an attack command
    private static final long MESSAGE_TIMEOUT = 3000;
    private final Map<String, QueuedTerritory> queuedTerritories = new HashMap<>();

    @SubscribeEvent
    public void onInventoryClick(InventoryMouseClickedEvent event) {
        if (event.getHoveredSlot() == null || McUtils.mc().screen == null) return;
        Matcher titleMatcher =
                ATTACK_SCREEN_TITLE.matcher(McUtils.mc().screen.getTitle().getString());
        if (!titleMatcher.matches()) return;

        ItemStack itemStack = event.getHoveredSlot().getItem();

        for (Component tooltipLine : LoreUtils.getTooltipLines(itemStack)) {
            Matcher matcher = StyledText.fromComponent(tooltipLine)
                    .getMatcher(TERRITORY_DEFENSE_PATTERN, PartStyle.StyleType.NONE);
            if (matcher.matches()) {
                // intentionally not localized to match Wynncraft language
                queuedTerritories.put(
                        titleMatcher.group(1),
                        new QueuedTerritory(
                                System.currentTimeMillis(), GuildResourceValues.fromString(matcher.group(1))));
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatMessage(ChatMessageReceivedEvent e) {
        Matcher matcher =
                StyledText.fromComponent(e.getMessage()).getMatcher(GuildAttackTimerModel.WAR_MESSAGE_PATTERN);
        if (!matcher.matches()) return;

        // remove all expired messages
        queuedTerritories
                .entrySet()
                .removeIf(entry -> System.currentTimeMillis() - entry.getValue().timestamp() > MESSAGE_TIMEOUT);

        String territory = matcher.group(1);
        if (!queuedTerritories.containsKey(territory)) return;

        Handlers.Command.sendCommandImmediately(DEFENSE_MESSAGE.formatted(
                territory, queuedTerritories.get(territory).defense().getAsString()));
        queuedTerritories.remove(territory);
    }

    private record QueuedTerritory(long timestamp, GuildResourceValues defense) {}
}
