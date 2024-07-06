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
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
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
    private static final Pattern TERRITORY_ATTACK_MESSAGE_PATTERN =
            Pattern.compile("§3\\[WAR]§c ?The war for (.+) will start in \\d+ minutes?\\.");
    private static final long MESSAGE_TIMEOUT = 3000; // 3 seconds for the server to respond to an attack command
    private final Map<String, Pair<Long, String>> territoryMessages = new HashMap<>();

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
                territoryMessages.put(
                        titleMatcher.group(1),
                        Pair.of(
                                System.currentTimeMillis(),
                                "g %s defense is %s".formatted(titleMatcher.group(1), matcher.group(1))));
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatMessage(ChatMessageReceivedEvent e) {
        Matcher matcher = StyledText.fromComponent(e.getMessage()).getMatcher(TERRITORY_ATTACK_MESSAGE_PATTERN);
        if (!matcher.matches()) return;

        // remove all expired messages
        territoryMessages
                .entrySet()
                .removeIf(entry -> System.currentTimeMillis() - entry.getValue().a() > MESSAGE_TIMEOUT);

        String territory = matcher.group(1);
        if (!territoryMessages.containsKey(territory)) return;

        Pair<Long, String> attackInfo = territoryMessages.get(territory);
        Handlers.Command.sendCommandImmediately(attackInfo.b());
    }
}
