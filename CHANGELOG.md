All Map Screens
- Removed empty spaces from map button backgrounds
- Added zoom buttons and slider to bottom right

Auto Skip Cutscenes
- New config Send Notification, whether or not a notification should be sent when a cutscene was automatically skipped/voted to be skipped, default enabled

Coordinates Overlay
- Now replicates the vanilla coordinates
  - The previous overlay style can be recreated with functions
- Default position moved to where the vanilla coordinates are
- Should Display Original config is now disabled by default
- Colored config removed
- Replace Direction config, replaces the cardinal direction with your Y coordinate, default disabled
- Compass Style config, change how the compass at the end of the overlay looks, Static always points forwards, Animated rotates with your camera rotation, None removes the compass, default Static
- Compass End config, which end of the overlay should the compass be on, head or tail, default tail

Enhanced Streamer Mode
- Disabled by default in all profiles
- All configs only activate whilst in streamer mode
- Hide Hotbar Weapons config, replaces weapons in your hotbar with a letter signifying their type (Wand -> W, Dagger -> D etc), default disabled
- Hide Equipped Gear config, prevents equipped gear from rendering when viewing your inventory, default disabled
- Hide Gear Tooltips config, hovering gear in your inventory will not show the usual tooltip unless the space key is held, default disabled

Guild Map Screen
- Added treasury filter button
- Added button to change to main map keeping current position and zoom level
- Pressing open map keybind whilst on the guild map will keep current position and zoom level

Main Map Screen
- Added button to change to guild map keeping current position and zoom level
- Pressing open guild map keybind whilst on the main map will keep current position and zoom level

Minimap & Territory Overlays
- Moved default positions up slightly
- Changed default value for Hide in Unmapped Areas config for minimap from minimap and coordinates to just minimap

Reveal Nicknames
- New config Keep Own Nickname, will not reveal the nickname from messages containing your name, default disabled

Waypoint Management Screen
- Reworked icon filtering system to not use a separate screen
- Screen no longer scales to a fixed size across different GUI scales
- Added a new waypoint button to go the waypoint creation screen
- Added smooth scrolling

Functions
- Inventory Functions
  - `equipped_armor_name` returns the name of the given armor type as a String, if no armor found then "NONE" is returned
    - Required String argument `armor`, the armor type to retrieve such as "Helmet" or "Boots"

Fixes
- Fix not being able to click territories that are only visible after scrolling in the custom territory management screen
- Fix entries in custom player list overlay being cut off earlier than expected
- Wynntils capes will now render if you do not have a vanilla cape equipped
- Wynntils capes will now render in the correct position if chestplate is hidden by a Wynntils feature or the hide glint
