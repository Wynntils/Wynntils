Nametag Rendering & Player Viewer
- This version is now required to view other players' gear, though you may be able to see gear from some users still running v3.3.0
- You no longer need to be in a guild to see gear from other players
- Gear sharing should in theory now always work in expected scenarios

NPC Dialogue
- Re-enabled feature by default
- Chat messages will now be recoloured after exiting NPC Dialogue
- Will no longer cause other chat related features to not function as expected

Functions
- Bomb Functions
  - `bomb_remaining_time` returns a Time of the remaining time on the given bomb
    - Same arguments as other bomb functions

Fixes
- Fixed certain cases where non NPC dialogue would be displayed as NPC dialogue
- Duplicate messages received whilst in NPC dialogue are no longer filtered out
- Bomb Functions will no longer crash on invalid indexes
- Hades Party functions will no longer crash on invalid indexes
- Format Time Advanced function will now function properly
