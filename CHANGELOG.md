## [1.2.17](https://github.com/Wynntils/Artemis/compare/v1.2.16...v1.2.17) (2024-05-29)


### New Features

* Settings Screen Redesign ([#2335](https://github.com/Wynntils/Artemis/issues/2335)) ([f0a73be](https://github.com/Wynntils/Artemis/commit/f0a73be93fe14f576b7242bdb0fd759521645dd9))
* Start using MapData for some providers [skip ci] ([#2536](https://github.com/Wynntils/Artemis/issues/2536)) ([d5598ed](https://github.com/Wynntils/Artemis/commit/d5598edaac6d0a5d04db4cc5e4c9c7a19a6c5f7b))


### Miscellaneous Chores

* Revert "feat: Start using MapData for some providers [skip ci]" [skip ci] ([#2538](https://github.com/Wynntils/Artemis/issues/2538)) ([7940283](https://github.com/Wynntils/Artemis/commit/794028369a209b1b9ca9ff175021bda599c71033)), closes [#2536](https://github.com/Wynntils/Artemis/issues/2536)

## [1.2.16](https://github.com/Wynntils/Artemis/compare/v1.2.15...v1.2.16) (2024-05-28)


### Bug Fixes

* Remove "Optional" from map level hover ([#2535](https://github.com/Wynntils/Artemis/issues/2535)) ([3de3c07](https://github.com/Wynntils/Artemis/commit/3de3c0785ea10d9f8f01f0c0ef18959072638043))
* Set inStream to false in WorldStateModel when changing world state [skip ci] ([#2534](https://github.com/Wynntils/Artemis/issues/2534)) ([c5e75ae](https://github.com/Wynntils/Artemis/commit/c5e75ae06256201da72a3e6dfe625980e4f877fb))


### Miscellaneous Chores

* **release:** v1.2.16 [skip ci] ([e5f6145](https://github.com/Wynntils/Artemis/commit/e5f61452bf09b45a024dca1f12e297e299fb6cd3))


### Code Refactoring

* Better handling of MapData attributes [skip ci] ([#2494](https://github.com/Wynntils/Artemis/issues/2494)) ([9cb5ae9](https://github.com/Wynntils/Artemis/commit/9cb5ae970a00b3eebbaed6cb9c7303ef51506a4d))

## [1.2.15](https://github.com/Wynntils/Artemis/compare/v1.2.14...v1.2.15) (2024-05-27)


### New Features

* Add guild objective functions [skip ci] ([#2526](https://github.com/Wynntils/Artemis/issues/2526)) ([333933a](https://github.com/Wynntils/Artemis/commit/333933a5135b96035b7afffc46fa215b6f2fafbd))
* Overlay Management visual enhancements [skip ci] ([#2531](https://github.com/Wynntils/Artemis/issues/2531)) ([02f61ef](https://github.com/Wynntils/Artemis/commit/02f61ef82d15a2b2733885a8626916a4e6620448))


### Bug Fixes

* Fix charms being counted as defective, if they had no identifications [skip ci] ([#2530](https://github.com/Wynntils/Artemis/issues/2530)) ([153ff14](https://github.com/Wynntils/Artemis/commit/153ff1438cd63f5a7eb064e7cd278cbcd6e8a170))
* Fix CustomPlayerListOverlay below the boss bars [skip ci] ([#2527](https://github.com/Wynntils/Artemis/issues/2527)) ([268f48e](https://github.com/Wynntils/Artemis/commit/268f48e3565acdc0df0b34ec8db2dec32a060a8b))
* Fix tomes being counted as defective, if they had no identifications [skip ci] ([#2529](https://github.com/Wynntils/Artemis/issues/2529)) ([473a5da](https://github.com/Wynntils/Artemis/commit/473a5daf601e7011eddfbc6d00c3e005148cb385))


### Miscellaneous Chores

* **release:** v1.2.15 [skip ci] ([1b41e33](https://github.com/Wynntils/Artemis/commit/1b41e33c2093f08a100a0c77357df3f8c3378c87))


### Code Refactoring

* Make label levels Optional<Integer> in preparation for MapData ([#2532](https://github.com/Wynntils/Artemis/issues/2532)) ([1ff8e1b](https://github.com/Wynntils/Artemis/commit/1ff8e1bb2335ea42d7c1b343a3b55df8f2daf1fe))

## [1.2.14](https://github.com/Wynntils/Artemis/compare/v1.2.13...v1.2.14) (2024-05-24)


### New Features

* Add InStream function [skip ci] ([#2524](https://github.com/Wynntils/Artemis/issues/2524)) ([90e373c](https://github.com/Wynntils/Artemis/commit/90e373cbd32864469613012488bc73103662619e))
* Add Ophanim Bar Overlay [skip ci] ([#2518](https://github.com/Wynntils/Artemis/issues/2518)) ([379f1e4](https://github.com/Wynntils/Artemis/commit/379f1e42f5f0e0ed9b3242087d2b45665338de3a))
* Add option to limit number of leaderboard badges shown [skip ci] ([#2525](https://github.com/Wynntils/Artemis/issues/2525)) ([ebc5ded](https://github.com/Wynntils/Artemis/commit/ebc5deda26933beae11c8a2a832a9406257109ac))
* Allow showing territory owner [skip ci] ([#2520](https://github.com/Wynntils/Artemis/issues/2520)) ([cc907df](https://github.com/Wynntils/Artemis/commit/cc907df63406f71ba302ab509c688076ff5fa3b8))
* Highlight bombs active in current world [skip ci] ([#2516](https://github.com/Wynntils/Artemis/issues/2516)) ([75c9d9b](https://github.com/Wynntils/Artemis/commit/75c9d9b1cee9c9eff9baa4e35e9d2cb8d9c1f5f0))
* Highlight the guild you are in on the seasonal leaderboard [skip ci] ([#2519](https://github.com/Wynntils/Artemis/issues/2519)) ([d83c2d9](https://github.com/Wynntils/Artemis/commit/d83c2d94fd3875b86361bd0790e565c4d45bfe85))
* Improvements to profession highlight feature for crafting stations ([#2523](https://github.com/Wynntils/Artemis/issues/2523)) ([b183934](https://github.com/Wynntils/Artemis/commit/b183934281ea03a224e957562e7fc665857eb009))


### Bug Fixes

* Account for timezone in player commands [skip ci] ([#2517](https://github.com/Wynntils/Artemis/issues/2517)) ([845bfc7](https://github.com/Wynntils/Artemis/commit/845bfc7754527973234c282f120fad76d9562b59))
* Fix accidentally opening the territory management UI after backing from other guild related screens [skip ci] ([#2522](https://github.com/Wynntils/Artemis/issues/2522)) ([3f5936d](https://github.com/Wynntils/Artemis/commit/3f5936d19a243a4a3c34cc1f943d981185395fb0))


### Miscellaneous Chores

* **release:** v1.2.14 [skip ci] ([2664566](https://github.com/Wynntils/Artemis/commit/2664566af53f963386b4cd5c693a13c76465e626))

## [1.2.13](https://github.com/Wynntils/Artemis/compare/v1.2.12...v1.2.13) (2024-05-22)


### New Features

* Add Guild Functions ([#2515](https://github.com/Wynntils/Artemis/issues/2515)) ([c837bfc](https://github.com/Wynntils/Artemis/commit/c837bfca678b7f83f4e36234ae9128d54b6cb8a3))


### Bug Fixes

* Scroll text fully in config tiles [skip ci] ([#2513](https://github.com/Wynntils/Artemis/issues/2513)) ([f9d963a](https://github.com/Wynntils/Artemis/commit/f9d963abec0d127a674d70c1acdede0b55430c4f))


### Miscellaneous Chores

* Add Storage upfixers [skip ci] ([#2511](https://github.com/Wynntils/Artemis/issues/2511)) ([e4d1133](https://github.com/Wynntils/Artemis/commit/e4d1133c937d79713a6fe067703f2654f6410697))
* make the territory infobar pattern more strict, add tests [skip ci] ([#2514](https://github.com/Wynntils/Artemis/issues/2514)) ([d9d610a](https://github.com/Wynntils/Artemis/commit/d9d610aaa63261901d4532f4d12232164cb9d135))
* **release:** v1.2.13 [skip ci] ([75ec09c](https://github.com/Wynntils/Artemis/commit/75ec09c50db62e94e6700b4705fe51226617ceb7))


### Code Refactoring

* MapData visibility inheritance refactoring [skip ci] ([#2500](https://github.com/Wynntils/Artemis/issues/2500)) ([cfa5ee4](https://github.com/Wynntils/Artemis/commit/cfa5ee4e77491f3ec95008cbfea7f0e4bfdbdc57))

