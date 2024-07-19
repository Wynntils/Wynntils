## [2.0.0-beta.9](https://github.com/Wynntils/Artemis/compare/v2.0.0-beta.8...v2.0.0-beta.9) (2024-07-19)


### New Features

* Improve chat tab switching hotkeys [skip ci] ([#2664](https://github.com/Wynntils/Artemis/issues/2664)) ([3660b84](https://github.com/Wynntils/Artemis/commit/3660b8444576e41b7eedd1dc85a56942eb596b1c))
* Make the Wynncraft health and mana bars the default [skip ci] ([#2666](https://github.com/Wynntils/Artemis/issues/2666)) ([9c8ea7b](https://github.com/Wynntils/Artemis/commit/9c8ea7bada2e7653a91de5e52035ce9be580994c))


### Bug Fixes

* Fix everything lootrun related for 2.1 ([#2665](https://github.com/Wynntils/Artemis/issues/2665)) ([7b51ae4](https://github.com/Wynntils/Artemis/commit/7b51ae47f3cf1e3bde421371870ff3f9abad3fd3))
* Fix time spent in raid stats [skip ci] ([#2663](https://github.com/Wynntils/Artemis/issues/2663)) ([441b8f4](https://github.com/Wynntils/Artemis/commit/441b8f463ab5d191afd753726a2a89eebbd41cec))
* Parse activities correctly (after 2.1 patch [#1](https://github.com/Wynntils/Artemis/issues/1)) [skip ci] ([#2667](https://github.com/Wynntils/Artemis/issues/2667)) ([3858321](https://github.com/Wynntils/Artemis/commit/3858321a830ae566e9aa44811640ba20926ac104))

## [2.0.0-beta.8](https://github.com/Wynntils/Artemis/compare/v2.0.0-beta.7...v2.0.0-beta.8) (2024-07-13)


### New Features

* Change the "purpose" of EntityLabelEvent to no longer do label matching [skip ci] ([#2659](https://github.com/Wynntils/Artemis/issues/2659)) ([9ec4990](https://github.com/Wynntils/Artemis/commit/9ec499028cfbdea6868d62f1fa9e35f18504873a))
* Port DamageModel to TextDisplay labels [skip ci] ([#2654](https://github.com/Wynntils/Artemis/issues/2654)) ([9ffef5e](https://github.com/Wynntils/Artemis/commit/9ffef5eeaf7a0c22a022ef3570d25849162ee37d))
* Port ExtendedSeasonLeaderboardFeature to TextDisplay (+ pass LabelInfo in TextDisplayChangedEvent) [skip ci] ([#2655](https://github.com/Wynntils/Artemis/issues/2655)) ([937c18a](https://github.com/Wynntils/Artemis/commit/937c18a2ced5a5981950ca46a8b316ca70c98e4a))
* Port LabelHandler to TextDisplay (without porting usages) [skip ci] ([#2653](https://github.com/Wynntils/Artemis/issues/2653)) ([4bc1410](https://github.com/Wynntils/Artemis/commit/4bc14106e9a52303803df903f300bcb5d889a9c3))
* Port MobTotemModel to TextDisplay [skip ci] ([#2658](https://github.com/Wynntils/Artemis/issues/2658)) ([531ceb6](https://github.com/Wynntils/Artemis/commit/531ceb68e7b48f964cf7701023b8fa710104eabb))
* Port ProfessionModel to TextDisplay [skip ci] ([#2656](https://github.com/Wynntils/Artemis/issues/2656)) ([27140f5](https://github.com/Wynntils/Artemis/commit/27140f507f15ea5b980274ab863b29ef980c409c))
* Port ShamanTotemModel to TextDisplay (tracking still broken) [skip ci] ([#2657](https://github.com/Wynntils/Artemis/issues/2657)) ([d3f5c18](https://github.com/Wynntils/Artemis/commit/d3f5c18af5ecf297cf5bac75414f1b56dcce2bd7))


### Bug Fixes

* Fix quick jump issues [skip ci] ([#2651](https://github.com/Wynntils/Artemis/issues/2651)) ([6a1b85c](https://github.com/Wynntils/Artemis/commit/6a1b85cc7f049eb6e0461eeeef6b3a5084ae32a6))
* Improve reliability of diplomacy tracking [skip ci] ([#2652](https://github.com/Wynntils/Artemis/issues/2652)) ([4eca9fd](https://github.com/Wynntils/Artemis/commit/4eca9fd3bb9e08406d3e50157b1be0b95f45dbcb))
* Update all friend and party regexes ([#2660](https://github.com/Wynntils/Artemis/issues/2660)) ([70db660](https://github.com/Wynntils/Artemis/commit/70db660c09b16c70795d6fe5b1cc17d4a9a101b9))


### Miscellaneous Chores

* **release:** v2.0.0-beta.8 [skip ci] ([f8edc20](https://github.com/Wynntils/Artemis/commit/f8edc2013116be658b6c7ca3a0e234b194614f8a))
* Update dependency versions [skip ci] ([d70a1fd](https://github.com/Wynntils/Artemis/commit/d70a1fd78ae53b12984e328fad6fb96ba89dccaf))

## [2.0.0-beta.7](https://github.com/Wynntils/Artemis/compare/v2.0.0-beta.6...v2.0.0-beta.7) (2024-07-10)


### Bug Fixes

* Fix BufferSource memory leaks ([#2650](https://github.com/Wynntils/Artemis/issues/2650)) ([d3a1f99](https://github.com/Wynntils/Artemis/commit/d3a1f998184c4ca5fc40ebf01a7c78c1e96e1674))
* Fix health bar parsing when health is "critical" [skip ci] ([#2647](https://github.com/Wynntils/Artemis/issues/2647)) ([1bc634c](https://github.com/Wynntils/Artemis/commit/1bc634cff085bae2ac7efdc0b33fd914de3c551b))
* Fix MaterialAnnotator crashing when trying to look up "Unprocessed" items [skip ci] ([#2648](https://github.com/Wynntils/Artemis/issues/2648)) ([2d6b789](https://github.com/Wynntils/Artemis/commit/2d6b789214e7121db8ff04a404a9b8a7b334f822))
* SkillPointModel port to beta ([#2649](https://github.com/Wynntils/Artemis/issues/2649)) ([94ea9dc](https://github.com/Wynntils/Artemis/commit/94ea9dc061576ffadaa415d8e1a9b3ef1a8bfc40))


### Miscellaneous Chores

* **release:** v2.0.0-beta.7 [skip ci] ([d388fcc](https://github.com/Wynntils/Artemis/commit/d388fccffd035d41a31197c570459b4ec67b7d1c))

## [2.0.0-beta.6](https://github.com/Wynntils/Artemis/compare/v2.0.0-beta.5...v2.0.0-beta.6) (2024-07-09)


### New Features

* Add ability tree reset and aspects containers [skip ci] ([#2645](https://github.com/Wynntils/Artemis/issues/2645)) ([ce453d2](https://github.com/Wynntils/Artemis/commit/ce453d26a126ae4f85ee50e3419e7f4c0ed074a6))


### Bug Fixes

* Fix crashing in character selection screen ([#2646](https://github.com/Wynntils/Artemis/issues/2646)) ([b97bc8a](https://github.com/Wynntils/Artemis/commit/b97bc8adf1f4e0c613fd227c059586ccc4235c96))


### Miscellaneous Chores

* **release:** v2.0.0-beta.6 [skip ci] ([39070f1](https://github.com/Wynntils/Artemis/commit/39070f12bf8ed0d6cf1444ee8cb6887fea9c4a5f))
* Update bank textures [skip ci] ([#2644](https://github.com/Wynntils/Artemis/issues/2644)) ([24df868](https://github.com/Wynntils/Artemis/commit/24df86851f74251089e2e2a680623ff889fca305))

## [2.0.0-beta.5](https://github.com/Wynntils/Artemis/compare/v2.0.0-beta.4...v2.0.0-beta.5) (2024-07-09)


### New Features

* Support the 2.1 action bar (custom bars, disabling parts) ([#2636](https://github.com/Wynntils/Artemis/issues/2636)) ([2abca5c](https://github.com/Wynntils/Artemis/commit/2abca5c0ef5d9e3fe1ff8d6c543fb3849eb6ba6b))


### Miscellaneous Chores

* **release:** v2.0.0-beta.5 [skip ci] ([aac8612](https://github.com/Wynntils/Artemis/commit/aac8612141eef8a36cb1099317977a3484be1468))

