Archer Beast Tracker Overlay
- New overlay, enabled in the default profile by default
- Displays time left on your hounds and total number of crows and snakes
- Text Color config, the color to use for the overlay, default white

Functions
- Character Functions
  - `crow_count` returns the amount of your currently active crows
  - `hounds_time_left` the time left on your hound(s)
  - `snake_count` returns the amount of your currently active snakes
- Raid Functions
  - `chosen_gambits` returns the amount of gambits chosen
  - `chosen_gambit` returns the name of the chosen gambit by index
    - `index` required Integer argument, the index of the gambit to retrieve
  - `chosen_buffs` returns the amount of raid buffs chosen
  - `chosen_buff` returns the name of the chosen raid buff by index
    - `index` required Integer argument, the index of the raid buff to retrieve
- StyledText Functions
  - Added `st` alias to `styled_text`

Most messages sent by Wynntils to chat will now have be wrapped with a custom banner like vanilla messages, the banner is green with a small shield icon

Ascended items are now visible in the item guide with the "Ascended" prefix, when an indicator that an item is ascended is added we will release an update supporting them for percentages and sharing in chat

Fixes
- Fixed color handling in functions not working as expected since the introduction of StyledText functions
- Fixed status effects with no color not being tracked
- Fixed auto skip cutscenes
- Fixed rare crash with auto expand use items
