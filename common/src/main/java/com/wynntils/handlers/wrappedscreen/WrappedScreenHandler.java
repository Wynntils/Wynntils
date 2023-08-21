/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.wynntils.core.components.Handler;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WrappedScreenHandler extends Handler {
    private WrappedScreen currentWrappedScreen;

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent.Post event) {
        currentWrappedScreen = new WrappedScreen(McUtils.mc().screen, event.getContainerId());
        McUtils.mc().setScreen(currentWrappedScreen);
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        if (currentWrappedScreen == null) return;
        if (event.getScreen() != currentWrappedScreen) return;

        ContainerUtils.closeContainer(currentWrappedScreen.getContainerId());
        currentWrappedScreen = null;
    }
}
