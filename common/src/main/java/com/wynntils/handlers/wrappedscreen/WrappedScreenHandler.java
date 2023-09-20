/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.event.WrappedScreenOpenEvent;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.wrappedscreens.trademarket.TradeMarketSearchResultParent;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WrappedScreenHandler extends Handler {
    private final Set<WrappedScreenParent> wrappedScreenParents = new HashSet<>();

    private WrappedScreenParent<?> currentWrappedScreenParent;
    private Screen currentWrappedScreen;

    public WrappedScreenHandler() {
        registerWrappedScreens();
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent.Post event) {
        if (!(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)) return;

        StyledText titleStyledText = StyledText.fromComponent(abstractContainerScreen.getTitle());

        // If we opened a new screen, reset the current wrapped screen
        resetWrappedScreen(false);

        for (WrappedScreenParent parent : wrappedScreenParents) {
            if (!titleStyledText.matches(parent.getReplacedScreenTitlePattern())) continue;

            // Post an event to allow consumers to allow opening the wrapped screen
            WrappedScreenOpenEvent openEvent = new WrappedScreenOpenEvent(parent.getWrappedScreenClass());
            WynntilsMod.postEvent(openEvent);

            // If the event was not accepted, don't open the screen
            if (!openEvent.shouldOpenScreen()) return;

            // Set up and open the wrapped screen
            currentWrappedScreenParent = parent;
            WynntilsMod.registerEventListener(parent);

            currentWrappedScreen = parent.createWrappedScreen(new WrappedScreenInfo(
                    abstractContainerScreen, McUtils.containerMenu(), McUtils.containerMenu().containerId));

            parent.setWrappedScreen(currentWrappedScreen);
            McUtils.mc().setScreen(currentWrappedScreen);

            return;
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        resetWrappedScreen(true);
    }

    private void resetWrappedScreen(boolean closeContainer) {
        if (currentWrappedScreen == null) return;

        // If the container was overridden, we don't need to close it
        if (closeContainer) {
            ContainerUtils.closeContainer(((WrappedScreen) currentWrappedScreen)
                    .getWrappedScreenInfo()
                    .containerId());
        }

        currentWrappedScreen = null;

        currentWrappedScreenParent.reset();
        WynntilsMod.unregisterEventListener(currentWrappedScreenParent);
        currentWrappedScreenParent = null;
    }

    private void registerWrappedScreens() {
        registerWrappedScreen(new TradeMarketSearchResultParent());
    }

    private void registerWrappedScreen(WrappedScreenParent parent) {
        wrappedScreenParents.add(parent);
    }
}
