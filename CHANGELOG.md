Bomb Bell Command
- No bombs available message now uses the chat ranks font to display the rank

Guides
- Tomes and Charms can now be opened on the web item guide

Overlays
- New button on overlay management screen to customize render order.
- Customize the layering of all overlays,
- Customize layering around vanilla GUI elements

Personal Storage Utilities
- Bank page names and jump buttons now use the Wynncraft language font

Functions
- Combat Functions
  - ⁨⁨⁨⁨⁨`debuffs_in_radius_value`⁩⁩⁩⁩⁩ returns the total value of the given debuff in the given radius around you
    - ⁨⁨⁨⁨⁨`radius`⁩⁩⁩⁩⁩ required double argument, the radius around you to check for debuffs
    - ⁨⁨⁨⁨⁨`debuffName`⁩⁩⁩⁩⁩ required string argument, the name of the debuff to get the value for
   - ⁨⁨⁨⁨⁨`targeted_mob_debuff_value`⁩⁩⁩⁩⁩ returns the value of the given debuff for the targeted mob
    - ⁨⁨⁨`range`⁩⁩⁩ required double argument, the range to check for targeted mob
    - ⁨⁨⁨`horizontalDegrees`⁩⁩⁩ required douuble argument, horizontal field of view in degrees to check for targeted mob
    - ⁨⁨⁨`verticalDegrees`⁩⁩⁩ required douuble argument, vertical field of view in degrees to check for targeted mob
    - ⁨⁨⁨`debuffName`⁩⁩ ⁩required string argument, the name of the debuff to get the value for

Available debuff types for functions are: Bleeding, Blindness, Burning, Confused, Contaminated, Discombobulated, Enkindled, Freezing, Marked, Poison, Provoked, Resistance, Slowness, Trick, Weakness, Whipped and Winded

Fixes
- Fix ingredients "not touching" modifier not being parsed
- Fix per character gear sharing settings not saving
- Prevent health potion blocker, horse mounting, prevent trades & duels, quick casts and custom content book from working whilst in the wardrobe
- Fix crashing when handling errors with chat tabs
- Fix enhanced streamer mode hide tooltips option not working for crafted gear
- Auto apply resource pack feature will no longer function if not using OpenGL (e.g. Vulkan)
- Negative stats on shared crafted items are no longer decayed
