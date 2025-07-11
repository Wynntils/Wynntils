/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.wrappedscreen.event.WrappedScreenOpenEvent;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.screens.activities.WynntilsContentBookScreen;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.screens.guides.aspect.WynntilsAspectGuideScreen;
import com.wynntils.screens.guides.charm.WynntilsCharmGuideScreen;
import com.wynntils.screens.guides.emeraldpouch.WynntilsEmeraldPouchGuideScreen;
import com.wynntils.screens.guides.gear.WynntilsItemGuideScreen;
import com.wynntils.screens.guides.ingredient.WynntilsIngredientGuideScreen;
import com.wynntils.screens.guides.powder.WynntilsPowderGuideScreen;
import com.wynntils.screens.guides.tome.WynntilsTomeGuideScreen;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.screens.overlays.selection.OverlaySelectionScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ShiftBehavior;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UI)
public class WynntilsContentBookFeature extends Feature {
    private static final StyledText CONTENT_BOOK_NAME = StyledText.fromString("§dContent Book");

    @RegisterKeyBind
    private final KeyBind openQuestBook = new KeyBind(
            "Open Quest Book",
            GLFW.GLFW_KEY_K,
            true,
            () -> ContainerUtils.openInventory(InventoryUtils.CONTENT_BOOK_SLOT_NUM));

    @RegisterKeyBind
    private final KeyBind openWynntilsMenu = new KeyBind(
            "Open Wynntils Menu",
            GLFW.GLFW_KEY_I,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()));

    @RegisterKeyBind
    private final KeyBind openOverlayMenu =
            new KeyBind("Open Overlay Menu", GLFW.GLFW_KEY_UNKNOWN, true, () -> McUtils.mc()
                    .setScreen(OverlaySelectionScreen.create()));

    @RegisterKeyBind
    private final KeyBind openOverlayFreeMove =
            new KeyBind("Open Overlay Free Move", GLFW.GLFW_KEY_UNKNOWN, true, () -> McUtils.mc()
                    .setScreen(OverlayManagementScreen.create(null)));

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
    private final KeyBind openCharmGuide = new KeyBind(
            "Open Charm Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsCharmGuideScreen.create()));

    @RegisterKeyBind
    private final KeyBind openTomeGuide = new KeyBind(
            "Open Tome Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsTomeGuideScreen.create()));

    @RegisterKeyBind
    private final KeyBind openAspectGuide = new KeyBind(
            "Open Aspect Guide",
            GLFW.GLFW_KEY_UNKNOWN,
            true,
            () -> WynntilsMenuScreenBase.openBook(WynntilsAspectGuideScreen.create()));

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

    @Persisted
    private final Config<ShiftBehavior> shiftBehaviorConfig = new Config<>(ShiftBehavior.DISABLED_IF_SHIFT_HELD);

    @Persisted
    private final Config<Boolean> openWynntilsMenuInstead = new Config<>(false);

    @Persisted
    public final Config<Boolean> displayOverallProgress = new Config<>(true);

    private boolean shiftClickedBookItem = false;

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        handleClick(event);
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        handleClick(event);
    }

    @SubscribeEvent
    public void onUseItemOn(PlayerInteractEvent.RightClickBlock event) {
        handleClick(event);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.Interact event) {
        handleClick(event);
    }

    @SubscribeEvent
    public void onWrappedScreenOpen(WrappedScreenOpenEvent event) {
        if (event.getWrappedScreenClass() != WynntilsContentBookScreen.class) return;

        boolean shouldOpen = false;

        switch (shiftBehaviorConfig.get()) {
            case NONE -> {
                shouldOpen = true;
            }
            case ENABLED_IF_SHIFT_HELD -> {
                if (shiftClickedBookItem) {
                    shouldOpen = true;
                }
            }
            case DISABLED_IF_SHIFT_HELD -> {
                if (!shiftClickedBookItem) {
                    shouldOpen = true;
                }
            }
        }

        if (shouldOpen) {
            event.setOpenScreen(true);
            shiftClickedBookItem = false;
        }
    }

    private void handleClick(ICancellableEvent cancellableEvent) {
        shiftClickedBookItem = McUtils.player().isShiftKeyDown();

        ItemStack itemInHand = McUtils.player().getItemInHand(InteractionHand.MAIN_HAND);
        if (openWynntilsMenuInstead.get()
                && StyledText.fromComponent(itemInHand.getHoverName()).equals(CONTENT_BOOK_NAME)) {
            cancellableEvent.setCanceled(true);
            WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create());
        }
    }
}
