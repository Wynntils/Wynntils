Artemis
========
[![Discord](https://discordapp.com/api/guilds/394189072635133952/widget.png)](https://discord.gg/ve49m9J)
[![CurseForge](https://cf.way2muchnoise.eu/short_wynntils.svg)](https://www.curseforge.com/minecraft/mc-mods/wynntils)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/Wynntils?label=modrinth)
[![GitHub](https://img.shields.io/github/downloads/Wynntils/Artemis/total?logo=github)](https://github.com/Wynntils/Artemis/releases)
[![License](https://img.shields.io/badge/license-AGPL%203.0-green.svg)](https://github.com/Wynntils/Artemis/blob/main/LICENSE)

<div align="center">
<img src="https://upload.wikimedia.org/wikipedia/commons/d/d2/Artemis.png" width=20%>

*(Image in public domain)*
</div>

> Artemis is the greek goddess of hunting and the moon, born of Zeus and Leto, twin of Apollo.

Artemis is a rewrite of **[Wynntils](https://github.com/Wynntils/Wynntils)** (informally referred to as "Legacy") in 1.19.3 using Architectury, to support **Fabric**, **Forge** and **Quilt**.

Downloading a release
========
You can download the latest build from our [releases page](https://github.com/Wynntils/Artemis/releases). You can also download the latest build from our [Modrinth Page](https://modrinth.com/mod/wynntils) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/wynntils).

Pull Requests
========
All pull requests are welcome. We'll analyse it and if we determine it should a part of the mod, we'll accept it. Note that the process of a pull request getting merged here is likely more strenuous than legacy. To begin, one pull request could be porting features from legacy.

We welcome all forms of assistance. =)

**Make sure you set the title of your pull request according to the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/#summary) specification. Otherwise, your pull request won't be visible in release logs, and will fail to auto-build.**

Workspace Setup
========

### Initial Setup
To set up the workspace, just import the project as a gradle project into your IDE. You might have to run `./gradlew --refresh-dependencies` if your IDE does not automatically do it.

### Building
To build the mod just run `./gradlew buildDependents` and the artifacts should be generated in `fabric/build/libs`, `quilt/build/libs` and `forge/build/libs`. There are a lot of jars there, use the jar which has the respective loader at the end (eg. `wynntils-VERSION-fabric.jar`).

### Code Formatting
The code format is checked by Spotless using the Palantir engine. When opening a PR, Spotless checks will be run automatically by GitHub Actions. This bot runs the `spotlessApply` which fixes all formatting errors that it can find. If such errors are found, the bot will then push a commit to your branch with these fixes.

However, it can be nice to fix the formatting locally before pushing. There are several ways to do this.

* The most basic is to run the same gradle target as the bot, `spotlessApply`. This can be done from the command line `./gradlew spotlessApply` or as a gradle target from your IDE.

* If you are using IntelliJ IDEA, you can install the [Spotless Gradle plugin](https://plugins.jetbrains.com/plugin/18321-spotless-gradle) to get an easy and quick way of running the Spotless target for the current editor.

* To make sure your commits always pass the Spotless check, you can install a git pre-commit hook. The hook is optional to use. If you want to use it, you must tell git to pick up hooks from the directory containing the hook. To do this, run this from your repo root: `git config core.hooksPath utils/git-hooks`.

* Finally, you can also use the Palantir formatting directly. This will skip the additional non-Palantir rules we have in Spotless, but these are few and unlikely to affect most cases. For IntelliJ, there is the [Palantir plugin](https://plugins.jetbrains.com/plugin/13180-palantir-java-format) that is supposed to do that, but at the moment it unfortunately seems to be broken.

### Hot-swapping
Using the Hotswap Agent is recommended if you want to do live code editing. Please note that at the moment, only version [Hotswap Agent 1.4.2](https://github.com/HotswapProjects/HotswapAgent/releases/tag/1.4.2-SNAPSHOT) works. See [Hotswap Agent installation instructions](http://hotswapagent.org/mydoc_quickstart-jdk17.html),
but bear in mind that the instructions are incorrect (!). Don't "unpack" `hotswap-agent.jar`, instead
rename the downloaded jar file to `hotswap-agent.jar`. Finally, add `wynntils.hotswap=true` in your personal `gradle.properties` file.
By default, this is `C:\Users\<your username>\.gradle\gradle.properties` on Windows, or `~/.gradle/gradle.properties` on Linux/MacOS.
If the file does not exist, you can create it yourself.
Don't forget to set the correct Java installation for your run configurations, and make sure to use [these](https://i.imgur.com/4VMFCM0.png) settings in IntelliJ IDEA.

#### Windows
If you are using Hotswap Agent on Windows and experience the "The process cannot access the file because it is being used by another process" error, you can replace your Architectury transformer runtime as a temporary fix. Check out and build [this pull request](https://github.com/architectury/architectury-transformer/pull/17). Then, find `dev.architectury:architectury-transformer:runtime:5.2.72` and open the folder containing the existing `.jar` in explorer. Copy the `.jar` file from the build output and replace the existing one.

### Run Configurations and Authenticating
Architectury Loom currently only supports VSCode and IntelliJ IDEA. Eclipse is not supported by upstream at the moment. After running Initial Setup, run configurations should appear automatically (note that you might have to restart your IDE after Initial Setup).

The project has [DevAuth](https://github.com/DJtheRedstoner/DevAuth) set up by default. When you run the development run configurations, you will get a link to log in with your Microsoft account. After first login, you will be able to run the game like you would in a production environment. You can use an alt configuration by specifying `-Ddevauth.account=alt` in your JVM options, or by temporarily changing `.devauth/config.toml`.

### Quiltflower decompiler
The project has [LoomQuiltflower](https://github.com/Juuxel/LoomQuiltflower) set-up automatically. This is done so to highly increase the quality of decompiled sources. To use it, run `./gradlew genSourcesWithQuiltflower`. After it finished, the decompiled Minecraft source will be in `minecraft-project-@common-merged-named-sources.jar` You have to attach these sources in Intellij IDEA for Quiltflower to take effect.

License
========

Artemis is licensed over the license [GNU Affero General Public License v3.0](https://github.com/Wynntils/Artemis/blob/alpha/LICENSE)

Unless specified otherwise, All the assets **are over Wynntils domain © Wynntils**.
