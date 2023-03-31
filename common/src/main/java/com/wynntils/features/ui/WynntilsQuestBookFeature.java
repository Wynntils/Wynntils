/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.questbook.WynntilsQuestBookScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class WynntilsQuestBookFeature extends Feature {
    private static final String QUEST_BOOK_NAME = "§dQuest Book";

    @RegisterKeyBind
    private final KeyBind openQuestBook = new KeyBind(
            "Open Quest Book",
            GLFW.GLFW_KEY_K,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsQuestBookScreen.create()));

    @RegisterKeyBind
    private final KeyBind openWynntilsMenu = new KeyBind(
            "Open Wynntils Menu",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()));

    @RegisterConfig
    public final Config<Boolean> replaceWynncraftQuestBook = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> questBookShouldOpenWynntilsMenu = new Config<>(false);

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftQuestBook.get()) return;

        tryCancelQuestBookOpen(event);
    }

    @SubscribeEvent
    public void onUseItemOn(PlayerInteractEvent.RightClickBlock event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftQuestBook.get()) return;

        tryCancelQuestBookOpen(event);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.Interact event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftQuestBook.get()) return;

        tryCancelQuestBookOpen(event);
    }

    private void tryCancelQuestBookOpen(Event event) {
        ItemStack itemInHand = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

        if (itemInHand != null
                && ComponentUtils.getCoded(itemInHand.getHoverName()).equals(QUEST_BOOK_NAME)) {
            event.setCanceled(true);
            WynntilsMenuScreenBase.openBook(
                    questBookShouldOpenWynntilsMenu.get()
                            ? WynntilsMenuScreen.create()
                            : WynntilsQuestBookScreen.create());
        }
    }
}
