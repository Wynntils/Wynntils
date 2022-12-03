## [0.0.1-alpha.114](https://github.com/Wynntils/Artemis/compare/v0.0.1-alpha.113...v0.0.1-alpha.114) (2022-12-03)


### New Features

* Improve abstraction of redirected messages ([#765](https://github.com/Wynntils/Artemis/issues/765)) ([c0936a7](https://github.com/Wynntils/Artemis/commit/c0936a70be835088bf4238a1468d3789b7c06c88))

## [0.0.1-alpha.113](https://github.com/Wynntils/Artemis/compare/v0.0.1-alpha.112...v0.0.1-alpha.113) (2022-12-03)


### Miscellaneous Chores

* **release:** v0.0.1-alpha.113 [skip ci] ([be44587](https://github.com/Wynntils/Artemis/commit/be4458791100c0a37bd801b4ab247444d62583bb))
* Update README ([#764](https://github.com/Wynntils/Artemis/issues/764)) ([ad3e873](https://github.com/Wynntils/Artemis/commit/ad3e87397db11b7a2453f90bc547fd477a06dabf))

## [0.0.1-alpha.112](https://github.com/Wynntils/Artemis/compare/v0.0.1-alpha.111...v0.0.1-alpha.112) (2022-12-03)


### Bug Fixes

*  pre release ([3a64240](https://github.com/Wynntils/Artemis/commit/3a642408b6d5037f3032481cf5ffea95f68c9864))
* account for both types of boss bar name packets ([#674](https://github.com/Wynntils/Artemis/issues/674)) ([0ad85f2](https://github.com/Wynntils/Artemis/commit/0ad85f27b537dcd23126b2e446e62644afe0df5d))
* Add all ProfessionTypes ([#688](https://github.com/Wynntils/Artemis/issues/688)) ([4eaa961](https://github.com/Wynntils/Artemis/commit/4eaa9614c08b463403386711649cd2b1fc8ccaaa))
* Add correct dependency ([bc1dab0](https://github.com/Wynntils/Artemis/commit/bc1dab0073365017da320dbfcafefa48e69c4742))
* Add missing render previews for some overlays ([#678](https://github.com/Wynntils/Artemis/issues/678)) ([349f87b](https://github.com/Wynntils/Artemis/commit/349f87bf551b26d06d8ae573a9ee36ef4dc22efc))
* Add missing texture files ([3dacce2](https://github.com/Wynntils/Artemis/commit/3dacce24517e883407d37511cbcef0d8118c2c25))
* Allow clicks on locked emerald pouch and ability tree slots ([#726](https://github.com/Wynntils/Artemis/issues/726)) ([40a3744](https://github.com/Wynntils/Artemis/commit/40a3744dbccc46f1b64711d8343c245133fd5d1d))
* Auto-apply chat tabs on feature reload ([#689](https://github.com/Wynntils/Artemis/issues/689)) ([5f7c3a8](https://github.com/Wynntils/Artemis/commit/5f7c3a8be4ee084e21d8acf77c4bae634a7eeafe))
* BeaconBeamFeature not rendering correctly ([#638](https://github.com/Wynntils/Artemis/issues/638)) ([eb99b6e](https://github.com/Wynntils/Artemis/commit/eb99b6ed704e11c67b30ba4172541667176cb117))
* Catch all exceptions with readJson ([#711](https://github.com/Wynntils/Artemis/issues/711)) ([17e09b7](https://github.com/Wynntils/Artemis/commit/17e09b761c7b55ba140645b7d0ce5a09dfd0d562))
* Check if update has already been downloaded before downloading again ([#724](https://github.com/Wynntils/Artemis/issues/724)) ([9565ebc](https://github.com/Wynntils/Artemis/commit/9565ebcc867a1f2c67d14d5212b6d93461492d9d))
* correct chat encoding/decoding of spell cost ids ([#679](https://github.com/Wynntils/Artemis/issues/679)) ([d5d3eed](https://github.com/Wynntils/Artemis/commit/d5d3eed704da9d373a5a5e4b65a9f8a2779b2318))
* Disable feature as soon as possible on crash ([ffe3e21](https://github.com/Wynntils/Artemis/commit/ffe3e211e1329e36fa806aaf7c89d233c7f5da91))
* Do not expose MinecraftSchedulerManager queue ([#677](https://github.com/Wynntils/Artemis/issues/677)) ([c75274d](https://github.com/Wynntils/Artemis/commit/c75274d01571add57a7f06c937d7a2cfe07ce057))
* Do not render lootruns twice a frame ([5da0500](https://github.com/Wynntils/Artemis/commit/5da050034b142ed7a18ad92d279ce320870d1154))
* Do not scan quest book on entering world ([#706](https://github.com/Wynntils/Artemis/issues/706)) ([10426ab](https://github.com/Wynntils/Artemis/commit/10426abcfa15c0813adb9932b419a6476dfd70b1))
* Do not send data when player is in special states ([#667](https://github.com/Wynntils/Artemis/issues/667)) ([8b2c5e0](https://github.com/Wynntils/Artemis/commit/8b2c5e06a88358b6d126d19ff924683e3a03ec37))
* Don't trim custom commands ([b453ce5](https://github.com/Wynntils/Artemis/commit/b453ce562df93d31380ecbfe8c18e99a60fb5acd))
* emerald pouch parsing when picking emeralds up ([#723](https://github.com/Wynntils/Artemis/issues/723)) ([ab8752d](https://github.com/Wynntils/Artemis/commit/ab8752da9b26d9aa84226972fba5d8683b72ee07))
* Fix `allowClickOnEmeraldPouchInBlockingMode` config logic ([#755](https://github.com/Wynntils/Artemis/issues/755)) ([462e498](https://github.com/Wynntils/Artemis/commit/462e498bd7a38220b3b16b2a109db4a77d9177eb))
* Fix a bug which chat tabs that made action bar messages disappear ([2348416](https://github.com/Wynntils/Artemis/commit/234841608b47083c7a13b7dfcf1025cf49f2b1ba))
* Fix ability point coloring ([ea78a15](https://github.com/Wynntils/Artemis/commit/ea78a158ded5e7108a25d13f7a4117c9e8982c9d))
* Fix ability redirect regex to work in dialogues as well ([#651](https://github.com/Wynntils/Artemis/issues/651)) ([824da9d](https://github.com/Wynntils/Artemis/commit/824da9de3730014cae5e8fa1d9729d111d42b149))
* Fix AbilityTreeScrollFeature bug ([1e8cfa8](https://github.com/Wynntils/Artemis/commit/1e8cfa82746423f740be6465eb4f354eec0a0bfb))
* Fix auto chest not setting after there is an instance of a specific tier ([#747](https://github.com/Wynntils/Artemis/issues/747)) ([c595490](https://github.com/Wynntils/Artemis/commit/c595490da58682f4e688f5b92b06abcdd933ec63))
* Fix chat coordinate parsing ([ffc0fe9](https://github.com/Wynntils/Artemis/commit/ffc0fe9b5c7b5de22dabed8cf86ceb21a5d880a3))
* Fix chat history resetting ([#693](https://github.com/Wynntils/Artemis/issues/693)) ([8629d48](https://github.com/Wynntils/Artemis/commit/8629d4850fc11ab094cc13f454153e7007c307d6))
* Fix chat items not parsing + feature crash when screenshot copy fails ([#692](https://github.com/Wynntils/Artemis/issues/692)) ([be2f9a4](https://github.com/Wynntils/Artemis/commit/be2f9a4301730ba6eaf63f25733ce67e7733c694))
* Fix chat items resetting text colors after they appear ([#710](https://github.com/Wynntils/Artemis/issues/710)) ([b0595ce](https://github.com/Wynntils/Artemis/commit/b0595ce70c0e3f259c8dfef743283e6f36fc081f))
* fix chat tabs loading from config ([#690](https://github.com/Wynntils/Artemis/issues/690)) ([de4e2e1](https://github.com/Wynntils/Artemis/commit/de4e2e12e73232cd12d2207f2f95aeef63c314b1))
* Fix chat tabs resetting on config reload ([#705](https://github.com/Wynntils/Artemis/issues/705)) ([bdb0d62](https://github.com/Wynntils/Artemis/commit/bdb0d629855aa1632d0fd12c41c58e0d625dbe85))
* Fix CI build tasks ([f73fcf0](https://github.com/Wynntils/Artemis/commit/f73fcf05cc665d4a63ff3a5673a54787bce216ba))
* Fix colors disappearing when a message has chat items ([#699](https://github.com/Wynntils/Artemis/issues/699)) ([1a4dc59](https://github.com/Wynntils/Artemis/commit/1a4dc59a138cd8208643a6121dda3176c479d20b))
* Fix crash loops in feature exception ([#709](https://github.com/Wynntils/Artemis/issues/709)) ([66957bb](https://github.com/Wynntils/Artemis/commit/66957bbfc3d1109a34834b9f628ec74912733f10))
* Fix discovery queries ([d85c0e0](https://github.com/Wynntils/Artemis/commit/d85c0e0a5e3dd3f5f4cdb610e625e3a1c58cd7b9))
* Fix discovery screen api cache issues ([#703](https://github.com/Wynntils/Artemis/issues/703)) ([44c69bf](https://github.com/Wynntils/Artemis/commit/44c69bf2ecfb61fc3a225e725af08b09cbb83bcc))
* Fix Figura crash and black config screen ([#698](https://github.com/Wynntils/Artemis/issues/698)) ([a9f895e](https://github.com/Wynntils/Artemis/commit/a9f895e6aa53ac026e517667c79f4e2ccd37ee2b))
* Fix friend join redirect for archers ([65e3bba](https://github.com/Wynntils/Artemis/commit/65e3bba1cb2ee40de29123407b25a4d9686cc20d))
* Fix multiple issues with mini-quest summary ([#648](https://github.com/Wynntils/Artemis/issues/648)) ([18b9a6f](https://github.com/Wynntils/Artemis/commit/18b9a6f9d8d228387d56605f76672648d8936518))
* Fix multiple memory leaks ([#652](https://github.com/Wynntils/Artemis/issues/652)) ([6614dee](https://github.com/Wynntils/Artemis/commit/6614deeccfc6bd387f0678c2b456a6b5e4996fe9))
* Fix status overlay not parsing some statuses ([#683](https://github.com/Wynntils/Artemis/issues/683)) ([471ff88](https://github.com/Wynntils/Artemis/commit/471ff8805be067bd9ca1dcd61f6fc715fadf2ed1))
* Fix stencil mask buffer not clearing ([71e28a2](https://github.com/Wynntils/Artemis/commit/71e28a2d54354e9849d256c67e73380eec24410b))
* Give keybinds their default value on registration ([#740](https://github.com/Wynntils/Artemis/issues/740)) ([348a8c5](https://github.com/Wynntils/Artemis/commit/348a8c5a6244a46f27f1a4d882915b185f0c7a63))
* GuildTerritoryModel should account for partial updates ([#675](https://github.com/Wynntils/Artemis/issues/675)) ([49ff7c5](https://github.com/Wynntils/Artemis/commit/49ff7c5c4d55e9c3260ca84f0a20d7970f9785da))
* ignore duplicated bomb bell messages ([#713](https://github.com/Wynntils/Artemis/issues/713)) ([1d888ca](https://github.com/Wynntils/Artemis/commit/1d888caea2f7027f6208083112d1c0be044999d5))
* include savior's sacrifice in status overlay ([#729](https://github.com/Wynntils/Artemis/issues/729)) ([c069849](https://github.com/Wynntils/Artemis/commit/c069849f7f6fb924da8b464666ad74dad2eb0cec))
* Increase query timeout to make queries work when Wynncraft lags ([#642](https://github.com/Wynntils/Artemis/issues/642)) ([a7d06b3](https://github.com/Wynntils/Artemis/commit/a7d06b3d559b7b21dce079a697180ccd36e417ad))
* Label pois should not be behind ServicePois ([#730](https://github.com/Wynntils/Artemis/issues/730)) ([6be08d9](https://github.com/Wynntils/Artemis/commit/6be08d9fdcade17a5a583377f50f8b5a98f24714))
* Let the user know about failed item screenshots ([facffba](https://github.com/Wynntils/Artemis/commit/facffba28f85d146dcf48d11bc74089be4086b51))
* Lootrun recording fixes ([#680](https://github.com/Wynntils/Artemis/issues/680)) ([2b10b09](https://github.com/Wynntils/Artemis/commit/2b10b097953276503bf527acc44c9f9b55aa1fa7))
* Make coordinate parsing stricter ([#761](https://github.com/Wynntils/Artemis/issues/761)) ([15882cf](https://github.com/Wynntils/Artemis/commit/15882cf796f7422c6d9bc0e0c860c5ea831fae63))
* Make the mod loading fail gracefully if a feature couldn't init ([#645](https://github.com/Wynntils/Artemis/issues/645)) ([b4ce583](https://github.com/Wynntils/Artemis/commit/b4ce5831ee2c02ce45a583b583554393c1df75d3))
* Make unused ability point messages dark aqua ([e71ff49](https://github.com/Wynntils/Artemis/commit/e71ff496a52deb6723693380db73c9ce96f9199b))
* Make updating async ([#639](https://github.com/Wynntils/Artemis/issues/639)) ([606db70](https://github.com/Wynntils/Artemis/commit/606db700647f5a8506c5aad8f69cb8fba1233a65))
* Mana bank bar bugs ([#673](https://github.com/Wynntils/Artemis/issues/673)) ([7692538](https://github.com/Wynntils/Artemis/commit/7692538fe4e988a87fab15f48edcffb2722cfdcb))
* Mark and display development builds correctly  ([#641](https://github.com/Wynntils/Artemis/issues/641)) ([dbaea3d](https://github.com/Wynntils/Artemis/commit/dbaea3d74b4e889348e9a353da681f194077b967))
* Misc. spell cost ID fixes ([#684](https://github.com/Wynntils/Artemis/issues/684)) ([d5e30df](https://github.com/Wynntils/Artemis/commit/d5e30dfa2956ad4349fe0136873cb7ef36fb1be9))
* Render player shadows correctly when injecting nametags ([#666](https://github.com/Wynntils/Artemis/issues/666)) ([f29e57d](https://github.com/Wynntils/Artemis/commit/f29e57dd3b80873f39b59860ac1c6ef7f448ab40))
* RenderUtils should not have floating-point texture coordinates ([#731](https://github.com/Wynntils/Artemis/issues/731)) ([7606b44](https://github.com/Wynntils/Artemis/commit/7606b44ff7589aca2206168175b6c1817d0a4987))
* Respect autoTrackQuestCoordinates config ([#735](https://github.com/Wynntils/Artemis/issues/735)) ([0933c38](https://github.com/Wynntils/Artemis/commit/0933c38fc09e9aa081363b38ecb7e876d1f207fa))
* Rework how overlays positions work internally (and fix up some overlay positions) ([#708](https://github.com/Wynntils/Artemis/issues/708)) ([741a00f](https://github.com/Wynntils/Artemis/commit/741a00f316c8df56665fe2d65ccc8696ba5f6038))
* Save config on waypoint deletion ([#739](https://github.com/Wynntils/Artemis/issues/739)) ([90e1b3a](https://github.com/Wynntils/Artemis/commit/90e1b3a78c57d1aaaf2ad4785155c0e0468326d1))
* Show custom pois on minimap ([#737](https://github.com/Wynntils/Artemis/issues/737)) ([636fdf1](https://github.com/Wynntils/Artemis/commit/636fdf1677ed20bba493c5fd15219ccf94850b17))
* Show custom pois on minimap ([#737](https://github.com/Wynntils/Artemis/issues/737)) ([741fb34](https://github.com/Wynntils/Artemis/commit/741fb3461d80eb2d5e85db58660808bb74c65a7c))
* Support + in chat coordinate parsing ([c6611a7](https://github.com/Wynntils/Artemis/commit/c6611a792afe7bb22a19654eb8cf7924e6732ad1))
* Try to preserve invalid config file on invalid load ([#738](https://github.com/Wynntils/Artemis/issues/738)) ([dbd0ce0](https://github.com/Wynntils/Artemis/commit/dbd0ce08f84bc9197f41a02a0d78dbf53e994d37))
* update spell cost ID ranges ([#668](https://github.com/Wynntils/Artemis/issues/668)) ([eaf2e08](https://github.com/Wynntils/Artemis/commit/eaf2e0806b023535f622dafd225607f3494aae42))
* Use separate buffers for depth and stencil ([#670](https://github.com/Wynntils/Artemis/issues/670)) ([d01b11b](https://github.com/Wynntils/Artemis/commit/d01b11b2df0a9859a5be94f815d404c3ce0010cc))
* WynntilsDiscoveriesScreen should load web cache more often ([#676](https://github.com/Wynntils/Artemis/issues/676)) ([87dd520](https://github.com/Wynntils/Artemis/commit/87dd520dfe05bbf16460294330d925330e734dac))
* Zoom further and faster ([#733](https://github.com/Wynntils/Artemis/issues/733)) ([719fd60](https://github.com/Wynntils/Artemis/commit/719fd607a4cc1f7cb4f5559966024a51d19b0ab2))


### Build System

* **gradle:** ignore changelog file from spotless [skip ci] ([8d532f4](https://github.com/Wynntils/Artemis/commit/8d532f4ad2864d4c11959688fd78cecdc927c374))


### Code Refactoring

* Abstract map screen code ([#655](https://github.com/Wynntils/Artemis/issues/655)) ([f6c5e55](https://github.com/Wynntils/Artemis/commit/f6c5e55ba818107b238ba163316514b81ef56538))


### New Features

* Add /bombbell command ([#704](https://github.com/Wynntils/Artemis/issues/704)) ([7d14bb6](https://github.com/Wynntils/Artemis/commit/7d14bb6e087c296a0753824c9ab7ec4d57719dd2))
* Add /server list up to list servers with uptimes (and show uptime with /server info) ([#686](https://github.com/Wynntils/Artemis/issues/686)) ([bc660ed](https://github.com/Wynntils/Artemis/commit/bc660edd23d164fd876fdbd6b72539d852ee9594))
* Add a GUI to create custom map icons, and add helper buttons to maps ([#720](https://github.com/Wynntils/Artemis/issues/720)) ([469505e](https://github.com/Wynntils/Artemis/commit/469505eb28e19f614400ecd2231800ee1efe49ea))
* Add AuraTimerOverlayFeature ([#653](https://github.com/Wynntils/Artemis/issues/653)) ([30bfad8](https://github.com/Wynntils/Artemis/commit/30bfad8c0c05bc084a14208f2d806e3cd4fd0c38))
* Add center button to main map ([#734](https://github.com/Wynntils/Artemis/issues/734)) ([f42c0e7](https://github.com/Wynntils/Artemis/commit/f42c0e787169d1a8e664b6cb19471e38e0ffd0a3))
* Add Chat Tabs ([#671](https://github.com/Wynntils/Artemis/issues/671)) ([eac2136](https://github.com/Wynntils/Artemis/commit/eac2136847a32e8d59f2f630b27274cffb5bad28))
* Add chat timestamps ([#697](https://github.com/Wynntils/Artemis/issues/697)) ([365252e](https://github.com/Wynntils/Artemis/commit/365252e13632b8ef3e09caefcec599ac344c640d))
* Add command aliases feature ([#714](https://github.com/Wynntils/Artemis/issues/714)) ([6a31507](https://github.com/Wynntils/Artemis/commit/6a31507585a905e68abebf8287a29734b3c38f59))
* Add Discoveries Screen ([#646](https://github.com/Wynntils/Artemis/issues/646)) ([4c9a2c2](https://github.com/Wynntils/Artemis/commit/4c9a2c293ce43508427c1588d8b73c5935921910))
* Add gathering tool tier overlay ([#732](https://github.com/Wynntils/Artemis/issues/732)) ([ac18fcb](https://github.com/Wynntils/Artemis/commit/ac18fcbadc34e9fe233691b820cb7136b4cdee67))
* Add Guild Attack Timer overlay (and render attack timer on map) ([#649](https://github.com/Wynntils/Artemis/issues/649)) ([a0dcba8](https://github.com/Wynntils/Artemis/commit/a0dcba8e6bb99e63fece270524c70659a3be93f6))
* Add GuildMapScreen ([#657](https://github.com/Wynntils/Artemis/issues/657)) ([416ab01](https://github.com/Wynntils/Artemis/commit/416ab0118e59bfde409acbc4405c4ca63ccef166))
* Add I18n for UpdateCommand and UpdatesFeature ([#762](https://github.com/Wynntils/Artemis/issues/762)) ([89a7280](https://github.com/Wynntils/Artemis/commit/89a72807ed67c57ce4d85019d64d944cdab0f4df))
* Add off-screen indicator to WorldWaypointDistanceFeature ([#760](https://github.com/Wynntils/Artemis/issues/760)) ([6f08ee0](https://github.com/Wynntils/Artemis/commit/6f08ee0eba17bcd4086a0f9b0ae9c5d5f6af3c1d))
* Add player cursor to the guild map ([#681](https://github.com/Wynntils/Artemis/issues/681)) ([e08da46](https://github.com/Wynntils/Artemis/commit/e08da46f9ae6b1165d48c59f8196504a79f09f47))
* Add tier 10 emerald pouch to pouch guide ([#700](https://github.com/Wynntils/Artemis/issues/700)) ([69e95ba](https://github.com/Wynntils/Artemis/commit/69e95ba78b6eb69035980c809ce850a7db265a1f))
* added bps and bps_xz ([#721](https://github.com/Wynntils/Artemis/issues/721)) ([e319c24](https://github.com/Wynntils/Artemis/commit/e319c245b8c3f20ca1776887854b95d5d7b6e60b))
* added mana_pct function ([#718](https://github.com/Wynntils/Artemis/issues/718)) ([61d959d](https://github.com/Wynntils/Artemis/commit/61d959d01c211752a736ea6273d333422c38b1d8))
* Adds Fast Travel and Seaskipper Waypoints to Map ([#749](https://github.com/Wynntils/Artemis/issues/749)) ([a0547ba](https://github.com/Wynntils/Artemis/commit/a0547ba3f9209c145e5d06efdd5d32e04b691923))
* armor transparency ([#644](https://github.com/Wynntils/Artemis/issues/644)) ([a993da2](https://github.com/Wynntils/Artemis/commit/a993da2395a281db19a5efe47635e753cf6f653c))
* Auto create waypoints for chests ([#736](https://github.com/Wynntils/Artemis/issues/736)) ([ec91684](https://github.com/Wynntils/Artemis/commit/ec91684dbe9732b43fc8dab5aa7ef9086b2cbf88))
* Auto message territory defense on attack ([#650](https://github.com/Wynntils/Artemis/issues/650)) ([2c87967](https://github.com/Wynntils/Artemis/commit/2c87967f0fbaa8da2eb89b02baaef56e484da525))
* item stat decimal setting ([#663](https://github.com/Wynntils/Artemis/issues/663)) ([10ae76a](https://github.com/Wynntils/Artemis/commit/10ae76ad02c3a0ddda7152f49b0be0c821437ddb))
* Make chat tabs with unread messages yellow ([#696](https://github.com/Wynntils/Artemis/issues/696)) ([068b041](https://github.com/Wynntils/Artemis/commit/068b04198e2441e1b016ed3ed2f6bf6be090e2a2))
* Mob HP Formatting ([#661](https://github.com/Wynntils/Artemis/issues/661)) ([d787e25](https://github.com/Wynntils/Artemis/commit/d787e25736942e03be455af6a6c3f49a6ba7384c))
* Overwrite Territory Map button to open Guild Map ([#712](https://github.com/Wynntils/Artemis/issues/712)) ([4bfbb07](https://github.com/Wynntils/Artemis/commit/4bfbb07c4725c4761461453615806c17338bb9fb))
* Parse coordinates from chat and inject clickable component and add /compass share ([#742](https://github.com/Wynntils/Artemis/issues/742)) ([b0a1cab](https://github.com/Wynntils/Artemis/commit/b0a1cab8923e5088f0ed40744bffbffa30e28906))
* Parse defense in GuildAttackTimerOverlayFeature ([#656](https://github.com/Wynntils/Artemis/issues/656)) ([bb0d93a](https://github.com/Wynntils/Artemis/commit/bb0d93a8ae43d97a66c1e6a321515460b5f79f59))
* player armor hiding ([#664](https://github.com/Wynntils/Artemis/issues/664)) ([e67f2a0](https://github.com/Wynntils/Artemis/commit/e67f2a0037c9f0f738588e2c36249ffe1a30efbf))
* prevent trades and duels in combat ([#728](https://github.com/Wynntils/Artemis/issues/728)) ([5ac67a4](https://github.com/Wynntils/Artemis/commit/5ac67a4ffc50657d29303fb0908f6727a7b34712))
* Redirect "not enough mana", healing and speed effect messages ([#722](https://github.com/Wynntils/Artemis/issues/722)) ([9184386](https://github.com/Wynntils/Artemis/commit/9184386655db61674b969b27f0c11d488a694694))
* Redirect Blacksmith Sell/Scrap Messages to NotificationManager Overlay ([#743](https://github.com/Wynntils/Artemis/issues/743)) ([c330872](https://github.com/Wynntils/Artemis/commit/c3308723ee8b70ee33d313aecab07552c21d183d))
* Redirect Horse Despawning Messages to NotificationManager Overlay  ([#745](https://github.com/Wynntils/Artemis/issues/745)) ([075e589](https://github.com/Wynntils/Artemis/commit/075e5896c575da13e4c15d229b123dfa3955e4d3))
* Redirect Potion Stack Messages to NotificationManager Overlay ([#750](https://github.com/Wynntils/Artemis/issues/750)) ([c2f0ac6](https://github.com/Wynntils/Artemis/commit/c2f0ac660cc05ceeaf923bbf5365f39dab93efb9))
* Redirects Potion Replaced/At-Limit Messages as notifications ([#757](https://github.com/Wynntils/Artemis/issues/757)) ([fbbe1d8](https://github.com/Wynntils/Artemis/commit/fbbe1d8354a47629c4d0fd446914b9e66570929f))
* Redirects Shaman "No Totems Nearby" message to OverlayManager ([#758](https://github.com/Wynntils/Artemis/issues/758)) ([a23b69d](https://github.com/Wynntils/Artemis/commit/a23b69dff171d815dc71e36411d886a3732b1d76))
* Redirects Territory (Town) Entering/Leaving Messages to NotificationManager Overlay ([#753](https://github.com/Wynntils/Artemis/issues/753)) ([f7ca05a](https://github.com/Wynntils/Artemis/commit/f7ca05ac9e979542bb8b175c20424a9a15397eb6))
* Remove Quilted Fabric API dependency ([#744](https://github.com/Wynntils/Artemis/issues/744)) ([35ecb38](https://github.com/Wynntils/Artemis/commit/35ecb3811a8d7261a49342152869b0f6dd3a263c))
* Render guild territories on main map ([#643](https://github.com/Wynntils/Artemis/issues/643)) ([2d274f8](https://github.com/Wynntils/Artemis/commit/2d274f81f8ea8f7578ec9ed2b8bc033ab220203d))
* Render player POI's on minimap ([#754](https://github.com/Wynntils/Artemis/issues/754)) ([e6338df](https://github.com/Wynntils/Artemis/commit/e6338dfbee4b9c67ffb41ace5ed00a8bece7b47f))
* Stencils and support for compass distance ([#631](https://github.com/Wynntils/Artemis/issues/631)) ([d3d1ef1](https://github.com/Wynntils/Artemis/commit/d3d1ef1a71bebb0ab977e5bb7ed06f29e4a3cd99))
* Support overflowing health and mana in bars ([#707](https://github.com/Wynntils/Artemis/issues/707)) ([ada1b5f](https://github.com/Wynntils/Artemis/commit/ada1b5f1259819efb3288bd44dda6388eb23e0df))


### Miscellaneous Chores

* Add GHA for autopromotion to alpha ([e7cbd89](https://github.com/Wynntils/Artemis/commit/e7cbd8979874a87b175698ad08af1ebfe661f286))
* Add Quiltflower decompiler support and greatly improve workspace/development documentation ([#746](https://github.com/Wynntils/Artemis/issues/746)) ([9b01063](https://github.com/Wynntils/Artemis/commit/9b010632f929f80d411c78582e1d027a6bc990f5))
* Build ([b4f94c1](https://github.com/Wynntils/Artemis/commit/b4f94c197de2145ef09ae0e02b684d1961afd9f0))
* Rebuild broken quilt version ([4bd6e94](https://github.com/Wynntils/Artemis/commit/4bd6e94219f402a67e1e2e659786bbbc37d45778))
* Rebuild broken version [#2](https://github.com/Wynntils/Artemis/issues/2) ([02ee407](https://github.com/Wynntils/Artemis/commit/02ee407575615bcc06d01359554546e9b48c880d))
* Rebuild broken version [#3](https://github.com/Wynntils/Artemis/issues/3) ([109a8ba](https://github.com/Wynntils/Artemis/commit/109a8bab24d7461401ba2d5b685c202d1f927484))
* Rebuild for quilt ([7d558b0](https://github.com/Wynntils/Artemis/commit/7d558b01059360352d2cba48bb29b232f8825d48))
* Rebuild for quilt ([6aef47a](https://github.com/Wynntils/Artemis/commit/6aef47a0fd8f07a8a4b92f270fc352ab63416841))
* **release:** v0.0.1-alpha.112 [skip ci] ([41c9fa2](https://github.com/Wynntils/Artemis/commit/41c9fa282c2f508fb8f92f07187d3539fa0ad6d5))
* restore main version ([911c92e](https://github.com/Wynntils/Artemis/commit/911c92e4257d1647e70bda79223cb540d1155a86))
* test main -> alpha integration ([b947105](https://github.com/Wynntils/Artemis/commit/b947105015c63723ee7fe88239a8fc72bee472a3))
* Update gradle [skip ci] ([#727](https://github.com/Wynntils/Artemis/issues/727)) ([3425ece](https://github.com/Wynntils/Artemis/commit/3425ececbf59a080d2f361df99c7afebebb14ba7))

## [0.0.1-alpha.111](https://github.com/Wynntils/Artemis/compare/v0.0.1-alpha.110...v0.0.1-alpha.111) (2022-12-02)


### Bug Fixes

* Make coordinate parsing stricter ([#761](https://github.com/Wynntils/Artemis/issues/761)) ([b5ece79](https://github.com/Wynntils/Artemis/commit/b5ece79962330699c8c22007ec8b640b230a86bc))


### Miscellaneous Chores

* **release:** v0.0.1-alpha.111 [skip ci] ([e124426](https://github.com/Wynntils/Artemis/commit/e124426fa61fe757481d57666176baa6eaf3851a))

## [0.0.1-alpha.110](https://github.com/Wynntils/Artemis/compare/v0.0.1-alpha.109...v0.0.1-alpha.110) (2022-12-02)


### New Features

* Add I18n for UpdateCommand and UpdatesFeature ([#762](https://github.com/Wynntils/Artemis/issues/762)) ([3fb4c82](https://github.com/Wynntils/Artemis/commit/3fb4c828529f2ee63324afb6df175bd97105331d))


### Miscellaneous Chores

* **release:** v0.0.1-alpha.110 [skip ci] ([5f610a1](https://github.com/Wynntils/Artemis/commit/5f610a121763f1e5c706a5096257d8289d48617b))

