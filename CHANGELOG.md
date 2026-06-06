Highlight Gathering Nodes
- New feature, adds a box around gathering nodes
- Enabled for default, new player and lite profiles
- Highlight color config, the color for the box to be, default white
- Always show config, whether to only show when holding a gathering tool or not, default false

Item Statistics Info
- Item Weights default is now All
- Removed show stars config
- Added Rainbow Perfect Internal Roll config, makes the 100% text rainbow when a perfect internal roll is on the item (previously displayed with 3 stars)

Main Map
- New button to show gathering node positions, they will show on both the map and minimap
- Choose which specific resources are displayed
- Data is based on the Wynncraft API, may not be 100% accurate

Nametag Rendering
- Updated the icon to show Wynntils users to match the icon used in Wynntils chat messages

Online Members Command
- Will now default to current guild if no guild argument given

Fixes
- Possible fix for info boxes/custom bars resetting alignment
  - Note the fix for this may cause some of your info boxes/custom bars to reset their alignment once on first run after updating but in theory it shouldn't happen again
- The `has_no_gui` function will now work in most if not all situations
- Overlays and waypoints should no longer render whilst in a cutscene
- Raid tracking will now resume after disconnect if you are in the same session
- Fix world event start time function not always resetting
- Item weights are no longer duplicated after repeated downloads
- Fix auto skip cutscenes not working for group cutscenes (NoL)
