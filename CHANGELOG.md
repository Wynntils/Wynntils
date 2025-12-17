Auto Attack
- New config Attack Tick Delay, how long to wait between each attack in ticks, default 2
  - Setting this value below 1 will treat it as 1

Scoreboard Overlay
- Renders the vanilla scoreboard as an overlay
- Text Shadow config for what shadow type to render with, default None
- Font Scale config for how big/small to render the text, default 1.0
- Render Header config to render the play.wynncraft.com header or not, default enabled
- Header Background Color config for what color to render the background behind the header as, default `#00000066`
- Content Background Color config for what color to render the background behind the content as, default `#00000033`
- Display Original Scoreboard config for whether the default scoreboard should be rendered, default disabled

Functions
- Character Functions
  - `guild_objective_event_bonus` returns a boolean for whether your guild objective will give an event reward bonus
  - `personal_objective_event_bonus` returns a boolean for whether the given personal objective will give an event reward bonus
    - `index` optional Integer argument, the index of which objective to get default 0
  - `leaderboard_position` returns an Integer for the position you are in for the given leaderboard
    - `leaderboardKey` required string argument, the key of the leaderboard to get the position for
      - This key will be what is used on the Wynncraft website/API.
      - Updates every 60 seconds
- Combat Functions
  - `xp_overflow` returns a Long for how much XP you have over the max level on your current character
    - Updates every 60 seconds
- Guild Functions
  - `contributed_guild_xp` returns a Long for how much XP you have contributed to your guild
  - `contribution_rank` returns an Integer for the rank you are in for your guild contribution ranking
    - Both update every 60 seconds

Wynntils Command
- Added `secrets` argument, opens the Wynntils secrets menu
    - Do not share the data you enter here with anyone, not even Wynncraft or Wynntils staff
    - The only option currently is the Wynncraft API token, this may be required for the `leaderboard_position` and `xp_overflow` functions as well as for allowing the `/player` command to work in cases where you should have access to player info

Fixes
- Player command will no longer error if the user has hidden API data
- Fixed store tier annotator crashing
- Fix bomb bell not parsing bombs with an extra space on the 2nd line
- Fix legacy enchanted items missing glint
- Possibly fixed cases where the content tracker would keep trailing text from previous stage
