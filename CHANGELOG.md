Updated to Minecraft 1.21.11

(Fabric only) Updated required Fabric version to 0.18.4

(NeoForge only) Updated required NeoForge version to 21.11.23-beta

Categorised keybinds by their respective features category

All Map Screens
- Removed empty spaces from map button backgrounds
- Added zoom buttons and slider to bottom right

Auto Apply Resource Pack
- Will no longer function if not using OpenGL (e.g. Vulkan)

Auto Skip Cutscenes
- New config Send Notification, whether or not a notification should be sent when a cutscene was automatically skipped/voted to be skipped, default enabled

Bomb Bell Command
- No bombs available message now uses the chat ranks font to display the rank

Chat Item
- Changed default Share Item keybind to F5

Coordinates Overlay
- Now replicates the vanilla coordinates
  - The previous overlay style can be recreated with functions
- Default position moved to where the vanilla coordinates are
- Should Display Original config is now disabled by default
- Colored config removed
- Replace Direction config, replaces the cardinal direction with your Y coordinate, default disabled
- Compass Style config, change how the compass at the end of the overlay looks, Static always points forwards, Animated rotates with your camera rotation to always point North, None removes the compass, default Static
- Compass End config, which end of the overlay should the compass be on, head or tail, default tail

Custom Item Highlights
- Added new highlight texture option Wynn, changed default to this

Enhanced Streamer Mode
- Disabled by default in all profiles
- All configs only activate whilst in streamer mode
- Hide Hotbar Weapons config, replaces weapons in your hotbar with a letter signifying their type (Wand -> W, Dagger -> D etc), default disabled
- Hide Equipped Gear config, prevents equipped gear from rendering when viewing your inventory, default disabled
- Hide Gear Tooltips config, hovering gear in your inventory will not show the usual tooltip unless the space key is held, default disabled

Extended Item Count
- New config Hold Ctrl to Show Level, allows disabling the functionality to see item levels when holding ctrl, default enabled

Guides
- Tomes and Charms can now be opened on the web item guide
- Added Augments guide
- Added Runes guide
- Item textures can now be updated even if the Wynncraft API is outdated

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
- Temporarily removed circle map mask type
  - Will be reimplemented as soon as possible

Overlays
- New button on overlay management screen to customize render order.
- Customize the layering of all overlays,
- Customize layering around vanilla GUI elements

Personal Storage Utilities
- Bank page names and jump buttons now use the Wynncraft language font

Reveal Nicknames
- New config Keep Own Nickname, will not reveal the nickname from messages containing your name, default disabled

Waypoint Management Screen
- Reworked icon filtering system to not use a separate screen
- Screen no longer scales to a fixed size across different GUI scales
- Added a new waypoint button to go the waypoint creation screen
- Added smooth scrolling

Functions
- Combat Functions
  - `debuffs_in_radius_value` returns the total value of the given debuff in the given radius around you
    - `radius` required double argument, the radius around you to check for debuffs
    - `debuffName` required string argument, the name of the debuff to get the value for
  - `targeted_mob_debuff_value` returns the value of the given debuff for the targeted mob
    - `range` required double argument, the range to check for targeted mob
    - `horizontalDegrees` required douuble argument, horizontal field of view in degrees to check for targeted mob
    - `verticalDegrees` required douuble argument, vertical field of view in degrees to check for targeted mob
    - `debuffName` required string argument, the name of the debuff to get the value for

Available debuff types for functions are: Bleeding, Blindness, Burning, Confused, Contaminated, Discombobulated, Enkindled, Freezing, Marked, Poison, Provoked, Resistance, Slowness, Trick, Weakness, Whipped and Winded

Fixes
- Fixed various rendering issues
- Fixed item screenshots being off-center on NeoForge
- Fixed crashing with VulkanMod
- Fix entries in custom player list overlay being cut off earlier than expected
- Wynntils capes will now render in the correct position if chestplate is hidden by a Wynntils feature or the hide glint
- Negative stats on shared crafted items are no longer decayed
- Trying to copy item screenshots to clipboard when using Wayland will now handle errors gracefully
