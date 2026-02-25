/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Managers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public record KeyBindDefinition(
        String id,
        String name,
        KeyMapping.Category category,
        InputConstants.Type type,
        int defaultKey,
        boolean firstPress) {
    private static final List<KeyBindDefinition> DEFINITIONS = new ArrayList<>();

    public static List<KeyBindDefinition> definitions() {
        return Collections.unmodifiableList(DEFINITIONS);
    }

    public KeyBind create(Runnable onPress) {
        return Managers.KeyBind.createKeyBind(this, onPress, null);
    }

    public KeyBind create(Consumer<Slot> inventoryPress) {
        return Managers.KeyBind.createKeyBind(this, null, inventoryPress);
    }

    public KeyBind create(Runnable onPress, Consumer<Slot> inventoryPress) {
        return Managers.KeyBind.createKeyBind(this, onPress, inventoryPress);
    }

    // region Chat
    public static final KeyBindDefinition BOMB_RELAY_PARTY = register(
            "relayBombToParty",
            "Relay Bomb to Party",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition BOMB_RELAY_GUILD = register(
            "relayBombToGuild",
            "Relay Bomb to Guild",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition SHARE_ITEM = register(
            "shareItem",
            "Share Item",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F5,
            true);

    public static final KeyBindDefinition SAVE_ITEM_TO_RECORD = register(
            "saveItemToRecord",
            "Save Item to Item Record",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            true);

    public static final KeyBindDefinition OPEN_ITEM_RECORD = register(
            "openItemRecord",
            "Open Item Record",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Combat
    public static final KeyBindDefinition MOUNT_HORSE = register(
            "mountHorse",
            "Mount Horse",
            Managers.KeyBind.COMBAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            true);

    public static final KeyBindDefinition CAST_FIRST_SPELL = register(
            "castFirstSpell",
            "Cast 1st Spell",
            Managers.KeyBind.COMBAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            false);

    public static final KeyBindDefinition CAST_SECOND_SPELL = register(
            "castSecondSpell",
            "Cast 2nd Spell",
            Managers.KeyBind.COMBAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            false);

    public static final KeyBindDefinition CAST_THIRD_SPELL = register(
            "castThirdSpell",
            "Cast 3rd Spell",
            Managers.KeyBind.COMBAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            false);

    public static final KeyBindDefinition CAST_FOURTH_SPELL = register(
            "castFourthSpell",
            "Cast 4th Spell",
            Managers.KeyBind.COMBAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            false);
    // endregion

    // region Commands
    public static final KeyBindDefinition CUSTOM_COMMAND_ONE = register(
            "customCommandOne",
            "Execute 1st Custom Command Keybind",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_TWO = register(
            "customCommandTwo",
            "Execute 2nd Custom Command Keybind",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_THREE = register(
            "customCommandThree",
            "Execute 3rd Custom Command Keybind",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_FOUR = register(
            "customCommandFour",
            "Execute 4th Custom Command Keybind",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_FIVE = register(
            "customCommandFive",
            "Execute 5th Custom Command Keybind",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_SIX = register(
            "customCommandSix",
            "Execute 6th Custom Command Keybind",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Debug
    public static final KeyBindDefinition DUMP_CONTENT_BOOK = register(
            "dumpContentBook",
            "Dump Content Book",
            Managers.KeyBind.DEBUG_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition LOG_ITEM_INFO = register(
            "logItemInfo",
            "Log Item Info",
            Managers.KeyBind.DEBUG_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Inventory
    public static final KeyBindDefinition OPEN_EMERALD_POUCH = register(
            "openEmeraldPouch",
            "Open Emerald Pouch",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_GUILD_BANK = register(
            "openGuildBank",
            "Open Guild Bank",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            true);

    public static final KeyBindDefinition OPEN_INGREDIENT_POUCH = register(
            "openIngredientPouch",
            "Open Ingredient Pouch",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition TOGGLE_FAVORITE = register(
            "toggleFavorite",
            "Favorite/Unfavorite Item",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition LOCK_SLOT = register(
            "lockSlot",
            "Lock Slot",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            true);

    public static final KeyBindDefinition SCREENSHOT_ITEM = register(
            "screenshotItem",
            "Screenshot Item",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            true);
    // endregion

    // region Map
    public static final KeyBindDefinition OPEN_GUILD_MAP = register(
            "openGuildMap",
            "Open Guild Map",
            Managers.KeyBind.MAP_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            true);

    public static final KeyBindDefinition OPEN_MAIN_MAP = register(
            "openMainMap",
            "Open Main Map",
            Managers.KeyBind.MAP_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            true);

    public static final KeyBindDefinition NEW_WAYPOINT = register(
            "newWaypoint",
            "New Waypoint",
            Managers.KeyBind.MAP_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            true);

    public static final KeyBindDefinition MINIMAP_ZOOM_IN = register(
            "minimapZoomIn",
            "Increase Minimap Zoom",
            Managers.KeyBind.MAP_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL,
            false);

    public static final KeyBindDefinition MINIMAP_ZOOM_OUT = register(
            "minimapZoomOut",
            "Decrease Minimap Zoom",
            Managers.KeyBind.MAP_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_MINUS,
            false);
    // endregion

    // region Overlays
    public static final KeyBindDefinition TOGGLE_STOPWATCH = register(
            "toggleStopwatch",
            "Toggle Stopwatch",
            Managers.KeyBind.OVERLAYS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_0,
            true);

    public static final KeyBindDefinition RESET_STOPWATCH = register(
            "resetStopwatch",
            "Reset Stopwatch",
            Managers.KeyBind.OVERLAYS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_DECIMAL,
            true);
    // endregion

    // region Players
    public static final KeyBindDefinition OPEN_PARTY_MANAGEMENT = register(
            "openPartyManagement",
            "Open Party Management Screen",
            Managers.KeyBind.PLAYERS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            true);

    public static final KeyBindDefinition VIEW_PLAYER = register(
            "viewPlayer",
            "View player's gear",
            Managers.KeyBind.PLAYERS_CATEGORY,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            true);
    // endregion

    // region Tooltips
    public static final KeyBindDefinition HOLD_TO_COMPARE = register(
            "holdToCompare",
            "Hold to compare",
            Managers.KeyBind.TOOLTIPS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ENTER,
            false);

    public static final KeyBindDefinition SELECT_FOR_COMPARING = register(
            "selectForComparing",
            "Select for comparing",
            Managers.KeyBind.TOOLTIPS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            true);
    // endregion

    // region Trade Market
    public static final KeyBindDefinition QUICK_SEARCH_TRADE_MARKET = register(
            "quickSearchTradeMarket",
            "Quick Search TM",
            Managers.KeyBind.TRADEMARKET_CATEGORY,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            true);
    // endregion

    // region UI
    public static final KeyBindDefinition OPEN_TERRITORY_MENU = register(
            "openTerritoryMenu",
            "Open Territory Menu",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            true);

    public static final KeyBindDefinition OPEN_CONTENT_BOOK = register(
            "openContentBook",
            "Open Quest Book",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            true);

    public static final KeyBindDefinition OPEN_WYNNTILS_MENU = register(
            "openWynntilsMenu",
            "Open Wynntils Menu",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            true);

    public static final KeyBindDefinition OPEN_OVERLAY_MENU = register(
            "openOverlayMenu",
            "Open Overlay Menu",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_OVERLAY_FREE_MOVE = register(
            "openOverlayFreeMove",
            "Open Overlay Free Move",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_POWDER_GUIDE = register(
            "openPowerGuide",
            "Open Powder Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_ITEM_GUIDE = register(
            "openItemGuide",
            "Open Item Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_INGREDIENT_GUIDE = register(
            "openIngredientGuide",
            "Open Ingredient Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_CHARM_GUIDE = register(
            "openCharmGuide",
            "Open Charm Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_TOME_GUIDE = register(
            "openTomeGuide",
            "Open Tome Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_ASPECT_GUIDE = register(
            "openAspectGuide",
            "Open Aspect Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_EMERALD_GUIDE = register(
            "openEmeraldGuide",
            "Open Emerald Pouch Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_MISC_GUIDE = register(
            "openMiscGuide",
            "Open Misc Guide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_GUIDES_LIST = register(
            "openGuidesList",
            "Open Guides List",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Utilities
    public static final KeyBindDefinition TOGGLE_GAMMABRIGHT = register(
            "gammabright",
            "Gammabright",
            Managers.KeyBind.UTILITIES_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            true);

    public static final KeyBindDefinition TOGGLE_SILENCER = register(
            "toggleSilencer",
            "Toggle Silencer",
            Managers.KeyBind.UTILITIES_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    private static KeyBindDefinition register(
            String id,
            String name,
            KeyMapping.Category category,
            InputConstants.Type type,
            int defaultKey,
            boolean firstPress) {
        KeyBindDefinition definition = new KeyBindDefinition(id, name, category, type, defaultKey, firstPress);
        DEFINITIONS.add(definition);
        return definition;
    }
}
