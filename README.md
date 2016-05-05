Skript is a Bukkit plugin which allows server admins to customize their server easily, but without the hassle of programming a plugin or asking/paying someone to program a plugin for them.

This repository is an unofficial fork of [Njol](https://github.com/Njol)'s [original code](https://github.com/Njol/Skript), maintained by [bensku](https://github.com/bensku) with new features and fixes by [many contributors](https://github.com/bensku/Skript/graphs/contributors) for 1.9+ support.

# Resources

* http://njol.ch/projects/skript - Official website
  * http://njol.ch/projects/skript/doc/ - Scripting documentation
  * http://njol.ch/projects/skript/API/ - Skript addon API
* https://forums.skunity.com/t/benskus-fork-skript-for-minecraft-1-9/4588 - Bensku's fork discussion thread
* https://github.com/bensku/Skript/releases - Bensku's releases

# Building

Skript uses Maven for dependency management and building. These instructions are simply for building a jar file of Skript. This is useful for use with CI servers (e.g. Jenkins) or for checking if the code builds in your development environment.

## Command line (Win/Linux)

*Assuming Maven is [installed to or available in PATH](https://maven.apache.org/install.html)*

1. Clone this repository using your git client (e.g. `git clone https://github.com/bensku/Skript.git`)
* Go into repository directory
* [Execute `mvn clean package`](https://gfycat.com/UnnaturalSpottedHectorsdolphin)
* [Built jar file will be located in the new `target` directory](http://i.imgur.com/nyyIyDn.png)

## Eclipse (Mars)

1. [Clone this repository using your git client](http://i.imgur.com/wKeTymd.png)
* In a new or blank Eclipse Workspace, go to `File > Import`
* [Under "Maven", select "Existing Maven Projects" and go Next](http://i.imgur.com/bpJWkZR.png)
* [Set "Root Directory" to the cloned repository directory, click "Refresh", ensure the
`pom.xml` file is checked and go Finish](http://i.imgur.com/58hbKJY.png)
* Go to `Run > Run Configurations...`
* [Right-click "Maven Build" and click "New", then configure as such:](http://i.imgur.com/2iaeyZw.png)
    * Set Name to "Build Skript JAR"
    * Set "Base Directory" to the repository's directory
    * Set "Goals" to `clean package` - This will make Maven clean the workspace, and then
    build the jar, on each build.
* Click "Apply", and then "Run"
* [Built JAR file will be located in the new `target` directory](http://i.imgur.com/pDNYZtm.png)
* For subsequent builds, go to `Run > Run History > Build Skript JAR`

## IntelliJ

1. [Clone this repository using your git client](http://i.imgur.com/wKeTymd.png)
* In IntelliJ, go to `File > Open`
* [Navigate to the repository and open the `pom.xml` file](http://i.imgur.com/v9k2q5U.png)
* [Look for and open the "Maven Projects" tab, expand "Skript" and then "Lifecycle"](http://i.imgur.com/WIL8vXU.png)
* [Double-click "Clean" and wait for the process to finish.](http://i.imgur.com/PGZqeJq.png)
This will ensure there are no left-over files from previous Maven builds that may
interfere with the final build.
* [Double-click "Package" and wait for the process to finish](http://i.imgur.com/AwRknXE.png)
* [Built Jar file will be located in the new `target` directory](http://i.imgur.com/Ihrh4zb.png)

# Debugging

These instructions are for running and debugging Skript from within your development environment. These will help you debug Skript and reload certain code changes as it runs. [Each of these steps assumes you have a Bukkit/Spigot/PaperSpigot server locally installed.](http://i.imgur.com/q0B28cR.png)

## Eclipse (Mars)

1. Build a JAR using the above instructions for Maven in Eclipse
* [Copy the jar to the plugins folder of your local server](http://i.imgur.com/5FgbMj5.png)
* [Follow these instructions to set up your local server and Eclipse for remote debugging](https://www.spigotmc.org/wiki/eclipse-debug-your-plugin/)

## IntelliJ

1. [Clone this repository using your git client](http://i.imgur.com/wKeTymd.png)
* In IntelliJ, go to `File > Open`
* [Navigate to the repository and open the `pom.xml` file](http://i.imgur.com/v9k2q5U.png)
* Go to `File > Project Structure... > Artifacts`
* [Click `Add > JAR > Empty`](http://i.imgur.com/9eQItpD.png), then [configure as such](http://i.imgur.com/2pY6aJR.png):
    * Set Name to "Skript"
    * Set Output directory to the "plugins" folder of your local server
    * Check "Build on make"
* [Right-click "'skript' compile output" and then click "Put into Output Root"](http://i.imgur.com/t3WqW5S.png), then click OK
* Go to `Run > Edit Configurations...`
* [Click `Add New Configuration > JAR Application`](http://i.imgur.com/gioO71B.png), then [configure as such](http://i.imgur.com/6YZpsjz.png):
    * Set Name to "Server" (or "Spigot" or "PaperSpigot", etc)
    * Set Path to JAR to the full path of your local server's executable JAR
        * e.g. `C:\Users\SkriptDev\AppData\Local\Programs\Spigot\spigot-1.9.2.jar`
    * Set VM options to "-Xmx2G -XX:MaxPermSize=128M" (allocates 2GB RAM)
    * Set Working directory to the full path of your local server
        * e.g. `C:\Users\SkriptDev\AppData\Local\Programs\Spigot\`
    * Checkmark "Single instance only" on the top right corner
* [Under "Before launch", click `Add New Configuration > Build Artifacts`](http://i.imgur.com/PdRE3L2.png)
* [Check "Skript" and then click OK twice](http://i.imgur.com/ELzOkmv.png)

After setting up IntelliJ for debugging, all you need to do is press SHIFT+F9 to begin debugging. [This will automatically build a jar, put it in your local server's plugins folder and then start your server automatically.](http://vanderprot.gamealition.com/img/3960e.mp4)
