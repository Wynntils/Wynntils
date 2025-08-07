2.1.3 Support
- This update adds support for the new Wynncraft 2.1.3 update

Bomb Bell Overlay
- New config Show Loot Chest Bombs for if loot chest bombs should be displayed on the overlay, default enabled

Bomb Bell Relay
- Removed Clickable Message config, default messages are now clickable

Bomb Command
- Renamed to BombBell Command
- Removed `/bomb` and `/bombs` aliases

Command Expansions
- Added `/disguises` with `/disguise` alias
- Added `/effects` with `/effect` alias
- Added `/hats` with `/hat` alias
- Added `/mounts` with `/mount` alias
- Added `/weapons` with `/weapon` alias
- Added `/consumables` with `/bomb`, `/bombs`, `/token`, `/tokens` and `/consumable` aliases
- Added `/rank`, `/shop` and `/store` aliases to `/use`
- Removed `/silverbull`
- Removed `/discord`
- Added `HERO+` parameter to `/changetag`
- Removed `attacksound` and `rpwarning` parameters from `/toggle`

Cosmetics Player Preview
- Removed feature, use the wardrobe

Custom Lootrun Beacons, Lootrun Beacon Count Overlay, Lootrun Task Name Overlay and Lootrun Functions
- Added support for Crimson beacons

Guides
- Changed emerald pouch guides to show tier as raw number instead of Roman numeral
- Changed highlight texture for items

Guild Rank Replacement
- Removed feature, hasn't worked since 2.1 and not been requested to be fixed

Highlight Duplicate Cosmetics
- Removed feature, cosmetics are now stacked in scrap menu

Item Favorites, Mythic Blocker
- Item and ingredient bomb reward containers are now blockable

Item Highlights
- Renamed feature to Custom Item Highlights
- Added a new texture option Tag, set as default
  - This is the default texture used by Wynncraft
- Changed default value of Hotbar Opacity from 0.5 to 1
- Hotbar highlight now renders the selected texture instead of a coloured square
- New config Selected Item Highlight to render a different texture on your selected item (if it is highlightable), default enabled
- Removes vanilla highlight
  - Vanilla highlight is removed when the item is added to the current container or the feature is enabled. When disabling the feature a `/class` is recommended to re-enable the vanilla highlight

Item Text Overlay
- Set emerald pouch, gathering tool and horse tier Roman numeral configs to disabled by default

Lootrun Missions Overlay
- Added support for Interest Scheme, Optimism and Thrill Seeker

Mob Totem Tracking
- Renamed feature to Bonus Totem Tracking
- New overlay Gathering Totem Timer
  - Same as existing Mob Totem Timer overlay but for gathering totems

NPC Dialogue
- Disabled feature by default, causes issues with other features and is not yet fixed with this release

Overlays
- Most overlays are now hidden whilst in the wardrobe, overlays with a custom enabled template can overwrite this

Wynncraft Button
- Server Type will now be prioritised over Server Region Override if not set to `GAME`

Functions
- Character Functions
  - `status_effect_modifier` returns a NameValue for the modifier and its value for the given status effect
    - `query` required string argument, the query to get the status modifier for
- Color Functions
  - `gradient_shader` now has an optional `style` number argument, defaults to 1
    - Currently there are 2 styles. 1 goes from `#f56217ff` to `#0b486bff`, 2 goes from `#560505ff` to `#8a0303ff`
  - `shine_shader` returns an animated gradient from `#a3cc52ff` to `#ffffd2ff`
- World Functions
  - `gathering_totem_count` returns the number of gathering totems around you
  - `gathering_totem_owner` returns the owner of the given gathering totem as a string
    - `totemNumber` required number argument, the number of the gathering totem to get the owner of
  - `gathering_totem_distance` returns the distance to the given gathering totem as a decimal
    - `totemNumber` required number argument, the number of the gathering totem to get the distance to
  - `gathering_totem` returns the location of the given gathering totem
    - `totemNumber` required number argument, the number of the gathering totem to get the location of
  - `gathering_totem_time_left` returns the time left on the given gathering totem as a string
    - `totemNumber` required number argument, the number of the gathering totem to get the time left of

Fixes
- Fixed skill point item counts
- Fixed lootrun and content beacon tracking
- Vanilla powder special bar is no longer always hidden
- Fixed powder special overlay
- Fixed mythic box scaler mythic box detection
- Fixed Silverbull subscription status detection
- Fixed store remaining open after login
- Shift right clicking world events will no longer open the world event on the wiki on the Custom Content Book
- Fixed emerald pouch, gear box, ingredient, potion, ingredient pouch and other item stats not being detected
- Bombs thrown on current server will be detected again
- Fixed parsing active bombs on current world from info boss bar
- Fixed damage detection
- Fixed element symbol rendering in various places
- Buying boats on the Custom Seaskipper Screen will no longer crash the screen
- Fixed private message, shouts and pet message detection
- Fixed esc to cancel on trade market price input
- Fixed trade market price conversion
- Fixed trade market price match buttons
- Fixed mob totem tracking
- Updated high roller reroll count to 1
- Fixed bulk buy
- Fixed bomb bell parsing
