## [2.2.4](https://github.com/Wynntils/Wynntils/compare/v2.2.3...v2.2.4) (2024-09-19)


### ⚠ BREAKING CHANGES

* mob health is now represented as a `long` in the `DamageModel`

* fix: fix issues with "more overlays" implementation

* Tracks focused mob state more correctly in `DamageModel`
* Moves the crosshair position fix to the "utilities" category
* Factors item cooldown out of the inventory function and into the character stats model
* Changes the fallback value for `StringUtils::SUFFIX_MULTIPLIERS` to 1

* ci: spotless formatting

* chore: move `Minecraft` access into `FixCrosshairPositionFeature::shouldOverrideCrosshair`

* fix: allow any colour mob name in `DamageModel` health bar parser

* chore: `cdPercent` -> `cooldownPercent`

* feat!: emit first boss bar progress update on bar creation in `BossBarHandler`

While this is technically a breaking change, it shouldn't break behaviour in any meaningful way; if anything, it should remove the possibility of ephemeral inconsistent states between the creation of a boss bar and the arrival of the first progress update

* fix: todo line

### New Features

* attack/spell keybind inversion [skip ci] ([#2872](https://github.com/Wynntils/Wynntils/issues/2872)) ([b150f81](https://github.com/Wynntils/Wynntils/commit/b150f814168333d299cadd7cf8988a5cfe30a44f))
* held item cooldown overlay + focused mob health overlay [skip ci] ([#2862](https://github.com/Wynntils/Wynntils/issues/2862)) ([925366a](https://github.com/Wynntils/Wynntils/commit/925366ae196233400dced36828fec9c0909b9949))
* Highlight jump button for current page [skip ci] ([#2868](https://github.com/Wynntils/Wynntils/issues/2868)) ([0aad12f](https://github.com/Wynntils/Wynntils/commit/0aad12f6859480eb74a366503d5ca7e5288499de))


### Bug Fixes

* custom loading screen fixes [skip ci] ([#2860](https://github.com/Wynntils/Wynntils/issues/2860)) ([af392d8](https://github.com/Wynntils/Wynntils/commit/af392d89af70633738a071bc7fd209ef02211231))
* Fix (Buffered)FontRenderer not calculating width/height correctly in every case [skip ci] ([#2866](https://github.com/Wynntils/Wynntils/issues/2866)) ([5b94995](https://github.com/Wynntils/Wynntils/commit/5b949953fb826cc1d5dd35826362dad95a5b5a0f))
* Fix auto join party [skip ci] ([#2867](https://github.com/Wynntils/Wynntils/issues/2867)) ([88ef6d1](https://github.com/Wynntils/Wynntils/commit/88ef6d12b1b8d6f6d367d6acf52bebc7db5cecb3))
* Fix CustomLoadingScreen Wynncraft logo position [skip ci] ([#2873](https://github.com/Wynntils/Wynntils/issues/2873)) ([deab186](https://github.com/Wynntils/Wynntils/commit/deab1862b61486077fe2c1c24e923f199487bb29))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#2864](https://github.com/Wynntils/Wynntils/issues/2864)) ([c2a905b](https://github.com/Wynntils/Wynntils/commit/c2a905b11f0896a529674684d7cf575d62a09a70))
* Swap save and cancel buttons on colour picker [skip ci] ([#2869](https://github.com/Wynntils/Wynntils/issues/2869)) ([8f6be26](https://github.com/Wynntils/Wynntils/commit/8f6be26ee359d8d5b15890829931aef4d6e185bf))


### Code Refactoring

* Don't require onConfigUpdate to be overridden in overlays [skip ci] ([#2874](https://github.com/Wynntils/Wynntils/issues/2874)) ([e2252e6](https://github.com/Wynntils/Wynntils/commit/e2252e67c00737530a8ddc77cc27d19ac486dff0))

## [2.2.3](https://github.com/Wynntils/Wynntils/compare/v2.2.2...v2.2.3) (2024-09-15)


### New Features

* Poi Creation Screen redesign [skip ci] ([#2850](https://github.com/Wynntils/Wynntils/issues/2850)) ([ab86658](https://github.com/Wynntils/Wynntils/commit/ab86658658495dbd5165d17c71ee14f06bb15bdd))
* Rewrite ChatTimestampFeature ([#2844](https://github.com/Wynntils/Wynntils/issues/2844)) ([9a0a07f](https://github.com/Wynntils/Wynntils/commit/9a0a07ff808d132124bea0413a1f175affd67b4d))


### Bug Fixes

* Don't crash when stat filters exceed the integer limit [skip ci] ([#2858](https://github.com/Wynntils/Wynntils/issues/2858)) ([5759363](https://github.com/Wynntils/Wynntils/commit/5759363dbaa4e69026319095b8e1317adf05aca8))
* Update TM bulk sell for 2.1 [skip ci] ([#2856](https://github.com/Wynntils/Wynntils/issues/2856)) ([bb4575b](https://github.com/Wynntils/Wynntils/commit/bb4575b4dd4f33ce88e65e51e10136459185c22c))


### Miscellaneous Chores

* **release:** v2.2.3 [skip ci] ([3386a49](https://github.com/Wynntils/Wynntils/commit/3386a4971c270876e5d9cd606a9962c75a9f3185))

## [2.2.2](https://github.com/Wynntils/Wynntils/compare/v2.2.1...v2.2.2) (2024-09-10)


### Bug Fixes

* Fix attack timer defense not being detected [skip ci] ([#2851](https://github.com/Wynntils/Wynntils/issues/2851)) ([d5b3658](https://github.com/Wynntils/Wynntils/commit/d5b36580119f901a3c25f4287e152814dfe67e23))
* Fix aura & volley timers ([#2853](https://github.com/Wynntils/Wynntils/issues/2853)) ([659ca98](https://github.com/Wynntils/Wynntils/commit/659ca987e4e104af7a9f498cd39370ded617d305))
* Fix blacksmith redirector [skip ci] ([#2849](https://github.com/Wynntils/Wynntils/issues/2849)) ([f4c4206](https://github.com/Wynntils/Wynntils/commit/f4c420659aa8eb3093315d57773a740fd8042df9))


### Miscellaneous Chores

* **release:** v2.2.2 [skip ci] ([a8d52dc](https://github.com/Wynntils/Wynntils/commit/a8d52dc75ed2f32844eb127371f6c4000cd0d3df))

## [2.2.1](https://github.com/Wynntils/Wynntils/compare/v2.2.0...v2.2.1) (2024-09-09)


### New Features

* Add Range Visualizer for Raid Buff Major Ids [skip ci] ([#2827](https://github.com/Wynntils/Wynntils/issues/2827)) ([edc39f7](https://github.com/Wynntils/Wynntils/commit/edc39f75db77411c45c54a0ef9c0374452a4be64))


### Bug Fixes

* Fix certain ingredients using API name not being annotated [skip ci] ([#2842](https://github.com/Wynntils/Wynntils/issues/2842)) ([24637da](https://github.com/Wynntils/Wynntils/commit/24637da6be6ab4ba1693e423abaf74a0e6c0ae7e))
* Fix info bar territory pattern ([#2848](https://github.com/Wynntils/Wynntils/issues/2848)) ([d246ce5](https://github.com/Wynntils/Wynntils/commit/d246ce5ef125c1fb5a4562c28ee8e51a4cba7b85))
* Fix lootrun challenge count [skip ci] ([#2841](https://github.com/Wynntils/Wynntils/issues/2841)) ([2c9ca74](https://github.com/Wynntils/Wynntils/commit/2c9ca74879748c7fe566697b321201e517f0e3b5))
* Fix mythic detection from lootrun reward chest [skip ci] ([#2846](https://github.com/Wynntils/Wynntils/issues/2846)) ([769fe46](https://github.com/Wynntils/Wynntils/commit/769fe4628506b3d33837b3a5750faaab7dc7b976))
* Handle null acquired and no treasury in guild map [skip ci] ([#2843](https://github.com/Wynntils/Wynntils/issues/2843)) ([721731c](https://github.com/Wynntils/Wynntils/commit/721731c2bff80a6c2ad9e1888efb30e1b5407249))


### Miscellaneous Chores

* **release:** v2.2.1 [skip ci] ([3f412b6](https://github.com/Wynntils/Wynntils/commit/3f412b6b2b90220b98834c15fcdcd1f1c41831fb))


### Code Refactoring

* dont return a pair of nulls [skip ci] ([#2847](https://github.com/Wynntils/Wynntils/issues/2847)) ([a56c194](https://github.com/Wynntils/Wynntils/commit/a56c194dcf2fb2291e0ff5736f97195a10871e6a))

## [2.2.0](https://github.com/Wynntils/Wynntils/compare/v2.1.4...v2.2.0) (2024-09-08)


### ⚠ BREAKING CHANGES

* Bump development version to 1.21.1 (and maintain 1.21 compatibility) (#2840)

### New Features

* Bump development version to 1.21.1 (and maintain 1.21 compatibility) ([#2840](https://github.com/Wynntils/Wynntils/issues/2840)) ([bc3d9ba](https://github.com/Wynntils/Wynntils/commit/bc3d9baf21b623bb5207f9ea16bba9490a25f144))


### Bug Fixes

* Fix custom territory management screen [skip ci] ([#2837](https://github.com/Wynntils/Wynntils/issues/2837)) ([6ed05ad](https://github.com/Wynntils/Wynntils/commit/6ed05ade533f55a19ddba8046308263929341b93))
* Fix issues with slot rendering [skip ci] ([#2839](https://github.com/Wynntils/Wynntils/issues/2839)) ([9923a69](https://github.com/Wynntils/Wynntils/commit/9923a69f8e6de2a2653cc76aaf84908988aa9b6e))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#2838](https://github.com/Wynntils/Wynntils/issues/2838)) ([7437500](https://github.com/Wynntils/Wynntils/commit/7437500f34509054f5e6e5ca7534b08632b50b31))
* **release:** v2.2.0 [skip ci] ([5879775](https://github.com/Wynntils/Wynntils/commit/587977544d4efa76a766acc3bc3d01cd2d84c5ca))

