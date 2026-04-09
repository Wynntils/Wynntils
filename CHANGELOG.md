Game Bars
- Removed distortion bar overlay

Mage Distortion Overlay
- New overlay, shows the distortion count
- Enabled in the default profile

Mounts
- New config Switch to Third Person on Mount, will put you into 3rd person view upon riding mount and revert to previous view after dismount, default disabled

Functions
- Character Functions
  - `capped_distortion` removed
  - `current_distortion` added, returns an Integer of the current distortion count
- Color Functions
  - `wynncraft_shader` returns the color for the given Wynncraft shader
    - `shaderName` required String argument, the name of the shader to get, view the description to see options
    - Going forward this function will be used instead of adding new functions for each shader

Fixes
- Trade market quick search now removes unicode padding
- Fixed distortion count tracking
- Item name functions now remove unicode padding
- Equipped armor/accessory name functions now correctly return "NONE" when no item present
- Fix custom content book items not updating
- Added missing emerald icon to territory management map view
- Fix death whistle quest being counted as a mount
- Fixed bank searching not continuing automatically
