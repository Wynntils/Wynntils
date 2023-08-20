/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class PerClassGuildContributionFeature extends Feature {
    private static final Pattern CONTRIBUTION_PATTERN = Pattern.compile("guild xp (\\d+).*");

    @Persisted
    private final HiddenConfig<Map<String, String>> classContributions = new HiddenConfig<>(new TreeMap<>());

    @SubscribeEvent
    public void onCharacterChange(CharacterUpdateEvent e) {
        if (Models.Guild.getGuildName().isEmpty()) return;

        String amountToContribute = classContributions.get().getOrDefault(Models.Character.getId(), null);

        if (amountToContribute != null) {
            McUtils.sendCommand("guild xp " + amountToContribute);
        }
    }

    @SubscribeEvent
    public void onCommandSent(CommandSentEvent event) {
        if (Models.Guild.getGuildName().isEmpty()) return;

        String characterId = Models.Character.getId();

        if (characterId.equals("-")) return;

        String command = event.getCommand();

        Matcher contributionMatcher = CONTRIBUTION_PATTERN.matcher(command);

        if (contributionMatcher.matches()) {
            if (Integer.parseInt(contributionMatcher.group(1)) < 0
                    || Integer.parseInt(contributionMatcher.group(1)) > 100) return;

            classContributions.get().put(Models.Character.getId(), contributionMatcher.group(1));

            classContributions.touched();
        }
    }
}
