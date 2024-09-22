## [2.3.3](https://github.com/Wynntils/Wynntils/compare/v2.3.2...v2.3.3) (2024-09-22)


### New Features

* inventory interaction improvements and desync fixes ([#2871](https://github.com/Wynntils/Wynntils/issues/2871)) ([8966795](https://github.com/Wynntils/Wynntils/commit/8966795aaae1fdf503db74c9f3a39dfa7825e97c))


### Bug Fixes

* Fix a race between UrlManager and CoreComponent initialization [skip ci] ([#2885](https://github.com/Wynntils/Wynntils/issues/2885)) ([17863fa](https://github.com/Wynntils/Wynntils/commit/17863fa804e43abc30750bbf7c852d8d9b4797cb))

## [2.3.2](https://github.com/Wynntils/Wynntils/compare/v2.3.1...v2.3.2) (2024-09-21)


### New Features

* Add functions for damage in raids, use in raid overlay ([#2882](https://github.com/Wynntils/Wynntils/issues/2882)) ([fba6976](https://github.com/Wynntils/Wynntils/commit/fba697633bd9d27c4a015bf66d785a29a6618d2a))


### Bug Fixes

* Time Rift, Pandemonium, and overall totem performance fixes [skip ci] ([#2863](https://github.com/Wynntils/Wynntils/issues/2863)) ([d04a565](https://github.com/Wynntils/Wynntils/commit/d04a5655fb2d473ea1c5c4102de82bc5c5096400))


### Miscellaneous Chores

* **release:** v2.3.2 [skip ci] ([75d38a4](https://github.com/Wynntils/Wynntils/commit/75d38a492387993b5a88f6e5a5e91a97d5d16000))

## [2.3.1](https://github.com/Wynntils/Wynntils/compare/v2.3.0...v2.3.1) (2024-09-19)


### Bug Fixes

* Do not send nor display crafted item names from item encodings ([#2877](https://github.com/Wynntils/Wynntils/issues/2877)) ([13509a1](https://github.com/Wynntils/Wynntils/commit/13509a14038c366fe8af3c1405b2beacf0563589))


### Miscellaneous Chores

* **release:** v2.3.1 [skip ci] ([b5272e9](https://github.com/Wynntils/Wynntils/commit/b5272e930d3556eb29453212c10689717dfcf607))

## [2.3.0](https://github.com/Wynntils/Wynntils/compare/v2.2.4...v2.3.0) (2024-09-19)


### ⚠ BREAKING CHANGES

* Introduce the new data dependency loading systems (#2845)

### New Features

* Introduce the new data dependency loading systems ([#2845](https://github.com/Wynntils/Wynntils/issues/2845)) ([1d25ab5](https://github.com/Wynntils/Wynntils/commit/1d25ab5166c3a9cba4fa369afcae9c68261246a2)), closes [#2797](https://github.com/Wynntils/Wynntils/issues/2797)


### Miscellaneous Chores

* **release:** v2.3.0 [skip ci] ([ca5cbf8](https://github.com/Wynntils/Wynntils/commit/ca5cbf8fbe39cdaca59cd037ae057e4b165ada0d))

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
* **release:** v2.2.4 [skip ci] ([3c54a16](https://github.com/Wynntils/Wynntils/commit/3c54a16cb7040d5ab915e3c2806e0438d3619039))
* Swap save and cancel buttons on colour picker [skip ci] ([#2869](https://github.com/Wynntils/Wynntils/issues/2869)) ([8f6be26](https://github.com/Wynntils/Wynntils/commit/8f6be26ee359d8d5b15890829931aef4d6e185bf))


### Code Refactoring

* Don't require onConfigUpdate to be overridden in overlays [skip ci] ([#2874](https://github.com/Wynntils/Wynntils/issues/2874)) ([e2252e6](https://github.com/Wynntils/Wynntils/commit/e2252e67c00737530a8ddc77cc27d19ac486dff0))

