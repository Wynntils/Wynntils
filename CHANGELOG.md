## [1.2.1](https://github.com/Wynntils/Artemis/compare/v1.2.0...v1.2.1) (2024-05-05)


### New Features

* Add keybind to open territory management [skip ci] ([#2454](https://github.com/Wynntils/Artemis/issues/2454)) ([78f302d](https://github.com/Wynntils/Artemis/commit/78f302daf4bb314c23bf2ca7a73647f2821c673a))
* Click territory pois to set marker on guild map [skip ci] ([#2455](https://github.com/Wynntils/Artemis/issues/2455)) ([83712b8](https://github.com/Wynntils/Artemis/commit/83712b82d161cdace71c10ac9f1f08adb783a890))
* Make territory attack timer bold if the player is standing in it ([#2458](https://github.com/Wynntils/Artemis/issues/2458)) ([8f1aadf](https://github.com/Wynntils/Artemis/commit/8f1aadf0f05ce37607c458953a323ea90bff3766))


### Bug Fixes

* Fix PlayerCommand not using the correct date format [skip ci] ([#2456](https://github.com/Wynntils/Artemis/issues/2456)) ([b748bb2](https://github.com/Wynntils/Artemis/commit/b748bb2fff8312353a4265ffd230d484373d5933))
* Fix territory connections not working due to some connections not being displayed as bidirectional [skip ci] ([#2453](https://github.com/Wynntils/Artemis/issues/2453)) ([ea97818](https://github.com/Wynntils/Artemis/commit/ea978187f9b229d5071c9ab653c999f3812ced68)), closes [#1](https://github.com/Wynntils/Artemis/issues/1)
* Fix Territory Management Screen not working in wars [skip ci] ([#2457](https://github.com/Wynntils/Artemis/issues/2457)) ([89a108c](https://github.com/Wynntils/Artemis/commit/89a108ce0ac43779e19787747311376429f6d127))

## [1.2.0](https://github.com/Wynntils/Artemis/compare/v1.1.14...v1.2.0) (2024-05-05)


### ⚠ BREAKING CHANGES

* Release Wynntils 1.2.0

### New Features

* Add an overall mode the statistics page ([#2432](https://github.com/Wynntils/Artemis/issues/2432)) ([31e4c5d](https://github.com/Wynntils/Artemis/commit/31e4c5ddd7ec45e09248185c935327b270b88a12))
* Add beacons/POIs for territories with queued war timers ([#2406](https://github.com/Wynntils/Artemis/issues/2406)) ([4d023bf](https://github.com/Wynntils/Artemis/commit/4d023bf425b4567b846298a98834098557e27bca))
* Add ExtendedSeasonLeaderboardFeature ([#2431](https://github.com/Wynntils/Artemis/issues/2431)) ([5e80f8e](https://github.com/Wynntils/Artemis/commit/5e80f8e01fa2af91e4a6f068a6dda4c3c53258eb))
* Add guild bank redirector in ChatRedirectFeature ([#2430](https://github.com/Wynntils/Artemis/issues/2430)) ([171a012](https://github.com/Wynntils/Artemis/commit/171a0123853cccb8a76387bbc362be66a5606423))
* Add mythic pull dry streaks and some new lootrun statistics ([#2435](https://github.com/Wynntils/Artemis/issues/2435)) ([c4b6f1a](https://github.com/Wynntils/Artemis/commit/c4b6f1a01b75b1a4b7ba72b5a808e880330c4cf3))
* Add onlinemembers and player command ([#2412](https://github.com/Wynntils/Artemis/issues/2412)) ([53f63de](https://github.com/Wynntils/Artemis/commit/53f63ded9bd78cd9e2440a2af7413b228a7e7731))
* Add ProfessionHighlightFeature ([#2424](https://github.com/Wynntils/Artemis/issues/2424)) ([2bff405](https://github.com/Wynntils/Artemis/commit/2bff405aa1b4eb1f03c2b27eb89508f6addf2890))
* Add RevealNicknamesFeature ([#2408](https://github.com/Wynntils/Artemis/issues/2408)) ([ad4c00f](https://github.com/Wynntils/Artemis/commit/ad4c00fff07a2b00e2983b0606ed88a7dd0e8864))
* Add ServerUptimeInfoOverlay ([#2414](https://github.com/Wynntils/Artemis/issues/2414)) ([a8ef08a](https://github.com/Wynntils/Artemis/commit/a8ef08a617fabf4f9622384f7d8b40c6e890d9ef))
* Add soulpoint subcommand and aliases for server commands, click-to-switch server texts, fix formatting issues, command no longer hands the game ([#2415](https://github.com/Wynntils/Artemis/issues/2415)) ([aa94e6e](https://github.com/Wynntils/Artemis/commit/aa94e6ecd2bcbdcaf7f8dfe1c854f0936b1cd2b4))
* Add statistics functions, add overall support for statistics command ([#2434](https://github.com/Wynntils/Artemis/issues/2434)) ([ebde28b](https://github.com/Wynntils/Artemis/commit/ebde28bb238a2f39de3073aa236ab824f5142978))
* Add TerritoryManagementScreen ([#2439](https://github.com/Wynntils/Artemis/issues/2439)) ([e9035c9](https://github.com/Wynntils/Artemis/commit/e9035c946e28222435d6c584758462440f51704a))
* Add timer and vignette overlay, sound effect and functions for tower volley and tower aura ([#2405](https://github.com/Wynntils/Artemis/issues/2405)) ([9b169fd](https://github.com/Wynntils/Artemis/commit/9b169fd5a773fb713129b4255c9f5a659c183652))
* Add TowerStatsFeature and TowerStatsOverlay ([#2427](https://github.com/Wynntils/Artemis/issues/2427)) ([7613756](https://github.com/Wynntils/Artemis/commit/7613756512a28b4e10c0be7322cd27924f1708a7))
* Add war statistics and WarSinceFunction ([#2451](https://github.com/Wynntils/Artemis/issues/2451)) ([8e07892](https://github.com/Wynntils/Artemis/commit/8e07892e8562f4c126597c539332e4d7199f481c))
* Add WarHornFeature ([#2411](https://github.com/Wynntils/Artemis/issues/2411)) ([16b682c](https://github.com/Wynntils/Artemis/commit/16b682c79e46a45021a79b6d26410569479d6455))
* Add WeeklyConfigBackupFeature ([#2449](https://github.com/Wynntils/Artemis/issues/2449)) ([a58ef62](https://github.com/Wynntils/Artemis/commit/a58ef62e48b4fdba26b70e4b5ee5e341361bfde4))
* Bomb Bell Overlay ([#2409](https://github.com/Wynntils/Artemis/issues/2409)) ([460dc41](https://github.com/Wynntils/Artemis/commit/460dc41fad68d22930b1596b5de1bb096da327e0))
* Handle territory timers in a more advanced way to make sure they always appear as accurate as they can ([#2429](https://github.com/Wynntils/Artemis/issues/2429)) ([eee5682](https://github.com/Wynntils/Artemis/commit/eee5682aba3d93a8185e2311d3ed1391affa0946))
* Item Filter GUI ([#2417](https://github.com/Wynntils/Artemis/issues/2417)) ([ec77450](https://github.com/Wynntils/Artemis/commit/ec7745062f0d4737c457d46c01a882807368379d))
* Raid progress overlay and print times feature ([#2446](https://github.com/Wynntils/Artemis/issues/2446)) ([b4e5c43](https://github.com/Wynntils/Artemis/commit/b4e5c436049236a2119ab87cc0596e9c1536d082))
* Redirect guild reward messages ([#2437](https://github.com/Wynntils/Artemis/issues/2437)) ([bfdb3fa](https://github.com/Wynntils/Artemis/commit/bfdb3fa72130d1b26b20b7b35cb55bc4dd954ddf))
* Release Wynntils 1.2.0 ([c104c45](https://github.com/Wynntils/Artemis/commit/c104c452b734e295a0cd9735ab4322576bdcdd41))
* Spell Recast Functions [skip ci] ([#2448](https://github.com/Wynntils/Artemis/issues/2448)) ([f216c38](https://github.com/Wynntils/Artemis/commit/f216c38e766aa6838d36901cb2a4c0287d15846e))


### Bug Fixes

* Fix CrowdSourcedData races by using concurrent collections ([#2447](https://github.com/Wynntils/Artemis/issues/2447)) ([e580f9c](https://github.com/Wynntils/Artemis/commit/e580f9ca01a32c9431487225a436894cfb4aaa2d))
* Fix TerritoryModel[#get](https://github.com/Wynntils/Artemis/issues/get)TerritoryProfileFromShortName not having a stable order ([#2442](https://github.com/Wynntils/Artemis/issues/2442)) ([adfcf50](https://github.com/Wynntils/Artemis/commit/adfcf5052cfa185fbc9c08e3e3732801e6cd483d))
* Fix WorldWaypointDistanceFeature not rendering all waypoint markers correctly ([1af00a8](https://github.com/Wynntils/Artemis/commit/1af00a834ee23c8facb769303b51cfbf1d2f3f31))
* Make boss-bars update, if they are rendered, but tracked ([675c681](https://github.com/Wynntils/Artemis/commit/675c6816d3c447b9a22e0fec8ad733128f6a108b))
* Make guild map compass behaviour consistent with main map [skip ci] ([#2450](https://github.com/Wynntils/Artemis/issues/2450)) ([b43bbe6](https://github.com/Wynntils/Artemis/commit/b43bbe644a6f50d63a8deda9ee8feec3fd3fac4b))
* Make GuildAttackTimerModel use string territory names instead of profiles ([#2445](https://github.com/Wynntils/Artemis/issues/2445)) ([0467354](https://github.com/Wynntils/Artemis/commit/0467354cb197b11b6ba3a7d88b0957e8bab5723b))
* Use proper url encoding [skip ci] ([#2410](https://github.com/Wynntils/Artemis/issues/2410)) ([c510838](https://github.com/Wynntils/Artemis/commit/c510838f3faa83e30416715eb666e0eb4c78697e))
* Use the new dropMeta info for showing item guesses [skip ci] ([#2399](https://github.com/Wynntils/Artemis/issues/2399)) ([81337ca](https://github.com/Wynntils/Artemis/commit/81337ca53501904b7956733b957c474e09611f8f))


### Performance Improvements

* Multiple `StyledText` optimizations [skip ci] ([#2444](https://github.com/Wynntils/Artemis/issues/2444)) ([91e84e2](https://github.com/Wynntils/Artemis/commit/91e84e2f329f9d6f483e101957de7b23d74eeeac))
* Optimize ItemHandler lore soft matching [skip ci] ([#2440](https://github.com/Wynntils/Artemis/issues/2440)) ([14de02f](https://github.com/Wynntils/Artemis/commit/14de02f834ca434d227908f26b880d94e45a4921))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#2452](https://github.com/Wynntils/Artemis/issues/2452)) ([4493d0e](https://github.com/Wynntils/Artemis/commit/4493d0edd9532022dbce5b45211b889b95732591))
* **release:** v1.2.0 [skip ci] ([fe4d7cc](https://github.com/Wynntils/Artemis/commit/fe4d7cc28f7d1af786cfdb35e2169f33e218e974))


### Code Refactoring

* Move onlinemembers and player command json handling to models ([#2438](https://github.com/Wynntils/Artemis/issues/2438)) ([b0437cb](https://github.com/Wynntils/Artemis/commit/b0437cb3b364ac64f7d600a234d009dbc7c092e0))

## [1.1.14](https://github.com/Wynntils/Artemis/compare/v1.1.13...v1.1.14) (2024-04-25)


### Bug Fixes

* Detect flying chests correctly ([#2428](https://github.com/Wynntils/Artemis/issues/2428)) ([6dd95b8](https://github.com/Wynntils/Artemis/commit/6dd95b872fd0627663b380c612b7335c255ff60c))
* Fix crops resource name causing TerritoryAnnotator to crash [skip ci] ([#2425](https://github.com/Wynntils/Artemis/issues/2425)) ([dd6174f](https://github.com/Wynntils/Artemis/commit/dd6174fb7ce135a663e91615abbdefdc4e20361d))


### Miscellaneous Chores

* **release:** v1.1.14 [skip ci] ([a301276](https://github.com/Wynntils/Artemis/commit/a301276d41aeaf260c02097c3bdc2cce2f45c161))
* Update mod dependency and ci versions [skip ci] ([#2426](https://github.com/Wynntils/Artemis/issues/2426)) ([12438f3](https://github.com/Wynntils/Artemis/commit/12438f3f1d8ecf52f2ab0faf4b18e2edbeccebdc))

## [1.1.13](https://github.com/Wynntils/Artemis/compare/v1.1.12...v1.1.13) (2024-04-23)


### Bug Fixes

* Fix minimap disabling/crashing when changing the minimap's configs ([#2423](https://github.com/Wynntils/Artemis/issues/2423)) ([9d85b5a](https://github.com/Wynntils/Artemis/commit/9d85b5a092c8a30f5bc0f136083f0cae9bcb8da5))


### Miscellaneous Chores

* **release:** v1.1.13 [skip ci] ([75c0a0d](https://github.com/Wynntils/Artemis/commit/75c0a0d0b0bb57608c677d7ef74e2ca4f1d09c4b))

## [1.1.12](https://github.com/Wynntils/Artemis/compare/v1.1.11...v1.1.12) (2024-04-23)


### New Features

* Add Enabled Template to TextOverlay [skip ci] ([#2407](https://github.com/Wynntils/Artemis/issues/2407)) ([b8b289c](https://github.com/Wynntils/Artemis/commit/b8b289c94b9e2aa9bdd270a422394548fb72550f))
* Add exponential interpolation map zooming [skip ci] ([#2373](https://github.com/Wynntils/Artemis/issues/2373)) ([54bc3af](https://github.com/Wynntils/Artemis/commit/54bc3af66cd500466dfd91fb1b51756b29e5af4e))
* Introduce provider types for ItemStatProviders and SearchableContainers [skip ci] ([#2418](https://github.com/Wynntils/Artemis/issues/2418)) ([983d50f](https://github.com/Wynntils/Artemis/commit/983d50f03ab13d00e5edbb51c41827979420c15e))
* Parse territory items, add territory item properties, advanced search for territory list [skip ci] ([#2421](https://github.com/Wynntils/Artemis/issues/2421)) ([7d337ed](https://github.com/Wynntils/Artemis/commit/7d337ed80692d329085cb136537ed0df60f53786))


### Bug Fixes

* Make Wynntils much smarter about user info requests to greatly reduce Athena load ([#2422](https://github.com/Wynntils/Artemis/issues/2422)) ([4681f99](https://github.com/Wynntils/Artemis/commit/4681f998bf38b1b8c17dd0bbe66ae8a473e40baf))
* Parse awakened bar & mask correctly [skip ci] ([#2402](https://github.com/Wynntils/Artemis/issues/2402)) ([80c0aa9](https://github.com/Wynntils/Artemis/commit/80c0aa94525b968195d6432f37ec1ed4f5cdc7c9))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#2403](https://github.com/Wynntils/Artemis/issues/2403)) ([fab7e3f](https://github.com/Wynntils/Artemis/commit/fab7e3f80f976289ee66e9137962e6e96bfa13e3))
* [auto-generated] Update urls.json [ci skip] ([#2413](https://github.com/Wynntils/Artemis/issues/2413)) ([3b2f44d](https://github.com/Wynntils/Artemis/commit/3b2f44d5f28769e6de3613896269b90ce40d36ae))
* **release:** v1.1.12 [skip ci] ([230bc0b](https://github.com/Wynntils/Artemis/commit/230bc0bc0dfe24246973a4bdcf37ef1800b25d6a))


### Code Refactoring

* Container design rehaul [skip ci] ([#2419](https://github.com/Wynntils/Artemis/issues/2419)) ([0297eeb](https://github.com/Wynntils/Artemis/commit/0297eebb4f3ee32f748412ecc8cf1138a4c6b72a))
* Remove InteractiveContainerType and replace with WynncraftContainers [skip ci] ([#2416](https://github.com/Wynntils/Artemis/issues/2416)) ([0e1027e](https://github.com/Wynntils/Artemis/commit/0e1027e5e76523435e14ea2a82b733f56bf33d31))
* Sort ItemAnnotators into Game and Gui annotators [skip ci] ([#2420](https://github.com/Wynntils/Artemis/issues/2420)) ([efe98de](https://github.com/Wynntils/Artemis/commit/efe98de5414c7d020a012bdc19c5f0b30a5cc920))

