## [1.1.4](https://github.com/Wynntils/Artemis/compare/v1.1.3...v1.1.4) (2024-03-20)


### New Features

* Skill point loadouts smooth scrolling ([#2348](https://github.com/Wynntils/Artemis/issues/2348)) ([04395e5](https://github.com/Wynntils/Artemis/commit/04395e57a6971c9dfa1c57c68b25f0bbd2088c6e))

## [1.1.3](https://github.com/Wynntils/Artemis/compare/v1.1.2...v1.1.3) (2024-03-17)


### New Features

* Add favorite filter [skip ci] ([#2376](https://github.com/Wynntils/Artemis/issues/2376)) ([2f55163](https://github.com/Wynntils/Artemis/commit/2f55163b1d773429fc96b863777ff8a14c7a5d73))
* Add initial new MapData service [skip ci] ([#2158](https://github.com/Wynntils/Artemis/issues/2158)) ([2929a43](https://github.com/Wynntils/Artemis/commit/2929a43b0ce248f3c60772b5b4698115f0ef66c9))
* Add StatusEffect function [skip ci] ([#2368](https://github.com/Wynntils/Artemis/issues/2368)) ([c5abcc5](https://github.com/Wynntils/Artemis/commit/c5abcc55e31dafeef92802af6312359beaf342b6))


### Bug Fixes

* fix DestinationService throwing NPE if destinations fail to load [skip ci] ([#2374](https://github.com/Wynntils/Artemis/issues/2374)) ([d03fcd6](https://github.com/Wynntils/Artemis/commit/d03fcd61d348c4aa394b4748c0ffe1d61114c473))
* fix lootrun started message not matching the coordinate pattern [skip ci] ([#2370](https://github.com/Wynntils/Artemis/issues/2370)) ([a188818](https://github.com/Wynntils/Artemis/commit/a188818fe02a77fe37996e3f6e401fc47c6978ad))
* Incorrect price lines on certain bulk buyable items [skip ci] ([#2371](https://github.com/Wynntils/Artemis/issues/2371)) ([de0a9cf](https://github.com/Wynntils/Artemis/commit/de0a9cf00c498a83b9a37b626fe61d7432cbcd6e))
* Prevent right clicking helper button in guides clearing input [skip ci] ([#2372](https://github.com/Wynntils/Artemis/issues/2372)) ([4b5e602](https://github.com/Wynntils/Artemis/commit/4b5e602651328996430f349ed9a924a36d334806))
* Staff join messages not redirecting [skip ci] ([#2369](https://github.com/Wynntils/Artemis/issues/2369)) ([ac9998e](https://github.com/Wynntils/Artemis/commit/ac9998ef37ff82791055dd4335af23691e5eca56))


### Miscellaneous Chores

* **release:** v1.1.3 [skip ci] ([21292fd](https://github.com/Wynntils/Artemis/commit/21292fd9ea9d166fcc4f6dc1054889fa7c208c11))

## [1.1.2](https://github.com/Wynntils/Artemis/compare/v1.1.1...v1.1.2) (2024-03-12)


### Bug Fixes

* Make translations work with NPC dialogues ([#2362](https://github.com/Wynntils/Artemis/issues/2362)) ([dd0360e](https://github.com/Wynntils/Artemis/commit/dd0360e195d33e26e9e00e8a803f4ceee1db42ba))


### Miscellaneous Chores

* **release:** v1.1.2 [skip ci] ([5695e25](https://github.com/Wynntils/Artemis/commit/5695e25d281eae1772347d6c9009d225cf44da0c))

## [1.1.1](https://github.com/Wynntils/Artemis/compare/v1.1.0...v1.1.1) (2024-03-11)


### New Features

* Add support for events in string-parsed StyledTexts [skip ci] ([#2365](https://github.com/Wynntils/Artemis/issues/2365)) ([c54326f](https://github.com/Wynntils/Artemis/commit/c54326f79718ab62ee0df92abc9027ebdb99bdea))
* Custom Seaskipper screen redesign [skip ci] ([#2364](https://github.com/Wynntils/Artemis/issues/2364)) ([1dda1cf](https://github.com/Wynntils/Artemis/commit/1dda1cfbce9944b21fb6775552d9a560690c571e))


### Bug Fixes

* Fix NotificationManager not displaying notifications if the Game Notification overlay is disabled, but the parent feature is enabled [skip ci] ([#2361](https://github.com/Wynntils/Artemis/issues/2361)) ([7f27f28](https://github.com/Wynntils/Artemis/commit/7f27f2869bc733c2c20008df5eb84cfa137c8b22))
* Fix StatusEffectsOverlay rendering some status effects without respecting the font size setting [skip ci] ([#2360](https://github.com/Wynntils/Artemis/issues/2360)) ([12f462a](https://github.com/Wynntils/Artemis/commit/12f462a4e854a8fcbcc918da7a3eadc09b2dc3f5))
* Refactor multi-line message handling in ChatHandler, fix multi-line messages not appearing twice, even if they should ([#2366](https://github.com/Wynntils/Artemis/issues/2366)) ([8e767f7](https://github.com/Wynntils/Artemis/commit/8e767f7c9b0de486402f6038951a9597b8c4669b))
* Wait even more in CommandHandler when trying to execute a command during a dialogue [skip ci] ([#2367](https://github.com/Wynntils/Artemis/issues/2367)) ([07f7a81](https://github.com/Wynntils/Artemis/commit/07f7a811a2826d38303d31fb389e075d24a44489))


### Miscellaneous Chores

* **release:** v1.1.1 [skip ci] ([d5c8521](https://github.com/Wynntils/Artemis/commit/d5c852198ac2476c23488e2148593870b387714f))


### Code Refactoring

* Make TranscribeMessagesFeature use StyledText to produce a translator-friendly output [skip ci] ([#2363](https://github.com/Wynntils/Artemis/issues/2363)) ([f096ee4](https://github.com/Wynntils/Artemis/commit/f096ee4a92249c29d98e6594396cc6685b5aec06))
* Turn SearchableContainerType into InteractiveContainerType [skip ci] ([#2353](https://github.com/Wynntils/Artemis/issues/2353)) ([98df8d1](https://github.com/Wynntils/Artemis/commit/98df8d1d563e10b56210db80a4440de0f08d65c1))

## [1.1.0](https://github.com/Wynntils/Artemis/compare/v1.0.13...v1.1.0) (2024-03-06)


### ⚠ BREAKING CHANGES

* Make NpcDialogueFeature work in a chat-based mode, add legacy chat mode, fix many NPC dialogue related bugs, make the overlay work once again (#2358)

### New Features

* Make NpcDialogueFeature work in a chat-based mode, add legacy chat mode, fix many NPC dialogue related bugs, make the overlay work once again ([#2358](https://github.com/Wynntils/Artemis/issues/2358)) ([c58214f](https://github.com/Wynntils/Artemis/commit/c58214f52fc584e8343bfce0ddf24b703d3dc5e1))


### Bug Fixes

* Add a versions file for CI ([cafeeaf](https://github.com/Wynntils/Artemis/commit/cafeeaf7398a1f79cf05f4f73014479966972038))
* Fix some bugs in dialogue parsing (in ChatHandler) [ci skip] ([#2357](https://github.com/Wynntils/Artemis/issues/2357)) ([c2d2c16](https://github.com/Wynntils/Artemis/commit/c2d2c163707e683aa1cf4d5ded9d40482ac56005))
* Fix some bugs in dialogue parsing (in ChatHandler) [ci skip] ([#2357](https://github.com/Wynntils/Artemis/issues/2357)) ([54fed73](https://github.com/Wynntils/Artemis/commit/54fed73b4c6f757e982124c074813b2149e5859f))
* Fix TradeMarketScreen crashing the game if the user closes an ItemSharingScreen ([#2354](https://github.com/Wynntils/Artemis/issues/2354)) ([4fc0a07](https://github.com/Wynntils/Artemis/commit/4fc0a070d5d073c73cf934d523f94acf1e53a544))
* Fix TradeMarketScreen crashing the game if the user closes an ItemSharingScreen ([#2354](https://github.com/Wynntils/Artemis/issues/2354)) ([07a8613](https://github.com/Wynntils/Artemis/commit/07a8613177b293bc1a47a1be6ddf2ed34a5b5064))
* fix versioning by adding a version.json file for release branch ([1c6c3d8](https://github.com/Wynntils/Artemis/commit/1c6c3d859f11f0802d365c7b8094b166a1f1ebb3))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#2352](https://github.com/Wynntils/Artemis/issues/2352)) ([9286e50](https://github.com/Wynntils/Artemis/commit/9286e505d616d0022cf9bc9a12c9d1f1a3b20ad1))
* [auto-generated] Update urls.json [ci skip] ([#2352](https://github.com/Wynntils/Artemis/issues/2352)) ([70ba0da](https://github.com/Wynntils/Artemis/commit/70ba0daf57e775fc7004b4f125a01147f27719e1))
* **release:** v1.0.14 [skip ci] ([a91ad35](https://github.com/Wynntils/Artemis/commit/a91ad35e4c998f8f08c30283c2910804783e349c))
* **release:** v1.1.0 [skip ci] ([f879a17](https://github.com/Wynntils/Artemis/commit/f879a17abc264e23360b4def6c1d4d2e145df36d))
* Remove unused UrlIds ([#2355](https://github.com/Wynntils/Artemis/issues/2355)) ([4c54247](https://github.com/Wynntils/Artemis/commit/4c54247a48a1737fba7d81fb1d6655e855fc7735))

