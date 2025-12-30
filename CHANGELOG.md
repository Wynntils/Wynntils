Add Command Expansion
- As command arguments are now provided by Wynncraft, this feature will now only suggest command root nodes
    - This means that commands like `/emote` will now only suggest emotes you own, or `/party` will only suggest arguments based on your current party state

Config Profiles
- Added 5 config profiles to change the default enabled state of features
- Existing users will recieve a one time toast on server join to inform them of the profiles
- Users playing Wynncraft for the first time with Wynntils will be taken to the config profile UI upon reaching the character creation screen
  - The "New Player" profile will be focused by default in this case
- Profiles do not modify user configs, they are only used for features that users have not manually enabled/disabled

Gear Sharing
- Added per character sharing settings
- By default, global settings are used but by toggling the checkbox on the sharing UI, character specific settings can be set instead

Guides
- Added set bonus guide
- Displays the various item sets available in Wynncraft with their bonuses and eligible items.
- Note that some bonuses may be innacurate, most were verified but not all so report any mistakes and they can be updated

Held Item Name Overlay
- Renders the text when changing held item as an overlay
  - Display Original Name config, whther the original text should be displayed or not, default disabled
  - Message Display Time config, how long in ticks should the message be displayed for, default 40
  - Text Shadow config for what shadow type to render with, default Normal
  - Font Scale config for how big/small to render the text, default 1.0

Settings Screen
- Added new "Enabled State" filter tag next to the "All" tag
  - Filters the feature list to only show features that are enabled/disabled based on the current filter state
  - Defaults to enabled only
- Added new "Profiles" tag to open the config profile UI

Config Command
- Added `profile` argument to open the config profile UI
  - The profile name can also be added as another argument to change profile without the UI

Fixes
- Add Command Expansion, Command Aliases and Filter Admin Commands will now properly enable/disable without the need to leave and rejoin the server
- Fixed cases where the scoreboard would not remove lines
- Fixed some ingredients in the guide not using the correct texture
- Fixed crash when using ctrl+1-9 to access invalid chat tabs
- Fixed certain helmets not having a texture in the item guide
- Checkmark is now displayed on owned cosmetics when previewing crates with custom item highlights enabled
