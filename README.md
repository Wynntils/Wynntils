<div align="center">
<img src="https://upload.wikimedia.org/wikipedia/commons/d/d2/Artemis.png" width=30%>
<p align="center"><i>(Image in public domain)</i></p>
<br>
<a href="https://discord.gg/ve49m9J"><img src="https://discordapp.com/api/guilds/394189072635133952/widget.png"></a>
<a href="https://ci.wynntils.com/job/Artemis/"><img src="http://ci.wynntils.com/buildStatus/icon?job=Artemis"></a>
<a href="https://github.com/Wynntils/Artemis/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-AGPL%203.0-green.svg"></a>
</div>

About Artemis
========
> Artemis is the greek goddess of hunting and the moon, born of Zeus and Leto, twin of Apollo.

This is a preliminary rewrite of **[Wynntils](https://github.com/Wynntils/Wynntils)** (informally referred to as "Legacy") in 1.18.2 using Architectury, to support Fabric, Forge and Quilt.

Pull Request
========
All pull requests are welcome. We'll analyse it and if we determine it should a part of the mod, we'll accept it. Note that the process of a pull request getting merged here is likely more strenuous than legacy. To begin, one pull request could be porting features from legacy.

We welcome all forms of assistance. =)

Workspace Setup
========
To set up the workspace, just import the project as a gradle project into your IDE
<br> To build the mod just call the ``buildDependents`` and the artifacts should be generated in `fabric/build/libs` and `forge/build/libs`. There are a lot of jars there, but the mod jars are the ones without a dashed suffix at the end.

The code format is checked by Spotless using the Palantir engine. To make sure your commits pass the Spotless check, you can install a git pre-commit hook. The hook is optional to use. If you want to use it, you must tell git to pick up hooks from the directory containing the hook. To do this, run this from your repo root: `git config core.hooksPath utils/git-hooks`.

If you are using IntelliJ IDEA, it is recommended to install the [Palantir plugin](https://plugins.jetbrains.com/plugin/13180-palantir-java-format), to get proper formatting using the "Reformat code" command.

Using the Hotswap Agent is recommended if you want to do live code editing. See [Hotswap Agent installation instructions](http://hotswapagent.org/mydoc_quickstart-jdk17.html),
but bear in mind that the instructions are incorrect (!). Don't "unpack" `hotswap-agent.jar`, instead
rename the downloaded jar file to `hotswap-agent.jar`. Finally, add `wynntils.hotswap=true` in your personal `gradle.properties` file.
By default, this is `C:\Users\<your username>\.gradle\gradle.properties` on Windows, or `~/.gradle/gradle.properties` on Linux/MacOS.


<i>TODO Run Configurations and Authenticating</i>

License
========

Artemis is licensed over the license [GNU Affero General Public License v3.0](https://github.com/Wynntils/Artemis/blob/development/LICENSE)<br>
Unless specified otherwise, All the assets **are over Wynntils domain © Wynntils**.
