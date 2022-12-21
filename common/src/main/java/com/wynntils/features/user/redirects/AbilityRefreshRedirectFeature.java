/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.handlers.chat.events.ChatMessageReceivedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.REDIRECTS)
public class AbilityRefreshRedirectFeature extends UserFeature {
    private static final Pattern REFRESH_PATTERN = Pattern.compile("\\[⬤\\] (.+) has been refreshed!");

    @Override
    public List<Model> getModelDependencies() {
        return List.of();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        Matcher matcher = REFRESH_PATTERN.matcher(ComponentUtils.stripFormatting(event.getOriginalCodedMessage()));
        if (matcher.matches()) {
            event.setCanceled(true);

            NotificationManager.queueMessage(event.getOriginalMessage());
        }
    }
}
