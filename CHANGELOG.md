2.2.0 Support
- This update adds support for the Wynncraft 2.2.0 update

Auto Attack
- Removed Attack Tick Delay config
- New config Adaptive Lag Correction, adds a small delay when observed melee feedback is lagging behind current weapon timings, default enabled

Block Inventory Casting
- Removed feature, issue fixed in vanilla

Dialogue Option Override
- Removed feature, no longer necessary

Durability Overlay
- Disabled by default until hiding vanilla version implemented

Game Bars
- New overlay Mirror Image Bar, replaces the vanilla boss bar
- New overlay Distortion Bar, replaces the vanilla boss bar
- New overlay Nightcloak knives, replaces the vanilla boss bar

Guides
- Added tier 7 powders to powder guide
- Updated guide powder tooltips

Hide Swap Item Animation
- New feature, prevents the item from moving slot when pressing F to go to the next page on items
- Enabled by default in all profiles besides blank slate

Hide Tripwires
- New feature, enabled by default in all profiles besides Blank Slate
- Forces tripwires to render as invisible
- This fixes an incompatibility between Sodium and the Wynncraft resource pack

Horse Mount
- Renamed to Mount Keybind
- Removed Guaranteed Mount config
- Removed Summon Attempts config

Info Message Filter
- Renamed Horse config to Mount

Item Statistics Info
- Disabled feature by default until issues fixed

Item Text Overlay
- Removed horse configs
- Added mount configs
- Mount statistic config, which stat to display, default Potential

Lootrun Overlays & Functions
- Added support for pink beacons

NPC Dialogue & Overlay
- Removed features and related keybinds

Powder Special Overlay
- Only Show When Weapon Held config is now disabled by default

Quick Casts
- Reworked feature to use millisecond delays instead of ticks
- Default delays changed from 3 ticks to 100ms
- Block attacks config removed
- New config Repeat Melee, default disabled
- Removed safe casting config
- New config, Adaptive Lag Correction, adds a small delay when observed spell feedback is lagging behind current configured timings, default enabled

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

Territory Management Map Screen
- Improved seekings and production territory colouring
- Added visualisation of tower multi-attacks
- Overhauled territory tooltip
- Guild output widget improvements
- Show city and double production territories

Tooltip Fitting
- Removed Wrap Text config
  - Was causing issues with new item tooltip spacing

Translations
- Removed NPC Dialogue translation config
- Added DeepL, Libre Translate and Ollama translation service options
- Added new configs for DeepL API key, Libre translation URL and API key and Ollama URL and API key

Updated XP requirements for level 106-121

Added Crystallized and removed Winded as tracked debuffs

Functions
- Character Functions
  - `mirror_image_clone` returns the status of the given clone as an Integer, -1 for invalid index, 0 for inactive or 1 if active
    - `cloneNumber` required integer argument, the clone to get the data of
  - `mirror_image_duration` returns the duration remaining on mirror image as an Integer
  - `powder_special_charge` returns the charge of your powder special as a CappedValue
  - `distortion` returns the current distortion amount as a CappedValue
- Combat Functions
  - `spell_name_from_direction` returns the name of a spell from your class given direction
    - `spellDirection` required String argument, the directions for the spell name to get (LRL/RRR)
    - `class` required String argument, the class to get the spell for
  - `spell_name_from_number` returns the name of a spell for specific class given its number
    - `spellNumber` required Integer argument, the spell number to get
    - `class` required String argument, the class to get the spell for
  - `ticks_since_specific_spell` returns the ticks since specific spell cast
    - `spellName` required String argument, the name of the spell to get ticks since last cast
- Horse Functions
  - Removed all horse functions
- Mount Functions
  - `capped_mount_stat` or `cap_mnt_stat` returns your selected mount stat as a capped value
    - `stat` required String argument, mount stat name to get (acceleration, altitude, energy, handling, powerup, speed, toughness, training)
  - `mount_stat` or `mnt_stat` returns current value of the selected mount stat
    - `stat` required String argument, mount stat name to get (acceleration, altitude, energy, handling, powerup, speed, toughness, training)
  - `mount_stat_max` or `mnt_stat_max` returns maximum value of the selected mount stat
    - `stat` required String argument, mount stat name to get (acceleration, altitude, energy, handling, powerup, speed, toughness, training)
  - `mount_name` or `mnt_name` returns the name of your mount as a String

Fixes
- Fixed HUD parsing
- Fixed all chat related issues from NPC Dialogue
- Fixed gear boxes not being recognised
- Fixed ingredients not being recognised
- Fixed ingredient pouch not counting ingredients
- Fixed damage tracking
- Fixed gathering tools not being recognised
- Fixed not being able to scroll on final ability tree page
- Fixed materials not being recognised
  - New materials are still unrecognised
- Fixed most weapons and armor items not being recognised
- Fixed charms not being recognised
- Fixed tomes not being recognised
- Fixed fonts used in custom UIs
- Pressing F to change page on locked items is no longer blocked
- Waypoints now render behind the action bar (including NPC Dialogue)
- Fixed tier 7 powders spamming errors
- Powder special is no longer reset on switching item
- Powder can now be applied to items in locked slots
- Fixed spell cast detection
- Fix more orange and rainbow beacon tracking cases
- Fix Hades party functions picking up other players
- Fix input transcription buttons showing when not on Wynncraft servers
- Fixed crafting station UI not being detected
