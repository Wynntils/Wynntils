(Fabric only) Bumped ModMenu version from 17.0.0-beta.1 to 17.0.1-beta.1

Auto Progress Dialogue
- New feature, disabled by default
- Automatically progresses through dialogue that requires no user choice
- Auto Progress Toggle config, whether the functionality is enabled, can be toggled by the Toggle Auto Progress keybind, default unbound
- Skip Directly config, press shift directly after the base delay without waiting for dialogue text to finish. If the first press reveals the full text instead of advancing, it will press shift again after the base delay, default false
- Show Skip Time config, should the auto progress notification show the time remaining before dialogue is skipped, default true
- Base Delay ms config, how long should auto progress wait before pressing shift once dialogue can continue. When Skip Directly is enabled, this delay starts as soon as dialogue appears and is used before each skip press, default 500
- Delay per Word ms config, how much extra time should auto progress wait for each dialogue word, default 50

Build Loadout Screen
- Fully reworked the screen
- Search for loadouts
- Mark loadouts as favorites
- Filter by loadout type
- Rename loadouts
- View selected abilities
- View equipped aspects
- View equipped tomes
- View equipped gear in build loadout

Character Info Indicator
- New feature, enabled for Default, Lite and Minimal profile
- Adds a button to the character info menu to force trigger an update of character info, ability tree, equipped aspects (It runs /wynntils rescan)

Container Search
- New mount related filters
  - `mountEnergy` Current energy, Integer
  - `mountName` Mount name, String
  - `mountPotential` Mount potential, Integer
  - `mountPrimaryColor` Mount primary color, String
  - `mountSecondaryColor` Mount secondary color, String
  - `mountType` Mount type, String
  - Mount stat filter, Integer. One for each stat `mountJumpHeight`, `mountSpeed` etc
- Added `attackSpeed` Base Attack Speed, String
- Added `averageDps` Average DPS, Integer

Emote Wheel
- New feature, enabled for the Default profile
- Opens a wheel with favourite emotes to quickly run the /emote command
- Set favourite emotes by opening the config screen through Wynntils settings screen
- Number of Buttons config, how many to display on the wheel, default 8
- Show Numbers config, show the number hotkeys for each emote on the wheel, default enabled
- Scale config, how big to render the wheel, default 1
- Button Style config, what style to make the buttons look like, default button
- Text Color config, what color to render the text with, default white
- Text Color Hovered config, what color to render the text with when hovered, default white
- Text Shadow config, what text shadow style to render the text with, default outline

Guides
- Fully reworked guide screens, scrollable list instead of pages, many more items can be viewed at once
- One screen with types listed on left side
- Separated misc guide items into their own types
- Added Material and Gathering Tool guides
- Updated tooltips of ingredients, wards, runes and set guide entries to be Items 2.0 styled
  - Gear, tomes and charms will have their tooltips redone soon including adding back Major ID
- Minor styling changes to aspect, powder, dungeon key and emerald tooltips
- Added a clear favorites button
- Simple to use widgets for all relevant filters

Mounts
- Removed summon delay ticks config
- New config Mount Choice, which mount in your hotbar should the keybind choose to mount. Default first, other options are the 3 mount types

Wynntils Content Book
- Removed all specific guide keybinds

Wynntils Command
- `rescan` will now also update ability tree and equipped aspects

Functions
- Character Functions
  - `is_ability_unlocked`/`has_ability` returns whether or not the given ability is unlocked in the current ability tree (requires tree to have been scanned passively or via rescan command)
    - `abilityName` required String argument, the name of the ability to check for, case insensitive
- Mount Functions
  - Removed `mount_stat`/`mnt_stat` and `mount_stat_max`/`mnt_stat_max` use the capped function to get this data instead
  - Added a `mountType` parameter to all other mount functions for which mount to get the data from, options are "first" or the 3 mount types.
  - `capped_mount_stat`/`cap_mnt_stat` no longer accepts potential
  - `mount_potential`/`mnt_potential` returns the potential of the given mount as an Integer
    - `mountType` required String argument, which mount to get the potential of

Reduced jar size by ~2.8MB

Fixes
- Fixed ingredients missing not touching modifier in guide
- Fixed perfect roll chance calculation for inverted stats
- Fixed quests not opening on the wiki in custom content book
- Fixed auto 3rd person only working for first mount in hotbar
- Hopefully fixed all desync issues with mount keybind where server and client have different held items
- Fixed aspect tier and equipped aspect functions
- Fixed creeper mask and mama zomble missing texture in guides
- Fixed amplifiers on guides saying they come from lootruns instead of raids
