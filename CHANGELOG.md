Config file is now global, your settings will persist between different Minecraft accounts, the first account you login with will be migrated to the global config if a config for that account exists.

Ability Cooldown Overlay
- New overlay, enabled for Default and Lite profiles
- Displays the cooldowns of abilities with a square radial cooldown and unique icon per ability
- Remove Status Effect config, removes the status effect for the cooldown from the status effects overlay, default enabled
  - This will not affect functions that read the status effects list
- Redirect Refreshed Messages config, how to handle the refreshed ability messages, default Hide
- Interpolate Time config, whether the cooldowns should use interpolated time or the server time, default enabled
- Show Timer config, show the timer beneath the ability cooldown icon or not, default enabled
- Text Shadow config, shadow type to use for the timer, default Outline

Ability Refreshes
- Removed feature, logic moved into Ability Cooldown Overlay

Build Loadouts
- Replaced Skill Point Loadouts
- Now supports ability tree and aspect loadouts.
- Can save loadout as build (includes skill points, ability tree and aspects) or a loadout for each specific type
- Ability Tree loadouts may break with Wynncraft updates, applying a broken loadout will load what it can and halt upon reaching an unfixable state.

Custom Item Highlights
- Gathering tools now have highlights
- New config Hide Profession Star, hides the tier indicator on ingredients and materials, default disabled

Guides
- Added wards to misc guide

Ingredient Pouch Tooltip Customization
- New feature, enabled for Default and Lite profiles
- Merge Items config, merges counts of the same item together, default enabled
- Ingredient Style config, how should ingredients be styled, default Colored With Stars
  - Options are Vanilla, Colored, With Stars and Colored With Stars
- Primary Sort config, what should be the main factor for sorting ingredients, default Rarity
- Secondary Sort config, what should be the secondary factor for sorting ingredients, default Level
- Invert Sort config, should the sort be inverted, default disabled
- Hide Non-Ingredients config, should items in the ingredient pouch that are not ingredients be hidden from the tooltip, default disabled

Item Compare
- Tooltips now display the correct background style

Nametag Rendering
- Added player guild raid badges
  - These look the same as normal raid badges but with a blue gem

Functions
- Character Functions
  - `ability_cooldown` returns the cooldown of the given ability as a Float
    - `name` required String argument, the ability to retrieve the cooldown of. This will be the name of the node in the ability tree, except for Tribal Chants which uses the name "Chant of the <Mask>"
    - `interpolated` required Boolean argument, whether to return the cooldown as interpolated or not
- Combat Functions
  - `last_spell_mana_cost` or `mana_cost` returns the mana cost of the previous spell
  - `last_spell_health_cost`, `health_cost` or `hp_cost` returns the health cost of the previous spell

Fixes
- Blood pool maximum is no longer rounded to nearest 10
- Fixed item tooltips overlapping when comparing
- Attempted another fix of overlay alignment resetting
- Hopefully fixed overlay render order not persisting in some cases
- Fixed ingredients & materials not removing vanilla highlight
- Fixed log spam when hovering tooltips with unbound compare keybind
