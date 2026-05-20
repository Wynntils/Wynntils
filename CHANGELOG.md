Container Scroll
- The blacksmith repair menu is now scrollable

Functions
- Social Functions
  - `party_member_name` returns a String of the name of the given party member
    - Required Integer argument `index`, the index of the player in your party
  - `party_member_health` returns an Integer of the health of the given party member
    - Required Integer argument `index`, the index of the player in your party
  - `party_member_level` returns an Integer of the level of the given party member
    - Required Integer argument `index`, the index of the player in your party
  - `is_party_member_online`, returns a Boolean of the online status of the given party member
    - Required Integer argument `index`, the index of the player in your party
  - `is_party_member_alive` returns a Boolean of whether the given party member is alive
    - Required Integer argument `index`, the index of the player in your party
  - `party_total_level` returns an Integer of the total level of your party

Fixes
- Fixed overall roll marker causing crashes in rare cases
- Removed logging of empty action bar
- Fixed gathering tools not being parsed
- Fixed shaman totem timer not being parsed when Transfused is applied
- Waypoints now render behind the hotbar
- Lootrun path performance should be improved
