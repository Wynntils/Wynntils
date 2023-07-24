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
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.screens.activities.WynntilsCaveScreen;
import com.wynntils.screens.activities.WynntilsDiscoveriesScreen;
import com.wynntils.screens.activities.WynntilsQuestBookScreen;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.screens.guides.emeraldpouch.WynntilsEmeraldPouchGuideScreen;
import com.wynntils.screens.guides.gear.WynntilsItemGuideScreen;
import com.wynntils.screens.guides.ingredient.WynntilsIngredientGuideScreen;
import com.wynntils.screens.guides.powder.WynntilsPowderGuideScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class WynntilsContentBookFeature extends Feature {
    private static final StyledText CONTENT_BOOK_NAME = StyledText.fromString("§dContent Book");
    private static final int TUTORIAL_HIGHLIGHT_SLOT = 8;

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

    @RegisterKeyBind
    private final KeyBind openPowderGuide = new KeyBind(
            "Open Powder Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsPowderGuideScreen.create()));

    @RegisterKeyBind
    private final KeyBind openItemGuide = new KeyBind(
            "Open Item Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsItemGuideScreen.create()));

    @RegisterKeyBind
    private final KeyBind openIngredientGuide = new KeyBind(
            "Open Ingredient Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsIngredientGuideScreen.create()));

    @RegisterKeyBind
    private final KeyBind openEmeraldPouchGuide = new KeyBind(
            "Open Emerald Pouch Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsEmeraldPouchGuideScreen.create()));

    @RegisterKeyBind
    private final KeyBind openGuidesList = new KeyBind(
            "Open Guides List",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsGuidesListScreen.create()));

    @RegisterConfig
    public final Config<Boolean> replaceWynncraftContentBook = new Config<>(true);

    @RegisterConfig
    public final Config<InitialPage> initialPage = new Config<>(InitialPage.USER_PROFILE);

    @RegisterConfig
    public final Config<Boolean> showContentBookLoadingUpdates = new Config<>(true);

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftContentBook.get()) return;

        tryCancelQuestBookOpen(event);
    }

    @SubscribeEvent
    public void onUseItemOn(PlayerInteractEvent.RightClickBlock event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftContentBook.get()) return;

        tryCancelQuestBookOpen(event);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.Interact event) {
        if (McUtils.player().isShiftKeyDown() || !replaceWynncraftContentBook.get()) return;

        tryCancelQuestBookOpen(event);
    }

    private void tryCancelQuestBookOpen(Event event) {
        // Tutorial safeguard, don't replace the content book if the player hasn't completed the tutorial
        if (McUtils.inventory().getItem(TUTORIAL_HIGHLIGHT_SLOT).getItem() == Items.GOLDEN_AXE) return;

        ItemStack itemInHand = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);

        if (itemInHand != null
                && StyledText.fromComponent(itemInHand.getHoverName()).equals(CONTENT_BOOK_NAME)) {
            event.setCanceled(true);
            WynntilsMenuScreenBase.openBook(
                    switch (initialPage.get()) {
                        case USER_PROFILE -> WynntilsMenuScreen.create();
                        case QUEST_BOOK -> WynntilsQuestBookScreen.create();
                        case DISCOVERIES -> WynntilsDiscoveriesScreen.create();
                        case CAVES -> WynntilsCaveScreen.create();
                    });
        }
    }

    private enum InitialPage {
        USER_PROFILE,
        QUEST_BOOK,
        DISCOVERIES,
        CAVES
    }
}
