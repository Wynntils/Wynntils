Auto Attack
- Reworked to hopefully fix issues
- Still disabled by default for now

Chat Items
- Reverted fix to make chat items not display stone when using certain mods and/or clients

Lootrun Overlays
- New overlay Lootrun Trials, displays the selected trials for the current lootrun
- Adjusted default vertical position of missions overlay slightly

Nametag Rendering, Wynntils Cosmetics
- Leaderboard Badges, Wynntils Capes, Markers & Roles will now be displayed on ghosts
  - For Wynntils related information/cosmetics, ghosts may not immediately display the information/cosmetics

Overlays
- All overlays now have an Enabled Template option to customise when they should be rendered using functions
  - If no valid template is given, overlays will render if you are on a world and are not riding a Display entity (used in situations where you see your own player). Certain overlays override this and use other conditions such as custom player list or lootrun overlays

Functions
- Font Functions
  - `to_fancy_text` & `to_background_text` if the given character is not available in the font, then it will be rendered using the normal font
- Lootrun Functions
  - `lootrun_trial` returns the name of the lootrun trial at the given index or "Unknown" if not yet selected
    - `index` required number argument, the index of which trial to get (starts at index 0)

Fixes
- Loot chest locations are collected again
- Opening loot chests with left click will now work for getting location
- Added missing negative space to `to_background_text` function when using pill style for the right edge
- Fix bank quick jumps not jumping under certain conditions
- Lootrun rerolls will now be handled if the option to sacrifice is available
- Fixed chat items being locked to a certain display type
- Possibly fixed issue with Hades failing to connect causing the game to hang
