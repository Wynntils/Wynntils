Bulk Buy
- When hovering a bulk buyable item with shift held the item name will now display how many will be bought

Command Expansion
- Added parameters to `/emotes` & `/emote`
  - Currently this will list all emotes, in a future release it will only show available emotes
- Removed `/scrap`

Container Scroll
- Store pages are now scrollable

Container Search Highlight
- Added Search Bar in Cosmetic Menus config, covers all cosmetic types, default enabled
- Removed Search Bar in Helmet Cosmetics Menu config
- Removed Search Bar in Pet Menu config
- Removed Search Bar in Player Effects Menu config
- Removed Search Bar in Weapon Cosmetics Menu config
- Removed Search Bar in Scrap Menu config

Durability Overlay
- New render mode Percentage, will render the durability as a percentage below the item

Hide Damage Labels
- Renamed feature to Hide Labels
- Hide Damage Labels is now a config, still disabled by default
  - If you had the feature enabled it will carry over to this config
- New config Hide Kill Labels, will hide the label upon killing a mob, default disabled

Hide Irrelevant Tooltip Info
- Removed Hide Additional Tooltip Info config, the info previously hidden by this config is no longer displayed

Horse Mounting
- New config Summon Attempts, how many times should horse mounting be attempted before stopping, default 8

Personal Storage Utilities
- Added support for misc bucket and bookshelf pages 11 & 12
- Pages that have not yet been visited/unlocked now have red text
- Changed quick jump button tooltips

Settings Screen
- Hovering features on the left page will now display their configs on the right page until a feature has been selected
  - These are only visual, in order to actually edit them you still need to click them on the left page
- Closing the screen via esc when you have interacted with a setting will now give a prompt to save changes
  - Closing the screen via the designated save/discard changes button will not give this prompt

Fixes
- Cosmetic pages can now be scrolled again
- Scrap menu can now be scrolled again
- Chat items should no longer appear as stone when using certain mods and/or clients
- `capped_inventory_slots` and `inventory_free` functions now take into account the extra slot from soul points being removed
- Fix horse mounting failing to swap back to original item if failing to mount horse
