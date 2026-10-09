Ability Cooldown Overlay
- Added support for Overcharge and Strides of Heresy
- Moved the redirect refreshed message config to the feature rather than the overlay
  - This way you can disable the overlay but keep the redirects
- New config Ignored Cooldowns, a comma separated list of cooldowns you do not want to see in the overlay

Hades & Player Viewer
- Made Player Viewer a sub-feature of Hades
- Renamed View Player keybind to Open Hades Interaction Wheel, moved from Player Viewer to Hades
- Using the keybind will now open a wheel to send pings or view player (if one is hovered). If you are not in a party with other Hades users then the player viewer will always open. The View Player button is always at the top of the interaction wheel.

Item Guess
- Made guesses of same level span multiple lines instead of always being on one line

Item Text Overlay
- Moved position of mount text and forced consistent size
- New config Estimate Potential, whether an estimated potential should be used rather than the rounded value, default enabled
- New config, Dynamic Potential Color, when potential is used as the text value, should the color change based on the potential or be static, default enabled

Player Ping
- New feature, displays ping markers from other Hades users in your current party, enabled for default and lite profiles
- There are 6 ping options, each have a unique icon, if that marker style is chosen, and a unique color.
  - Options are: Focus, Need Help, Wait Here, Attack Here, Look Here and Group Up
- Scrolling with the wheel open can choose a party members username to send with the ping
- Marker Style config, how the markers should be displayed. Floating icon or cube outline, default floating icon
- Marker Scale config, how big to render the markers, default 1
- Show Own Pings config, whether or not your own pings are displayed, default enabled
- Own Pings Duration config, how long your own pings should be displayed for in seconds, default 5
- Other Pings Duration config, how long pings from others should be displayed in seconds, default 5
- Ping Volume config, how loud should the ping noise be, default 1
- Show Chat Message config, whether a chat message should be displayed when a user sends a ping, default disabled
- Various style configs for the ping wheel

Ping Command
- New command to ignore pings from specific players
- `/ping ignore add <username>`
- `/ping ignore remove <username>`
- `/ping ignore clear`
- `/ping ignore list`

Functions
- World Functions
  - `closest_gathering_totem_number` returns the number of the nearest gathering totem
  - `gathering_totem_seconds_left` returns the time left on the specified gathering totem in seconds
    - `totemNumber` required Integer argument, the number of the gathering totem to get the remaining time of
  - `closest_mob_totem_number` returns the number of the nearest mob totem
  - `mob_totem_seconds_left` returns the time left on the specified mob totem in seconds
    - `totemNumber` required Integer argument, the number of the mob totem to get the remaining time of

Fixes
- Fixed trade market undercutting going negative
- Fixed 3rd person state being reset when another player joins your mount
- Fixed various items not behaving properly when favorited or trying to be favorited
- Fixed loot chests not being detected when recording lootrun paths
- Fixed cases where character ID was not reset after changing character
- Fixed the last harvest functions not working
