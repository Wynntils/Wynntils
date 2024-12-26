## [2.4.11](https://github.com/Wynntils/Wynntils/compare/v2.4.10...v2.4.11) (2024-12-26)


### Bug Fixes

* More fixes for regional servers ([#3042](https://github.com/Wynntils/Wynntils/issues/3042)) ([7692b1b](https://github.com/Wynntils/Wynntils/commit/7692b1b18ef1892e862c6e01690804da19fa32fc))
* Update combat XP requirement values [skip ci] ([#3043](https://github.com/Wynntils/Wynntils/issues/3043)) ([8e591ec](https://github.com/Wynntils/Wynntils/commit/8e591ecd06a1c102653c64c8ec07f47c682943fb))

## [2.4.10](https://github.com/Wynntils/Wynntils/compare/v2.4.9...v2.4.10) (2024-12-23)


### New Features

* Add some basic support for regional servers [skip ci] ([#3038](https://github.com/Wynntils/Wynntils/issues/3038)) ([7983587](https://github.com/Wynntils/Wynntils/commit/79835876ad4b5deaffe0c4c6eecd84bbaf5205a9))


### Bug Fixes

* User-edited chat items with invalid powders causes crash ([#3030](https://github.com/Wynntils/Wynntils/issues/3030)) ([74c62ad](https://github.com/Wynntils/Wynntils/commit/74c62ad6d9f47b5b70183a62d56e9f1e02c993be))


### Miscellaneous Chores

* **release:** v2.4.10 [skip ci] ([504ae64](https://github.com/Wynntils/Wynntils/commit/504ae640d9560e89a471a0660b82aadbee74d34d))

## [2.4.9](https://github.com/Wynntils/Wynntils/compare/v2.4.8...v2.4.9) (2024-12-22)


### New Features

* Add consumable duration, ingredient effectiveness and charges/durability/duration modifier item filters [skip ci] ([#3039](https://github.com/Wynntils/Wynntils/issues/3039)) ([83625bc](https://github.com/Wynntils/Wynntils/commit/83625bc66638c0d14c2090284c8d97b6c350d52a))


### Bug Fixes

* Fix end of lootrun stats not being parsed ([#3041](https://github.com/Wynntils/Wynntils/issues/3041)) ([f4ce4b8](https://github.com/Wynntils/Wynntils/commit/f4ce4b8ef7643d3324d3496a0baa75b02e0d25cd))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#3036](https://github.com/Wynntils/Wynntils/issues/3036)) ([5764e9d](https://github.com/Wynntils/Wynntils/commit/5764e9db65c31a5c734a2958044f847eb8f705c8))
* **release:** v2.4.9 [skip ci] ([624b053](https://github.com/Wynntils/Wynntils/commit/624b053654ac8ec1e8cf20e326cf5bdbe1565234))

## [2.4.8](https://github.com/Wynntils/Wynntils/compare/v2.4.7...v2.4.8) (2024-12-20)


### New Features

* Add /characters and /char to command expansion [skip ci] ([#3020](https://github.com/Wynntils/Wynntils/issues/3020)) ([2af49b8](https://github.com/Wynntils/Wynntils/commit/2af49b8519f69ec1008fd72e5d3314f9c801b82b))
* add legacy options for the item stat colors + threshold [skip ci] ([#3014](https://github.com/Wynntils/Wynntils/issues/3014)) ([a70904b](https://github.com/Wynntils/Wynntils/commit/a70904b2d4c38b24cc78e0536ed118ddc619ef8a))
* Open item guide links on official item guide instead of data.wynntils.com [skip ci] ([#3023](https://github.com/Wynntils/Wynntils/issues/3023)) ([e997b37](https://github.com/Wynntils/Wynntils/commit/e997b3780605e67d9062b5792a63c81cfc902f89))
* Ported horse levelling time function from legacy [skip ci] ([#3008](https://github.com/Wynntils/Wynntils/issues/3008)) ([0eb5fe7](https://github.com/Wynntils/Wynntils/commit/0eb5fe74636a412c519cabfcf004175a0a029a26))
* Reimplement safe casting to quick casts [skip ci] ([#3026](https://github.com/Wynntils/Wynntils/issues/3026)) ([c22ad0b](https://github.com/Wynntils/Wynntils/commit/c22ad0b085fd0b5467d64c459dbd8249cc3fab25))
* Support critical damage bonus stat [skip ci] ([#3033](https://github.com/Wynntils/Wynntils/issues/3033)) ([7d79b66](https://github.com/Wynntils/Wynntils/commit/7d79b66bbe4b4e63580173df7173d7cfc6b60de9))


### Bug Fixes

* Don't count gathered xp again after refining [skip ci] ([#3032](https://github.com/Wynntils/Wynntils/issues/3032)) ([0b68221](https://github.com/Wynntils/Wynntils/commit/0b68221d60956ea54931408a2c409ba4960de639))
* Don't render own cape if option disabled [skip ci] ([#3022](https://github.com/Wynntils/Wynntils/issues/3022)) ([76604d8](https://github.com/Wynntils/Wynntils/commit/76604d8bdcd11b80ab5c65efa4d57b936bc90c38))
* Fix attack timer captured pattern [skip ci] ([#3029](https://github.com/Wynntils/Wynntils/issues/3029)) ([eecbae3](https://github.com/Wynntils/Wynntils/commit/eecbae3f3cfd4e1c0a57d55562cf2a498f657885))
* Fix auto attack interrupting queued spells [skip ci] ([#3021](https://github.com/Wynntils/Wynntils/issues/3021)) ([09963ca](https://github.com/Wynntils/Wynntils/commit/09963ca19e9fb26cbb21ed3e1c3edd3d2df5ccc0))
* Fix character selection state not being detected if user has no characters ([#3035](https://github.com/Wynntils/Wynntils/issues/3035)) ([590880d](https://github.com/Wynntils/Wynntils/commit/590880d228af5f1ca95890cf1fd32694e5265542))
* Fix quick cast not working with crafted weapons [skip ci] ([#3019](https://github.com/Wynntils/Wynntils/issues/3019)) ([c84858d](https://github.com/Wynntils/Wynntils/commit/c84858daddd27b23edb27837fd54d5919096bf5f))
* Ignore None guild resource value in filters [skip ci] ([#3018](https://github.com/Wynntils/Wynntils/issues/3018)) ([5020863](https://github.com/Wynntils/Wynntils/commit/5020863776ad802c947b14072ace009a227f295a))
* Make first press keybinds only trigger once [skip ci] ([#3013](https://github.com/Wynntils/Wynntils/issues/3013)) ([7ada5d8](https://github.com/Wynntils/Wynntils/commit/7ada5d86311378efc8a89494894adb85c9fbbd6e))
* Update Heart of the Pack (Altruism) range and name [skip ci] ([#3031](https://github.com/Wynntils/Wynntils/issues/3031)) ([9cfebae](https://github.com/Wynntils/Wynntils/commit/9cfebaea97d1caed451ad7ca53e63cc66540fce7))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#3034](https://github.com/Wynntils/Wynntils/issues/3034)) ([70a4326](https://github.com/Wynntils/Wynntils/commit/70a4326f17174e45517f2529d1b656f4fc434fbf))
* **release:** v2.4.8 [skip ci] ([a68e820](https://github.com/Wynntils/Wynntils/commit/a68e820be872191391005fb935f571fd89bd7132))

## [2.4.7](https://github.com/Wynntils/Wynntils/compare/v2.4.6...v2.4.7) (2024-12-07)


### Bug Fixes

* Fix quick casts blocking casts on world change [skip ci] ([#3015](https://github.com/Wynntils/Wynntils/issues/3015)) ([3062eff](https://github.com/Wynntils/Wynntils/commit/3062eff655b0ac7c376d2e42e8fc7d161a906414))
* More quick casting fixes ([#3016](https://github.com/Wynntils/Wynntils/issues/3016)) ([0d8b5b9](https://github.com/Wynntils/Wynntils/commit/0d8b5b9916e722951757708798039f78ee97b438))


### Miscellaneous Chores

* **release:** v2.4.7 [skip ci] ([dd168f5](https://github.com/Wynntils/Wynntils/commit/dd168f59d9dfde1b81a767921e101492ce7d9197))

