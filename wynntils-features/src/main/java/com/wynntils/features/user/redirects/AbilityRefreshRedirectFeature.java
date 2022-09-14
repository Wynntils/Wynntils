/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.REDIRECTS)
public class AbilityRefreshRedirectFeature extends UserFeature {
    private static final Pattern REFRESH_PATTERN =
            Pattern.compile("§8\\[§r§7⬤§r§8\\] §r§7(.+)§r§8 has been refreshed!");

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ChatModel.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ChatMessageReceivedEvent event) {
        Matcher matcher = REFRESH_PATTERN.matcher(event.getCodedMessage());
        if (matcher.matches()) {
            event.setCanceled(true);

            NotificationManager.queueMessage(event.getMessage());
        }
    }
}
