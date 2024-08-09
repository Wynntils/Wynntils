## [2.0.0-beta.18](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.17...v2.0.0-beta.18) (2024-08-09)


### Bug Fixes

* Fix ChatHandler not detecting slowdown effect correctly [skip ci] ([#2715](https://github.com/Wynntils/Wynntils/issues/2715)) ([02c2000](https://github.com/Wynntils/Wynntils/commit/02c200060b3ce80ba1c8c70cc107fe462fdcec5a))


### Miscellaneous Chores

* rawtypes need not be suppressed in LabelHandler [skip ci] ([#2712](https://github.com/Wynntils/Wynntils/issues/2712)) ([54d1f72](https://github.com/Wynntils/Wynntils/commit/54d1f72df972cf64eb24da458708ca430a83ffec))
* Rebrand to Wynntils (move away from using the Artemis codename) ([#2711](https://github.com/Wynntils/Wynntils/issues/2711)) ([8f3f081](https://github.com/Wynntils/Wynntils/commit/8f3f0819f7fcce1fd90611a223f42b583dfcc977))
* Sunset the old gear chat encoding [skip ci] ([#2713](https://github.com/Wynntils/Wynntils/issues/2713)) ([2aa5694](https://github.com/Wynntils/Wynntils/commit/2aa5694eeb3ee5645ea8b3e535bbd0f2f1131b54))


### Code Refactoring

* Java 21 related refactors [skip ci] ([#2714](https://github.com/Wynntils/Wynntils/issues/2714)) ([2e283fa](https://github.com/Wynntils/Wynntils/commit/2e283fa28502f8f8e4a34d8a24db281449c807da))

## [2.0.0-beta.17](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.16...v2.0.0-beta.17) (2024-08-09)


### Miscellaneous Chores

* rawtypes need not be suppressed in LabelHandler ([adf02d6](https://github.com/Wynntils/Wynntils/commit/adf02d6f0343e676fd1090cbd7bb591e17f7ec15))
* **release:** v2.0.0-beta.17 [skip ci] ([63c92e0](https://github.com/Wynntils/Wynntils/commit/63c92e0fec78cf29092d4afd5955401f843a35d6))

## [2.0.0-beta.16](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.15...v2.0.0-beta.16) (2024-08-08)


### New Features

* Add ModMenu support for Fabric and configuration screen for NeoForge [skip ci] ([#2703](https://github.com/Wynntils/Wynntils/issues/2703)) ([ab79a4d](https://github.com/Wynntils/Wynntils/commit/ab79a4d3071fb3acdcf42d5defc4c0620740bb92))
* Localize Wynntils to 13 languages using GPT4-o (hu, nl, pl, es, fr, de, it, pt, ru, ja, ko, zh_CN, zh_TW) ([#2709](https://github.com/Wynntils/Wynntils/issues/2709)) ([227f1df](https://github.com/Wynntils/Wynntils/commit/227f1df79ca89f3bc1db9f69e50c080da99907f4))
* Parse World Event items as activities, prepare WorldEventModel/WorldEventInfo [skip ci] ([#2706](https://github.com/Wynntils/Wynntils/issues/2706)) ([925feb6](https://github.com/Wynntils/Wynntils/commit/925feb65ebabfa431943bed6c82c28264f288261))


### Bug Fixes

* Don't render overlays when F1/Hide Gui is enabled [skip ci] ([#2710](https://github.com/Wynntils/Wynntils/issues/2710)) ([f5e44a5](https://github.com/Wynntils/Wynntils/commit/f5e44a58884da78f21db82efceb57f75287c9dc8))
* Try to parse a crafted items' name from the item, if it's not in the lore [skip ci] ([#2708](https://github.com/Wynntils/Wynntils/issues/2708)) ([8d74aea](https://github.com/Wynntils/Wynntils/commit/8d74aeacd9fdd16b499d6f756ccb9c6de0d26713))


### Miscellaneous Chores

* **release:** v2.0.0-beta.16 [skip ci] ([c26ef55](https://github.com/Wynntils/Wynntils/commit/c26ef557bb699901e2bc5b55849b7c7ec44b1f89))

## [2.0.0-beta.15](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.14...v2.0.0-beta.15) (2024-08-06)


### New Features

* Add better support for confirmationless dialogues ([#2687](https://github.com/Wynntils/Wynntils/issues/2687)) ([5c74f7e](https://github.com/Wynntils/Wynntils/commit/5c74f7ecd8f95f7c14cbde24107ff21b88a96dc0))


### Miscellaneous Chores

* Fix comment typos [skip ci] ([#2704](https://github.com/Wynntils/Wynntils/issues/2704)) ([4c0226f](https://github.com/Wynntils/Wynntils/commit/4c0226f60239bb6bda806d3109da5721f20ff0df))
* **release:** v2.0.0-beta.15 [skip ci] ([9ccfa67](https://github.com/Wynntils/Wynntils/commit/9ccfa6790be11656639d2d30a6692cbd58129451))


### Code Refactoring

* Move ChatMessageReceivedEvent and ClientsideMessageEvent to StyledText (only) [skip ci] ([#2684](https://github.com/Wynntils/Wynntils/issues/2684)) ([c18dcf6](https://github.com/Wynntils/Wynntils/commit/c18dcf65c8a2647c491ad4fd44b18c9fb591b2f2))

## [2.0.0-beta.14](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.13...v2.0.0-beta.14) (2024-08-05)


### New Features

* Add MappingProgressFeature (for development purposes) [skip ci] ([#2701](https://github.com/Wynntils/Wynntils/issues/2701)) ([1ac6bab](https://github.com/Wynntils/Wynntils/commit/1ac6bab01c5fe437e23fd544aa73976344e4e665))


### Bug Fixes

* Fix annotating tier 4 aspects [skip ci] ([#2699](https://github.com/Wynntils/Wynntils/issues/2699)) ([ebbb755](https://github.com/Wynntils/Wynntils/commit/ebbb7552f8e330c91ea4efe455cc0f039c65f176))
* Fix Ophanim bar overlay with mythic aspects and add tracking for 4th shaman totem [skip ci] ([#2700](https://github.com/Wynntils/Wynntils/issues/2700)) ([7831ba8](https://github.com/Wynntils/Wynntils/commit/7831ba8d79104a716f26a8a3ad67f61a70785068))
* Fix raid tracking for 2.1 ([#2662](https://github.com/Wynntils/Wynntils/issues/2662)) ([f40f626](https://github.com/Wynntils/Wynntils/commit/f40f626694eb79f3b47d8aab7c37b82b48277742))


### Miscellaneous Chores

* **release:** v2.0.0-beta.14 [skip ci] ([fefdae6](https://github.com/Wynntils/Wynntils/commit/fefdae60cb66a3dcad43987b4c0acb44fbfc216b))
* Require Fabric Loader 0.16.0+ and stable NeoForge [skip ci] ([#2698](https://github.com/Wynntils/Wynntils/issues/2698)) ([950cd56](https://github.com/Wynntils/Wynntils/commit/950cd56956c6125739f70f305867b13b68e4d7f1))
* update urls.json for lr tasks [skip ci] ([1e78b75](https://github.com/Wynntils/Wynntils/commit/1e78b7551f14472804df94c9300bf5ff08b7b31d))

