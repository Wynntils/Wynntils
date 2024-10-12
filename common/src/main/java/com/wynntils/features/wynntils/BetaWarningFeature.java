/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PlayerInfoEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class BetaWarningFeature extends Feature {
    private WarnType warnType = WarnType.NONE;

    @SubscribeEvent
    public void onConnect(WynncraftConnectionEvent.Connected event) {
        if (WynntilsMod.isDevelopmentBuild()) return;

        if (event.getHost().equals("beta") && !WynntilsMod.isBeta()) { // Joined beta, not on beta build
            warnType = WarnType.RELEASE;
        } else if (!event.getHost().equals("beta") && WynntilsMod.isBeta()) { // Joined normal, on beta build
            warnType = WarnType.BETA;
        } else {
            warnType = WarnType.NONE;
        }
    }

    // We use this event because WorldStateModel might break on beta
    @SubscribeEvent
    public void onDisplayNameChange(PlayerInfoEvent.PlayerDisplayNameChangeEvent e) {
        if (warnType == WarnType.NONE) return;

        McUtils.sendMessageToClient(warnType.getWarning());
        warnType = WarnType.NONE;
    }

    // As urls may not be initialised yet, we have to use a supplier so that it will be loaded when we need
    // to access the Component which will only be after joining the server and urls should be loaded by then.
    private enum WarnType {
        NONE(Component::empty),
        BETA(() -> Component.translatable("feature.wynntils.betaWarning.usingBetaOnNormalServer")),
        RELEASE(() -> Component.translatable(
                "feature.wynntils.betaWarning.usingReleaseOnBetaServer",
                Managers.Url.getUrl(UrlId.LINK_WYNNTILS_DISCORD_INVITE)));

        private final Supplier<Component> warningSupplier;

        WarnType(Supplier<Component> warningSupplier) {
            this.warningSupplier = warningSupplier;
        }

        public Component getWarning() {
            return warningSupplier.get();
        }
    }
}
