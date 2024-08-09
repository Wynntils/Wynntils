## [2.0.0-beta.17](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.16...v2.0.0-beta.17) (2024-08-09)


### Miscellaneous Chores

* rawtypes need not be suppressed in LabelHandler ([adf02d6](https://github.com/Wynntils/Wynntils/commit/adf02d6f0343e676fd1090cbd7bb591e17f7ec15))

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

## [2.0.0-beta.13](https://github.com/Wynntils/Wynntils/compare/v2.0.0-beta.12...v2.0.0-beta.13) (2024-08-04)


### New Features

* Aspect container scrolling [skip ci] ([#2691](https://github.com/Wynntils/Wynntils/issues/2691)) ([a60869f](https://github.com/Wynntils/Wynntils/commit/a60869f9c5648d2bac0036a87537394be8903747))


### Bug Fixes

* 2.1 attack timer fixes and don't send defence levels on failed attacks [skip ci] ([#2621](https://github.com/Wynntils/Wynntils/issues/2621)) ([d691030](https://github.com/Wynntils/Wynntils/commit/d691030e63f03cc6eb855f634dd8b1c04d540a77))
* Annotate mythic aspects [skip ci] ([#2690](https://github.com/Wynntils/Wynntils/issues/2690)) ([910c928](https://github.com/Wynntils/Wynntils/commit/910c928e483b96ff35cfeec4338e96b414877575))
* Don't spam tracker update sound when tracking world events [skip ci] ([#2692](https://github.com/Wynntils/Wynntils/issues/2692)) ([06e3c2c](https://github.com/Wynntils/Wynntils/commit/06e3c2c9c605bc2fd7ab0f495b5629ab59d68d30))
* Fix fallback recognition for gears (fix 2.1 items not getting recognized) [skip ci] ([#2697](https://github.com/Wynntils/Wynntils/issues/2697)) ([87fe079](https://github.com/Wynntils/Wynntils/commit/87fe0791c129cd583d58da20e9d593c2e2deb36d))
* Fix NPC crowdsourcing, bump crowdsourcing version [skip ci] ([#2696](https://github.com/Wynntils/Wynntils/issues/2696)) ([5b9f47b](https://github.com/Wynntils/Wynntils/commit/5b9f47bd197726209405c472c5c92861e909a553))
* Fix player nametags rendering incorrectly [skip ci] ([#2693](https://github.com/Wynntils/Wynntils/issues/2693)) ([0be513a](https://github.com/Wynntils/Wynntils/commit/0be513a0be909023a2aee837231182df61b9104d))
* Parse health/mana values with "k/b/t" shorthands in them [skip ci] ([#2695](https://github.com/Wynntils/Wynntils/issues/2695)) ([2258524](https://github.com/Wynntils/Wynntils/commit/22585243211c468e7f9cf560ee1b4fc9c03e1626))


### Miscellaneous Chores

* Make StyledText[#from](https://github.com/Wynntils/Wynntils/issues/from)String support hex color codes [skip ci] ([#2694](https://github.com/Wynntils/Wynntils/issues/2694)) ([51f76b2](https://github.com/Wynntils/Wynntils/commit/51f76b26e00e9d1d5930788ea32df64d314d5b23))
* **release:** v2.0.0-beta.13 [skip ci] ([86be0ad](https://github.com/Wynntils/Wynntils/commit/86be0adf0e0cca422c5bab691c3632691f3281ce))

