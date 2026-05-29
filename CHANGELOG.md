Bulk Buy
- Bulk Buy now supports the Seavale treasure merchant

Item Compare
- Removed Remove Flavour config
- Removed Remove Set Info config

Mounts
- New overlay Mount Energy, renders the mount energy bar as an overlay

Nametag Rendering
- 10th place on leaderboards are now given bronze badges

Overlays
- Text Overlays (Made with functions) now have 2 new configs to render a color background behind the text
- Background Color, the color to render the background
- Background Border Width, the extra border width for the background

Remove Shiny Glint
- Removed feature, use WynnIris for glint shader support

Waypoint Distance
- New config Distance Opacity, the opacity level to render waypoint with, default 1.0

Functions
- Character Functions
  - `remnant_count` returns the amount of your currently active Remnants
  - `patchwork_abomination_duration` returns the time left on your Patchwork Abomination
- Lootrun Functions
  - `lootrun_current_mission` returns the Current Lootrun Mission
    - `colored` required Boolean argument, whether or not the output should return colored
  - `lootrun_current_mission_objective` returns the current Lootrun Mission objective at the given index
    - `index` required Integer argument, the index of the objective
  - `lootrun_current_mission_progress` returns the progress of the current Lootrun Mission objective at the given index
    - `index` required Integer argument, the index of the objective
  - `lootrun_current_trial` returns the Current Lootrun Trial
  - `lootrun_current_trial_objective` returns the current Lootrun Trial objective
    - `index` required Integer argument, the index of the objective
  - `lootrun_current_trial_progress` returns the progress of the current Lootrun Trial objective
    - `index` required Integer argument, the index of the objective
- Minecraft Functions
  - The `dir` function now accepts an optional Boolean argument `wrap`, which will wrap the direction to a standard -180 to 180 degree range, default false
  - `location_at_crosshair` returns the location of the block you are currently looking at
    - `distance` required Double argument, the maximum distance to check for a block
    - `colliderOnly` required Boolean argument, only check for collider blocks (ignore non-solid blocks such as Tall Grass, Signs, etc)
  - `pitch` returns your current X-rotation
- Social Functions
  - `scoreboard_party_members` or `sb_party_members` returns the number of party members parsed from scoreboard
- Spell Functions
  - `shaman_totem_transfused_amount` returns the amount of Transfused on the shaman totem
    - `totemNumber` required Integer argument, the number of the totem to get the Transfused amount for
  - `shaman_totem_poison_amount` returns the amount of poison on the shaman totem
    - `totemNumber` required Integer amount, the number of the totem to get the poison amount for
- StyledText Functions
  - `repeat_styled_text` or `repeat_st` repeats the given StyledText the given number of times
    - `value` required StyledText argument, the StyledText to repeat
    - `count` required Integer argument, the amount of times to repeat the StyledText

Hypoxia and Unstable debuffs can now be tracked

Fixes
- Fixed cases of content book searching not matching query
- Fixed trade market bulk sell not detecting items
- Skill point loadouts now remove he unicode characters from item names
  - Any previously saved loadouts with these characters will not have them removed. Save them again them to fix
- Fixed item compare not working
- Fix bulk buy not working
- Fixed failed spell cast message not disappearing
- Fixed quest requirements and world event fast travel text being added to custom content book description
- Fixed completed caves not being displayed in custom content book
