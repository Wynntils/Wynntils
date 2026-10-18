Map & Minimap
- Added Time Trial locations

Time Trial Overlay
- New overlay as part of the Mount feature displaying the current time trial elapsed time and checkpoints

Statistics
- Added statistics for total time trials completed and failed

Functions
- Time Trial Functions
  - `current_trial_checkpoints` or `trial_checkpoints` returns the checkpoints progress of the current time trial
  - `current_trial_elapsed_time` or `trial_time` returns the amount of time elapsed for the current time trial in milliseconds
  - `current_trial_mount_type` or `trial_mount_type` returns the name of the mount type the current time trial uses
  - `current_trial_name` or `trial_name` returns the name of the current time trial

Fixes
- Trying to apply an ability tree loadout that includes a removed node will now display an error rather than timing out
- Auto 3rd person for mounts will now work when starting or resuming time trials
