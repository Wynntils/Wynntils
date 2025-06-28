Bomb Bell Overlay
- Added group bombs option to group bombs of same type together, default enabled
- Added max bombs option to limit the number of bombs displayed. If grouping is enabled it applies per bomb type, otherwise it applies to all, default 5
- Added sort order config to change the order bombs are displayed in, default newest

Changelog
- Changelogs are now manually written for extra details on changes
- Changelog is now displayed on the character selection screen instead of on world join
- Pages are scrollable and paginated by version instead of merged into one changelog

Chat Item, Item Statistics Info, Player Viewer
- Uses the rainbow shader for perfect items
- Reverted recent change to display stars on negative items when sharing or viewing other players' items

Command Expansion
- `/switch` will now suggest active worlds for autocompletion

Custom Content Book
- Completely reworked feature to only display the current page of the default book instead of scanning the entire book
- Tracking & untracking is now instant
- All content types are supported
- Tracked content details are displayed in lower portion of screen including rewards
- XP until next level is displayed
- Searching will dim non matching content
- Button to access the Wynntils menu
- Added tutorial support
- New config Shift Behavior on Content Book, determines if the custom screen should only be opened if shift is held or not held, default disabled if shift held
  - If you had the Replace Wynncraft Content Book option disabled, then the above config will be set to enabled if shift held by default
- Removed Replace Wynncraft Content Book config
- Removed Initial Page config
- Removed Show Content Book Loading Updates config
- Removed Cancel All Queries on Screen Close config

Custom Universal Bars, Ability bars & other bar overlays
- Added multiple texture options
- Added Color Template option, use functions returning a color value to dynamically set the color of the bar and text

Guides
- Added accessory filters to item guide
- Repositioned all widgets
- Added keybinds to open charm, tome and aspect guide, default unbound

Highlight Duplicate Cosmetics
- Added option to move tooltip when hovering cosmetics, default enabled

Item Highlights
- Added 2 new texture options, Circle Outline Large and Circle Outline Small

Item Record
- Changed item storage method to allow gear items to update texture with API changes
- Crafted items may still have their texture break with resource pack updates

Nametag Rendering
- The Wynntils marker is now a shield again

Overlay Selection Screen, Settings Screen
- Overlay buttons with parent feature disabled will now display a tooltip with a warning when hovered
- Added buttons in configurable list for overlay position, size and alignment

Overlays
- Text based overlays vertical alignment now functions correctly for center and bottom when the overlay has multiple lines. When center aligned the center of the lines is in the middle of the overlay area. When bottom aligned the final line will be at the bottom of the area.

Unidentified Item Icon
- Added all alignment opions to revealed icon location config

Wynncraft Button
- Now displays a warning if a Wynntils update is available and on click will take you to an update prompt screen

Wynntils Hint Messages
- Displays hint messages relating to Wynntils upon first world join of the current session, default enabled
- First Join Only config, whether messages should only be sent on first world join or every world join, default enabled

Function Command
- `/functions list` is now paginated via the `page` argument

Servers Command
- `/server info` and `/server i` will now suggest active worlds for autocompletion

Functions
- Color Functions
	- `from_hex` returns a color from the given hex string
		- `hex` required string argument, the hex code of the desired color
- Profession Functions
  - `profession_xp` or `prof_xp` returns the a CappedValue for the XP progress through current level for the given profession
    - `profession` required string argument, the name of the profession to get XP progress for

Item Filters
- Filters containing the word `defence` will now accept the `defense` spelling as well

Many interactable widgets on screens have received alternate textures/colors upon being hovered and/or tooltips to indicate they can be interacted with.

Fixes
- Sharing locations in chat now rounds to the block position correctly
- Fixed rendering issues with duplicate cosmetic highlight
- Fixed rendering issues with item record
- Added lootrun overlays to the overlays category on settings screen
- Adding a new info box or custom bar will no longer undo config changes made in the current session
- Fixed negative rolls sometimes being incorrect when sharing
