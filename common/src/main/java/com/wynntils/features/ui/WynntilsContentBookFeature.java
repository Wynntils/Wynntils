/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.handlers.wrappedscreen.event.WrappedScreenOpenEvent;
import com.wynntils.models.activities.event.ContentBookOpenEvent;
import com.wynntils.models.activities.event.ContentBookOpenEvent.OpenAction;
import com.wynntils.screens.activities.WynntilsContentBookScreen;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.screens.overlays.selection.OverlaySettingsScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ShiftBehavior;
import com.wynntils.utils.wynn.ContainerUtils;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class WynntilsContentBookFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind openContentBook = KeyBindDefinition.OPEN_CONTENT_BOOK.create(ContainerUtils::openContentBook);

    @RegisterKeyBind
    private final KeyBind openWynntilsMenu = KeyBindDefinition.OPEN_WYNNTILS_MENU.create(
            () -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()));

    @RegisterKeyBind
    private final KeyBind openOverlayMenu =
            KeyBindDefinition.OPEN_OVERLAY_MENU.create(() -> McUtils.setScreen(OverlaySettingsScreen.create()));

    @RegisterKeyBind
    private final KeyBind openOverlayFreeMove = KeyBindDefinition.OPEN_OVERLAY_FREE_MOVE.create(
            () -> McUtils.setScreen(OverlayManagementScreen.create(null)));

    @RegisterKeyBind
    private final KeyBind openGuidesList = KeyBindDefinition.OPEN_GUIDES_LIST.create(
            () -> WynntilsMenuScreenBase.openBook(WynntilsGuideScreen.create(null)));

    @Persisted
    private final Config<ShiftBehavior> shiftBehaviorConfig = new Config<>(ShiftBehavior.DISABLED_IF_SHIFT_HELD);

    @Persisted
    private final Config<Boolean> openWynntilsMenuInstead = new Config<>(false);

    @Persisted
    public final Config<Boolean> displayOverallProgress = new Config<>(true);

    private boolean shiftClickedBookItem = false;

    public WynntilsContentBookFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onContentBookOpen(ContentBookOpenEvent event) {
        if (event.isCanceled()) return;
        shiftClickedBookItem = event.getAction().isShift();

        if (event.getAction() == OpenAction.PROGRAMMATIC
                || event.getAction() == OpenAction.INVENTORY_CLICK
                || event.getAction() == OpenAction.SHIFT_INVENTORY_CLICK) return;

        if (openWynntilsMenuInstead.get()) {
            event.setCanceled(true);
            if (!(McUtils.screen() instanceof WynntilsMenuScreen)) {
                WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create());
            }
        }
    }

    @SubscribeEvent
    public void onWrappedScreenOpen(WrappedScreenOpenEvent event) {
        if (event.getWrappedScreenClass() != WynntilsContentBookScreen.class) return;
        if (Models.WorldState.inCharacterWardrobe()) return;

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
}
