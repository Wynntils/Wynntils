Auto Attack
- New config Weapon Swing, should a weapon swing animation be played when auto attack sends a left click, default enabled

Chat Item
- Added support for sharing powders and powder special
  - To support this, when sharing an item it will cycle through all 3 pages to parse information before being available to share
- Clicking a shared item in chat will now cycle through its pages
- Perfect items with full perfect internal rolls now have a rainbow [100%]

Discord Rich Presence
- Will now display current raid in activity details

Guides
- Added support for pages 2 & 3 of gear and charms
- Redesigned obtain info segment of items

Item Lock
- New config Block Clicks in Cutscenes, should clicks be blocked in cutscenes, default enabled

Item Statistics Info
- Restored hold right shift to show vanilla tooltip functionality
- Changed percentage and roll wheel configs to instead be options for display style of overall and individual percentages. Options are available for identified item and crafted items separately. Can choose Percentage, Wheel or None

On the Brink Bar
- New feature, displays the value of the On the Brink ability as a custom bar, enabled for default profile

Quick Cast
- New config Weapon Swing, should a weapon swing animation be played when quick spell cast sends a left click, default enabled

Compass Command
- Added various new supported formats for parsing coordinates

Functions
- Character Functions
  - `on_the_brink_percent` or `on_the_brink_pct` returns your current On the Brink as percentage of max
    - Return type: `CappedValue`
  - `on_the_brink` returns the current On the Brink value
    - Return type: `Integer`
  - `ritual_circle_radius` returns the current radius of Ritual Circle
    - Return type: `Integer`

Fixes
- Mount potential text overlay now uses exact potential when below 1000 and estimated potential is enabled
- Custom in-world rendering such as lootrun paths and player pings should now be visible with most shader packs
- Fixed alignment issues with pages 2 and 3 of tooltips when they have been edited
- Crafted consumables now fully support percentage display
- Various tooltip text shadow fixes to match original style
- Various alignment improvements on tooltips
- If first viewing of an item was not on the 1st page, upon changing to the 1st page percentages will now properly display
- Chat and guide items now display the correct elements under the item name
- Fixed set bonuses not showing on items
- Fixed charm of the corruption having the wrong emblem when shared in chat and in the guide
- Fixed certain items displaying an outdated quest requirement
