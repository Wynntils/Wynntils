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
import com.wynntils.models.quests.event.TrackedQuestUpdateEvent;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.screens.guides.emeraldpouch.WynntilsEmeraldPouchGuideScreen;
import com.wynntils.screens.guides.gear.WynntilsItemGuideScreen;
import com.wynntils.screens.guides.ingredient.WynntilsIngredientGuideScreen;
import com.wynntils.screens.guides.powder.WynntilsPowderGuideScreen;
import com.wynntils.screens.questbook.WynntilsQuestBookScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class WynntilsQuestBookFeature extends Feature {
    private static final ResourceLocation QUEST_UPDATE_ID = new ResourceLocation("wynntils:ui.quest.update");
    private static final SoundEvent QUEST_UPDATE_SOUND = SoundEvent.createVariableRangeEvent(QUEST_UPDATE_ID);

    private static final StyledText QUEST_BOOK_NAME = StyledText.fromString("§dQuest Book");

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
    public final Config<Boolean> replaceWynncraftQuestBook = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> questBookShouldOpenWynntilsMenu = new Config<>(false);

    @RegisterConfig
    public final Config<Boolean> playSoundOnUpdate = new Config<>(true);

    @SubscribeEvent
    public void onQuestUpdate(TrackedQuestUpdateEvent event) {
        if (event.getQuestInfo() == null) return;

        if (playSoundOnUpdate.get()) {
            McUtils.playSoundUI(QUEST_UPDATE_SOUND);
        }
    }

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
