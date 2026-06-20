Custom Player List Overlay
- Updated texture for better contrast with new rank colors

MantleShieldTrackerOverlay
- Added support for broken mantles

Waypoint Distance
- New config Dynamic Distance Opacity, should the waypoint display fade as you approach the auto remove distance, default disabled
- New config Auto Remove Reached Waypoints, how close, in blocks, should you be to a waypoint before it is removed, default 20
- New config Auto Remove Waypoint Distance, should waypoints be removed automatically when you are close enough to them, default disabled

Functions
- Conditional Functions
  - `switch_case` or `switch` Compares the given base value with a list of given test values and returns the value associated to that test. If no match is found the default value is returned
    - `switch` Base value to compare with the other elements of the list
    - `default` Default value to return if no match is found. If `toTestN` is of a different type than `switch` or if the size of `cases` is not even this value is returned
    - `cases` List of values to test followed by the value to return if match. The size must be even and the architechure is as follow `[toTest1, ifTrue1, toTest2, ifTrue2, ..., toTestN, ifTrueN]]`
- Spell Functions
  - `broken_mantle_shield_count` or `broken_mantle_shield` returns the number of broken mantle shield charges you have
  - `judrajim_active` or `is_judrajim_active` returns if the judrajim ability is active or not

Added support for new lootrun missions and trials

Fixes
- Possibly fixed orange and rainbow amounts not being tracked when aqua and/or vibrant boosted
