Block Inventory Casting
- Removed feature, issue fixed in vanilla

Game Bars
- New overlay Mirror Image Bar, replaces the vanilla boss bar
- New overlay Distortion Bar, replaces the vanilla boss bar
- New overlay Nightcloak knives, replaces the vanilla boss bar

Hide Tripwires
- New feature, enabled by default in all profiles besides Blank Slate
- Forces tripwires to render as invisible
- This fixes an incompatibility between Sodium and the Wynncraft resource pack

Powder Special Overlay
- Only Show When Weapon Held config is now disabled by default

Raid Progress Overlay
- Added support for The Wartorn Palace raid

Spell Inputs Overlay
- New config Clear on Failed Cast, whether the inputs should be cleared on a failed spell cast

Spell Cast Message Overlay
- New config Should Display Original, whether or not to render the original message, default disabled
- New config Message Style, how the spell cast message should look, Modern or Legacy, default Modern
- New config Show Failed Casts, should a message be displayed on failed casts, default enabled
- New config Text Shadow, what shadow type to render the text with, default Normal
- New config Font Scale, how big/small to render the text, default 1
- Moved default position to match vanilla position

Updated XP requirements for level 106-121

Added Crystallized and removed Winded as tracked debuffs

Functions
- Character Functions
  - `mirror_image_clone` returns the status of the given clone as an Integer, -1 for invalid index, 0 for inactive or 1 if active
    - `cloneNumber` required integer argument, the clone to get the data of
  - `mirror_image_duration` returns the duration remaining on mirror image as an Integer
  - `powder_special_charge` returns the charge of your powder special as a CappedValue
  - `distortion` returns the current distortion amount as a CappedValue

Fixes
- Powder special is no longer reset on switching item
- Powder can now be applied to items in locked slots
- Fixed damage parsing
- Fixed spell cast detection
