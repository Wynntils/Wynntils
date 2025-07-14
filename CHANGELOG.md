Custom Content Book
- New config Open Wynntils Menu Instead will open the Wynntils menu instead of the content book on interaction, default disabled

Custom Universal Bars, Ability bars & other bar overlays
- Added missing Experience B texture

Durability Arc
- Renamed feature to Durability Overlay
- New config Durability Render Mode, whether to render durability as an arc or a bar like vanilla Minecraft, default arc

Status Overlay
- New config Ignored Effects, enter a list of comma separated status effects to hide from the overlay

Functions
- Character Functions
  - `status_effect_duration` returns a NamedValue for the status effect name and duration
    - `query` required string argument, the status effect to search for
- Font Functions
  - `to_background_text` converts the given text into the background style font
    - `text` required string argument, the text to convert
    - `textColor` required color argument, the color to use for the text
    - `backgroundColor` required color argument, the color to use for the background
    - `leftEdge` required string argument, what edge style to use for the left either "None" or "Pill"
    - `rightEdge` required string argument, what edge style to use for the right either "None" or "Pill"
  - `to_fancy_text` converts the given text into the fancy style font
    - `text` required string argument, the text to convert
- Lootrun Functions
  - `lootrun_sacrifices` returns the number of sacrifices in current lootrun
  - `lootrun_rerolls` returns the number of rerolls in current lootrun
    - Does not account for free daily reroll

Improvements were made to the character info checks ran when joining a world, they will no longer run if entering a world without changing character

Fixes
- Disabling "Hide if not charged" on powder special bar overlay will now work
- If an overlay crashes it will no longer cause other rendering events to fail
- Fixes progress bar rendering on update screen
- Fix opening content on map in custom content book breaking the open content book keybind
- Fix error spamming log when tracking content with scoreboard disabled
- Fix chat being cleared when config is reloaded and chat tabs are disabled
- Fix overlay selection screen crashing on rare occasions
- Fix combat XP gain tracking (XP gain functions, XP gain messages)
  - Due to Wynncraft changes these numbers are no longer as accurate as they once were
- Improved lootrun mission tracking
- Fixed issue causing you to not meet raid requirements occasionally
