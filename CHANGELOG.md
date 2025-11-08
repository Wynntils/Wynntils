Game Bars
- New overlay Momentum Bar
  - Displays the momentum count & progress from the Assassin ability
  - Normal bar configs
  - Maximum Color config, the color of the bar when maximum momentum is reached
 
Item Statistics Info
- Weight stats when holding shift are now sorted from most to least impactful
- Improved spacing in weight tooltip section
- Holding Shift + Control now displays the contribution per stat instead for weightings

NPC Dialogue
- New keybind Progress NPC Dialogue, when pressed will send the sneak input to progress dialogue, default unbound
- New config Override Sneak Key, only allow progressing NPC dialogue via the custom keybind if bound, default enabled

NPC Dialogue Overlay
- Disabled by default until issues with parsing background chat issues fixed

Remove Shiny Glint
- Disabled by default
- Removes the default glint effect from shiny items for shader compatibility
- Replace Glint config, gives shiny items the vanilla enchanted glint, default enabled

Functions
- Character Functions
  - `momentum_percent` or `momentum_pct` returns a CappedValue of the progress towards the next momentum charge
  - `momentum` returns the current momentum count

Fixes
- Spell inputs will no longer remain on screen if swapping item before they should clear
- Shiny perfect/defective items will now show their shiny status in the item name
- Gear sharing will work once again if your chestplate, leggings or boots have cosmetics enabled
- Spell inputs will now be visible if the Spell Message feature is disabled
- Status Effects should now always use the correct font for symbols
- Item lock slots will no longer function whilst in housing edit mode
