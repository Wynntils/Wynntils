## [2.4.9](https://github.com/Wynntils/Wynntils/compare/v2.4.8...v2.4.9) (2024-12-22)


### New Features

* Add consumable duration, ingredient effectiveness and charges/durability/duration modifier item filters [skip ci] ([#3039](https://github.com/Wynntils/Wynntils/issues/3039)) ([83625bc](https://github.com/Wynntils/Wynntils/commit/83625bc66638c0d14c2090284c8d97b6c350d52a))


### Bug Fixes

* Fix end of lootrun stats not being parsed ([#3041](https://github.com/Wynntils/Wynntils/issues/3041)) ([f4ce4b8](https://github.com/Wynntils/Wynntils/commit/f4ce4b8ef7643d3324d3496a0baa75b02e0d25cd))


### Miscellaneous Chores

* [auto-generated] Update urls.json [ci skip] ([#3036](https://github.com/Wynntils/Wynntils/issues/3036)) ([5764e9d](https://github.com/Wynntils/Wynntils/commit/5764e9db65c31a5c734a2958044f847eb8f705c8))

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

## [2.4.6](https://github.com/Wynntils/Wynntils/compare/v2.4.5...v2.4.6) (2024-12-06)


### New Features

* Add support for Commander Bar [skip ci] ([#3011](https://github.com/Wynntils/Wynntils/issues/3011)) ([9b83f5a](https://github.com/Wynntils/Wynntils/commit/9b83f5ac3bcf9db45fcc072649a70f4a05d9d9ac))
* change item stat color lerping [skip ci] ([#3009](https://github.com/Wynntils/Wynntils/issues/3009)) ([1b6a51d](https://github.com/Wynntils/Wynntils/commit/1b6a51da2c928b85a40d42df75f28d8f1a19f007))
* Rework quick casting ([#3012](https://github.com/Wynntils/Wynntils/issues/3012)) ([f6d7a1d](https://github.com/Wynntils/Wynntils/commit/f6d7a1df761fc232acddfc3656e71e3096542cab))


### Miscellaneous Chores

* **release:** v2.4.6 [skip ci] ([73c996b](https://github.com/Wynntils/Wynntils/commit/73c996bdbaa3134ff7f94dd64eff883d09b0abbd))

## [2.4.5](https://github.com/Wynntils/Wynntils/compare/v2.4.4...v2.4.5) (2024-12-03)


### Bug Fixes

* Fix quick casts and horse mounting using wrong player rotation ([#3010](https://github.com/Wynntils/Wynntils/issues/3010)) ([1fcb9f5](https://github.com/Wynntils/Wynntils/commit/1fcb9f50713fd278e960c88dab6a8835a7326ab9))


### Miscellaneous Chores

* **release:** v2.4.5 [skip ci] ([8e9224f](https://github.com/Wynntils/Wynntils/commit/8e9224f91a04f637c74b0e641f7af8678aa67309))

