/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.event.WrappedScreenOpenEvent;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.models.containers.containers.GuildManagementContainer;
import com.wynntils.screens.guildlog.GuildLogScreen;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.type.ShiftBehavior;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomGuildLogScreenFeature extends Feature {
    private static final Pattern GUILD_LOG_ITEM_PATTERN = Pattern.compile("§7§lGuild Log");

    @Persisted
    private final Config<ShiftBehavior> shiftBehaviorConfig = new Config<>(ShiftBehavior.DISABLED_IF_SHIFT_HELD);

    private boolean shiftClickedLogItem = false;

    public CustomGuildLogScreenFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onWrappedScreenOpen(WrappedScreenOpenEvent event) {
        if (event.getWrappedScreenClass() != GuildLogScreen.class) return;

        boolean shouldOpen = false;

        switch (shiftBehaviorConfig.get()) {
            case NONE -> {
                shouldOpen = true;
            }
            case ENABLED_IF_SHIFT_HELD -> {
                if (shiftClickedLogItem) {
                    shouldOpen = true;
                }
            }
            case DISABLED_IF_SHIFT_HELD -> {
                if (!shiftClickedLogItem) {
                    shouldOpen = true;
                }
            }
        }

        if (shouldOpen) {
            event.setOpenScreen(true);
        }
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent event) {
        if (!(Models.Container.getCurrentContainer() instanceof GuildManagementContainer)) return;
        if (!StyledText.fromComponent(event.getItemStack().getHoverName()).matches(GUILD_LOG_ITEM_PATTERN)) return;

        shiftClickedLogItem = KeyboardUtils.isShiftDown();
    }
}
