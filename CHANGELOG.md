Item Statistics Info
- Re-enabled feature by default in the default, lite and minimal profiles
- Updated displays to work with new item tooltips
- Obfuscation Chance configs removed
- Show Max Values config replaced with Show Roll Wheel (for crafteds)
- New config Overall Percentage on Perfect/Defective, should the overall % be displayed alongside a Perfect/Defective marker, default enabled

Item Text Overlay
- New config Teleport Scroll Color By Charges, changes the text color based on charges remaining
- Some locations now use a 3 letter abbreviation

Functions
- Inventory Functions
  - `teleport_scroll_charges` or `tp_scroll_charges` returns the number of scroll charges you have remaining. Returns -1 if no scroll is present.
  - `teleport_scroll_recharge_timer` or `tp_scroll_timer` returns seconds until the next teleport scroll charge. Returns -1 if no timer is active.

Fixes
- Distortion count is now reset on death or /class
- Fixed shaman mask and totem tracking
- T4 amps and The Wartorn Palace crafter bags are now handled
- Fixed rune parsing
- Fixed level up chat filter
- Fixed mana bar overlay removing background from final NPC dialogue choice
- Possibly fixed Azael portrait being removed if health bar overlay enabled
- Fixed character ID and other on join stats not being parsed sometimes
- Fixed ping function not always working
- Fixed various issues related to gear parsing
