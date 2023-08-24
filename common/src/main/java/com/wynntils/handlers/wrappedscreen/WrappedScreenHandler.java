/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.wrappedscreens.trademarket.TradeMarketSearchResultParent;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WrappedScreenHandler extends Handler {
    private final Set<WrappedScreenParent> wrappedScreenParents = new HashSet<>();

    private WrappedScreenParent<?> currentWrappedScreenParent;
    private WrappedScreen currentWrappedScreen;

    public WrappedScreenHandler() {
        registerWrappedScreens();
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent.Post event) {
        if (McUtils.mc().screen == null) return;

        StyledText titleStyledText =
                StyledText.fromComponent(McUtils.mc().screen.getTitle());

        // If we opened a new screen, reset the current wrapped screen
        resetWrappedScreen();

        for (WrappedScreenParent parent : wrappedScreenParents) {
            if (!titleStyledText.matches(parent.getReplacedScreenTitlePattern())) continue;

            currentWrappedScreenParent = parent;
            WynntilsMod.registerEventListener(parent);

            currentWrappedScreen =
                    parent.createWrappedScreen(McUtils.mc().screen, McUtils.containerMenu(), event.getContainerId());

            parent.setWrappedScreen(currentWrappedScreen);

            McUtils.mc().setScreen(currentWrappedScreen);
            return;
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        resetWrappedScreen();
    }

    public <T extends WrappedScreen> WrappedScreenParent<T> getParent(
            Class<T> screenClass, Class<? extends WrappedScreenParent<?>> parentClass) {
        if (currentWrappedScreenParent == null) {
            WynntilsMod.error("No current wrapped screen parent!");
            return null;
        }

        Class<T> parentScreenClass = (Class<T>)
                (((ParameterizedType) currentWrappedScreenParent.getClass().getGenericSuperclass()))
                        .getActualTypeArguments()[0];

        if (parentScreenClass != screenClass) {
            WynntilsMod.error("Caller screen class " + screenClass + " does not match current parent screen class "
                    + parentScreenClass + "!");
            return null;
        }

        if (parentClass != currentWrappedScreenParent.getClass()) {
            WynntilsMod.error("Required parent class " + parentClass + " does not match current parent class "
                    + currentWrappedScreenParent.getClass() + "!");
            return null;
        }

        return (WrappedScreenParent<T>) currentWrappedScreenParent;
    }

    private void resetWrappedScreen() {
        if (currentWrappedScreen == null) return;

        ContainerUtils.closeContainer(currentWrappedScreen.getContainerId());
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
