Command Expansion
- Added `explode` and `faint` parameters to `/emote`

Container Scroll & Container Search Highlight
- Crate preview containers are now scrollable and searchable

Guides & Item Guess
- Updated obtain methods of gear and ingredients, almost every item should now display a method of obtaining
- Filtered item guess to exclude more items that are not possibilities
- This information uses a hybrid of the API and information from the wiki, it may not be fully accurate so report any errors and we can fix them

Hades
- The Share Info with Guild config now functions

Info Boxes
- New config Color Template, the default color to use for text if not overridden by the content function

Item Statistics Info
- New config Item Weights, the source to use when displaying item weights on items, options are None, Wynnpool, Nori or All, default None
- Option to set custom weight sources coming in a future release

Major ID Range Visualizer
- Renamed feature to Range Visualizer
- New config Show gambit ranges, whether or not the ranges for the Farsighted's and Myopic's gambits should be displayed when those gambits have been selected should be rendered
- New config Show major ID ranges, whether or not the ranges for your or other player's major ID's should be rendered
- Increased Guardian range to 12
- Increased Altruism range to 16
- Ranges of Major ID's that affect nearby players will be rendered if the gear with that Major ID is shared

Map & Minimap
- New configs Render Remote Guild Player, whether guild members connected to Hades and on your current world should be rendered

Nametag Rendering & Player Viewer
- Viewing players gear is once again possible via Hades
  - This means it will only be shared with mutual friends, party members and guild members who are also connected to Hades and running a supported version
- Supports held item, armour and accessories
- Supports normal, crafted and shiny items
- All sharing is disabled by default, edit what is shared via the "Edit Shared Gear" button visible when viewing another player's gear or by running `/wynntils gearsharing`
  - Sharing options include per gear type toggles, sharing crafted gear and sharing crafted item names
- New config for Nametag Rendering, Show Gear Percentage for whether the overall percentage of items should be shown next to the gear name, default enabled

Party Members Overlay
- New config Show Heads, whether or not the party members head should be displayed, default enabled

Unidentified Item Icon
- Updated the textures of the Wynn option
- New texture option Legacy, this is what the Wynn option was prior to this change

Valuable Found
- New config Corrupted Cache Found Sound, sound to play upon getting a corrupted cache, default Cache
  - Sound options: Modern, Classic, Cache or None
- New config Show Cache Dry Streak Message, whether or not a message should be sent upon getting a corrupted cache, default enabled
- Cache sound option is available to the other sound configs, lootrun, loot chest, aspect, tome and emerald pouch

Valuables Protection
- Now supports the item identifier augments screen

Wynncraft Button
- New config Return to title screen after disconnect, whether or not disconnecting from the server should send you to the title screen instead of the server screen after disconnecting if you joined via the button, default enabled

Functions
- World Event Functions
  - `annihilation_dry_count`, `dry_annis` or `dry_anni_count` returns an Integer of the number of Annihilation world events completed without getting a corrupted cache
  - `current_world_event` returns a String of the name of the world event you are currently in the radius of
  - `current_world_event_start_time` returns a Time of the starting time for the world event you are currently in the radius of
  - `world_event_start_time` returns a Time of the starting time for the given world event
    - `worldEventName` required String argument, the name of the world event to get the start time for, case sensitive.
    - These times are currently collected from chat or when you get near a world event

Statistics
- Annihilations completed, number of Annihilation world events completed
- Annihilations failed, number of Annihilation world events failed
- Corrupted Caches Found, number of Corrupted Caches found

Fixes
- Fixed various issues with the custom loading screen to make it much smoother
- Fixed chat tabs causing chat to not appear on character selection screen prior to first join
- Item lock icon will no longer render on the party finder match found screen
