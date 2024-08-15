## [2.0.6](https://github.com/Wynntils/Wynntils/compare/v2.0.5...v2.0.6) (2024-08-15)


### Bug Fixes

* Add /party lobby to command autocompletion [skip ci] ([#2748](https://github.com/Wynntils/Wynntils/issues/2748)) ([9044c74](https://github.com/Wynntils/Wynntils/commit/9044c746c351a664992d662ab62f6611564caa8b))
* Fix ItemFavoriteFeature and ItemLockFeature overlapping tooltips [skip ci] ([#2747](https://github.com/Wynntils/Wynntils/issues/2747)) ([e22e216](https://github.com/Wynntils/Wynntils/commit/e22e21619934fd50146a484ea2743e6e914e1044))
* Make RecipientType work with multiline messages [skip ci] ([#2749](https://github.com/Wynntils/Wynntils/issues/2749)) ([00bd563](https://github.com/Wynntils/Wynntils/commit/00bd563a96fcc84decaaf3afc0b02e8ffc98b9d3))

## [2.0.5](https://github.com/Wynntils/Wynntils/compare/v2.0.4...v2.0.5) (2024-08-15)


### New Features

* Functions for different resources [skip ci] ([#2741](https://github.com/Wynntils/Wynntils/issues/2741)) ([7e65d67](https://github.com/Wynntils/Wynntils/commit/7e65d67447cbb6845f7ee3ba292ac3fc27a2e504))


### Bug Fixes

* Correctly categorize 2.1 messages in chat tabs (RecipientType fixes) ([#2745](https://github.com/Wynntils/Wynntils/issues/2745)) ([1b5b965](https://github.com/Wynntils/Wynntils/commit/1b5b9659e5ce83eeb1100ae369ec4d527029057e))
* Fix pack preloading getting users stuck in class selection [skip ci] ([#2744](https://github.com/Wynntils/Wynntils/issues/2744)) ([faf09d3](https://github.com/Wynntils/Wynntils/commit/faf09d3ef859f0e335d38577afd4c5e00edd5873))
* Fix TradeMarketPriceConversionFeature [skip ci] ([#2738](https://github.com/Wynntils/Wynntils/issues/2738)) ([b2f76f0](https://github.com/Wynntils/Wynntils/commit/b2f76f06787739f33ad04cb94b27afb3471fbc7d))
* Fix unwrapping related message bugs (in RemoveWynncraftChatWrapFeature) [skip ci] ([#2743](https://github.com/Wynntils/Wynntils/issues/2743)) ([7cfac52](https://github.com/Wynntils/Wynntils/commit/7cfac52b93c0c65000c24dc435c92e5903d1a163))
* Fix widgets with scrolling text not being masked [skip ci] ([#2723](https://github.com/Wynntils/Wynntils/issues/2723)) ([f689655](https://github.com/Wynntils/Wynntils/commit/f68965552df01b1503273fbc9869da7001539b34))
* Move emeralds higher to avoid clipping [skip ci] ([#2740](https://github.com/Wynntils/Wynntils/issues/2740)) ([6f99858](https://github.com/Wynntils/Wynntils/commit/6f99858bf0a1d5c1c8402b1a2a990f6f84313044))
* Refine crafted item parsing, handle more edge-cases [skip ci] ([#2742](https://github.com/Wynntils/Wynntils/issues/2742)) ([f7cbe72](https://github.com/Wynntils/Wynntils/commit/f7cbe7283170b748a84d8f86a1fde6020f89cb20))


### Miscellaneous Chores

* **release:** v2.0.5 [skip ci] ([04e5dbf](https://github.com/Wynntils/Wynntils/commit/04e5dbff0977c9deeaf50a59fde88ed4b61dc526))

## [2.0.4](https://github.com/Wynntils/Wynntils/compare/v2.0.3...v2.0.4) (2024-08-14)


### New Features

* Add world event container as a reward container [skip ci] ([#2737](https://github.com/Wynntils/Wynntils/issues/2737)) ([052bb5b](https://github.com/Wynntils/Wynntils/commit/052bb5ba21c0e0721360d0d3c774096ddc410291))


### Bug Fixes

* 2.1 Server annotator pattern [skip ci] ([#2739](https://github.com/Wynntils/Wynntils/issues/2739)) ([8cb53cb](https://github.com/Wynntils/Wynntils/commit/8cb53cb504964d62d58d927ed5ef4ac9fb9275a1))
* Accessories not detected properly by GearType [skip ci] ([#2736](https://github.com/Wynntils/Wynntils/issues/2736)) ([a8847e2](https://github.com/Wynntils/Wynntils/commit/a8847e2495525c1092d7e59cef6c428cfe97b6f5))
* Don't send unexpected disconnect on IO thread ([#2492](https://github.com/Wynntils/Wynntils/issues/2492)) ([bd798b1](https://github.com/Wynntils/Wynntils/commit/bd798b10a9fa9691f48d3e27d5fbf6c086e0d11f))


### Miscellaneous Chores

* **release:** v2.0.4 [skip ci] ([83a2b14](https://github.com/Wynntils/Wynntils/commit/83a2b14b0ec1bcbc49821547a5fd01be411945ba))

## [2.0.3](https://github.com/Wynntils/Wynntils/compare/v2.0.2...v2.0.3) (2024-08-12)


### New Features

* Add RemoveWynncraftChatWrapFeature and fix ChatItemFeature not working in party/guild/private chat ([#2734](https://github.com/Wynntils/Wynntils/issues/2734)) ([725781a](https://github.com/Wynntils/Wynntils/commit/725781a1facf8c33ca81ba73229954236fcb75af))


### Bug Fixes

* Correctly set game version requirement tags on Modrinth and CurseForge [skip ci] ([#2732](https://github.com/Wynntils/Wynntils/issues/2732)) ([5838da2](https://github.com/Wynntils/Wynntils/commit/5838da29f7d990bfeaa91d5f37d4c845206aa434))
* Fix CustomSeaskipperScreenFeature crashing the game [skip ci] ([#2733](https://github.com/Wynntils/Wynntils/issues/2733)) ([d3bfd88](https://github.com/Wynntils/Wynntils/commit/d3bfd887b04c44049e2874037e1698b9a6e2c943))
* Reset current container if on a container screen [skip ci] ([#2731](https://github.com/Wynntils/Wynntils/issues/2731)) ([a7950c3](https://github.com/Wynntils/Wynntils/commit/a7950c35b4e6827a053874627c4c78cba7b1ca70))


### Miscellaneous Chores

* **release:** v2.0.3 [skip ci] ([a498c63](https://github.com/Wynntils/Wynntils/commit/a498c632e98b4308dd06dd0a9d454b454fe01196))

## [2.0.2](https://github.com/Wynntils/Wynntils/compare/v2.0.1...v2.0.2) (2024-08-11)


### Bug Fixes

* Fix ReplayMod causing container features to not work [skip ci] ([#2729](https://github.com/Wynntils/Wynntils/issues/2729)) ([7856d2f](https://github.com/Wynntils/Wynntils/commit/7856d2fb8f4bcc0eb644f90fcb3fac0cc4083b0e))
* Fix TradeMarketAutoOpenChatFeature [skip ci] ([#2727](https://github.com/Wynntils/Wynntils/issues/2727)) ([417b9f2](https://github.com/Wynntils/Wynntils/commit/417b9f2b22b241c48e288c0b8187988e2e315152))
* Fix tutorial content book ([#2730](https://github.com/Wynntils/Wynntils/issues/2730)) ([721e8c6](https://github.com/Wynntils/Wynntils/commit/721e8c6a32604d102b2a9c947b01e17176131ee1))
* Update bank page names after setting/resetting [skip ci] ([#2728](https://github.com/Wynntils/Wynntils/issues/2728)) ([86feb4e](https://github.com/Wynntils/Wynntils/commit/86feb4ee7c9b94937d375b543f1ff3aaf139dbea))


### Miscellaneous Chores

* **release:** v2.0.2 [skip ci] ([60e0998](https://github.com/Wynntils/Wynntils/commit/60e0998937a3253f1ce20a41c1a57656680dfcee))

