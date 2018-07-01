# Skript [![Build Status](https://travis-ci.org/SkriptLang/Skript.svg?branch=master)](https://travis-ci.org/SkriptLang/Skript)
Skript is a plugin for Bukkit/Spigot, which allows server owners and other people
to modify their servers without learning Java. It can also be useful if you
*do* know Java; some tasks are quicker to do with Skript, and so it can be used
for prototyping etc.

This Github fork of Skript is based on Mirreski's improvements which was built
on Njol's original Skript. It is supported for **Spigot** (not Bukkit) versions of
Minecraft 1.9-1.12. Other versions might work, but no guarantees. **Paper** is
recommended, but not mandatory - without it, some features such as timings
will be not available.

## Documentation
Documentation is available [here](http://skriptlang.github.io/Skript/) for the
latest version of Skript.

## Reporting Issues
You should use Github [issue tracker](https://github.com/bensku/Skript/issues)
for all bug reports, feature requests and such. If you are not sure if something
is a bug, please still report it.

Please use the search to see if an issue has been reported already. Duplicates will be
closed and ignored. If the issue is indeed not yet reported, please use
common sense: what might a Skript developer need to know to solve your issue?

If issue has been reported before, and is open, you can comment to it to ask if there
has been progress or provide more information. If it has been closed, you can do
same thing, but then it might be good idea to ping `@bensku` and/or `@Snow-Pyon`
since closed issues are not often checked.

Finally, there is no guarantee that an issue will be resolved. Sometimes it might be
harder than it sounds to you; in other cases, no one has time to take a look at it.

If your having an aliases issue please report that
[here](https://github.com/tim740/skAliases/issues) instead.

## A Note About Addons
Skript developers cannot provide support for third-party addons of Skript. If you encounter issues
with them, contact the author of that addon. Also, when reporting issues which seem
to be unrelated to addons, you may be asked to test without any addons
(and you should do so to get your issue resolved).

That being said, if the **addon developer** thinks that some bug is caused by Skript,
they should report it. We just do not want everyone who has an issue with an addon to
clutter Skript's issue tracker; in *most* cases, we cannot do anything to help.

As a side note, I really, really, discourage making addons closed-source. After all,
Skript has been licensed under GPLv3 for ages...

## Compiling
Skript uses Gradle for compilation. Use your command prompt of preference and
navigate to Skript's source directory. Then you can just call Gradle to compile
and package Skript for you:

```bash
./gradlew clean build # on UNIX-based systems (mac, linux)
gradlew clean build # on Windows
```

You can get source code from the [releases](https://github.com/bensku/Skript/releases) page. You may also clone this
repository, but that code may or may not be stable.

### Importing to Eclipse
With new Eclipse versions, there is integrated Gradle support, and it actually works now.
So, first get latest Eclipse, then import Skript as any Gradle project. Just
make sure to **keep** the configuration when the importer asks for that!

If you encounter strange issues, make sure you follow the instructions above and have
actually downloaded latest Eclipse or update your installation correctly. Skript's
new Gradle version (starting from dev26) does not work very well with older Eclipse
versions. Also, do *not* use Gradle STS; it is outdated.

### Importing to IDEA
Skript relies heavily on use of nullness annotations and the way how Eclipse
interprets them. Thus, using IDEA is not easy. However, if you have truly
exceptional new features, I might be able to complete the code
with applicable nullness rules. Note that this really means *exceptional*;
adding some expressions and stuff like that do not count.

## Contributing
Code guidelines can be found below. You should also see CONTRIBUTING.md for
a lot of useful information.

## Code Guidelines
So, you want to work with Skript's codebase? There are a few guidelines for you:
* Understand Java as a language (Skript is not good learning project)
* Use Eclipse as your IDE for Skript *or* get your own IDE to support Eclipse's nullness annotations
* Not to alter Eclipse's nullness annotation settings; they affect even the compiler
* Use tabs as indentation (provided Eclipse settings will do this)
* Try to write code that looks similar to Skript's
* **DO NOT** use NMS code (Net Minecraft Server)

Of course, these are just recommendations. However, not following them may get
your pull requests rejected.

More clear code style guidelines are probably coming in future.

### Maven repository
If you use Skript as (soft) dependency for your plugin, and use maven or Gradle,
this is for you.

First, you need the repository. Skript is not available in Maven Central.
```
maven {
    url "https://raw.githubusercontent.com/bensku/mvn-repo/master"
}
```

Or, if you use Maven:
```
<repository>
    <id>bensku-repo</id>
    <url>https://raw.githubusercontent.com/bensku/mvn-repo/master</url>
</repository>
```

Then you will also need to add Skript as a dependency.
```
compile "ch.njol:skript:2.2-RELEASE_TAG"
```

Or, if you use Maven:
```
<dependency>
    <groupId>ch.njol</groupId>
    <artifactId>skript</artifactId>
    <version>2.2-RELEASE_TAG</version>
</dependency>
```

Note that these repositories are provided as-is, for now. I cannot currently spend time to add nice, but not mandatory, features like Javadoc.

## Relevant Links
* [SkUnity Forums](https://forums.skunity.com/)
* [Original Skript at BukkitDev](https://dev.bukkit.org/bukkit-plugins/skript/) (inactive)
* [Addon Releases @SkUnity](https://forums.skunity.com/forums/addon-releases/)
* [Skript Chat Discord Invite](https://discord.gg/0lx4QhQvwelCZbEX)
* [Skript Hub](https://skripthub.net/)

Note that these resources are not maintained by me. If you notice something wrong with them, do not contact me.

## Developers
Current team behind Skript:

* [bensku](https://github.com/bensku) (Skript maintainer/developer)
* [Snow-Pyon](https://github.com/Snow-Pyon) (Skript developer)
* [Pikachu920](https://github.com/Pikachu920) (Skript developer)
* [TheBentoBox](https://github.com/TheBentoBox) (issue tracker manager)

Also, of course, we should thank [Njol](https://github.com/Njol) for creating
Skript and [Mirreski](https://github.com/Mirreski) for maintaining it for a
long time.

In addition to that, Skript has received a lot of pull requests over time.
You can find all contributors [here](https://github.com/bensku/Skript/graphs/contributors).

All code is owned by it's writer, licensed for others under GPLv3 (see LICENSE)
unless otherwise specified.
