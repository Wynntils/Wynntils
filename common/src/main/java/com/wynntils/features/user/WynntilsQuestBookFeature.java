/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.gui.screens.WynntilsMenuScreen;
import com.wynntils.gui.screens.WynntilsQuestBookScreen;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class WynntilsQuestBookFeature extends UserFeature {
    private static final String QUEST_BOOK_NAME = "§dQuest Book";

    @RegisterKeyBind
    private final KeyBind openQuestBook = new KeyBind(
            "Open Quest Book", GLFW.GLFW_KEY_K, true, () -> McUtils.mc().setScreen(WynntilsQuestBookScreen.create()));

    @RegisterKeyBind
    private final KeyBind openWynntilsMenu =
            new KeyBind("Open Wynntils Menu", GLFW.GLFW_KEY_UNKNOWN, true, () -> McUtils.mc()
                    .setScreen(WynntilsMenuScreen.create()));

    @Config
    public boolean replaceWynncraftQuestBook = true;

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftQuestBook) return;

        tryCancelQuestBookOpen(event);
    }

    @SubscribeEvent
    public void onUseItemOn(PlayerInteractEvent.RightClickBlock event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftQuestBook) return;

        tryCancelQuestBookOpen(event);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.Interact event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftQuestBook) return;

        tryCancelQuestBookOpen(event);
    }

    private static void tryCancelQuestBookOpen(Event event) {
        ItemStack itemInHand = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

        if (itemInHand != null
                && ComponentUtils.getCoded(itemInHand.getHoverName()).equals(QUEST_BOOK_NAME)) {
            event.setCanceled(true);
            McUtils.mc().setScreen(WynntilsQuestBookScreen.create());
        }
    }
}
