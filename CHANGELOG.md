Core Component Configs
- A new button has been added to the settings screen to allow configuring previously always enabled Wynntils logic
- Available configs
  - Ability
    - Track Ability Cooldowns, whether the cooldowns of abilities should be tracked from the status effects list
  - Account
    - Query API Details, whether the Wynncraft API should be queried every 60 seconds for stats unobtainable in-game
    - Query Rank Info on Join, whether the character info & store menu should be queried on join to check for information such as class, profession levels and guild info?
  - Combat
    - Track Damage, whether damage dealt to mobs should be tracked or not
    - Track Debuffs, whether debuffs on nearby mobs should be tracked or not
    - Track Kills, whether mob kills should be tracked or not
  - Emerald
    - Count Emeralds, whether the amount of emeralds in your inventory be kept updated
    - Recount Interval, how often should the amount of emeralds in your inventory be recounted
  - Friends
    - Query Friends List, whether your friends list should be queried at certain intervals
  - Guild
    - Query Guild Diplomacy Menu, whether the guild diplomacy menu should be queried on join to check for information such as tributes from allies?
    - Query Guild Info Menu, whether the guild info menu should be queried on join to check for information such as guild level, objectices completed and allies?
    - Request Guild Members, whether the Wynncraft API should be queried for current guild members at certain intervals
  - Hades
    - Connect to Hades, whether Hades should be connected to
  - Party
    - Query Party Members, whether your party members should be queried at certain intervals
  - Ping
    - Calculate Ping, whether a ping packet should be sent every second to calculate ping
  - Raid
    - Track Raids, whether raid state should be tracked
  - Statistics
    - Collect Statistics, whether various gameplay stats should be tracked
  - Territory
    - Lookup API Info, whether the Wynncraft API should be queried for up-to-date territory info
  - Token
    - Track Token Gatekeepers, whether token gatekeepers and your token counts should be tracked
  - Update
    - Check For Updates, whether updates should be checked for on launch
  - War
    - Save Historic War Info, whether info about participated wars should be saved
- Changing these configs will affect how different features in Wynntils work, the affected features will be listed in the tooltip of the config button so you can see if it will affect a feature you use or not

Game Bar Overlays
- Removed Awakened bar
- Added Mantra bar
  - Show Mask Names config, adds the mask names next to their icon on the bar text, default disabled

Mount Jump Bar
- New feature, renders the jump bar when riding a horse or adasaur. Enabled by default for all profiles

Tooltip Fitting
- Tooltips on screens such as item sharing and guides are now resized to always fit

Functions
- Character Functions
  - `capped_awakened_progress` removed function
  - `is_mask_overload_capped` returns whether the given mask overload is capped
    - `maskType` required String argument, the mask type to check the overload status of
  - `mask_overload` returns the overload value of the given mask
    - `maskType` required String argument, the mask type to check the overload of
  - `mask_overload_decay` returns the decay progress of the current mask overload
- Inventory Functions
  - `item_count` unidentified gear now requires an "Unidentified " prefix
- Mount Functions
  - `mount_type` returns the name of the mount type you are currently riding (horse, wyvern or adasaur)

Corroded, Static and Weathering debuffs are now tracked

Fixes
- Fixed some cases where horse mounting was not detected
- Fixed trade market sell all button not differentiating material tiers
- Fixed trade metket sell all button not differentiating identified and unidentified gear
- Awakened mask type is no longer overriden when changing masks
- Wynntils keybinds that were held upon closing a screen are no longer triggered after closing
