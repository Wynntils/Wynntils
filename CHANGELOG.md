Guides
- Renamed Emerald Pouch guide to Emerald Guide, added emerald, emerald block and liquid emerald items
- Renamed Rune Guide to Misc Item Guide, added dungeon keys

Waypoint Management Screen
- Shift clicking icon filter buttons will toggle between excluding and including all other icons

Functions
- String Functions
  - `from_codepoint` Converts the given number to the equivalent string character
    - `codepoint` required Number argument, the codepoint to convert
- StyledText Functions
  - `styled_text` Converts the given String to StyledText
    - `value` required String argument, the string to convert to StyledText
  - `concat_styled_text` or `concat_st` concatenates a list of StyledText together
    - `values` required arguments, a list of StyledText
  - `with_atlas_sprite_font` Uses the given atlas sprite for rendering the given StyledText
    - `value` required StyledText argument, the text to display as an atlas sprite
    - `atlas` required String argument, the namespace path for the atlas to use
    - `sprite` required String argument, the name path for the sprite to use
  - `with_player_sprite` Uses the given player for rendering the given StyledText
    - `value` required StyledText argument, the text to display as a player sprite
    - `uuid` required String argument, the UUID of the player to use
    - `hat` required Boolean argument, whether or not to render the hat layer of the player
  - `with_resource_font` or `with_font` Uses the given font to render the StyledText
    - `value` required StyledText argument, the text to render
    - `font` required String argumernt, the namespace path of the font to use
  - `with_color` Applies the given color to the whole StyledText unless overriden
    - `color` required CustomColor argument, the color to apply to the StyledText
  - `with_bold` Applies bold to the whole StyledText
    - `isBold` required Boolean argument, whether or not to apply bold
  - `with_italic` Applies italics to the whole StyledText
    - `isItalic` required Boolean argument, whether or not to apply italics
  - `with_strikethrough` Applies strikethrough to the whole StyledText
    - `isStrikethrough` required Boolean argument, whether or not to apply strikethrough
  - `with_obfuscated` Applies obfuscated to the whole StyledText
    - `isObfuscated` required Boolean argument, whether or not to apply obfuscated
  - `with_shadow_color` Applies the given color to the whole StyledText shadow unless overriden
    - `color` required CustomColor argument, the color to apply to the StyledText shadow
    - This effect will only be visible when using the `Normal` text shadow config for an overlay
  - `with_underlined` Applies underlined to the whole StyledText
    - `isUnderlined` required Boolean argument, whether or not to apply underlined
- Hades Party Functions
  - `hades_party_member_uuid` returns the UUID of the given Hades party member as a String
    - `index` required Integer argument, the party member to get
- Social Functions
  - `player_uuid` or `uuid` returns your UUID as a String
- Inventory Functions
  - `equipped_accessory_name` returns the name of the given accessory type as a String, if no accessory found then "NONE" is returned
    - `accessory` required String argument, the accessory type to retrieve such as "Ring_1" or "Bracelet"
 
Fixes
- Overlays will stop rendering when disabled
- Fix mob kills not being detected with 1.5x XP bonus
- Debuff functions now use player eye position instead of camera position
- Simulators and Insulators in guide correctly state obtain from lootruns now instead of raids
