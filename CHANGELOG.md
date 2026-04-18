Map & Minimap
- Added Item Upgraders
- Added Mount Merchants
- Added Mount Enclosures
- Added Lootrun Camps

Functions
- Character Functions
  - `puppet_count` returns the amount of your currently active puppets
  - `puppets_in_time_range` returns the amount of your currently active puppets that are within specified time range
    - `min` required Integer argument, minimum time range to check
    - `max` required Integer argument, maximum time range to check

Fixes
- Fixed performance issues introduced from shaman totem tracking
- Unid character is now removed when quick searching on trade market
- Reset hummingbird state on world change
